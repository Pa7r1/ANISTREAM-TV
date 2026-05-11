package com.anistream.tv.ui.detail

import android.os.Bundle
import androidx.fragment.app.FragmentActivity
import com.anistream.tv.R

class DetailActivity : FragmentActivity() {

    companion object {
        const val EXTRA_ANIME_ID = "extra_anime_id"
        const val EXTRA_ANIME_TITLE = "extra_anime_title"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_detail)
        if (savedInstanceState == null) {
            val animeId = intent.getIntExtra(EXTRA_ANIME_ID, -1)
            val fragment = DetailFragment.newInstance(animeId)
            supportFragmentManager.beginTransaction()
                .replace(R.id.detail_container, fragment)
                .commit()
        }
    }
}
