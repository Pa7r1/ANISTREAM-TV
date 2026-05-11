package com.anistream.tv.data.api

import com.anistream.tv.data.model.Anime
import com.google.gson.JsonObject
import com.google.gson.annotations.SerializedName
import retrofit2.http.Body
import retrofit2.http.POST

interface AniListService {

    @POST(".")
    suspend fun query(@Body body: Map<String, String>): AniListResponse
}

// ── Response wrappers ──────────────────────────────────────────────────────────

data class AniListResponse(
    @SerializedName("data") val data: AniListData?
)

data class AniListData(
    @SerializedName("Page") val page: AniListPage?,
    @SerializedName("Media") val media: AniListMedia?
)

data class AniListPage(
    @SerializedName("media") val media: List<AniListMedia>?
)

data class AniListMedia(
    @SerializedName("id") val id: Int?,
    @SerializedName("title") val title: AniListTitle?,
    @SerializedName("coverImage") val coverImage: AniListCoverImage?,
    @SerializedName("bannerImage") val bannerImage: String?,
    @SerializedName("description") val description: String?,
    @SerializedName("genres") val genres: List<String>?,
    @SerializedName("episodes") val episodes: Int?,
    @SerializedName("averageScore") val averageScore: Int?,
    @SerializedName("status") val status: String?
) {
    fun toAnime(): Anime? {
        val animeId = id ?: return null
        val romaji = title?.romaji ?: return null
        return Anime(
            id = animeId,
            titleRomaji = romaji,
            titleEnglish = title.english,
            coverImageLarge = coverImage?.large,
            bannerImage = bannerImage,
            description = description?.replace(Regex("<[^>]+>"), ""),
            genres = genres ?: emptyList(),
            episodeCount = episodes,
            averageScore = averageScore,
            status = status
        )
    }
}

data class AniListTitle(
    @SerializedName("romaji") val romaji: String?,
    @SerializedName("english") val english: String?
)

data class AniListCoverImage(
    @SerializedName("large") val large: String?,
    @SerializedName("medium") val medium: String?
)

// ── Query builders ─────────────────────────────────────────────────────────────

object AniListQueries {

    fun trending(perPage: Int = 20): Map<String, String> = mapOf(
        "query" to """
            {
              Page(perPage: $perPage) {
                media(sort: TRENDING_DESC, type: ANIME, isAdult: false) {
                  id
                  title { romaji english }
                  coverImage { large }
                  genres
                  episodes
                  averageScore
                  status
                }
              }
            }
        """.trimIndent()
    )

    fun recentlyUpdated(perPage: Int = 20): Map<String, String> = mapOf(
        "query" to """
            {
              Page(perPage: $perPage) {
                media(sort: UPDATED_AT_DESC, type: ANIME, status: RELEASING, isAdult: false) {
                  id
                  title { romaji english }
                  coverImage { large }
                  genres
                  episodes
                  averageScore
                  status
                }
              }
            }
        """.trimIndent()
    )

    fun mediaDetail(animeId: Int): Map<String, String> = mapOf(
        "query" to """
            {
              Media(id: $animeId, type: ANIME) {
                id
                title { romaji english }
                coverImage { large }
                bannerImage
                description
                genres
                episodes
                averageScore
                status
              }
            }
        """.trimIndent()
    )

    fun search(query: String, perPage: Int = 20): Map<String, String> = mapOf(
        "query" to """
            {
              Page(perPage: $perPage) {
                media(search: "$query", type: ANIME, isAdult: false) {
                  id
                  title { romaji english }
                  coverImage { large }
                  genres
                  episodes
                  averageScore
                  status
                }
              }
            }
        """.trimIndent()
    )

    fun byIds(ids: List<Int>): Map<String, String> = mapOf(
        "query" to """
            {
              Page(perPage: ${ids.size.coerceAtLeast(1)}) {
                media(id_in: [${ids.joinToString(",")}], type: ANIME) {
                  id
                  title { romaji english }
                  coverImage { large }
                  genres
                  episodes
                  averageScore
                  status
                }
              }
            }
        """.trimIndent()
    )
}
