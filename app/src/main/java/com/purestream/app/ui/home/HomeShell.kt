package com.purestream.app.ui.home

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.hilt.navigation.compose.hiltViewModel
import com.purestream.app.ui.browse.BrowseScreen
import com.purestream.app.ui.library.LibraryViewModel
import com.purestream.app.ui.video.VideoScreen
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.ui.unit.dp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeShell(
    onVideoClick: (String) -> Unit,
    onSettingsClick: () -> Unit,
    viewModel: LibraryViewModel = hiltViewModel()
) {
    // This state controls which screen is currently visible in the middle
    var currentScreen by remember { mutableStateOf("video") }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("PureStream", fontWeight = FontWeight.Bold) },
                actions = {
                    IconButton(onClick = { /* Future Search */ }) {
                        Icon(Icons.Default.Search, contentDescription = "Search")
                    }
                    IconButton(onClick = { viewModel.refreshMedia() }) {
                        Icon(Icons.Default.Refresh, contentDescription = "Scan")
                    }
                }
            )
        },
        bottomBar = {
            NavigationBar(
                containerColor = MaterialTheme.colorScheme.surface,
                tonalElevation = 8.dp
            ) {
                NavigationBarItem(
                    selected = currentScreen == "video",
                    onClick = { currentScreen = "video" },
                    icon = {
                        Icon(
                            imageVector = if (currentScreen == "video") Icons.Filled.Movie else Icons.Outlined.Movie,
                            contentDescription = "Video"
                        )
                    },
                    label = { Text("Video") }
                )
                NavigationBarItem(
                    selected = currentScreen == "audio",
                    onClick = { currentScreen = "audio" },
                    icon = {
                        Icon(
                            imageVector = if (currentScreen == "audio") Icons.Filled.AudioFile else Icons.Outlined.AudioFile,
                            contentDescription = "Audio"
                        )
                    },
                    label = { Text("Audio") }
                )
                NavigationBarItem(
                    selected = currentScreen == "browse",
                    onClick = { currentScreen = "browse" },
                    icon = {
                        Icon(
                            imageVector = if (currentScreen == "browse") Icons.Filled.Folder else Icons.Outlined.Folder,
                            contentDescription = "Browse"
                        )
                    },
                    label = { Text("Browse") }
                )
                NavigationBarItem(
                    selected = false,
                    onClick = onSettingsClick,
                    icon = { Icon(Icons.Default.MoreHoriz, contentDescription = "More") },
                    label = { Text("More") }
                )
            }
        }
    ) { paddingValues ->
        // This is the "Shell" logic. It swaps the screen based on the bottom bar click!
        Box(modifier = Modifier.padding(paddingValues)) {
            when (currentScreen) {
                "video" -> VideoScreen(onVideoClick = onVideoClick)
                "browse" -> BrowseScreen()
                "audio" -> Box(contentAlignment = androidx.compose.ui.Alignment.Center, modifier = Modifier.fillMaxSize()) {
                    Text("Audio Player Coming Soon")
                }
            }
        }
    }
}