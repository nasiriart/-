package com.example.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.local.InmateEntity
import com.example.data.local.MissionEntity
import com.example.data.local.OfficerEntity
import com.example.data.local.PlayerProfileEntity
import com.example.data.local.PoliceDatabase
import com.example.data.local.VehicleEntity
import com.example.data.model.ChiefTrait
import com.example.data.model.EquipmentType
import com.example.data.model.InmateStatus
import com.example.data.model.MissionStatus
import com.example.data.model.PrisonUpgradeType
import com.example.data.model.TacticalAlertOption
import com.example.data.repository.ActionResult
import com.example.data.repository.GameRepository
import com.example.data.repository.MissionResolutionResult
import com.example.domain.RecruitTemplate
import com.example.domain.VehicleTemplate
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class GameUiState(
    val isLoading: Boolean = true,
    val profile: PlayerProfileEntity = PlayerProfileEntity(),
    val officers: List<OfficerEntity> = emptyList(),
    val vehicles: List<VehicleEntity> = emptyList(),
    val availableMissions: List<MissionEntity> = emptyList(),
    val activeMissions: List<MissionEntity> = emptyList(),
    val completedMissions: List<MissionEntity> = emptyList(),
    val activeInmates: List<InmateEntity> = emptyList(),
    val inmateHistory: List<InmateEntity> = emptyList(),
    val nowMillis: Long = System.currentTimeMillis()
) {
    val freeOfficersCount: Int get() = officers.count { !it.isBusy }
    val freeVehiclesCount: Int get() = vehicles.count { !it.isBusy }
    val readyToClaimCount: Int get() = activeMissions.count { nowMillis >= it.endTimeMillis }
    val urgentAlertsCount: Int get() = activeMissions.count { it.tacticalAlertActive && nowMillis < it.endTimeMillis }
    val breakoutAlertsCount: Int get() = activeInmates.count { it.status == InmateStatus.BREAKOUT_ALERT.name }
    val readyTransfersCount: Int get() = activeInmates.count {
        it.status == InmateStatus.IN_CUSTODY.name && nowMillis >= it.releaseAtMillis
    }
    val prisonBreakoutPursuits: List<MissionEntity>
        get() = (availableMissions + activeMissions).filter {
            it.title.startsWith("فراری از زندان:")
        }
}

class GameViewModel(application: Application) : AndroidViewModel(application) {

    private val repository: GameRepository = GameRepository(
        PoliceDatabase.getInstance(application).policeDao()
    )

    private val _nowMillis = MutableStateFlow(System.currentTimeMillis())

    private val _dispatchMissionId = MutableStateFlow<Int?>(null)
    val dispatchMissionId: StateFlow<Int?> = _dispatchMissionId.asStateFlow()

    private val _missionResultDialog = MutableStateFlow<MissionResolutionResult?>(null)
    val missionResultDialog: StateFlow<MissionResolutionResult?> = _missionResultDialog.asStateFlow()

    private val _feedbackMessages = MutableSharedFlow<String>(extraBufferCapacity = 8)
    val feedbackMessages = _feedbackMessages.asSharedFlow()

    private val coreDataFlow = combine(
        repository.profileFlow,
        repository.officersFlow,
        repository.vehiclesFlow,
        repository.missionsFlow,
        repository.inmatesFlow
    ) { profile, officers, vehicles, missions, inmates ->
        CoreGameBundle(profile, officers, vehicles, missions, inmates)
    }

    val uiState: StateFlow<GameUiState> = combine(
        coreDataFlow,
        _nowMillis
    ) { bundle, now ->
        val profile = bundle.profile
        if (profile == null) {
            GameUiState(isLoading = true, nowMillis = now)
        } else {
            val available = bundle.missions
                .filter { it.status == MissionStatus.AVAILABLE.name }
                .sortedWith(compareByDescending<MissionEntity> { it.isFugitivePursuit }.thenBy { it.expiresAtMillis })
            val active = bundle.missions
                .filter { it.status == MissionStatus.IN_PROGRESS.name }
                .sortedWith(compareByDescending<MissionEntity> { it.tacticalAlertActive }.thenBy { it.endTimeMillis })
            val completed = bundle.missions.filter {
                it.status == MissionStatus.COMPLETED_SUCCESS.name ||
                    it.status == MissionStatus.COMPLETED_FAIL.name ||
                    it.status == MissionStatus.ESCAPED.name
            }
            val activeInmates = bundle.inmates
                .filter {
                    it.status == InmateStatus.IN_CUSTODY.name ||
                        it.status == InmateStatus.BREAKOUT_ALERT.name
                }
                .sortedWith(
                    compareByDescending<InmateEntity> { it.status == InmateStatus.BREAKOUT_ALERT.name }
                        .thenBy { it.releaseAtMillis }
                )
            val inmateHistory = bundle.inmates.filter {
                it.status == InmateStatus.TRANSFERRED.name ||
                    it.status == InmateStatus.ESCAPED_PRISON.name
            }
            GameUiState(
                isLoading = false,
                profile = profile,
                officers = bundle.officers,
                vehicles = bundle.vehicles,
                availableMissions = available,
                activeMissions = active,
                completedMissions = completed,
                activeInmates = activeInmates,
                inmateHistory = inmateHistory,
                nowMillis = now
            )
        }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = GameUiState()
    )

    init {
        viewModelScope.launch {
            repository.ensureInitialized()
        }
        startClockAndIncidentTicker()
    }

    private fun startClockAndIncidentTicker() {
        viewModelScope.launch {
            var secondsSinceSpawn = 0
            var secondsSinceLiveCheck = 0
            while (true) {
                delay(1000L)
                val now = System.currentTimeMillis()
                _nowMillis.value = now
                secondsSinceSpawn++
                secondsSinceLiveCheck++

                if (secondsSinceLiveCheck >= 3) {
                    secondsSinceLiveCheck = 0
                    if (!uiState.value.isLoading) {
                        val eventMsg = repository.processLiveCityEvents(now)
                        if (eventMsg != null) {
                            _feedbackMessages.tryEmit(eventMsg)
                        }
                    }
                }

                if (secondsSinceSpawn >= 24) {
                    secondsSinceSpawn = 0
                    val currentState = uiState.value
                    if (!currentState.isLoading && currentState.availableMissions.size < 4) {
                        repository.spawnNewIncidentIfSpace()
                    }
                }
            }
        }
    }

    fun openDispatchSheet(missionId: Int) {
        _dispatchMissionId.value = missionId
    }

    fun closeDispatchSheet() {
        _dispatchMissionId.value = null
    }

    fun dismissMissionResult() {
        _missionResultDialog.value = null
    }

    fun scanForNewCrime() {
        viewModelScope.launch {
            handleActionResult(repository.spawnNewIncidentIfSpace())
        }
    }

    fun dispatchMission(missionId: Int, officerIds: List<Int>, vehicleId: Int?) {
        viewModelScope.launch {
            val res = repository.dispatchMission(missionId, officerIds, vehicleId)
            if (res is ActionResult.Success) {
                _dispatchMissionId.value = null
            }
            handleActionResult(res)
        }
    }

    fun respondToTacticalAlert(missionId: Int, option: TacticalAlertOption) {
        viewModelScope.launch {
            handleActionResult(repository.respondToTacticalAlert(missionId, option))
        }
    }

    fun claimMissionResult(missionId: Int) {
        viewModelScope.launch {
            val result = repository.resolveMission(missionId)
            if (result != null) {
                _missionResultDialog.value = result
            }
        }
    }

    fun hireOfficer(template: RecruitTemplate) {
        viewModelScope.launch {
            handleActionResult(repository.hireOfficer(template))
        }
    }

    fun upgradeOfficer(officerId: Int) {
        viewModelScope.launch {
            handleActionResult(repository.upgradeOfficer(officerId))
        }
    }

    fun buyVehicle(template: VehicleTemplate) {
        viewModelScope.launch {
            handleActionResult(repository.buyVehicle(template))
        }
    }

    fun upgradeVehicle(vehicleId: Int) {
        viewModelScope.launch {
            handleActionResult(repository.upgradeVehicle(vehicleId))
        }
    }

    fun upgradeBuilding() {
        viewModelScope.launch {
            handleActionResult(repository.upgradeBuilding())
        }
    }

    fun upgradeCapacity() {
        viewModelScope.launch {
            handleActionResult(repository.upgradeCapacity())
        }
    }

    fun upgradeEquipment(type: EquipmentType) {
        viewModelScope.launch {
            handleActionResult(repository.upgradeEquipment(type))
        }
    }

    fun interrogateInmate(inmateId: Int) {
        viewModelScope.launch {
            handleActionResult(repository.interrogateInmate(inmateId))
        }
    }

    fun transferInmate(inmateId: Int) {
        viewModelScope.launch {
            handleActionResult(repository.transferInmate(inmateId))
        }
    }

    fun quellPrisonBreakout(inmateId: Int) {
        viewModelScope.launch {
            handleActionResult(repository.quellPrisonBreakout(inmateId))
        }
    }

    fun upgradePrison(type: PrisonUpgradeType) {
        viewModelScope.launch {
            handleActionResult(repository.upgradePrison(type))
        }
    }

    fun inspectPrisonCellblock() {
        viewModelScope.launch {
            handleActionResult(repository.inspectPrisonCellblock())
        }
    }

    fun saveChiefProfile(
        name: String,
        callsign: String,
        avatarKey: String,
        trait: ChiefTrait,
        bio: String
    ) {
        viewModelScope.launch {
            handleActionResult(
                repository.saveChiefProfile(
                    name = name,
                    callsign = callsign,
                    avatarKey = avatarKey,
                    trait = trait,
                    bio = bio
                )
            )
        }
    }

    fun resetCareer() {
        viewModelScope.launch {
            repository.resetGame()
            _dispatchMissionId.value = null
            _missionResultDialog.value = null
            _feedbackMessages.tryEmit("بازی با موفقیت از ابتدا شروع شد.")
        }
    }

    private fun handleActionResult(result: ActionResult) {
        when (result) {
            is ActionResult.Success -> _feedbackMessages.tryEmit(result.message)
            is ActionResult.Error -> _feedbackMessages.tryEmit(result.message)
        }
    }
}

private data class CoreGameBundle(
    val profile: PlayerProfileEntity?,
    val officers: List<OfficerEntity>,
    val vehicles: List<VehicleEntity>,
    val missions: List<MissionEntity>,
    val inmates: List<InmateEntity>
)
