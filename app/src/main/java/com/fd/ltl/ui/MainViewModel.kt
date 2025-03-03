package com.fd.ltl.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.fd.ltl.data.AppDatabase
import com.fd.ltl.data.LocationEntity
import com.fd.ltl.service.LiveTrackingServiceLiveData
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.util.Calendar

class MainViewModel : ViewModel() {

    // Private mutable state for today's locations list
    private val _todayLocations = MutableStateFlow<List<LocationEntity>>(emptyList())
    val todayLocations: StateFlow<List<LocationEntity>> = _todayLocations.asStateFlow()

    private val _isTrackingActive = MutableStateFlow(false)
    val isTrackingActive: StateFlow<Boolean> = _isTrackingActive.asStateFlow()

    // Database reference
    private lateinit var database: AppDatabase

    fun initialize(db: AppDatabase) {
        database = db
        loadTodayLocations()
    }

    fun setTrackingActive(state : String) {
        _isTrackingActive.value = state != LiveTrackingServiceLiveData.STOP
    }

    /**
     * Loads all locations recorded today
     */
    fun loadTodayLocations() {
        viewModelScope.launch {
            val calendar = Calendar.getInstance()
            calendar.set(Calendar.HOUR_OF_DAY, 0)
            calendar.set(Calendar.MINUTE, 0)
            calendar.set(Calendar.SECOND, 0)
            calendar.set(Calendar.MILLISECOND, 0)
            val startOfDayTimestamp = calendar.timeInMillis

            val todayLocations = database.locationDao().getLocationsFromToday(startOfDayTimestamp)
            _todayLocations.value = todayLocations
        }
    }
}