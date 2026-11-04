package com.autowalls.app.util

import android.app.WallpaperManager
import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.Build
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.io.IOException

/**
 * Applies a downloaded file as the device wallpaper through [WallpaperManager].
 * Images are decoded downsampled so a 6000px original never blows up the heap.
 */
object WallpaperSetter {

    enum class Target(val label: String) {
        HOME("Home screen"),
        LOCK("Lock screen"),
        BOTH("Home and lock screen")
    }

    private const val MAX_DIMENSION = 2560

    suspend fun apply(context: Context, file: File, target: Target): Result<Unit> =
        withContext(Dispatchers.IO) {
            runCatching {
                if (!file.exists() || file.length() == 0L) {
                    throw IOException("Wallpaper file is missing. Download it again.")
                }

                val manager = WallpaperManager.getInstance(context)
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                    if (!manager.isWallpaperSupported) {
                        throw UnsupportedOperationException("This device does not allow wallpapers")
                    }
                    if (!manager.isSetWallpaperAllowed) {
                        throw SecurityException("Setting wallpapers is blocked by device policy")
                    }
                }

                val bitmap = decodeSampled(file)
                    ?: throw IOException("Could not decode the wallpaper image")

                try {
                    when (target) {
                        Target.HOME ->
                            manager.setBitmap(bitmap, null, true, WallpaperManager.FLAG_SYSTEM)
                        Target.LOCK ->
                            manager.setBitmap(bitmap, null, true, WallpaperManager.FLAG_LOCK)
                        Target.BOTH -> {
                            manager.setBitmap(bitmap, null, true, WallpaperManager.FLAG_SYSTEM)
                            manager.setBitmap(bitmap, null, true, WallpaperManager.FLAG_LOCK)
                        }
                    }
                } finally {
                    bitmap.recycle()
                }
                // setBitmap returns a Boolean; this task only reports success.
                Unit
            }
        }

    private fun decodeSampled(file: File): Bitmap? {
        val bounds = BitmapFactory.Options().apply { inJustDecodeBounds = true }
        BitmapFactory.decodeFile(file.absolutePath, bounds)
        if (bounds.outWidth <= 0 || bounds.outHeight <= 0) return null

        var sampleSize = 1
        var longest = maxOf(bounds.outWidth, bounds.outHeight)
        while (longest / sampleSize > MAX_DIMENSION) {
            sampleSize *= 2
            longest /= 2
        }

        val options = BitmapFactory.Options().apply {
            inSampleSize = sampleSize
            inPreferredConfig = Bitmap.Config.ARGB_8888
        }
        return BitmapFactory.decodeFile(file.absolutePath, options)
    }
}
