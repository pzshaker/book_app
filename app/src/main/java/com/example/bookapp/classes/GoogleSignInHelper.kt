package com.example.bookapp.classes

import android.widget.Toast
import androidx.credentials.CredentialManager
import androidx.credentials.GetCredentialRequest
import androidx.credentials.exceptions.GetCredentialException
import androidx.fragment.app.Fragment
import com.google.android.libraries.identity.googleid.GetGoogleIdOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

object GoogleSignInHelper {

    fun handleGoogleSignIn(
        fragment: Fragment,
        navigateToHome: () -> Unit
    ) {
        val context = fragment.requireContext()
        val credentialManager = CredentialManager.create(context)

        val googleIdOption = GetGoogleIdOption.Builder()
            .setFilterByAuthorizedAccounts(false)
            .setServerClientId("1022947997512-89vgli0sitfvr2uekio3drcglipki2mm.apps.googleusercontent.com")
            .build()

        val request = GetCredentialRequest.Builder()
            .addCredentialOption(googleIdOption)
            .build()

        CoroutineScope(Dispatchers.Main).launch {
            try {
                val result = credentialManager.getCredential(context, request)
                val googleCredential = GoogleIdTokenCredential.createFrom(result.credential.data)
                val idToken = googleCredential.idToken

                if (idToken != null) {
                    val email = googleCredential.id

                    val db = FirebaseFirestore.getInstance()
                    val userDocRef = db.collection("users").document(email)

                    userDocRef.get()
                        .addOnSuccessListener { document ->
                            if (document != null && document.exists()) {
                                val firstName = document.getString("firstName") ?: ""
                                val lastName = document.getString("lastName") ?: ""
                                val emailFromDb = document.getString("email") ?: ""

                                val favoriteBooksList = document.get("favoriteBooks") as? List<Map<String, String>> ?: emptyList()
                                val favoriteBooks = ArrayList<Book>()
                                for (bookData in favoriteBooksList) {
                                    val book = Book(
                                        id = bookData["id"] as String,
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

                            } else {
                                val newUser = User(
                                    firstName = "",
                                    lastName = "",
                                    email = email,
                                    password = "",
                                    favoriteBooks = ArrayList()
                                )
                                CurrentUser.user = newUser

                                val userMap = hashMapOf(
                                    "firstName" to "",
                                    "lastName" to "",
                                    "email" to email,
                                    "favoriteBooks" to ArrayList<String>()
                                )

                                userDocRef.set(userMap)
                                .addOnSuccessListener {
                                        Toast.makeText(context, "New Google user added to Firestore!", Toast.LENGTH_SHORT).show()
                                    }
                                    .addOnFailureListener { e ->
                                        Toast.makeText(context, "Error creating user: ${e.message}", Toast.LENGTH_SHORT).show()
                                    }
                            }

                            Toast.makeText(context, "Google Sign-In success!", Toast.LENGTH_SHORT).show()
                            navigateToHome()
                        }
                        .addOnFailureListener { e ->
                            Toast.makeText(context, "Failed to fetch user data: ${e.message}", Toast.LENGTH_SHORT).show()
                        }
                }
            } catch (e: GetCredentialException) {
                Toast.makeText(context, "Google Sign-In failed: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }
}