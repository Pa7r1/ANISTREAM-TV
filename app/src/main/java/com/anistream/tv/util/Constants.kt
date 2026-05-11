package com.anistream.tv.util

import com.anistream.tv.BuildConfig

object Constants {
    const val ANILIST_BASE_URL = "https://graphql.anilist.co/"
    val CONSUMET_BASE_URL: String = BuildConfig.CONSUMET_BASE_URL

    const val PREFS_NAME = "anistream_prefs"
    const val KEY_FAVORITES = "favorites"
    const val KEY_HISTORY = "watch_history"
    const val KEY_PROGRESS_PREFIX = "progress_"
    const val HISTORY_MAX_SIZE = 20

    const val IMG_WIDTH = 480
    const val IMG_HEIGHT = 270

    const val SEARCH_DEBOUNCE_MS = 500L
    const val ITEMS_PER_PAGE = 20
}
