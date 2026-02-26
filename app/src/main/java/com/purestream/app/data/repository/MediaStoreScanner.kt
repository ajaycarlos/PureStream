package com.purestream.app.data.repository

import android.content.ContentUris
import android.content.Context
import android.provider.MediaStore
import com.purestream.app.data.local.entity.VideoEntity
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject

class MediaStoreScanner @Inject constructor(
    @ApplicationContext private val context: Context
) {
    fun scanDeviceForVideos(): List<VideoEntity> {
        val videoList = mutableListOf<VideoEntity>()

        val projection = arrayOf(
            MediaStore.Video.Media._ID,
            MediaStore.Video.Media.DISPLAY_NAME,
            MediaStore.Video.Media.DURATION,
            MediaStore.Video.Media.BUCKET_DISPLAY_NAME // VLC uses this for folders!
        )

        val cursor = context.contentResolver.query(
            MediaStore.Video.Media.EXTERNAL_CONTENT_URI,
            projection,
            null,
            null,
            "${MediaStore.Video.Media.DATE_ADDED} DESC"
        )

        cursor?.use {
            val idCol = it.getColumnIndexOrThrow(MediaStore.Video.Media._ID)
            val nameCol = it.getColumnIndexOrThrow(MediaStore.Video.Media.DISPLAY_NAME)
            val durCol = it.getColumnIndexOrThrow(MediaStore.Video.Media.DURATION)
            val folderCol = it.getColumnIndexOrThrow(MediaStore.Video.Media.BUCKET_DISPLAY_NAME)

            while (it.moveToNext()) {
                val id = it.getLong(idCol)
                val uri = ContentUris.withAppendedId(MediaStore.Video.Media.EXTERNAL_CONTENT_URI, id).toString()
                // Safely get the folder name, default to "Internal" if null
                val folderName = it.getString(folderCol) ?: "Internal Storage"

                videoList.add(VideoEntity(
                    title = it.getString(nameCol) ?: "Unknown Video",
                    fileUri = uri,
                    durationMs = it.getLong(durCol),
                    progressMs = 0L,
                    folderName = folderName // Save it to the entity!
                ))
            }
        }
        return videoList
    }
}