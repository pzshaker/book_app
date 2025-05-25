package com.example.bookapp.classes

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class Book(
    val id: String,
    val title: String?,
    val subtitle: String?,
    val description: String?,
    val authors: String?,
    val publisher: String?,
    val pages: String?,
    val year: String?,
    val image: String?,
    val url: String?,
    val download: String?
) : Parcelable {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is Book) return false
        return id == other.id
    }

    override fun hashCode(): Int {
        return id.hashCode()
    }
}