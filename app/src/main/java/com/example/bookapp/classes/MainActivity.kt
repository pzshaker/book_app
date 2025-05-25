package com.example.bookapp.classes

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.view.View
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.navigation.NavController
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.setupWithNavController
import com.example.bookapp.R
import com.example.bookapp.classes.CurrentUser
import com.example.bookapp.classes.User
import com.example.bookapp.databinding.ActivityMainBinding
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

class MainActivity : AppCompatActivity() {
    lateinit var binding: ActivityMainBinding
    private lateinit var navController: NavController
    private var isNotificationSent = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        binding = ActivityMainBinding.inflate(layoutInflater)
        val view = binding.root
        setContentView(view)

        val navHostFragment =
            supportFragmentManager.findFragmentById(R.id.nav_host_fragment) as NavHostFragment
        navController = navHostFragment.navController


        binding.navBar.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.homePage -> {
                    navController.navigate(R.id.action_global_to_homePage)
                    true
                }
                R.id.favoritesPage -> {
                    navController.navigate(R.id.action_global_to_favoritesPage)
                    true
                }
                R.id.searchPage -> {
                    navController.navigate(R.id.action_global_to_searchPage)
                    true
                }
                else -> false
            }
        }

        val bottomNav = findViewById<BottomNavigationView>(R.id.nav_bar)

        handleAutoLogin()

        createNotificationChannel()

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ActivityCompat.checkSelfPermission(
                    this, Manifest.permission.POST_NOTIFICATIONS
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                ActivityCompat.requestPermissions(
                    this,
                    arrayOf(Manifest.permission.POST_NOTIFICATIONS),
                    1
                )
            }
        }

        navController.addOnDestinationChangedListener { _, destination, _ ->
            bottomNav?.visibility = when (destination.id) {
                R.id.welcomePage, R.id.signinPage, R.id.createAccountPage -> View.GONE
                else -> View.VISIBLE
            }

            if (destination.id == R.id.homePage) {
                checkFavoritesAndNotify()
            }
        }

    }

    private fun createNotificationChannel() {
        val name = "Favorites Notification"
        val descriptionText = "Notification to encourage adding books to favorites"
        val importance = NotificationManager.IMPORTANCE_HIGH
        val channel = NotificationChannel("favorites_channel", name, importance).apply {
            description = descriptionText
        }

        val notificationManager: NotificationManager =
            getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.createNotificationChannel(channel)
    }

    private fun checkFavoritesAndNotify() {
        if ((CurrentUser.user?.favoriteBooks?.isEmpty() == true) && !isNotificationSent) {
            sendNotification()
            isNotificationSent = true
        }
    }

    private fun sendNotification() {
        val intent = Intent(this, MainActivity::class.java)
        val pendingIntent: PendingIntent = PendingIntent.getActivity(
            this,
            1,
            intent,
            PendingIntent.FLAG_IMMUTABLE
        )

        val notificationBuilder = NotificationCompat.Builder(this, "favorites_channel")
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setContentTitle("Add Books to Favorites!")
            .setContentText("Your favorites list is empty. Explore and add books now!")
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ActivityCompat.checkSelfPermission(
                    this, Manifest.permission.POST_NOTIFICATIONS
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                return
            }
        }

        with(NotificationManagerCompat.from(this)) {
            notify(1, notificationBuilder.build())
        }
    }

    private fun handleAutoLogin() {
        val navHostFragment =
            supportFragmentManager.findFragmentById(R.id.nav_host_fragment) as NavHostFragment
        navController = navHostFragment.navController

        val graph = navController.navInflater.inflate(R.navigation.nav_graph)
        val currentUser = FirebaseAuth.getInstance().currentUser

        if (currentUser != null) {
            // Load user data from Firestore
            val db = FirebaseFirestore.getInstance()
            val email = currentUser.email ?: ""

            db.collection("users").document(email).get()
                .addOnSuccessListener { document ->
                    if (document != null && document.exists()) {
                        val firstName = document.getString("firstName") ?: ""
                        val lastName = document.getString("lastName") ?: ""
                        val emailFromDb = document.getString("email") ?: ""

                        val favoriteBooksList = document.get("favoriteBooks") as? List<Map<String, String>> ?: emptyList()
                        val favoriteBooks = ArrayList<Book>()

                        for (bookData in favoriteBooksList) {
                            val book = Book(
                                id = bookData["id"] ?: "",
                                title = bookData["title"],
                                subtitle = null,
                                description = null,
                                authors = bookData["authors"],
                                publisher = null,
                                pages = null,
                                year = null,
                                image = bookData["image"],
                                url = null,
                                download = null
                            )
                            favoriteBooks.add(book)
                        }

                        CurrentUser.user = User(firstName, lastName, emailFromDb, "", favoriteBooks)

                        graph.setStartDestination(R.id.homePage)
                        navController.graph = graph
                    }
                }
                .addOnFailureListener {
                    graph.setStartDestination(R.id.welcomePage)
                    navController.graph = graph
                }
        } else {
            graph.setStartDestination(R.id.welcomePage)
            navController.graph = graph
        }
    }

}
