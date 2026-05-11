package com.anistream.tv

import android.app.Application
import com.anistream.tv.util.ServiceLocator

class App : Application() {
    override fun onCreate() {
        super.onCreate()
        ServiceLocator.init(this)
    }
}
