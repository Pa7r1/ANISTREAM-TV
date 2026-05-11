package com.anistream.tv.ui.home

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.leanback.app.BrowseSupportFragment
import androidx.leanback.widget.ArrayObjectAdapter
import androidx.leanback.widget.HeaderItem
import androidx.leanback.widget.ListRow
import androidx.leanback.widget.ListRowPresenter
import androidx.leanback.widget.OnItemViewClickedListener
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import com.anistream.tv.R
import com.anistream.tv.data.model.Anime
import com.anistream.tv.ui.detail.DetailActivity
import com.anistream.tv.ui.search.SearchActivity
import com.anistream.tv.util.UiState
import kotlinx.coroutines.launch

class HomeBrowseFragment : BrowseSupportFragment() {

    private lateinit var viewModel: HomeViewModel
    private val rowsAdapter = ArrayObjectAdapter(ListRowPresenter())

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        title = getString(R.string.app_name)
        headersState = HEADERS_ENABLED
        isHeadersTransitionOnBackEnabled = true
        brandColor = requireContext().getColor(R.color.leanback_brand)

        adapter = rowsAdapter

        onItemViewClickedListener = OnItemViewClickedListener { _, item, _, _ ->
            if (item is Anime) openDetail(item)
        }

        setOnSearchClickedListener {
            startActivity(Intent(requireContext(), SearchActivity::class.java))
        }
    }

    override fun onStart() {
        super.onStart()
        viewModel = ViewModelProvider(requireActivity())[HomeViewModel::class.java]
        observeState()
    }

    override fun onResume() {
        super.onResume()
        // Recargar para reflejar cambios en favoritos/historial
        viewModel.loadHome()
    }

    private fun observeState() {
        lifecycleScope.launch {
            viewModel.state.collect { state ->
                when (state) {
                    is UiState.Loading -> showLoading(true)
                    is UiState.Success -> {
                        showLoading(false)
                        buildRows(state.data)
                    }
                    is UiState.Error -> {
                        showLoading(false)
                        Toast.makeText(requireContext(), state.message, Toast.LENGTH_LONG).show()
                    }
                }
            }
        }
    }

    private fun buildRows(data: HomeData) {
        rowsAdapter.clear()
        addRow(getString(R.string.row_continue), data.continueWatching)
        addRow(getString(R.string.row_favorites), data.favorites)
        addRow(getString(R.string.row_trending), data.trending)
        addRow(getString(R.string.row_recent), data.recent)
    }

    private fun addRow(title: String, items: List<Anime>) {
        if (items.isEmpty()) return
        val listAdapter = ArrayObjectAdapter(AnimeCardPresenter()).apply { addAll(0, items) }
        rowsAdapter.add(ListRow(HeaderItem(title), listAdapter))
    }

    private fun showLoading(loading: Boolean) {
        progressBarManager.apply { if (loading) show() else hide() }
    }

    private fun openDetail(anime: Anime) {
        startActivity(Intent(requireContext(), DetailActivity::class.java).apply {
            putExtra(DetailActivity.EXTRA_ANIME_ID, anime.id)
            putExtra(DetailActivity.EXTRA_ANIME_TITLE, anime.displayTitle)
        })
    }
}
