package com.example.bookapp

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.bookapp.classes.Book
import com.example.bookapp.classes.BooksAdapter
import com.example.bookapp.classes.BooksResponse
import com.example.bookapp.classes.CurrentUser
import com.example.bookapp.classes.RetrofitAPI
import com.example.bookapp.databinding.FragmentHomePageBinding
import com.google.firebase.auth.FirebaseAuth
import retrofit2.Call
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

class HomePage : Fragment() {
    lateinit var binding: FragmentHomePageBinding
    lateinit var recyclerView: RecyclerView
    lateinit var adapter: BooksAdapter
    var bookList = ArrayList<Book>()
    private val baseUrl = "https://www.dbooks.org/api/"

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_home_page, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding = FragmentHomePageBinding.bind(view)
        recyclerView = binding.recyclerView
        recyclerView.layoutManager = GridLayoutManager(requireContext(), 2)
        adapter = BooksAdapter(bookList, BooksAdapter.SourceFragment.HOME)
        recyclerView.adapter = adapter


        if (bookList.isEmpty()) {
            fetchBooks()
        }

        binding.logoutButton.setOnClickListener {
            FirebaseAuth.getInstance().signOut()
            CurrentUser.user = null

            val action = HomePageDirections.actionHomePageToSigninPage()
            findNavController().navigate(action)
        }
    }

    private fun fetchBooks() {
        val retrofit =
            Retrofit.Builder().baseUrl(baseUrl).addConverterFactory(GsonConverterFactory.create())
                .build()

        val retrofitAPI = retrofit.create(RetrofitAPI::class.java)
        val call: Call<BooksResponse> = retrofitAPI.getBooks()

        call.enqueue(object : retrofit2.Callback<BooksResponse> {
            override fun onResponse(
                call: Call<BooksResponse>, response: Response<BooksResponse>
            ) {
                if (response.isSuccessful) {
                    val recentBooksResponse = response.body()
                    val fetchedBookList =
                        recentBooksResponse?.books as? ArrayList<Book> ?: ArrayList()
                    updateBookList(fetchedBookList)
                } else {
                    Log.e("HomePage", "API call failed with code: ${response.code()}")
                }
            }

            override fun onFailure(call: Call<BooksResponse>, t: Throwable) {
                Toast.makeText(requireContext(), t.localizedMessage, Toast.LENGTH_LONG).show()
            }
        })
    }

    fun updateBookList(newBookList: ArrayList<Book>) {
        bookList.clear()
        bookList.addAll(newBookList)
        adapter.notifyDataSetChanged()
    }
}