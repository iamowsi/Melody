package com.mustafa.musicplayer.data

import android.content.Context
import org.json.JSONArray
import org.json.JSONObject

enum class ThemeMode {
    SYSTEM,
    LIGHT,
    DARK
}

data class MusicFolder(
    val id: String,
    val name: String,
    val songIds: Set<Long> = emptySet()
)

object MelodyPreferences {
    private const val PREFS_NAME = "melody_prefs"
    private const val KEY_FAVORITES = "favorite_song_ids"
    private const val KEY_THEME_MODE = "theme_mode"
    private const val KEY_FOLDERS = "custom_folders"

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

    fun loadFolders(context: Context): List<MusicFolder> {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val raw = prefs.getString(KEY_FOLDERS, null) ?: return emptyList()
        return runCatching {
            val array = JSONArray(raw)
            buildList {
                for (i in 0 until array.length()) {
                    val obj = array.getJSONObject(i)
                    val id = obj.optString("id")
                    val name = obj.optString("name")
                    if (id.isNotBlank() && name.isNotBlank()) {
                        val idsArray = obj.optJSONArray("songIds")
                        val songIds = mutableSetOf<Long>()
                        if (idsArray != null) {
                            for (j in 0 until idsArray.length()) {
                                songIds.add(idsArray.optLong(j))
                            }
                        }
                        add(MusicFolder(id = id, name = name, songIds = songIds))
                    }
                }
            }
        }.getOrDefault(emptyList())
    }

    fun saveFolders(context: Context, folders: List<MusicFolder>) {
        val array = JSONArray()
        folders.forEach { folder ->
            val idsArray = JSONArray()
            folder.songIds.forEach { idsArray.put(it) }
            val obj = JSONObject().apply {
                put("id", folder.id)
                put("name", folder.name)
                put("songIds", idsArray)
            }
            array.put(obj)
        }
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        prefs.edit().putString(KEY_FOLDERS, array.toString()).apply()
    }
}
