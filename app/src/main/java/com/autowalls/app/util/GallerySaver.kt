package com.autowalls.app.util

import android.content.ContentValues
import android.content.Context
import android.media.MediaScannerConnection
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.io.IOException

/** Copies a downloaded wallpaper into the shared Pictures gallery. */
object GallerySaver {

    private const val ALBUM = "AutoWalls"

    suspend fun save(context: Context, source: File, displayName: String): Result<Uri> =
        withContext(Dispatchers.IO) {
            runCatching {
                if (!source.exists()) throw IOException("Download the wallpaper first")
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                    saveWithMediaStore(context, source, displayName)
                } else {
                    saveLegacy(context, source, displayName)
                }
            }
        }

    private fun saveWithMediaStore(context: Context, source: File, displayName: String): Uri {
        val resolver = context.contentResolver
        val mime = if (source.extension.equals("png", true)) "image/png" else "image/jpeg"
        val values = ContentValues().apply {
            put(MediaStore.Images.Media.DISPLAY_NAME, safeName(displayName, source))
            put(MediaStore.Images.Media.MIME_TYPE, mime)
            put(
                MediaStore.Images.Media.RELATIVE_PATH,
                Environment.DIRECTORY_PICTURES + File.separator + ALBUM
            )
            put(MediaStore.Images.Media.IS_PENDING, 1)
        }

        val collection = MediaStore.Images.Media.getContentUri(MediaStore.VOLUME_EXTERNAL_PRIMARY)
        val uri = resolver.insert(collection, values)
            ?: throw IOException("Could not create a gallery entry")

        resolver.openOutputStream(uri)?.use { output ->
            source.inputStream().use { input -> input.copyTo(output) }
        } ?: throw IOException("Could not open the gallery file")

        values.clear()
        values.put(MediaStore.Images.Media.IS_PENDING, 0)
        resolver.update(uri, values, null, null)
        return uri
    }

    @Suppress("DEPRECATION")
    private fun saveLegacy(context: Context, source: File, displayName: String): Uri {
        val album = File(
            Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES),
            ALBUM
        )
        if (!album.exists() && !album.mkdirs()) throw IOException("Cannot create gallery folder")

        val target = File(album, safeName(displayName, source))
        source.copyTo(target, overwrite = true)

        MediaScannerConnection.scanFile(context, arrayOf(target.absolutePath), null, null)
        return Uri.fromFile(target)
    }

    private fun safeName(displayName: String, source: File): String {
        val base = displayName
            .replace(Regex("[^A-Za-z0-9 _-]"), "")
            .trim()
            .replace(' ', '_')
            .take(70)
            .ifBlank { "autowalls" }
        return "$base.${source.extension}"
    }
}
