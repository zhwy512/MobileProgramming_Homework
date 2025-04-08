package com.example.photogallery.ui.screens

import android.Manifest
import android.content.Context
import android.net.Uri
import android.util.Log
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.camera.core.*
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Camera
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.photogallery.viewmodel.PhotoViewModel
import java.io.File
import java.text.SimpleDateFormat
import java.util.*
import java.util.concurrent.Executor
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CameraScreen(
    onNavigateBack: () -> Unit,
    onPhotoTaken: (Uri) -> Unit,
    viewModel: PhotoViewModel = viewModel()
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    
    // Check camera permission
    var hasCameraPermission by remember { mutableStateOf(false) }
    val requestPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        hasCameraPermission = isGranted
    }
    
    // Request permission when screen appears
    LaunchedEffect(key1 = true) {
        requestPermissionLauncher.launch(Manifest.permission.CAMERA)
    }
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Take Photo") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(
                            imageVector = Icons.Default.ArrowBack,
                            contentDescription = "Back"
                        )
                    }
                }
            )
        }
    ) { paddingValues ->
        if (hasCameraPermission) {
            Box(modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
            ) {
                // Camera preview
                val cameraProviderFuture = remember { ProcessCameraProvider.getInstance(context) }
                var imageCapture: ImageCapture? by remember { mutableStateOf(null) }
                var preview by remember { mutableStateOf<Preview?>(null) }
                val executor = ContextCompat.getMainExecutor(context)
                
                AndroidView(
                    factory = { ctx ->
                        val previewView = PreviewView(ctx)
                        val executor = ContextCompat.getMainExecutor(ctx)
                        cameraProviderFuture.addListener({
                            val cameraProvider = cameraProviderFuture.get()
                            // Preview
                            preview = Preview.Builder().build().also {
                                it.setSurfaceProvider(previewView.surfaceProvider)
                            }

                            // Image capture
                            imageCapture = ImageCapture.Builder()
                                .setCaptureMode(ImageCapture.CAPTURE_MODE_MAXIMIZE_QUALITY)
                                .build()

                            // Select back camera
                            val cameraSelector = CameraSelector.DEFAULT_BACK_CAMERA

                            try {
                                // Unbind any bound use cases before rebinding
                                cameraProvider.unbindAll()

                                // Bind use cases to camera
                                cameraProvider.bindToLifecycle(
                                    lifecycleOwner,
                                    cameraSelector,
                                    preview,
                                    imageCapture
                                )
                            } catch (e: Exception) {
                                Log.e("CameraScreen", "Use case binding failed", e)
                            }
                        }, executor)
                        previewView
                    },
                    modifier = Modifier.fillMaxSize()
                )
                
                // Camera controls
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp),
                    contentAlignment = Alignment.BottomCenter
                ) {
                    IconButton(
                        onClick = {
                            takePhoto(
                                imageCapture = imageCapture,
                                context = context,
                                executor = executor,
                                onPhotoTaken = onPhotoTaken,
                                onError = { Log.e("CameraScreen", "Photo capture failed: $it") }
                            )
                        },
                        modifier = Modifier
                            .size(72.dp)
                            .padding(8.dp)
                            .border(2.dp, Color.White, CircleShape)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Camera,
                            contentDescription = "Take photo",
                            tint = Color.White,
                            modifier = Modifier.size(36.dp)
                        )
                    }
                }
            }
        } else {
            // Show message when camera permission not granted
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Text("Camera permission is required to take photos")
                    Spacer(modifier = Modifier.height(16.dp))
                    Button(
                        onClick = {
                            requestPermissionLauncher.launch(Manifest.permission.CAMERA)
                        }
                    ) {
                        Text("Request Permission")
                    }
                }
            }
        }
    }
}

private fun takePhoto(
    imageCapture: ImageCapture?,
    context: Context,
    executor: Executor,
    onPhotoTaken: (Uri) -> Unit,
    onError: (String) -> Unit
) {
    imageCapture?.let { capture ->
        // Create a unique file name
        val photoFile = File(
            context.cacheDir,
            SimpleDateFormat("yyyyMMdd-HHmmss", Locale.US)
                .format(System.currentTimeMillis()) + ".jpg"
        )
        
        // Setup output options
        val outputOptions = ImageCapture.OutputFileOptions.Builder(photoFile).build()
        
        // Take the picture
        capture.takePicture(
            outputOptions,
            executor,
            object : ImageCapture.OnImageSavedCallback {
                override fun onImageSaved(outputFileResults: ImageCapture.OutputFileResults) {
                    outputFileResults.savedUri?.let { uri ->
                        onPhotoTaken(uri)
                    } ?: onError("Error: Saved image URI is null")
                }
                
                override fun onError(exception: ImageCaptureException) {
                    onError(exception.message ?: "Unknown error")
                }
            }
        )
    } ?: onError("Error: ImageCapture is null")
}