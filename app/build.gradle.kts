import java.util.Properties

// app/build.gradle.kts
plugins {
    alias(libs.plugins.android.application)
    id("org.jetbrains.kotlin.android")
    alias(libs.plugins.kotlin.compose)
    id("com.google.devtools.ksp")
}

android {
    namespace = "com.example.random_drive"
    // ... (기존 compileSdk 등 그대로 유지) ...

    // local.properties 파일(금고)을 열어서 읽어올 준비를 합니다.
    val properties = Properties()
    properties.load(project.rootProject.file("local.properties").inputStream())

    defaultConfig {
        // ... (기존 applicationId, minSdk 등 그대로 유지) ...

        // 금고에서 TMAP_API_KEY를 꺼내서 앱이 쓸 수 있는 BuildConfig 변수로 만듭니다. (쌍따옴표 처리 필수)
        buildConfigField("String", "TMAP_API_KEY", "\"${properties.getProperty("TMAP_API_KEY")}\"")
    }

    buildFeatures {
        compose = true
        buildConfig = true
    }
    namespace = "com.example.random_drive"
    compileSdk = 36

    defaultConfig {
        applicationId = "com.example.random_drive"
        minSdk = 26
        targetSdk = 36
        versionCode = 1
        versionName = "1.0"
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    // 최신 문법: compilerOptions 적용
    kotlin {
        compilerOptions {
            jvmTarget.set(org.jetbrains.kotlin.gradle.dsl.JvmTarget.JVM_11)
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }

    buildFeatures {
        compose = true
    }
}

dependencies {
    // 1. 기본 Compose 및 Lifecycle 설정
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.activity.compose)
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.compose.ui)
    implementation(libs.androidx.compose.ui.graphics)
    implementation(libs.androidx.compose.ui.tooling.preview)
    implementation(libs.androidx.compose.material3)

    // 2. 외부 라이브러리 (카탈로그 전용 사용)
    implementation(libs.retrofit)
    implementation(libs.retrofit.gson)
    implementation(libs.play.services.location)
    implementation(libs.androidx.lifecycle.viewmodel.compose)
    implementation(libs.kotlinx.coroutines.android)

    // 3. Room (로컬 DB - 카탈로그 전용 사용)
    implementation(libs.androidx.room.runtime)
    implementation(libs.androidx.room.ktx)
    ksp(libs.androidx.room.compiler)
}