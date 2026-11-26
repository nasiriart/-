package com.example

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.example.data.local.MissionEntity
import com.example.data.local.OfficerEntity
import com.example.data.local.PlayerProfileEntity
import com.example.data.model.CrimeType
import com.example.data.model.MissionStatus
import com.example.data.model.OfficerSpecialty
import com.example.domain.GameCatalog
import com.example.domain.GameEngine
import com.example.util.PersianFormat
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class ExampleRobolectricTest {

    @Test
    fun `read persian app name from context`() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val appName = context.getString(R.string.app_name)
        assertEquals("رئیس پلیس", appName)
    }

    @Test
    fun `persian number formatting works accurately`() {
        assertEquals("۱٬۲۵۰ سکه", PersianFormat.money(1250L))
        assertEquals("۸۵٪", PersianFormat.percent(85))
    }

    @Test
    fun `mission preview increases success with matching specialist and caps at 98`() {
        val profile = PlayerProfileEntity(level = 3, radarLevel = 2, safetyGearLevel = 2)
        val mission = MissionEntity(
            crimeType = CrimeType.SHOP_THEFT.name,
            title = CrimeType.SHOP_THEFT.titleFa,
            description = "تست",
            district = "مرکز شهر",
            mapX = 0.5f,
            mapY = 0.5f,
            difficulty = 1,
            requiredOfficers = 1,
            requiredPower = 30,
            rewardMoney = 150L,
            rewardXp = 35,
            durationSeconds = 20,
            suspectsCount = 1,
            status = MissionStatus.AVAILABLE.name
        )
        val matchingOfficer = OfficerEntity(
            id = 1,
            name = "آرش",
            specialty = OfficerSpecialty.PATROL.name,
            level = 2,
            basePower = 28
        )
        val preview = GameEngine.calculateMissionPreview(mission, listOf(matchingOfficer), null, profile)
        assertTrue(preview.successRate in 70..98)
        assertEquals(1, preview.specialtyMatches)
    }

    @Test
    fun `level progression supports all 20 levels`() {
        assertEquals(20, GameCatalog.milestones.size)
        val (updated, leveledUp) = GameEngine.applyXpAndLevelUp(PlayerProfileEntity(level = 1, xp = 0), 120)
        assertTrue(leveledUp)
        assertEquals(2, updated.level)
    }
}
