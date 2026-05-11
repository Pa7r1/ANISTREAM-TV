package com.anistream.tv.ui.detail

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import com.bumptech.glide.Glide
import androidx.leanback.app.DetailsSupportFragment
import androidx.leanback.widget.Action
import androidx.leanback.widget.ArrayObjectAdapter
import androidx.leanback.widget.ClassPresenterSelector
import androidx.leanback.widget.DetailsOverviewRow
import androidx.leanback.widget.FullWidthDetailsOverviewRowPresenter
import androidx.leanback.widget.HeaderItem
import androidx.leanback.widget.ListRow
import androidx.leanback.widget.ListRowPresenter
import androidx.leanback.widget.OnActionClickedListener
import androidx.leanback.widget.OnItemViewClickedListener
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import com.anistream.tv.R
import com.anistream.tv.data.model.Anime
import com.anistream.tv.data.model.Episode
import com.anistream.tv.ui.detail.DetailActivity.Companion.EXTRA_ANIME_ID
import com.anistream.tv.ui.player.PlayerActivity
import com.anistream.tv.util.ServiceLocator
import com.anistream.tv.util.UiState
import kotlinx.coroutines.launch

class DetailFragment : DetailsSupportFragment() {

    companion object {
        private const val ACTION_WATCH = 1L
        private const val ACTION_FAVORITE = 2L

        fun newInstance(animeId: Int) = DetailFragment().apply {
            arguments = Bundle().apply { putInt(EXTRA_ANIME_ID, animeId) }
        }
    }

    private lateinit var viewModel: DetailViewModel
    private val rowsAdapter = ArrayObjectAdapter(ClassPresenterSelector())
    private var currentAnime: Anime? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        adapter = rowsAdapter

        val animeId = arguments?.getInt(EXTRA_ANIME_ID, -1) ?: -1
        viewModel = ViewModelProvider(this, DetailViewModel.Factory(animeId))[DetailViewModel::class.java]

        onItemViewClickedListener = OnItemViewClickedListener { _, item, _, _ ->
            if (item is Episode) openPlayer(item)
        }

        lifecycleScope.launch {
            viewModel.state.collect { state ->
                when (state) {
                    is UiState.Loading -> { }
                    is UiState.Success -> {
                        currentAnime = state.data.anime
                        ServiceLocator.preferences.addToHistory(state.data.anime.id)
                        buildUi(state.data)
                    }
                    is UiState.Error -> {
                        Toast.makeText(requireContext(), state.message, Toast.LENGTH_LONG).show()
                    }
                }
            }
        }
    }

    private fun buildUi(data: DetailData) {
        rowsAdapter.clear()
        buildDetailsRow(data.anime)
        buildEpisodesRow(data.episodes)
    }

    private fun buildDetailsRow(anime: Anime) {
        val isFav = ServiceLocator.preferences.isFavorite(anime.id)
        val favLabel = if (isFav) "★ Favorito" else getString(R.string.action_favorite)

        val detailRow = DetailsOverviewRow(anime).apply {
            actionsAdapter = ArrayObjectAdapter().apply {
                add(Action(ACTION_WATCH, getString(R.string.action_watch)))
                add(Action(ACTION_FAVORITE, favLabel))
            }
        }

        Glide.with(this)
            .load(anime.coverImageLarge)
            .override(com.anistream.tv.util.Constants.IMG_WIDTH, com.anistream.tv.util.Constants.IMG_HEIGHT)
            .placeholder(R.drawable.placeholder_anime)
            .into(object : com.bumptech.glide.request.target.CustomTarget<android.graphics.drawable.Drawable>() {
                override fun onResourceReady(r: android.graphics.drawable.Drawable, t: com.bumptech.glide.request.transition.Transition<in android.graphics.drawable.Drawable>?) {
                    detailRow.imageDrawable = r
                }
                override fun onLoadCleared(p: android.graphics.drawable.Drawable?) {}
            })

        val detailPresenter = FullWidthDetailsOverviewRowPresenter(AnimeDetailsPresenter()).apply {
            onActionClickedListener = OnActionClickedListener { action ->
                when (action.id) {
                    ACTION_WATCH -> openPlayer(Episode(1, null, null))
                    ACTION_FAVORITE -> toggleFavorite(anime, detailRow)
                }
            }
        }

        val selector = rowsAdapter.presenterSelector as ClassPresenterSelector
        selector.addClassPresenter(DetailsOverviewRow::class.java, detailPresenter)
        selector.addClassPresenter(ListRow::class.java, ListRowPresenter())
        rowsAdapter.add(detailRow)
    }

    private fun buildEpisodesRow(episodes: List<Episode>) {
        if (episodes.isEmpty()) return
        val listAdapter = ArrayObjectAdapter(EpisodeCardPresenter()).apply { addAll(0, episodes) }
        rowsAdapter.add(ListRow(HeaderItem(getString(R.string.row_episodes)), listAdapter))
    }

    private fun openPlayer(episode: Episode) {
        val anime = currentAnime ?: return
        startActivity(Intent(requireContext(), PlayerActivity::class.java).apply {
            putExtra(PlayerActivity.EXTRA_ANIME_ID, anime.id)
            putExtra(PlayerActivity.EXTRA_ANIME_TITLE, anime.displayTitle)
            putExtra(PlayerActivity.EXTRA_EPISODE_NUMBER, episode.number)
        })
    }

    private fun toggleFavorite(anime: Anime, detailRow: DetailsOverviewRow) {
        val added = ServiceLocator.preferences.toggleFavorite(anime.id)
        val label = if (added) "★ Favorito" else getString(R.string.action_favorite)
        val msg = if (added) "Agregado a favoritos" else "Eliminado de favoritos"
        (detailRow.actionsAdapter as? ArrayObjectAdapter)?.let { adapter ->
            val action = (0 until adapter.size()).mapNotNull { adapter.get(it) as? Action }
                .find { it.id == ACTION_FAVORITE }
            action?.label1 = label
            adapter.notifyArrayItemRangeChanged(1, 1)
        }
        Toast.makeText(requireContext(), msg, Toast.LENGTH_SHORT).show()
    }
}
