# Android Live Location Tracking App

This Android application demonstrates live location tracking using Google's Fused Location Provider, along with various modern Android development practices and Jetpack Compose.

<img src="https://github.com/firdausmaulan/Live-Tracking-Location/blob/master/result/live-track.jpeg" width="250">

## Features

* **Real-time Location Tracking:** Utilizes Google's Fused Location Provider for accurate and efficient location updates.
* **Foreground Service:** Runs location tracking in a foreground service to ensure continuous operation even when the app is in the background.
* **Notifications:** Provides user-visible notifications to indicate ongoing tracking and service status.
* **Geocoder Integration:** Converts latitude and longitude coordinates into human-readable addresses.
* **Local Data Persistence:** Stores location data in a Room database for offline access and historical tracking.
* **Shared LiveData:** Employs Shared LiveData to communicate the state of the LiveTrackingService (START, STOP, UPDATE) to the UI.
* **Jetpack Compose UI:** Builds a modern and declarative user interface using Jetpack Compose.
* **Coroutines:** Leverages Kotlin coroutines for asynchronous operations, improving performance and responsiveness.
* **ViewModel:** Implements ViewModel to manage UI-related data in a lifecycle-aware manner.
* **Dynamic UI Updates:**
    * Changes the floating action button icon on the MainScreen based on the LiveTrackingService state:
        * **START:** Displays a "Close" icon.
        * **STOP:** Displays a "Play" icon.
    * Updates the location data list on the MainScreen whenever a new location update is received (UPDATE).