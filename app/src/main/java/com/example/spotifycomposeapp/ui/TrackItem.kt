package com.example.spotifycomposeapp.ui
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.spotifycomposeapp.model.Track

// function to display an individual track item in a card layout
@Composable
fun TrackItem(track: Track, onClick: (String) -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp)
            .clickable { onClick(track.previewUrl) },
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Text(text = "🎵 ${track.name}", fontWeight = FontWeight.Bold)
            Text(text = "👤 ${track.artist}")
            Text(text = "⏱ ${track.duration}")
        }
    }
}