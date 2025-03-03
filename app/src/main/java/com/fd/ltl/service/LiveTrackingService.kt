package com.fd.ltl.service

import android.app.Service
import android.content.Intent
import android.os.IBinder

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.os.Build
import android.util.Log
import androidx.core.app.NotificationCompat
import com.fd.ltl.ui.MainActivity
import com.fd.ltl.R
import com.fd.ltl.data.LocationEntity
import com.fd.ltl.util.SimpleLocationHelper

class LiveTrackingService : Service() {
    private lateinit var locationHelper: SimpleLocationHelper
    private val notificationId = 1001
    private val channelId = "location_tracking_channel"

    companion object {
        private const val TAG = "LiveTrackingService"
        const val ACTION_START_TRACKING = "com.fd.ltl.START_TRACKING"
        const val ACTION_STOP_TRACKING = "com.fd.ltl.STOP_TRACKING"
    }

    override fun onCreate() {
        super.onCreate()
        locationHelper = SimpleLocationHelper(context = this, interval = 1)
        createNotificationChannel()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        when (intent?.action) {
            ACTION_START_TRACKING -> startLocationTracking()
            ACTION_STOP_TRACKING -> stopLocationTracking()
        }

        return START_STICKY
    }

    private fun startLocationTracking() {
        Log.d(TAG, "Starting location tracking")

        startForeground(notificationId, createNotification("Starting location tracking..."))

        locationHelper.startLocationUpdates { location ->
            updateLocationAndNotification(location)
            LiveTrackingServiceLiveData.state.postValue(LiveTrackingServiceLiveData.UPDATE)
        }
    }

    private fun updateLocationAndNotification(location: LocationEntity?) {
        val notificationManager = getSystemService(NOTIFICATION_SERVICE) as NotificationManager

        if (location != null) {
            val latitude = location.latitude
            val longitude = location.longitude

            // Update notification
            notificationManager.notify(
                notificationId,
                createNotification("Location: $latitude, $longitude")
            )
        } else {
            // Handle null location if needed
            notificationManager.notify(
                notificationId,
                createNotification("Location: Unavailable")
            )
        }
    }

    private fun stopLocationTracking() {
        Log.d(TAG, "Stopping location tracking")

        locationHelper.stopLocationUpdates()
        stopForeground(true)
        stopSelf()
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                channelId,
                "Location Tracking",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Used for tracking location in background"
                setShowBadge(false)
            }

            val notificationManager = getSystemService(NotificationManager::class.java)
            notificationManager.createNotificationChannel(channel)
        }
    }

    private fun createNotification(content: String): Notification {
        val pendingIntent = PendingIntent.getActivity(
            this,
            0,
            Intent(this, MainActivity::class.java),
            PendingIntent.FLAG_IMMUTABLE
        )

        return NotificationCompat.Builder(this, channelId)
            .setContentTitle("Location Tracking")
            .setContentText(content)
            .setSmallIcon(R.drawable.ic_location)
            .setContentIntent(pendingIntent)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setOngoing(true)
            .build()
    }

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onDestroy() {
        super.onDestroy()
        locationHelper.stopLocationUpdates()
    }
}