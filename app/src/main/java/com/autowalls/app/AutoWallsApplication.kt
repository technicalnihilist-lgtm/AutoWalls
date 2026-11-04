package com.autowalls.app

import android.app.Application
import android.app.NotificationChannel
import android.app.NotificationManager
import android.os.Build
import coil.ImageLoader
import coil.ImageLoaderFactory
import coil.disk.DiskCache
import coil.memory.MemoryCache
import com.autowalls.app.di.AppContainer
import okhttp3.OkHttpClient

class AutoWallsApplication : Application(), ImageLoaderFactory {

    lateinit var container: AppContainer
        private set

    override fun onCreate() {
        super.onCreate()
        container = AppContainer(this)
        createNotificationChannel()
    }

    /**
     * Coil loader tuned for Wikimedia: sends the required descriptive User-Agent
     * and caches aggressively on disk so re-scrolling a feed costs no network.
     */
    override fun newImageLoader(): ImageLoader = ImageLoader.Builder(this)
        .okHttpClient {
            OkHttpClient.Builder()
                .addInterceptor { chain ->
                    chain.proceed(
                        chain.request().newBuilder()
                            .header("User-Agent", USER_AGENT)
                            .build()
                    )
                }
                .build()
        }
        .memoryCache {
            MemoryCache.Builder(this)
                .maxSizePercent(0.25)
                .build()
        }
        .diskCache {
            DiskCache.Builder()
                .directory(cacheDir.resolve("wallpaper_image_cache"))
                .maxSizeBytes(512L * 1024L * 1024L)
                .build()
        }
        .crossfade(280)
        .respectCacheHeaders(false)
        .build()

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) return
        val manager = getSystemService(NotificationManager::class.java) ?: return
        val channel = NotificationChannel(
            "autowalls_downloads",
            getString(R.string.download_channel_name),
            NotificationManager.IMPORTANCE_LOW
        ).apply {
            description = getString(R.string.download_channel_description)
            setShowBadge(false)
        }
        manager.createNotificationChannel(channel)
    }

    companion object {
        const val USER_AGENT =
            "AutoWalls/1.0 (Android wallpaper app; contact: nihaonihao54059@gmail.com)"
    }
}
