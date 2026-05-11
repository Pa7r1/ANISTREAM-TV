package com.anistream.tv.util

import android.content.Context
import com.anistream.tv.data.api.AniListService
import com.anistream.tv.data.api.ConsumetService
import com.anistream.tv.data.prefs.AppPreferences
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

object ServiceLocator {

    private lateinit var appContext: Context

    val preferences: AppPreferences by lazy { AppPreferences(appContext) }

    val aniListService: AniListService by lazy {
        buildRetrofit(Constants.ANILIST_BASE_URL).create(AniListService::class.java)
    }

    val consumetService: ConsumetService by lazy {
        buildRetrofit(Constants.CONSUMET_BASE_URL + "/").create(ConsumetService::class.java)
    }

    fun init(context: Context) {
        appContext = context.applicationContext
    }

    private fun buildRetrofit(baseUrl: String): Retrofit {
        val logging = HttpLoggingInterceptor().apply {
            level = HttpLoggingInterceptor.Level.BASIC
        }
        val client = OkHttpClient.Builder()
            .connectTimeout(15, TimeUnit.SECONDS)
            .readTimeout(15, TimeUnit.SECONDS)
            .addInterceptor(logging)
            .build()
        return Retrofit.Builder()
            .baseUrl(baseUrl)
            .client(client)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }
}
