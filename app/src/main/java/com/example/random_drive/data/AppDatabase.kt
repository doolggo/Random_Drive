package com.example.random_drive.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

/**
 * Room 데이터베이스의 생성 및 초기화를 담당하는 클래스
 */
@Database(entities = [Destination::class], version = 1, exportSchema = false)
abstract class AppDatabase : RoomDatabase() {
    abstract fun destinationDao(): DestinationDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        // 싱글톤 패턴을 적용하여 DB 인스턴스 중복 생성 방지
        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "drive_database"
                ).build()
                INSTANCE = instance
                instance
            }
        }
    }
}