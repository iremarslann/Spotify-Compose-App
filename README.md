# Isla Bonita – Spotify Clone 🎶  

Isla Bonita is a **Spotify-inspired Android music streaming app** built with **Kotlin, Jetpack Compose, and MVVM architecture**. The app integrates the **Spotify API** to provide real-time music search and playback of preview tracks, along with playlist management powered by **Firebase Firestore**.  

This project was developed as a **capstone project** for my Computer Science degree.  

---

## Features  

- 🔍 **Search Music**  
  Search for tracks, artists, and albums using the Spotify API.  

- 🎧 **Music Playback**  
  Play 30-second preview clips with play, pause, skip, shuffle, and repeat functionality.  

- 📱 **Now Playing Screen**  
  Full-screen player with album art, song details, progress bar, and controls.  

- ⬇️ **Mini Player**  
  Always-visible bottom player to quickly control the current track.  

- 📂 **Playlist Management**  
  Create, edit, and delete playlists stored in Firebase Firestore.  

- 🎨 **Modern UI Design**  
  Custom brown-and-pink theme with sidebar navigation inspired by Spotify’s design.  

---

## Tech Stack  

- **Language:** Kotlin  
- **Framework:** Jetpack Compose  
- **Architecture:** MVVM (Model–View–ViewModel)  
- **Database/Backend:** Firebase Firestore  
- **API Integration:** Spotify API (Client Credentials Flow)  
- **Playback:** Android MediaPlayer  

---

## Project Structure  

├── MainActivity.kt
├── SpotifyViewModel.kt
├── SpotifyApiHandler.kt
├── models/
│ └── Track.kt
├── ui/
│ ├── SpotifyApp.kt
│ ├── TrackItem.kt
│ └── ModernSpotifyApp.kt


---

## Setup & Installation  

1. Clone the repository:  
   ```bash
   git clone https://github.com/yourusername/isla-bonita.git
   cd isla-bonita
