package com.example.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface StepDao {
    // Daily Steps
    @Query("SELECT * FROM daily_steps WHERE date = :date LIMIT 1")
    fun getStepsForDateFlow(date: String): Flow<StepDaily?>

    @Query("SELECT * FROM daily_steps WHERE date = :date LIMIT 1")
    suspend fun getStepsForDate(date: String): StepDaily?

    @Query("SELECT * FROM daily_steps ORDER BY date DESC")
    fun getAllStepsFlow(): Flow<List<StepDaily>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdate(stepDaily: StepDaily)

    @Query("DELETE FROM daily_steps")
    suspend fun clearAll()

    // Smartwatches / Devices
    @Query("SELECT * FROM devices ORDER BY name ASC")
    fun getAllDevicesFlow(): Flow<List<DeviceEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdateDevice(device: DeviceEntity)

    @Query("UPDATE devices SET isConnected = 0")
    suspend fun disconnectAllDevices()

    @Query("UPDATE devices SET isConnected = :connected WHERE macAddress = :macAddress")
    suspend fun updateDeviceConnectionState(macAddress: String, connected: Boolean)

    @Query("DELETE FROM devices WHERE macAddress = :macAddress")
    suspend fun deleteDevice(macAddress: String)

    // Vitals
    @Query("SELECT * FROM vitals WHERE date = :date LIMIT 1")
    fun getVitalsForDateFlow(date: String): Flow<VitalsEntity?>

    @Query("SELECT * FROM vitals WHERE date = :date LIMIT 1")
    suspend fun getVitalsForDate(date: String): VitalsEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun saveVitals(vitals: VitalsEntity)

    // Workouts
    @Query("SELECT * FROM workouts ORDER BY timestamp DESC")
    fun getAllWorkoutsFlow(): Flow<List<WorkoutEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertWorkout(workout: WorkoutEntity)

    @Query("DELETE FROM workouts")
    suspend fun clearWorkouts()
}
