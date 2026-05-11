package com.anistream.tv.ui.home

import android.view.ViewGroup
import androidx.leanback.widget.ImageCardView
import androidx.leanback.widget.Presenter
import com.anistream.tv.data.model.Anime
import com.anistream.tv.util.Constants
import com.anistream.tv.util.ImageLoader

class AnimeCardPresenter : Presenter() {

    override fun onCreateViewHolder(parent: ViewGroup): ViewHolder {
        val card = ImageCardView(parent.context).apply {
            isFocusable = true
            isFocusableInTouchMode = true
            setMainImageDimensions(Constants.IMG_WIDTH / 2, Constants.IMG_HEIGHT / 2)
        }
        return ViewHolder(card)
    }

    override fun onBindViewHolder(viewHolder: ViewHolder, item: Any) {
        val anime = item as Anime
        val card = viewHolder.view as ImageCardView
        card.titleText = anime.displayTitle
        card.contentText = anime.genres.take(2).joinToString(", ")
        ImageLoader.load(card.mainImageView, anime.coverImageLarge)
    }

    override fun onUnbindViewHolder(viewHolder: ViewHolder) {
        val card = viewHolder.view as ImageCardView
        card.mainImage = null
    }
}
