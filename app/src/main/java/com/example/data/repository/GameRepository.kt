package com.example.data.repository

import com.example.data.local.InmateEntity
import com.example.data.local.MissionEntity
import com.example.data.local.OfficerEntity
import com.example.data.local.PlayerProfileEntity
import com.example.data.local.PoliceDao
import com.example.data.local.VehicleEntity
import com.example.data.model.ChiefTrait
import com.example.data.model.EquipmentType
import com.example.data.model.InmateStatus
import com.example.data.model.MissionStatus
import com.example.data.model.OfficerSpecialty
import com.example.data.model.PrisonUpgradeType
import com.example.data.model.TacticalAlertOption
import com.example.domain.GameCatalog
import com.example.domain.GameEngine
import com.example.domain.RecruitTemplate
import com.example.domain.VehicleTemplate
import kotlinx.coroutines.flow.Flow
import kotlin.random.Random

data class MissionResolutionResult(
    val missionTitle: String,
    val district: String,
    val isSuccess: Boolean,
    val suspectEscaped: Boolean,
    val escapeReason: String,
    val fugitiveSpawned: Boolean,
    val earnedMoney: Long,
    val earnedXp: Int,
    val suspectsArrested: Int,
    val inmatesBooked: Int = 0,
    val leveledUp: Boolean,
    val newLevel: Int,
    val dailyBonusAwarded: Long,
    val summaryMessage: String
)

sealed class ActionResult {
    data class Success(val message: String) : ActionResult()
    data class Error(val message: String) : ActionResult()
}

class GameRepository(private val dao: PoliceDao) {

    val profileFlow: Flow<PlayerProfileEntity?> = dao.observeProfile()
    val officersFlow: Flow<List<OfficerEntity>> = dao.observeOfficers()
    val vehiclesFlow: Flow<List<VehicleEntity>> = dao.observeVehicles()
    val missionsFlow: Flow<List<MissionEntity>> = dao.observeMissions()
    val inmatesFlow: Flow<List<InmateEntity>> = dao.observeInmates()

    suspend fun ensureInitialized() {
        val existing = dao.getProfileOnce()
        if (existing != null) return
        seedInitialGameData()
    }

    private suspend fun seedInitialGameData() {
        val starterProfile = PlayerProfileEntity(
            id = 1,
            money = 650L,
            xp = 0,
            level = 1,
            stationBuildingLevel = 1,
            capacityLevel = 1,
            radioLevel = 1,
            radarLevel = 1,
            safetyGearLevel = 1,
            commandCenterLevel = 1,
            missionsSucceeded = 0,
            missionsFailed = 0,
            totalEarnings = 650L,
            criminalsArrested = 0,
            criminalsEscaped = 0,
            dayNumber = 1
        )
        dao.upsertProfile(starterProfile)

        dao.insertOfficers(
            listOf(
                OfficerEntity(
                    name = "ستوان آرش کریمی",
                    specialty = OfficerSpecialty.PATROL.name,
                    level = 1,
                    basePower = 26
                ),
                OfficerEntity(
                    name = "استوار سارا محمدی",
                    specialty = OfficerSpecialty.TRAFFIC.name,
                    level = 1,
                    basePower = 24
                )
            )
        )

        dao.insertVehicle(
            VehicleEntity(
                modelKey = "sedan_cruiser",
                name = "سدان گشت کلانتری",
                level = 1,
                baseSpeedBonus = 12,
                basePowerBonus = 14
            )
        )

        val coords = mutableListOf<Pair<Float, Float>>()
        val starterMissions = (1..3).map { index ->
            val m = GameEngine.generateMission(
                playerLevel = 1,
                existingCoords = coords,
                forceEasy = true
            )
            coords.add(m.mapX to m.mapY)
            m.copy(createdAtMillis = System.currentTimeMillis() - index * 1000L)
        }
        dao.insertMissions(starterMissions)

        val now = System.currentTimeMillis()
        dao.insertInmate(
            InmateEntity(
                name = "بهزاد کریمیان",
                nickname = "قفل‌شکن",
                crimeTitle = "سرقت از فروشگاه",
                district = "بازار بزرگ",
                dangerLevel = 2,
                bailReward = 175L,
                sentenceSeconds = 42,
                detainedAtMillis = now,
                releaseAtMillis = now + 42_000L,
                status = InmateStatus.IN_CUSTODY.name,
                interrogated = false
            )
        )
    }

    suspend fun spawnNewIncidentIfSpace(): ActionResult {
        val profile = dao.getProfileOnce() ?: return ActionResult.Error("پروفایل بازی یافت نشد")
        val available = dao.getMissionsByStatus(MissionStatus.AVAILABLE.name)
        val inProgress = dao.getMissionsByStatus(MissionStatus.IN_PROGRESS.name)
        if (available.size >= 6) {
            return ActionResult.Error("در حال حاضر گزارش‌های کافی روی نقشه ثبت شده است.")
        }
        val existingCoords = (available + inProgress).map { it.mapX to it.mapY }
        val mission = GameEngine.generateMission(profile.level, existingCoords)
        dao.insertMission(mission)
        return ActionResult.Success("گزارش جدید دریافت شد: ${mission.title} در ${mission.district}")
    }

    /**
     * Checks unattended missions whose escape timer expired, and triggers exciting mid-mission
     * tactical radio alerts on active operations.
     */
    suspend fun processLiveCityEvents(nowMillis: Long): String? {
        val profile = dao.getProfileOnce() ?: return null
        var alertMessage: String? = null

        // 1. Check expired AVAILABLE missions (thief fled before dispatch!)
        val available = dao.getMissionsByStatus(MissionStatus.AVAILABLE.name)
        val expired = available.filter { it.expiresAtMillis > 0L && nowMillis >= it.expiresAtMillis }
        if (expired.isNotEmpty()) {
            var escapedCountDelta = 0
            val activeCoords = dao.getMissionsByStatus(MissionStatus.IN_PROGRESS.name).map { it.mapX to it.mapY }.toMutableList()
            available.filterNot { it in expired }.forEach { activeCoords.add(it.mapX to it.mapY) }

            for (mission in expired) {
                val reason = "تأخیر در اعزام نیرو: پیش از رسیدن مأموران، مجرم از محدوده ${mission.district} متواری شد!"
                dao.updateMission(
                    mission.copy(
                        status = MissionStatus.ESCAPED.name,
                        escapeReason = reason,
                        resultMessage = reason
                    )
                )
                escapedCountDelta += mission.suspectsCount.coerceAtLeast(1)
                alertMessage = "فرار مجرم! ${mission.title} در ${mission.district} به دلیل تأخیر در اعزام فرار کرد."

                // Spawn a follow-up hot pursuit if it wasn't already a fugitive mission
                if (!mission.isFugitivePursuit && Random.nextFloat() < 0.65f) {
                    val pursuit = GameEngine.generateFugitivePursuitMission(mission, profile.level, activeCoords)
                    dao.insertMission(pursuit)
                    activeCoords.add(pursuit.mapX to pursuit.mapY)
                }
            }

            dao.upsertProfile(
                profile.copy(
                    missionsFailed = profile.missionsFailed + expired.size,
                    criminalsEscaped = profile.criminalsEscaped + escapedCountDelta
                )
            )
            dao.trimOldMissionHistory()

            val remaining = dao.getMissionsByStatus(MissionStatus.AVAILABLE.name)
            if (remaining.size < 2) {
                spawnNewIncidentIfSpace()
            }
        }

        // 2. Trigger mid-mission tactical radio emergency when mission reaches 35%..75% progress
        val inProgress = dao.getMissionsByStatus(MissionStatus.IN_PROGRESS.name)
        for (mission in inProgress) {
            if (!mission.tacticalAlertActive && !mission.tacticalAlertResolved && mission.endTimeMillis > nowMillis) {
                val totalMs = (mission.endTimeMillis - mission.startTimeMillis).coerceAtLeast(1000L)
                val elapsedRatio = (nowMillis - mission.startTimeMillis).toFloat() / totalMs.toFloat()
                if (elapsedRatio in 0.35f..0.72f && mission.escapeRisk >= 15 && Random.nextFloat() < 0.24f) {
                    val text = GameEngine.generateTacticalAlertText(mission)
                    dao.updateMission(
                        mission.copy(
                            tacticalAlertActive = true,
                            tacticalAlertText = text
                        )
                    )
                    if (alertMessage == null) {
                        alertMessage = "پیام فوری بی‌سیم در مأموریت «${mission.title}»! دستور تاکتیکی بدهید."
                    }
                    break
                }
            }
        }

        // 3. Process Prison Breakout Alerts & Escalations
        val activeInmates = dao.getActiveInmatesOnce()
        val latestProfile = dao.getProfileOnce() ?: profile
        val expiredBreakouts = activeInmates.filter {
            it.status == InmateStatus.BREAKOUT_ALERT.name &&
                it.breakoutDeadlineMillis > 0L &&
                nowMillis >= it.breakoutDeadlineMillis
        }
        if (expiredBreakouts.isNotEmpty()) {
            val activeCoords = (dao.getMissionsByStatus(MissionStatus.AVAILABLE.name) +
                dao.getMissionsByStatus(MissionStatus.IN_PROGRESS.name))
                .map { it.mapX to it.mapY }
                .toMutableList()

            for (inmate in expiredBreakouts) {
                val reason = GameEngine.generatePrisonEscapedReason(inmate)
                val pursuit = GameEngine.generatePrisonBreakoutPursuit(inmate, latestProfile.level, activeCoords)
                val pursuitId = dao.insertMission(pursuit).toInt()
                dao.updateInmate(
                    inmate.copy(
                        status = InmateStatus.ESCAPED_PRISON.name,
                        breakoutReason = reason,
                        pursuitMissionId = pursuitId
                    )
                )
                activeCoords.add(pursuit.mapX to pursuit.mapY)
                alertMessage = "فرار از زندان! «${inmate.name}» از بازداشتگاه گریخت؛ فوری تعقیبش کنید!"
            }

            dao.upsertProfile(
                latestProfile.copy(criminalsEscaped = latestProfile.criminalsEscaped + expiredBreakouts.size)
            )
            dao.trimOldInmates()
        } else {
            val alreadyHasAlert = activeInmates.any { it.status == InmateStatus.BREAKOUT_ALERT.name }
            if (!alreadyHasAlert) {
                val candidates = activeInmates.filter {
                    it.status == InmateStatus.IN_CUSTODY.name &&
                        nowMillis < it.releaseAtMillis - 8_000L &&
                        nowMillis > it.detainedAtMillis + 7_000L
                }
                val candidate = candidates.randomOrNull()
                if (candidate != null) {
                    val riskPercent = GameEngine.inmateEscapeRiskPercent(candidate, latestProfile)
                    val breakoutChance = (riskPercent / 380f).coerceIn(0.02f, 0.22f)
                    if (Random.nextFloat() < breakoutChance) {
                        val reason = GameEngine.generatePrisonBreakoutAttemptReason(candidate)
                        dao.updateInmate(
                            candidate.copy(
                                status = InmateStatus.BREAKOUT_ALERT.name,
                                breakoutDeadlineMillis = nowMillis + 25_000L,
                                breakoutReason = reason
                            )
                        )
                        if (alertMessage == null) {
                            alertMessage = "آژیر قرمز زندان! «${candidate.name}» قصد فرار از سلول دارد؛ فوری مهار کنید!"
                        }
                    }
                }
            }
        }

        return alertMessage
    }

    suspend fun respondToTacticalAlert(missionId: Int, option: TacticalAlertOption): ActionResult {
        val profile = dao.getProfileOnce() ?: return ActionResult.Error("پروفایل یافت نشد")
        val mission = dao.getMissionById(missionId) ?: return ActionResult.Error("مأموریت یافت نشد")
        if (!mission.tacticalAlertActive || mission.status != MissionStatus.IN_PROGRESS.name) {
            return ActionResult.Error("این هشدار قبلاً رسیدگی شده است.")
        }

        if (option == TacticalAlertOption.HOT_PURSUIT && mission.assignedVehicleId == null) {
            return ActionResult.Error("برای تعقیب پرسرعت باید خودروی گشت در مأموریت حضور داشته باشد!")
        }

        if (option.costCoins > 0L) {
            if (profile.money < option.costCoins) {
                return ActionResult.Error("موجودی سکه برای پشتیبانی رادار کافی نیست.")
            }
            dao.upsertProfile(profile.copy(money = profile.money - option.costCoins))
        }

        val updatedSuccess = (mission.calculatedSuccessRate + option.successBoost).coerceAtMost(98)
        val updatedEscape = (mission.escapeRisk - option.escapeReduction).coerceAtLeast(3)
        val updatedEnd = mission.endTimeMillis + option.extraSeconds * 1000L

        dao.updateMission(
            mission.copy(
                calculatedSuccessRate = updatedSuccess,
                escapeRisk = updatedEscape,
                endTimeMillis = updatedEnd,
                tacticalAlertActive = false,
                tacticalAlertResolved = true
            )
        )
        return ActionResult.Success("دستور «${option.titleFa}» اجرا شد! شانس موفقیت به ${updatedSuccess}٪ رسید.")
    }

    suspend fun dispatchMission(
        missionId: Int,
        officerIds: List<Int>,
        vehicleId: Int?
    ): ActionResult {
        val profile = dao.getProfileOnce() ?: return ActionResult.Error("اطلاعات کلانتری یافت نشد")
        val mission = dao.getMissionById(missionId) ?: return ActionResult.Error("مأموریت یافت نشد")
        if (mission.status != MissionStatus.AVAILABLE.name) {
            return ActionResult.Error("این مأموریت دیگر در دسترس نیست.")
        }
        if (officerIds.isEmpty()) {
            return ActionResult.Error("حداقل یک مأمور برای اعزام انتخاب کنید.")
        }

        val allOfficers = dao.getOfficersOnce()
        val selectedOfficers = allOfficers.filter { it.id in officerIds && !it.isBusy }
        if (selectedOfficers.size != officerIds.size) {
            return ActionResult.Error("یکی از مأموران انتخاب‌شده در حال انجام مأموریت دیگری است.")
        }

        val selectedVehicle = vehicleId?.let { vid ->
            dao.getVehiclesOnce().firstOrNull { it.id == vid && !it.isBusy }
        }
        if (vehicleId != null && selectedVehicle == null) {
            return ActionResult.Error("خودروی انتخاب‌شده در حال حاضر آزاد نیست.")
        }

        val preview = GameEngine.calculateMissionPreview(
            mission = mission,
            selectedOfficers = selectedOfficers,
            selectedVehicle = selectedVehicle,
            profile = profile
        )

        val now = System.currentTimeMillis()
        val updatedMission = mission.copy(
            status = MissionStatus.IN_PROGRESS.name,
            assignedOfficerIds = selectedOfficers.joinToString(",") { it.id.toString() },
            assignedVehicleId = selectedVehicle?.id,
            calculatedSuccessRate = preview.successRate,
            escapeRisk = preview.escapeRisk,
            rewardMoney = preview.finalRewardMoney,
            durationSeconds = preview.finalDurationSeconds,
            startTimeMillis = now,
            endTimeMillis = now + preview.finalDurationSeconds * 1000L,
            tacticalAlertActive = false,
            tacticalAlertResolved = false
        )

        dao.updateMission(updatedMission)
        dao.setOfficersBusy(selectedOfficers.map { it.id }, true, missionId)
        if (selectedVehicle != null) {
            dao.setVehicleBusy(selectedVehicle.id, true, missionId)
        }

        return ActionResult.Success("نیروها به مأموریت «${mission.title}» اعزام شدند!")
    }

    suspend fun resolveMission(missionId: Int): MissionResolutionResult? {
        val profile = dao.getProfileOnce() ?: return null
        val mission = dao.getMissionById(missionId) ?: return null
        if (mission.status != MissionStatus.IN_PROGRESS.name) return null

        val officerIds = mission.assignedOfficerIds
            .split(",")
            .mapNotNull { it.trim().toIntOrNull() }

        val allOfficers = dao.getOfficersOnce()
        val assignedOfficers = allOfficers.filter { it.id in officerIds }
        val assignedVehicle = mission.assignedVehicleId?.let { vid ->
            dao.getVehiclesOnce().firstOrNull { it.id == vid }
        }

        // If the player ignored an active mid-mission tactical alert, escape risk rises!
        val effectiveSuccess = if (mission.tacticalAlertActive) {
            (mission.calculatedSuccessRate - 12).coerceAtLeast(10)
        } else {
            mission.calculatedSuccessRate
        }
        val effectiveEscapeRisk = if (mission.tacticalAlertActive) {
            (mission.escapeRisk + 18).coerceAtMost(90)
        } else {
            mission.escapeRisk
        }

        val roll = Random.nextInt(1, 101)
        val isSuccess = roll <= effectiveSuccess
        // Even on partial slip or failure, determine if the criminal actually escaped with a reason!
        val escapedRoll = Random.nextInt(1, 101)
        val suspectEscaped = !isSuccess && (mission.suspectsCount > 0 || escapedRoll <= effectiveEscapeRisk + 25)

        val earnedMoney: Long
        val earnedXp: Int
        val suspectsArrested: Int
        val suspectsEscapedCount: Int
        val escapeReason: String
        val summaryMessage: String
        var fugitiveSpawned = false

        if (isSuccess) {
            earnedMoney = mission.rewardMoney
            earnedXp = mission.rewardXp
            suspectsArrested = mission.suspectsCount
            suspectsEscapedCount = 0
            escapeReason = ""
            summaryMessage = if (suspectsArrested > 0) {
                "عملیات با موفقیت کامل انجام شد و تمام متهمان در ${mission.district} دستگیر شدند."
            } else {
                "مأموریت با موفقیت و آرامش کامل به پایان رسید و امنیت منطقه برقرار شد."
            }
            if (officerIds.isNotEmpty()) {
                dao.releaseOfficersWithCredit(officerIds)
            }
        } else if (suspectEscaped) {
            // Partial arrest possible if multiple suspects & team came close!
            val caughtSome = mission.suspectsCount >= 2 && roll <= effectiveSuccess + 18
            suspectsArrested = if (caughtSome) 1 else 0
            suspectsEscapedCount = (mission.suspectsCount - suspectsArrested).coerceAtLeast(1)

            escapeReason = if (mission.tacticalAlertActive) {
                "بی‌پاسخ ماندن پیام اضطراری بی‌سیم در میانه عملیات فرصت فرار را به سارق داد!"
            } else {
                GameEngine.determineEscapeReason(mission, assignedOfficers, assignedVehicle, profile)
            }

            earnedMoney = if (caughtSome) {
                (mission.rewardMoney * 0.45).toLong().coerceAtLeast(45L)
            } else {
                (mission.rewardMoney * 0.18).toLong().coerceAtLeast(25L)
            }
            earnedXp = (mission.rewardXp * 0.35).toInt().coerceAtLeast(10)
            summaryMessage = if (caughtSome) {
                "یک متهم بازداشت شد اما سرکرده سارقان توانست از صحنه فرار کند!"
            } else {
                "متهم توانست پیش از تکمیل محاصره از دست نیروها بگریزد!"
            }
            if (officerIds.isNotEmpty()) {
                dao.releaseOfficersWithoutCredit(officerIds)
            }
        } else {
            earnedMoney = (mission.rewardMoney * 0.25).toLong().coerceAtLeast(30L)
            earnedXp = (mission.rewardXp * 0.35).toInt().coerceAtLeast(10)
            suspectsArrested = 0
            suspectsEscapedCount = 0
            escapeReason = ""
            summaryMessage = "نیروها موفق به تکمیل کامل عملیات نشدند. برای موفقیت بیشتر، مأمور متخصص‌تر اعزام یا تجهیزات را ارتقا دهید."
            if (officerIds.isNotEmpty()) {
                dao.releaseOfficersWithoutCredit(officerIds)
            }
        }

        mission.assignedVehicleId?.let { vid ->
            dao.setVehicleBusy(vid, false, null)
        }

        val completedTotal = profile.missionsSucceeded + profile.missionsFailed + 1
        val newDay = 1 + (completedTotal / 3)
        val dailyBonus = if (newDay > profile.dayNumber) {
            (profile.stationBuildingLevel * 85L + profile.level * 30L)
        } else {
            0L
        }

        val totalCashGained = earnedMoney + dailyBonus
        val baseUpdatedProfile = profile.copy(
            money = profile.money + totalCashGained,
            totalEarnings = profile.totalEarnings + totalCashGained,
            missionsSucceeded = profile.missionsSucceeded + if (isSuccess) 1 else 0,
            missionsFailed = profile.missionsFailed + if (isSuccess) 0 else 1,
            criminalsArrested = profile.criminalsArrested + suspectsArrested,
            criminalsEscaped = profile.criminalsEscaped + suspectsEscapedCount,
            dayNumber = newDay
        )

        val (leveledProfile, leveledUp) = GameEngine.applyXpAndLevelUp(baseUpdatedProfile, earnedXp)
        val finalProfile = if (leveledUp) {
            val levelUpBonus = leveledProfile.level * 120L
            leveledProfile.copy(
                money = leveledProfile.money + levelUpBonus,
                totalEarnings = leveledProfile.totalEarnings + levelUpBonus
            )
        } else {
            leveledProfile
        }

        dao.upsertProfile(finalProfile)
        dao.updateMission(
            mission.copy(
                status = when {
                    isSuccess -> MissionStatus.COMPLETED_SUCCESS.name
                    suspectEscaped -> MissionStatus.ESCAPED.name
                    else -> MissionStatus.COMPLETED_FAIL.name
                },
                escapeReason = escapeReason,
                tacticalAlertActive = false,
                resultMessage = if (escapeReason.isNotBlank()) escapeReason else summaryMessage
            )
        )
        dao.trimOldMissionHistory()

        // Book arrested suspects into the City Prison up to cell capacity!
        var inmatesBooked = 0
        var overflowNotice = ""
        if (suspectsArrested > 0) {
            val currentActiveInmates = dao.getActiveInmatesOnce().size
            val maxCells = GameEngine.maxPrisonCapacity(finalProfile)
            val freeCells = (maxCells - currentActiveInmates).coerceAtLeast(0)
            val toBook = suspectsArrested.coerceAtMost(freeCells)
            val overflow = suspectsArrested - toBook
            val nowBooking = System.currentTimeMillis()

            if (toBook > 0) {
                val newInmates = (0 until toBook).map { idx ->
                    GameEngine.generateInmateFromMission(
                        mission = mission,
                        profile = finalProfile,
                        nowMillis = nowBooking + idx * 500L
                    )
                }
                dao.insertInmates(newInmates)
                inmatesBooked = toBook
            }
            if (overflow > 0) {
                val overflowBonus = overflow * 40L
                dao.upsertProfile(
                    finalProfile.copy(
                        money = finalProfile.money + overflowBonus,
                        totalEarnings = finalProfile.totalEarnings + overflowBonus
                    )
                )
                overflowNotice = " (ظرفیت سلول‌های زندان پر بود؛ $overflow متهم به بازداشتگاه مرکزی منتقل شدند)"
            }
        }

        // If a criminal escaped, spawn an exciting Fugitive Hot Pursuit bounty mission!
        val availableNow = dao.getMissionsByStatus(MissionStatus.AVAILABLE.name)
        val activeNow = dao.getMissionsByStatus(MissionStatus.IN_PROGRESS.name)
        val existingCoords = (availableNow + activeNow).map { it.mapX to it.mapY }

        if (suspectEscaped && !mission.isFugitivePursuit && availableNow.size < 6) {
            val pursuit = GameEngine.generateFugitivePursuitMission(mission, finalProfile.level, existingCoords)
            dao.insertMission(pursuit)
            fugitiveSpawned = true
        } else if (availableNow.size < 3) {
            spawnNewIncidentIfSpace()
        }

        return MissionResolutionResult(
            missionTitle = mission.title,
            district = mission.district,
            isSuccess = isSuccess,
            suspectEscaped = suspectEscaped,
            escapeReason = escapeReason,
            fugitiveSpawned = fugitiveSpawned,
            earnedMoney = earnedMoney,
            earnedXp = earnedXp,
            suspectsArrested = suspectsArrested,
            inmatesBooked = inmatesBooked,
            leveledUp = leveledUp,
            newLevel = finalProfile.level,
            dailyBonusAwarded = dailyBonus,
            summaryMessage = summaryMessage + overflowNotice
        )
    }

    suspend fun hireOfficer(template: RecruitTemplate): ActionResult {
        val profile = dao.getProfileOnce() ?: return ActionResult.Error("پروفایل یافت نشد")
        val officers = dao.getOfficersOnce()
        val maxCap = GameEngine.maxOfficerCapacity(profile)

        if (profile.level < template.unlockLevel) {
            return ActionResult.Error("برای استخدام این نیرو باید به سطح ${template.unlockLevel} برسید.")
        }
        if (officers.size >= maxCap) {
            return ActionResult.Error("ظرفیت کلانتری تکمیل است! از بخش کلانتری ظرفیت نیروها را افزایش دهید.")
        }
        if (profile.money < template.hireCost) {
            return ActionResult.Error("موجودی سکه کافی نیست.")
        }

        dao.upsertProfile(profile.copy(money = profile.money - template.hireCost))
        dao.insertOfficer(
            OfficerEntity(
                name = template.name,
                specialty = template.specialty.name,
                level = 1,
                basePower = template.basePower
            )
        )
        return ActionResult.Success("${template.name} به نیروهای کلانتری پیوست!")
    }

    suspend fun upgradeOfficer(officerId: Int): ActionResult {
        val profile = dao.getProfileOnce() ?: return ActionResult.Error("پروفایل یافت نشد")
        val officer = dao.getOfficersOnce().firstOrNull { it.id == officerId }
            ?: return ActionResult.Error("مأمور یافت نشد")
        if (officer.isBusy) {
            return ActionResult.Error("مأمور در حال انجام مأموریت است؛ پس از بازگشت ارتقا دهید.")
        }
        if (officer.level >= GameCatalog.MAX_OFFICER_LEVEL) {
            return ActionResult.Error("این مأمور به حداکثر سطح رسیده است.")
        }
        val cost = GameEngine.officerUpgradeCost(officer)
        if (profile.money < cost) {
            return ActionResult.Error("موجودی سکه برای ارتقا کافی نیست.")
        }
        dao.upsertProfile(profile.copy(money = profile.money - cost))
        dao.updateOfficer(officer.copy(level = officer.level + 1))
        return ActionResult.Success("${officer.name} به سطح ${officer.level + 1} ارتقا یافت!")
    }

    suspend fun buyVehicle(template: VehicleTemplate): ActionResult {
        val profile = dao.getProfileOnce() ?: return ActionResult.Error("پروفایل یافت نشد")
        if (profile.level < template.unlockLevel) {
            return ActionResult.Error("این خودرو در سطح ${template.unlockLevel} باز می‌شود.")
        }
        if (profile.money < template.cost) {
            return ActionResult.Error("موجودی سکه برای خرید خودرو کافی نیست.")
        }
        dao.upsertProfile(profile.copy(money = profile.money - template.cost))
        dao.insertVehicle(
            VehicleEntity(
                modelKey = template.modelKey,
                name = template.name,
                level = 1,
                baseSpeedBonus = template.baseSpeedBonus,
                basePowerBonus = template.basePowerBonus
            )
        )
        return ActionResult.Success("«${template.name}» به ناوگان گاراژ اضافه شد!")
    }

    suspend fun upgradeVehicle(vehicleId: Int): ActionResult {
        val profile = dao.getProfileOnce() ?: return ActionResult.Error("پروفایل یافت نشد")
        val vehicle = dao.getVehiclesOnce().firstOrNull { it.id == vehicleId }
            ?: return ActionResult.Error("خودرو یافت نشد")
        if (vehicle.isBusy) {
            return ActionResult.Error("این خودرو در مأموریت است؛ پس از بازگشت ارتقا دهید.")
        }
        if (vehicle.level >= GameCatalog.MAX_VEHICLE_LEVEL) {
            return ActionResult.Error("این خودرو به حداکثر سطح رسیده است.")
        }
        val cost = GameEngine.vehicleUpgradeCost(vehicle)
        if (profile.money < cost) {
            return ActionResult.Error("موجودی سکه کافی نیست.")
        }
        dao.upsertProfile(profile.copy(money = profile.money - cost))
        dao.updateVehicle(vehicle.copy(level = vehicle.level + 1))
        return ActionResult.Success("«${vehicle.name}» به سطح ${vehicle.level + 1} ارتقا یافت!")
    }

    suspend fun upgradeBuilding(): ActionResult {
        val profile = dao.getProfileOnce() ?: return ActionResult.Error("پروفایل یافت نشد")
        if (profile.stationBuildingLevel >= GameCatalog.MAX_BUILDING_LEVEL) {
            return ActionResult.Error("ساختمان کلانتری در بالاترین سطح قرار دارد.")
        }
        val cost = GameEngine.buildingUpgradeCost(profile.stationBuildingLevel)
        if (profile.money < cost) {
            return ActionResult.Error("موجودی سکه برای ارتقای ساختمان کافی نیست.")
        }
        val updated = profile.copy(
            money = profile.money - cost,
            stationBuildingLevel = profile.stationBuildingLevel + 1
        )
        dao.upsertProfile(updated)
        return ActionResult.Success("کلانتری به سطح ${updated.stationBuildingLevel} ارتقا یافت!")
    }

    suspend fun upgradeCapacity(): ActionResult {
        val profile = dao.getProfileOnce() ?: return ActionResult.Error("پروفایل یافت نشد")
        if (profile.capacityLevel >= GameCatalog.MAX_CAPACITY_LEVEL) {
            return ActionResult.Error("ظرفیت نیروها در حداکثر مقدار است.")
        }
        val cost = GameEngine.capacityUpgradeCost(profile.capacityLevel)
        if (profile.money < cost) {
            return ActionResult.Error("موجودی سکه برای افزایش ظرفیت کافی نیست.")
        }
        val updated = profile.copy(
            money = profile.money - cost,
            capacityLevel = profile.capacityLevel + 1
        )
        dao.upsertProfile(updated)
        return ActionResult.Success("ظرفیت پذیرش مأموران به ${GameEngine.maxOfficerCapacity(updated)} نفر افزایش یافت!")
    }

    suspend fun upgradeEquipment(type: EquipmentType): ActionResult {
        val profile = dao.getProfileOnce() ?: return ActionResult.Error("پروفایل یافت نشد")
        val currentLevel = GameEngine.equipmentLevel(profile, type)
        if (currentLevel >= type.maxLevel) {
            return ActionResult.Error("این تجهیزات به حداکثر سطح رسیده است.")
        }
        val cost = GameEngine.equipmentUpgradeCost(type, currentLevel)
        if (profile.money < cost) {
            return ActionResult.Error("موجودی سکه کافی نیست.")
        }
        val updated = when (type) {
            EquipmentType.RADIO -> profile.copy(money = profile.money - cost, radioLevel = currentLevel + 1)
            EquipmentType.RADAR -> profile.copy(money = profile.money - cost, radarLevel = currentLevel + 1)
            EquipmentType.SAFETY_GEAR -> profile.copy(money = profile.money - cost, safetyGearLevel = currentLevel + 1)
            EquipmentType.COMMAND_CENTER -> profile.copy(money = profile.money - cost, commandCenterLevel = currentLevel + 1)
        }
        dao.upsertProfile(updated)
        return ActionResult.Success("${type.titleFa} به سطح ${currentLevel + 1} ارتقا یافت!")
    }

    suspend fun interrogateInmate(inmateId: Int): ActionResult {
        val profile = dao.getProfileOnce() ?: return ActionResult.Error("پروفایل یافت نشد")
        val inmate = dao.getInmateById(inmateId) ?: return ActionResult.Error("زندانی یافت نشد")
        if (inmate.status != InmateStatus.IN_CUSTODY.name) {
            return ActionResult.Error("ابتدا وضعیت اضطراری این زندانی را مهار کنید.")
        }
        if (inmate.interrogated) {
            return ActionResult.Error("بازجویی و تخلیه اطلاعات این زندانی قبلاً انجام شده است.")
        }

        val bonusMoney = GameEngine.interrogationRewardMoney(inmate, profile)
        val bonusXp = 22 + inmate.dangerLevel * 6
        val (leveledProfile, _) = GameEngine.applyXpAndLevelUp(
            profile.copy(
                money = profile.money + bonusMoney,
                totalEarnings = profile.totalEarnings + bonusMoney
            ),
            bonusXp
        )
        dao.upsertProfile(leveledProfile)
        dao.updateInmate(inmate.copy(interrogated = true))

        val availableNow = dao.getMissionsByStatus(MissionStatus.AVAILABLE.name)
        var intelExtra = ""
        if (availableNow.size < 6 && Random.nextFloat() < 0.45f) {
            val existingCoords = (availableNow + dao.getMissionsByStatus(MissionStatus.IN_PROGRESS.name))
                .map { it.mapX to it.mapY }
            val leadMission = GameEngine.generateMission(leveledProfile.level, existingCoords)
            dao.insertMission(
                leadMission.copy(
                    rewardMoney = (leadMission.rewardMoney * 1.25).toLong(),
                    description = "سرنخ به‌دست‌آمده از بازجویی «${inmate.name}»: ${leadMission.description}"
                )
            )
            intelExtra = " + سرنخ مأموریت جدید روی نقشه ثبت شد!"
        }
        return ActionResult.Success("بازجویی موفق: +$bonusMoney سکه و +$bonusXp تجربه دریافت شد$intelExtra")
    }

    suspend fun transferInmate(inmateId: Int): ActionResult {
        val profile = dao.getProfileOnce() ?: return ActionResult.Error("پروفایل یافت نشد")
        val inmate = dao.getInmateById(inmateId) ?: return ActionResult.Error("زندانی یافت نشد")
        if (inmate.status != InmateStatus.IN_CUSTODY.name) {
            return ActionResult.Error("این زندانی در حال حاضر آماده تحویل نیست.")
        }
        if (System.currentTimeMillis() < inmate.releaseAtMillis) {
            return ActionResult.Error("تکمیل پرونده بازداشتگاه هنوز تمام نشده است.")
        }

        val xpGain = 26 + inmate.dangerLevel * 12
        val (leveledProfile, leveledUp) = GameEngine.applyXpAndLevelUp(
            profile.copy(
                money = profile.money + inmate.bailReward,
                totalEarnings = profile.totalEarnings + inmate.bailReward,
                inmatesTransferred = profile.inmatesTransferred + 1
            ),
            xpGain
        )
        dao.upsertProfile(leveledProfile)
        dao.updateInmate(inmate.copy(status = InmateStatus.TRANSFERRED.name))
        dao.trimOldInmates()

        val lvlNote = if (leveledUp) " • تبریک! ارتقا به سطح ${leveledProfile.level}" else ""
        return ActionResult.Success("«${inmate.name}» تحویل دادگاه شد! +${inmate.bailReward} سکه و +$xpGain تجربه$lvlNote")
    }

    suspend fun quellPrisonBreakout(inmateId: Int): ActionResult {
        val profile = dao.getProfileOnce() ?: return ActionResult.Error("پروفایل یافت نشد")
        val inmate = dao.getInmateById(inmateId) ?: return ActionResult.Error("زندانی یافت نشد")
        if (inmate.status != InmateStatus.BREAKOUT_ALERT.name) {
            return ActionResult.Error("هشدار فرار این سلول قبلاً برطرف شده است.")
        }

        val rewardMoney = 65L + inmate.dangerLevel * 22L
        val rewardXp = 32 + inmate.dangerLevel * 8
        val (leveledProfile, _) = GameEngine.applyXpAndLevelUp(
            profile.copy(
                money = profile.money + rewardMoney,
                totalEarnings = profile.totalEarnings + rewardMoney
            ),
            rewardXp
        )
        dao.upsertProfile(leveledProfile)
        dao.updateInmate(
            inmate.copy(
                status = InmateStatus.IN_CUSTODY.name,
                breakoutDeadlineMillis = 0L,
                breakoutReason = ""
            )
        )
        return ActionResult.Success("تلاش فرار «${inmate.name}» مهار شد! +$rewardMoney سکه و +$rewardXp تجربه")
    }

    suspend fun upgradePrison(type: PrisonUpgradeType): ActionResult {
        val profile = dao.getProfileOnce() ?: return ActionResult.Error("پروفایل یافت نشد")
        val currentLevel = GameEngine.prisonUpgradeLevel(profile, type)
        if (currentLevel >= type.maxLevel) {
            return ActionResult.Error("این بخش زندان در بالاترین سطح است.")
        }
        val cost = GameEngine.prisonUpgradeCost(type, currentLevel)
        if (profile.money < cost) {
            return ActionResult.Error("موجودی سکه برای توسعه زندان کافی نیست.")
        }
        val updated = when (type) {
            PrisonUpgradeType.CELL_BLOCKS -> profile.copy(
                money = profile.money - cost,
                prisonCapacityLevel = currentLevel + 1
            )
            PrisonUpgradeType.PRISON_SECURITY -> profile.copy(
                money = profile.money - cost,
                prisonSecurityLevel = currentLevel + 1
            )
            PrisonUpgradeType.INTERROGATION_WING -> profile.copy(
                money = profile.money - cost,
                prisonRehabLevel = currentLevel + 1
            )
        }
        dao.upsertProfile(updated)
        return ActionResult.Success("${type.titleFa} به سطح ${currentLevel + 1} ارتقا یافت!")
    }

    suspend fun inspectPrisonCellblock(): ActionResult {
        val profile = dao.getProfileOnce() ?: return ActionResult.Error("پروفایل یافت نشد")
        val activeInmates = dao.getActiveInmatesOnce()
        if (activeInmates.isEmpty()) {
            return ActionResult.Error("در حال حاضر زندانی فعالی در سلول‌ها برای بازرسی وجود ندارد.")
        }
        val existingAlert = activeInmates.firstOrNull { it.status == InmateStatus.BREAKOUT_ALERT.name }
        if (existingAlert != null) {
            return ActionResult.Error("آژیر فرار برای «${existingAlert.name}» فعال است! ابتدا او را مهار کنید.")
        }

        val target = activeInmates.maxByOrNull { it.dangerLevel } ?: activeInmates.first()
        val risk = GameEngine.inmateEscapeRiskPercent(target, profile)
        val roll = Random.nextInt(1, 101)
        val now = System.currentTimeMillis()

        return when {
            roll <= (risk * 0.55f).toInt().coerceAtLeast(22) -> {
                // High tension: prisoner bolts during inspection & spawns immediate City Pursuit!
                val activeCoords = (dao.getMissionsByStatus(MissionStatus.AVAILABLE.name) +
                    dao.getMissionsByStatus(MissionStatus.IN_PROGRESS.name))
                    .map { it.mapX to it.mapY }
                val pursuit = GameEngine.generatePrisonBreakoutPursuit(target, profile.level, activeCoords)
                val pursuitId = dao.insertMission(pursuit).toInt()
                val reason = GameEngine.generatePrisonEscapedReason(target)
                dao.updateInmate(
                    target.copy(
                        status = InmateStatus.ESCAPED_PRISON.name,
                        breakoutReason = reason,
                        pursuitMissionId = pursuitId
                    )
                )
                dao.upsertProfile(profile.copy(criminalsEscaped = profile.criminalsEscaped + 1))
                ActionResult.Success("فرار ناگهانی هنگام بازرسی! «${target.name}» به ${pursuit.district} گریخت؛ روی «تعقیب فوری» بزنید!")
            }

            roll <= (risk + 28).coerceAtMost(78) -> {
                // Caught red-handed mid-breakout attempt!
                val attemptReason = GameEngine.generatePrisonBreakoutAttemptReason(target)
                dao.updateInmate(
                    target.copy(
                        status = InmateStatus.BREAKOUT_ALERT.name,
                        breakoutDeadlineMillis = now + 24_000L,
                        breakoutReason = attemptReason
                    )
                )
                ActionResult.Success("بازرسی نقشه فرار «${target.name}» را لو داد! سریعاً دکمه مهار فرار را بزنید.")
            }

            else -> {
                val bonusMoney = 35L + profile.prisonSecurityLevel * 12L
                val (leveledProfile, _) = GameEngine.applyXpAndLevelUp(
                    profile.copy(
                        money = profile.money + bonusMoney,
                        totalEarnings = profile.totalEarnings + bonusMoney
                    ),
                    18
                )
                dao.upsertProfile(leveledProfile)
                ActionResult.Success("بازرسی امنیتی موفق بود؛ تمام سلول‌ها ایمن هستند (+$bonusMoney سکه و +۱۸ تجربه).")
            }
        }
    }

    suspend fun saveChiefProfile(
        name: String,
        callsign: String,
        avatarKey: String,
        trait: ChiefTrait,
        bio: String
    ): ActionResult {
        val profile = dao.getProfileOnce() ?: return ActionResult.Error("پروفایل بازی یافت نشد")
        val cleanName = name.trim()
        if (cleanName.length < 2) {
            return ActionResult.Error("لطفاً نام شخصیت فرمانده را (حداقل ۲ حرف) وارد کنید.")
        }
        val cleanCallsign = callsign.trim().ifBlank { "عقاب ۱" }
        val cleanBio = bio.trim().ifBlank { "حافظ امنیت و آرامش شهر" }

        dao.upsertProfile(
            profile.copy(
                chiefName = cleanName,
                chiefCallsign = cleanCallsign,
                chiefAvatarKey = avatarKey,
                chiefTrait = trait.name,
                chiefBio = cleanBio,
                profileConfigured = true
            )
        )
        return ActionResult.Success("پروفایل «$cleanName» با تخصص ${trait.titleFa} ذخیره شد!")
    }

    suspend fun resetGame() {
        dao.clearMissions()
        dao.clearOfficers()
        dao.clearVehicles()
        dao.clearInmates()
        dao.clearProfile()
        seedInitialGameData()
    }
}
