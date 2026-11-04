package com.autowalls.app.util

import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import java.util.Locale
import java.util.concurrent.TimeUnit

object Formatters {

    fun fileSize(bytes: Long): String {
        if (bytes <= 0) return "—"
        val mb = bytes / (1024.0 * 1024.0)
        return if (mb >= 1.0) String.format(Locale.US, "%.1f MB", mb)
        else String.format(Locale.US, "%.0f KB", bytes / 1024.0)
    }

    fun relativeTime(timestamp: Long): String {
        val diff = System.currentTimeMillis() - timestamp
        val minutes = TimeUnit.MILLISECONDS.toMinutes(diff)
        val hours = TimeUnit.MILLISECONDS.toHours(diff)
        val days = TimeUnit.MILLISECONDS.toDays(diff)
        return when {
            minutes < 1 -> "just now"
            minutes < 60 -> "$minutes min ago"
            hours < 24 -> "$hours h ago"
            days < 7 -> "$days d ago"
            else -> String.format(Locale.US, "%d weeks ago", days / 7)
        }
    }
}

object NetworkUtils {

    fun isOnline(context: Context): Boolean {
        val manager = context.getSystemService(Context.CONNECTIVITY_SERVICE) as? ConnectivityManager
            ?: return true
        val network = manager.activeNetwork ?: return false
        val capabilities = manager.getNetworkCapabilities(network) ?: return false
        return capabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET) &&
            capabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_VALIDATED)
    }
}
