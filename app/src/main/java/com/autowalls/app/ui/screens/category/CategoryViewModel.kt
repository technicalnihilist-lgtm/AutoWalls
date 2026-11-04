package com.autowalls.app.ui.screens.category

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.autowalls.app.AutoWallsApplication
import com.autowalls.app.domain.model.VehicleCatalog
import com.autowalls.app.domain.model.VehicleCategory
import com.autowalls.app.domain.model.Wallpaper
import com.autowalls.app.ui.WallpaperCache
import com.autowalls.app.util.ErrorMapper
import com.autowalls.app.util.NetworkUtils
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class CategoryUiState(
    val category: VehicleCategory? = null,
    val items: List<Wallpaper> = emptyList(),
    val loading: Boolean = false,
    val loadingMore: Boolean = false,
    val error: String? = null,
    val endReached: Boolean = false,
    val favorites: Set<Long> = emptySet(),
    val totalLoaded: Int = 0
)

class CategoryViewModel(
    application: Application,
    private val categoryId: String
) : AndroidViewModel(application) {

    private val repository = (application as AutoWallsApplication).container.repository

    private val _uiState = MutableStateFlow(
        CategoryUiState(category = VehicleCatalog.byId(categoryId), loading = true)
    )
    val uiState: StateFlow<CategoryUiState> = _uiState.asStateFlow()

    private var nextOffset: Int? = null
    private val seenIds = mutableSetOf<Long>()

    init {
        repository.observeFavoriteIds()
            .onEach { ids -> _uiState.update { it.copy(favorites = ids) } }
            .launchIn(viewModelScope)
        load(reset = true)
    }

    fun load(reset: Boolean = false) {
        val category = _uiState.value.category ?: return
        if (_uiState.value.loading || _uiState.value.loadingMore) return

        if (reset) {
            seenIds.clear()
            nextOffset = null
            _uiState.update {
                it.copy(items = emptyList(), loading = true, error = null, endReached = false)
            }
        } else {
            if (_uiState.value.endReached) return
            _uiState.update { it.copy(loadingMore = true) }
        }

        viewModelScope.launch {
            val online = NetworkUtils.isOnline(getApplication())
            repository.loadCategory(
                category = category,
                offset = if (reset) null else nextOffset
            )
                .onSuccess { page ->
                    // Duplicate prevention: Commons can repeat a page across offsets.
                    val fresh = page.items.filter { seenIds.add(it.id) }
                    WallpaperCache.putAll(fresh)
                    nextOffset = page.nextOffset
                    _uiState.update { state ->
                        state.copy(
                            items = if (reset) fresh else state.items + fresh,
                            loading = false,
                            loadingMore = false,
                            error = null,
                            endReached = page.nextOffset == null || fresh.isEmpty(),
                            totalLoaded = if (reset) fresh.size else state.totalLoaded + fresh.size
                        )
                    }
                }
                .onFailure { error ->
                    _uiState.update {
                        it.copy(
                            loading = false,
                            loadingMore = false,
                            error = ErrorMapper.message(error, online)
                        )
                    }
                }
        }
    }

    fun loadMore() {
        if (nextOffset == null) {
            _uiState.update { it.copy(endReached = true) }
            return
        }
        load(reset = false)
    }

    fun toggleFavorite(wallpaper: Wallpaper) {
        viewModelScope.launch { repository.toggleFavorite(wallpaper) }
    }
}
