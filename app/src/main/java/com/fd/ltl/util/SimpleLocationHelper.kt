package com.fd.ltl.util

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.pm.PackageManager
import android.location.Address
import android.location.Geocoder
import android.location.Location
import android.util.Log
import androidx.core.app.ActivityCompat
import com.fd.ltl.data.AppDatabase
import com.fd.ltl.data.LocationEntity
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.concurrent.TimeUnit
import kotlin.coroutines.resume
import kotlinx.coroutines.suspendCancellableCoroutine as suspendCancellableCoroutine1

class SimpleLocationHelper(
    private val context: Context,
    private val interval: Long = 2,
    private val accuracy: Int = Priority.PRIORITY_HIGH_ACCURACY
) {
    private val fusedLocationClient: FusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(context)
    private val locationDao = AppDatabase.getDatabase(context).locationDao()
    private val coroutineScope = CoroutineScope(Dispatchers.IO)
    private var locationUpdateJob: Job? = null

    companion object {
        private const val TAG = "LocationHelper"
    }

    /**
     * Start periodic location updates using getCurrentLocation with a specified delay
     * Falls back to lastLocation if getCurrentLocation fails
     */
    fun startLocationUpdates(callback: (LocationEntity?) -> Unit) {
        // Cancel any existing job first
        stopLocationUpdates()

        locationUpdateJob = coroutineScope.launch {
            while (isActive) {
                fetchLocation(callback)
                delay(TimeUnit.MINUTES.toMillis(interval))
            }
        }
    }

    /**
     * Stop all location updates
     */
    fun stopLocationUpdates() {
        locationUpdateJob?.cancel()
        locationUpdateJob = null
    }

    @SuppressLint("MissingPermission")
    private suspend fun fetchLocation(callback: (LocationEntity?) -> Unit) {
        if (!hasLocationPermission()) {
            Log.e(TAG, "Location permissions not granted")
            withContext(Dispatchers.Main) {
                callback(null)
            }
            return
        }

        try {
            // First try to get location using getCurrentLocation
            var location = tryGetCurrentLocation()

            // If getCurrentLocation failed, fall back to lastLocation
            if (location == null) {
                Log.d(TAG, "getCurrentLocation failed, falling back to lastLocation")
                location = tryGetLastLocation()
            }

            if (location != null) {
                val timestamp = System.currentTimeMillis()
                val formattedTime = SimpleDateFormat("dd-MMM-yyyy HH:mm:ss", Locale.getDefault())
                    .format(Date(timestamp))
                val address = getAddressFromLocation(location.latitude, location.longitude)

                val locationEntity = LocationEntity(
                    latitude = location.latitude,
                    longitude = location.longitude,
                    address = address,
                    timestamp = timestamp,
                    formattedTime = formattedTime
                )

                // Save to database
                locationDao.insertLocation(locationEntity)

                // Notify callback on main thread
                withContext(Dispatchers.Main) {
                    callback(locationEntity)
                }

                Log.d(TAG, "Location saved: $locationEntity")
            } else {
                Log.d(TAG, "Both getCurrentLocation and lastLocation failed")
                withContext(Dispatchers.Main) {
                    callback(null)
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error in fetchLocation: ${e.message}")
            withContext(Dispatchers.Main) {
                callback(null)
            }
        }
    }

    @SuppressLint("MissingPermission")
    private suspend fun tryGetCurrentLocation(): Location? {
        return try {
            suspendCancellableCoroutine1 { continuation ->
                fusedLocationClient.getCurrentLocation(accuracy, null)
                    .addOnSuccessListener { location ->
                        Log.d(TAG, "getCurrentLocation succeeded")
                        continuation.resume(location)
                    }
                    .addOnFailureListener { exception ->
                        Log.e(TAG, "getCurrentLocation failed: ${exception.message}")
                        continuation.resume(null)
                    }
                    .addOnCanceledListener {
                        Log.d(TAG, "getCurrentLocation was cancelled")
                        if (continuation.isActive) continuation.resume(null)
                    }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Exception in tryGetCurrentLocation: ${e.message}")
            null
        }
    }

    @SuppressLint("MissingPermission")
    private suspend fun tryGetLastLocation(): Location? {
        return try {
            suspendCancellableCoroutine1 { continuation ->
                fusedLocationClient.lastLocation
                    .addOnSuccessListener { location ->
                        if (location != null) {
                            Log.d(TAG, "lastLocation succeeded")
                        } else {
                            Log.d(TAG, "lastLocation returned null")
                        }
                        continuation.resume(location)
                    }
                    .addOnFailureListener { exception ->
                        Log.e(TAG, "lastLocation failed: ${exception.message}")
                        continuation.resume(null)
                    }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Exception in tryGetLastLocation: ${e.message}")
            null
        }
    }

    private fun getAddressFromLocation(latitude: Double, longitude: Double): String {
        try {
            val geocoder = Geocoder(context, Locale.getDefault())

            @Suppress("DEPRECATION")
            val addresses: List<Address>? = geocoder.getFromLocation(latitude, longitude, 1)

            if (!addresses.isNullOrEmpty()) {
                val address = addresses[0]
                val addressParts = mutableListOf<String>()

                for (i in 0..address.maxAddressLineIndex) {
                    addressParts.add(address.getAddressLine(i))
                }

                return addressParts.joinToString(", ")
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error getting address: ${e.message}")
        }

        return "Unknown location"
    }

    private fun hasLocationPermission(): Boolean {
        return ActivityCompat.checkSelfPermission(
            context,
            Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED || ActivityCompat.checkSelfPermission(
            context,
            Manifest.permission.ACCESS_COARSE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED
    }
}