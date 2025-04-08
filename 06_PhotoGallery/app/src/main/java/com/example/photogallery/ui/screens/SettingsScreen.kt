package com.example.photogallery.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.photogallery.viewmodel.PhotoViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    onNavigateBack: () -> Unit,
    viewModel: PhotoViewModel = viewModel()
) {
    var showFavoritesOnly by remember { mutableStateOf(false) }
    var gridColumns by remember { mutableStateOf(3) }
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Settings") },
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
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Show favorites only switch
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("Show favorites only")
                Switch(
                    checked = showFavoritesOnly,
                    onCheckedChange = { showFavoritesOnly = it }
                )
            }
            
            // Grid columns slider
            Text("Grid columns: $gridColumns")
            Slider(
                value = gridColumns.toFloat(),
                onValueChange = { gridColumns = it.toInt() },
                valueRange = 2f..5f,
                steps = 2
            )
            
            // Version info
            Spacer(modifier = Modifier.weight(1f))
            Text(
                text = "Photo Gallery v1.0",
                style = MaterialTheme.typography.bodySmall,
                modifier = Modifier.align(Alignment.CenterHorizontally)
            )
        }
    }
}