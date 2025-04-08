package com.example.photogallery.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "photos")
data class PhotoEntity(
    @PrimaryKey val id: String,
    val uri: String,
    val name: String,
    val timestamp: Long,
    val isFavorite: Boolean = false
)
