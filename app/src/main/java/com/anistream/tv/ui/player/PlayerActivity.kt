package com.anistream.tv.ui.player

import android.app.AlertDialog
import android.net.Uri
import android.os.Bundle
import android.view.KeyEvent
import android.view.View
import android.widget.ProgressBar
import android.widget.Toast
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.media3.common.MediaItem
import androidx.media3.common.PlaybackException
import androidx.media3.common.Player
import androidx.media3.datasource.DefaultHttpDataSource
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.exoplayer.source.DefaultMediaSourceFactory
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
        private const val MENU_HINT_TIMEOUT_MS = 4000L
    }

    private lateinit var viewModel: PlayerViewModel
    private var player: ExoPlayer? = null
    private lateinit var playerView: PlayerView
    private lateinit var progressBar: ProgressBar

    private var sourceIndex = 0
    private var currentSources: List<StreamSource> = emptyList()
    private var refetchOnError = true
    private var hintShown = false

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
                        if (!hintShown && currentSources.size > 1) {
                            hintShown = true
                            Toast.makeText(
                                this@PlayerActivity,
                                "Pulsá MENÚ o INFO para cambiar idioma/servidor",
                                Toast.LENGTH_LONG
                            ).show()
                        }
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

        // Headers HTTP que el CDN del embed exige (Referer, User-Agent).
        // Default Referer animeflv.net por si el server no mandó nada.
        val ua =
            "Mozilla/5.0 (Linux; Android 10) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/120 Safari/537.36"
        val requestHeaders = mutableMapOf(
            "User-Agent" to ua,
            "Referer" to "https://www3.animeflv.net/"
        )
        source.headers?.forEach { (k, v) -> requestHeaders[k] = v }

        val httpFactory = DefaultHttpDataSource.Factory()
            .setUserAgent(requestHeaders["User-Agent"])
            .setAllowCrossProtocolRedirects(true)
            .setConnectTimeoutMs(20_000)
            .setReadTimeoutMs(20_000)
            .setDefaultRequestProperties(requestHeaders)
        val mediaSourceFactory = DefaultMediaSourceFactory(httpFactory)

        player = ExoPlayer.Builder(this)
            .setMediaSourceFactory(mediaSourceFactory)
            .build().also { exo ->
            playerView.player = exo

            val mediaItem = MediaItem.fromUri(Uri.parse(source.url))
            exo.setMediaItem(mediaItem)
            exo.prepare()

            val savedPosition = viewModel.savedPositionMs
            if (savedPosition > 0) exo.seekTo(savedPosition)

            exo.playWhenReady = true

            Toast.makeText(
                this,
                "Reproduciendo: ${source.version ?: "default"} — ${source.server ?: ""}",
                Toast.LENGTH_SHORT
            ).show()

            exo.addListener(object : Player.Listener {
                override fun onPlayerError(error: PlaybackException) {
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

    /** Intercepta MENÚ/INFO/Y para abrir el selector de versión sin perder progreso. */
    override fun onKeyDown(keyCode: Int, event: KeyEvent?): Boolean {
        when (keyCode) {
            KeyEvent.KEYCODE_MENU,
            KeyEvent.KEYCODE_INFO,
            KeyEvent.KEYCODE_BUTTON_Y -> {
                if (currentSources.size > 1) {
                    showSourcePicker()
                    return true
                }
            }
        }
        return super.onKeyDown(keyCode, event)
    }

    private fun showSourcePicker() {
        val labels = currentSources.map { s ->
            val v = s.version ?: "default"
            val srv = s.server ?: "?"
            val q = s.quality
            "$v — $srv — $q"
        }.toTypedArray()
        AlertDialog.Builder(this)
            .setTitle("Idioma / Servidor")
            .setSingleChoiceItems(labels, sourceIndex) { dialog, which ->
                dialog.dismiss()
                if (which != sourceIndex) {
                    val pos = player?.currentPosition ?: 0L
                    sourceIndex = which
                    val sel = currentSources[which]
                    sel.version?.let { v ->
                        if (v != viewModel.getLanguagePreference() && v in setOf("LAT", "SUB", "ESP")) {
                            viewModel.setLanguagePreference(v)
                        }
                    }
                    playSource(sel)
                    // restaurar posición tras el swap
                    player?.seekTo(pos)
                }
            }
            .setNegativeButton("Cancelar", null)
            .show()
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
