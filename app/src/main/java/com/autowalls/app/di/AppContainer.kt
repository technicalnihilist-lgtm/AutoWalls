package com.autowalls.app.di

import android.content.Context
import android.os.Environment
import com.autowalls.app.data.local.AutoWallsDatabase
import com.autowalls.app.data.remote.WikimediaClient
import com.autowalls.app.data.repository.WallpaperRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import okhttp3.OkHttpClient
import java.io.File
import java.util.concurrent.TimeUnit

/** Manual dependency graph — small enough that a DI framework would be noise. */
class AppContainer(context: Context) {

    private val appContext = context.applicationContext

    private val database: AutoWallsDatabase = AutoWallsDatabase.get(appContext)

    private val api = WikimediaClient.createApi()

    private val downloadClient: OkHttpClient = OkHttpClient.Builder()
        .connectTimeout(30, TimeUnit.SECONDS)
        .readTimeout(120, TimeUnit.SECONDS)
        .writeTimeout(120, TimeUnit.SECONDS)
        .retryOnConnectionFailure(true)
        .build()

    private val picturesDir: File = File(
        appContext.getExternalFilesDir(Environment.DIRECTORY_PICTURES)
            ?: File(appContext.filesDir, "Pictures"),
        "AutoWalls"
    )

    val repository: WallpaperRepository = WallpaperRepository(
        api = api,
        dao = database.wallpaperDao(),
        downloadClient = downloadClient,
        picturesDir = picturesDir
    )

    val applicationScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
}
