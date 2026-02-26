package com.weatherdrive.ui

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.ui.semantics.Role
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.weatherdrive.download.DownloadProgress
import com.weatherdrive.download.DownloadProgressState
import com.weatherdrive.model.FileItem
import com.weatherdrive.util.formatInfo
import com.weatherdrive.viewmodel.DownloadsListViewModel
import org.koin.compose.viewmodel.koinViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DownloadsListScreen(
    viewModel: DownloadsListViewModel = koinViewModel<DownloadsListViewModel>(),
    onBack: (() -> Unit)? = null
) {
    val downloads by viewModel.downloads.collectAsState()
    val completedDownloads = downloads.values.filter { 
        it.state == DownloadProgressState.Completed 
    }
    
    var itemToDelete by remember { mutableStateOf<FileItem?>(null) }
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Downloads") },
                navigationIcon = if (onBack != null) {
                    {
                        IconButton(onClick = onBack) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                contentDescription = "Back"
                            )
                        }
                    }
                } else {
                    {}
                }
            )
        }
    ) { paddingValues ->
        if (completedDownloads.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "No downloads yet",
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(
                    items = completedDownloads,
                    key = { it.fileItem.googleDriveId }
                ) { downloadProgress ->
                    DownloadItemCard(
                        downloadProgress = downloadProgress,
                        onPlayClick = { viewModel.playFile(downloadProgress.fileItem) },
                        onDeleteClick = { itemToDelete = downloadProgress.fileItem }
                    )
                }
            }
        }
    }
    
    // Delete confirmation dialog
    itemToDelete?.let { fileItem ->
        AlertDialog(
            onDismissRequest = { itemToDelete = null },
            title = { Text("Delete Download") },
            text = { 
                Text("Are you sure you want to delete \"${fileItem.title}\"? This will remove the downloaded file from your device.") 
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        viewModel.deleteDownload(fileItem)
                        itemToDelete = null
                    }
                ) {
                    Text("Delete", color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = {
                TextButton(onClick = { itemToDelete = null }) {
                    Text("Cancel")
                }
            }
        )
    }
}

@Composable
private fun DownloadItemCard(
    downloadProgress: DownloadProgress,
    onPlayClick: () -> Unit,
    onDeleteClick: () -> Unit
) {
    val fileItem = downloadProgress.fileItem
    
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onPlayClick, role = Role.Button),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = fileItem.title,
                    style = MaterialTheme.typography.titleMedium
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = fileItem.formatInfo(),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            
            Spacer(modifier = Modifier.width(8.dp))
            
            IconButton(onClick = onDeleteClick) {
                Icon(
                    imageVector = Icons.Default.Delete,
                    contentDescription = "Delete",
                    tint = MaterialTheme.colorScheme.error
                )
            }
        }
    }
}
