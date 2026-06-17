package com.example.random_drive.data

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Room Database 엔티티 정의 (목적지 스키마)
 */
@Entity(tableName = "destinations")
data class Destination(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val name: String,
    val type: String, // "바다", "산", "강", "도심"
    val latitude: Double,
    val longitude: Double
)