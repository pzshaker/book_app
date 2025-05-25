package com.example.bookapp

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.example.bookapp.classes.CurrentUser
import com.example.bookapp.classes.GoogleSignInHelper
import com.example.bookapp.classes.User
import com.example.bookapp.databinding.FragmentCreateAccountPageBinding
import com.google.firebase.FirebaseApp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.ktx.Firebase


class CreateAccountPage : Fragment() {
    private lateinit var binding: FragmentCreateAccountPageBinding
    private lateinit var auth: FirebaseAuth

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return inflater.inflate(R.layout.fragment_create_account_page, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding = FragmentCreateAccountPageBinding.bind(view)

        FirebaseApp.initializeApp(requireContext())
        auth = Firebase.auth

        binding.loginBTN.setOnClickListener {
            val action = CreateAccountPageDirections.actionCreateAccountPageToSigninPage()
            findNavController().navigate(action)
        }

        binding.signUpButton.setOnClickListener {
            val firstName = binding.firstNameEditText.text.toString()
            val lastName = binding.lastNameEditText.text.toString()
            val email = binding.emailEditText.text.toString()
            val password = binding.passwordEditText.text.toString()

            if (firstName.isNotBlank() && lastName.isNotBlank() && email.isNotBlank() && password.isNotBlank()) {
                auth.createUserWithEmailAndPassword(email, password)
                    .addOnCompleteListener(requireActivity()) { task ->
                        if (task.isSuccessful) {
                            val firebaseUser = auth.currentUser
                            if (firebaseUser != null) {
                                Toast.makeText(context, "Account created!", Toast.LENGTH_SHORT).show()

                                val newUser = User(firstName, lastName, email, password)
                                CurrentUser.user = newUser

                                // Save user to Firestore
                                val db = FirebaseFirestore.getInstance()
                                val userMap = hashMapOf(
                                    "firstName" to firstName,
                                    "lastName" to lastName,
                                    "email" to email,
                                    "favoriteBooks" to ArrayList<String>()
                                )

                                db.collection("users")
                                    .document(email)
                                    .set(userMap)

                                val action = CreateAccountPageDirections.actionCreateAccountPageToHomePage()
                                findNavController().navigate(action)
                            }
                        } else {
                            Toast.makeText(context, "Failed to create account.", Toast.LENGTH_SHORT).show()
                        }
                    }
            } else {
                Toast.makeText(requireContext(), "Please fill in all fields", Toast.LENGTH_SHORT).show()
            }
        }

        binding.googleSignInButton.setOnClickListener {
            GoogleSignInHelper.handleGoogleSignIn(this) {
                val action = CreateAccountPageDirections.actionCreateAccountPageToHomePage()
                findNavController().navigate(action)
            }
        }
    }
}
