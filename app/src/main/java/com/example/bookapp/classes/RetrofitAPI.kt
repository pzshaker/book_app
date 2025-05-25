package com.example.bookapp.classes

import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.Path

interface RetrofitAPI {
    @GET("recent")
    fun getBooks(): Call<BooksResponse>

    @GET("book/{id}")
    fun getBookDetails(@Path("id") id: String): Call<Book>

    @GET("search/{query}")
    fun searchBooks(@Path("query") query: String): Call<BooksResponse>
}