package com.example.random_drive.ui

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.random_drive.api.RetrofitClient
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import java.util.Locale

// 화면에 전달할 목적지 데이터 구조
data class Destination(val name: String, val lat: Double, val lon: Double)

class DriveViewModel : ViewModel() {
    private val _selectedDestination = MutableStateFlow<Destination?>(null)
    val selectedDestination: StateFlow<Destination?> = _selectedDestination

    // 🚨 주의: 원래는 보안을 위해 숨겨야 하지만, 테스트를 위해 여기에 직접 적어줍니다.
    // 🔒 BuildConfig를 통해 금고에서 안전하게 키를 불러옵니다.
    private val tmapAppKey = com.example.random_drive.BuildConfig.TMAP_API_KEY

    fun findRandomDestination(currentLat: Double, currentLon: Double, minDist: Double, maxDist: Double, type: String) {
        viewModelScope.launch {
            try {
                // 1. 임시 목적지 설정 (나중에는 'type'에 맞는 장소의 좌표를 랜덤으로 뽑아올 예정입니다)
                // 현재 테스트용 임시 목적지: 잠실 롯데월드
                val targetLat = 37.5113
                val targetLon = 127.0980
                val targetName = "잠실 롯데월드 ($type)"

                // 2. 티맵 서버에 길 찾기 요청 (진짜 통신 시작!)
                Log.d("TMapAPI", "티맵 서버에 경로를 요청합니다...")
                val response = RetrofitClient.tMapApiService.getRoute(
                    appKey = tmapAppKey,
                    startX = currentLon.toString(), // 💡 티맵 API는 경도(X), 위도(Y) 순서로 넣어야 합니다!
                    startY = currentLat.toString(),
                    endX = targetLon.toString(),
                    endY = targetLat.toString()
                )

                // 3. 서버에서 준 결과물 알짜배기만 뽑아내기
                val properties = response.features.firstOrNull()?.properties
                if (properties != null) {
                    val distanceKm = properties.totalDistance / 1000.0 // 미터를 킬로미터로 변환
                    val timeMin = properties.totalTime / 60            // 초를 분으로 변환

                    Log.d("TMapAPI", "성공! 거리: ${distanceKm}km, 시간: ${timeMin}분")

                    // 4. 화면(MainActivity)에 보여주기 위해 데이터 업데이트
                    _selectedDestination.value = Destination(
                        name = "$targetName\n(주행 약 ${timeMin}분, ${String.format(Locale.US, "%.1f", distanceKm)}km)",
                        lat = targetLat,
                        lon = targetLon
                    )
                }

            } catch (e: Exception) {
                // 인터넷이 끊겼거나 키가 틀렸을 때 에러 처리
                Log.e("TMapAPI", "API 통신 에러 발생: ${e.message}")
                _selectedDestination.value = Destination("경로 탐색 실패 😢\n인터넷이나 키를 확인해주세요.", 0.0, 0.0)
            }
        }
    }
}