package com.autowalls.app.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.autowalls.app.R
import com.autowalls.app.domain.LicensePolicy
import com.autowalls.app.domain.model.Wallpaper
import com.autowalls.app.ui.theme.AutoWallsColors

@Composable
fun AutoWallsLogo(modifier: Modifier = Modifier, compact: Boolean = false) {
    Row(modifier = modifier, verticalAlignment = Alignment.CenterVertically) {
        Box(
            modifier = Modifier
                .size(if (compact) 30.dp else 36.dp)
                .clip(RoundedCornerShape(10.dp))
                .background(
                    Brush.linearGradient(
                        listOf(AutoWallsColors.Champagne.copy(alpha = 0.22f), Color(0x0AFFFFFF))
                    )
                )
                .border(
                    1.dp,
                    AutoWallsColors.ChampagneDim.copy(alpha = 0.7f),
                    RoundedCornerShape(10.dp)
                ),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "A",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Light,
                color = AutoWallsColors.ChampagneBright,
                fontSize = if (compact) 15.sp else 18.sp
            )
        }
        Spacer(Modifier.width(10.dp))
        Column {
            Text(
                text = "AUTOWALLS",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Medium,
                letterSpacing = 3.2.sp,
                color = AutoWallsColors.TextPrimary,
                fontSize = if (compact) 13.sp else 15.sp
            )
            if (!compact) {
                Text(
                    text = "VEHICLE WALLPAPERS",
                    style = MaterialTheme.typography.labelSmall,
                    color = AutoWallsColors.TextTertiary,
                    letterSpacing = 2.4.sp,
                    fontSize = 8.sp
                )
            }
        }
    }
}

@Composable
fun SectionHeader(
    eyebrow: String,
    title: String,
    modifier: Modifier = Modifier,
    actionLabel: String? = null,
    onActionClick: (() -> Unit)? = null
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        verticalAlignment = Alignment.Bottom,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Column(Modifier.weight(1f)) {
            Text(
                text = eyebrow.uppercase(),
                style = MaterialTheme.typography.labelSmall,
                color = AutoWallsColors.Champagne.copy(alpha = 0.85f)
            )
            Spacer(Modifier.height(6.dp))
            Text(
                text = title,
                style = MaterialTheme.typography.headlineMedium,
                color = AutoWallsColors.TextPrimary
            )
        }
        if (actionLabel != null && onActionClick != null) {
            Text(
                text = actionLabel,
                style = MaterialTheme.typography.labelLarge,
                color = AutoWallsColors.TextSecondary,
                modifier = Modifier
                    .clip(RoundedCornerShape(8.dp))
                    .clickable(onClick = onActionClick)
                    .padding(horizontal = 8.dp, vertical = 6.dp)
            )
        }
    }
}

@Composable
fun PillChip(
    text: String,
    modifier: Modifier = Modifier,
    selected: Boolean = false,
    onClick: () -> Unit
) {
    val shape = RoundedCornerShape(50)
    Box(
        modifier = modifier
            .clip(shape)
            .background(
                if (selected) AutoWallsColors.Champagne.copy(alpha = 0.16f)
                else Color(0x0DFFFFFF)
            )
            .border(
                BorderStroke(
                    1.dp,
                    if (selected) AutoWallsColors.Champagne.copy(alpha = 0.6f)
                    else AutoWallsColors.GlassBorder
                ),
                shape
            )
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 9.dp)
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.labelLarge,
            color = if (selected) AutoWallsColors.ChampagneBright else AutoWallsColors.TextSecondary,
            maxLines = 1
        )
    }
}

/** Small uppercase license marker so provenance is never hidden on a card. */
@Composable
fun LicenseBadge(licenseName: String, modifier: Modifier = Modifier) {
    val shape = RoundedCornerShape(6.dp)
    Box(
        modifier = modifier
            .clip(shape)
            .background(Color(0xB3000000))
            .border(1.dp, AutoWallsColors.GlassBorder, shape)
            .padding(horizontal = 7.dp, vertical = 3.dp)
    ) {
        Text(
            text = licenseName.uppercase(),
            style = MaterialTheme.typography.labelSmall,
            color = AutoWallsColors.Silver,
            letterSpacing = 0.8.sp,
            fontSize = 8.5.sp,
            maxLines = 1
        )
    }
}

@Composable
fun ErrorState(
    message: String,
    modifier: Modifier = Modifier,
    onRetry: () -> Unit
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp, vertical = 28.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Icon(
            painter = painterResource(R.drawable.ic_cloud_off),
            contentDescription = null,
            tint = AutoWallsColors.TextTertiary,
            modifier = Modifier.size(30.dp)
        )
        Spacer(Modifier.height(12.dp))
        Text(
            text = message,
            style = MaterialTheme.typography.bodyMedium,
            color = AutoWallsColors.TextSecondary,
            textAlign = TextAlign.Center
        )
        Spacer(Modifier.height(14.dp))
        Row(
            modifier = Modifier
                .clip(RoundedCornerShape(50))
                .border(
                    1.dp,
                    AutoWallsColors.Champagne.copy(alpha = 0.5f),
                    RoundedCornerShape(50)
                )
                .clickable(onClick = onRetry)
                .padding(horizontal = 18.dp, vertical = 9.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                painter = painterResource(R.drawable.ic_refresh),
                contentDescription = null,
                tint = AutoWallsColors.ChampagneBright,
                modifier = Modifier.size(15.dp)
            )
            Spacer(Modifier.width(7.dp))
            Text(
                text = "Retry",
                style = MaterialTheme.typography.labelLarge,
                color = AutoWallsColors.ChampagneBright
            )
        }
    }
}

@Composable
fun EmptyState(
    icon: Painter,
    title: String,
    message: String,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 32.dp, vertical = 56.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Icon(
            painter = icon,
            contentDescription = null,
            tint = AutoWallsColors.TextTertiary,
            modifier = Modifier.size(34.dp)
        )
        Spacer(Modifier.height(14.dp))
        Text(
            text = title,
            style = MaterialTheme.typography.titleMedium,
            color = AutoWallsColors.TextPrimary
        )
        Spacer(Modifier.height(6.dp))
        Text(
            text = message,
            style = MaterialTheme.typography.bodyMedium,
            color = AutoWallsColors.TextTertiary,
            textAlign = TextAlign.Center
        )
    }
}

/** Full attribution block: title, creator, license, source. Detail + credits. */
@Composable
fun AttributionBlock(
    wallpaper: Wallpaper,
    modifier: Modifier = Modifier,
    onOpenSource: () -> Unit,
    onOpenLicense: () -> Unit
) {
    Column(modifier = modifier.fillMaxWidth()) {
        Text(
            text = "PHOTO CREDIT",
            style = MaterialTheme.typography.labelSmall,
            color = AutoWallsColors.Champagne.copy(alpha = 0.85f)
        )
        Spacer(Modifier.height(10.dp))
        Text(
            text = wallpaper.displayName,
            style = MaterialTheme.typography.titleMedium,
            color = AutoWallsColors.TextPrimary
        )
        Spacer(Modifier.height(4.dp))
        Text(
            text = "Photographer / creator: ${wallpaper.artist}",
            style = MaterialTheme.typography.bodyMedium,
            color = AutoWallsColors.TextSecondary
        )
        if (wallpaper.uploadedBy.isNotBlank() && wallpaper.uploadedBy != wallpaper.artist) {
            Text(
                text = "Uploaded to Commons by ${wallpaper.uploadedBy}",
                style = MaterialTheme.typography.bodySmall,
                color = AutoWallsColors.TextTertiary
            )
        }
        if (wallpaper.dateOriginal.isNotBlank()) {
            Text(
                text = "Original date: ${wallpaper.dateOriginal}",
                style = MaterialTheme.typography.bodySmall,
                color = AutoWallsColors.TextTertiary
            )
        }
        Spacer(Modifier.height(10.dp))
        Text(
            text = "License: ${wallpaper.licenseName}",
            style = MaterialTheme.typography.bodyMedium,
            color = AutoWallsColors.TextSecondary
        )
        Text(
            text = LicensePolicy.summary(wallpaper.licenseName),
            style = MaterialTheme.typography.bodySmall,
            color = AutoWallsColors.TextTertiary
        )
        Spacer(Modifier.height(12.dp))
        AttributionLink(text = "License terms", onClick = onOpenLicense)
        Spacer(Modifier.height(6.dp))
        AttributionLink(text = "Original Wikimedia Commons page", onClick = onOpenSource)
        Spacer(Modifier.height(12.dp))
        Text(
            text = "Modifications: AutoWalls displays this photograph unmodified at full " +
                "resolution and uses Wikimedia's standard scaled thumbnail for previews. " +
                "AutoWalls does not claim ownership of this image.",
            style = MaterialTheme.typography.bodySmall,
            color = AutoWallsColors.TextTertiary
        )
    }
}

@Composable
private fun AttributionLink(text: String, onClick: () -> Unit) {
    Text(
        text = text,
        style = MaterialTheme.typography.labelLarge,
        color = AutoWallsColors.ChampagneBright,
        modifier = Modifier
            .clip(RoundedCornerShape(6.dp))
            .clickable(onClick = onClick)
            .padding(vertical = 2.dp)
    )
}
