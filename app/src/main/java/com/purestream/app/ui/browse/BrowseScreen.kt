package com.purestream.app.ui.browse

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Folder
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.purestream.app.ui.library.LibraryViewModel

@Composable
fun BrowseScreen(viewModel: LibraryViewModel = hiltViewModel()) {
    val groupedFolders by viewModel.groupedVideos.collectAsState(initial = emptyMap())

    if (groupedFolders.isEmpty()) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text("No folders found.")
        }
    } else {
        LazyColumn(modifier = Modifier.fillMaxSize()) {
            groupedFolders.forEach { (folderName, videoList) ->
                item {
                    ListItem(
                        headlineContent = { Text(folderName, fontWeight = FontWeight.Bold) },
                        supportingContent = { Text("${videoList.size} items") },
                        leadingContent = {
                            Icon(
                                imageVector = Icons.Outlined.Folder,
                                contentDescription = "Folder",
                                modifier = Modifier.size(40.dp),
                                tint = MaterialTheme.colorScheme.primary
                            )
                        },
                        modifier = Modifier.clickable { /* Future: Open Folder */ }
                    )
                    HorizontalDivider()
                }
            }
        }
    }
}