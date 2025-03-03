package com.fd.ltl.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query

@Dao
interface LocationDao {
    @Insert
    suspend fun insertLocation(location: LocationEntity)

    @Query("SELECT * FROM t_location_track ORDER BY timestamp DESC")
    suspend fun getAllLocations(): List<LocationEntity>

    @Query("SELECT * FROM t_location_track ORDER BY timestamp DESC LIMIT 1")
    suspend fun getLatestLocation(): LocationEntity?

    // New method to get locations from today
    @Query("SELECT * FROM t_location_track WHERE timestamp >= :startOfDay ORDER BY timestamp DESC")
    suspend fun getLocationsFromToday(startOfDay: Long): List<LocationEntity>
}