package com.autowalls.app.ui.screens.library

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
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.autowalls.app.R
import com.autowalls.app.domain.model.Wallpaper
import com.autowalls.app.ui.components.EmptyState
import com.autowalls.app.ui.components.PillChip
import com.autowalls.app.ui.components.PortraitWallpaperCard
import com.autowalls.app.ui.theme.AutoWallsColors

private enum class LibraryTab(val label: String) {
    FAVORITES("Favourites"),
    DOWNLOADS("Offline"),
    RECENT("Recent")
}

@Composable
fun LibraryScreen(
    state: LibraryUiState,
    onOpenWallpaper: (Wallpaper) -> Unit,
    onToggleFavorite: (Wallpaper) -> Unit,
    onRemoveDownload: (Wallpaper) -> Unit,
    onClearRecents: () -> Unit,
    modifier: Modifier = Modifier
) {
    var selectedTab by remember { mutableIntStateOf(0) }
    val tab = LibraryTab.entries[selectedTab]

    val items: List<Wallpaper> = when (tab) {
        LibraryTab.FAVORITES -> state.favorites
        LibraryTab.DOWNLOADS -> state.downloads
        LibraryTab.RECENT -> state.recents
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(AutoWallsColors.Black)
            .statusBarsPadding()
    ) {
        Column(Modifier.padding(horizontal = 20.dp, vertical = 12.dp)) {
            Text(
                text = "YOUR COLLECTION",
                style = MaterialTheme.typography.labelSmall,
                color = AutoWallsColors.Champagne
            )
            Text(
                text = "Saved",
                style = MaterialTheme.typography.headlineLarge,
                color = AutoWallsColors.TextPrimary
            )
        }

        LazyRow(
            contentPadding = PaddingValues(horizontal = 20.dp, vertical = 6.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(LibraryTab.entries.toList(), key = { it.name }) { entry ->
                PillChip(
                    text = entry.label,
                    selected = entry == tab,
                    onClick = { selectedTab = LibraryTab.entries.indexOf(entry) }
                )
            }
        }

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "${items.size} ${if (items.size == 1) "wallpaper" else "wallpapers"}",
                style = MaterialTheme.typography.labelMedium,
                color = AutoWallsColors.TextTertiary
            )
            Spacer(Modifier.weight(1f))
            if (tab == LibraryTab.RECENT && state.recents.isNotEmpty()) {
                Text(
                    text = "Clear history",
                    style = MaterialTheme.typography.labelMedium,
                    color = AutoWallsColors.ChampagneBright,
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .clickable(onClick = onClearRecents)
                        .padding(horizontal = 8.dp, vertical = 5.dp)
                )
            }
        }

        Box(Modifier.weight(1f)) {
            if (items.isEmpty()) {
                EmptyState(
                    icon = painterResource(
                        when (tab) {
                            LibraryTab.FAVORITES -> R.drawable.ic_favorite_border
                            LibraryTab.DOWNLOADS -> R.drawable.ic_download_done
                            LibraryTab.RECENT -> R.drawable.ic_history
                        }
                    ),
                    title = when (tab) {
                        LibraryTab.FAVORITES -> "No favourites yet"
                        LibraryTab.DOWNLOADS -> "Nothing downloaded"
                        LibraryTab.RECENT -> "Nothing viewed yet"
                    },
                    message = when (tab) {
                        LibraryTab.FAVORITES ->
                            "Tap the heart on any wallpaper to keep it here."
                        LibraryTab.DOWNLOADS ->
                            "Download a wallpaper to view and set it without a connection."
                        LibraryTab.RECENT ->
                            "Wallpapers you open will appear here."
                    }
                )
            } else {
                LazyVerticalGrid(
                    columns = GridCells.Fixed(2),
                    contentPadding = PaddingValues(start = 16.dp, end = 16.dp, bottom = 120.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(items, key = { "${tab.name}-${it.id}" }) { wallpaper ->
                        Column {
                            PortraitWallpaperCard(
                                wallpaper = wallpaper,
                                onClick = { onOpenWallpaper(wallpaper) },
                                isFavorite = state.favorites.any { it.id == wallpaper.id },
                                onFavoriteToggle = { onToggleFavorite(wallpaper) }
                            )
                            if (tab == LibraryTab.DOWNLOADS) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clip(RoundedCornerShape(6.dp))
                                        .clickable { onRemoveDownload(wallpaper) }
                                        .padding(vertical = 6.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(
                                        painter = painterResource(R.drawable.ic_delete),
                                        contentDescription = "Remove download",
                                        tint = AutoWallsColors.TextTertiary,
                                        modifier = Modifier.size(13.dp)
                                    )
                                    Spacer(Modifier.width(4.dp))
                                    Text(
                                        text = "Remove",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = AutoWallsColors.TextTertiary
                                    )
                                }
                            }
                        }
                    }

                    if (tab == LibraryTab.DOWNLOADS) {
                        item {
                            Text(
                                text = "Offline copies live in app storage. Use “Save to gallery” on a " +
                                    "wallpaper to also keep it in Pictures.",
                                style = MaterialTheme.typography.bodySmall,
                                color = AutoWallsColors.TextTertiary,
                                textAlign = TextAlign.Start,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 12.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}
