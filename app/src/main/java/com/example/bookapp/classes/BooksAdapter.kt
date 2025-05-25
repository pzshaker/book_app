package com.example.bookapp.classes

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.cardview.widget.CardView
import androidx.navigation.findNavController
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.bookapp.R
import com.example.bookapp.HomePageDirections
import com.example.bookapp.FavoritesPageDirections
import com.example.bookapp.SearchPageDirections


class BooksAdapter(var bookList: ArrayList<Book>, var sourceFragment: SourceFragment) :
    RecyclerView.Adapter<BooksAdapter.BookViewHolder>() {
    enum class SourceFragment {
        HOME,
        FAVORITES,
        SEARCH
    }

    class BookViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val bookCoverImageView: ImageView = itemView.findViewById(R.id.bookCoverImageView)
        val bookTitleTextView: TextView = itemView.findViewById(R.id.bookTitleTextView)
        val authorTextView: TextView = itemView.findViewById(R.id.authorTextView)
        val subtitleTextView: TextView = itemView.findViewById(R.id.subtitleTextView)
        val cardView: CardView = itemView.findViewById(R.id.cardView)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BookViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.card_view, parent, false)
        return BookViewHolder(view)
    }

    override fun getItemCount(): Int {
        return bookList.size
    }

    override fun onBindViewHolder(holder: BookViewHolder, position: Int) {
        val book = bookList[position]
        Glide.with(holder.itemView.context).load(book.image).placeholder(R.drawable.page_not_found)
            .into(holder.bookCoverImageView)
        holder.bookTitleTextView.text = book.title
        holder.authorTextView.text = book.authors
        holder.subtitleTextView.text = book.subtitle
        holder.cardView.setOnClickListener {
            val action = when (sourceFragment) {
                SourceFragment.HOME -> HomePageDirections.actionHomePageToBookDetailsPage(book.id)
                SourceFragment.FAVORITES -> FavoritesPageDirections.actionFavoritesPageToBookDetailsPage(book.id)
                SourceFragment.SEARCH -> SearchPageDirections.actionSearchPageToBookDetailsPage(book.id)
            }
            holder.itemView.findNavController().navigate(action)
        }
    }
}