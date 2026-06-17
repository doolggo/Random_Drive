````text
com.example.random_drive
 ├── api/
 │   ├── RetrofitClient.kt      # Retrofit object initialization and configuration
 │   └── TMapApiService.kt      # T map Directions API interface and Data Classes
 ├── ui/
 │   └── DriveViewModel.kt      # UI state management, random logic processing, and API communication (MVVM)
 └── MainActivity.kt            # Jetpack Compose UI (Permission requests, screen navigation)
 `````
# Random Drive

**"When you're not sure where to go, a customized random drive starting right from your current location."**
An Android app that recommends an optimal, randomized drive course based on your current location (GPS), desired distance, and theme. It also connects you directly to navigation apps with a single touch.

---

## Key Features

* ** Real-time Location Based (GPS)**
  * Collects the user's exact current latitude and longitude coordinates using `FusedLocationProviderClient`.
* ** Customized Theme Random Recommendation**
  * Recommends destinations that match your conditions by taking the driving radius (km) and preferred scenery theme (Ocean, Mountain, River, City) as inputs.
* ** Real-time Route Calculation based on T map API**
  * Integrates the T map Directions API to provide accurate actual driving distance (km) and estimated travel time (minutes), rather than just a straight-line distance.
* ** Direct Navigation Deep Link Integration**
  * Instantly launches the **T map** or **Naver Map** app based on the recommended destination's name and coordinates, starting route guidance immediately without any typing. (Redirects to Google Play Store if not installed).

---

## Tech Stack

* **Language:** Kotlin
* **UI Toolkit:** Jetpack Compose (Material Design 3)
* **Architecture:** MVVM (Model-View-ViewModel)
* **Asynchronous / Reactive:** Coroutines, StateFlow
* **Network:** Retrofit2, Gson Converter
* **Location Services:** Google Play Services Location (FusedLocation)
* **Build System:** Gradle (Version Catalogs `libs.versions.toml` applied), KSP

---

## Screenshots

| Distance & Theme Selection | Destination Recommendation & Navigation |
| :---: | :---: |
<img width="1768" height="2208" alt="Screenshot_20260617_174719_Random_Drive" src="https://github.com/user-attachments/assets/691437ea-c424-45b0-baa5-8c20ea00cb76" />|<img width="1768" height="2208" alt="Screenshot_20260617_174748_Random_Drive" src="https://github.com/user-attachments/assets/ac176ba8-f60e-4439-84b9-dc165870d706" />




----------------------

## How to Run

This project uses the T map Open API. To build and run the project, you will need your own App Key.

1. Create a project on the [SK Open API Portal](https://openapi.sk.com/) and issue an App Key.
2. Enter the issued key into the `tmapAppKey` variable inside the `ui/DriveViewModel.kt` file.
   // DriveViewModel.kt
   private val tmapAppKey = "YOUR_TMAP_APP_KEY_HERE"
