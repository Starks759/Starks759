package com.example.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "daily_steps")
data class StepDaily(
    @PrimaryKey
    val date: String, // Format: YYYY-MM-DD
    val steps: Int,
    val goal: Int = 10000
)
