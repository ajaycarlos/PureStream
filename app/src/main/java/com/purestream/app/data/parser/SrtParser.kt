package com.purestream.app.data.parser

import com.purestream.app.domain.model.MuteSegment
import java.util.regex.Pattern

object SrtParser {

    fun parseSrtToMuteSegments(srtContent: String, profanityList: List<String>): List<MuteSegment> {
        val segments = mutableListOf<MuteSegment>()
        val pattern = Pattern.compile("(\\d+)\\n(\\d{2}:\\d{2}:\\d{2},\\d{3}) --> (\\d{2}:\\d{2}:\\d{2},\\d{3})\\n([\\s\\S]*?)(?=\\n\\n|\\z)")
        val matcher = pattern.matcher(srtContent)

        val START_PADDING = 150L
        val END_PADDING = 100L

        while (matcher.find()) {
            val startMs = parseTimestampToMs(matcher.group(2)!!)
            val endMs = parseTimestampToMs(matcher.group(3)!!)
            val fullText = matcher.group(4) ?: ""

            val cleanText = fullText.replace("\n", " ").trim()
            val words = cleanText.split(Regex("\\s+"))

            val blockDuration = endMs - startMs
            val msPerWord = if (words.isNotEmpty()) blockDuration / words.size else 0L

            words.forEachIndexed { index, word ->
                val lowerWord = word.lowercase().filter { it.isLetter() }

                if (profanityList.contains(lowerWord)) {
                    var wordStart = startMs + (index * msPerWord) - START_PADDING
                    var wordEnd = wordStart + msPerWord + END_PADDING
                    wordStart = maxOf(0L, wordStart)

                    segments.add(MuteSegment(wordStart, wordEnd))
                }
            }
        }
        return segments
    }

    private fun parseTimestampToMs(timestamp: String): Long {
        val parts = timestamp.replace(",", ":").split(":")
        val hours = parts[0].toLong()
        val minutes = parts[1].toLong()
        val seconds = parts[2].toLong()
        val millis = parts[3].toLong()
        return (hours * 3600 + minutes * 60 + seconds) * 1000 + millis
    }
}