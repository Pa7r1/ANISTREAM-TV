package com.anistream.tv.ui.player

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.anistream.tv.data.model.StreamSource
import com.anistream.tv.util.ServiceLocator
import com.anistream.tv.util.UiState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class PlayerViewModel(
    private val animeTitle: String,
    private val episodeNumber: Int,
    private val animeId: Int
) : ViewModel() {

    private val _state = MutableStateFlow<UiState<List<StreamSource>>>(UiState.Loading)
    val state: StateFlow<UiState<List<StreamSource>>> = _state

    private val consumet = ServiceLocator.consumetService
    private val prefs = ServiceLocator.preferences

    val savedPositionMs: Long get() = prefs.getProgress(animeId, episodeNumber)

    fun saveProgress(positionMs: Long) {
        prefs.saveProgress(animeId, episodeNumber, positionMs)
    }

    fun loadSources() {
        viewModelScope.launch {
            _state.value = UiState.Loading
            try {
                val sources = fetchSources()
                if (sources.isEmpty()) throw Exception("No hay fuentes disponibles para este episodio")
                _state.value = UiState.Success(sources)
            } catch (e: Exception) {
                _state.value = UiState.Error(e.message ?: "Error al cargar el video")
            }
        }
    }

    private suspend fun fetchSources(): List<StreamSource> {
        // 1. Buscar el anime en Consumet por título
        val searchResults = consumet.searchAnime(animeTitle).results
        val animeResult = searchResults?.firstOrNull() ?: return emptyList()

        // 2. Obtener lista de episodios del anime
        val animeInfo = consumet.getAnimeInfo(animeResult.id)
        val episode = animeInfo.episodes?.find { it.number == episodeNumber }
            ?: animeInfo.episodes?.getOrNull(episodeNumber - 1)
            ?: return emptyList()

        // 3. Obtener fuentes de streaming
        return consumet.getStreamSources(episode.id).toStreamSources()
    }

    class Factory(
        private val animeTitle: String,
        private val episodeNumber: Int,
        private val animeId: Int
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T =
            PlayerViewModel(animeTitle, episodeNumber, animeId) as T
    }
}
