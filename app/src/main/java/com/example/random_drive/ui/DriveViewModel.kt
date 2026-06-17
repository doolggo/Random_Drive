package com.example.random_drive.ui

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.random_drive.BuildConfig
import com.example.random_drive.api.RetrofitClient
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import java.util.Locale

// 1. 화면에 넘겨줄 최종 목적지 데이터 (좌표 필수)
data class Destination(val name: String, val lat: Double, val lon: Double, val type: String)

// 2. 조원분이 만든 카테고리-장소 데이터 구조
data class PlaceItem(val category: String, val placeName: String)

class DriveViewModel : ViewModel() {
    private val _selectedDestination = MutableStateFlow<Destination?>(null)
    val selectedDestination: StateFlow<Destination?> = _selectedDestination

    // 안전한 금고에서 앱키 가져오기
    private val tmapAppKey = BuildConfig.TMAP_API_KEY

    // ⭐️ 조원분이 새롭게 업데이트한 DB 리스트 적용 완료!
    private val destinationList = listOf(
        PlaceItem("바다", "을왕리 해수욕장"),
        PlaceItem("바다", "왕산 해수욕장"),
        PlaceItem("바다", "무의도 하나개 해수욕장"),
        PlaceItem("바다", "방아머리해변"),
        PlaceItem("바다", "제부도 해수욕장"),
        PlaceItem("바다", "탄도항 갯벌"),
        PlaceItem("바다", "덕적도 해수욕장"),
        PlaceItem("바다", "십리포 해수욕장"),
        PlaceItem("바다", "구봉도 낙조 전망대"),
        PlaceItem("바다", "선재도 갯벌 체험장"),
        PlaceItem("바다", "시흥 오이도 빨강등대"),
        PlaceItem("바다", "인천 소래포구"),
        PlaceItem("바다", "영종도 선녀바위 해수욕장"),
        PlaceItem("바다", "대부도 팬션촌 해변"),
        PlaceItem("바다", "화성 궁평항"),
        PlaceItem("바다", "인천 송도 달빛축제공원 해변"),
        PlaceItem("바다", "강화도 동막해수욕장"),
        PlaceItem("바다", "강화도 마니산 해변"),
        PlaceItem("바다", "인천 진두 해수욕장"),
        PlaceItem("바다", "인천 장경리 해수욕장"),
        PlaceItem("산", "북한산"),
        PlaceItem("산", "관악산"),
        PlaceItem("산", "청계산"),
        PlaceItem("산", "도봉산"),
        PlaceItem("산", "수락산"),
        PlaceItem("산", "불암산"),
        PlaceItem("산", "아차산"),
        PlaceItem("산", "남한산성"),
        PlaceItem("산", "용마산"),
        PlaceItem("산", "마니산"),
        PlaceItem("산", "광교산"),
        PlaceItem("산", "백운산"),
        PlaceItem("산", "운악산"),
        PlaceItem("산", "감악산"),
        PlaceItem("산", "용문산"),
        PlaceItem("도심", "남산타워"),
        PlaceItem("도심", "롯데월드타워"),
        PlaceItem("도심", "63빌딩"),
        PlaceItem("도심", "광화문 광장"),
        PlaceItem("도심", "경복궁"),
        PlaceItem("도심", "청와대 앞길"),
        PlaceItem("도심", "동대문 디자인플라자"),
        PlaceItem("도심", "코엑스몰&스타필드 코엑스 아쿠아리움"),
        PlaceItem("도심", "잠실 롯데월드"),
        PlaceItem("도심", "홍대 상상마당"),
        PlaceItem("도심", "서울숲"),
        PlaceItem("강", "여의도 한강공원"),
        PlaceItem("강", "반포 한강공원"),
        PlaceItem("강", "잠원 한강공원"),
        PlaceItem("강", "뚝섬 한강공원"),
        PlaceItem("강", "망원 한강공원"),
        PlaceItem("강", "난지 한강공원"),
        PlaceItem("강", "이촌 한강공원"),
        PlaceItem("강", "양화 한강공원"),
        PlaceItem("강", "강서 한강공원"),
        PlaceItem("강", "구리 한강공원"),
        PlaceItem("도심", "국회의사당"),
        PlaceItem("도심", "서울역"),
        PlaceItem("도심", "스타필드 하남"),
        PlaceItem("도심", "스타필드 고양"),
        PlaceItem("도심", "인천 차이나타운"),
        PlaceItem("도심", "송도 컨벤시아"),
        PlaceItem("도심", "경리단길"),
        PlaceItem("도심", "서울 올림픽주경기장"),
        PlaceItem("도심", "에버랜드"),
        PlaceItem("도심", "익선동 한옥거리"),
        PlaceItem("도심", "북촌한옥마을"),
        PlaceItem("도심", "청계천"),
        PlaceItem("강", "세빛섬"),
        PlaceItem("강", "노들섬"),
        PlaceItem("도심", "성수동 카페거리"),
        PlaceItem("도심", "더현대 서울"),
        PlaceItem("도심", "IFC몰"),
        PlaceItem("도심", "인천대교"),
        PlaceItem("도심", "월미도"),
        PlaceItem("도심", "송월동 동화마을"),
        PlaceItem("도심", "아라뱃길"),
        PlaceItem("도심", "일산 호수공원"),
        PlaceItem("도심", "아쿠아플라넷 일산"),
        PlaceItem("도심", "압구정 로데오거리"),
        PlaceItem("도심", "예술의전당"),
        PlaceItem("도심", "올림픽공원"),
        PlaceItem("도심", "낙산동원"),
        PlaceItem("도심", "서울식물원"),
        PlaceItem("도심", "보라매공원"),
        PlaceItem("도심", "용산공원"),
        PlaceItem("도심", "송도 센트럴파크"),
        PlaceItem("도심", "노량진 수산시장"),
        PlaceItem("도심", "현대프리미엄아울렛 송도"),
        PlaceItem("도심", "임진각 평화누리공원"),
        PlaceItem("도심", "헤이리 예술마을"),
        PlaceItem("도심", "프로방스마을"),
        PlaceItem("도심", "파주 출판도시"),
        PlaceItem("도심", "롯데프리미엄아울렛 파주"),
        PlaceItem("도심", "퍼스트가든"),
        PlaceItem("도심", "마장호수 출렁다리"),
        PlaceItem("도심", "오두산 통일전망대"),
        PlaceItem("도심", "국립민속박물관 파주"),
        PlaceItem("도심", "행주산성"),
        PlaceItem("도심", "캐리비안베이")
    )

    fun findRandomDestination(currentLat: Double, currentLon: Double, minDist: Double, maxDist: Double, selectedType: String) {
        viewModelScope.launch {
            // 버튼을 누르자마자 즉시 화면을 로딩 상태로 변경하여 이전 찌꺼기나 에러 문구를 지웁니다.
            _selectedDestination.value = Destination(
                name = "목적지를 찾는 중입니다...\n잠시만 기다려 주세요. 🚗💨",
                lat = 0.0,
                lon = 0.0,
                type = ""
            )

            try {
                // 1. 선택한 테마에 맞는 장소들을 거른 뒤, 무작위 순서로 섞습니다(.shuffled())
                val filteredList = destinationList.filter { it.category == selectedType }.shuffled()

                if (filteredList.isEmpty()) {
                    _selectedDestination.value = Destination("해당 테마의 목적지가 없습니다.", 0.0, 0.0, "")
                    return@launch
                }

                var isMatched = false

                // 2. 조건에 맞는 장소가 나올 때까지 섞인 리스트 전체를 순서대로 탐색합니다.
                for ((i, currentPick) in filteredList.withIndex()) {
                    val targetName = currentPick.placeName

                    Log.d("TMapAPI", "${i + 1}번째 후보 탐색 중: '$targetName'")

                    // 3. 티맵 API로 후보 장소의 좌표 검색
                    val poiResponse = RetrofitClient.tMapApiService.searchPoi(
                        appKey = tmapAppKey,
                        keyword = targetName
                    )

                    val firstPoi = poiResponse.searchPoiInfo.pois.poi.firstOrNull() ?: continue
                    val targetLat = firstPoi.noorLat.toDouble()
                    val targetLon = firstPoi.noorLon.toDouble()

                    // 4. 해당 좌표까지의 실제 주행 거리 계산
                    val routeResponse = RetrofitClient.tMapApiService.getRoute(
                        appKey = tmapAppKey,
                        startX = currentLon.toString(),
                        startY = currentLat.toString(),
                        endX = targetLon.toString(),
                        endY = targetLat.toString()
                    )

                    val properties = routeResponse.features.firstOrNull()?.properties
                    if (properties != null) {
                        val distanceKm = properties.totalDistance / 1000.0
                        val timeMin = properties.totalTime / 60

                        Log.d("TMapAPI", "-> 후보 '$targetName'의 실제 주행거리: ${distanceKm}km")

                        // 5. 실제 주행거리가 사용자가 설정한 범위(minDist ~ maxDist) 사이에 있는지 확인
                        if (distanceKm in minDist..maxDist) {
                            Log.d("TMapAPI", "🎉 조건 일치 목적지 발견! 화면에 표출합니다.")

                            _selectedDestination.value = Destination(
                                name = "$targetName\n(주행 약 ${timeMin}분, ${String.format(Locale.US, "%.1f", distanceKm)}km)",
                                lat = targetLat,
                                lon = targetLon,
                                type = selectedType
                            )
                            isMatched = true
                            break // 완벽한 목적지를 찾았으므로 반복문을 즉시 중단하고 탈출합니다!
                        } else {
                            Log.d("TMapAPI", "-> 설정한 거리 범위(${minDist}~${maxDist}km)를 벗어나 탈락. 다음 후보로 넘어갑니다.")
                        }
                    }
                }

                // 6. 리스트를 끝까지 다 뒤졌는데도 조건에 맞는 장소가 주변에 단 하나도 없는 경우
                if (!isMatched) {
                    _selectedDestination.value = Destination(
                        name = "선택하신 거리 조건(${minDist.toInt()}~${maxDist.toInt()}km)에 맞는 장소를 주변에서 찾지 못했습니다. 😢\n다시 시도하거나 거리 설정을 넓혀주세요.",
                        lat = 0.0,
                        lon = 0.0,
                        type = ""
                    )
                }

            } catch (e: Exception) {
                Log.e("TMapAPI", "API 통신 에러 발생: ${e.message}")
                _selectedDestination.value = Destination("경로 탐색 실패 😢\n인터넷을 확인하거나 고객센터에 문의하세요.", 0.0, 0.0, "")
            }
        }
    }
}