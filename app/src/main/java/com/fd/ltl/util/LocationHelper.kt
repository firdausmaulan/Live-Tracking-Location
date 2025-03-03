package com.fd.ltl.util
import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.pm.PackageManager
import android.location.Address
import android.location.Geocoder
import android.os.Looper
import android.util.Log
import androidx.core.app.ActivityCompat
import com.fd.ltl.data.AppDatabase
import com.fd.ltl.data.LocationEntity
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.concurrent.TimeUnit

class LocationHelper(
    private val context: Context,
    interval : Long = 2,
    accuracy : Int = Priority.PRIORITY_HIGH_ACCURACY
) {
    private val fusedLocationClient: FusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(context)
    private val locationRequest: LocationRequest =
        LocationRequest.Builder(accuracy, TimeUnit.MINUTES.toMillis(interval))
            .setWaitForAccurateLocation(false)
            .setMinUpdateIntervalMillis(TimeUnit.MINUTES.toMillis(interval - 1))
            .setMaxUpdateDelayMillis(TimeUnit.MINUTES.toMillis(interval + 1))
            .build()
    private val locationDao = AppDatabase.getDatabase(context).locationDao()
    private val coroutineScope = CoroutineScope(Dispatchers.IO)
    private var locationCallback: LocationCallback? = null

    companion object {
        private const val TAG = "LocationHelper"
    }

    @SuppressLint("MissingPermission")
    fun startLocationUpdates(callback: (LocationEntity) -> Unit) {
        if (!hasLocationPermission()) {
            Log.e(TAG, "Location permissions not granted")
            return
        }

        locationCallback = object : LocationCallback() {
            override fun onLocationResult(locationResult: LocationResult) {
                locationResult.lastLocation?.let { location ->
                    coroutineScope.launch {
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

                        // Notify callback
                        callback(locationEntity)

                        Log.d(TAG, "Location saved: $locationEntity")
                    }
                }
            }
        }

        fusedLocationClient.requestLocationUpdates(
            locationRequest,
            locationCallback!!,
            Looper.getMainLooper()
        )
    }

    fun stopLocationUpdates() {
        locationCallback?.let {
            fusedLocationClient.removeLocationUpdates(it)
            locationCallback = null
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