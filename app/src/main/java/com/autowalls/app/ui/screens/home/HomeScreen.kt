package com.autowalls.app.ui.screens.home

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.autowalls.app.R
import com.autowalls.app.domain.model.VehicleCatalog
import com.autowalls.app.domain.model.VehicleCategory
import com.autowalls.app.domain.model.Wallpaper
import com.autowalls.app.ui.components.AutoWallsLogo
import com.autowalls.app.ui.components.BrandCard
import com.autowalls.app.ui.components.ErrorState
import com.autowalls.app.ui.components.GlassSurface
import com.autowalls.app.ui.components.LicenseBadge
import com.autowalls.app.ui.components.MetallicDivider
import com.autowalls.app.ui.components.PortraitWallpaperCard
import com.autowalls.app.ui.components.SectionHeader
import com.autowalls.app.ui.components.ShimmerBlock
import com.autowalls.app.ui.components.ShimmerGridCell
import com.autowalls.app.ui.components.ShimmerRailCard
import com.autowalls.app.ui.components.WallpaperImage
import com.autowalls.app.ui.components.WideWallpaperCard
import com.autowalls.app.ui.theme.AutoWallsColors

@Composable
fun HomeScreen(
    state: HomeUiState,
    onRefresh: () -> Unit,
    onOpenSearch: () -> Unit,
    onOpenWallpaper: (Wallpaper) -> Unit,
    onOpenCategory: (VehicleCategory) -> Unit,
    onOpenCategories: () -> Unit,
    onOpenCredits: () -> Unit,
    onToggleFavorite: (Wallpaper) -> Unit,
    modifier: Modifier = Modifier
) {
    val rotation by animateFloatAsState(
        targetValue = if (state.refreshing) 360f else 0f,
        animationSpec = tween(900),
        label = "refreshRotation"
    )

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(AutoWallsColors.Black)
            .statusBarsPadding(),
        contentPadding = PaddingValues(bottom = 120.dp)
    ) {
        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 14.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                AutoWallsLogo()
                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconButton(onClick = onOpenSearch) {
                        Icon(
                            painter = painterResource(R.drawable.ic_search),
                            contentDescription = "Search wallpapers",
                            tint = AutoWallsColors.TextSecondary
                        )
                    }
                    IconButton(onClick = onRefresh) {
                        Icon(
                            painter = painterResource(R.drawable.ic_refresh),
                            contentDescription = "Refresh live feed",
                            tint = if (state.refreshing) AutoWallsColors.Champagne
                            else AutoWallsColors.TextSecondary,
                            modifier = Modifier.rotate(rotation)
                        )
                    }
                }
            }
        }

        item {
            SearchEntry(onClick = onOpenSearch, modifier = Modifier.padding(horizontal = 20.dp))
        }

        item { Spacer(Modifier.height(24.dp)) }

        item {
            DailyWallpaperHero(
                wallpaper = state.daily,
                loading = state.dailyLoading,
                error = state.dailyError,
                onOpen = onOpenWallpaper,
                onRetry = onRefresh,
                modifier = Modifier.padding(horizontal = 20.dp)
            )
        }

        item {
            Spacer(Modifier.height(30.dp))
            SectionHeader(
                eyebrow = "Curated",
                title = "Featured supercars",
                actionLabel = "See all",
                onActionClick = { onOpenCategory(VehicleCatalog.featuredCollection) },
                modifier = Modifier.padding(horizontal = 20.dp)
            )
            Spacer(Modifier.height(14.dp))
            WallpaperRail(
                section = state.featured,
                onOpenWallpaper = onOpenWallpaper,
                onRetry = onRefresh
            )
        }

        item {
            Spacer(Modifier.height(30.dp))
            SectionHeader(
                eyebrow = "Live from Commons",
                title = "Trending vehicles",
                actionLabel = "See all",
                onActionClick = { onOpenCategory(VehicleCatalog.trendingCollection) },
                modifier = Modifier.padding(horizontal = 20.dp)
            )
            Spacer(Modifier.height(14.dp))
            WallpaperRail(
                section = state.trending,
                onOpenWallpaper = onOpenWallpaper,
                onRetry = onRefresh
            )
        }

        item {
            Spacer(Modifier.height(30.dp))
            SectionHeader(
                eyebrow = "Manufacturers",
                title = "Popular brands",
                actionLabel = "All categories",
                onActionClick = onOpenCategories,
                modifier = Modifier.padding(horizontal = 20.dp)
            )
            Spacer(Modifier.height(14.dp))
            LazyRow(
                contentPadding = PaddingValues(horizontal = 20.dp),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                items(state.brands, key = { it.id }) { brand ->
                    BrandCard(category = brand, onClick = { onOpenCategory(brand) })
                }
            }
        }

        item {
            Spacer(Modifier.height(30.dp))
            SectionHeader(
                eyebrow = "Freshly added",
                title = "Latest wallpapers",
                actionLabel = "See all",
                onActionClick = { onOpenCategory(VehicleCatalog.latestCollection) },
                modifier = Modifier.padding(horizontal = 20.dp)
            )
            Spacer(Modifier.height(14.dp))
            val latest = state.latest
            when {
                latest.loading -> Row(
                    modifier = Modifier.padding(horizontal = 20.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    ShimmerGridCell(Modifier.weight(1f))
                    ShimmerGridCell(Modifier.weight(1f))
                }

                latest.error != null -> ErrorState(
                    message = latest.error,
                    onRetry = onRefresh,
                    modifier = Modifier.fillMaxWidth()
                )

                latest.items.isNotEmpty() -> LazyRow(
                    contentPadding = PaddingValues(horizontal = 20.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(latest.items.take(10), key = { it.id }) { wallpaper ->
                        PortraitWallpaperCard(
                            wallpaper = wallpaper,
                            onClick = { onOpenWallpaper(wallpaper) },
                            modifier = Modifier.width(158.dp),
                            isFavorite = state.favorites.contains(wallpaper.id),
                            onFavoriteToggle = { onToggleFavorite(wallpaper) }
                        )
                    }
                }
            }
        }

        item {
            Spacer(Modifier.height(30.dp))
            SectionHeader(
                eyebrow = "Two wheels",
                title = "Motorcycle collection",
                actionLabel = "See all",
                onActionClick = { onOpenCategory(VehicleCatalog.motorcycleCollection) },
                modifier = Modifier.padding(horizontal = 20.dp)
            )
            Spacer(Modifier.height(14.dp))
            WallpaperRail(
                section = state.motorcycles,
                onOpenWallpaper = onOpenWallpaper,
                onRetry = onRefresh
            )
        }

        item {
            Spacer(Modifier.height(30.dp))
            SectionHeader(
                eyebrow = "Above the ordinary",
                title = "Luxury collection",
                actionLabel = "See all",
                onActionClick = { onOpenCategory(VehicleCatalog.luxuryCollection) },
                modifier = Modifier.padding(horizontal = 20.dp)
            )
            Spacer(Modifier.height(14.dp))
            WallpaperRail(
                section = state.luxury,
                onOpenWallpaper = onOpenWallpaper,
                onRetry = onRefresh
            )
        }

        if (state.recent.isNotEmpty()) {
            item {
                Spacer(Modifier.height(30.dp))
                SectionHeader(
                    eyebrow = "Pick up where you left",
                    title = "Recently viewed",
                    modifier = Modifier.padding(horizontal = 20.dp)
                )
                Spacer(Modifier.height(14.dp))
                LazyRow(
                    contentPadding = PaddingValues(horizontal = 20.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(state.recent, key = { "recent-${it.id}" }) { wallpaper ->
                        PortraitWallpaperCard(
                            wallpaper = wallpaper,
                            onClick = { onOpenWallpaper(wallpaper) },
                            modifier = Modifier.width(132.dp)
                        )
                    }
                }
            }
        }

        item {
            Spacer(Modifier.height(34.dp))
            MetallicDivider(
                modifier = Modifier
                    .padding(horizontal = 20.dp)
                    .height(1.dp)
                    .fillMaxWidth()
            )
            Spacer(Modifier.height(18.dp))
            Column(Modifier.padding(horizontal = 20.dp)) {
                Text(
                    text = "Every photograph is loaded live from Wikimedia Commons with its " +
                        "photographer and license attached. AutoWalls does not own these images.",
                    style = MaterialTheme.typography.bodySmall,
                    color = AutoWallsColors.TextTertiary
                )
                Spacer(Modifier.height(10.dp))
                Text(
                    text = "View all credits",
                    style = MaterialTheme.typography.labelLarge,
                    color = AutoWallsColors.ChampagneBright,
                    modifier = Modifier
                        .clip(RoundedCornerShape(6.dp))
                        .clickable(onClick = onOpenCredits)
                )
            }
        }
    }
}

@Composable
private fun WallpaperRail(
    section: SectionState,
    onOpenWallpaper: (Wallpaper) -> Unit,
    onRetry: () -> Unit
) {
    when {
        section.loading -> LazyRow(
            contentPadding = PaddingValues(horizontal = 20.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items(3) { ShimmerRailCard() }
        }

        section.error != null -> ErrorState(
            message = section.error,
            onRetry = onRetry,
            modifier = Modifier.fillMaxWidth()
        )

        section.items.isEmpty() -> Text(
            text = "No images available in this collection right now.",
            style = MaterialTheme.typography.bodyMedium,
            color = AutoWallsColors.TextTertiary,
            modifier = Modifier.padding(horizontal = 20.dp)
        )

        else -> LazyRow(
            contentPadding = PaddingValues(horizontal = 20.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items(section.items, key = { it.id }) { wallpaper ->
                WideWallpaperCard(wallpaper = wallpaper, onClick = { onOpenWallpaper(wallpaper) })
            }
        }
    }
}

@Composable
private fun SearchEntry(onClick: () -> Unit, modifier: Modifier = Modifier) {
    GlassSurface(
        modifier = modifier
            .fillMaxWidth()
            .height(50.dp)
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(14.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                painter = painterResource(R.drawable.ic_search),
                contentDescription = null,
                tint = AutoWallsColors.TextTertiary,
                modifier = Modifier.size(18.dp)
            )
            Spacer(Modifier.width(12.dp))
            Text(
                text = "Search a vehicle or manufacturer",
                style = MaterialTheme.typography.bodyMedium,
                color = AutoWallsColors.TextTertiary
            )
        }
    }
}

@Composable
private fun DailyWallpaperHero(
    wallpaper: Wallpaper?,
    loading: Boolean,
    error: String?,
    onOpen: (Wallpaper) -> Unit,
    onRetry: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                text = "DAILY VEHICLE WALLPAPER",
                style = MaterialTheme.typography.labelSmall,
                color = AutoWallsColors.Champagne
            )
            Spacer(Modifier.width(10.dp))
            MetallicDivider(
                modifier = Modifier
                    .weight(1f)
                    .height(1.dp)
            )
        }
        Spacer(Modifier.height(12.dp))

        when {
            loading -> Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .aspectRatio(4f / 5f)
                    .clip(RoundedCornerShape(22.dp))
                    .background(AutoWallsColors.Graphite)
            ) {
                ShimmerBlock(Modifier.fillMaxSize(), shape = RoundedCornerShape(22.dp))
            }

            error != null || wallpaper == null -> GlassSurface(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(160.dp),
                shape = RoundedCornerShape(22.dp)
            ) {
                ErrorState(message = error ?: "No wallpaper available today", onRetry = onRetry)
            }

            else -> Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .aspectRatio(4f / 5f)
                    .clip(RoundedCornerShape(22.dp))
                    .background(AutoWallsColors.Graphite)
                    .clickable { onOpen(wallpaper) }
            ) {
                WallpaperImage(
                    url = wallpaper.previewUrl,
                    contentDescription = wallpaper.displayName,
                    modifier = Modifier.fillMaxSize()
                )
                Box(
                    Modifier
                        .fillMaxSize()
                        .background(
                            Brush.verticalGradient(
                                0f to Color.Transparent,
                                0.5f to Color(0x4D000000),
                                1f to Color(0xF2000000)
                            )
                        )
                )
                LicenseBadge(
                    licenseName = wallpaper.licenseName,
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(14.dp)
                )
                Column(
                    modifier = Modifier
                        .align(Alignment.BottomStart)
                        .padding(18.dp)
                ) {
                    Text(
                        text = wallpaper.categoryName.uppercase(),
                        style = MaterialTheme.typography.labelSmall,
                        color = AutoWallsColors.ChampagneBright,
                        fontSize = 9.sp
                    )
                    Spacer(Modifier.height(6.dp))
                    Text(
                        text = wallpaper.displayName,
                        style = MaterialTheme.typography.headlineMedium,
                        color = AutoWallsColors.TextPrimary,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )
                    Spacer(Modifier.height(4.dp))
                    Text(
                        text = "© ${wallpaper.artist} · ${wallpaper.resolution}",
                        style = MaterialTheme.typography.bodySmall,
                        color = AutoWallsColors.TextSecondary,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }
        }
    }
}
