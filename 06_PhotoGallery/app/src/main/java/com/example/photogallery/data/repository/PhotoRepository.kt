package com.example.photogallery.data.repository

import android.content.Context
import android.net.Uri
import androidx.core.net.toUri
import com.example.photogallery.data.Photo
import com.example.photogallery.data.local.PhotoDatabase
import com.example.photogallery.data.local.PhotoEntity
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class PhotoRepository(context: Context) {
    private val photoDao = PhotoDatabase.getDatabase(context).photoDao()
    
    fun getAllPhotos(): Flow<List<Photo>> {
        return photoDao.getAllPhotos().map { entities ->
            entities.map { it.toPhoto() }
        }
    }
    
    fun getFavoritePhotos(): Flow<List<Photo>> {
        return photoDao.getFavoritePhotos().map { entities ->
            entities.map { it.toPhoto() }
        }
    }
    
    suspend fun addPhoto(photo: Photo) {
        photoDao.insertPhoto(photo.toEntity())
    }
    
    suspend fun updatePhoto(photo: Photo) {
        photoDao.updatePhoto(photo.toEntity())
    }
    
    suspend fun deletePhoto(photoId: String) {
        photoDao.deletePhotoById(photoId)
    }
    
    suspend fun toggleFavorite(photoId: String, isFavorite: Boolean) {
        photoDao.updateFavoriteStatus(photoId, isFavorite)
    }
    
    private fun PhotoEntity.toPhoto(): Photo {
        return Photo(
            id = this.id,
            uri = this.uri.toUri(),
            name = this.name,
            timestamp = this.timestamp,
            isFavorite = this.isFavorite
        )
    }
    
    private fun Photo.toEntity(): PhotoEntity {
        return PhotoEntity(
            id = this.id,
            uri = this.uri.toString(),
            name = this.name,
            timestamp = this.timestamp,
            isFavorite = this.isFavorite
        )
    }
}