package com.anistream.tv.ui.detail

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.anistream.tv.data.api.AniListQueries
import com.anistream.tv.data.model.Anime
import com.anistream.tv.data.model.Episode
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

                // Build episode list from count (streams loaded lazily in Phase 2)
                val episodes = (1..(anime.episodeCount ?: 1)).map { num ->
                    Episode(number = num, title = null, thumbnail = null)
                }

                _state.value = UiState.Success(DetailData(anime, episodes))
            } catch (e: Exception) {
                _state.value = UiState.Error(e.message ?: "Error desconocido")
            }
        }
    }

    class Factory(private val animeId: Int) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T =
            DetailViewModel(animeId) as T
    }
}
