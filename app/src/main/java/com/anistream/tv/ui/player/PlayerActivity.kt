package com.anistream.tv.ui.player

import android.net.Uri
import android.os.Bundle
import android.view.View
import android.widget.ProgressBar
import android.widget.Toast
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.media3.common.MediaItem
import androidx.media3.common.PlaybackException
import androidx.media3.common.Player
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.ui.PlayerView
import com.anistream.tv.R
import com.anistream.tv.data.model.StreamSource
import com.anistream.tv.util.UiState
import kotlinx.coroutines.launch

class PlayerActivity : FragmentActivity() {

    companion object {
        const val EXTRA_ANIME_ID = "extra_anime_id"
        const val EXTRA_ANIME_TITLE = "extra_anime_title"
        const val EXTRA_EPISODE_NUMBER = "extra_episode_number"
    }

    private lateinit var viewModel: PlayerViewModel
    private var player: ExoPlayer? = null
    private lateinit var playerView: PlayerView
    private lateinit var progressBar: ProgressBar

    private var sourceIndex = 0
    private var currentSources: List<StreamSource> = emptyList()
    private var refetchOnError = true

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_player)

        playerView = findViewById(R.id.player_view)
        progressBar = findViewById(R.id.player_progress)

        val animeId = intent.getIntExtra(EXTRA_ANIME_ID, -1)
        val animeTitle = intent.getStringExtra(EXTRA_ANIME_TITLE) ?: ""
        val episodeNumber = intent.getIntExtra(EXTRA_EPISODE_NUMBER, 1)

        viewModel = ViewModelProvider(
            this, PlayerViewModel.Factory(animeTitle, episodeNumber, animeId)
        )[PlayerViewModel::class.java]

        observeState()
        viewModel.loadSources()
    }

    private fun observeState() {
        lifecycleScope.launch {
            viewModel.state.collect { state ->
                when (state) {
                    is UiState.Loading -> progressBar.visibility = View.VISIBLE
                    is UiState.Success -> {
                        progressBar.visibility = View.GONE
                        currentSources = state.data
                        sourceIndex = 0
                        playSource(currentSources[sourceIndex])
                    }
                    is UiState.Error -> {
                        progressBar.visibility = View.GONE
                        Toast.makeText(this@PlayerActivity, state.message, Toast.LENGTH_LONG).show()
                        finish()
                    }
                }
            }
        }
    }

    private fun playSource(source: StreamSource) {
        releasePlayer()
        player = ExoPlayer.Builder(this).build().also { exo ->
            playerView.player = exo

            val mediaItem = MediaItem.fromUri(Uri.parse(source.url))
            exo.setMediaItem(mediaItem)
            exo.prepare()

            val savedPosition = viewModel.savedPositionMs
            if (savedPosition > 0) exo.seekTo(savedPosition)

            exo.playWhenReady = true

            exo.addListener(object : Player.Listener {
                override fun onPlayerError(error: PlaybackException) {
                    // Intentar siguiente fuente o re-fetch
                    val nextIndex = sourceIndex + 1
                    if (nextIndex < currentSources.size) {
                        sourceIndex = nextIndex
                        playSource(currentSources[sourceIndex])
                    } else if (refetchOnError) {
                        refetchOnError = false
                        viewModel.loadSources()
                    } else {
                        Toast.makeText(
                            this@PlayerActivity,
                            "No se pudo reproducir el video",
                            Toast.LENGTH_LONG
                        ).show()
                        finish()
                    }
                }
            })
        }
    }

    override fun onPause() {
        super.onPause()
        player?.let { viewModel.saveProgress(it.currentPosition) }
        player?.pause()
    }

    override fun onResume() {
        super.onResume()
        player?.play()
    }

    override fun onDestroy() {
        player?.let { viewModel.saveProgress(it.currentPosition) }
        releasePlayer()
        super.onDestroy()
    }

    private fun releasePlayer() {
        player?.release()
        player = null
    }
}
