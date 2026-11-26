package com.mustafa.musicplayer.data

import android.content.Context

enum class ThemeMode {
    SYSTEM,
    LIGHT,
    DARK
}

object MelodyPreferences {
    private const val PREFS_NAME = "melody_prefs"
    private const val KEY_FAVORITES = "favorite_song_ids"
    private const val KEY_THEME_MODE = "theme_mode"

    fun loadFavorites(context: Context): Set<Long> {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val rawSet = prefs.getStringSet(KEY_FAVORITES, emptySet()) ?: emptySet()
        return rawSet.mapNotNull { it.toLongOrNull() }.toSet()
    }

    fun saveFavorites(context: Context, favoriteIds: Set<Long>) {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val stringSet = favoriteIds.map { it.toString() }.toSet()
        prefs.edit().putStringSet(KEY_FAVORITES, stringSet).apply()
    }

    fun loadThemeMode(context: Context): ThemeMode {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val savedName = prefs.getString(KEY_THEME_MODE, ThemeMode.SYSTEM.name)
        return runCatching { ThemeMode.valueOf(savedName ?: ThemeMode.SYSTEM.name) }
            .getOrDefault(ThemeMode.SYSTEM)
    }

    fun saveThemeMode(context: Context, mode: ThemeMode) {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        prefs.edit().putString(KEY_THEME_MODE, mode.name).apply()
    }
}
