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
            playWhenReady = false // DO NOT PLAY YET! We need to seek first.
        }
    }

    // 2. Fetch Progress and Seek
    LaunchedEffect(videoUri) {
        val savedProgress = playerViewModel.getVideoProgress(videoUri.toString())
        if (savedProgress > 0L) {
            exoPlayer.seekTo(savedProgress)
        }
        exoPlayer.playWhenReady = true // Start playing from the saved timestamp
    }

    // 3. Setup the Subtitle File Picker
    val srtPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument(),
        onResult = { uri ->
            uri?.let { playerViewModel.loadSubtitlesFromUri(context, it) }
        }
    )

    // 4. THE PROFANITY FILTER & PROGRESS SAVER LOOP
    LaunchedEffect(muteSegments) {
        var loopCount = 0
        while (isActive) {
            val currentPos = exoPlayer.currentPosition

            // --- Profanity Mute Logic ---
            val shouldMute = muteSegments.any { segment ->
                currentPos in segment.startTimeMs..segment.endTimeMs
            }

            if (shouldMute && exoPlayer.volume > 0f) {
                exoPlayer.volume = 0f
            } else if (!shouldMute && exoPlayer.volume == 0f) {
                exoPlayer.volume = 1f
            }

            // --- Progress Save Logic ---
            // Save Progress every 5 seconds (50 loops of 100ms) to reduce DB writes
            loopCount++
            if (loopCount >= 50 && exoPlayer.isPlaying) {
                playerViewModel.updateVideoProgress(context, videoUri.toString(), currentPos)
                loopCount = 0
            }

            delay(100)
        }
    }

    // 5. The Cleanup
    DisposableEffect(Unit) {
        onDispose {
            // Save one absolute last time before the screen is destroyed
            playerViewModel.updateVideoProgress(context, videoUri.toString(), exoPlayer.currentPosition)
            exoPlayer.release()
        }
    }

    // 6. The Final UI
    Box(modifier = Modifier.fillMaxSize()) {
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