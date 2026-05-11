package com.anistream.tv.util

import android.widget.ImageView
import com.anistream.tv.R
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.request.RequestOptions

object ImageLoader {

    private val options = RequestOptions()
        .override(Constants.IMG_WIDTH, Constants.IMG_HEIGHT)
        .placeholder(R.drawable.placeholder_anime)
        .error(R.drawable.placeholder_anime)
        .diskCacheStrategy(DiskCacheStrategy.ALL)
        .centerCrop()

    fun load(imageView: ImageView, url: String?) {
        Glide.with(imageView.context)
            .load(url)
            .apply(options)
            .into(imageView)
    }
}
