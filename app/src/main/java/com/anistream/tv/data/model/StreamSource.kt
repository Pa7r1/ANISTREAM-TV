package com.anistream.tv.data.model

data class StreamSource(
    val url: String,
    val quality: String,
    val isM3U8: Boolean,
    /** "SUB" | "LAT" | "ESP" | null */
    val version: String? = null,
    /** Nombre del embed origen (YourUpload, Streamtape, etc.) */
    val server: String? = null
)
