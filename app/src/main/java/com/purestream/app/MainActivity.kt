package com.purestream.app

import android.Manifest
import android.net.Uri
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Modifier
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.purestream.app.ui.player.VideoPlayerScreen
import com.purestream.app.ui.settings.BlacklistScreen
import dagger.hilt.android.AndroidEntryPoint
import java.net.URLEncoder
import java.nio.charset.StandardCharsets
import com.purestream.app.ui.theme.PureStreamTheme

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            PureStreamTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    val navController = rememberNavController()

                    // --- PERMISSION REQUEST BLOCK ---
                    val permissionsToRequest = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                        arrayOf(Manifest.permission.READ_MEDIA_VIDEO)
                    } else {
                        arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE)
                    }

                    val launcher = rememberLauncherForActivityResult(
                        ActivityResultContracts.RequestMultiplePermissions()
                    ) { permissions ->
                        val isGranted = permissions.values.all { it }
                        if (isGranted) {
                            // Permissions granted! The scanner can now run.
                        }
                    }

                    LaunchedEffect(Unit) {
                        launcher.launch(permissionsToRequest)
                    }
                    // --------------------------------

                    NavHost(navController = navController, startDestination = "library") {
                        // --- SCREEN 1: THE HOME SHELL ---
                        composable("library") {
                            // We now call HomeShell instead of LibraryScreen
                            com.purestream.app.ui.home.HomeShell(
                                onVideoClick = { fileUri ->
                                    val encodedUri = URLEncoder.encode(fileUri, StandardCharsets.UTF_8.toString())
                                    navController.navigate("player/$encodedUri")
                                },
                                onSettingsClick = { navController.navigate("settings") }
                            )
                        }

                        composable("player/{encodedUri}") { backStackEntry ->
                            val autoDecodedUri = backStackEntry.arguments?.getString("encodedUri") ?: ""
                            VideoPlayerScreen(videoUri = Uri.parse(autoDecodedUri))
                        }

                        composable("settings") {
                            BlacklistScreen()
                        }
                    }
                }
            }
        }
    }
}