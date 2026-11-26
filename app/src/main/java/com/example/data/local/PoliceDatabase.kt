package com.example.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

@Database(
    entities = [
        PlayerProfileEntity::class,
        OfficerEntity::class,
        VehicleEntity::class,
        MissionEntity::class,
        InmateEntity::class
    ],
    version = 4,
    exportSchema = false
)
abstract class PoliceDatabase : RoomDatabase() {
    abstract fun policeDao(): PoliceDao

    companion object {
        @Volatile
        private var INSTANCE: PoliceDatabase? = null

        private val MIGRATION_1_2 = object : Migration(1, 2) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("ALTER TABLE player_profile ADD COLUMN criminalsEscaped INTEGER NOT NULL DEFAULT 0")
                db.execSQL("ALTER TABLE missions ADD COLUMN escapeRisk INTEGER NOT NULL DEFAULT 20")
                db.execSQL("ALTER TABLE missions ADD COLUMN expiresAtMillis INTEGER NOT NULL DEFAULT 0")
                db.execSQL("ALTER TABLE missions ADD COLUMN escapeReason TEXT NOT NULL DEFAULT ''")
                db.execSQL("ALTER TABLE missions ADD COLUMN isFugitivePursuit INTEGER NOT NULL DEFAULT 0")
                db.execSQL("ALTER TABLE missions ADD COLUMN tacticalAlertActive INTEGER NOT NULL DEFAULT 0")
                db.execSQL("ALTER TABLE missions ADD COLUMN tacticalAlertText TEXT NOT NULL DEFAULT ''")
                db.execSQL("ALTER TABLE missions ADD COLUMN tacticalAlertResolved INTEGER NOT NULL DEFAULT 0")
            }
        }

        private val MIGRATION_2_3 = object : Migration(2, 3) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("ALTER TABLE player_profile ADD COLUMN prisonCapacityLevel INTEGER NOT NULL DEFAULT 1")
                db.execSQL("ALTER TABLE player_profile ADD COLUMN prisonSecurityLevel INTEGER NOT NULL DEFAULT 1")
                db.execSQL("ALTER TABLE player_profile ADD COLUMN prisonRehabLevel INTEGER NOT NULL DEFAULT 1")
                db.execSQL("ALTER TABLE player_profile ADD COLUMN inmatesTransferred INTEGER NOT NULL DEFAULT 0")
                db.execSQL(
                    """
                    CREATE TABLE IF NOT EXISTS `inmates` (
                        `id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                        `name` TEXT NOT NULL,
                        `nickname` TEXT NOT NULL,
                        `crimeTitle` TEXT NOT NULL,
                        `district` TEXT NOT NULL,
                        `dangerLevel` INTEGER NOT NULL,
                        `bailReward` INTEGER NOT NULL,
                        `sentenceSeconds` INTEGER NOT NULL,
                        `detainedAtMillis` INTEGER NOT NULL,
                        `releaseAtMillis` INTEGER NOT NULL,
                        `status` TEXT NOT NULL,
                        `interrogated` INTEGER NOT NULL,
                        `breakoutDeadlineMillis` INTEGER NOT NULL,
                        `breakoutReason` TEXT NOT NULL
                    )
                    """.trimIndent()
                )
            }
        }

        private val MIGRATION_3_4 = object : Migration(3, 4) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("ALTER TABLE player_profile ADD COLUMN chiefName TEXT NOT NULL DEFAULT 'فرمانده کلانتری'")
                db.execSQL("ALTER TABLE player_profile ADD COLUMN chiefCallsign TEXT NOT NULL DEFAULT 'عقاب ۱'")
                db.execSQL("ALTER TABLE player_profile ADD COLUMN chiefAvatarKey TEXT NOT NULL DEFAULT 'tactical'")
                db.execSQL("ALTER TABLE player_profile ADD COLUMN chiefTrait TEXT NOT NULL DEFAULT 'TACTICAL_LEADER'")
                db.execSQL("ALTER TABLE player_profile ADD COLUMN chiefBio TEXT NOT NULL DEFAULT 'حافظ امنیت و آرامش شهر'")
                db.execSQL("ALTER TABLE player_profile ADD COLUMN profileConfigured INTEGER NOT NULL DEFAULT 0")
                db.execSQL("ALTER TABLE inmates ADD COLUMN pursuitMissionId INTEGER DEFAULT NULL")
            }
        }

        fun getInstance(context: Context): PoliceDatabase {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: Room.databaseBuilder(
                    context.applicationContext,
                    PoliceDatabase::class.java,
                    "police_chief_db"
                )
                    .addMigrations(MIGRATION_1_2, MIGRATION_2_3, MIGRATION_3_4)
                    .fallbackToDestructiveMigration(true)
                    .build()
                    .also { INSTANCE = it }
            }
        }
    }
}
