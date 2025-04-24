package com.example.spotifycomposeapp.model

data class Playlist(
    val id: String = "",
    val name: String = "",
    val songs: List<Track> = emptyList()
)
