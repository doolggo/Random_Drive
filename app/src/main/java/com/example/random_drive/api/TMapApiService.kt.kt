package com.example.random_drive.api

import retrofit2.http.Field
import retrofit2.http.FormUrlEncoded
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.POST
import retrofit2.http.Query

// POI 장소 검색 API 응답 데이터 모델
data class PoiResponse(val searchPoiInfo: SearchPoiInfo)
data class SearchPoiInfo(val pois: Pois)
data class Pois(val poi: List<PoiItem>)
data class PoiItem(
    val name: String,
    val noorLat: String, // TMap API 기준 위도
    val noorLon: String  // TMap API 기준 경도
)

// 길 찾기(Route) API 응답 데이터 모델
data class TMapResponse(val features: List<Feature>)
data class Feature(val properties: PropertyDetails)
data class PropertyDetails(
    val totalDistance: Int, // 총 거리 (미터 단위)
    val totalTime: Int,     // 총 시간 (초 단위)
    val totalFare: Int,     // 총 통행 요금
    val taxiFare: Int       // 예상 택시 요금
)

/**
 * TMap API 통신을 위한 Retrofit 인터페이스 정의
 */
interface TMapApiService {

    // 장소 명칭(Keyword) 기반 좌표 조회
    @GET("tmap/pois?version=1&format=json")
    suspend fun searchPoi(
        @Header("appKey") appKey: String,
        @Query("searchKeyword") keyword: String
    ): PoiResponse

    // 좌표 기반 경로 탐색 (거리 및 소요 시간 도출)
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
        @Field("searchOption") searchOption: String = "0", // 0: 추천 및 교통최적화
        @Field("trafficInfo") trafficInfo: String = "N"
    ): TMapResponse
}