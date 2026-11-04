package com.autowalls.app.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface WallpaperDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertFavorite(entity: FavoriteEntity)

    @Query("DELETE FROM favorites WHERE wallpaperId = :wallpaperId")
    suspend fun deleteFavorite(wallpaperId: Long)

    @Query("SELECT * FROM favorites ORDER BY addedAt DESC")
    fun observeFavorites(): Flow<List<FavoriteEntity>>

    @Query("SELECT COUNT(*) FROM favorites WHERE wallpaperId = :wallpaperId")
    suspend fun favoriteCount(wallpaperId: Long): Int

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertDownload(entity: DownloadEntity)

    @Query("DELETE FROM downloads WHERE wallpaperId = :wallpaperId")
    suspend fun deleteDownload(wallpaperId: Long)

    @Query("SELECT * FROM downloads ORDER BY downloadedAt DESC")
    fun observeDownloads(): Flow<List<DownloadEntity>>

    @Query("SELECT localPath FROM downloads WHERE wallpaperId = :wallpaperId LIMIT 1")
    suspend fun localPath(wallpaperId: Long): String?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertRecent(entity: RecentEntity)

    @Query("SELECT * FROM recents ORDER BY viewedAt DESC LIMIT :limit")
    fun observeRecents(limit: Int): Flow<List<RecentEntity>>

    @Query("DELETE FROM recents")
    suspend fun clearRecents()
}
