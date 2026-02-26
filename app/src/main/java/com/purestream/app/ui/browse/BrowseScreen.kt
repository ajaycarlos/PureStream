package com.purestream.app.ui.browse

import android.os.Environment
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.VideoFile
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File

@Composable
fun BrowseScreen(
    onVideoClick: (String) -> Unit,
    searchQuery: String = ""
) {
    // Start at the root of the user's primary shared/external storage
    val rootPath = Environment.getExternalStorageDirectory().absolutePath
    var currentPath by remember { mutableStateOf(rootPath) }
    val currentDir = File(currentPath)

    // States for our background file crawler
    var filesAndFolders by remember { mutableStateOf<List<File>>(emptyList()) }
    var isSearching by remember { mutableStateOf(false) }

    // LaunchedEffect allows us to safely run background tasks when the path or query changes
    LaunchedEffect(currentPath, searchQuery) {
        if (searchQuery.isBlank()) {
            isSearching = false
            // SHALLOW SCAN: Just show what is inside the current folder
            val list = currentDir.listFiles()?.filter { file ->
                file.isDirectory || file.extension.lowercase() in listOf("mp4", "mkv", "avi", "webm")
            }?.sortedWith(compareBy({ !it.isDirectory }, { it.name.lowercase() })) ?: emptyList()

            filesAndFolders = list
        } else {
            isSearching = true
            // DEEP SCAN: Offload to IO thread so the UI doesn't freeze!
            withContext(Dispatchers.IO) {
                // walkTopDown() goes inside every folder recursively
                val list = currentDir.walkTopDown()
                    .onEnter { !it.isHidden && !it.name.startsWith(".") } // Skip hidden/system folders to save time
                    .filter { file ->
                        val isMediaOrDir = file.isDirectory || file.extension.lowercase() in listOf("mp4", "mkv", "avi", "webm")
                        val matchesSearch = file.name.contains(searchQuery, ignoreCase = true)

                        // Ensure it matches, is the right type, and isn't just returning the parent folder itself
                        isMediaOrDir && matchesSearch && file.absolutePath != currentPath
                    }
                    .toList()
                    .sortedWith(compareBy({ !it.isDirectory }, { it.name.lowercase() }))

                filesAndFolders = list
            }
            isSearching = false
        }
    }

    // Intercept hardware back button to navigate UP a directory level
    BackHandler(enabled = currentPath != rootPath && searchQuery.isBlank()) {
        val parent = currentDir.parent
        if (parent != null && currentPath != rootPath) {
            currentPath = parent
        }
    }

    Column(modifier = Modifier.fillMaxSize()) {
        // Breadcrumb / Current Path Header
        Surface(
            color = MaterialTheme.colorScheme.surfaceVariant,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(
                // Cleans up the path display for the user
                text = currentPath.replace(rootPath, "Internal Storage"),
                modifier = Modifier.padding(16.dp),
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        Box(modifier = Modifier.fillMaxSize()) {
            LazyColumn(modifier = Modifier.fillMaxSize()) {
                if (filesAndFolders.isEmpty() && !isSearching) {
                    item {
                        Box(
                            modifier = Modifier.fillMaxWidth().padding(32.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(if (searchQuery.isNotBlank()) "No results found" else "Empty folder")
                        }
                    }
                }

                items(filesAndFolders) { file ->
                    FileListItem(
                        file = file,
                        onClick = {
                            if (file.isDirectory) {
                                // Dive deeper into the selected folder
                                currentPath = file.absolutePath
                            } else {
                                // It's a video file, pass the absolute URI string to the player
                                onVideoClick(file.absolutePath)
                            }
                        }
                    )
                }
            }

            // Show a sleek loading spinner while deep scanning
            if (isSearching) {
                CircularProgressIndicator(
                    modifier = Modifier.align(Alignment.Center),
                    color = MaterialTheme.colorScheme.primary
                )
            }
        }
    }
}

@Composable
fun FileListItem(file: File, onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = if (file.isDirectory) Icons.Default.Folder else Icons.Default.VideoFile,
            contentDescription = if (file.isDirectory) "Folder" else "Video",
            tint = if (file.isDirectory) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.secondary,
            modifier = Modifier.size(40.dp)
        )
        Spacer(modifier = Modifier.width(16.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = file.name,
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.Medium,
                maxLines = 1
            )
            if (!file.isDirectory) {
                // Calculate and display file size in MB
                val fileSizeMb = file.length() / (1024 * 1024)
                Text(
                    text = "$fileSizeMb MB",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}