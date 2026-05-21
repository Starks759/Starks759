package com.example.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "vitals")
data class VitalsEntity(
    @PrimaryKey
    val date: String, // Format: YYYY-MM-DD
    val avgHeartRate: Int = 72,
    val maxHeartRate: Int = 135,
    val minHeartRate: Int = 58,
    val spo2: Int = 98, // SpO2 percentage
    val sleepMinutes: Int = 420, // 7 hours default
    val sleepQualityScore: Int = 85,
    val stressScore: Int = 30, // 0 - 100
    val waterIntakeMl: Int = 1500,
    val waterGoalMl: Int = 2500,
    val weightKg: Float = 72.5f,
    val heightCm: Float = 178f
)
