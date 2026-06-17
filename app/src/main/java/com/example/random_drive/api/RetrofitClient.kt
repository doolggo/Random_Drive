package com.example.random_drive.api

import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

/**
 * Retrofit 인스턴스를 관리하는 싱글톤(Object) 객체
 */
object RetrofitClient {
    private const val BASE_URL = "https://apis.openapi.sk.com/"

    // lazy 초기화를 통한 메모리 최적화
    val tMapApiService: TMapApiService by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .addConverterFactory(GsonConverterFactory.create()) // JSON 파싱을 위한 Gson 컨버터
            .build()
            .create(TMapApiService::class.java)
    }
}