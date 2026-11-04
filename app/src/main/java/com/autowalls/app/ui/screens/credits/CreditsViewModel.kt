package com.autowalls.app.ui.screens.credits

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.autowalls.app.AutoWallsApplication
import com.autowalls.app.domain.model.Wallpaper
import com.autowalls.app.ui.WallpaperCache
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach

data class CreditsUiState(
    val credited: List<Wallpaper> = emptyList()
)

class CreditsViewModel(application: Application) : AndroidViewModel(application) {

    private val repository = (application as AutoWallsApplication).container.repository

    private val _uiState = MutableStateFlow(CreditsUiState())
    val uiState: StateFlow<CreditsUiState> = _uiState.asStateFlow()

    init {
        repository.observeCredits()
            .onEach { items ->
                WallpaperCache.putAll(items)
                _uiState.value = CreditsUiState(credited = items)
            }
            .launchIn(viewModelScope)
    }
}
