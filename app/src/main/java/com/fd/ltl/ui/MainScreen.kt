package com.fd.ltl.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.fd.ltl.data.LocationEntity

@Composable
fun MainScreen(
    viewModel: MainViewModel,
    onStartTracking: () -> Unit,
    onStopTracking: () -> Unit,
) {
    val todayLocations by viewModel.todayLocations.collectAsState()
    val isTrackingActive by viewModel.isTrackingActive.collectAsState()

    Scaffold(
        floatingActionButton = {
            FloatingActionButton(
                onClick = {
                    if (isTrackingActive) {
                        onStopTracking()
                    } else {
                        onStartTracking()
                    }
                },
                containerColor = MaterialTheme.colorScheme.primary
            ) {
                Icon(
                    imageVector = if (isTrackingActive) Icons.Default.Close else Icons.Filled.PlayArrow,
                    contentDescription = if (isTrackingActive) "Stop Tracking" else "Start Tracking",
                    tint = Color.White
                )
            }
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "Location Tracker",
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(top = 8.dp, bottom = 8.dp)
            )

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = "Today's Locations (${todayLocations.size})",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 8.dp)
            )

            if (todayLocations.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(100.dp)
                        .background(Color(0xFFF0F0F0))
                        .padding(16.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "No location data recorded today",
                        color = Color.Gray
                    )
                }
            } else {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                ) {
                    items(todayLocations) { location ->
                        LocationItem(
                            location = location,
                        )
                        HorizontalDivider(thickness = 1.dp, color = Color.Transparent)
                    }
                }
            }
        }
    }
}

@Composable
fun LocationItem(
    location: LocationEntity,
) {

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "Location #${location.id}",
                    fontWeight = FontWeight.Bold,
                    style = MaterialTheme.typography.titleMedium
                )
                Text(
                    text = location.formattedTime,
                    style = MaterialTheme.typography.bodyMedium
                )
            }

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = "Lat: ${location.latitude}, Lon: ${location.longitude}",
                style = MaterialTheme.typography.bodyMedium
            )

            Spacer(modifier = Modifier.height(2.dp))

            Text(
                text = location.address,
                style = MaterialTheme.typography.bodySmall,
                color = Color.Gray
            )
        }
    }
}