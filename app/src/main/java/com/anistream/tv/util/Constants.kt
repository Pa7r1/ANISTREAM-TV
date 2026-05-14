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

    // Providers de Consumet a probar, en orden. Validado contra el server local
    // anistream/consumet:v1.5 (`@consumet/extensions` 1.8.8, post-DMCA mar/2026):
    //  - animesaturn: search+info+watch entregan m3u8 HLS reproducible (multi-resolución, subs ITA)
    //  - animeunity: search+info funcionan; watch a veces sin sources (fallback)
    //  - animekai: search+info OK, watch falla porque enc-dec.app rechaza el dec-mega
    //  - animepahe: DDoS-Guard tras dominio nuevo (animepahe.pw); deja como último intento
    val ANIME_PROVIDERS = listOf("animesaturn", "animeunity", "animekai", "animepahe")
}
