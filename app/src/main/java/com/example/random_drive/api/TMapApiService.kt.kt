package com.example.random_drive.api

import retrofit2.http.Field
import retrofit2.http.FormUrlEncoded
import retrofit2.http.Header
import retrofit2.http.POST

// 1. 서버가 보내주는 응답(Response) 구조 정의
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

// 2. 서버에 보낼 요청(Request) 규격 정의
interface TMapApiService {
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