package com.purestream.app.domain.usecase

import com.purestream.app.domain.model.MuteSegment
import com.purestream.app.domain.model.SubtitleLine

class AnalyzeSubtitlesUseCase {

    /**
     * Scans subtitle lines against a blacklist of words.
     * Returns a list of timestamps where the audio must be muted.
     */
    fun execute(subtitles: List<SubtitleLine>, badWords: List<String>): List<MuteSegment> {
        val muteSegments = mutableListOf<MuteSegment>()

        for (line in subtitles) {
            // Convert to lowercase to make matching easier (e.g., "BadWord" == "badword")
            val textToAnalyze = line.text.lowercase()

            for (badWord in badWords) {
                if (textToAnalyze.contains(badWord.lowercase())) {

                    // We found a bad word! Add this timestamp to our mute list.
                    // Pro-Tip: We add a small 100ms buffer so we don't accidentally
                    // hear the very first consonant of the word before the mute kicks in.
                    muteSegments.add(
                        MuteSegment(
                            startTimeMs = maxOf(0L, line.startTimeMs - 100L),
                            endTimeMs = line.endTimeMs + 100L
                        )
                    )

                    // We already know this line is bad, no need to check other words for this specific line.
                    break
                }
            }
        }

        // Optional: In a highly optimized app, if two mute segments overlap
        // (e.g., 1:00-1:05 and 1:04-1:10), we would merge them here.
        // For MVP, this basic list is perfect.

        return muteSegments
    }
}