package com.autowalls.app.ui.screens.detail

import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.autowalls.app.R
import com.autowalls.app.data.download.DownloadState
import com.autowalls.app.domain.model.Wallpaper
import com.autowalls.app.ui.components.AttributionBlock
import com.autowalls.app.ui.components.ErrorState
import com.autowalls.app.ui.components.FullscreenImageViewer
import com.autowalls.app.ui.components.GlassSurface
import com.autowalls.app.ui.components.LicenseBadge
import com.autowalls.app.ui.components.ShimmerBlock
import com.autowalls.app.ui.components.WallpaperImage
import com.autowalls.app.ui.theme.AutoWallsColors
import com.autowalls.app.util.Formatters
import com.autowalls.app.util.WallpaperSetter

@Composable
fun DetailScreen(
    state: DetailUiState,
    onBack: () -> Unit,
    onToggleFavorite: () -> Unit,
    onDownload: () -> Unit,
    onSetWallpaper: (WallpaperSetter.Target) -> Unit,
    onSaveToGallery: () -> Unit,
    onShare: () -> Unit,
    onCopyAttribution: () -> Unit,
    onRetry: () -> Unit,
    onConsumeMessage: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val snackbarHostState = remember { SnackbarHostState() }
    var fullscreen by remember { mutableStateOf(false) }

    LaunchedEffect(state.message) {
        val message = state.message
        if (message != null) {
            snackbarHostState.showSnackbar(message)
            onConsumeMessage()
        }
    }

    val wallpaper = state.wallpaper

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(AutoWallsColors.Black)
    ) {
        when {
            wallpaper == null && state.loading -> ShimmerBlock(
                Modifier.fillMaxSize(),
                shape = RoundedCornerShape(0.dp)
            )

            wallpaper == null && state.error != null -> Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(24.dp),
                contentAlignment = Alignment.Center
            ) {
                GlassSurface(shape = RoundedCornerShape(20.dp)) {
                    ErrorState(message = state.error, onRetry = onRetry)
                }
            }

            wallpaper != null -> Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(bottom = 28.dp)
            ) {
                // The whole photograph, never cropped — landscape shots stay landscape.
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .aspectRatio(heroAspectRatio(wallpaper))
                        .background(Color.Black)
                        .clickable { fullscreen = true }
                ) {
                    WallpaperImage(
                        url = wallpaper.previewUrl,
                        contentDescription = wallpaper.displayName,
                        contentScale = ContentScale.Fit,
                        placeholderUrl = wallpaper.thumbUrl,
                        modifier = Modifier.fillMaxSize()
                    )
                    ExpandHint(
                        modifier = Modifier
                            .align(Alignment.BottomEnd)
                            .padding(12.dp)
                    )
                }

                Column(Modifier.padding(horizontal = 20.dp)) {
                    Spacer(Modifier.height(18.dp))

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = wallpaper.categoryName.uppercase(),
                            style = MaterialTheme.typography.labelSmall,
                            color = AutoWallsColors.Champagne
                        )
                        Spacer(Modifier.width(10.dp))
                        LicenseBadge(licenseName = wallpaper.licenseName)
                    }

                    Spacer(Modifier.height(10.dp))

                    Text(
                        text = wallpaper.displayName,
                        style = MaterialTheme.typography.displayMedium,
                        color = AutoWallsColors.TextPrimary,
                        maxLines = 3,
                        overflow = TextOverflow.Ellipsis
                    )

                    Spacer(Modifier.height(8.dp))

                    Text(
                        text = "Photograph by ${wallpaper.artist} · Wikimedia Commons",
                        style = MaterialTheme.typography.bodyMedium,
                        color = AutoWallsColors.TextSecondary
                    )

                    Spacer(Modifier.height(16.dp))

                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        MetaPill("${wallpaper.resolution} px")
                        MetaPill(wallpaper.megapixels)
                        MetaPill(Formatters.fileSize(wallpaper.fileSizeBytes))
                    }

                    Spacer(Modifier.height(20.dp))

                    DownloadButton(state = state, onDownload = onDownload)

                    Spacer(Modifier.height(12.dp))

                    Text(
                        text = "SET AS WALLPAPER",
                        style = MaterialTheme.typography.labelSmall,
                        color = AutoWallsColors.TextTertiary
                    )
                    Spacer(Modifier.height(8.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        ActionChip(
                            icon = painterResource(R.drawable.ic_smartphone),
                            label = "Home",
                            busy = state.applying == WallpaperSetter.Target.HOME,
                            enabled = state.applying == null,
                            onClick = { onSetWallpaper(WallpaperSetter.Target.HOME) },
                            modifier = Modifier.weight(1f)
                        )
                        ActionChip(
                            icon = painterResource(R.drawable.ic_lock),
                            label = "Lock",
                            busy = state.applying == WallpaperSetter.Target.LOCK,
                            enabled = state.applying == null,
                            onClick = { onSetWallpaper(WallpaperSetter.Target.LOCK) },
                            modifier = Modifier.weight(1f)
                        )
                        ActionChip(
                            icon = painterResource(R.drawable.ic_phone_android),
                            label = "Both",
                            busy = state.applying == WallpaperSetter.Target.BOTH,
                            enabled = state.applying == null,
                            onClick = { onSetWallpaper(WallpaperSetter.Target.BOTH) },
                            modifier = Modifier.weight(1f)
                        )
                    }

                    Spacer(Modifier.height(10.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        ActionChip(
                            icon = painterResource(R.drawable.ic_wallpaper),
                            label = if (state.savingToGallery) "Saving…" else "Save to gallery",
                            busy = state.savingToGallery,
                            enabled = !state.savingToGallery,
                            onClick = onSaveToGallery,
                            modifier = Modifier.weight(1f)
                        )
                        ActionChip(
                            icon = painterResource(R.drawable.ic_content_copy),
                            label = "Copy credit",
                            busy = false,
                            enabled = true,
                            onClick = onCopyAttribution,
                            modifier = Modifier.weight(1f)
                        )
                    }

                    Spacer(Modifier.height(22.dp))

                    GlassSurface(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(18.dp)
                    ) {
                        AttributionBlock(
                            wallpaper = wallpaper,
                            modifier = Modifier.padding(16.dp),
                            onOpenSource = { openUrl(context, wallpaper.sourcePage) },
                            onOpenLicense = { openUrl(context, wallpaper.licenseUrl) }
                        )
                    }

                    if (wallpaper.description.isNotBlank()) {
                        Spacer(Modifier.height(16.dp))
                        Text(
                            text = "ABOUT THIS IMAGE",
                            style = MaterialTheme.typography.labelSmall,
                            color = AutoWallsColors.TextTertiary
                        )
                        Spacer(Modifier.height(6.dp))
                        Text(
                            text = wallpaper.description,
                            style = MaterialTheme.typography.bodyMedium,
                            color = AutoWallsColors.TextSecondary
                        )
                    }
                }
            }
        }

        // Fixed top bar so the controls stay reachable while the page scrolls.
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    Brush.verticalGradient(
                        listOf(Color(0xCC000000), Color(0x00000000))
                    )
                )
                .statusBarsPadding()
                .padding(horizontal = 8.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onBack) {
                Icon(
                    painter = painterResource(R.drawable.ic_arrow_back),
                    contentDescription = "Back",
                    tint = AutoWallsColors.TextPrimary
                )
            }
            Spacer(Modifier.weight(1f))
            if (wallpaper != null) {
                IconButton(onClick = onToggleFavorite) {
                    Icon(
                        painter = painterResource(
                            if (state.isFavorite) R.drawable.ic_favorite
                            else R.drawable.ic_favorite_border
                        ),
                        contentDescription = if (state.isFavorite) "Remove favourite"
                        else "Add favourite",
                        tint = if (state.isFavorite) AutoWallsColors.ChampagneBright
                        else AutoWallsColors.TextPrimary
                    )
                }
                IconButton(onClick = onShare) {
                    Icon(
                        painter = painterResource(R.drawable.ic_share),
                        contentDescription = "Share wallpaper",
                        tint = AutoWallsColors.TextPrimary
                    )
                }
            }
        }

        AnimatedVisibility(
            visible = fullscreen && wallpaper != null,
            enter = fadeIn(),
            exit = fadeOut()
        ) {
            if (wallpaper != null) {
                FullscreenImageViewer(
                    url = wallpaper.previewUrl,
                    contentDescription = wallpaper.displayName,
                    placeholderUrl = wallpaper.thumbUrl,
                    onClose = { fullscreen = false }
                )
            }
        }

        SnackbarHost(
            hostState = snackbarHostState,
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(16.dp)
        )
    }
}

/**
 * Natural aspect ratio of the photograph, clamped so an extreme panorama or a very
 * tall shot still leaves room for the controls on the next screen.
 */
private fun heroAspectRatio(wallpaper: Wallpaper): Float =
    wallpaper.aspectRatio.coerceIn(0.66f, 2.4f)

@Composable
private fun ExpandHint(modifier: Modifier = Modifier) {
    val shape = RoundedCornerShape(50)
    Row(
        modifier = modifier
            .clip(shape)
            .background(Color(0xB3000000))
            .border(1.dp, AutoWallsColors.GlassBorder, shape)
            .padding(horizontal = 12.dp, vertical = 7.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            painter = painterResource(R.drawable.ic_wallpaper),
            contentDescription = null,
            tint = AutoWallsColors.TextPrimary,
            modifier = Modifier.size(13.dp)
        )
        Spacer(Modifier.width(6.dp))
        Text(
            text = "Full screen",
            style = MaterialTheme.typography.labelMedium,
            color = AutoWallsColors.TextPrimary,
            fontSize = 10.sp
        )
    }
}

@Composable
private fun DownloadButton(state: DetailUiState, onDownload: () -> Unit) {
    val download = state.download
    val shape = RoundedCornerShape(14.dp)

    when (download) {
        is DownloadState.InProgress -> Column(
            modifier = Modifier
                .fillMaxWidth()
                .clip(shape)
                .background(Color(0x1AFFFFFF))
                .border(1.dp, AutoWallsColors.GlassBorder, shape)
                .padding(horizontal = 18.dp, vertical = 14.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = "Downloading full resolution…",
                    style = MaterialTheme.typography.labelLarge,
                    color = AutoWallsColors.TextPrimary
                )
                Spacer(Modifier.weight(1f))
                Text(
                    text = "${download.percent}%",
                    style = MaterialTheme.typography.labelLarge,
                    color = AutoWallsColors.ChampagneBright
                )
            }
            Spacer(Modifier.height(10.dp))
            LinearProgressIndicator(
                progress = { download.percent / 100f },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(3.dp)
                    .clip(RoundedCornerShape(3.dp)),
                color = AutoWallsColors.Champagne,
                trackColor = Color(0x1FFFFFFF)
            )
        }

        is DownloadState.Completed -> Row(
            modifier = Modifier
                .fillMaxWidth()
                .clip(shape)
                .background(AutoWallsColors.Champagne.copy(alpha = 0.12f))
                .border(1.dp, AutoWallsColors.Champagne.copy(alpha = 0.4f), shape)
                .padding(horizontal = 18.dp, vertical = 15.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                painter = painterResource(R.drawable.ic_check),
                contentDescription = null,
                tint = AutoWallsColors.ChampagneBright,
                modifier = Modifier.size(17.dp)
            )
            Spacer(Modifier.width(10.dp))
            Text(
                text = "Saved offline · ready to set",
                style = MaterialTheme.typography.labelLarge,
                color = AutoWallsColors.ChampagneBright
            )
        }

        else -> Row(
            modifier = Modifier
                .fillMaxWidth()
                .clip(shape)
                .background(AutoWallsColors.Champagne)
                .clickable(onClick = onDownload)
                .padding(horizontal = 18.dp, vertical = 15.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            Icon(
                painter = painterResource(R.drawable.ic_download),
                contentDescription = null,
                tint = Color.Black,
                modifier = Modifier.size(18.dp)
            )
            Spacer(Modifier.width(10.dp))
            Text(
                text = "Download full resolution",
                style = MaterialTheme.typography.labelLarge,
                color = Color.Black
            )
        }
    }
}

@Composable
private fun ActionChip(
    icon: Painter,
    label: String,
    busy: Boolean,
    enabled: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val shape = RoundedCornerShape(13.dp)
    Row(
        modifier = modifier
            .clip(shape)
            .background(Color(0x14FFFFFF))
            .border(1.dp, AutoWallsColors.GlassBorder, shape)
            .clickable(enabled = enabled && !busy, onClick = onClick)
            .padding(horizontal = 10.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Center
    ) {
        if (busy) {
            CircularProgressIndicator(
                modifier = Modifier.size(14.dp),
                color = AutoWallsColors.Champagne,
                strokeWidth = 1.5.dp
            )
        } else {
            Icon(
                painter = icon,
                contentDescription = null,
                tint = if (enabled) AutoWallsColors.Silver else AutoWallsColors.TextTertiary,
                modifier = Modifier.size(15.dp)
            )
        }
        Spacer(Modifier.width(7.dp))
        Text(
            text = label,
            style = MaterialTheme.typography.labelMedium,
            color = if (enabled) AutoWallsColors.TextPrimary else AutoWallsColors.TextTertiary,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            fontSize = 11.sp
        )
    }
}

@Composable
private fun MetaPill(text: String) {
    val shape = RoundedCornerShape(50)
    Box(
        modifier = Modifier
            .clip(shape)
            .background(Color(0x0FFFFFFF))
            .border(1.dp, AutoWallsColors.GlassBorder, shape)
            .padding(horizontal = 11.dp, vertical = 5.dp)
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.labelMedium,
            color = AutoWallsColors.TextSecondary,
            fontSize = 10.sp
        )
    }
}

private fun openUrl(context: Context, url: String) {
    if (url.isBlank()) return
    runCatching {
        context.startActivity(
            Intent(Intent.ACTION_VIEW, Uri.parse(url)).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        )
    }
}
