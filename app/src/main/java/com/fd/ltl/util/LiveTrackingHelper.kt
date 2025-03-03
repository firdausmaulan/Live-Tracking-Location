package com.fd.ltl.util

import android.content.Context
import android.content.Intent
import android.os.Build
import android.widget.Toast
import com.fd.ltl.service.LiveTrackingService
import com.fd.ltl.service.LiveTrackingServiceLiveData

/**
 * Helper class to handle location tracking service operations
 */
class LiveTrackingHelper(private val context: Context) {

    /**
     * Starts the location tracking service
     */
    fun startLocationService() {
        val intent = Intent(context, LiveTrackingService::class.java).apply {
            action = LiveTrackingService.ACTION_START_TRACKING
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            context.startForegroundService(intent)
        } else {
            context.startService(intent)
        }

        Toast.makeText(context, "Location tracking started", Toast.LENGTH_SHORT).show()
        LiveTrackingServiceLiveData.state.postValue(LiveTrackingServiceLiveData.START)
    }

    /**
     * Stops the location tracking service
     */
    fun stopLocationService() {
        val intent = Intent(context, LiveTrackingService::class.java).apply {
            action = LiveTrackingService.ACTION_STOP_TRACKING
        }
        context.stopService(intent)

        Toast.makeText(context, "Location tracking stopped", Toast.LENGTH_SHORT).show()
        LiveTrackingServiceLiveData.state.postValue(LiveTrackingServiceLiveData.STOP)
    }
}