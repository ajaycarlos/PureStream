package com.purestream.app.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.purestream.app.data.local.entity.VideoEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface VideoDao {

    // Insert a new video. If it already exists, replace it to update progress.
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertVideo(video: VideoEntity)

    // Get all videos to show on our Poster Library screen.
    // Notice it returns a Flow? This means if a new video is added,
    // the UI will update instantly and automatically!
    @Query("SELECT * FROM videos")
    fun getAllVideos(): Flow<List<VideoEntity>>

    // Get a single video by its URI to check if we already have progress saved
    @Query("SELECT * FROM videos WHERE fileUri = :uri LIMIT 1")
    suspend fun getVideoByUri(uri: String): VideoEntity?
}