package com.purestream.app.ui.player

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.media3.common.MediaItem
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.ui.PlayerView
import com.purestream.app.ui.player.viewmodel.PlayerViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive

@Composable
fun VideoPlayerScreen(
    videoUri: Uri,
    playerViewModel: PlayerViewModel = hiltViewModel()
) {
    val context = LocalContext.current
    val muteSegments by playerViewModel.muteSegments.collectAsState()

    // 1. Build the ExoPlayer Engine
    val exoPlayer = remember {
        ExoPlayer.Builder(context).build().apply {
            val mediaItem = MediaItem.fromUri(videoUri)
            setMediaItem(mediaItem)
            prepare()
            playWhenReady = true
        }
    }

    // 2. Setup the Subtitle File Picker
    val srtPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument(),
        onResult = { uri ->
            uri?.let { playerViewModel.loadSubtitlesFromUri(context, it) }
        }
    )

    // 3. The Cleanup (Important for battery life)
    DisposableEffect(Unit) {
        onDispose {
            exoPlayer.release()
        }
    }

    // 4. THE PROFANITY FILTER LOOP
    // This runs 10 times every second to check if the audio should be muted
    LaunchedEffect(muteSegments) {
        while (isActive) {
            val currentPos = exoPlayer.currentPosition

            val shouldMute = muteSegments.any { segment ->
                currentPos in segment.startTimeMs..segment.endTimeMs
            }

            if (shouldMute && exoPlayer.volume > 0f) {
                exoPlayer.volume = 0f
            } else if (!shouldMute && exoPlayer.volume == 0f) {
                exoPlayer.volume = 1f
            }
            delay(100) // 100ms delay to keep the CPU cool
        }
    }

    // 5. The Final UI (VLC Style Layout)
    Box(modifier = Modifier.fillMaxSize()) {
        // The Video Content
        AndroidView(
            factory = { ctx ->
                PlayerView(ctx).apply {
                    player = exoPlayer
                    useController = true
                }
            },
            update = { view ->
                view.player = exoPlayer
            },
            modifier = Modifier.fillMaxSize()
        )

        // The "Load Subtitles" Button Overlay
        Button(
            onClick = {
                srtPickerLauncher.launch(arrayOf("application/x-subrip", "text/plain"))
            },
            modifier = Modifier
                .align(Alignment.TopEnd)
                .padding(16.dp)
        ) {
            Text("Load SRT")
        }
    }
}