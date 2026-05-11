package com.anistream.tv.ui.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.anistream.tv.data.api.AniListQueries
import com.anistream.tv.data.model.Anime
import com.anistream.tv.util.ServiceLocator
import com.anistream.tv.util.UiState
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

data class HomeData(
    val trending: List<Anime>,
    val recent: List<Anime>,
    val continueWatching: List<Anime>,
    val favorites: List<Anime>
)

class HomeViewModel : ViewModel() {

    private val _state = MutableStateFlow<UiState<HomeData>>(UiState.Loading)
    val state: StateFlow<UiState<HomeData>> = _state

    private val aniList = ServiceLocator.aniListService
    private val prefs = ServiceLocator.preferences

    init {
        loadHome()
    }

    fun loadHome() {
        viewModelScope.launch {
            _state.value = UiState.Loading
            try {
                val trendingDeferred = async { aniList.query(AniListQueries.trending()) }
                val recentDeferred = async { aniList.query(AniListQueries.recentlyUpdated()) }

                val historyIds = prefs.getHistory()
                val favoriteIds = prefs.getFavoriteIds().toList()

                val historyDeferred = async {
                    if (historyIds.isEmpty()) emptyList()
                    else aniList.query(AniListQueries.byIds(historyIds))
                        .data?.page?.media?.mapNotNull { it.toAnime() } ?: emptyList()
                }
                val favoritesDeferred = async {
                    if (favoriteIds.isEmpty()) emptyList()
                    else aniList.query(AniListQueries.byIds(favoriteIds))
                        .data?.page?.media?.mapNotNull { it.toAnime() } ?: emptyList()
                }

                val trending = trendingDeferred.await()
                    .data?.page?.media?.mapNotNull { it.toAnime() } ?: emptyList()
                val recent = recentDeferred.await()
                    .data?.page?.media?.mapNotNull { it.toAnime() } ?: emptyList()

                _state.value = UiState.Success(
                    HomeData(
                        trending = trending,
                        recent = recent,
                        continueWatching = historyDeferred.await(),
                        favorites = favoritesDeferred.await()
                    )
                )
            } catch (e: Exception) {
                _state.value = UiState.Error(e.message ?: "Error desconocido")
            }
        }
    }
}
