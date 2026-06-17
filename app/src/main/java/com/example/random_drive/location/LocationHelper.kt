package com.example.random_drive.location

import android.location.Location

/**
 * 위치 및 거리 계산 유틸리티 객체
 */
object LocationHelper {
    // 두 좌표 간의 직선거리를 계산 (하버사인 공식 기반 안드로이드 기본 API 사용)
    fun calculateDistance(lat1: Double, lon1: Double, lat2: Double, lon2: Double): Double {
        val results = FloatArray(1)
        Location.distanceBetween(lat1, lon1, lat2, lon2, results)
        return results[0] / 1000.0 // 계산 결과를 km 단위로 변환하여 반환
    }
}