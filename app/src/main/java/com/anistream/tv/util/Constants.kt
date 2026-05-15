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

    // Providers de Consumet a probar, en orden. v1.6: localización estricta al español.
    // Server `anistream/consumet:v1.6` (Docker local, requiere flaresolverr en :8191):
    //  - animeflv (www3.animeflv.net): SUB + LAT, sinopsis y géneros en español
    //  - tioanime (tioanime.com): SUB español, sinopsis y géneros en español
    // Catálogo italiano (animesaturn/animeunity) eliminado — pedido del usuario.
    val ANIME_PROVIDERS = listOf("animeflv", "tioanime")

    // Preferencia de audio/idioma. "AUTO" = orden natural del provider.
    const val KEY_LANG_PREFERENCE = "lang_preference"
    const val DEFAULT_LANG_PREFERENCE = "LAT"
    val LANG_OPTIONS = listOf("LAT", "SUB", "ESP", "AUTO")
    val LANG_LABELS = mapOf(
        "LAT" to "Dub Latino",
        "SUB" to "Sub Español",
        "ESP" to "Dub Español",
        "AUTO" to "Automático"
    )
}
