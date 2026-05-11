package com.anistream.tv.data.model

data class Anime(
    val id: Int,
    val titleRomaji: String,
    val titleEnglish: String?,
    val coverImageLarge: String?,
    val bannerImage: String?,
    val description: String?,
    val genres: List<String>,
    val episodeCount: Int?,
    val averageScore: Int?,
    val status: String?
) {
    val displayTitle: String get() = titleEnglish?.takeIf { it.isNotBlank() } ?: titleRomaji
}
