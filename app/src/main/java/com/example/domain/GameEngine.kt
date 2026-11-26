package com.example.domain

import com.example.data.local.InmateEntity
import com.example.data.local.MissionEntity
import com.example.data.local.OfficerEntity
import com.example.data.local.PlayerProfileEntity
import com.example.data.local.VehicleEntity
import com.example.data.model.ChiefTrait
import com.example.data.model.CrimeType
import com.example.data.model.EquipmentType
import com.example.data.model.InmateStatus
import com.example.data.model.MissionStatus
import com.example.data.model.OfficerSpecialty
import com.example.data.model.PrisonUpgradeType
import kotlin.math.roundToInt
import kotlin.random.Random

data class DispatchPreview(
    val successRate: Int,
    val escapeRisk: Int,
    val finalDurationSeconds: Int,
    val finalRewardMoney: Long,
    val teamPower: Int,
    val specialtyMatches: Int,
    val tacticalWarnings: List<String>
)

object GameEngine {

    fun officerPower(officer: OfficerEntity): Int =
        officer.basePower + (officer.level - 1) * 14

    fun officerUpgradeCost(officer: OfficerEntity): Long =
        (140L + officer.basePower * 3L) * officer.level

    fun vehicleSpeedBonus(vehicle: VehicleEntity): Int =
        (vehicle.baseSpeedBonus + (vehicle.level - 1) * 3).coerceAtMost(55)

    fun vehiclePowerBonus(vehicle: VehicleEntity): Int =
        vehicle.basePowerBonus + (vehicle.level - 1) * 10

    fun vehicleUpgradeCost(vehicle: VehicleEntity): Long =
        (220L + vehicle.basePowerBonus * 6L) * vehicle.level

    fun maxOfficerCapacity(profile: PlayerProfileEntity): Int {
        val levelBonus = profile.level / 4
        return 3 + (profile.capacityLevel - 1) * 2 + levelBonus
    }

    fun buildingUpgradeCost(currentLevel: Int): Long =
        (450L * currentLevel * currentLevel * 0.65).roundToInt().toLong().coerceAtLeast(450L)

    fun capacityUpgradeCost(currentLevel: Int): Long =
        (360L * currentLevel * currentLevel * 0.60).roundToInt().toLong().coerceAtLeast(360L)

    fun equipmentUpgradeCost(type: EquipmentType, currentLevel: Int): Long =
        (type.baseCost * currentLevel * (1.0 + (currentLevel - 1) * 0.25)).roundToInt().toLong()

    fun equipmentLevel(profile: PlayerProfileEntity, type: EquipmentType): Int = when (type) {
        EquipmentType.RADIO -> profile.radioLevel
        EquipmentType.RADAR -> profile.radarLevel
        EquipmentType.SAFETY_GEAR -> profile.safetyGearLevel
        EquipmentType.COMMAND_CENTER -> profile.commandCenterLevel
    }

    fun maxPrisonCapacity(profile: PlayerProfileEntity): Int =
        4 + (profile.prisonCapacityLevel - 1) * 3

    fun prisonUpgradeLevel(profile: PlayerProfileEntity, type: PrisonUpgradeType): Int = when (type) {
        PrisonUpgradeType.CELL_BLOCKS -> profile.prisonCapacityLevel
        PrisonUpgradeType.PRISON_SECURITY -> profile.prisonSecurityLevel
        PrisonUpgradeType.INTERROGATION_WING -> profile.prisonRehabLevel
    }

    fun prisonUpgradeCost(type: PrisonUpgradeType, currentLevel: Int): Long =
        (type.baseCost * currentLevel * (1.0 + (currentLevel - 1) * 0.25)).roundToInt().toLong()

    fun prisonSecurityRating(profile: PlayerProfileEntity): Int =
        (54 + (profile.prisonSecurityLevel - 1) * 5).coerceIn(50, 98)

    fun inmateEscapeRiskPercent(inmate: InmateEntity, profile: PlayerProfileEntity): Int {
        val base = 16 + inmate.dangerLevel * 12 - (profile.prisonSecurityLevel - 1) * 6
        val traitAdj = if (ChiefTrait.fromName(profile.chiefTrait) == ChiefTrait.DETECTIVE_MIND) -6 else 0
        return (base + traitAdj).coerceIn(6, 85)
    }

    fun generatePrisonBreakoutAttemptReason(inmate: InmateEntity): String {
        val reasons = listOf(
            "«${inmate.name}» (${inmate.nickname}) با ابزار مخفی در حال باز کردن قفل درب سلول است!",
            "هشدار دوربین بند: «${inmate.name}» میله هواکش سلول را شل کرده و قصد فرار به پشت‌بام را دارد!",
            "«${inmate.name}» هنگام جابه‌جایی هواخوری سعی در عبور از فنس شرقی زندان دارد!"
        )
        return reasons.random()
    }

    fun generatePrisonEscapedReason(inmate: InmateEntity): String {
        val reasons = listOf(
            "عدم مهار به‌موقع هشدار و ضعف امنیت بند باعث شد «${inmate.name}» از دیوار شرقی زندان فرار کند!",
            "«${inmate.name}» (${inmate.nickname}) از نقطه کور دوربین‌های بازداشتگاه گریخت و وارد خیابان‌های شهر شد!",
            "در شلوغی تعویض شیفت زندان، «${inmate.name}» قفل سلول را شکست و متواری شد!"
        )
        return reasons.random()
    }

    fun interrogationRewardMoney(inmate: InmateEntity, profile: PlayerProfileEntity): Long {
        val base = 45L + inmate.dangerLevel * 25L + (profile.prisonRehabLevel - 1) * 20L
        return if (ChiefTrait.fromName(profile.chiefTrait) == ChiefTrait.DETECTIVE_MIND) {
            (base * 1.25).roundToInt().toLong()
        } else {
            base
        }
    }

    fun generateInmateFromMission(
        mission: MissionEntity,
        profile: PlayerProfileEntity,
        nowMillis: Long = System.currentTimeMillis()
    ): InmateEntity {
        val (name, nickname) = GameCatalog.criminalProfiles.random()
        val sentenceSeconds = (35 + mission.difficulty * 10).coerceIn(35, 85)
        val rehabMult = 1.0 + (profile.prisonRehabLevel - 1) * 0.15 +
            if (ChiefTrait.fromName(profile.chiefTrait) == ChiefTrait.CITY_ECONOMIST) 0.15 else 0.0
        val baseBail = 95L + mission.difficulty * 55L + if (mission.isFugitivePursuit) 85L else 0L
        val bailReward = (baseBail * rehabMult).roundToInt().toLong()

        return InmateEntity(
            name = name,
            nickname = nickname,
            crimeTitle = mission.title.removePrefix("تعقیب فراری: ").removePrefix("فراری از زندان: "),
            district = mission.district,
            dangerLevel = mission.difficulty.coerceIn(1, 5),
            bailReward = bailReward,
            sentenceSeconds = sentenceSeconds,
            detainedAtMillis = nowMillis,
            releaseAtMillis = nowMillis + sentenceSeconds * 1000L,
            status = InmateStatus.IN_CUSTODY.name,
            interrogated = false,
            breakoutDeadlineMillis = 0L,
            breakoutReason = ""
        )
    }

    fun generatePrisonBreakoutPursuit(
        inmate: InmateEntity,
        playerLevel: Int,
        existingCoords: List<Pair<Float, Float>>
    ): MissionEntity {
        val (districtName, bestX, bestY) = pickDistrictCoords(existingCoords)
        val now = System.currentTimeMillis()
        val baseType = if (playerLevel >= 8) CrimeType.CRIMINAL_PURSUIT else CrimeType.CAR_THEFT
        val danger = inmate.dangerLevel.coerceIn(1, 5)
        return MissionEntity(
            crimeType = baseType.name,
            title = "فراری از زندان: ${inmate.name}",
            description = "زندانی فراری «${inmate.name}» با لقب «${inmate.nickname}» در محدوده $districtName رؤیت شد! با خودروی سریع و تیم گشت، قبل از خروج از شهر دستگیرش کنید.",
            district = districtName,
            mapX = bestX,
            mapY = bestY,
            difficulty = (danger + 1).coerceAtMost(5),
            requiredOfficers = if (danger >= 3) 2 else 1,
            requiredPower = (48 + danger * 26 + playerLevel * 8),
            rewardMoney = (inmate.bailReward * 1.70f).roundToInt().toLong().coerceAtLeast(260L),
            rewardXp = 70 + danger * 28,
            durationSeconds = (20 + danger * 4).coerceAtMost(40),
            suspectsCount = 1,
            status = MissionStatus.AVAILABLE.name,
            escapeRisk = (34 + danger * 6).coerceAtMost(66),
            expiresAtMillis = now + 95_000L,
            isFugitivePursuit = true,
            createdAtMillis = now
        )
    }

    fun stationStatusLabel(profile: PlayerProfileEntity): String = when (profile.stationBuildingLevel) {
        1 -> "کلانتری محلی"
        2 -> "کلانتری منطقه‌ای"
        3 -> "کلانتری پیشرفته"
        4 -> "مرکز پلیس ناحیه"
        5 -> "ستاد فرماندهی"
        6 -> "ستاد هوشمند شهر"
        7 -> "مرکز امنیت کلان‌شهر"
        8 -> "فرماندهی ویژه"
        9 -> "دژ امنیت شهری"
        else -> "ستاد مرکزی افسانه‌ای"
    }

    fun citySafetyScore(profile: PlayerProfileEntity, officersCount: Int, vehiclesCount: Int): Int {
        val total = profile.missionsSucceeded + profile.missionsFailed
        val winRatio = if (total == 0) 0.72f else profile.missionsSucceeded.toFloat() / total.toFloat()
        val escapePenalty = (profile.criminalsEscaped * 2).coerceAtMost(24)
        val base = (50 + profile.stationBuildingLevel * 4 + officersCount * 2 + vehiclesCount * 2 - escapePenalty)
        val combined = (base * 0.65f + winRatio * 40f).roundToInt()
        return combined.coerceIn(25, 100)
    }

    fun calculateMissionPreview(
        mission: MissionEntity,
        selectedOfficers: List<OfficerEntity>,
        selectedVehicle: VehicleEntity?,
        profile: PlayerProfileEntity
    ): DispatchPreview {
        val crimeType = CrimeType.fromName(mission.crimeType)
        var officerPowerSum = 0
        var specialtyMatches = 0

        for (officer in selectedOfficers) {
            val base = officerPower(officer)
            val isMatch = OfficerSpecialty.fromName(officer.specialty) == crimeType.preferredSpecialty
            if (isMatch) {
                specialtyMatches++
                officerPowerSum += (base * 1.28f).roundToInt()
            } else {
                officerPowerSum += base
            }
        }

        val chiefTrait = ChiefTrait.fromName(profile.chiefTrait)
        val safetyBonus = if (selectedOfficers.isNotEmpty()) (profile.safetyGearLevel - 1) * 10 + 4 else 0
        val vehiclePower = selectedVehicle?.let { vehiclePowerBonus(it) } ?: 0
        val buildingBonus = (profile.stationBuildingLevel - 1) * 6
        val rawTeamPower = officerPowerSum + safetyBonus + vehiclePower + buildingBonus
        val totalTeamPower = if (chiefTrait == ChiefTrait.TACTICAL_LEADER) {
            (rawTeamPower * 1.08f).roundToInt()
        } else {
            rawTeamPower
        }

        val warnings = mutableListOf<String>()

        if (selectedOfficers.isEmpty()) {
            return DispatchPreview(
                successRate = 0,
                escapeRisk = 95,
                finalDurationSeconds = mission.durationSeconds,
                finalRewardMoney = mission.rewardMoney,
                teamPower = 0,
                specialtyMatches = 0,
                tacticalWarnings = listOf("حداقل یک مأمور انتخاب کنید تا عملیات آغاز شود.")
            )
        }

        // More challenging, strategic power curve so upgrades & tactics genuinely matter
        val powerRatio = totalTeamPower.toFloat() / mission.requiredPower.coerceAtLeast(1).toFloat()
        val baseChance = when {
            powerRatio >= 1.60f -> 92f
            powerRatio >= 1.25f -> 78f + (powerRatio - 1.25f) * 40f
            powerRatio >= 0.90f -> 52f + (powerRatio - 0.90f) * 74f
            else -> (powerRatio / 0.90f) * 50f
        }

        val officerCountDiff = selectedOfficers.size - mission.requiredOfficers
        val staffAdjustment = when {
            officerCountDiff < 0 -> officerCountDiff * 18
            officerCountDiff > 0 -> officerCountDiff * 4
            else -> 0
        }
        val radarBonus = (profile.radarLevel - 1) * 3
        val traitSuccessBonus = when (chiefTrait) {
            ChiefTrait.TACTICAL_LEADER -> 5
            ChiefTrait.PURSUIT_ACE -> if (mission.isFugitivePursuit) 8 else 0
            else -> 0
        }
        val fugitivePenalty = if (mission.isFugitivePursuit) -7 else 0
        val noVehiclePenalty = if (crimeType.needsFastVehicle && selectedVehicle == null) -14 else 0
        val noSpecialtyPenalty = if (specialtyMatches == 0 && mission.difficulty >= 2) -8 else 0

        val successRate = (baseChance + staffAdjustment + radarBonus + traitSuccessBonus + fugitivePenalty + noVehiclePenalty + noSpecialtyPenalty)
            .roundToInt()
            .coerceIn(10, 96)

        // Calculate explicit Criminal Escape Risk %
        var escapeRisk = crimeType.baseEscapeRisk + if (mission.isFugitivePursuit) 14 else 0
        if (crimeType.needsFastVehicle && selectedVehicle == null) {
            escapeRisk += 24
            warnings.add("بدون خودروی گشت در این مأموریت، احتمال فرار مجرم بسیار بالاست!")
        } else if (selectedVehicle != null) {
            val speedBonus = vehicleSpeedBonus(selectedVehicle)
            escapeRisk -= (speedBonus / 2)
            if (crimeType.needsFastVehicle && speedBonus < 15 && mission.difficulty >= 3) {
                warnings.add("سرعت خودروی انتخابی برای این تعقیب کم است؛ احتمال گریز سارق وجود دارد.")
            }
        }
        if (officerCountDiff < 0) {
            escapeRisk += (-officerCountDiff) * 18
            warnings.add("کمبود نیرو (${selectedOfficers.size} از ${mission.requiredOfficers}): راه‌های فرعی ممکن است باز بماند!")
        } else if (officerCountDiff > 0) {
            escapeRisk -= officerCountDiff * 6
        }
        if (specialtyMatches == 0) {
            escapeRisk += 10
            warnings.add("نبود متخصص «${crimeType.preferredSpecialty.titleFa}» شانس فرار متهم را بیشتر می‌کند.")
        } else {
            escapeRisk -= specialtyMatches * 7
        }
        if (powerRatio < 1.0f) {
            escapeRisk += ((1.0f - powerRatio) * 30f).roundToInt()
            warnings.add("قدرت تیم از سطح جرم کمتر است؛ احتمال در رفتن مجرم بالاست.")
        }
        if (chiefTrait == ChiefTrait.DETECTIVE_MIND) {
            escapeRisk -= 10
        } else if (chiefTrait == ChiefTrait.PURSUIT_ACE && mission.isFugitivePursuit) {
            escapeRisk -= 10
        }
        escapeRisk -= (profile.radarLevel - 1) * 4
        escapeRisk = escapeRisk.coerceIn(5, 88)

        val radioReduction = ((profile.radioLevel - 1) * 4).coerceAtMost(36)
        val vehicleSpeed = selectedVehicle?.let { vehicleSpeedBonus(it) } ?: 0
        val traitSpeed = if (chiefTrait == ChiefTrait.PURSUIT_ACE) 12 else 0
        val totalSpeedReduction = (radioReduction + vehicleSpeed + traitSpeed).coerceAtMost(65)
        val finalDuration = (mission.durationSeconds * (100 - totalSpeedReduction) / 100.0)
            .roundToInt()
            .coerceAtLeast(7)

        val traitRewardBonus = if (chiefTrait == ChiefTrait.CITY_ECONOMIST) 0.15 else 0.0
        val rewardMultiplier = 1.0 +
            (profile.commandCenterLevel - 1) * 0.12 +
            (profile.stationBuildingLevel - 1) * 0.08 +
            traitRewardBonus
        val finalReward = (mission.rewardMoney * rewardMultiplier).roundToInt().toLong()

        return DispatchPreview(
            successRate = successRate,
            escapeRisk = escapeRisk,
            finalDurationSeconds = finalDuration,
            finalRewardMoney = finalReward,
            teamPower = totalTeamPower,
            specialtyMatches = specialtyMatches,
            tacticalWarnings = warnings
        )
    }

    fun determineEscapeReason(
        mission: MissionEntity,
        assignedOfficers: List<OfficerEntity>,
        assignedVehicle: VehicleEntity?,
        profile: PlayerProfileEntity
    ): String {
        val crimeType = CrimeType.fromName(mission.crimeType)
        val specialtyMatch = assignedOfficers.any {
            OfficerSpecialty.fromName(it.specialty) == crimeType.preferredSpecialty
        }
        val teamPower = assignedOfficers.sumOf { officerPower(it) } +
            (assignedVehicle?.let { vehiclePowerBonus(it) } ?: 0) +
            (profile.safetyGearLevel - 1) * 10

        return when {
            crimeType.needsFastVehicle && assignedVehicle == null ->
                "مجرم با خودروی سریع وارد بزرگراه شد؛ نیروهای شما بدون خودروی گشت نتوانستند به او برسند!"
            assignedOfficers.size < mission.requiredOfficers ->
                "به دلیل کمبود تعداد مأموران اعزامی، خروجی پشتی منطقه ${mission.district} باز ماند و سارق گریخت!"
            !specialtyMatch && mission.difficulty >= 2 ->
                "نبود افسر متخصص «${crimeType.preferredSpecialty.titleFa}» باعث شد متهم از نقطه کور صحنه متواری شود!"
            crimeType.needsFastVehicle && assignedVehicle != null && vehicleSpeedBonus(assignedVehicle) < 18 && mission.difficulty >= 3 ->
                "خودروی «${assignedVehicle.name}» سرعت کافی در تعقیب نداشت و مجرم در پیچ بزرگراه ناپدید شد!"
            teamPower < mission.requiredPower ->
                "مهارت فرار و مقاومت مجرم بیشتر از قدرت تیم اعزامی بود؛ مأموران یا تجهیزات ایمنی را ارتقا دهید!"
            profile.radarLevel <= 2 && Random.nextBoolean() ->
                "سارق در شلوغی محله ${mission.district} تغییر مسیر داد و به دلیل محدودیت رادار شهری ردپایش گم شد!"
            else ->
                "مجرم با استفاده از کوچه‌های فرعی و ترافیک سنگین ${mission.district} پیش از تکمیل محاصره گریخت!"
        }
    }

    fun generateTacticalAlertText(mission: MissionEntity): String {
        val prompts = listOf(
            "پیام فوری از ${mission.district}: سارق متوجه حضور پلیس شده و به سمت کوچه فرعی می‌دود!",
            "هشدار بی‌سیم: مظنون قصد خروج از محاصره در ${mission.district} را دارد! دستور تاکتیکی بدهید:",
            "تماس اضطراری گشت: مجرم تغییر مسیر داده و ممکن است تا لحظاتی دیگر فرار کند!"
        )
        return prompts.random()
    }

    fun generateMission(
        playerLevel: Int,
        existingCoords: List<Pair<Float, Float>>,
        forceEasy: Boolean = false
    ): MissionEntity {
        val unlockedTypes = CrimeType.entries.filter { it.minLevel <= playerLevel }
        val pool = if (forceEasy) {
            unlockedTypes.filter { it.minLevel == 1 }
        } else {
            val highTier = unlockedTypes.filter { it.minLevel >= (playerLevel - 5).coerceAtLeast(1) }
            if (highTier.isNotEmpty() && Random.nextFloat() < 0.72f) highTier else unlockedTypes
        }

        val crimeType = pool.random()
        val levelScale = 1.0 + ((playerLevel - crimeType.minLevel).coerceAtLeast(0) * 0.08)
        val requiredPower = (crimeType.baseRequiredPower * levelScale).roundToInt()
        val rewardMoney = (crimeType.baseRewardMoney * (1.0 + (playerLevel - 1) * 0.05)).roundToInt().toLong()
        val rewardXp = (crimeType.baseRewardXp * (1.0 + (playerLevel - 1) * 0.04)).roundToInt()
        val suspects = if (crimeType.maxSuspects <= crimeType.minSuspects) {
            crimeType.minSuspects
        } else {
            Random.nextInt(crimeType.minSuspects, crimeType.maxSuspects + 1)
        }

        val (districtName, bestX, bestY) = pickDistrictCoords(existingCoords)
        val difficulty = (crimeType.baseDifficulty + if (playerLevel - crimeType.minLevel >= 8) 1 else 0)
            .coerceIn(1, 5)
        val now = System.currentTimeMillis()
        // Starter easy missions get a generous window; harder crimes expire faster!
        val windowSec = if (forceEasy) crimeType.escapeWindowSeconds + 45 else crimeType.escapeWindowSeconds

        return MissionEntity(
            crimeType = crimeType.name,
            title = crimeType.titleFa,
            description = crimeType.reportTemplates.random(),
            district = districtName,
            mapX = bestX,
            mapY = bestY,
            difficulty = difficulty,
            requiredOfficers = crimeType.requiredOfficers,
            requiredPower = requiredPower,
            rewardMoney = rewardMoney,
            rewardXp = rewardXp,
            durationSeconds = crimeType.baseDurationSec,
            suspectsCount = suspects,
            status = MissionStatus.AVAILABLE.name,
            escapeRisk = crimeType.baseEscapeRisk,
            expiresAtMillis = now + windowSec * 1000L,
            createdAtMillis = now
        )
    }

    fun generateFugitivePursuitMission(
        escapedMission: MissionEntity,
        playerLevel: Int,
        existingCoords: List<Pair<Float, Float>>
    ): MissionEntity {
        val (districtName, bestX, bestY) = pickDistrictCoords(existingCoords)
        val now = System.currentTimeMillis()
        val baseType = CrimeType.fromName(escapedMission.crimeType)
        return MissionEntity(
            crimeType = baseType.name,
            title = "تعقیب فراری: ${escapedMission.title.removePrefix("تعقیب فراری: ")}",
            description = "دوربین‌های شهر مجرم فراری قبلی را در $districtName شناسایی کردند! قبل از خروج از شهر با خودروی سریع دستگیرش کنید.",
            district = districtName,
            mapX = bestX,
            mapY = bestY,
            difficulty = (escapedMission.difficulty + 1).coerceAtMost(5),
            requiredOfficers = escapedMission.requiredOfficers,
            requiredPower = (escapedMission.requiredPower * 1.15f).roundToInt(),
            rewardMoney = (escapedMission.rewardMoney * 1.45f).roundToInt().toLong(),
            rewardXp = (escapedMission.rewardXp * 1.35f).roundToInt(),
            durationSeconds = (escapedMission.durationSeconds + 4).coerceAtMost(55),
            suspectsCount = escapedMission.suspectsCount.coerceAtLeast(1),
            status = MissionStatus.AVAILABLE.name,
            escapeRisk = (baseType.baseEscapeRisk + 14).coerceAtMost(68),
            expiresAtMillis = now + 70_000L,
            isFugitivePursuit = true,
            createdAtMillis = now
        )
    }

    private fun pickDistrictCoords(existingCoords: List<Pair<Float, Float>>): Triple<String, Float, Float> {
        val shuffledDistricts = GameCatalog.districts.shuffled()
        var chosenName = shuffledDistricts.first().first
        var bestX = shuffledDistricts.first().second.first
        var bestY = shuffledDistricts.first().second.second

        for (candidate in shuffledDistricts) {
            val jitterX = (candidate.second.first + Random.nextFloat() * 0.08f - 0.04f).coerceIn(0.14f, 0.86f)
            val jitterY = (candidate.second.second + Random.nextFloat() * 0.08f - 0.04f).coerceIn(0.16f, 0.84f)
            val overlaps = existingCoords.any { (ex, ey) ->
                val dx = ex - jitterX
                val dy = ey - jitterY
                (dx * dx + dy * dy) < 0.015f
            }
            if (!overlaps) {
                chosenName = candidate.first
                bestX = jitterX
                bestY = jitterY
                break
            }
        }
        return Triple(chosenName, bestX, bestY)
    }

    fun applyXpAndLevelUp(profile: PlayerProfileEntity, gainedXp: Int): Pair<PlayerProfileEntity, Boolean> {
        var currentLevel = profile.level.coerceIn(1, GameCatalog.MAX_PLAYER_LEVEL)
        var currentXp = profile.xp + gainedXp
        var leveledUp = false

        while (currentLevel < GameCatalog.MAX_PLAYER_LEVEL) {
            val needed = GameCatalog.xpRequiredForLevel(currentLevel)
            if (currentXp >= needed) {
                currentXp -= needed
                currentLevel++
                leveledUp = true
            } else {
                break
            }
        }

        if (currentLevel >= GameCatalog.MAX_PLAYER_LEVEL) {
            currentLevel = GameCatalog.MAX_PLAYER_LEVEL
            currentXp = 0
        }

        return profile.copy(level = currentLevel, xp = currentXp) to leveledUp
    }
}
