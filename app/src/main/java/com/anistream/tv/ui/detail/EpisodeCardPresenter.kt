package com.anistream.tv.ui.detail

import android.view.ViewGroup
import androidx.leanback.widget.ImageCardView
import androidx.leanback.widget.Presenter
import com.anistream.tv.data.model.Episode
import com.anistream.tv.util.ImageLoader

class EpisodeCardPresenter : Presenter() {

    override fun onCreateViewHolder(parent: ViewGroup): ViewHolder {
        val card = ImageCardView(parent.context).apply {
            isFocusable = true
            isFocusableInTouchMode = true
            setMainImageDimensions(240, 135)
        }
        return ViewHolder(card)
    }

    override fun onBindViewHolder(viewHolder: ViewHolder, item: Any) {
        val episode = item as Episode
        val card = viewHolder.view as ImageCardView
        card.titleText = episode.displayTitle
        card.contentText = "Ep. ${episode.number}"
        ImageLoader.load(card.mainImageView, episode.thumbnail)
    }

    override fun onUnbindViewHolder(viewHolder: ViewHolder) {
        val card = viewHolder.view as ImageCardView
        card.mainImage = null
    }
}
