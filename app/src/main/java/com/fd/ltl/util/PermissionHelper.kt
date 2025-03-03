package com.fd.ltl.util

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import androidx.activity.ComponentActivity
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat

class PermissionHelper(
    private val activity: ComponentActivity,
    private val onPermissionResult: (Boolean) -> Unit
) {
    private val requestPermissionLauncher: ActivityResultLauncher<Array<String>> =
        activity.registerForActivityResult(
            ActivityResultContracts.RequestMultiplePermissions()
        ) { permissions ->
            val allGranted = permissions.entries.all { it.value }
            onPermissionResult(allGranted)
        }

    /**
     * Checks if all necessary permissions are granted and requests them if not
     * @return true if all permissions are already granted, false if permissions were requested
     */
    fun checkAndRequestPermissions(): Boolean {
        val permissions = mutableListOf(
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.ACCESS_COARSE_LOCATION
        )

        // Add notification permission for Android 13+
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            permissions.add(Manifest.permission.POST_NOTIFICATIONS)
        }

        val permissionsToRequest = permissions.filter {
            ContextCompat.checkSelfPermission(activity, it) != PackageManager.PERMISSION_GRANTED
        }.toTypedArray()

        return if (permissionsToRequest.isEmpty()) {
            // All permissions are already granted
            true
        } else {
            // Request permissions
            requestPermissionLauncher.launch(permissionsToRequest)
            false
        }
    }

    /**
     * Checks if a specific permission is granted
     */
    fun isPermissionGranted(permission: String): Boolean {
        return ContextCompat.checkSelfPermission(activity, permission) ==
                PackageManager.PERMISSION_GRANTED
    }

    /**
     * Checks if location permissions are granted
     */
    fun areLocationPermissionsGranted(): Boolean {
        return isPermissionGranted(Manifest.permission.ACCESS_FINE_LOCATION) ||
                isPermissionGranted(Manifest.permission.ACCESS_COARSE_LOCATION)
    }
}