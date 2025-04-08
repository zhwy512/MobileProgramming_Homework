package com.example.photogallery

import android.Manifest
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.*
import androidx.compose.ui.platform.LocalContext
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.photogallery.ui.screens.*
import com.example.photogallery.ui.theme.PhotoGalleryTheme
import com.example.photogallery.viewmodel.PhotoViewModel

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        // Request permissions
        if (!hasReadExternalStoragePermission()) {
            ActivityCompat.requestPermissions(
                this,
                arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE),
                READ_EXTERNAL_STORAGE_REQUEST
            )
        }
        
        setContent {
            PhotoGalleryTheme {
                PhotoGalleryApp()
            }
        }
    }
    
    private fun hasReadExternalStoragePermission(): Boolean {
        return ContextCompat.checkSelfPermission(
            this,
            Manifest.permission.READ_EXTERNAL_STORAGE
        ) == PackageManager.PERMISSION_GRANTED
    }
    
    companion object {
        private const val READ_EXTERNAL_STORAGE_REQUEST = 1001
    }
}

@Composable
fun PhotoGalleryApp() {
    val navController = rememberNavController()
    val viewModel: PhotoViewModel = viewModel()
    val context = LocalContext.current

    // Image picker launcher
    val getContent = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let {
            viewModel.addPhoto(it)
        }
    }

    AnimatedNavHost(
        navController = navController,
        startDestination = "grid"
    ) {
        composable("grid") {
            FadeTransition(visible = true) { // Bao bọc PhotoGridScreen
                PhotoGridScreen(
                    onPhotoClick = { photo ->
                        navController.navigate("photo/${photo.id}")
                    },
                    onAddPhotoClick = {
                        getContent.launch("image/*")
                    },
                    onSettingsClick = {
                        navController.navigate("settings")
                    }
                )
            }
        }

        composable(
            route = "photo/{photoId}",
            arguments = listOf(navArgument("photoId") { type = NavType.StringType })
        ) { backStackEntry ->
            val photoId = backStackEntry.arguments?.getString("photoId") ?: return@composable
            SlideTransition(visible = true) { // Bao bọc FullPhotoScreen
                FullPhotoScreen(
                    photoId = photoId,
                    onNavigateBack = {
                        navController.popBackStack()
                    }
                )
            }
        }

        composable("settings") {
            FadeTransition(visible = true) { // Bao bọc SettingsScreen
                SettingsScreen(
                    onNavigateBack = {
                        navController.popBackStack()
                    }
                )
            }
        }
	composable("camera") {
    	    CameraScreen(
                onNavigateBack = {
                    navController.popBackStack()
                },
                onPhotoTaken = { uri ->
                    viewModel.addPhoto(uri)
                    navController.navigate("grid") {
                        popUpTo("grid") { inclusive = true }
            	    }
                }
            )
        }
    }
}