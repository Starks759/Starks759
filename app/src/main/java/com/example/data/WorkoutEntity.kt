package com.example.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "workouts")
data class WorkoutEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val type: String, // "Running", "Walking", "Cycling"
    val durationSeconds: Long,
    val distanceKm: Float,
    val calories: Int,
    val timestamp: Long = System.currentTimeMillis(),
    val routePoints: String // JSON string of coordinates: [{"lat": 37.77, "lng": -122.41}, ...]
)
