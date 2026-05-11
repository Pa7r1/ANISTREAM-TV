package com.anistream.tv.data.api

import com.anistream.tv.data.model.StreamSource
import com.google.gson.annotations.SerializedName
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query

interface ConsumetService {

    /** Busca un anime por nombre, retorna lista de resultados */
    @GET("anime/gogoanime/{query}")
    suspend fun searchAnime(
        @Path("query") query: String,
        @Query("page") page: Int = 1
    ): ConsumetSearchResponse

    /** Info de un anime con lista completa de episodios */
    @GET("anime/gogoanime/info/{id}")
    suspend fun getAnimeInfo(
        @Path("id") animeId: String
    ): ConsumetAnimeInfo

    /** Fuentes de streaming para un episodio */
    @GET("anime/gogoanime/watch/{episodeId}")
    suspend fun getStreamSources(
        @Path("episodeId") episodeId: String,
        @Query("server") server: String = "gogocdn"
    ): ConsumetStreamResponse
}

// ── Search ────────────────────────────────────────────────────────────────────

data class ConsumetSearchResponse(
    @SerializedName("currentPage") val currentPage: Int?,
    @SerializedName("hasNextPage") val hasNextPage: Boolean?,
    @SerializedName("results") val results: List<ConsumetSearchResult>?
)

data class ConsumetSearchResult(
    @SerializedName("id") val id: String,
    @SerializedName("title") val title: String?,
    @SerializedName("url") val url: String?,
    @SerializedName("image") val image: String?,
    @SerializedName("releaseDate") val releaseDate: String?,
    @SerializedName("subOrDub") val subOrDub: String?
)

// ── Anime Info + Episodes ─────────────────────────────────────────────────────

data class ConsumetAnimeInfo(
    @SerializedName("id") val id: String?,
    @SerializedName("title") val title: String?,
    @SerializedName("episodes") val episodes: List<ConsumetEpisode>?
)

data class ConsumetEpisode(
    @SerializedName("id") val id: String,
    @SerializedName("number") val number: Int?,
    @SerializedName("url") val url: String?
)

// ── Stream Sources ─────────────────────────────────────────────────────────────

data class ConsumetStreamResponse(
    @SerializedName("sources") val sources: List<ConsumetSource>?,
    @SerializedName("headers") val headers: Map<String, String>?
) {
    fun toStreamSources(): List<StreamSource> =
        sources?.mapNotNull { src ->
            if (src.url.isNotBlank())
                StreamSource(url = src.url, quality = src.quality ?: "default", isM3U8 = src.isM3U8 ?: false)
            else null
        } ?: emptyList()
}

data class ConsumetSource(
    @SerializedName("url") val url: String,
    @SerializedName("quality") val quality: String?,
    @SerializedName("isM3U8") val isM3U8: Boolean?
)
