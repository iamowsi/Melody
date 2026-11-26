package com.mustafa.musicplayer.data

import android.content.Context

data class LyricLine(
    val timeMs: Long?,
    val text: String
)

data class ParsedLyrics(
    val isSynced: Boolean,
    val lines: List<LyricLine>
)

object LyricsManager {
    private const val PREFS_NAME = "melody_lyrics_prefs"
    private const val KEY_PREFIX = "lyrics_"

    private val lrcTimeRegex = Regex("""\[(\d{1,2}):(\d{2})(?:[.:](\d{1,3}))?]""")
    private val lrcMetaRegex = Regex("""^\[[a-zA-Z]+:.*]$""")

    fun loadRawLyrics(context: Context, songId: Long): String {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        return prefs.getString("$KEY_PREFIX$songId", "") ?: ""
    }

    fun saveRawLyrics(context: Context, songId: Long, rawText: String) {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val trimmed = rawText.trim()
        if (trimmed.isEmpty()) {
            prefs.edit().remove("$KEY_PREFIX$songId").apply()
        } else {
            prefs.edit().putString("$KEY_PREFIX$songId", trimmed).apply()
        }
    }

    fun parse(rawText: String): ParsedLyrics {
        val clean = rawText.trim()
        if (clean.isEmpty()) {
            return ParsedLyrics(isSynced = false, lines = emptyList())
        }

        val syncedLines = mutableListOf<LyricLine>()
        val rawLines = clean.lines()

        for (line in rawLines) {
            val trimmed = line.trim()
            if (trimmed.isEmpty() || lrcMetaRegex.matches(trimmed)) continue

            val matches = lrcTimeRegex.findAll(trimmed).toList()
            if (matches.isNotEmpty()) {
                val lyricText = trimmed.replace(lrcTimeRegex, "").trim()
                if (lyricText.isNotEmpty()) {
                    for (match in matches) {
                        val minutes = match.groupValues[1].toLongOrNull() ?: 0L
                        val seconds = match.groupValues[2].toLongOrNull() ?: 0L
                        val fracStr = match.groupValues[3]
                        val millis = when (fracStr.length) {
                            1 -> (fracStr.toLongOrNull() ?: 0L) * 100L
                            2 -> (fracStr.toLongOrNull() ?: 0L) * 10L
                            3 -> fracStr.toLongOrNull() ?: 0L
                            else -> 0L
                        }
                        val totalMs = (minutes * 60_000L) + (seconds * 1_000L) + millis
                        syncedLines.add(LyricLine(timeMs = totalMs, text = lyricText))
                    }
                }
            }
        }

        if (syncedLines.isNotEmpty()) {
            return ParsedLyrics(
                isSynced = true,
                lines = syncedLines.sortedBy { it.timeMs ?: 0L }
            )
        }

        val plainLines = rawLines.map { LyricLine(timeMs = null, text = it.trim()) }
        return ParsedLyrics(isSynced = false, lines = plainLines)
    }

    fun findActiveLineIndex(lines: List<LyricLine>, positionMs: Long): Int {
        if (lines.isEmpty()) return -1
        var active = -1
        for (i in lines.indices) {
            val lineTime = lines[i].timeMs ?: continue
            if (lineTime <= positionMs) {
                active = i
            } else {
                break
            }
        }
        return active
    }
}
