package com.autowalls.app.data.repository

import com.autowalls.app.data.local.DownloadEntity
import com.autowalls.app.data.local.FavoriteEntity
import com.autowalls.app.data.local.RecentEntity
import com.autowalls.app.data.local.WallpaperDao
import com.autowalls.app.data.local.toColumns
import com.autowalls.app.data.local.toWallpaper
import com.autowalls.app.data.remote.WikimediaApi
import com.autowalls.app.data.remote.WikimediaMapper
import com.autowalls.app.domain.model.CategoryGroup
import com.autowalls.app.domain.model.VehicleCatalog
import com.autowalls.app.domain.model.VehicleCategory
import com.autowalls.app.domain.model.Wallpaper
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import java.io.File
import java.io.IOException
import java.time.LocalDate

data class WallpaperPage(val items: List<Wallpaper>, val nextOffset: Int?)

data class DownloadedWallpaper(
    val wallpaper: Wallpaper,
    val localPath: String,
    val downloadedAt: Long
)

/**
 * Single source of truth. Remote data is always live — nothing is bundled,
 * seeded or faked. Room only stores what the user explicitly kept
 * (favourites, downloads, recently viewed).
 */
class WallpaperRepository(
    private val api: WikimediaApi,
    private val dao: WallpaperDao,
    private val downloadClient: OkHttpClient,
    private val picturesDir: File
) {

    private val searchCategory = VehicleCategory("search", "Search", CategoryGroup.OTHER, "")

    // ---- Live catalogue ---------------------------------------------------

    suspend fun loadCategory(
        category: VehicleCategory,
        offset: Int? = null,
        limit: Int = PAGE_SIZE
    ): Result<WallpaperPage> = withContext(Dispatchers.IO) {
        runCatching {
            val response = api.searchImages(
                search = category.query,
                limit = limit,
                offset = offset
            )
            response.error?.let { throw IOException(it.info ?: "Commons API error") }
            val mapped = WikimediaMapper.mapAll(response, category)
            WallpaperPage(
                items = mapped.distinctBy { it.id },
                nextOffset = response.continueToken?.gsrOffset
            )
        }
    }

    suspend fun searchWallpapers(
        rawQuery: String,
        offset: Int? = null,
        limit: Int = PAGE_SIZE
    ): Result<WallpaperPage> = withContext(Dispatchers.IO) {
        val query = rawQuery.trim()
        if (query.length < 2) return@withContext Result.success(WallpaperPage(emptyList(), null))
        runCatching {
            val response = api.searchImages(
                search = "$query filetype:bitmap",
                limit = limit,
                offset = offset
            )
            response.error?.let { throw IOException(it.info ?: "Commons API error") }
            WallpaperPage(
                items = WikimediaMapper.mapAll(response, searchCategory).distinctBy { it.id },
                nextOffset = response.continueToken?.gsrOffset
            )
        }
    }

    /** Re-fetches a single image straight from Commons — used for deep links. */
    suspend fun loadById(pageId: Long): Result<Wallpaper> = withContext(Dispatchers.IO) {
        runCatching {
            val response = api.imageByPageId(pageIds = pageId)
            val page = response.query?.pages?.firstOrNull()
                ?: throw IOException("Image not found on Commons")
            WikimediaMapper.mapPage(page, searchCategory)
                ?: throw IOException("Image is missing a verifiable free license")
        }
    }

    /**
     * Daily wallpaper: rotates through the daily collections and picks a stable
     * index for today. The image itself is fetched live every time.
     */
    suspend fun dailyWallpaper(): Result<Wallpaper> = withContext(Dispatchers.IO) {
        val rotation = VehicleCatalog.dailyRotation
        if (rotation.isEmpty()) return@withContext Result.failure(IOException("No collections"))
        val day = LocalDate.now().toEpochDay()
        val start = (day % rotation.size).toInt()
        var lastError: Throwable? = null

        for (attempt in rotation.indices) {
            val category = rotation[((start + attempt) % rotation.size).toInt()]
            val result = loadCategory(category, offset = null, limit = 30)
            result.onSuccess { page ->
                if (page.items.isNotEmpty()) {
                    val index = ((day * 7) % page.items.size).toInt()
                    return@withContext Result.success(page.items[index])
                }
            }.onFailure { lastError = it }
        }
        Result.failure(lastError ?: IOException("No daily wallpaper available"))
    }

    // ---- Persistence ------------------------------------------------------

    fun observeFavorites(): Flow<List<Wallpaper>> =
        dao.observeFavorites().map { list -> list.map { it.wallpaper.toWallpaper() } }

    fun observeFavoriteIds(): Flow<Set<Long>> =
        dao.observeFavorites().map { list -> list.map { it.wallpaperId }.toSet() }

    fun observeDownloads(): Flow<List<DownloadedWallpaper>> =
        dao.observeDownloads().map { list ->
            list.map {
                DownloadedWallpaper(
                    wallpaper = it.wallpaper.toWallpaper(),
                    localPath = it.localPath,
                    downloadedAt = it.downloadedAt
                )
            }
        }

    fun observeRecents(limit: Int = 20): Flow<List<Wallpaper>> =
        dao.observeRecents(limit).map { list -> list.map { it.wallpaper.toWallpaper() } }

    suspend fun isFavorite(wallpaperId: Long): Boolean = dao.favoriteCount(wallpaperId) > 0

    /** @return true when the wallpaper is now a favourite. */
    suspend fun toggleFavorite(wallpaper: Wallpaper): Boolean = withContext(Dispatchers.IO) {
        if (dao.favoriteCount(wallpaper.id) > 0) {
            dao.deleteFavorite(wallpaper.id)
            false
        } else {
            dao.upsertFavorite(
                FavoriteEntity(
                    wallpaperId = wallpaper.id,
                    wallpaper = wallpaper.toColumns(),
                    addedAt = System.currentTimeMillis()
                )
            )
            true
        }
    }

    suspend fun recordView(wallpaper: Wallpaper) = withContext(Dispatchers.IO) {
        dao.upsertRecent(
            RecentEntity(
                wallpaperId = wallpaper.id,
                wallpaper = wallpaper.toColumns(),
                viewedAt = System.currentTimeMillis()
            )
        )
    }

    suspend fun clearRecents() = withContext(Dispatchers.IO) { dao.clearRecents() }

    suspend fun localPath(wallpaperId: Long): String? = withContext(Dispatchers.IO) {
        dao.localPath(wallpaperId)?.takeIf { File(it).exists() }
    }

    suspend fun isDownloaded(wallpaperId: Long): Boolean = localPath(wallpaperId) != null

    /** Credits for everything the user kept, for the dedicated credits page. */
    fun observeCredits(): Flow<List<Wallpaper>> =
        combine(observeFavorites(), observeDownloads()) { favorites, downloads ->
            (favorites + downloads.map { it.wallpaper })
                .distinctBy { it.id }
                .sortedBy { it.artist.lowercase() }
        }

    // ---- Downloads --------------------------------------------------------

    /**
     * Streams the full-resolution original into app storage so it stays available
     * without a connection. Progress is reported 0..100.
     */
    suspend fun download(
        wallpaper: Wallpaper,
        onProgress: (Int) -> Unit = {}
    ): Result<File> = withContext(Dispatchers.IO) {
        runCatching {
            if (!picturesDir.exists() && !picturesDir.mkdirs()) {
                throw IOException("Cannot create download folder")
            }

            val existing = dao.localPath(wallpaper.id)?.let { File(it) }
            if (existing != null && existing.exists() && existing.length() > 0) {
                onProgress(100)
                return@runCatching existing
            }

            val target = File(picturesDir, fileNameFor(wallpaper))
            val partial = File(picturesDir, "${target.name}.part")
            if (partial.exists()) partial.delete()

            val request = Request.Builder()
                .url(wallpaper.fullUrl)
                .header("User-Agent", USER_AGENT)
                .build()

            downloadClient.newCall(request).execute().use { response ->
                if (!response.isSuccessful) {
                    throw IOException("Download failed (HTTP ${response.code})")
                }
                val body = response.body ?: throw IOException("Empty response body")
                val total = body.contentLength()

                body.byteStream().use { input ->
                    partial.outputStream().use { output ->
                        val buffer = ByteArray(64 * 1024)
                        var written = 0L
                        var lastPercent = -1
                        while (true) {
                            val read = input.read(buffer)
                            if (read <= 0) break
                            output.write(buffer, 0, read)
                            written += read
                            if (total > 0) {
                                val percent = ((written * 100) / total).toInt().coerceIn(0, 100)
                                if (percent != lastPercent) {
                                    lastPercent = percent
                                    onProgress(percent)
                                }
                            }
                        }
                        output.flush()
                    }
                }
            }

            if (!partial.renameTo(target)) {
                partial.copyTo(target, overwrite = true)
                partial.delete()
            }
            if (target.length() <= 0) throw IOException("Downloaded file is empty")

            dao.upsertDownload(
                DownloadEntity(
                    wallpaperId = wallpaper.id,
                    wallpaper = wallpaper.toColumns(),
                    localPath = target.absolutePath,
                    downloadedAt = System.currentTimeMillis()
                )
            )
            onProgress(100)
            target
        }
    }

    suspend fun deleteDownload(wallpaperId: Long): Boolean = withContext(Dispatchers.IO) {
        val path = dao.localPath(wallpaperId)
        dao.deleteDownload(wallpaperId)
        path?.let { runCatching { File(it).delete() } }
        true
    }

    private fun fileNameFor(wallpaper: Wallpaper): String {
        val base = wallpaper.displayName
            .replace(Regex("[^A-Za-z0-9 _-]"), "")
            .trim()
            .replace(' ', '_')
            .take(70)
            .ifBlank { "autowalls" }
        val ext = if (wallpaper.mime.contains("png")) "png" else "jpg"
        return "${base}_${wallpaper.id}.$ext"
    }

    companion object {
        const val PAGE_SIZE = 30

        private const val USER_AGENT =
            "AutoWalls/1.0 (Android wallpaper app; contact: nihaonihao54059@gmail.com)"
    }
}
