package com.autowalls.app.data.local

import androidx.room.Embedded
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.autowalls.app.domain.model.Wallpaper

/** Flattened wallpaper columns shared by every table. */
data class WallpaperColumns(
    val id: Long,
    val title: String,
    val displayName: String,
    val fullUrl: String,
    val previewUrl: String,
    val thumbUrl: String,
    val width: Int,
    val height: Int,
    val mime: String,
    val fileSizeBytes: Long,
    val artist: String,
    val credit: String,
    val description: String,
    val licenseName: String,
    val licenseUrl: String,
    val sourcePage: String,
    val uploadedBy: String?,
    val dateOriginal: String?,
    val categoryId: String,
    val categoryName: String
)

fun Wallpaper.toColumns(): WallpaperColumns = WallpaperColumns(
    id = id,
    title = title,
    displayName = displayName,
    fullUrl = fullUrl,
    previewUrl = previewUrl,
    thumbUrl = thumbUrl,
    width = width,
    height = height,
    mime = mime,
    fileSizeBytes = fileSizeBytes,
    artist = artist,
    credit = credit,
    description = description,
    licenseName = licenseName,
    licenseUrl = licenseUrl,
    sourcePage = sourcePage,
    uploadedBy = uploadedBy,
    dateOriginal = dateOriginal,
    categoryId = categoryId,
    categoryName = categoryName
)

fun WallpaperColumns.toWallpaper(): Wallpaper = Wallpaper(
    id = id,
    title = title,
    displayName = displayName,
    fullUrl = fullUrl,
    previewUrl = previewUrl,
    thumbUrl = thumbUrl,
    width = width,
    height = height,
    mime = mime,
    fileSizeBytes = fileSizeBytes,
    artist = artist,
    credit = credit,
    description = description,
    licenseName = licenseName,
    licenseUrl = licenseUrl,
    sourcePage = sourcePage,
    uploadedBy = uploadedBy.orEmpty(),
    dateOriginal = dateOriginal.orEmpty(),
    categoryId = categoryId,
    categoryName = categoryName
)

@Entity(tableName = "favorites")
data class FavoriteEntity(
    @PrimaryKey val wallpaperId: Long,
    @Embedded(prefix = "w_") val wallpaper: WallpaperColumns,
    val addedAt: Long
)

@Entity(tableName = "downloads")
data class DownloadEntity(
    @PrimaryKey val wallpaperId: Long,
    @Embedded(prefix = "w_") val wallpaper: WallpaperColumns,
    val localPath: String,
    val downloadedAt: Long
)

@Entity(tableName = "recents")
data class RecentEntity(
    @PrimaryKey val wallpaperId: Long,
    @Embedded(prefix = "w_") val wallpaper: WallpaperColumns,
    val viewedAt: Long
)
