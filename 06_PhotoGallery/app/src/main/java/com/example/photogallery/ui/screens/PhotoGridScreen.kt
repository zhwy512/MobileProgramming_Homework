package com.example.photogallery.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import androidx.compose.ui.Alignment
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import com.example.photogallery.data.Photo
import com.example.photogallery.viewmodel.PhotoViewModel

@Composable
fun PhotoGridScreen(
    onPhotoClick: (Photo) -> Unit,
    onAddPhotoClick: () -> Unit,
    onSettingsClick: () -> Unit,
    viewModel: PhotoViewModel = viewModel()
) {
    val photos by viewModel.photos.collectAsState()
    var expandFab by remember { mutableStateOf(false) }
    
    Box(modifier = Modifier.fillMaxSize()) {
        LazyVerticalGrid(
            columns = GridCells.Adaptive(minSize = 128.dp),
            contentPadding = PaddingValues(4.dp, 4.dp, 4.dp, 80.dp), // Bottom padding for FAB
            modifier = Modifier.fillMaxSize()
        ) {
            items(photos) { photo ->
                PhotoItem(
                    photo = photo,
                    onClick = { onPhotoClick(photo) }
                )
            }
        }
        
        // Main FAB
        FloatingActionButton(
            onClick = { expandFab = !expandFab },
            modifier = Modifier
                .padding(16.dp)
                .align(Alignment.BottomEnd)
        ) {
            Icon(
                imageVector = Icons.Default.Add,
                contentDescription = "Add photo"
            )
        }
        
        // Expanded FAB options (visible when expandFab is true)
        if (expandFab) {
            Column(
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(bottom = 80.dp, end = 16.dp),
                horizontalAlignment = Alignment.End,
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // Settings option
                SmallFloatingActionButton(
                    onClick = {
                        expandFab = false
                        navController.navigate("camera")
                    }
                ) {
                    Icon(
                        imageVector = Icons.Default.Camera,
                        contentDescription = "Take photo"
                    )
                }
                
                // Add photo option
                SmallFloatingActionButton(
                    onClick = {
                        expandFab = false
                        onAddPhotoClick()
                    }
                ) {
                    Icon(
                        imageVector = Icons.Default.Add,
                        contentDescription = "Add photo"
                    )
                }
            }
        }
    }
}

@Composable
fun PhotoItem(
    photo: Photo,
    onClick: () -> Unit,
    onLongClick: () -> Unit,
    onFavorite: () -> Unit,
    onDelete: () -> Unit,
    viewModel: PhotoViewModel = viewModel()
) {
    var showOptions by remember { mutableStateOf(false) }
    val interactionSource = remember { MutableInteractionSource() }
    
    Box {
        Card(
            modifier = Modifier
                .padding(4.dp)
                .fillMaxWidth()
                .aspectRatio(1f)
                .combinedClickable(
                    interactionSource = interactionSource,
                    indication = null,
                    onClick = onClick,
                    onLongClick = {
                        showOptions = true
                        onLongClick()
                    }
                ),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
        ) {
            Box {
                AsyncImage(
                    model = photo.uri,
                    contentDescription = "Photo thumbnail",
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize()
                )
                
                // Show favorite icon if photo is favorited
                if (photo.isFavorite) {
                    Icon(
                        imageVector = Icons.Default.Favorite,
                        contentDescription = "Favorited",
                        tint = MaterialTheme.colorScheme.error,
                        modifier = Modifier
                            .align(Alignment.TopEnd)
                            .padding(4.dp)
                            .size(24.dp)
                    )
                }
            }
        }
        
        // Dropdown menu for long press
        DropdownMenu(
            expanded = showOptions,
            onDismissRequest = { showOptions = false }
        ) {
            DropdownMenuItem(
                text = { 
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = if (photo.isFavorite) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                            contentDescription = if (photo.isFavorite) "Unfavorite" else "Favorite"
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(if (photo.isFavorite) "Remove from favorites" else "Add to favorites")
                    }
                },
                onClick = {
                    onFavorite()
                    showOptions = false
                }
            )
            
            DropdownMenuItem(
                text = { 
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Delete,
                            contentDescription = "Delete"
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Delete")
                    }
                },
                onClick = {
                    onDelete()
                    showOptions = false
                }
            )
        }
    }
}