package com.autowalls.app.ui.screens.credits

import android.content.Context
import android.content.Intent
import android.net.Uri
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.autowalls.app.R
import com.autowalls.app.domain.model.Wallpaper
import com.autowalls.app.ui.components.GlassSurface
import com.autowalls.app.ui.components.MetallicDivider
import com.autowalls.app.ui.theme.AutoWallsColors

@Composable
fun CreditsScreen(
    state: CreditsUiState,
    modifier: Modifier = Modifier
) {
    val context = androidx.compose.ui.platform.LocalContext.current

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(AutoWallsColors.Black)
            .statusBarsPadding(),
        contentPadding = PaddingValues(start = 20.dp, end = 20.dp, top = 12.dp, bottom = 120.dp)
    ) {
        item {
            Text(
                text = "OPEN SOURCE PHOTOGRAPHY",
                style = MaterialTheme.typography.labelSmall,
                color = AutoWallsColors.Champagne
            )
            Spacer(Modifier.height(4.dp))
            Text(
                text = "Credits & licensing",
                style = MaterialTheme.typography.headlineLarge,
                color = AutoWallsColors.TextPrimary
            )
            Spacer(Modifier.height(14.dp))
            Text(
                text = "AutoWalls is a viewer for free-licensed automotive photography hosted on " +
                    "Wikimedia Commons. AutoWalls does not own, claim or relicense any of these " +
                    "photographs. Every image shown in the app carries the name of its creator, " +
                    "its license and a link to the original Commons file page.",
                style = MaterialTheme.typography.bodyMedium,
                color = AutoWallsColors.TextSecondary
            )
            Spacer(Modifier.height(18.dp))
        }

        item {
            GlassSurface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(18.dp)
            ) {
                Column(Modifier.padding(16.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            painter = painterResource(R.drawable.ic_verified_user),
                            contentDescription = null,
                            tint = AutoWallsColors.Champagne,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(Modifier.width(8.dp))
                        Text(
                            text = "LICENSE POLICY",
                            style = MaterialTheme.typography.labelSmall,
                            color = AutoWallsColors.Champagne
                        )
                    }
                    Spacer(Modifier.height(10.dp))
                    Text(
                        text = "Only CC0, Public Domain, CC BY and CC BY-SA images are shown. " +
                            "Non-commercial and no-derivatives licenses are filtered out in code, " +
                            "so they can never reach a commercial build of AutoWalls.",
                        style = MaterialTheme.typography.bodySmall,
                        color = AutoWallsColors.TextSecondary
                    )
                    Spacer(Modifier.height(10.dp))
                    Text(
                        text = "CC BY and CC BY-SA require credit to the creator and, for " +
                            "CC BY-SA, that any derivative is shared under the same license. " +
                            "AutoWalls displays images unmodified, so no share-alike obligation " +
                            "is triggered by viewing or setting a wallpaper.",
                        style = MaterialTheme.typography.bodySmall,
                        color = AutoWallsColors.TextTertiary
                    )
                    Spacer(Modifier.height(12.dp))
                    MetallicDivider(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(1.dp)
                    )
                    Spacer(Modifier.height(12.dp))
                    Row(
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .clickable {
                                openUrl(
                                    context,
                                    "https://commons.wikimedia.org/wiki/Commons:Licensing"
                                )
                            },
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Commons licensing rules",
                            style = MaterialTheme.typography.labelLarge,
                            color = AutoWallsColors.ChampagneBright
                        )
                        Spacer(Modifier.width(6.dp))
                        Icon(
                            painter = painterResource(R.drawable.ic_open_in_new),
                            contentDescription = null,
                            tint = AutoWallsColors.ChampagneBright,
                            modifier = Modifier.size(13.dp)
                        )
                    }
                }
            }
            Spacer(Modifier.height(26.dp))
            Text(
                text = "YOUR SAVED IMAGES",
                style = MaterialTheme.typography.labelSmall,
                color = AutoWallsColors.TextTertiary
            )
            Spacer(Modifier.height(4.dp))
            Text(
                text = "Credits for everything you favourited or downloaded",
                style = MaterialTheme.typography.bodyMedium,
                color = AutoWallsColors.TextSecondary
            )
            Spacer(Modifier.height(12.dp))
        }

        if (state.credited.isEmpty()) {
            item {
                GlassSurface(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Text(
                        text = "Nothing saved yet. Favourite or download a wallpaper and its " +
                            "photographer will be listed here.",
                        style = MaterialTheme.typography.bodySmall,
                        color = AutoWallsColors.TextTertiary,
                        modifier = Modifier.padding(16.dp)
                    )
                }
            }
        }

        items(state.credited, key = { it.id }) { wallpaper ->
            CreditRowItem(
                wallpaper = wallpaper,
                onOpen = { openUrl(context, wallpaper.sourcePage) },
                onOpenLicense = { openUrl(context, wallpaper.licenseUrl) }
            )
            Spacer(Modifier.height(8.dp))
        }

        item {
            Spacer(Modifier.height(18.dp))
            Text(
                text = "All photographs remain the property of their respective creators and are " +
                    "used under the licenses shown above. Text and metadata are provided by the " +
                    "Wikimedia Commons API.",
                style = MaterialTheme.typography.bodySmall,
                color = AutoWallsColors.TextTertiary
            )
            Spacer(Modifier.height(10.dp))
            Text(
                text = "App design © AutoWalls.",
                style = MaterialTheme.typography.bodySmall,
                color = AutoWallsColors.TextTertiary
            )
        }
    }
}

@Composable
private fun CreditRowItem(
    wallpaper: Wallpaper,
    onOpen: () -> Unit,
    onOpenLicense: () -> Unit
) {
    GlassSurface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(14.dp)
    ) {
        Column(Modifier.padding(14.dp)) {
            Text(
                text = wallpaper.displayName,
                style = MaterialTheme.typography.titleMedium,
                color = AutoWallsColors.TextPrimary,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )
            Spacer(Modifier.height(3.dp))
            Text(
                text = wallpaper.artist,
                style = MaterialTheme.typography.bodyMedium,
                color = AutoWallsColors.TextSecondary
            )
            Spacer(Modifier.height(8.dp))
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                Text(
                    text = wallpaper.licenseName,
                    style = MaterialTheme.typography.labelMedium,
                    color = AutoWallsColors.Champagne,
                    fontSize = 10.sp
                )
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(6.dp))
                        .clickable(onClick = onOpenLicense)
                ) {
                    Text(
                        text = "License",
                        style = MaterialTheme.typography.labelMedium,
                        color = AutoWallsColors.TextTertiary,
                        fontSize = 10.sp
                    )
                }
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(6.dp))
                        .clickable(onClick = onOpen)
                ) {
                    Text(
                        text = "Commons page",
                        style = MaterialTheme.typography.labelMedium,
                        color = AutoWallsColors.TextTertiary,
                        fontSize = 10.sp
                    )
                }
            }
        }
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
