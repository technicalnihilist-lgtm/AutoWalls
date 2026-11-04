package com.autowalls.app.ui.screens.search

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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.grid.rememberLazyGridState
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
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
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.autowalls.app.R
import com.autowalls.app.domain.model.VehicleCategory
import com.autowalls.app.domain.model.Wallpaper
import com.autowalls.app.ui.components.EmptyState
import com.autowalls.app.ui.components.ErrorState
import com.autowalls.app.ui.components.GlassSurface
import com.autowalls.app.ui.components.PillChip
import com.autowalls.app.ui.components.PortraitWallpaperCard
import com.autowalls.app.ui.components.ShimmerGridCell
import com.autowalls.app.ui.theme.AutoWallsColors

@Composable
fun SearchScreen(
    state: SearchUiState,
    onQueryChange: (String) -> Unit,
    onSubmit: () -> Unit,
    onFilter: (VehicleCategory) -> Unit,
    onClearFilter: () -> Unit,
    onBack: () -> Unit,
    onOpenWallpaper: (Wallpaper) -> Unit,
    onToggleFavorite: (Wallpaper) -> Unit,
    onLoadMore: () -> Unit,
    onRetry: () -> Unit,
    modifier: Modifier = Modifier
) {
    val gridState = rememberLazyGridState()
    val keyboard = LocalSoftwareKeyboardController.current

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
                .padding(start = 4.dp, end = 16.dp, top = 6.dp, bottom = 6.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onBack) {
                Icon(
                    painter = painterResource(R.drawable.ic_arrow_back),
                    contentDescription = "Back",
                    tint = AutoWallsColors.TextPrimary
                )
            }

            GlassSurface(
                modifier = Modifier
                    .weight(1f)
                    .height(46.dp),
                shape = RoundedCornerShape(13.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 14.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        painter = painterResource(R.drawable.ic_search),
                        contentDescription = null,
                        tint = AutoWallsColors.TextTertiary,
                        modifier = Modifier.size(17.dp)
                    )
                    Spacer(Modifier.width(10.dp))
                    BasicTextField(
                        value = state.query,
                        onValueChange = onQueryChange,
                        singleLine = true,
                        textStyle = MaterialTheme.typography.bodyLarge.copy(
                            color = AutoWallsColors.TextPrimary
                        ),
                        cursorBrush = SolidColor(AutoWallsColors.Champagne),
                        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
                        keyboardActions = KeyboardActions(
                            onSearch = {
                                onSubmit()
                                keyboard?.hide()
                            }
                        ),
                        modifier = Modifier.weight(1f),
                        decorationBox = { innerTextField ->
                            Box(Modifier.fillMaxWidth()) {
                                if (state.query.isEmpty()) {
                                    Text(
                                        text = "Search vehicle or brand",
                                        style = MaterialTheme.typography.bodyLarge,
                                        color = AutoWallsColors.TextTertiary
                                    )
                                }
                                innerTextField()
                            }
                        }
                    )
                    if (state.query.isNotEmpty()) {
                        Icon(
                            painter = painterResource(R.drawable.ic_close),
                            contentDescription = "Clear search",
                            tint = AutoWallsColors.TextTertiary,
                            modifier = Modifier
                                .size(18.dp)
                                .clip(RoundedCornerShape(50))
                                .clickable { onClearFilter() }
                        )
                    }
                }
            }
        }

        LazyRow(
            contentPadding = PaddingValues(horizontal = 20.dp, vertical = 6.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(state.filters, key = { it.id }) { filter ->
                PillChip(
                    text = filter.name,
                    selected = state.activeFilter == filter.id,
                    onClick = { onFilter(filter) }
                )
            }
        }

        Box(Modifier.weight(1f)) {
            when {
                state.loading && state.items.isEmpty() -> LazyVerticalGrid(
                    columns = GridCells.Fixed(2),
                    contentPadding = PaddingValues(16.dp),
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

                !state.hasSearched -> EmptyState(
                    icon = painterResource(R.drawable.ic_search),
                    title = "Search the garage",
                    message = "Try “Aventador”, “Ducati Panigale”, “G-Class” or any manufacturer. " +
                        "Results come straight from Wikimedia Commons."
                )

                state.items.isEmpty() -> EmptyState(
                    icon = painterResource(R.drawable.ic_search_off),
                    title = "No matches",
                    message = "Nothing free-licensed matched “${state.query}”. " +
                        "Try a shorter term or a manufacturer name."
                )

                else -> LazyVerticalGrid(
                    state = gridState,
                    columns = GridCells.Fixed(2),
                    contentPadding = PaddingValues(16.dp),
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
                                text = "${state.items.size} RESULTS",
                                style = MaterialTheme.typography.labelSmall,
                                color = AutoWallsColors.TextTertiary,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 20.dp),
                                textAlign = TextAlign.Center
                            )
                        }
                    }
                }
            }
        }
    }
}
