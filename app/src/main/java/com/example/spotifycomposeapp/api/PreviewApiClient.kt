package com.example.spotifycomposeapp.api

import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

// Singleton object that creates and provides a Retrofit instance for calling the preview API.
object PreviewApiClient {
    private const val BASE_URL = "http://10.0.2.2:5000" // Base URL for the local server. "10.0.2.2" is used to refer to localhost from the Android emulator.

    // Lazily initialized Retrofit service that uses Gson to parse JSON responses.
    val api: PreviewApiService by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL) // Set the base URL for the API
            .addConverterFactory(GsonConverterFactory.create())// Use Gson to convert JSON to data classes
            .build()
            .create(PreviewApiService::class.java) // Create the implementation of the PreviewApiService interface
    }
}