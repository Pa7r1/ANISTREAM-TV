package com.anistream.tv.ui.player

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.anistream.tv.data.model.StreamSource
import com.anistream.tv.util.Constants
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
                if (sources.isEmpty()) {
                    val providers = Constants.ANIME_PROVIDERS.joinToString()
                    throw Exception("No hay fuentes disponibles. Probados: $providers")
                }
                _state.value = UiState.Success(sources)
            } catch (e: Exception) {
                _state.value = UiState.Error(e.message ?: "Error al cargar el video")
            }
        }
    }

    private suspend fun fetchSources(): List<StreamSource> {
        for (provider in Constants.ANIME_PROVIDERS) {
            try {
                val results = consumet.searchAnime(provider, animeTitle).results.orEmpty()
                val match = results.firstOrNull()
                if (match == null) {
                    Log.w(TAG, "provider=$provider: sin resultados para '$animeTitle'")
                    continue
                }

                val info = consumet.getAnimeInfo(provider, match.id)
                val episode = info.episodes?.find { it.number == episodeNumber }
                    ?: info.episodes?.getOrNull(episodeNumber - 1)
                if (episode == null) {
                    Log.w(TAG, "provider=$provider: episodio $episodeNumber no encontrado en ${match.id}")
                    continue
                }

                val defaultSources = runCatching {
                    consumet.getStreamSources(provider, episode.id, null).toStreamSources()
                }.onFailure { Log.w(TAG, "provider=$provider default watch fallo: ${it.message}") }
                    .getOrNull().orEmpty()
                if (defaultSources.isNotEmpty()) {
                    Log.i(TAG, "provider=$provider: ${defaultSources.size} fuentes (default server)")
                    return defaultSources
                }

                val servers = runCatching { consumet.getServers(provider, episode.id) }
                    .onFailure { Log.w(TAG, "provider=$provider /servers fallo: ${it.message}") }
                    .getOrNull().orEmpty()
                for (s in servers) {
                    val name = s.name ?: continue
                    val src = runCatching {
                        consumet.getStreamSources(provider, episode.id, name).toStreamSources()
                    }.onFailure { Log.w(TAG, "provider=$provider server=$name fallo: ${it.message}") }
                        .getOrNull().orEmpty()
                    if (src.isNotEmpty()) {
                        Log.i(TAG, "provider=$provider server=$name: ${src.size} fuentes")
                        return src
                    }
                }
            } catch (e: Exception) {
                Log.w(TAG, "provider=$provider error general: ${e.message}")
            }
        }
        return emptyList()
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

    companion object {
        private const val TAG = "PlayerVM"
    }
}
