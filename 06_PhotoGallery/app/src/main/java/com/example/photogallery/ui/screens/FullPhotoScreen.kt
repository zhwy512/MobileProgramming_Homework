package com.example.photogallery.ui.screens

import androidx.compose.foundation.gestures.*
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.runtime.*
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.dp
import kotlin.math.absoluteValue

@Composable
fun FullPhotoScreen(
    photoId: String,
    onNavigateBack: () -> Unit,
    viewModel: PhotoViewModel = viewModel()
) {
    val photos by viewModel.photos.collectAsState()
    val currentIndex = photos.indexOfFirst { it.id == photoId }.takeIf { it >= 0 } ?: 0
    var currentPhotoIndex by remember { mutableStateOf(currentIndex) }
    val currentPhoto = photos.getOrNull(currentPhotoIndex) ?: return

    // Zoom and pan state
    var scale by remember { mutableStateOf(1f) }
    var offsetX by remember { mutableStateOf(0f) }
    var offsetY by remember { mutableStateOf(0f) }
    
    // For horizontal swiping detection
    val density = LocalDensity.current
    val minSwipeDistance = with(density) { 50.dp.toPx() }
    
    Box(modifier = Modifier.fillMaxSize()) {
        // Photo with zoom, pan, and swipe gestures
        AsyncImage(
            model = currentPhoto.uri,
            contentDescription = "Full size photo",
            contentScale = ContentScale.Fit,
            modifier = Modifier
                .fillMaxSize()
                .graphicsLayer(
                    scaleX = scale,
                    scaleY = scale,
                    translationX = offsetX,
                    translationY = offsetY
                )
                .pointerInput(Unit) {
                    detectTransformGestures { _, pan, zoom, _ ->
                        scale = (scale * zoom).coerceIn(1f, 3f)
                        
                        if (scale > 1f) {
                            val maxX = (scale - 1) * size.width / 2
                            val maxY = (scale - 1) * size.height / 2
                            
                            offsetX = (offsetX + pan.x).coerceIn(-maxX, maxX)
                            offsetY = (offsetY + pan.y).coerceIn(-maxY, maxY)
                        } else {
                            offsetX = 0f
                            offsetY = 0f
                        }
                    }
                }
                .pointerInput(currentPhotoIndex) {
                    // Only allow swiping when not zoomed in
                    if (scale <= 1f) {
                        var initialX = 0f
                        detectHorizontalDragGestures(
                            onDragStart = { initialX = it.x },
                            onDragEnd = {
                                // Only process if we've moved enough
                                if ((initialX - it.x).absoluteValue >= minSwipeDistance) {
                                    if (initialX < it.x && currentPhotoIndex > 0) {
                                        // Swiped right -> previous photo
                                        currentPhotoIndex--
                                    } else if (initialX > it.x && currentPhotoIndex < photos.size - 1) {
                                        // Swiped left -> next photo
                                        currentPhotoIndex++
                                    }
                                }
                            },
                            onDragCancel = { },
                            onHorizontalDrag = { change, _ ->
                                change.consume()
                            }
                        )
                    }
                }
        )
        
        // Navigation buttons - same as before
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .align(Alignment.BottomCenter)
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            IconButton(
                onClick = {
                    if (currentPhotoIndex > 0) {
                        currentPhotoIndex--
                        scale = 1f
                        offsetX = 0f
                        offsetY = 0f
                    }
                },
                enabled = currentPhotoIndex > 0
            ) {
                Icon(
                    Icons.Default.ArrowBack,
                    contentDescription = "Previous photo"
                )
            }
            
            IconButton(
                onClick = {
                    if (currentPhotoIndex < photos.size - 1) {
                        currentPhotoIndex++
                        scale = 1f
                        offsetX = 0f
                        offsetY = 0f
                    }
                },
                enabled = currentPhotoIndex < photos.size - 1
            ) {
                Icon(
                    Icons.Default.ArrowForward,
                    contentDescription = "Next photo"
                )
            }
        }
    }
}