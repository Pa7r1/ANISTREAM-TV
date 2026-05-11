package com.anistream.tv.data.model

data class Episode(
    val number: Int,
    val title: String?,
    val thumbnail: String?
) {
    val displayTitle: String get() = title?.takeIf { it.isNotBlank() } ?: "Episodio $number"
}
