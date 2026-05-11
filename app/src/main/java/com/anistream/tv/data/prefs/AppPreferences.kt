package com.anistream.tv.data.prefs

import android.content.Context
import android.content.SharedPreferences
import com.anistream.tv.util.Constants
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

class AppPreferences(context: Context) {

    private val prefs: SharedPreferences =
        context.getSharedPreferences(Constants.PREFS_NAME, Context.MODE_PRIVATE)
    private val gson = Gson()

    // ── Favorites ──────────────────────────────────────────────────────────────

    fun getFavoriteIds(): Set<Int> {
        val json = prefs.getString(Constants.KEY_FAVORITES, null) ?: return emptySet()
        val type = object : TypeToken<Set<Int>>() {}.type
        return gson.fromJson(json, type)
    }

    fun toggleFavorite(animeId: Int): Boolean {
        val favorites = getFavoriteIds().toMutableSet()
        val added = favorites.add(animeId)
        if (!added) favorites.remove(animeId)
        prefs.edit().putString(Constants.KEY_FAVORITES, gson.toJson(favorites)).apply()
        return added
    }

    fun isFavorite(animeId: Int): Boolean = animeId in getFavoriteIds()

    // ── Watch History (FIFO, max 20) ───────────────────────────────────────────

    fun getHistory(): List<Int> {
        val json = prefs.getString(Constants.KEY_HISTORY, null) ?: return emptyList()
        val type = object : TypeToken<List<Int>>() {}.type
        return gson.fromJson(json, type)
    }

    fun addToHistory(animeId: Int) {
        val history = getHistory().toMutableList()
        history.remove(animeId)
        history.add(0, animeId)
        if (history.size > Constants.HISTORY_MAX_SIZE) history.removeLast()
        prefs.edit().putString(Constants.KEY_HISTORY, gson.toJson(history)).apply()
    }

    // ── Watch Progress ─────────────────────────────────────────────────────────

    fun saveProgress(animeId: Int, episode: Int, positionMs: Long) {
        val key = "${Constants.KEY_PROGRESS_PREFIX}${animeId}_$episode"
        prefs.edit().putLong(key, positionMs).apply()
    }

    fun getProgress(animeId: Int, episode: Int): Long {
        val key = "${Constants.KEY_PROGRESS_PREFIX}${animeId}_$episode"
        return prefs.getLong(key, 0L)
    }
}
