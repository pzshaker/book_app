package com.example.bookapp.classes

data class User(
    val firstName: String,
    val lastName: String,
    val email: String,
    val password: String,
    val favoriteBooks: ArrayList<Book> = ArrayList()
)