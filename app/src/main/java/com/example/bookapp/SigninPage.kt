package com.example.bookapp

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.example.bookapp.classes.Book
import com.example.bookapp.classes.CurrentUser
import com.example.bookapp.classes.GoogleSignInHelper
import com.example.bookapp.classes.User
import com.example.bookapp.databinding.FragmentSigninPageBinding
import com.google.firebase.FirebaseApp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.ktx.Firebase


class SigninPage : Fragment() {
    lateinit var binding: FragmentSigninPageBinding
    private lateinit var auth: FirebaseAuth

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return inflater.inflate(R.layout.fragment_signin_page, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding = FragmentSigninPageBinding.bind(view)

        FirebaseApp.initializeApp(requireContext())
        auth = Firebase.auth

        binding.googleSignInButton.setOnClickListener {
            GoogleSignInHelper.handleGoogleSignIn(this) {
                val action = SigninPageDirections.actionSigninPageToHomePage()
                findNavController().navigate(action)
            }
        }

        binding.signUpBTN.setOnClickListener {
            val action = SigninPageDirections.actionSigninPageToCreateAccountPage()
            findNavController().navigate(action)
        }

        binding.loginButton.setOnClickListener {
            val email = binding.emailEditText.text.toString()
            val password = binding.passwordEditText.text.toString()

            if (email.isNotBlank()) {
                if (password.isNotBlank()) {
                    auth.signInWithEmailAndPassword(email, password)
                        .addOnCompleteListener(requireActivity()) { task ->
                            if (task.isSuccessful) {
                                val db = FirebaseFirestore.getInstance()
                                db.collection("users")
                                    .document(email)
                                    .get()
                                    .addOnSuccessListener { document ->
                                        if (document != null && document.exists()) {
                                            val firstName = document.getString("firstName") ?: ""
                                            val lastName = document.getString("lastName") ?: ""
                                            val emailFromDb = document.getString("email") ?: ""

                                            val favoriteBooksList =
                                                document.get("favoriteBooks") as? List<Map<String, String>>
                                                    ?: emptyList()

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

                                            CurrentUser.user = User(
                                                firstName,
                                                lastName,
                                                emailFromDb,
                                                password,
                                                favoriteBooks
                                            )

                                            Toast.makeText(context,"Login successful!", Toast.LENGTH_SHORT).show()
                                            val action =
                                                SigninPageDirections.actionSigninPageToHomePage()
                                            findNavController().navigate(action)
                                        } else {
                                            Toast.makeText(context,"User data not found in Firestore", Toast.LENGTH_SHORT).show()
                                        }
                                    }
                            } else {
                                Toast.makeText(context, "Incorrect email or password", Toast.LENGTH_SHORT).show()
                            }
                        }
                } else {
                    Toast.makeText(context, "Please enter a password.", Toast.LENGTH_SHORT).show()
                }
            } else {
                Toast.makeText(context, "Please enter an email.", Toast.LENGTH_SHORT).show()
            }
        }
    }
}
