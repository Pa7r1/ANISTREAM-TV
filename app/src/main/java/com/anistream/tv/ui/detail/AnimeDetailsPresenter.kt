package com.anistream.tv.ui.detail

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.leanback.widget.AbstractDetailsDescriptionPresenter
import com.anistream.tv.data.model.Anime

class AnimeDetailsPresenter : AbstractDetailsDescriptionPresenter() {

    override fun onBindDescription(viewHolder: ViewHolder, item: Any) {
        val anime = item as Anime
        viewHolder.title.text = anime.displayTitle
        viewHolder.subtitle.text = anime.genres.take(3).joinToString(" • ")
        viewHolder.body.text = anime.description ?: ""
    }
}
