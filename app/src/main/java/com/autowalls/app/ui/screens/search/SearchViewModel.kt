package com.autowalls.app.ui.screens.search

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
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class SearchUiState(
    val query: String = "",
    val items: List<Wallpaper> = emptyList(),
    val loading: Boolean = false,
    val loadingMore: Boolean = false,
    val error: String? = null,
    val endReached: Boolean = false,
    val hasSearched: Boolean = false,
    val favorites: Set<Long> = emptySet(),
    val activeFilter: String? = null,
    val filters: List<VehicleCategory> = VehicleCatalog.popularBrands.take(8)
)

@OptIn(FlowPreview::class)
class SearchViewModel(application: Application) : AndroidViewModel(application) {

    private val repository = (application as AutoWallsApplication).container.repository

    private val _uiState = MutableStateFlow(SearchUiState())
    val uiState: StateFlow<SearchUiState> = _uiState.asStateFlow()

    private val queryFlow = MutableStateFlow("")
    private var nextOffset: Int? = null
    private val seenIds = mutableSetOf<Long>()

    init {
        repository.observeFavoriteIds()
            .onEach { ids -> _uiState.update { it.copy(favorites = ids) } }
            .launchIn(viewModelScope)

        queryFlow
            .debounce(350)
            .distinctUntilChanged()
            .onEach { query ->
                if (query.trim().length >= 2) {
                    runSearch(query.trim(), reset = true)
                } else if (query.isBlank()) {
                    seenIds.clear()
                    nextOffset = null
                    _uiState.update {
                        it.copy(
                            items = emptyList(),
                            hasSearched = false,
                            error = null,
                            endReached = false,
                            activeFilter = null
                        )
                    }
                }
            }
            .launchIn(viewModelScope)
    }

    fun onQueryChange(query: String) {
        _uiState.update { it.copy(query = query) }
        queryFlow.value = query
    }

    /** Tapping a brand chip searches that manufacturer directly. */
    fun applyFilter(category: VehicleCategory) {
        _uiState.update { it.copy(activeFilter = category.id, query = category.name) }
        queryFlow.value = category.name
    }

    fun clearFilter() {
        _uiState.update { it.copy(activeFilter = null, query = "") }
        queryFlow.value = ""
    }

    fun submit() {
        val query = _uiState.value.query.trim()
        if (query.length >= 2) runSearch(query, reset = true)
    }

    private fun runSearch(query: String, reset: Boolean) {
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
            repository.searchWallpapers(
                rawQuery = query,
                offset = if (reset) null else nextOffset
            )
                .onSuccess { page ->
                    val fresh = page.items.filter { seenIds.add(it.id) }
                    WallpaperCache.putAll(fresh)
                    nextOffset = page.nextOffset
                    _uiState.update { state ->
                        state.copy(
                            items = if (reset) fresh else state.items + fresh,
                            loading = false,
                            loadingMore = false,
                            error = null,
                            hasSearched = true,
                            endReached = page.nextOffset == null || fresh.isEmpty()
                        )
                    }
                }
                .onFailure { error ->
                    _uiState.update {
                        it.copy(
                            loading = false,
                            loadingMore = false,
                            error = ErrorMapper.message(error, online),
                            hasSearched = true
                        )
                    }
                }
        }
    }

    fun loadMore() {
        val query = _uiState.value.query.trim()
        if (query.length < 2 || nextOffset == null) return
        runSearch(query, reset = false)
    }

    fun retry() {
        val query = _uiState.value.query.trim()
        if (query.length >= 2) runSearch(query, reset = true)
    }

    fun toggleFavorite(wallpaper: Wallpaper) {
        viewModelScope.launch { repository.toggleFavorite(wallpaper) }
    }
}
