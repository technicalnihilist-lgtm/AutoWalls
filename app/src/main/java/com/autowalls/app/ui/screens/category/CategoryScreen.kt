package com.autowalls.app.ui.screens.category

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.grid.rememberLazyGridState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.autowalls.app.R
import com.autowalls.app.domain.model.Wallpaper
import com.autowalls.app.ui.components.EmptyState
import com.autowalls.app.ui.components.ErrorState
import com.autowalls.app.ui.components.GlassSurface
import com.autowalls.app.ui.components.PortraitWallpaperCard
import com.autowalls.app.ui.components.ShimmerGridCell
import com.autowalls.app.ui.theme.AutoWallsColors

@Composable
fun CategoryScreen(
    state: CategoryUiState,
    onBack: () -> Unit,
    onOpenWallpaper: (Wallpaper) -> Unit,
    onToggleFavorite: (Wallpaper) -> Unit,
    onLoadMore: () -> Unit,
    onRetry: () -> Unit,
    modifier: Modifier = Modifier
) {
    val gridState = rememberLazyGridState()

    LaunchedEffect(gridState, state.items.size, state.endReached) {
        snapshotFlow { gridState.layoutInfo.visibleItemsInfo.lastOrNull()?.index ?: -1 }
            .collect { lastVisible ->
                if (!state.endReached && state.items.isNotEmpty() &&
                    lastVisible >= state.items.size - 3
                ) {
                    onLoadMore()
                }
            }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(AutoWallsColors.Black)
            .statusBarsPadding()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onBack) {
                Icon(
                    painter = painterResource(R.drawable.ic_arrow_back),
                    contentDescription = "Back",
                    tint = AutoWallsColors.TextPrimary
                )
            }
            Column(Modifier.weight(1f)) {
                Text(
                    text = state.category?.group?.label?.uppercase() ?: "COLLECTION",
                    style = MaterialTheme.typography.labelSmall,
                    color = AutoWallsColors.Champagne
                )
                Text(
                    text = state.category?.name ?: "Collection",
                    style = MaterialTheme.typography.headlineMedium,
                    color = AutoWallsColors.TextPrimary
                )
            }
            if (state.totalLoaded > 0) {
                Text(
                    text = "${state.totalLoaded} loaded",
                    style = MaterialTheme.typography.labelMedium,
                    color = AutoWallsColors.TextTertiary,
                    modifier = Modifier.padding(end = 14.dp)
                )
            }
        }

        when {
            state.loading && state.items.isEmpty() -> LazyVerticalGrid(
                columns = GridCells.Fixed(2),
                contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 8.dp, bottom = 120.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(6) { ShimmerGridCell() }
            }

            state.error != null && state.items.isEmpty() -> ErrorState(
                message = state.error,
                onRetry = onRetry,
                modifier = Modifier.fillMaxWidth()
            )

            state.items.isEmpty() -> EmptyState(
                icon = painterResource(R.drawable.ic_photo_library),
                title = "Nothing here yet",
                message = "Commons has no free-licensed photographs for this collection right now."
            )

            else -> LazyVerticalGrid(
                state = gridState,
                columns = GridCells.Fixed(2),
                contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 8.dp, bottom = 120.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(state.items, key = { it.id }) { wallpaper ->
                    PortraitWallpaperCard(
                        wallpaper = wallpaper,
                        onClick = { onOpenWallpaper(wallpaper) },
                        isFavorite = state.favorites.contains(wallpaper.id),
                        onFavoriteToggle = { onToggleFavorite(wallpaper) }
                    )
                }

                if (state.loadingMore) {
                    item {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(64.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(22.dp),
                                color = AutoWallsColors.Champagne,
                                strokeWidth = 1.6.dp
                            )
                        }
                    }
                } else if (state.endReached && state.items.isNotEmpty()) {
                    item {
                        Text(
                            text = "END OF COLLECTION · ${state.items.size} WALLPAPERS",
                            style = MaterialTheme.typography.labelSmall,
                            color = AutoWallsColors.TextTertiary,
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 22.dp),
                            textAlign = TextAlign.Center
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun CategoryTile(
    name: String,
    group: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    GlassSurface(
        modifier = modifier
            .height(78.dp)
            .clip(RoundedCornerShape(14.dp))
            .clickable(onClick = onClick)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 14.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = name,
                style = MaterialTheme.typography.titleMedium,
                color = AutoWallsColors.TextPrimary,
                maxLines = 2
            )
            Spacer(Modifier.height(3.dp))
            Text(
                text = group.uppercase(),
                style = MaterialTheme.typography.labelSmall,
                color = AutoWallsColors.TextTertiary
            )
        }
    }
}
