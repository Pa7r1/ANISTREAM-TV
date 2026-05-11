package com.anistream.tv.ui.search

import android.content.Intent
import android.os.Bundle
import androidx.leanback.app.SearchSupportFragment
import androidx.leanback.widget.ArrayObjectAdapter
import androidx.leanback.widget.HeaderItem
import androidx.leanback.widget.ListRow
import androidx.leanback.widget.ListRowPresenter
import androidx.leanback.widget.ObjectAdapter
import androidx.lifecycle.lifecycleScope
import com.anistream.tv.data.api.AniListQueries
import com.anistream.tv.data.model.Anime
import com.anistream.tv.ui.detail.DetailActivity
import com.anistream.tv.ui.home.AnimeCardPresenter
import com.anistream.tv.util.Constants
import com.anistream.tv.util.ServiceLocator
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class SearchFragment : SearchSupportFragment(), SearchSupportFragment.SearchResultProvider {

    private val rowsAdapter = ArrayObjectAdapter(ListRowPresenter())
    private var searchJob: Job? = null
    private val aniList = ServiceLocator.aniListService

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setSearchResultProvider(this)
        setOnItemViewClickedListener { _, item, _, _ ->
            if (item is Anime) openDetail(item)
        }
    }

    override fun getResultsAdapter(): ObjectAdapter = rowsAdapter

    override fun onQueryTextChange(newQuery: String): Boolean {
        searchJob?.cancel()
        if (newQuery.length < 2) {
            rowsAdapter.clear()
            return true
        }
        searchJob = lifecycleScope.launch {
            delay(Constants.SEARCH_DEBOUNCE_MS)
            runSearch(newQuery)
        }
        return true
    }

    override fun onQueryTextSubmit(query: String): Boolean {
        searchJob?.cancel()
        lifecycleScope.launch { runSearch(query) }
        return true
    }

    private suspend fun runSearch(query: String) {
        try {
            val results = aniList.query(AniListQueries.search(query))
                .data?.page?.media?.mapNotNull { it.toAnime() } ?: emptyList()
            rowsAdapter.clear()
            if (results.isNotEmpty()) {
                val listAdapter = ArrayObjectAdapter(AnimeCardPresenter()).apply { addAll(0, results) }
                rowsAdapter.add(ListRow(HeaderItem("Resultados"), listAdapter))
            }
        } catch (_: Exception) {
            rowsAdapter.clear()
        }
    }

    private fun openDetail(anime: Anime) {
        startActivity(Intent(requireContext(), DetailActivity::class.java).apply {
            putExtra(DetailActivity.EXTRA_ANIME_ID, anime.id)
            putExtra(DetailActivity.EXTRA_ANIME_TITLE, anime.displayTitle)
        })
    }
}
