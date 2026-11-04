package com.autowalls.app.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(
    entities = [FavoriteEntity::class, DownloadEntity::class, RecentEntity::class],
    version = 1,
    exportSchema = true
)
abstract class AutoWallsDatabase : RoomDatabase() {

    abstract fun wallpaperDao(): WallpaperDao

    companion object {
        @Volatile
        private var instance: AutoWallsDatabase? = null

        fun get(context: Context): AutoWallsDatabase =
            instance ?: synchronized(this) {
                instance ?: Room.databaseBuilder(
                    context.applicationContext,
                    AutoWallsDatabase::class.java,
                    "autowalls.db"
                )
                    .fallbackToDestructiveMigration()
                    .build()
                    .also { instance = it }
            }
    }
}
