package com.example.random_drive.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "destinations")
data class Destination(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val name: String,
    val type: String, // "바다", "산", "강", "도심"
    val latitude: Double,
    val longitude: Double
)