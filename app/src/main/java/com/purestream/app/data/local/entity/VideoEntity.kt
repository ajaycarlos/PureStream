package com.purestream.app.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "videos")
data class VideoEntity(
    @PrimaryKey val fileUri: String,
    val title: String,
    val durationMs: Long,
    val progressMs: Long,
    val folderName: String // NEW: This will store "Downloads", "Camera", etc.
)