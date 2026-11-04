package com.autowalls.app.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.autowalls.app.R
import com.autowalls.app.domain.model.VehicleCategory
import com.autowalls.app.domain.model.Wallpaper
import com.autowalls.app.ui.theme.AutoWallsColors

/**
 * Coil-backed image with a shimmer placeholder, so a feed never shows a blank
 * rectangle while a thumbnail streams in.
 */
@Composable
fun WallpaperImage(
    url: String,
    contentDescription: String?,
    modifier: Modifier = Modifier,
    contentScale: ContentScale = ContentScale.Crop
) {
    val context = LocalContext.current
    var loaded by remember(url) { mutableStateOf(false) }

    Box(modifier = modifier) {
        if (!loaded) {
            ShimmerBlock(Modifier.fillMaxSize(), shape = RoundedCornerShape(0.dp))
        }
        AsyncImage(
            model = ImageRequest.Builder(context)
                .data(url)
                .crossfade(300)
                .build(),
            contentDescription = contentDescription,
            contentScale = contentScale,
            onSuccess = { loaded = true },
            modifier = Modifier.fillMaxSize()
        )
    }
}

private fun imageScrim() = Brush.verticalGradient(
    0f to Color.Transparent,
    0.45f to Color(0x33000000),
    1f to AutoWallsColors.ImageScrimBottom
)

/** 3:4 portrait card used in every grid. */
@Composable
fun PortraitWallpaperCard(
    wallpaper: Wallpaper,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    isFavorite: Boolean = false,
    onFavoriteToggle: (() -> Unit)? = null
) {
    val shape = RoundedCornerShape(16.dp)
    Column(modifier = modifier) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .aspectRatio(0.75f)
                .clip(shape)
                .background(AutoWallsColors.Graphite)
                .clickable(onClick = onClick)
        ) {
            WallpaperImage(
                url = wallpaper.thumbUrl,
                contentDescription = wallpaper.displayName,
                modifier = Modifier.fillMaxSize()
            )
            Box(Modifier.fillMaxSize().background(imageScrim()))

            LicenseBadge(
                licenseName = wallpaper.licenseName,
                modifier = Modifier
                    .align(Alignment.TopStart)
                    .padding(8.dp)
            )

            if (onFavoriteToggle != null) {
                Box(
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(6.dp)
                        .size(30.dp)
                        .clip(RoundedCornerShape(50))
                        .background(Color(0x8C000000))
                        .clickable(onClick = onFavoriteToggle),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        painter = painterResource(
                            if (isFavorite) R.drawable.ic_favorite else R.drawable.ic_favorite_border
                        ),
                        contentDescription = if (isFavorite) "Remove from favourites"
                        else "Add to favourites",
                        tint = if (isFavorite) AutoWallsColors.ChampagneBright
                        else AutoWallsColors.Silver,
                        modifier = Modifier.size(15.dp)
                    )
                }
            }

            Column(
                modifier = Modifier
                    .align(Alignment.BottomStart)
                    .padding(horizontal = 10.dp, vertical = 9.dp)
            ) {
                Text(
                    text = wallpaper.displayName,
                    style = MaterialTheme.typography.labelLarge,
                    color = AutoWallsColors.TextPrimary,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = wallpaper.artist,
                    style = MaterialTheme.typography.bodySmall,
                    color = AutoWallsColors.TextSecondary,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    fontSize = 10.sp
                )
            }
        }
    }
}

/** 16:10 cinematic card for horizontal rails. */
@Composable
fun WideWallpaperCard(
    wallpaper: Wallpaper,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    width: Dp = 268.dp
) {
    val shape = RoundedCornerShape(18.dp)
    Box(
        modifier = modifier
            .width(width)
            .aspectRatio(16f / 10f)
            .clip(shape)
            .background(AutoWallsColors.Graphite)
            .clickable(onClick = onClick)
    ) {
        WallpaperImage(
            url = wallpaper.thumbUrl,
            contentDescription = wallpaper.displayName,
            modifier = Modifier.fillMaxSize()
        )
        Box(Modifier.fillMaxSize().background(imageScrim()))
        LicenseBadge(
            licenseName = wallpaper.licenseName,
            modifier = Modifier
                .align(Alignment.TopEnd)
                .padding(8.dp)
        )
        Column(
            modifier = Modifier
                .align(Alignment.BottomStart)
                .padding(12.dp)
        ) {
            Text(
                text = wallpaper.categoryName.uppercase(),
                style = MaterialTheme.typography.labelSmall,
                color = AutoWallsColors.ChampagneBright,
                fontSize = 8.sp
            )
            Spacer(Modifier.height(3.dp))
            Text(
                text = wallpaper.displayName,
                style = MaterialTheme.typography.titleMedium,
                color = AutoWallsColors.TextPrimary,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Text(
                text = "© ${wallpaper.artist}",
                style = MaterialTheme.typography.bodySmall,
                color = AutoWallsColors.TextSecondary,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

/** Brand tile for the popular-brands rail. */
@Composable
fun BrandCard(
    category: VehicleCategory,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    GlassSurface(
        modifier = modifier
            .width(124.dp)
            .height(74.dp)
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(14.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 12.dp, vertical = 10.dp),
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = category.name,
                style = MaterialTheme.typography.titleMedium,
                color = AutoWallsColors.TextPrimary,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )
            Spacer(Modifier.height(4.dp))
            Text(
                text = category.group.label.uppercase(),
                style = MaterialTheme.typography.labelSmall,
                color = AutoWallsColors.TextTertiary,
                fontSize = 8.sp
            )
        }
    }
}

@Composable
fun ShimmerRailCard(modifier: Modifier = Modifier, width: Dp = 268.dp) {
    ShimmerBlock(
        modifier = modifier
            .width(width)
            .aspectRatio(16f / 10f),
        shape = RoundedCornerShape(18.dp)
    )
}

@Composable
fun ShimmerGridCell(modifier: Modifier = Modifier) {
    ShimmerBlock(
        modifier = modifier
            .fillMaxWidth()
            .aspectRatio(0.75f),
        shape = RoundedCornerShape(16.dp)
    )
}
