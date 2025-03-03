package com.fd.ltl.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "t_location_track")
data class LocationEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val latitude: Double,
    val longitude: Double,
    val address: String,
    val timestamp: Long,
    val formattedTime: String
)