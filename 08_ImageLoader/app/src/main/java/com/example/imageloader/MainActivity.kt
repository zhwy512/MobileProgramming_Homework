package com.example.imageloader

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.graphics.Bitmap
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.loader.app.LoaderManager
import androidx.loader.content.Loader
import com.example.imageloader.loaders.ImageAsyncTaskLoader
import com.example.imageloader.service.ImageLoaderService
import com.example.imageloader.tasks.ImageLoadAsyncTask
import com.example.imageloader.ui.theme.ImageLoaderTheme

class MainActivity : ComponentActivity() {
    private var internetConnected = mutableStateOf(true)
    private val connectivityReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            updateConnectivityStatus()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Log.d("MainActivity", "onCreate called, savedInstanceState: $savedInstanceState")

        // Check initial connectivity
        updateConnectivityStatus()

        // Register for connectivity changes
        registerReceiver(
            connectivityReceiver,
            IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION)
        )

        // Start the background service
        val serviceIntent = Intent(this, ImageLoaderService::class.java)
        ContextCompat.startForegroundService(this, serviceIntent)

        setContent {
            ImageLoaderTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    ImageLoaderApp(internetConnected.value)
                }
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        Log.d("MainActivity", "onDestroy called")
        unregisterReceiver(connectivityReceiver)
    }

    private fun updateConnectivityStatus() {
        val connectivityManager = getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val networkCapabilities = connectivityManager.activeNetwork?.let {
            connectivityManager.getNetworkCapabilities(it)
        }
        internetConnected.value = networkCapabilities?.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET) == true
    }
}

@Composable
fun ImageLoaderApp(isConnected: Boolean) {
    val context = LocalContext.current
    var urlText by remember { mutableStateOf(TextFieldValue("")) }
    var imageBitmap by remember { mutableStateOf<Bitmap?>(null) }
    var loadingState by rememberSaveable { mutableStateOf(LoadingState.IDLE) }

    val mainActivity = context as MainActivity

    val loaderCallback = remember {
        object : LoaderManager.LoaderCallbacks<Bitmap?> {
            override fun onCreateLoader(id: Int, args: Bundle?): Loader<Bitmap?> {
                val url = args?.getString("url") ?: ""
                Log.d("ImageLoaderApp", "Creating loader for URL: $url")
                return ImageAsyncTaskLoader(context, url)
            }

            override fun onLoadFinished(loader: Loader<Bitmap?>, data: Bitmap?) {
                Log.d("ImageLoaderApp", "Loader finished with data: ${data != null}")
                loadingState = if (data != null) {
                    imageBitmap = data
                    LoadingState.SUCCESS
                } else {
                    LoadingState.ERROR
                }
            }

            override fun onLoaderReset(loader: Loader<Bitmap?>) {
                Log.d("ImageLoaderApp", "Loader reset")
                imageBitmap = null
            }
        }
    }

    // Only initialize the loader once (empty at first)
    LaunchedEffect(Unit) {
        LoaderManager.getInstance(mainActivity).initLoader(0, null, loaderCallback)
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "URL Image Loader",
            style = MaterialTheme.typography.headlineMedium,
            modifier = Modifier.padding(bottom = 16.dp)
        )
        TextField(
            value = urlText,
            onValueChange = { urlText = it },
            label = { Text("Image URL") },
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp)
        )
        Text(
            text = if (isConnected) "Internet Connected" else "No Internet Connection",
            color = if (isConnected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.error,
            modifier = Modifier.padding(bottom = 8.dp)
        )
        Button(
            onClick = {
                Log.d("ImageLoaderApp", "Load button clicked")
                loadingState = LoadingState.LOADING
                imageBitmap = null
                val args = Bundle().apply { putString("url", urlText.text) }
                LoaderManager.getInstance(mainActivity)
                    .restartLoader(0, args, loaderCallback)
            },
            enabled = isConnected,
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp)
        ) {
            Text("Load Image")
        }

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f),
            contentAlignment = Alignment.Center
        ) {
            when (loadingState) {
                LoadingState.IDLE -> Text("Enter a URL and press Load Image")
                LoadingState.LOADING -> CircularProgressIndicator()
                LoadingState.SUCCESS -> {
                    imageBitmap?.let {
                        Image(
                            bitmap = it.asImageBitmap(),
                            contentDescription = "Loaded Image",
                            modifier = Modifier.fillMaxSize()
                        )
                    } ?: Text("Image not loaded")
                }
                LoadingState.ERROR -> Text("", color = MaterialTheme.colorScheme.error)
            }
        }
    }
}

enum class LoadingState {
    IDLE, LOADING, SUCCESS, ERROR
}