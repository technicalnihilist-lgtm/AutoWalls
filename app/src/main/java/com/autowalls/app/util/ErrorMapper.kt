package com.autowalls.app.util

import java.io.IOException
import java.net.SocketTimeoutException
import java.net.UnknownHostException

object ErrorMapper {

    /** Turns low-level failures into something a person can act on. */
    fun message(error: Throwable, online: Boolean): String {
        val raw = error.message.orEmpty()
        return when {
            !online -> "You're offline. AutoWalls loads wallpapers live from Wikimedia Commons."
            error is UnknownHostException ->
                "Can't reach Wikimedia Commons. Check your connection and try again."
            error is SocketTimeoutException ->
                "Wikimedia Commons took too long to respond. Try again in a moment."
            error is IOException && raw.contains("HTTP 429") ->
                "Wikimedia is rate-limiting requests. Wait a few seconds, then retry."
            error is IOException && raw.startsWith("HTTP") ->
                "Wikimedia Commons returned an error ($raw). Please retry."
            raw.isNotBlank() -> raw
            else -> "Something went wrong while loading wallpapers."
        }
    }
}
