package com.example.bookapp

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.bookapp.classes.Book
import com.example.bookapp.classes.BooksAdapter
import com.example.bookapp.classes.BooksResponse
import com.example.bookapp.classes.RetrofitAPI
import com.example.bookapp.databinding.FragmentSearchPageBinding
import retrofit2.Call
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

class SearchPage : Fragment() {
    lateinit var binding: FragmentSearchPageBinding
    lateinit var recyclerView: RecyclerView
    private val baseUrl = "https://www.dbooks.org/api/"
    private val searchResults = ArrayList<Book>()
    lateinit var adapter: BooksAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_search_page, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding = FragmentSearchPageBinding.bind(view)
        recyclerView = binding.recyclerView
        recyclerView.layoutManager = GridLayoutManager(requireContext(), 2)
        adapter = BooksAdapter(searchResults, BooksAdapter.SourceFragment.SEARCH)
        recyclerView.adapter = adapter

        binding.imageButton.setOnClickListener {
            val query = binding.searchEditText.text.toString()
            if (query.isNotEmpty()) {
                searchBooks(query)
            } else {
                Toast.makeText(requireContext(), "Please enter a search query", Toast.LENGTH_SHORT)
                    .show()
            }
        }
    }

    private fun searchBooks(query: String) {
        val retrofit = Retrofit.Builder()
            .baseUrl(baseUrl)
            .addConverterFactory(GsonConverterFactory.create())
            .build()

        val retrofitAPI = retrofit.create(RetrofitAPI::class.java)
        val call: Call<BooksResponse> = retrofitAPI.searchBooks(query)

        call.enqueue(object : retrofit2.Callback<BooksResponse> {
            override fun onResponse(
                call: Call<BooksResponse>,
                response: Response<BooksResponse>
            ) {
                if (response.isSuccessful) {
                    val booksResponse = response.body()
                    val fetchedBooks = booksResponse?.books as? ArrayList<Book> ?: ArrayList()
                    searchResults.clear()
                    searchResults.addAll(fetchedBooks)
                    adapter.notifyDataSetChanged()
                } else {
                    Toast.makeText(requireContext(),"Failed to load search results",Toast.LENGTH_LONG).show()
                    searchResults.clear()
                    adapter.notifyDataSetChanged()
                }
            }

            override fun onFailure(call: Call<BooksResponse>, t: Throwable) {
                Toast.makeText(requireContext(), "Error: ${t.localizedMessage}", Toast.LENGTH_LONG).show()
                searchResults.clear()
                adapter.notifyDataSetChanged()
            }
        })
    }
}