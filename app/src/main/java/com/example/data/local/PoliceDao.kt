package com.example.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface PoliceDao {

    @Query("SELECT * FROM player_profile WHERE id = 1")
    fun observeProfile(): Flow<PlayerProfileEntity?>

    @Query("SELECT * FROM player_profile WHERE id = 1")
    suspend fun getProfileOnce(): PlayerProfileEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertProfile(profile: PlayerProfileEntity)

    @Query("SELECT * FROM officers ORDER BY isBusy ASC, level DESC, basePower DESC")
    fun observeOfficers(): Flow<List<OfficerEntity>>

    @Query("SELECT * FROM officers")
    suspend fun getOfficersOnce(): List<OfficerEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOfficer(officer: OfficerEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOfficers(officers: List<OfficerEntity>)

    @Update
    suspend fun updateOfficer(officer: OfficerEntity)

    @Query("UPDATE officers SET isBusy = :busy, assignedMissionId = :missionId WHERE id IN (:ids)")
    suspend fun setOfficersBusy(ids: List<Int>, busy: Boolean, missionId: Int?)

    @Query("UPDATE officers SET isBusy = 0, assignedMissionId = NULL, missionsCompleted = missionsCompleted + 1 WHERE id IN (:ids)")
    suspend fun releaseOfficersWithCredit(ids: List<Int>)

    @Query("UPDATE officers SET isBusy = 0, assignedMissionId = NULL WHERE id IN (:ids)")
    suspend fun releaseOfficersWithoutCredit(ids: List<Int>)

    @Query("SELECT * FROM vehicles ORDER BY isBusy ASC, level DESC, basePowerBonus DESC")
    fun observeVehicles(): Flow<List<VehicleEntity>>

    @Query("SELECT * FROM vehicles")
    suspend fun getVehiclesOnce(): List<VehicleEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertVehicle(vehicle: VehicleEntity): Long

    @Update
    suspend fun updateVehicle(vehicle: VehicleEntity)

    @Query("UPDATE vehicles SET isBusy = :busy, assignedMissionId = :missionId WHERE id = :id")
    suspend fun setVehicleBusy(id: Int, busy: Boolean, missionId: Int?)

    @Query("SELECT * FROM missions ORDER BY createdAtMillis DESC")
    fun observeMissions(): Flow<List<MissionEntity>>

    @Query("SELECT * FROM missions WHERE id = :id")
    suspend fun getMissionById(id: Int): MissionEntity?

    @Query("SELECT * FROM missions WHERE status = :status")
    suspend fun getMissionsByStatus(status: String): List<MissionEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMission(mission: MissionEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMissions(missions: List<MissionEntity>)

    @Update
    suspend fun updateMission(mission: MissionEntity)

    @Query("DELETE FROM missions WHERE status IN ('COMPLETED_SUCCESS', 'COMPLETED_FAIL', 'ESCAPED') AND id NOT IN (SELECT id FROM missions WHERE status IN ('COMPLETED_SUCCESS', 'COMPLETED_FAIL', 'ESCAPED') ORDER BY createdAtMillis DESC LIMIT 24)")
    suspend fun trimOldMissionHistory()

    @Query("DELETE FROM player_profile")
    suspend fun clearProfile()

    @Query("DELETE FROM officers")
    suspend fun clearOfficers()

    @Query("DELETE FROM vehicles")
    suspend fun clearVehicles()

    @Query("DELETE FROM missions")
    suspend fun clearMissions()

    @Query("SELECT * FROM inmates ORDER BY detainedAtMillis DESC")
    fun observeInmates(): Flow<List<InmateEntity>>

    @Query("SELECT * FROM inmates WHERE status IN ('IN_CUSTODY', 'BREAKOUT_ALERT')")
    suspend fun getActiveInmatesOnce(): List<InmateEntity>

    @Query("SELECT * FROM inmates WHERE id = :id")
    suspend fun getInmateById(id: Int): InmateEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertInmate(inmate: InmateEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertInmates(inmates: List<InmateEntity>)

    @Update
    suspend fun updateInmate(inmate: InmateEntity)

    @Query("DELETE FROM inmates WHERE status IN ('TRANSFERRED', 'ESCAPED_PRISON') AND id NOT IN (SELECT id FROM inmates WHERE status IN ('TRANSFERRED', 'ESCAPED_PRISON') ORDER BY detainedAtMillis DESC LIMIT 24)")
    suspend fun trimOldInmates()

    @Query("DELETE FROM inmates")
    suspend fun clearInmates()
}
