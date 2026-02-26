package com.purestream.app.domain.model

/**
 * Represents a single block of text from a subtitle file.
 * We store time in Milliseconds (Long) because ExoPlayer uses milliseconds!
 */
data class SubtitleLine(
    val index: Int,
    val startTimeMs: Long,
    val endTimeMs: Long,
    val text: String
)