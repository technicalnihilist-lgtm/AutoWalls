package com.autowalls.app.ui.screens.home

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
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class SectionState(
    val items: List<Wallpaper> = emptyList(),
    val loading: Boolean = false,
    val error: String? = null
)

data class HomeUiState(
    val daily: Wallpaper? = null,
    val dailyLoading: Boolean = true,
    val dailyError: String? = null,
    val featured: SectionState = SectionState(loading = true),
    val trending: SectionState = SectionState(loading = true),
    val latest: SectionState = SectionState(loading = true),
    val motorcycles: SectionState = SectionState(loading = true),
    val luxury: SectionState = SectionState(loading = true),
    val brands: List<VehicleCategory> = VehicleCatalog.popularBrands,
    val recent: List<Wallpaper> = emptyList(),
    val favorites: Set<Long> = emptySet(),
    val refreshing: Boolean = false
)

class HomeViewModel(application: Application) : AndroidViewModel(application) {

    private val repository = (application as AutoWallsApplication).container.repository

    private val _uiState = MutableStateFlow(HomeUiState())
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()

    init {
        observeLocalState()
        refresh()
    }

    private fun observeLocalState() {
        repository.observeFavoriteIds()
            .onEach { ids -> _uiState.update { it.copy(favorites = ids) } }
            .launchIn(viewModelScope)

        repository.observeRecents(limit = 10)
            .onEach { items -> _uiState.update { it.copy(recent = items) } }
            .launchIn(viewModelScope)
    }

    /** Always hits the network — the feed is never served from a stale bundle. */
    fun refresh() {
        val online = NetworkUtils.isOnline(getApplication())

        _uiState.update {
            it.copy(
                refreshing = true,
                dailyLoading = true,
                dailyError = null,
                featured = it.featured.copy(loading = true, error = null),
                trending = it.trending.copy(loading = true, error = null),
                latest = it.latest.copy(loading = true, error = null),
                motorcycles = it.motorcycles.copy(loading = true, error = null),
                luxury = it.luxury.copy(loading = true, error = null)
            )
        }

        viewModelScope.launch {
            val daily = async { repository.dailyWallpaper() }
            val featured = async { repository.loadCategory(VehicleCatalog.featuredCollection, limit = 12) }
            val trending = async { repository.loadCategory(VehicleCatalog.trendingCollection, limit = 12) }
            val latest = async { repository.loadCategory(VehicleCatalog.latestCollection, limit = 12) }
            val motorcycles = async { repository.loadCategory(VehicleCatalog.motorcycleCollection, limit = 12) }
            val luxury = async { repository.loadCategory(VehicleCatalog.luxuryCollection, limit = 12) }

            daily.await()
                .onSuccess { wallpaper ->
                    WallpaperCache.put(wallpaper)
                    _uiState.update { it.copy(daily = wallpaper, dailyLoading = false) }
                }
                .onFailure { error ->
                    _uiState.update {
                        it.copy(
                            dailyLoading = false,
                            dailyError = ErrorMapper.message(error, online)
                        )
                    }
                }

            _uiState.update {
                it.copy(
                    featured = featured.await().toSection(online),
                    trending = trending.await().toSection(online),
                    latest = latest.await().toSection(online),
                    motorcycles = motorcycles.await().toSection(online),
                    luxury = luxury.await().toSection(online),
                    refreshing = false
                )
            }
        }
    }

    private fun Result<com.autowalls.app.data.repository.WallpaperPage>.toSection(
        online: Boolean
    ): SectionState = fold(
        onSuccess = { page ->
            WallpaperCache.putAll(page.items)
            SectionState(items = page.items, loading = false)
        },
        onFailure = { error ->
            SectionState(loading = false, error = ErrorMapper.message(error, online))
        }
    )

    fun toggleFavorite(wallpaper: Wallpaper) {
        viewModelScope.launch { repository.toggleFavorite(wallpaper) }
    }
}
