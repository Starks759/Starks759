package com.example.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "devices")
data class DeviceEntity(
    @PrimaryKey
    val macAddress: String,
    val name: String,
    val type: String, // "Apple Watch Ultra", "Galaxy Watch 6 Pro", "Garmin Fenix 7X", "Fitbit Sense 2"
    val batteryPercent: Int = 88,
    val isConnected: Boolean = false,
    val lastSyncTime: Long = System.currentTimeMillis()
)
