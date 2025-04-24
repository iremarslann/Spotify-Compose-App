package com.example.spotifycomposeapp.api

import retrofit2.http.GET
import retrofit2.http.Query

// Retrofit service interface for accessing the preview-fetching backend
// Sends a GET request to the `/preview` endpoint
// This is used to search for song previews based on a query string
interface PreviewApiService {
    @GET("/preview")
    suspend fun getPreview(
        @Query("q") query: String
    ): PreviewResponse
}