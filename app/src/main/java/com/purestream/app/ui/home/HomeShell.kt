package com.purestream.app.ui.home

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import com.purestream.app.ui.browse.BrowseScreen
import com.purestream.app.ui.library.LibraryViewModel
import com.purestream.app.ui.video.VideoScreen

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeShell(
    onVideoClick: (String) -> Unit,
    onSettingsClick: () -> Unit,
    viewModel: LibraryViewModel = hiltViewModel()
) {
    var currentScreen by remember { mutableStateOf("video") }

    // --- State Extractors ---
    var isSearchActive by remember { mutableStateOf(false) }
    val searchQuery by viewModel.searchQuery.collectAsState()
    val lastPlayedUri by viewModel.lastPlayedUri.collectAsState()

    // --- LIFECYCLE FIX: Refresh data exactly when returning from the Video Player ---
    val lifecycleOwner = LocalLifecycleOwner.current
    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) {
                viewModel.refreshLastPlayed()
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    if (isSearchActive) {
                        TextField(
                            value = searchQuery,
                            onValueChange = { viewModel.updateSearchQuery(it) },
                            placeholder = { Text("Search videos...", color = MaterialTheme.colorScheme.onSurfaceVariant) },
                            singleLine = true,
                            colors = TextFieldDefaults.colors(
                                focusedContainerColor = Color.Transparent,
                                unfocusedContainerColor = Color.Transparent,
                                focusedIndicatorColor = Color.Transparent,
                                unfocusedIndicatorColor = Color.Transparent
                            ),
                            modifier = Modifier.fillMaxWidth()
                        )
                    } else {
                        Text("PureStream", fontWeight = FontWeight.Bold)
                    }
                },
                actions = {
                    // Search Toggle
                    IconButton(onClick = {
                        isSearchActive = !isSearchActive
                        if (!isSearchActive) viewModel.updateSearchQuery("") // Clear on close
                    }) {
                        Icon(if (isSearchActive) Icons.Default.Close else Icons.Default.Search, contentDescription = "Search")
                    }
                    // Refresh Button (Hide when searching)
                    if (!isSearchActive) {
                        IconButton(onClick = { viewModel.refreshMedia() }) {
                            Icon(Icons.Default.Refresh, contentDescription = "Scan")
                        }
                    }
                }
            )
        },
        floatingActionButton = {
            // ONLY SHOW IF ON VIDEO TAB AND A VIDEO HAS BEEN PLAYED PREVIOUSLY
            if (currentScreen == "video" && lastPlayedUri != null) {
                FloatingActionButton(
                    onClick = { onVideoClick(lastPlayedUri!!) },
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = MaterialTheme.colorScheme.onPrimary
                ) {
                    Icon(Icons.Filled.PlayArrow, contentDescription = "Resume Playback")
                }
            }
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
        Box(modifier = Modifier.padding(paddingValues)) {
            when (currentScreen) {
                "video" -> VideoScreen(onVideoClick = onVideoClick)
                "browse" -> BrowseScreen(
                    onVideoClick = onVideoClick,
                    searchQuery = searchQuery // <-- Passing the state down
                )
                "audio" -> Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxSize()) {
                    Text("Audio Player Coming Soon")
                }
            }
        }
    }
}