package com.example.data

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class StepRepository(private val stepDao: StepDao) {

    // Daily Steps
    val allSteps: Flow<List<StepDaily>> = stepDao.getAllStepsFlow()

    fun getStepsForDateFlow(date: String): Flow<StepDaily> {
        return stepDao.getStepsForDateFlow(date).map {
            it ?: StepDaily(date = date, steps = 0, goal = 8000)
        }
    }

    suspend fun getStepsForDate(date: String): StepDaily {
        return stepDao.getStepsForDate(date) ?: StepDaily(date = date, steps = 0, goal = 8000)
    }

    suspend fun saveSteps(stepDaily: StepDaily) {
        stepDao.insertOrUpdate(stepDaily)
    }

    suspend fun clearHistory() {
        stepDao.clearAll()
    }

    // Devices
    val allDevices: Flow<List<DeviceEntity>> = stepDao.getAllDevicesFlow()

    suspend fun saveDevice(device: DeviceEntity) {
        stepDao.insertOrUpdateDevice(device)
    }

    suspend fun disconnectAllDevices() {
        stepDao.disconnectAllDevices()
    }

    suspend fun updateDeviceConnectionState(macAddress: String, connected: Boolean) {
        stepDao.updateDeviceConnectionState(macAddress, connected)
    }

    suspend fun deleteDevice(macAddress: String) {
        stepDao.deleteDevice(macAddress)
    }

    // Vitals
    fun getVitalsForDateFlow(date: String): Flow<VitalsEntity> {
        return stepDao.getVitalsForDateFlow(date).map {
            it ?: VitalsEntity(date = date)
        }
    }

    suspend fun getVitalsForDate(date: String): VitalsEntity {
        return stepDao.getVitalsForDate(date) ?: VitalsEntity(date = date)
    }

    suspend fun saveVitals(vitals: VitalsEntity) {
        stepDao.saveVitals(vitals)
    }

    // Workouts
    val allWorkouts: Flow<List<WorkoutEntity>> = stepDao.getAllWorkoutsFlow()

    suspend fun saveWorkout(workout: WorkoutEntity) {
        stepDao.insertWorkout(workout)
    }

    suspend fun clearWorkouts() {
        stepDao.clearWorkouts()
    }
}
