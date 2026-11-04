package com.autowalls.app.ui.screens.detail

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.autowalls.app.AutoWallsApplication
import com.autowalls.app.data.download.DownloadService
import com.autowalls.app.data.download.DownloadState
import com.autowalls.app.data.download.DownloadTracker
import com.autowalls.app.domain.model.Wallpaper
import com.autowalls.app.ui.WallpaperCache
import com.autowalls.app.util.ErrorMapper
import com.autowalls.app.util.GallerySaver
import com.autowalls.app.util.NetworkUtils
import com.autowalls.app.util.ShareUtils
import com.autowalls.app.util.WallpaperSetter
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.io.File

data class DetailUiState(
    val wallpaper: Wallpaper? = null,
    val loading: Boolean = true,
    val error: String? = null,
    val isFavorite: Boolean = false,
    val download: DownloadState = DownloadState.Idle,
    val localPath: String? = null,
    val applying: WallpaperSetter.Target? = null,
    val message: String? = null,
    val savingToGallery: Boolean = false
)

class DetailViewModel(
    application: Application,
    private val wallpaperId: Long
) : AndroidViewModel(application) {

    private val repository = (application as AutoWallsApplication).container.repository

    private val _uiState = MutableStateFlow(DetailUiState())
    val uiState: StateFlow<DetailUiState> = _uiState.asStateFlow()

    init {
        load()
        observeDownload()
        observeFavorite()
    }

    private fun load() {
        val cached = WallpaperCache.get(wallpaperId)
        if (cached != null) {
            _uiState.update { it.copy(wallpaper = cached, loading = false, error = null) }
            viewModelScope.launch {
                repository.recordView(cached)
                refreshLocalState(cached)
            }
            return
        }

        viewModelScope.launch {
            val online = NetworkUtils.isOnline(getApplication())
            repository.loadById(wallpaperId)
                .onSuccess { wallpaper ->
                    WallpaperCache.put(wallpaper)
                    _uiState.update {
                        it.copy(wallpaper = wallpaper, loading = false, error = null)
                    }
                    repository.recordView(wallpaper)
                    refreshLocalState(wallpaper)
                }
                .onFailure { error ->
                    _uiState.update {
                        it.copy(loading = false, error = ErrorMapper.message(error, online))
                    }
                }
        }
    }

    private suspend fun refreshLocalState(wallpaper: Wallpaper) {
        val favorite = repository.isFavorite(wallpaper.id)
        val path = repository.localPath(wallpaper.id)
        _uiState.update {
            it.copy(
                isFavorite = favorite,
                localPath = path,
                download = if (path != null) DownloadState.Completed(path) else DownloadState.Idle
            )
        }
    }

    private fun observeDownload() {
        DownloadTracker.states
            .onEach { states ->
                val state = states[wallpaperId] ?: return@onEach
                _uiState.update { current ->
                    current.copy(
                        download = state,
                        localPath = (state as? DownloadState.Completed)?.localPath
                            ?: current.localPath
                    )
                }
                if (state is DownloadState.Failed) {
                    _uiState.update { it.copy(message = state.message) }
                    DownloadTracker.clear(wallpaperId)
                }
            }
            .launchIn(viewModelScope)
    }

    private fun observeFavorite() {
        repository.observeFavoriteIds()
            .onEach { ids -> _uiState.update { it.copy(isFavorite = ids.contains(wallpaperId)) } }
            .launchIn(viewModelScope)
    }

    fun toggleFavorite() {
        val wallpaper = _uiState.value.wallpaper ?: return
        viewModelScope.launch {
            val nowFavorite = repository.toggleFavorite(wallpaper)
            _uiState.update {
                it.copy(
                    isFavorite = nowFavorite,
                    message = if (nowFavorite) "Added to favourites" else "Removed from favourites"
                )
            }
        }
    }

    /** Starts the foreground download service; progress returns via the tracker. */
    fun download() {
        val wallpaper = _uiState.value.wallpaper ?: return
        when (_uiState.value.download) {
            is DownloadState.InProgress -> return
            is DownloadState.Completed -> {
                _uiState.update { it.copy(message = "Already downloaded") }
                return
            }
            else -> Unit
        }
        DownloadService.start(getApplication(), wallpaper)
    }

    fun setAsWallpaper(target: WallpaperSetter.Target) {
        val wallpaper = _uiState.value.wallpaper ?: return
        if (_uiState.value.applying != null) return

        _uiState.update { it.copy(applying = target, message = null) }
        viewModelScope.launch {
            // Full resolution is required for a crisp result, so fetch it first.
            val file: File = repository.localPath(wallpaper.id)?.let { File(it) }
                ?: repository.download(wallpaper) { percent ->
                    _uiState.update { it.copy(download = DownloadState.InProgress(percent)) }
                }.getOrElse { error ->
                    _uiState.update {
                        it.copy(
                            applying = null,
                            message = "Could not download the image: " +
                                (error.message ?: "unknown error")
                        )
                    }
                    return@launch
                }

            _uiState.update { it.copy(localPath = file.absolutePath) }

            WallpaperSetter.apply(getApplication(), file, target)
                .onSuccess {
                    _uiState.update {
                        it.copy(
                            applying = null,
                            download = DownloadState.Completed(file.absolutePath),
                            message = "Set as ${target.label.lowercase()}"
                        )
                    }
                }
                .onFailure { error ->
                    _uiState.update {
                        it.copy(
                            applying = null,
                            message = error.message ?: "Could not set the wallpaper"
                        )
                    }
                }
        }
    }

    fun saveToGallery() {
        val wallpaper = _uiState.value.wallpaper ?: return
        if (_uiState.value.savingToGallery) return
        _uiState.update { it.copy(savingToGallery = true) }
        viewModelScope.launch {
            val file: File = repository.localPath(wallpaper.id)?.let { File(it) }
                ?: repository.download(wallpaper).getOrElse { error ->
                    _uiState.update {
                        it.copy(
                            savingToGallery = false,
                            message = "Download failed: ${error.message ?: "unknown error"}"
                        )
                    }
                    return@launch
                }

            GallerySaver.save(getApplication(), file, wallpaper.displayName)
                .onSuccess {
                    _uiState.update {
                        it.copy(
                            savingToGallery = false,
                            localPath = file.absolutePath,
                            download = DownloadState.Completed(file.absolutePath),
                            message = "Saved to gallery in Pictures/AutoWalls"
                        )
                    }
                }
                .onFailure { error ->
                    _uiState.update {
                        it.copy(
                            savingToGallery = false,
                            message = error.message ?: "Could not save to gallery"
                        )
                    }
                }
        }
    }

    fun share() {
        val wallpaper = _uiState.value.wallpaper ?: return
        val file = _uiState.value.localPath?.let { File(it) }
        ShareUtils.shareWallpaper(getApplication(), wallpaper, file)
    }

    fun copyAttribution() {
        val wallpaper = _uiState.value.wallpaper ?: return
        ShareUtils.copyAttribution(getApplication(), wallpaper)
        _uiState.update { it.copy(message = "Attribution copied") }
    }

    fun consumeMessage() {
        _uiState.update { it.copy(message = null) }
    }

    fun retry() {
        _uiState.update { it.copy(loading = true, error = null) }
        load()
    }
}
