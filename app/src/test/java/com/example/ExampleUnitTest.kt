package com.example

import com.example.data.local.MissionEntity
import com.example.data.local.OfficerEntity
import com.example.data.local.PlayerProfileEntity
import com.example.data.local.VehicleEntity
import com.example.data.model.ChiefTrait
import com.example.data.model.CrimeType
import com.example.data.model.MissionStatus
import com.example.data.model.OfficerSpecialty
import com.example.domain.GameCatalog
import com.example.domain.GameEngine
import com.example.util.PersianFormat
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class ExampleUnitTest {

    @Test
    fun persianFormat_formatsNumbersMoneyAndPercent() {
        assertEquals("۱٬۲۵۰ سکه", PersianFormat.money(1250L))
        assertEquals("۸۵٪", PersianFormat.percent(85))
        assertEquals("۰۱:۱۵", PersianFormat.duration(75L))
    }

    @Test
    fun missionPreview_computesEscapeRiskAndWarnsWhenVehicleMissingOnPursuit() {
        val profile = PlayerProfileEntity(level = 6, radioLevel = 2, radarLevel = 2, safetyGearLevel = 2)
        val mission = MissionEntity(
            crimeType = CrimeType.CAR_THEFT.name,
            title = CrimeType.CAR_THEFT.titleFa,
            description = "گزارش تست",
            district = "بزرگراه شرقی",
            mapX = 0.5f,
            mapY = 0.5f,
            difficulty = 3,
            requiredOfficers = 2,
            requiredPower = 138,
            rewardMoney = 620L,
            rewardXp = 115,
            durationSeconds = 30,
            suspectsCount = 2,
            status = MissionStatus.AVAILABLE.name
        )
        val officers = listOf(
            OfficerEntity(id = 1, name = "امیر", specialty = OfficerSpecialty.TRAFFIC.name, level = 3, basePower = 36),
            OfficerEntity(id = 2, name = "سارا", specialty = OfficerSpecialty.PATROL.name, level = 2, basePower = 30)
        )
        val vehicle = VehicleEntity(
            id = 1,
            modelKey = "turbo_interceptor",
            name = "خودروی تعقیب توربو",
            level = 2,
            baseSpeedBonus = 28,
            basePowerBonus = 46
        )

        val previewWithoutCar = GameEngine.calculateMissionPreview(mission, officers, null, profile)
        val previewWithCar = GameEngine.calculateMissionPreview(mission, officers, vehicle, profile)

        assertTrue(previewWithoutCar.escapeRisk > previewWithCar.escapeRisk)
        assertTrue(previewWithoutCar.tacticalWarnings.isNotEmpty())
        assertTrue(previewWithCar.finalDurationSeconds < mission.durationSeconds)
        assertEquals(1, previewWithCar.specialtyMatches)

        val reason = GameEngine.determineEscapeReason(mission, officers, null, profile)
        assertTrue(reason.contains("خودرو"))
    }

    @Test
    fun progression_supportsAll20LevelsAndCapsAt20() {
        assertEquals(20, GameCatalog.milestones.size)
        val (level2, leveledUp) = GameEngine.applyXpAndLevelUp(PlayerProfileEntity(level = 1, xp = 0), 110)
        assertTrue(leveledUp)
        assertEquals(2, level2.level)

        val (capped, _) = GameEngine.applyXpAndLevelUp(PlayerProfileEntity(level = 20, xp = 0), 5000)
        assertEquals(20, capped.level)
        assertEquals(0, capped.xp)
    }

    @Test
    fun prisonSystem_scalesCapacitySecurityAndGeneratesInmateAndBreakoutPursuit() {
        val baseProfile = PlayerProfileEntity(prisonCapacityLevel = 1, prisonSecurityLevel = 1, prisonRehabLevel = 1)
        val upgradedProfile = PlayerProfileEntity(prisonCapacityLevel = 3, prisonSecurityLevel = 4, prisonRehabLevel = 3)

        assertEquals(4, GameEngine.maxPrisonCapacity(baseProfile))
        assertEquals(10, GameEngine.maxPrisonCapacity(upgradedProfile))
        assertTrue(GameEngine.prisonSecurityRating(upgradedProfile) > GameEngine.prisonSecurityRating(baseProfile))

        val mission = MissionEntity(
            crimeType = CrimeType.SHOP_THEFT.name,
            title = CrimeType.SHOP_THEFT.titleFa,
            description = "سرقت تست",
            district = "بازار بزرگ",
            mapX = 0.3f,
            mapY = 0.4f,
            difficulty = 3,
            requiredOfficers = 1,
            requiredPower = 60,
            rewardMoney = 200L,
            rewardXp = 50,
            durationSeconds = 20,
            suspectsCount = 1,
            status = MissionStatus.COMPLETED_SUCCESS.name
        )

        val inmateBase = GameEngine.generateInmateFromMission(mission, baseProfile, 10_000L)
        val inmateUpgraded = GameEngine.generateInmateFromMission(mission, upgradedProfile, 10_000L)

        assertTrue(inmateUpgraded.bailReward > inmateBase.bailReward)
        assertTrue(inmateBase.releaseAtMillis > inmateBase.detainedAtMillis)

        val pursuit = GameEngine.generatePrisonBreakoutPursuit(inmateBase, 4, emptyList())
        assertTrue(pursuit.isFugitivePursuit)
        assertTrue(pursuit.title.contains("فراری از زندان"))
        assertTrue(pursuit.rewardMoney >= 260L)

        // Verify inmate escape risk decreases with security upgrades
        val riskLowSec = GameEngine.inmateEscapeRiskPercent(inmateBase, baseProfile)
        val riskHighSec = GameEngine.inmateEscapeRiskPercent(inmateBase, upgradedProfile)
        assertTrue(riskLowSec > riskHighSec)
    }

    @Test
    fun chiefTraits_applyRealGameplayBonuses() {
        val officers = listOf(
            OfficerEntity(id = 1, name = "آرش", specialty = OfficerSpecialty.PATROL.name, level = 2, basePower = 30)
        )
        val mission = MissionEntity(
            crimeType = CrimeType.SHOP_THEFT.name,
            title = CrimeType.SHOP_THEFT.titleFa,
            description = "تست",
            district = "مرکز شهر",
            mapX = 0.5f,
            mapY = 0.5f,
            difficulty = 2,
            requiredOfficers = 1,
            requiredPower = 55,
            rewardMoney = 200L,
            rewardXp = 40,
            durationSeconds = 30,
            suspectsCount = 1,
            status = MissionStatus.AVAILABLE.name
        )

        val tactical = GameEngine.calculateMissionPreview(
            mission, officers, null, PlayerProfileEntity(chiefTrait = ChiefTrait.TACTICAL_LEADER.name)
        )
        val economist = GameEngine.calculateMissionPreview(
            mission, officers, null, PlayerProfileEntity(chiefTrait = ChiefTrait.CITY_ECONOMIST.name)
        )
        val pursuitAce = GameEngine.calculateMissionPreview(
            mission, officers, null, PlayerProfileEntity(chiefTrait = ChiefTrait.PURSUIT_ACE.name)
        )
        val detective = GameEngine.calculateMissionPreview(
            mission, officers, null, PlayerProfileEntity(chiefTrait = ChiefTrait.DETECTIVE_MIND.name)
        )

        assertTrue(tactical.teamPower > economist.teamPower)
        assertTrue(economist.finalRewardMoney > tactical.finalRewardMoney)
        assertTrue(pursuitAce.finalDurationSeconds < tactical.finalDurationSeconds)
        assertTrue(detective.escapeRisk < economist.escapeRisk)
    }
}
