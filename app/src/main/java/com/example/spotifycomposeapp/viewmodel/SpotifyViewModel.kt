package com.example.spotifycomposeapp.viewmodel

import android.media.MediaPlayer
import android.util.Log
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.spotifycomposeapp.api.PreviewApiClient
import com.example.spotifycomposeapp.api.SpotifyApiHandler
import com.example.spotifycomposeapp.model.FilterType
import com.example.spotifycomposeapp.model.Track
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.net.HttpURLConnection
import java.net.URL
import java.util.UUID

class SpotifyViewModel : ViewModel() {
    private val api = SpotifyApiHandler()
    private val firestore = FirebaseFirestore.getInstance()
    val selectedPlaylists = mutableStateListOf<String>()

    var query by mutableStateOf("")
        private set

    var searchResults by mutableStateOf<List<Track>>(emptyList())
        private set

    var selectedFilter by mutableStateOf(FilterType.All)
        private set

    val filteredResults: List<Track>
        get() = when (selectedFilter) {
            FilterType.All -> searchResults
            FilterType.Songs -> searchResults
            FilterType.Artists -> searchResults.filter { it.artist.contains(query, ignoreCase = true) }
            FilterType.Albums -> emptyList()
        }

    var isShuffleEnabled by mutableStateOf(false)
    var isRepeatEnabled by mutableStateOf(false)

    fun toggleShuffle() {
        isShuffleEnabled = !isShuffleEnabled
    }

    fun toggleRepeat() {
        isRepeatEnabled = !isRepeatEnabled
    }

    fun updateFilter(filter: FilterType) {
        selectedFilter = filter
    }

    init {
        viewModelScope.launch {
            api.buildSearchApi()
            loadPlaylistsFromFirebase()
        }
    }

    fun updateQuery(newQuery: String) {
        query = newQuery
    }

    // Performs a search query and fetches preview URLs
    fun searchTracks() {
        viewModelScope.launch {
            val result = api.trackSearch(query)

            // Map Spotify API track results into custom Track model
            val tracks = result.items.map { item ->
                Track(
                    name = item.name,
                    artist = item.artists.firstOrNull()?.name ?: "Unknown",
                    previewUrl = "",
                    duration = "${(item.length / 1000) / 60}:${(item.length / 1000) % 60}",
                    imageUrl = item.album.images?.firstOrNull()?.url ?: ""
                )
            }

            // Fetch preview URLs from custom backend and update each track
            val updatedTracks = tracks.map { track ->
                val previewUrl = fetchPreviewUrlFromNode(track.name)
                track.copy(previewUrl = previewUrl)
            }

            searchResults = updatedTracks
        }
    }

    // Calls backend server to get a preview URL for a track
    private suspend fun fetchPreviewUrlFromNode(trackName: String): String {
        return withContext(Dispatchers.IO) {
            try {
                val encodedQuery = java.net.URLEncoder.encode(trackName, "UTF-8")
                val url = URL("http://10.0.2.2:5000/preview?q=$encodedQuery")
                val connection = url.openConnection() as HttpURLConnection
                connection.requestMethod = "GET"
                connection.connectTimeout = 5000
                connection.readTimeout = 5000

                val responseCode = connection.responseCode
                if (responseCode == HttpURLConnection.HTTP_OK) {
                    val responseText = connection.inputStream.bufferedReader().use { it.readText() }
                    val json = JSONObject(responseText)
                    val results = json.getJSONArray("results")
                    if (results.length() > 0) {
                        val first = results.getJSONObject(0)
                        val previewUrls = first.getJSONArray("previewUrls")
                        if (previewUrls.length() > 0) {
                            return@withContext previewUrls.getString(0)
                        }
                    }
                }
                ""
            } catch (e: Exception) {
                Log.e("NodePreviewFetch", "Error fetching preview: ${e.message}")
                ""
            }
        }
    }

    fun fetchAndPlayPreview(query: String) {
        viewModelScope.launch {
            try {
                val response = PreviewApiClient.api.getPreview(query)

                if (response.success && response.results.isNotEmpty()) {
                    val previewUrl = response.results[0].previewUrls.firstOrNull()
                    if (!previewUrl.isNullOrEmpty()) {
                        playPreview(previewUrl)
                    } else {
                        Log.d("PreviewFetch", "No preview URL found")
                    }
                } else {
                    Log.d("PreviewFetch", "No results or unsuccessful request")
                }
            } catch (e: Exception) {
                e.printStackTrace()
                Log.e("PreviewFetch", "Error: ${e.message}")
            }
        }
    }

    private var mediaPlayer: MediaPlayer? = null

    var isPlaying by mutableStateOf(false)
        private set

    private val _playbackProgress = MutableStateFlow(0f)
    val playbackProgress: StateFlow<Float> = _playbackProgress

    // Pause or resume playback
    fun togglePlayback() {
        mediaPlayer?.let {
            if (it.isPlaying) {
                it.pause()
                isPlaying = false
            } else {
                it.start()
                isPlaying = true
                updateProgress()
            }
        }
    }

    override fun onCleared() {
        super.onCleared()
        mediaPlayer?.release()
        mediaPlayer = null
    }

    // Load and play a preview audio clip from a given URL
    fun playPreview(url: String) {
        try {
            mediaPlayer?.release()
            mediaPlayer = MediaPlayer().apply {
                setDataSource(url)
                setOnPreparedListener {
                    it.start()
                    this@SpotifyViewModel.isPlaying = true
                    updateProgress()
                }
                setOnCompletionListener {
                    this@SpotifyViewModel.isPlaying = false
                    _playbackProgress.value = 0f
                    handleTrackCompletion()

                    viewModelScope.launch {
                        val nextTrack = if (isShuffleEnabled) {
                            currentPlaylistTracks.filterNot { it == currentTrack }.randomOrNull()
                        } else {
                            skipToNextTrack()
                        }

                        if (nextTrack != null) {
                            selectTrack(nextTrack)
                            delay(100)
                            playPreview(nextTrack.previewUrl)
                        } else if (isRepeatEnabled && currentTrack != null) {
                            playPreview(currentTrack!!.previewUrl)
                        }
                    }
                }
                prepareAsync()
            }
        } catch (e: Exception) {
            Log.e("MediaPlayer", "Failed to play: ${e.message}")
            e.printStackTrace()
            isPlaying = false
        }
    }

    var currentTrack by mutableStateOf<Track?>(null)
        private set

    fun selectTrack(track: Track) {
        currentTrack = track.copy()
    }

    // Playlist logic with Firebase
    var playlists by mutableStateOf<Map<String, String>>(emptyMap())
        private set

    fun createPlaylist(name: String) {
        val id = UUID.randomUUID().toString()
        val data = mapOf("name" to name, "tracks" to emptyList<Any>())

        firestore.collection("playlists").document(id).set(data)
            .addOnSuccessListener { loadPlaylistsFromFirebase() }
            .addOnFailureListener { Log.e("Firebase", "Failed to create playlist: ${it.message}") }
    }

    fun deletePlaylist(id: String) {
        firestore.collection("playlists").document(id).delete()
            .addOnSuccessListener { loadPlaylistsFromFirebase() }
            .addOnFailureListener { Log.e("Firebase", "Failed to delete playlist: ${it.message}") }
    }

    fun renamePlaylist(id: String, newName: String) {
        firestore.collection("playlists").document(id)
            .update("name", newName)
            .addOnSuccessListener { loadPlaylistsFromFirebase() }
            .addOnFailureListener { Log.e("Firebase", "Failed to rename playlist: ${it.message}") }
    }

    fun addToPlaylist(track: Track, playlistId: String) {
        firestore.collection("playlists").document(playlistId)
            .update("tracks", FieldValue.arrayUnion(track.toMap()))
            .addOnSuccessListener { Log.d("Playlist", "Track added to playlist $playlistId") }
            .addOnFailureListener { Log.e("Playlist", "Add failed: ${it.message}") }
    }

    fun loadPlaylistsFromFirebase() {
        firestore.collection("playlists")
            .get()
            .addOnSuccessListener { result ->
                playlists = result.documents.associate { it.id to (it.getString("name") ?: "Untitled") }
            }
            .addOnFailureListener { Log.e("Firebase", "Load failed: ${it.message}") }
    }

    val currentPlaylistTracks = mutableListOf<Track>()

    fun getTracksFromPlaylist(id: String, onResult: (List<Track>) -> Unit) {
        firestore.collection("playlists").document(id)
            .get()
            .addOnSuccessListener { doc ->
                val tracks = (doc["tracks"] as? List<Map<String, Any>>)?.map {
                    Track(
                        name = it["name"] as String,
                        artist = it["artist"] as String,
                        imageUrl = it["imageUrl"] as String,
                        previewUrl = it["previewUrl"] as String,
                        duration = it["duration"] as String
                    )
                } ?: emptyList()

                currentPlaylistTracks.clear()
                currentPlaylistTracks.addAll(tracks)

                onResult(tracks)
            }
            .addOnFailureListener {
                Log.e("Firebase", "Failed to fetch tracks: ${it.message}")
                onResult(emptyList())
            }
    }

    fun skipToNextTrack(): Track? {
        val index = currentPlaylistTracks.indexOf(currentTrack)
        if (index != -1 && index < currentPlaylistTracks.lastIndex) {
            val nextTrack = currentPlaylistTracks[index + 1]
            return nextTrack
        }
        return null
    }

    fun skipToPreviousTrack(): Track? {
        val index = currentPlaylistTracks.indexOf(currentTrack)
        if (index > 0) {
            val previousTrack = currentPlaylistTracks[index - 1]
            return previousTrack
        }
        return null
    }

    private fun updateProgress() {
        viewModelScope.launch {
            while (isPlaying && mediaPlayer?.isPlaying == true) {
                val currentPosition = mediaPlayer?.currentPosition ?: 0
                val duration = mediaPlayer?.duration ?: 1
                _playbackProgress.value = currentPosition.toFloat() / duration.toFloat()
                delay(1000)
            }
        }
    }

    fun getRandomTrack(): Track? {
        if (currentPlaylistTracks.isNotEmpty()) {
            val currentIndex = currentPlaylistTracks.indexOf(currentTrack)
            val possibleIndices = currentPlaylistTracks.indices.filter { it != currentIndex }
            val randomIndex = possibleIndices.randomOrNull() ?: currentIndex
            return currentPlaylistTracks[randomIndex]
        }
        return null
    }

    fun handleTrackCompletion() {
        if (isRepeatEnabled && currentTrack != null) {
            playPreview(currentTrack!!.previewUrl)
        } else {
            val nextTrack = if (isShuffleEnabled) getRandomTrack() else skipToNextTrack()
            nextTrack?.let {
                currentTrack = it.copy()
                playPreview(it.previewUrl)
            }
        }
    }

    // Converts a Track object into a Map<String, String> for Firebase storage
    private fun Track.toMap(): Map<String, String> = mapOf(
        "name" to name,
        "artist" to artist,
        "imageUrl" to imageUrl,
        "previewUrl" to previewUrl,
        "duration" to duration
    )

}
