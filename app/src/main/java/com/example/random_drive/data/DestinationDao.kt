package com.example.random_drive.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query

/**
 * 데이터베이스 접근 객체(Data Access Object)
 */
@Dao
interface DestinationDao {
    @Insert
    suspend fun insertAll(vararg destinations: Destination)

    @Query("SELECT * FROM destinations WHERE type = :selectedType")
    suspend fun getDestinationsByType(selectedType: String): List<Destination>
}