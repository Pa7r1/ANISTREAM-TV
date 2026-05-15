package com.anistream.tv.data.api

import com.anistream.tv.data.model.StreamSource
import com.google.gson.annotations.SerializedName
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query

interface ConsumetService {

    @GET("anime/{provider}/{query}")
    suspend fun searchAnime(
        @Path("provider") provider: String,
        @Path("query") query: String,
        @Query("page") page: Int = 1
    ): ConsumetSearchResponse

    @GET("anime/{provider}/info/{id}")
    suspend fun getAnimeInfo(
        @Path("provider") provider: String,
        @Path("id") animeId: String
    ): ConsumetAnimeInfo

    @GET("anime/{provider}/watch/{episodeId}")
    suspend fun getStreamSources(
        @Path("provider") provider: String,
        @Path("episodeId") episodeId: String,
        @Query("server") server: String? = null
    ): ConsumetStreamResponse

    @GET("anime/{provider}/servers/{episodeId}")
    suspend fun getServers(
        @Path("provider") provider: String,
        @Path("episodeId") episodeId: String
    ): List<ConsumetServerEntry>
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
    @SerializedName("description") val description: String?,
    @SerializedName("genres") val genres: List<String>?,
    @SerializedName("episodes") val episodes: List<ConsumetEpisode>?
)

data class ConsumetEpisode(
    @SerializedName("id") val id: String,
    @SerializedName("number") val number: Int?,
    @SerializedName("url") val url: String?
)

// ── Servers list per episode ──────────────────────────────────────────────────

data class ConsumetServerEntry(
    @SerializedName("name") val name: String?,
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
                StreamSource(
                    url = src.url,
                    quality = src.quality ?: "default",
                    isM3U8 = src.isM3U8 ?: src.url.endsWith(".m3u8"),
                    version = src.version,
                    server = src.server
                )
            else null
        } ?: emptyList()
}

data class ConsumetSource(
    @SerializedName("url") val url: String,
    @SerializedName("quality") val quality: String?,
    @SerializedName("isM3U8") val isM3U8: Boolean?,
    @SerializedName("version") val version: String?,
    @SerializedName("server") val server: String?
)
