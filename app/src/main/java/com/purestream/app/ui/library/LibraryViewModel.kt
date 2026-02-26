package com.purestream.app.ui.library

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.purestream.app.data.local.dao.VideoDao
import com.purestream.app.data.local.entity.VideoEntity
import com.purestream.app.data.repository.MediaStoreScanner // Add this import
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import javax.inject.Inject
import kotlinx.coroutines.flow.map

@HiltViewModel
class LibraryViewModel @Inject constructor(
    private val videoDao: VideoDao,
    private val mediaStoreScanner: MediaStoreScanner // Add this line!
) : ViewModel() {

    val savedVideos = videoDao.getAllVideos()

    // 1. Update the manual add function
    fun addVideoToLibrary(uriString: String, title: String) {
        viewModelScope.launch {
            val newVideo = VideoEntity(
                fileUri = uriString,
                title = title,
                durationMs = 0L,
                progressMs = 0L,
                folderName = "Added Manually" // Provide the required parameter
            )
            videoDao.insertVideo(newVideo)
        }
    }

    // 2. The Grouping logic is now beautiful and simple!
    val groupedVideos = savedVideos.map { list ->
        list.groupBy { video ->
            video.folderName // Just group by the actual exact folder name!
        }
    }

    fun refreshMedia() {
        viewModelScope.launch(Dispatchers.IO) {
            // Now the ViewModel knows what mediaStoreScanner is!
            val foundVideos = mediaStoreScanner.scanDeviceForVideos()
            foundVideos.forEach { video ->
                videoDao.insertVideo(video)
            }
        }
    }
}