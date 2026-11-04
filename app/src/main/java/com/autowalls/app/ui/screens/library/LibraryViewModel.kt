package com.autowalls.app.ui.screens.library

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.autowalls.app.AutoWallsApplication
import com.autowalls.app.domain.model.Wallpaper
import com.autowalls.app.ui.WallpaperCache
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch

data class LibraryUiState(
    val favorites: List<Wallpaper> = emptyList(),
    val downloads: List<Wallpaper> = emptyList(),
    val recents: List<Wallpaper> = emptyList(),
    val downloadedIds: Set<Long> = emptySet()
)

class LibraryViewModel(application: Application) : AndroidViewModel(application) {

    private val repository = (application as AutoWallsApplication).container.repository

    private val _uiState = MutableStateFlow(LibraryUiState())
    val uiState: StateFlow<LibraryUiState> = _uiState.asStateFlow()

    init {
        combine(
            repository.observeFavorites(),
            repository.observeDownloads(),
            repository.observeRecents(limit = 24)
        ) { favorites, downloads, recents ->
            val downloaded = downloads.map { it.wallpaper }
            WallpaperCache.putAll(favorites + downloaded + recents)
            LibraryUiState(
                favorites = favorites,
                downloads = downloaded,
                recents = recents,
                downloadedIds = downloaded.map { it.id }.toSet()
            )
        }
            .onEach { state -> _uiState.value = state }
            .launchIn(viewModelScope)
    }

    fun toggleFavorite(wallpaper: Wallpaper) {
        viewModelScope.launch { repository.toggleFavorite(wallpaper) }
    }

    fun removeDownload(wallpaper: Wallpaper) {
        viewModelScope.launch { repository.deleteDownload(wallpaper.id) }
    }

    fun clearRecents() {
        viewModelScope.launch { repository.clearRecents() }
    }
}
