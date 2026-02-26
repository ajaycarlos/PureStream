package com.purestream.app.ui.player.viewmodel

import android.content.Context
import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.purestream.app.data.local.dao.VideoDao
import com.purestream.app.data.local.dao.ProfanityDao
import com.purestream.app.data.parser.SrtParser
import com.purestream.app.domain.model.MuteSegment
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class PlayerViewModel @Inject constructor(
    private val videoDao: VideoDao,
    private val profanityDao: ProfanityDao
) : ViewModel() {

    private val _muteSegments = MutableStateFlow<List<MuteSegment>>(emptyList())
    val muteSegments: StateFlow<List<MuteSegment>> = _muteSegments.asStateFlow()

    fun loadSubtitlesFromUri(context: Context, uri: Uri) {
        viewModelScope.launch {
            try {
                val dbWords = profanityDao.getAllWords().first().map { it.word }
                val content = context.contentResolver.openInputStream(uri)?.use {
                    it.bufferedReader().readText()
                } ?: ""
                val detectedSegments = SrtParser.parseSrtToMuteSegments(content, dbWords)
                _muteSegments.value = detectedSegments
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    // 1. Fetch saved progress from Room
    suspend fun getVideoProgress(uri: String): Long {
        return videoDao.getVideoByUri(uri)?.progressMs ?: 0L
    }

    // 2. Save progress to Room and save URI to SharedPreferences
    fun updateVideoProgress(context: Context, uri: String, progress: Long) {
        CoroutineScope(Dispatchers.IO).launch {
            val video = videoDao.getVideoByUri(uri)
            if (video != null) {
                videoDao.insertVideo(video.copy(progressMs = progress))
            }

            // Save as the absolute last played video globally
            context.getSharedPreferences("purestream_prefs", Context.MODE_PRIVATE)
                .edit()
                .putString("last_played_uri", uri)
                .apply()
        }
    }
}