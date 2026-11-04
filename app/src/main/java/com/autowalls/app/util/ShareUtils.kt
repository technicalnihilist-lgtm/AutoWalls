package com.autowalls.app.util

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import androidx.core.content.FileProvider
import com.autowalls.app.domain.model.Wallpaper
import java.io.File

object ShareUtils {

    fun shareWallpaper(context: Context, wallpaper: Wallpaper, localFile: File?) {
        val attribution = wallpaper.attributionText()
        val intent = if (localFile != null && localFile.exists()) {
            val uri = runCatching {
                FileProvider.getUriForFile(
                    context,
                    "${context.packageName}.fileprovider",
                    localFile
                )
            }.getOrNull()

            if (uri != null) {
                Intent(Intent.ACTION_SEND).apply {
                    type = if (localFile.extension.equals("png", true)) "image/png" else "image/jpeg"
                    putExtra(Intent.EXTRA_STREAM, uri)
                    putExtra(Intent.EXTRA_TEXT, attribution)
                    putExtra(Intent.EXTRA_SUBJECT, wallpaper.displayName)
                    addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                }
            } else {
                textIntent(wallpaper, attribution)
            }
        } else {
            textIntent(wallpaper, attribution)
        }

        runCatching {
            context.startActivity(
                Intent.createChooser(intent, "Share wallpaper")
                    .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            )
        }
    }

    private fun textIntent(wallpaper: Wallpaper, attribution: String) =
        Intent(Intent.ACTION_SEND).apply {
            type = "text/plain"
            putExtra(Intent.EXTRA_SUBJECT, wallpaper.displayName)
            putExtra(Intent.EXTRA_TEXT, attribution)
        }

    fun copyAttribution(context: Context, wallpaper: Wallpaper) {
        val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
        clipboard.setPrimaryClip(
            ClipData.newPlainText("AutoWalls attribution", wallpaper.attributionText())
        )
    }
}
