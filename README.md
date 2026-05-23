com.example.random_drive
 ┣ 📂api
 ┃ ┣ 📜RetrofitClient.kt     # Retrofit 객체 초기화 및 설정
 ┃ ┗ 📜TMapApiService.kt     # T map 경로 탐색 API 인터페이스 및 Data Class
 ┣ 📂ui
 ┃ ┗ 📜DriveViewModel.kt     # UI 상태 관리, 랜덤 로직 처리 및 API 통신 (MVVM)
 ┗ 📜MainActivity.kt         # Jetpack Compose UI (권한 요청, 화면 네비게이션)

Markdown
# 🚗 Random Drive (랜덤 드라이브)

**"어디로 갈지 고민될 때, 지금 내 위치에서 떠나는 맞춤형 랜덤 드라이브"**
현재 위치(GPS)를 기반으로 원하는 거리와 테마를 선택하면, 최적의 드라이브 코스를 무작위로 추천하고 내비게이션 길 안내까지 원터치로 연결해 주는 안드로이드 앱입니다.

---

## ✨ 주요 기능 (Key Features)

* **📍 실시간 내 위치 기반 (GPS)**
  * `FusedLocationProviderClient`를 활용하여 사용자의 현재 정확한 위경도 좌표를 수집합니다.
* **🎲 맞춤형 테마 랜덤 추천**
  * 주행 반경(km)과 선호하는 풍경 테마(바다, 산, 강, 도심)를 입력받아 조건에 부합하는 목적지를 추천합니다.
* **⏱️ T map API 기반 실시간 경로 계산**
  * T map 경로 탐색(Directions) API를 연동하여, 직선거리가 아닌 **실제 차량 주행 거리(km)**와 **예상 소요 시간(분)**을 정확하게 제공합니다.
* **🗺️ 내비게이션 딥링크(Deep Link) 다이렉트 연동**
  * 추천된 목적지 이름과 좌표를 바탕으로 **T map** 또는 **네이버 지도** 앱을 즉시 실행하여, 타이핑 없이 바로 길 안내를 시작합니다. (미설치 시 구글 플레이스토어로 이동)

---

## 🛠️ 기술 스택 (Tech Stack)

* **Language:** Kotlin
* **UI Toolkit:** Jetpack Compose (Material Design 3)
* **Architecture:** MVVM (Model-View-ViewModel)
* **Asynchronous / Reactive:** Coroutines, StateFlow
* **Network:** Retrofit2, Gson Converter
* **Location Services:** Google Play Services Location (FusedLocation)
* **Build System:** Gradle (Version Catalogs `libs.versions.toml` 적용), KSP

---

## 📱 스크린샷 (Screenshots)

| 거리 및 테마 선택 | 목적지 추천 및 내비게이션 연결 |
| :---: | :---: |
| <img src="" width="250" alt="화면1"> | <img src="" width="250" alt="화면2"> |

---

## 🚀 프로젝트 실행 방법 (How to run)

본 프로젝트는 T map Open API를 사용합니다. 프로젝트를 빌드하고 실행하기 위해서는 본인의 App Key가 필요합니다.

1. [SK Open API 포털](https://openapi.sk.com/)에서 프로젝트를 생성하고 앱 키(App Key)를 발급받습니다.
2. `ui/DriveViewModel.kt` 파일 내부의 `tmapAppKey` 변수에 발급받은 키를 입력합니다.
   ```kotlin
   // DriveViewModel.kt
   private val tmapAppKey = "본인의_TMAP_APP_KEY_입력"
