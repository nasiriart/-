package com.example.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "player_profile")
data class PlayerProfileEntity(
    @PrimaryKey val id: Int = 1,
    val money: Long = 650L,
    val xp: Int = 0,
    val level: Int = 1,
    val stationBuildingLevel: Int = 1,
    val capacityLevel: Int = 1,
    val radioLevel: Int = 1,
    val radarLevel: Int = 1,
    val safetyGearLevel: Int = 1,
    val commandCenterLevel: Int = 1,
    val missionsSucceeded: Int = 0,
    val missionsFailed: Int = 0,
    val totalEarnings: Long = 650L,
    val criminalsArrested: Int = 0,
    val criminalsEscaped: Int = 0,
    val dayNumber: Int = 1,
    val prisonCapacityLevel: Int = 1,
    val prisonSecurityLevel: Int = 1,
    val prisonRehabLevel: Int = 1,
    val inmatesTransferred: Int = 0,
    val chiefName: String = "فرمانده کلانتری",
    val chiefCallsign: String = "عقاب ۱",
    val chiefAvatarKey: String = "tactical",
    val chiefTrait: String = "TACTICAL_LEADER",
    val chiefBio: String = "حافظ امنیت و آرامش شهر",
    val profileConfigured: Boolean = false
)

@Entity(tableName = "officers")
data class OfficerEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val name: String,
    val specialty: String,
    val level: Int = 1,
    val basePower: Int = 24,
    val isBusy: Boolean = false,
    val assignedMissionId: Int? = null,
    val missionsCompleted: Int = 0
)

@Entity(tableName = "vehicles")
data class VehicleEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val modelKey: String,
    val name: String,
    val level: Int = 1,
    val baseSpeedBonus: Int,
    val basePowerBonus: Int,
    val isBusy: Boolean = false,
    val assignedMissionId: Int? = null
)

@Entity(tableName = "missions")
data class MissionEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val crimeType: String,
    val title: String,
    val description: String,
    val district: String,
    val mapX: Float,
    val mapY: Float,
    val difficulty: Int,
    val requiredOfficers: Int,
    val requiredPower: Int,
    val rewardMoney: Long,
    val rewardXp: Int,
    val durationSeconds: Int,
    val suspectsCount: Int,
    val status: String,
    val assignedOfficerIds: String = "",
    val assignedVehicleId: Int? = null,
    val calculatedSuccessRate: Int = 0,
    val escapeRisk: Int = 20,
    val expiresAtMillis: Long = 0L,
    val escapeReason: String = "",
    val isFugitivePursuit: Boolean = false,
    val tacticalAlertActive: Boolean = false,
    val tacticalAlertText: String = "",
    val tacticalAlertResolved: Boolean = false,
    val startTimeMillis: Long = 0L,
    val endTimeMillis: Long = 0L,
    val resultMessage: String = "",
    val createdAtMillis: Long = System.currentTimeMillis()
)

@Entity(tableName = "inmates")
data class InmateEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val name: String,
    val nickname: String,
    val crimeTitle: String,
    val district: String,
    val dangerLevel: Int,
    val bailReward: Long,
    val sentenceSeconds: Int,
    val detainedAtMillis: Long,
    val releaseAtMillis: Long,
    val status: String,
    val interrogated: Boolean = false,
    val breakoutDeadlineMillis: Long = 0L,
    val breakoutReason: String = "",
    val pursuitMissionId: Int? = null
)
