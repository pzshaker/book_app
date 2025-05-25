package com.example.bookapp

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.bumptech.glide.Glide
import com.example.bookapp.classes.Book
import com.example.bookapp.classes.CurrentUser
import com.example.bookapp.classes.RetrofitAPI
import com.example.bookapp.classes.User
import com.example.bookapp.databinding.FragmentBookDetailsPageBinding
import com.google.firebase.firestore.FirebaseFirestore
import retrofit2.Call
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

class BookDetailsPage : Fragment() {
    lateinit var binding: FragmentBookDetailsPageBinding
    private val baseUrl = "https://www.dbooks.org/api/"
    private var fetchedBook: Book? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_book_details_page, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding = FragmentBookDetailsPageBinding.bind(view)

        val bookId = arguments?.getString("bookId")
        if (bookId != null) {
            fetchBook(bookId)
        } else {
            Log.e("BookDetailsPage", "Book ID is null")
        }

        binding.favoriteButton.setOnClickListener {
            val currentUser = CurrentUser.user
            val book = fetchedBook

            if (currentUser != null && book != null) {
                val favorites = currentUser.favoriteBooks

                if (favorites.contains(book)) {
                    favorites.remove(book)
                    Toast.makeText(
                        requireContext(),
                        "${book.title} removed from Favorites!",
                        Toast.LENGTH_SHORT
                    ).show()
                    binding.favoriteButton.setBackgroundResource(R.drawable.favorite)
                } else {
                    favorites.add(book)
                    Toast.makeText(
                        requireContext(), "${book.title} added to Favorites!", Toast.LENGTH_SHORT
                    ).show()
                    binding.favoriteButton.setBackgroundResource(R.drawable.favorite_filled)
                }
                saveFavoriteBooksToFirestore(currentUser)
            }
        }
    }

    private fun fetchBook(bookId: String) {
        val retrofit =
            Retrofit.Builder().baseUrl(baseUrl).addConverterFactory(GsonConverterFactory.create())
                .build()

        val retrofitAPI = retrofit.create(RetrofitAPI::class.java)
        val call = retrofitAPI.getBookDetails(bookId)

        call.enqueue(object : retrofit2.Callback<Book> {
            override fun onResponse(call: Call<Book>, response: Response<Book>) {
                if (response.isSuccessful) {
                    response.body()?.let { book ->
                        fetchedBook = book

                        Glide.with(this@BookDetailsPage).load(book.image)
                            .into(binding.bookCoverImageView)
                        binding.bookTitleTextView.text = book.title
                        binding.authorTextView.text = book.authors
                        binding.publisherTextView.text = book.publisher
                        binding.pagesTextView.text = "${book.pages} pages"
                        binding.yearTextView.text = book.year
                        binding.descriptionTextView.text = book.description

                        val isFavorite = CurrentUser.user?.favoriteBooks?.contains(book) == true
                        binding.favoriteButton.setBackgroundResource(
                            if (isFavorite) R.drawable.favorite_filled else R.drawable.favorite
                        )

                        binding.downloadButton.setOnClickListener {
                            val intent = android.content.Intent(
                                android.content.Intent.ACTION_VIEW,
                                android.net.Uri.parse(book.download)
                            )
                            startActivity(intent)
                        }
                    }
                } else {
                    Toast.makeText(
                        requireContext(), "Failed to load book details", Toast.LENGTH_LONG
                    ).show()
                }
            }

            override fun onFailure(call: Call<Book>, t: Throwable) {
                Toast.makeText(requireContext(), "Error: ${t.localizedMessage}", Toast.LENGTH_LONG)
                    .show()
            }
        })
    }

    private fun saveFavoriteBooksToFirestore(user: User) {
        val db = FirebaseFirestore.getInstance()

        val favoriteBooksData = user.favoriteBooks.map { book ->
            mapOf(
                "id" to book.id,
                "title" to book.title,
                "authors" to book.authors,
                "image" to book.image
            )
        }

        db.collection("users")
            .document(user.email)
            .update("favoriteBooks", favoriteBooksData)
    }
}
