package com.example.bookapp

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.bookapp.classes.Book
import com.example.bookapp.classes.BooksAdapter
import com.example.bookapp.classes.CurrentUser
import com.example.bookapp.classes.RetrofitAPI
import com.example.bookapp.databinding.FragmentFavoritesPageBinding
import com.google.firebase.firestore.FirebaseFirestore
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

class FavoritesPage : Fragment() {
    lateinit var binding: FragmentFavoritesPageBinding
    lateinit var recyclerView: RecyclerView
    private lateinit var adapter: BooksAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_favorites_page, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding = FragmentFavoritesPageBinding.bind(view)
        recyclerView = binding.recyclerView
        recyclerView.layoutManager = GridLayoutManager(requireContext(), 2)
        adapter = BooksAdapter(
            CurrentUser.user?.favoriteBooks ?: arrayListOf(),
            BooksAdapter.SourceFragment.FAVORITES
        )
        recyclerView.adapter = adapter
        loadFavoriteBooks()
    }

    private fun loadFavoriteBooks() {
        val db = FirebaseFirestore.getInstance()
        val currentUser = CurrentUser.user ?: return

        db.collection("users")
            .document(currentUser.email)
            .get()
            .addOnSuccessListener { document ->
                if (document != null && document.exists()) {
                    val favoriteBooksList =
                        document.get("favoriteBooks") as? List<Map<String, String>> ?: emptyList()

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

                    CurrentUser.user?.favoriteBooks?.clear()
                    CurrentUser.user?.favoriteBooks?.addAll(favoriteBooks)

                    adapter.bookList.clear()
                    adapter.bookList.addAll(favoriteBooks)
                    adapter.notifyDataSetChanged()
                }
            }
    }
}