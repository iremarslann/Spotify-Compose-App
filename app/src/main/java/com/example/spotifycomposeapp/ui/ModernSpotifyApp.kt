package com.example.spotifycomposeapp.ui

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.BorderStroke
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import coil.compose.rememberAsyncImagePainter
import androidx.compose.ui.text.input.TextFieldValue
import com.example.spotifycomposeapp.model.Track
import com.example.spotifycomposeapp.viewmodel.SpotifyViewModel
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.compose.ui.zIndex
import kotlin.random.Random
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll

val DarkBrown = Color(0xFF381E03)
val LightBrown = Color(0xFF885621)
val LightPink = Color(0xFFFFB4DD)

// Full-screen player UI with song controls and info
@Composable
fun FullScreenPlayer(
    track: Track,
    isPlaying: Boolean,
    progress: Float,
    onPauseResume: () -> Unit,
    onClose: () -> Unit,
    onAddToPlaylist: () -> Unit,
    onSkipNext: () -> Unit,
    onSkipPrevious: () -> Unit,
    viewModel: SpotifyViewModel
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(DarkBrown)
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Image(
            painter = rememberAsyncImagePainter(track.imageUrl),
            contentDescription = "Album Art",
            contentScale = ContentScale.Crop,
            modifier = Modifier
                .size(300.dp)
                .clip(RoundedCornerShape(16.dp))
        )

        Spacer(modifier = Modifier.height(24.dp))

        // Playback control buttons: shuffle, prev, play/pause, next, repeat
        Text(text = track.name, style = MaterialTheme.typography.titleLarge, color = LightPink)
        Text(text = track.artist, style = MaterialTheme.typography.bodyMedium, color = Color.White)

        Spacer(modifier = Modifier.height(16.dp))

        // Track progress slider
        Slider(
            value = progress,
            onValueChange = {},
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp)
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Playback control buttons: shuffle, prev, play/pause, next, repeat
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceEvenly,
            modifier = Modifier.fillMaxWidth()
        ) {
            IconButton(onClick = { viewModel.toggleShuffle() }) {
                Icon(
                    imageVector = Icons.Default.Shuffle,
                    contentDescription = "Shuffle",
                    tint = if (viewModel.isShuffleEnabled) LightPink else Color.Gray
                )
            }

            IconButton(onClick = onSkipPrevious) {
                Icon(Icons.Default.SkipPrevious, contentDescription = "Previous", tint = LightPink)
            }

            IconButton(onClick = onPauseResume) {
                Icon(
                    if (isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
                    contentDescription = "Play/Pause",
                    tint = LightPink
                )
            }

            IconButton(onClick = onSkipNext) {
                Icon(Icons.Default.SkipNext, contentDescription = "Next", tint = LightPink)
            }

            IconButton(onClick = { viewModel.toggleRepeat() }) {
                Icon(
                    imageVector = Icons.Default.Repeat,
                    contentDescription = "Repeat",
                    tint = if (viewModel.isRepeatEnabled) LightPink else Color.Gray
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        Button(onClick = onAddToPlaylist, colors = ButtonDefaults.buttonColors(containerColor = LightBrown)) {
            Text("Add to Playlist", color = Color.White)
        }

        Spacer(modifier = Modifier.height(16.dp))

        TextButton(onClick = onClose) {
            Text("Minimize", color = LightPink)
        }
    }
}

// Mini player component for compact playback control
@Composable
fun MiniPlayer(
    viewModel: SpotifyViewModel,
    onExpand: () -> Unit,
    modifier: Modifier = Modifier
) {
    val track = viewModel.currentTrack ?: return

    Surface(
        tonalElevation = 3.dp,
        color = LightBrown,
        modifier = modifier
            .fillMaxWidth()
            .clickable { onExpand() }
            .padding(8.dp)
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column {
                Text(track.name, color = Color.White, style = MaterialTheme.typography.bodyLarge)
                Text(track.artist, color = Color.LightGray, style = MaterialTheme.typography.bodySmall)
            }
            IconButton(onClick = { viewModel.togglePlayback() }) {
                Icon(
                    if (viewModel.isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
                    contentDescription = null,
                    tint = LightPink
                )
            }
        }
    }
}

// Composable function for displaying the main search UI of the app
@Composable
fun SearchScreen(viewModel: SpotifyViewModel, onTrackSelected: (Track) -> Unit) {
    // Bind current search query and results from the ViewModel
    val query = viewModel.query
    val results = viewModel.filteredResults

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Search bar row
        item {
            Row(modifier = Modifier.fillMaxWidth()) {
                OutlinedTextField(
                    value = query,
                    onValueChange = { viewModel.updateQuery(it) },
                    label = { Text("Search", color = LightPink) },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = LightPink,
                        unfocusedBorderColor = LightPink,
                        cursorColor = LightPink,
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White
                    ),
                    modifier = Modifier.weight(1f)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Button(
                    onClick = { viewModel.searchTracks() },
                    colors = ButtonDefaults.buttonColors(containerColor = LightBrown),
                    modifier = Modifier.align(Alignment.CenterVertically)
                ) {
                    Icon(Icons.Default.Search, contentDescription = "Search", tint = Color.White)
                }
            }
        }

        // Genre category filters
        item {
            Text("Categories", color = LightPink, style = MaterialTheme.typography.titleMedium)
            Spacer(modifier = Modifier.height(8.dp))
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                listOf("Pop", "Rock", "EDM", "R&B").forEach { genre ->
                    Button(
                        onClick = {
                            viewModel.updateQuery(genre)
                            viewModel.searchTracks()
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = LightBrown),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text(genre, color = Color.White)
                    }
                }
            }
        }

        // Trending section (currently hardcoded)
        item {
            Spacer(modifier = Modifier.height(8.dp))
            Text("Trending Now", color = LightPink, style = MaterialTheme.typography.titleMedium)
            Spacer(modifier = Modifier.height(8.dp))
            LazyRow(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                items(listOf("Espresso", "Push Ups", "Beautiful Things")) { title ->
                    Surface(
                        color = LightBrown,
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .padding(4.dp)
                            .clickable {}
                    ) {
                        Text(title, modifier = Modifier.padding(12.dp), color = Color.White)
                    }
                }
            }
        }

        // Recent search results
        item {
            Spacer(modifier = Modifier.height(8.dp))
            Text("Recent Searces", color = LightPink, style = MaterialTheme.typography.titleMedium)
            Spacer(modifier = Modifier.height(8.dp))
            LazyRow(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                items(results.take(5)) { track ->
                    Card(
                        modifier = Modifier
                            .width(200.dp)
                            .height(250.dp)
                            .clickable { onTrackSelected(track) },
                        colors = CardDefaults.cardColors(containerColor = LightBrown),
                        shape = RoundedCornerShape(16.dp)
                    ) {
                        Column(
                            modifier = Modifier.padding(12.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Image(
                                painter = rememberAsyncImagePainter(track.imageUrl),
                                contentDescription = "Album Cover",
                                modifier = Modifier
                                    .size(140.dp)
                                    .clip(RoundedCornerShape(12.dp)),
                                contentScale = ContentScale.Crop
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(track.name, color = Color.White, maxLines = 1)
                            Text(track.artist, color = Color.LightGray, maxLines = 1)
                        }
                    }
                }
            }
        }

        // Full list of all search results
        if (results.isNotEmpty()) {
            item {
                Spacer(modifier = Modifier.height(8.dp))
                Text("Search Results", color = LightPink, style = MaterialTheme.typography.titleMedium)
                Spacer(modifier = Modifier.height(8.dp))
            }
            items(results) { track ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { onTrackSelected(track) }
                        .padding(vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Image(
                        painter = rememberAsyncImagePainter(track.imageUrl),
                        contentDescription = null,
                        modifier = Modifier
                            .size(64.dp)
                            .clip(RoundedCornerShape(8.dp)),
                        contentScale = ContentScale.Crop
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text(track.name, color = Color.White, style = MaterialTheme.typography.bodyLarge)
                        Text(track.artist, color = Color.LightGray, style = MaterialTheme.typography.bodySmall)
                        Text(track.duration, color = Color.Gray, style = MaterialTheme.typography.bodySmall)
                    }
                }
            }
        }
    }
}

// Composable screen for managing playlists: view, create, rename, delete, and play tracks.
@Composable
fun PlaylistScreen(viewModel: SpotifyViewModel, onTrackSelected: (Track) -> Unit) {
    val playlists = viewModel.playlists
    val playlistTracks = remember { mutableStateListOf<Track>() }
    var selectedPlaylist by remember { mutableStateOf<String?>(null) }
    var showCreateDialog by remember { mutableStateOf(false) }
    var newPlaylistName by remember { mutableStateOf("") }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(DarkBrown)
    ) {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Playlists", style = MaterialTheme.typography.headlineSmall, color = LightPink)
                    IconButton(onClick = { showCreateDialog = true }) {
                        Icon(Icons.Default.Add, contentDescription = "Add Playlist", tint = LightPink)
                    }
                }
            }

            // Display existing playlists
            items(playlists.entries.toList()) { (id, name) ->
                Column(modifier = Modifier.fillMaxWidth()) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                selectedPlaylist = id
                                viewModel.getTracksFromPlaylist(id) {
                                    playlistTracks.clear()
                                    playlistTracks.addAll(it)
                                }
                            }
                            .padding(8.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(name, color = LightPink, modifier = Modifier.weight(1f))
                        PlaylistActions(
                            playlistId = id,
                            playlistName = name,
                            onDelete = { viewModel.deletePlaylist(it) },
                            onRename = { pid, newName -> viewModel.renamePlaylist(pid, newName) }
                        )
                    }
                }
            }

            //Show playlist tracks
            if (playlistTracks.isNotEmpty()) {
                item {
                    Text("Songs in Playlist", color = LightPink, style = MaterialTheme.typography.titleMedium)
                }
            }

            // Render each track in the selected playlist
            items(playlistTracks) { track ->
                TrackItem(track = track, onClick = { onTrackSelected(track) })
            }
        }

        // Dialog for creating a new playlist
        if (showCreateDialog) {
            AlertDialog(
                onDismissRequest = { showCreateDialog = false },
                confirmButton = {
                    Button(
                        onClick = {
                            if (newPlaylistName.isNotBlank()) {
                                viewModel.createPlaylist(newPlaylistName)
                                newPlaylistName = ""
                                showCreateDialog = false
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = LightBrown)
                    ) {
                        Text("Create", color = Color.White)
                    }
                },
                dismissButton = {
                    TextButton(onClick = {
                        newPlaylistName = ""
                        showCreateDialog = false
                    }) {
                        Text("Cancel", color = LightPink)
                    }
                },
                title = { Text("New Playlist", color = LightPink) },
                text = {
                    OutlinedTextField(
                        value = newPlaylistName,
                        onValueChange = { newPlaylistName = it },
                        label = { Text("Playlist Name", color = LightPink) },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = LightPink,
                            unfocusedBorderColor = LightPink,
                            cursorColor = LightPink,
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White
                        )
                    )
                },
                containerColor = DarkBrown
            )
        }
    }
}

// Action buttons for renaming and deleting a playlist
@Composable
fun PlaylistActions(
    playlistId: String,
    playlistName: String,
    onDelete: (String) -> Unit,
    onRename: (String, String) -> Unit
) {
    var showRenameDialog by remember { mutableStateOf(false) }
    var newName by remember { mutableStateOf(playlistName) }

    Row(verticalAlignment = Alignment.CenterVertically) {
        IconButton(onClick = { showRenameDialog = true }) {
            Icon(Icons.Default.Edit, contentDescription = "Rename", tint = LightPink)
        }
        IconButton(onClick = { onDelete(playlistId) }) {
            Icon(Icons.Default.Delete, contentDescription = "Delete", tint = LightPink)
        }
    }

    // Rename dialog with text field and confirm/cancel options
    if (showRenameDialog) {
        AlertDialog(
            onDismissRequest = { showRenameDialog = false },
            confirmButton = {
                Button(onClick = {
                    if (newName.isNotBlank()) {
                        onRename(playlistId, newName)
                        showRenameDialog = false
                    }
                }, colors = ButtonDefaults.buttonColors(containerColor = LightBrown)) {
                    Text("Rename", color = Color.White)
                }
            },
            dismissButton = {
                TextButton(onClick = { showRenameDialog = false }) {
                    Text("Cancel", color = LightPink)
                }
            },
            title = { Text("Rename Playlist", color = LightPink) },
            text = {
                OutlinedTextField(
                    value = newName,
                    onValueChange = { newName = it },
                    label = { Text("New Name", color = LightPink) },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = LightPink,
                        unfocusedBorderColor = LightPink,
                        cursorColor = LightPink,
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White
                    )
                )
            },
            containerColor = DarkBrown
        )
    }
}

//hardcoded profile screen since OAuth 2.0 is not implemented
@Composable
fun ProfileScreen() {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(DarkBrown)
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Avatar
        Surface(
            shape = CircleShape,
            color = LightPink,
            modifier = Modifier.size(100.dp)
        ) {
            Icon(
                imageVector = Icons.Default.Person,
                contentDescription = "Profile",
                tint = DarkBrown,
                modifier = Modifier.padding(20.dp)
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Name & Bio
        Text("Irem Arslan", color = LightPink, style = MaterialTheme.typography.titleLarge)
        Text("@iremarslan", color = Color.White)

        Spacer(modifier = Modifier.height(32.dp))

        // Stats
        Text("Stats", color = LightPink, style = MaterialTheme.typography.titleMedium)
        Spacer(modifier = Modifier.height(12.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            StatItem("Playlists", "9")
            StatItem("Songs", "142")
            StatItem("Hours", "36h")
        }

        Spacer(modifier = Modifier.height(32.dp))

        // Settings
        Text("Preferences", color = LightPink, style = MaterialTheme.typography.titleMedium)
        Spacer(modifier = Modifier.height(12.dp))
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Enable Dark Mode", color = Color.White, modifier = Modifier.weight(1f))
            Switch(checked = true, onCheckedChange = {}, colors = SwitchDefaults.colors(checkedThumbColor = LightPink))
        }

        Spacer(modifier = Modifier.height(16.dp))

        Button(
            onClick = { /* Feedback */ },
            colors = ButtonDefaults.buttonColors(containerColor = LightBrown),
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Give Feedback", color = Color.White)
        }

        Spacer(modifier = Modifier.height(12.dp))

        OutlinedButton(
            onClick = { /* Log out */ },
            border = BorderStroke(1.dp, LightPink),
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Log Out", color = LightPink)
        }
    }
}

//helper function for profile screen
@Composable
fun StatItem(label: String, value: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(value, color = LightPink, style = MaterialTheme.typography.titleMedium)
        Text(label, color = Color.White)
    }
}

// Entry point of the app containing navigation, playback, and UI logic.
@Composable
fun ModernSpotifyApp(viewModel: SpotifyViewModel = viewModel()) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(DarkBrown)
            .padding(16.dp)
    ) {
        val query = viewModel.query
        val results = viewModel.filteredResults
        val selectedTrack = remember { mutableStateOf<Track?>(null) }
        var showFullPlayer by remember { mutableStateOf(false) }
        var showPlaylistManager by remember { mutableStateOf(false) }
        var selectedPlaylistId by remember { mutableStateOf<String?>(null) }
        val progress by viewModel.playbackProgress.collectAsState()
        var showAddToPlaylistDialog by remember { mutableStateOf(false) }
        val playlistTracks = remember { mutableStateListOf<Track>() }
        val currentScreen = remember { mutableStateOf("home") }

        // Ensure autoplay and repeat logic updates UI
        LaunchedEffect(viewModel.currentTrack) {
            selectedTrack.value = viewModel.currentTrack
        }

        // Auto-close full screen player when switching tabs
        LaunchedEffect(currentScreen.value) {
            showFullPlayer = false
        }

        // layout with bottom navigation and mini player
        Scaffold(
            containerColor = DarkBrown,
            bottomBar = {
                Column {
                    selectedTrack.value?.let {
                        if (!showFullPlayer) {
                            MiniPlayer(
                                viewModel = viewModel,
                                onExpand = { showFullPlayer = true },
                                modifier = Modifier.fillMaxWidth()
                            )
                        }
                    }
                    NavigationBar(containerColor = LightBrown) {
                        NavigationBarItem(
                            selected = currentScreen.value == "home",
                            onClick = { currentScreen.value = "home" },
                            icon = {
                                Icon(
                                    Icons.Default.Home,
                                    contentDescription = "Home",
                                    tint = LightPink
                                )
                            },
                            label = { Text("Home", color = LightPink) }
                        )
                        NavigationBarItem(
                            selected = currentScreen.value == "playlists",
                            onClick = { currentScreen.value = "playlists" },
                            icon = {
                                Icon(
                                    Icons.Default.QueueMusic,
                                    contentDescription = "Playlists",
                                    tint = LightPink
                                )
                            },
                            label = { Text("Playlists", color = LightPink) }
                        )
                        NavigationBarItem(
                            selected = currentScreen.value == "profile",
                            onClick = { currentScreen.value = "profile" },
                            icon = {
                                Icon(
                                    Icons.Default.Person,
                                    contentDescription = "Profile",
                                    tint = LightPink
                                )
                            },
                            label = { Text("Profile", color = LightPink) }
                        )
                    }
                }
            }
        ) { innerPadding -> // Navigation between screens
            Box(modifier = Modifier.padding(innerPadding).background(DarkBrown)) {
                when (currentScreen.value) {
                    "home" -> SearchScreen(viewModel) {
                        viewModel.selectTrack(it)
                        selectedTrack.value = it
                        viewModel.playPreview(it.previewUrl)
                        showFullPlayer = true
                    }

                    "playlists" -> PlaylistScreen(viewModel) {
                        viewModel.selectTrack(it)
                        selectedTrack.value = it
                        viewModel.playPreview(it.previewUrl)
                        showFullPlayer = true
                    }

                    "profile" -> ProfileScreen()
                }

                // Full screen player UI when a track is selected
                selectedTrack.value?.let { track ->
                    if (showFullPlayer) {
                        FullScreenPlayer(
                            track = track,
                            isPlaying = viewModel.isPlaying,
                            progress = progress,
                            onPauseResume = { viewModel.togglePlayback() },
                            onClose = { showFullPlayer = false },
                            onAddToPlaylist = { showAddToPlaylistDialog = true },
                            onSkipNext = {
                                val next = if (viewModel.isRepeatEnabled) {
                                    track
                                } else if (viewModel.isShuffleEnabled) {
                                    viewModel.getRandomTrack()
                                } else {
                                    viewModel.skipToNextTrack()
                                }
                                next?.let {
                                    viewModel.selectTrack(it)
                                    selectedTrack.value = it
                                    viewModel.playPreview(it.previewUrl)
                                }
                            },
                            onSkipPrevious = {
                                viewModel.skipToPreviousTrack()?.let {
                                    viewModel.selectTrack(it)
                                    selectedTrack.value = it
                                    viewModel.playPreview(it.previewUrl)
                                }
                            },
                            viewModel = viewModel
                        )
                    }
                }

                if (showAddToPlaylistDialog && selectedTrack.value != null) {
                    AlertDialog(
                        onDismissRequest = { showAddToPlaylistDialog = false },
                        confirmButton = {
                            Button(onClick = {
                                viewModel.selectedPlaylists.forEach { playlistId ->
                                    viewModel.addToPlaylist(selectedTrack.value!!, playlistId)
                                }
                                viewModel.selectedPlaylists.clear()
                                showAddToPlaylistDialog = false
                            }, colors = ButtonDefaults.buttonColors(containerColor = LightBrown)) {
                                Text("Add", color = Color.White)
                            }
                        },
                        dismissButton = {
                            TextButton(onClick = {
                                viewModel.selectedPlaylists.clear()
                                showAddToPlaylistDialog = false
                            }) {
                                Text("Cancel", color = LightPink)
                            }
                        },
                        title = { Text("Add to Playlists", color = LightPink) },
                        text = {
                            Column {
                                Text("Select playlists:", color = Color.White)
                                viewModel.playlists.forEach { (id, name) ->
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Checkbox(
                                            checked = viewModel.selectedPlaylists.contains(id),
                                            onCheckedChange = {
                                                if (it) viewModel.selectedPlaylists.add(id)
                                                else viewModel.selectedPlaylists.remove(id)
                                            },
                                            colors = CheckboxDefaults.colors(checkedColor = LightPink)
                                        )
                                        Text(name, color = Color.White)
                                    }
                                }
                            }
                        },
                        containerColor = DarkBrown
                    )
                }
            }
        }
    }
}
