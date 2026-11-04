package com.autowalls.app.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.unit.dp
import com.autowalls.app.ui.theme.AutoWallsColors

/**
 * Dark glassmorphism surface: a barely-there vertical wash plus a single
 * hairline border. No heavy gradients, no glow — just depth on pure black.
 */
@Composable
fun GlassSurface(
    modifier: Modifier = Modifier,
    shape: Shape = RoundedCornerShape(20.dp),
    content: @Composable BoxScope.() -> Unit
) {
    Box(
        modifier = modifier
            .clip(shape)
            .background(
                Brush.verticalGradient(
                    listOf(AutoWallsColors.GlassTop, AutoWallsColors.GlassBottom)
                )
            )
            .border(
                width = 1.dp,
                brush = Brush.verticalGradient(
                    listOf(AutoWallsColors.GlassBorder, Color(0x08FFFFFF))
                ),
                shape = shape
            ),
        content = content
    )
}

/** Brushed-metal hairline divider. */
@Composable
fun MetallicDivider(modifier: Modifier = Modifier) {
    Box(
        modifier = modifier.background(
            Brush.horizontalGradient(
                listOf(
                    Color.Transparent,
                    AutoWallsColors.ChampagneDim.copy(alpha = 0.55f),
                    Color.Transparent
                )
            )
        )
    )
}
