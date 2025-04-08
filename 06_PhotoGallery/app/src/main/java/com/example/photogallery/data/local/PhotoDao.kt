package com.example.photogallery.data.local

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface PhotoDao {
    @Query("SELECT * FROM photos ORDER BY timestamp DESC")
    fun getAllPhotos(): Flow<List<PhotoEntity>>
    
    @Query("SELECT * FROM photos WHERE isFavorite = 1 ORDER BY timestamp DESC")
    fun getFavoritePhotos(): Flow<List<PhotoEntity>>
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPhoto(photo: PhotoEntity)
    
    @Update
    suspend fun updatePhoto(photo: PhotoEntity)
    
    @Delete
    suspend fun deletePhoto(photo: PhotoEntity)
    
    @Query("DELETE FROM photos WHERE id = :photoId")
    suspend fun deletePhotoById(photoId: String)
    
    @Query("UPDATE photos SET isFavorite = :isFavorite WHERE id = :photoId")
    suspend fun updateFavoriteStatus(photoId: String, isFavorite: Boolean)
}