package com.purestream.app.ui.player.viewmodel

import android.content.Context
import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.purestream.app.data.local.dao.VideoDao
import com.purestream.app.data.local.dao.ProfanityDao // Added
import com.purestream.app.data.local.entity.VideoEntity
import com.purestream.app.data.parser.SrtParser
import com.purestream.app.domain.model.MuteSegment
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first // Added
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class PlayerViewModel @Inject constructor(
    private val videoDao: VideoDao,
    private val profanityDao: ProfanityDao // Injected the new DAO
) : ViewModel() {

    private val _muteSegments = MutableStateFlow<List<MuteSegment>>(emptyList())
    val muteSegments: StateFlow<List<MuteSegment>> = _muteSegments.asStateFlow()

    fun loadSubtitlesFromUri(context: Context, uri: Uri) {
        viewModelScope.launch {
            try {
                // 1. Fetch current blacklist from Database
                val dbWords = profanityDao.getAllWords().first().map { it.word }

                // 2. Read SRT file
                val content = context.contentResolver.openInputStream(uri)?.use {
                    it.bufferedReader().readText()
                } ?: ""

                // 3. Parse with dynamic list
                val detectedSegments = SrtParser.parseSrtToMuteSegments(content, dbWords)
                _muteSegments.value = detectedSegments
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    // Update the save function here too
    fun saveVideoToLibrary(uri: String, title: String) {
        viewModelScope.launch {
            val newVideo = VideoEntity(
                title = title,
                fileUri = uri,
                durationMs = 0L,
                progressMs = 0L,
                folderName = "Recent" // Provide the required parameter
            )
            videoDao.insertVideo(newVideo)
        }
    }
}