package com.autowalls.app.data.download

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

sealed interface DownloadState {
    data object Idle : DownloadState
    data class InProgress(val percent: Int) : DownloadState
    data class Completed(val localPath: String) : DownloadState
    data class Failed(val message: String) : DownloadState
}

/** Process-wide download progress, observed by the detail screen. */
object DownloadTracker {

    private val _states = MutableStateFlow<Map<Long, DownloadState>>(emptyMap())
    val states: StateFlow<Map<Long, DownloadState>> = _states.asStateFlow()

    fun update(wallpaperId: Long, state: DownloadState) {
        _states.update { it + (wallpaperId to state) }
    }

    fun clear(wallpaperId: Long) {
        _states.update { it - wallpaperId }
    }
}
