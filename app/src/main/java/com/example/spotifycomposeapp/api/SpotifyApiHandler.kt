package com.example.spotifycomposeapp.api

import com.adamratzman.spotify.SpotifyAppApi
import com.adamratzman.spotify.models.SpotifyPublicUser
import com.adamratzman.spotify.spotifyAppApi
import com.adamratzman.spotify.utils.Market
import com.adamratzman.spotify.models.PagingObject
import com.adamratzman.spotify.models.Track

class SpotifyApiHandler {
    private val clientID = "e759a2f67da744f5bc8effdb550fbb8a"
    private val clientSecret = "0c33b2d7f5e94d24a7d47a1585f9b401"
    private var api: SpotifyAppApi? = null

    init {

    }

    /// Pulls the developer ClientID and ClientSecret tokens provided by Spotify and builds them
    /// into an object that can easily call public Spotify APIs.
    suspend fun buildSearchApi() {
        api = spotifyAppApi(clientID, clientSecret).build()
    }

    // Performs Spotify database query for queries related to user information. Returns
    // the results as a SpotifyPublicUser object.
    suspend fun userSearch(userQuery: String): SpotifyPublicUser? {
        return api!!.users.getProfile(userQuery)
    }

    // Performs Spotify database query for queries related to track information. Returns
    // the results as a SpotifySearchResult object.
    suspend fun trackSearch(searchQuery: String): PagingObject<Track>
    {
        return api!!.search.searchTrack(
            searchQuery,
            limit = 50,
            market = Market.US
        )

    }

}