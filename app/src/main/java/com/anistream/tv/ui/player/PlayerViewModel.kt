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

    /** Persiste la preferencia de idioma (lo usa el dialog del player). */
    fun setLanguagePreference(value: String) = prefs.setLanguagePreference(value)
    fun getLanguagePreference(): String = prefs.getLanguagePreference()

    fun loadSources() {
        viewModelScope.launch {
            _state.value = UiState.Loading
            try {
                val sources = fetchSources()
                if (sources.isEmpty()) {
                    throw Exception(
                        "No disponible en español. Probados: ${Constants.ANIME_PROVIDERS.joinToString()}"
                    )
                }
                val sorted = sortByLanguage(sources)
                _state.value = UiState.Success(sorted)
            } catch (e: Exception) {
                _state.value = UiState.Error(e.message ?: "Error al cargar el video")
            }
        }
    }

    /** Recolecta TODAS las versiones disponibles del primer provider hispano que responda. */
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

                val sources = runCatching {
                    consumet.getStreamSources(provider, episode.id, null).toStreamSources()
                }.onFailure { Log.w(TAG, "provider=$provider watch fallo: ${it.message}") }
                    .getOrNull().orEmpty()

                if (sources.isNotEmpty()) {
                    Log.i(TAG, "provider=$provider: ${sources.size} fuentes (versiones=${sources.mapNotNull { it.version }.toSet()})")
                    return sources
                }
            } catch (e: Exception) {
                Log.w(TAG, "provider=$provider error general: ${e.message}")
            }
        }
        return emptyList()
    }

    /** Ordena las sources poniendo la preferencia de idioma primero. */
    private fun sortByLanguage(sources: List<StreamSource>): List<StreamSource> {
        val pref = prefs.getLanguagePreference()
        if (pref == "AUTO") return sources
        // Prioridad: preferida > otra hispana > resto
        val rank: (StreamSource) -> Int = { s ->
            when {
                s.version == pref -> 0
                s.version in setOf("LAT", "SUB", "ESP") -> 1
                else -> 2
            }
        }
        return sources.sortedBy(rank)
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
