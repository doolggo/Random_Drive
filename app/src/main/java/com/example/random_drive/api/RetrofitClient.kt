package com.example.random_drive.api

import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object RetrofitClient {
    private const val BASE_URL = "https://apis.openapi.sk.com/"

    val tMapApiService: TMapApiService by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .addConverterFactory(GsonConverterFactory.create()) // JSON을 코틀린 객체로 자동 변환
            .build()
            .create(TMapApiService::class.java) // 👈 .make를 .java로 수정했습니다!
    }
}