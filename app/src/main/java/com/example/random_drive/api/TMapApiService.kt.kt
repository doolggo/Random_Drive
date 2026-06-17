package com.example.random_drive.api

import retrofit2.http.Field
import retrofit2.http.FormUrlEncoded
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.POST
import retrofit2.http.Query

// ------------------------------------
// 1. 길 찾기(Route) API 응답 데이터 구조 (질문자님 기존 코드 유지!)
// ------------------------------------
data class TMapResponse(
    val features: List<Feature>
)

data class Feature(
    val properties: PropertyDetails
)

data class PropertyDetails(
    val totalDistance: Int, // 총 거리 (미터 단위)
    val totalTime: Int,     // 총 시간 (초 단위)
    val totalFare: Int,     // 총 통행 요금
    val taxiFare: Int       // 예상 택시 요금
)

// ------------------------------------
// 2. 장소 검색(POI) API 응답 데이터 구조 (새로 추가)
// ------------------------------------
data class PoiResponse(val searchPoiInfo: SearchPoiInfo)
data class SearchPoiInfo(val pois: Pois)
data class Pois(val poi: List<PoiItem>)
data class PoiItem(
    val name: String,
    val noorLat: String, // 티맵 API는 위도를 noorLat으로 줍니다.
    val noorLon: String  // 티맵 API는 경도를 noorLon으로 줍니다.
)

interface TMapApiService {

    // [NEW] 장소 이름으로 좌표 찾기 API (이건 GET 방식이 맞습니다)
    @GET("tmap/pois?version=1&format=json")
    suspend fun searchPoi(
        @Header("appKey") appKey: String,
        @Query("searchKeyword") keyword: String
    ): PoiResponse

    // [기존] 좌표로 주행 거리와 시간 찾기 API (작성하셨던 POST 방식이 맞습니다!)
    @FormUrlEncoded
    @POST("tmap/routes?version=1&format=json")
    suspend fun getRoute(
        @Header("appKey") appKey: String,
        @Field("startX") startX: String,
        @Field("startY") startY: String,
        @Field("endX") endX: String,
        @Field("endY") endY: String,
        @Field("reqCoordType") reqCoordType: String = "WGS84GEO",
        @Field("resCoordType") resCoordType: String = "WGS84GEO",
        @Field("searchOption") searchOption: String = "0", // 0: 교통최적+추천
        @Field("trafficInfo") trafficInfo: String = "N"
    ): TMapResponse
}