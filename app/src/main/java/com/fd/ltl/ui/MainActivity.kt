package com.fd.ltl.ui

import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import com.fd.ltl.data.AppDatabase
import com.fd.ltl.service.LiveTrackingServiceLiveData
import com.fd.ltl.util.LiveTrackingHelper
import com.fd.ltl.util.LocationHelper
import com.fd.ltl.util.PermissionHelper

class MainActivity : ComponentActivity() {
    private lateinit var locationHelper: LocationHelper
    private lateinit var permissionHelper: PermissionHelper
    private lateinit var liveTrackingHelper: LiveTrackingHelper
    private val viewModel: MainViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Initialize helpers
        locationHelper = LocationHelper(this)
        liveTrackingHelper = LiveTrackingHelper(this)

        // Initialize the database for the ViewModel
        val database = AppDatabase.getDatabase(this)
        viewModel.initialize(database)

        // Initialize permission helper with callback
        permissionHelper = PermissionHelper(this) { permissionsGranted ->
            if (permissionsGranted) {
                viewModel.loadTodayLocations()
            } else {
                Toast.makeText(
                    this,
                    "Location permissions are needed for tracking",
                    Toast.LENGTH_LONG
                ).show()
            }
        }

        setContent {
            MaterialTheme {
                Surface(
                    modifier = Modifier,
                    color = MaterialTheme.colorScheme.background
                ) {
                    MainScreen(
                        viewModel = viewModel,
                        onStartTracking = {
                            if (permissionHelper.checkAndRequestPermissions()) {
                                liveTrackingHelper.startLocationService()
                            }
                        },
                        onStopTracking = {
                            liveTrackingHelper.stopLocationService()
                            viewModel.loadTodayLocations()
                        }
                    )
                }
            }
        }

        // Check permissions on startup
        if (permissionHelper.checkAndRequestPermissions()) {
            viewModel.loadTodayLocations()
        }

        LiveTrackingServiceLiveData.state.observe(this) { state ->
            if (state != LiveTrackingServiceLiveData.START) {
                viewModel.loadTodayLocations()
            }
            viewModel.setTrackingActive(state)
        }
    }

    override fun onResume() {
        super.onResume()
        if (permissionHelper.checkAndRequestPermissions()) {
            viewModel.loadTodayLocations()
        }
    }
}