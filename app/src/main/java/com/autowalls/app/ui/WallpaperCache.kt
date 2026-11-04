package com.autowalls.app.ui

import com.autowalls.app.domain.model.Wallpaper

/**
 * Tiny in-memory hand-off between list screens and the detail screen so tapping
 * a card never re-fetches metadata it already has.
 */
object WallpaperCache {

    private const val MAX_ENTRIES = 240
    private val entries = object : LinkedHashMap<Long, Wallpaper>(64, 0.75f, true) {
        override fun removeEldestEntry(
            eldest: MutableMap.MutableEntry<Long, Wallpaper>?
        ): Boolean = size > MAX_ENTRIES
    }

    @Synchronized
    fun put(wallpaper: Wallpaper) {
        entries[wallpaper.id] = wallpaper
    }

    @Synchronized
    fun putAll(wallpapers: List<Wallpaper>) {
        wallpapers.forEach { entries[it.id] = it }
    }

    @Synchronized
    fun get(id: Long): Wallpaper? = entries[id]
}
