package com.example.photogallery.viewmodel

import android.app.Application
import android.content.ContentUris
import android.net.Uri
import android.provider.MediaStore
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.photogallery.data.Photo
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
class PhotoViewModel(application: Application) : AndroidViewModel(application) {
    private val repository = PhotoRepository(application)
    
    val photos: StateFlow<List<Photo>> = repository.getAllPhotos()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )
    
    val favoritePhotos: StateFlow<List<Photo>> = repository.getFavoritePhotos()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )
    
    init {
        loadPhotosFromGallery()
    }
    
    private fun loadPhotosFromGallery() {
        viewModelScope.launch {
            val photoList = loadPhotosFromMediaStore()
            photoList.forEach { photo ->
                repository.addPhoto(photo)
            }
        }
    }
    
    fun toggleFavorite(photoId: String) {
        viewModelScope.launch {
            val photo = photos.value.find { it.id == photoId } ?: return@launch
            repository.toggleFavorite(photoId, !photo.isFavorite)
        }
    }
    
    fun deletePhoto(photoId: String) {
        viewModelScope.launch {
            repository.deletePhoto(photoId)
        }
    }
    
    fun addPhoto(uri: Uri) {
        viewModelScope.launch {
            val newPhoto = Photo(
                uri = uri,
                name = "Photo ${System.currentTimeMillis()}"
            )
            repository.addPhoto(newPhoto)
        }
    }
}