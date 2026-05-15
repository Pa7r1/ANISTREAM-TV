package com.anistream.tv.ui.detail

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.anistream.tv.data.api.AniListQueries
import com.anistream.tv.data.model.Anime
import com.anistream.tv.data.model.Episode
import com.anistream.tv.util.Constants
import com.anistream.tv.util.ServiceLocator
import com.anistream.tv.util.UiState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

data class DetailData(
    val anime: Anime,
    val episodes: List<Episode>
)

class DetailViewModel(private val animeId: Int) : ViewModel() {

    private val _state = MutableStateFlow<UiState<DetailData>>(UiState.Loading)
    val state: StateFlow<UiState<DetailData>> = _state

    private val aniList = ServiceLocator.aniListService
    private val consumet = ServiceLocator.consumetService

    init {
        loadDetail()
    }

    fun loadDetail() {
        viewModelScope.launch {
            _state.value = UiState.Loading
            try {
                val response = aniList.query(AniListQueries.mediaDetail(animeId))
                val anime = response.data?.media?.toAnime()
                    ?: throw Exception("Anime no encontrado")

                val episodes = (1..(anime.episodeCount ?: 1)).map { num ->
                    Episode(number = num, title = null, thumbnail = null)
                }

                // Render inicial con datos de AniList (inglés).
                _state.value = UiState.Success(DetailData(anime, episodes))

                // Lookup best-effort de sinopsis/géneros en español. Primer provider
                // hispano que matchee el título gana.
                val translated = fetchSpanishMeta(anime)
                if (translated != null) {
                    _state.value = UiState.Success(DetailData(translated, episodes))
                }
            } catch (e: Exception) {
                _state.value = UiState.Error(e.message ?: "Error desconocido")
            }
        }
    }

    /** Intenta enriquecer el Anime con descripción/géneros en español
     *  desde el primer provider que encuentre el anime. */
    private suspend fun fetchSpanishMeta(anime: Anime): Anime? {
        // Probar primero con título romaji, después con título inglés
        val queries = listOfNotNull(anime.titleRomaji.takeIf { it.isNotBlank() }, anime.titleEnglish)
        for (provider in Constants.ANIME_PROVIDERS) {
            for (q in queries) {
                try {
                    val results = consumet.searchAnime(provider, q).results.orEmpty()
                    val match = results.firstOrNull() ?: continue
                    val info = consumet.getAnimeInfo(provider, match.id)
                    val descEs = info.description?.trim().orEmpty()
                    val genresEs = info.genres.orEmpty()
                    if (descEs.isNotBlank() || genresEs.isNotEmpty()) {
                        Log.i(TAG, "sinopsis ES desde $provider (${match.id})")
                        return anime.copy(
                            description = descEs.takeIf { it.isNotBlank() } ?: anime.description,
                            genres = if (genresEs.isNotEmpty()) genresEs else anime.genres
                        )
                    }
                } catch (e: Exception) {
                    Log.w(TAG, "lookup ES en $provider con '$q' falló: ${e.message}")
                }
            }
        }
        return null
    }

    class Factory(private val animeId: Int) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T =
            DetailViewModel(animeId) as T
    }

    companion object {
        private const val TAG = "DetailVM"
    }
}
