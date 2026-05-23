package com.example.random_drive.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.random_drive.data.AppDatabase
import com.example.random_drive.data.Destination
import com.example.random_drive.location.LocationHelper
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class DriveViewModel(application: Application) : AndroidViewModel(application) {
    private val dao = AppDatabase.getDatabase(application).destinationDao()

    private val _selectedDestination = MutableStateFlow<Destination?>(null)
    val selectedDestination: StateFlow<Destination?> = _selectedDestination

    init {
        // 앱 실행 시 테스트용 데이터 로드
        viewModelScope.launch {
            dao.insertAll(
                Destination(name = "을왕리 해수욕장", type = "바다", latitude = 37.4445, longitude = 126.3708),
                Destination(name = "북한산 국립공원", type = "산", latitude = 37.6593, longitude = 126.9839),
                Destination(name = "남양주 두물머리", type = "강", latitude = 37.5332, longitude = 127.3175),
                Destination(name = "강남역", type = "도심", latitude = 37.4979, longitude = 127.0276)
            )
        }
    }

    fun findRandomDestination(
        currentLat: Double,
        currentLon: Double,
        minDist: Double,
        maxDist: Double,
        selectedType: String
    ) {
        viewModelScope.launch {
            val typeFilteredList = dao.getDestinationsByType(selectedType)

            val distanceFilteredList = typeFilteredList.filter { dest ->
                val distance = LocationHelper.calculateDistance(
                    currentLat, currentLon, dest.latitude, dest.longitude
                )
                distance in minDist..maxDist
            }

            if (distanceFilteredList.isNotEmpty()) {
                _selectedDestination.value = distanceFilteredList.random()
            } else {
                _selectedDestination.value = Destination(name = "조건에 맞는 곳이 없습니다.", type = "", latitude = 0.0, longitude = 0.0)
            }
        }
    }
}