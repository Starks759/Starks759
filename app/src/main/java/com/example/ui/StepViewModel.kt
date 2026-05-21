package com.example.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.*
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

class StepViewModel(application: Application) : AndroidViewModel(application) {

    val repository: StepRepository

    private val todayDate: String
        get() {
            val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
            return sdf.format(Date())
        }

    // Flows
    val todaySteps: StateFlow<StepDaily>
    val allHistory: StateFlow<List<StepDaily>>
    val allDevices: StateFlow<List<DeviceEntity>>
    val todayVitals: StateFlow<VitalsEntity>
    val allWorkouts: StateFlow<List<WorkoutEntity>>

    // Sensor status
    private val _isSensorRegistered = MutableStateFlow(false)
    val isSensorRegistered: StateFlow<Boolean> = _isSensorRegistered.asStateFlow()

    private val _sensorAvailable = MutableStateFlow(false)
    val sensorAvailable: StateFlow<Boolean> = _sensorAvailable.asStateFlow()

    // AI States
    private val _aiInsight = MutableStateFlow<String>("")
    val aiInsight: StateFlow<String> = _aiInsight.asStateFlow()

    private val _isAnalyzing = MutableStateFlow(false)
    val isAnalyzing: StateFlow<Boolean> = _isAnalyzing.asStateFlow()

    // Live Workout Tracking States
    private val _isTrackingWorkout = MutableStateFlow(false)
    val isTrackingWorkout: StateFlow<Boolean> = _isTrackingWorkout.asStateFlow()

    private val _workoutTimeSeconds = MutableStateFlow(0L)
    val workoutTimeSeconds: StateFlow<Long> = _workoutTimeSeconds.asStateFlow()

    private val _workoutType = MutableStateFlow("Walking")
    val workoutType: StateFlow<String> = _workoutType.asStateFlow()

    private val _workoutRoutePoints = MutableStateFlow<List<Pair<Double, Double>>>(emptyList())
    val workoutRoutePoints: StateFlow<List<Pair<Double, Double>>> = _workoutRoutePoints.asStateFlow()

    private var workoutTimerJob: Job? = null

    init {
        val database = StepDatabase.getDatabase(application)
        repository = StepRepository(database.stepDao())

        // 1. Daily Steps
        todaySteps = repository.getStepsForDateFlow(todayDate)
            .stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(5000),
                initialValue = StepDaily(date = todayDate, steps = 0, goal = 10000)
            )

        allHistory = repository.allSteps
            .stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(5000),
                initialValue = emptyList()
            )

        // 2. Transceiver Devices list
        allDevices = repository.allDevices
            .stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(5000),
                initialValue = emptyList()
            )

        // 3. Health Vitals
        todayVitals = repository.getVitalsForDateFlow(todayDate)
            .stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(5000),
                initialValue = VitalsEntity(date = todayDate)
            )

        // 4. GPS Workouts
        allWorkouts = repository.allWorkouts
            .stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(5000),
                initialValue = emptyList()
            )

        prePopulateData()
        requestAutoInsight()
    }

    private fun prePopulateData() {
        viewModelScope.launch {
            // Populate Steps if empty
            val existing = repository.allSteps.first()
            if (existing.isEmpty() || existing.none { it.date != todayDate }) {
                val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
                val calendar = Calendar.getInstance()
                for (i in 7 downTo 1) {
                    calendar.time = Date()
                    calendar.add(Calendar.DAY_OF_YEAR, -i)
                    val dateStr = sdf.format(calendar.time)
                    val randomSteps = (4000..12000).random()
                    val targetGoal = listOf(8000, 10000, 12000).random()
                    repository.saveSteps(StepDaily(date = dateStr, steps = randomSteps, goal = targetGoal))

                    // Populate Vitals
                    val randHr = (65..80).random()
                    val randSpo2 = (96..99).random()
                    val randSleep = (360..520).random()
                    val randSleepScore = (75..94).random()
                    val randWater = (1000..3000).random()
                    repository.saveVitals(
                        VitalsEntity(
                            date = dateStr,
                            avgHeartRate = randHr,
                            maxHeartRate = randHr + 45,
                            minHeartRate = randHr - 15,
                            spo2 = randSpo2,
                            sleepMinutes = randSleep,
                            sleepQualityScore = randSleepScore,
                            stressScore = (15..45).random(),
                            waterIntakeMl = randWater,
                            waterGoalMl = 2500
                        )
                    )
                }
            }

            // Populate today vitals if not exists in database
            val vit = repository.getVitalsForDate(todayDate)
            if (vit.waterIntakeMl <= 0) {
                repository.saveVitals(VitalsEntity(date = todayDate, waterIntakeMl = 500, waterGoalMl = 2500))
            }

            // Populate default mock premium watch device if no watches are registered
            val devices = repository.allDevices.first()
            if (devices.isEmpty()) {
                repository.saveDevice(
                    DeviceEntity(
                        macAddress = "E4:F2:C1:89:AB:D4",
                        name = "Apple Watch Ultra 2",
                        type = "watchos",
                        batteryPercent = 94,
                        isConnected = true
                    )
                )
                repository.saveDevice(
                    DeviceEntity(
                        macAddress = "C8:9F:5D:22:11:FE",
                        name = "Galaxy Watch 6 Classic",
                        type = "wearos",
                        batteryPercent = 71,
                        isConnected = false
                    )
                )
            }

            // Populate some historical workouts if none
            val workouts = repository.allWorkouts.first()
            if (workouts.isEmpty()) {
                repository.saveWorkout(
                    WorkoutEntity(
                        type = "Running",
                        durationSeconds = 1860, // 31 mins
                        distanceKm = 5.25f,
                        calories = 420,
                        timestamp = System.currentTimeMillis() - 86400000 * 2, // 2 days ago
                        routePoints = "[{\"lat\":37.7749,\"lng\":-122.4194},{\"lat\":37.7752,\"lng\":-122.4185}]"
                    )
                )
                repository.saveWorkout(
                    WorkoutEntity(
                        type = "Cycling",
                        durationSeconds = 3600, // 1 hour
                        distanceKm = 18.2f,
                        calories = 650,
                        timestamp = System.currentTimeMillis() - 86400000 * 4, // 4 days ago
                        routePoints = "[{\"lat\":37.7849,\"lng\":-122.4094},{\"lat\":37.7952,\"lng\":-122.3985}]"
                    )
                )
            }
        }
    }

    // Step Sync & Modifiers
    fun addSteps(amount: Int) {
        viewModelScope.launch {
            val current = todaySteps.value
            val updatedSteps = (current.steps + amount).coerceAtLeast(0)
            repository.saveSteps(current.copy(steps = updatedSteps))

            // Pulse the server-side analysis incrementally if steps change a lot
            if (amount >= 1000) {
                requestAutoInsight()
            }
        }
    }

    fun setGoal(goal: Int) {
        viewModelScope.launch {
            val current = todaySteps.value
            repository.saveSteps(current.copy(goal = goal))
        }
    }

    fun setSensorStatus(available: Boolean, registered: Boolean) {
        _sensorAvailable.value = available
        _isSensorRegistered.value = registered
    }

    fun resetToday() {
        viewModelScope.launch {
            val current = todaySteps.value
            repository.saveSteps(current.copy(steps = 0))
            val vit = repository.getVitalsForDate(todayDate)
            repository.saveVitals(vit.copy(avgHeartRate = 70, waterIntakeMl = 0))
            requestAutoInsight()
        }
    }

    fun clearHistory() {
        viewModelScope.launch {
            repository.clearHistory()
            repository.clearWorkouts()
            val current = todaySteps.value
            repository.saveSteps(StepDaily(date = todayDate, steps = 0, goal = current.goal))
            repository.saveVitals(VitalsEntity(date = todayDate, waterIntakeMl = 0))
            requestAutoInsight()
        }
    }

    // Smartwatch Pairing Actions
    fun pairSmartwatch(name: String, type: String, mac: String) {
        viewModelScope.launch {
            // Disconnect other watches temporarily to swap primary
            repository.disconnectAllDevices()
            val newDevice = DeviceEntity(
                macAddress = mac,
                name = name,
                type = type,
                batteryPercent = (75..100).random(),
                isConnected = true,
                lastSyncTime = System.currentTimeMillis()
            )
            repository.saveDevice(newDevice)
        }
    }

    fun toggleDeviceConnection(device: DeviceEntity) {
        viewModelScope.launch {
            if (!device.isConnected) {
                // If connecting, disconnect all others
                repository.disconnectAllDevices()
            }
            repository.updateDeviceConnectionState(device.macAddress, !device.isConnected)
        }
    }

    fun removeDevice(macAddress: String) {
        viewModelScope.launch {
            repository.deleteDevice(macAddress)
        }
    }

    // Vitals modifier
    fun logWaterIntake(ml: Int) {
        viewModelScope.launch {
            val currentVitals = todayVitals.value
            val updatedWater = (currentVitals.waterIntakeMl + ml).coerceIn(0, 10000)
            repository.saveVitals(currentVitals.copy(waterIntakeMl = updatedWater))
        }
    }

    fun triggerHeartRateScan() {
        viewModelScope.launch {
            val currentVitals = todayVitals.value
            val simulatedHeartRate = (68..142).random()
            val simulatedStress = (10..95).random()
            val simulatedSpo2 = (95..100).random()
            repository.saveVitals(
                currentVitals.copy(
                    avgHeartRate = simulatedHeartRate,
                    maxHeartRate = simulatedHeartRate + 30,
                    minHeartRate = (simulatedHeartRate - 20).coerceAtLeast(40),
                    stressScore = simulatedStress,
                    spo2 = simulatedSpo2
                )
            )
            requestAutoInsight()
        }
    }

    // GPS workouts and Route Tracking Actions
    fun startWorkout(type: String) {
        if (_isTrackingWorkout.value) return
        _workoutType.value = type
        _isTrackingWorkout.value = true
        _workoutTimeSeconds.value = 0L
        _workoutRoutePoints.value = listOf(Pair(37.7749, -122.4194)) // San Francisco start pin

        workoutTimerJob = viewModelScope.launch {
            while (_isTrackingWorkout.value) {
                delay(1000)
                _workoutTimeSeconds.value += 1
                
                // Add a simulated walking GPS coordinate jitter
                val lastPoint = _workoutRoutePoints.value.lastOrNull() ?: Pair(37.7749, -122.4194)
                val latJitter = ((-10..10).random().toDouble() / 15000.0)
                val lngJitter = ((-10..10).random().toDouble() / 15000.0)
                _workoutRoutePoints.value = _workoutRoutePoints.value + Pair(lastPoint.first + latJitter, lastPoint.second + lngJitter)
            }
        }
    }

    fun stopAndSaveWorkout() {
        if (!_isTrackingWorkout.value) return
        workoutTimerJob?.cancel()
        _isTrackingWorkout.value = false

        viewModelScope.launch {
            val elapsedSecs = _workoutTimeSeconds.value
            val speedKmh = when (_workoutType.value) {
                "Running" -> 10.5f
                "Cycling" -> 22.0f
                else -> 5.0f // Walking
            }
            val calculatedDistance = (elapsedSecs / 3600f) * speedKmh
            val calculatedCalories = ((elapsedSecs / 60) * when (_workoutType.value) {
                "Running" -> 11f
                "Cycling" -> 8f
                else -> 4f // Walking
            }).toInt()

            // Map GPS trail coordinates list
            val pointsJson = buildString {
                append("[")
                _workoutRoutePoints.value.forEachIndexed { i, pair ->
                    append("{\"lat\":${pair.first},\"lng\":${pair.second}}")
                    if (i < _workoutRoutePoints.value.size - 1) append(",")
                }
                append("]")
            }

            val newWorkout = WorkoutEntity(
                type = _workoutType.value,
                durationSeconds = elapsedSecs,
                distanceKm = String.format(Locale.US, "%.2f", calculatedDistance).toFloat(),
                calories = calculatedCalories,
                routePoints = pointsJson
            )

            repository.saveWorkout(newWorkout)

            // Inject tracked distance into steps if workout and walks aligned !
            if (_workoutType.value == "Running" || _workoutType.value == "Walking") {
                val addedStepsFromWorkout = (calculatedDistance * 1333).toInt()
                addSteps(addedStepsFromWorkout)
            }
        }
    }

    fun discardWorkout() {
        workoutTimerJob?.cancel()
        _isTrackingWorkout.value = false
        _workoutTimeSeconds.value = 0
        _workoutRoutePoints.value = emptyList()
    }

    // AI Fitness Coaching Integrations
    fun requestCustomCoachQuery(query: String) {
        viewModelScope.launch {
            _isAnalyzing.value = true
            val current = todaySteps.value
            val cal = current.steps * 0.04f
            val km = current.steps * 0.00075f
            val sleepHrs = todayVitals.value.sleepMinutes / 60f

            val advice = GeminiAIService.getFitnessInsight(
                steps = current.steps,
                goal = current.goal,
                calories = cal,
                distanceKm = km,
                avgHeartRate = todayVitals.value.avgHeartRate,
                sleepHours = sleepHrs,
                customQuery = query
            )
            _aiInsight.value = advice
            _isAnalyzing.value = false
        }
    }

    fun requestAutoInsight() {
        viewModelScope.launch {
            _isAnalyzing.value = true
            val current = todaySteps.value
            val cal = current.steps * 0.04f
            val km = current.steps * 0.00075f
            val sleepHrs = todayVitals.value.sleepMinutes / 60f

            val advice = GeminiAIService.getFitnessInsight(
                steps = current.steps,
                goal = current.goal,
                calories = cal,
                distanceKm = km,
                avgHeartRate = todayVitals.value.avgHeartRate,
                sleepHours = sleepHrs,
                customQuery = null
            )
            _aiInsight.value = advice
            _isAnalyzing.value = false
        }
    }
}
