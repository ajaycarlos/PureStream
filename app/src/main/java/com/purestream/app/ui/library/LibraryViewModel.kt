package com.purestream.app.ui.library

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.purestream.app.data.local.dao.VideoDao
import com.purestream.app.data.repository.MediaStoreScanner
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class LibraryViewModel @Inject constructor(
    private val videoDao: VideoDao,
    private val mediaStoreScanner: MediaStoreScanner,
    @ApplicationContext private val context: Context // Injected to access SharedPreferences
) : ViewModel() {

    // --- Search State ---
    private val _searchQuery = MutableStateFlow("")
    val searchQuery = _searchQuery.asStateFlow()

    fun updateSearchQuery(query: String) {
        _searchQuery.value = query
    }

    // --- Last Played State ---
    private val _lastPlayedUri = MutableStateFlow<String?>(null)
    val lastPlayedUri = _lastPlayedUri.asStateFlow()

    init {
        refreshLastPlayed()
    }

    fun refreshLastPlayed() {
        val prefs = context.getSharedPreferences("purestream_prefs", Context.MODE_PRIVATE)
        _lastPlayedUri.value = prefs.getString("last_played_uri", null)
    }

    // --- Video Data ---
    val savedVideos = videoDao.getAllVideos()

    // Combining raw Room videos with the Search Query to generate the Hybrid Grid
    val groupedVideos = combine(savedVideos, searchQuery) { videos, query ->
        val filtered = if (query.isBlank()) {
            videos
        } else {
            videos.filter { it.title.contains(query, ignoreCase = true) }
        }
        filtered.groupBy { it.folderName }
    }

    fun refreshMedia() {
        viewModelScope.launch(Dispatchers.IO) {
            val foundVideos = mediaStoreScanner.scanDeviceForVideos()
            foundVideos.forEach { video ->
                // Ensure we don't overwrite existing progress when rescanning!
                val existing = videoDao.getVideoByUri(video.fileUri)
                if (existing != null) {
                    videoDao.insertVideo(video.copy(progressMs = existing.progressMs))
                } else {
                    videoDao.insertVideo(video)
                }
            }
        }
    }
}