package com.example.photogallery.data

import android.net.Uri
import java.util.UUID

data class Photo(
    val id: String = UUID.randomUUID().toString(),
    val uri: Uri,
    val name: String = "Photo",
    val timestamp: Long = System.currentTimeMillis(),
    val isFavorite: Boolean = false
)
