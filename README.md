# 📚 BookApp – Kotlin Android Book Explorer

**BookApp** is a feature-rich Android application that allows users to explore, search, and save books. The app integrates Firebase Authentication and Firestore to support user login, registration, and persistent favorite book storage.

---

## ✨ Features

- 🔍 **Search and Browse Books**  
  Fetch recent titles or search for specific books via a RESTful API.

- 📖 **Detailed Book Pages**  
  View book metadata including title, author, description, and cover. Download links are provided when available.

- ❤️ **Favorites System**  
  Authenticated users can mark books as favorites, with real-time syncing to Firebase Firestore.

- 🔐 **User Authentication**  
  - Email/password registration and login using Firebase Auth  
  - One-tap Google Sign-In via Credential Manager API

- 🔔 **Smart Notifications**  
  If a user has no favorite books, the app sends a notification suggesting they explore and save titles.

- 🧠 **Auto Login**  
  Automatically loads previously authenticated users on app launch.

- 📦 **Modern Architecture & Tools**  
  - MVVM-style logic separation  
  - Retrofit for network calls  
  - Glide for image loading  
  - ViewBinding and Navigation Component for UI and transitions
