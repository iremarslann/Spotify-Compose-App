package com.example.spotifycomposeapp.api

// Represents the entire response returned by the preview API.
data class PreviewResponse(
    val success: Boolean,
    val results: List<PreviewResult>
)

// Represents a single track result in the preview API response.
data class PreviewResult(
    val name: String,
    val spotifyUrl: String,
    val previewUrls: List<String>
)
