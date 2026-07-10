package com.salat.gbinder.ui

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawWithCache
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.dp
import com.salat.gbinder.entity.DisplayAdbState
import com.salat.gbinder.ui.theme.AppTheme

@Composable
fun StatusLampSquare(
    state: DisplayAdbState,
    title: String,
    subtitle: String,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier,
        verticalAlignment = Alignment.CenterVertically
    ) {
        SquareLamp(state = state)

        Spacer(Modifier.width(16.dp))

        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Text(
                text = title,
                style = AppTheme.typography.screenTitle,
                color = AppTheme.colors.contentPrimary
            )
            Text(
                text = subtitle,
                style = AppTheme.typography.dialogSubtitle,
                color = AppTheme.colors.contentPrimary.copy(.4f)
            )
        }
    }
}

@Composable
private fun SquareLamp(state: DisplayAdbState) {
    val baseColor by animateColorAsState(
        targetValue = statusLampColor(state),
        animationSpec = tween(durationMillis = 350),
        label = "lampColorSquare"
    )

    val shape = RoundedCornerShape(16.dp)

    Box(
        modifier = Modifier
            .size(64.dp)
            .clip(shape)
            .background(Color.White.copy(alpha = 0.2f))
            .padding(5.dp)
            .clip(RoundedCornerShape(12.dp))
            .drawWithCache {
                val cornerRadius = androidx.compose.ui.geometry.CornerRadius(size.minDimension * 0.18f)

                val volumeBrush = Brush.radialGradient(
                    colorStops = arrayOf(
                        0.0f to baseColor.copy(alpha = 1.0f),
                        0.55f to baseColor.copy(alpha = 0.95f),
                        1.0f to baseColor.copy(alpha = 1.0f)
                    ),
                    center = Offset(x = size.width * 0.35f, y = size.height * 0.30f),
                    radius = size.minDimension * 0.9f
                )

                val vignetteBrush = Brush.radialGradient(
                    colorStops = arrayOf(
                        0.65f to Color.Transparent,
                        1.0f to Color.Black.copy(alpha = 0.22f)
                    ),
                    center = Offset(x = size.width / 2f, y = size.height / 2f),
                    radius = size.minDimension * 0.72f
                )

                val specularBrush = Brush.radialGradient(
                    colorStops = arrayOf(
                        0.0f to Color.White.copy(alpha = 0.3f),
                        1.0f to Color.Transparent
                    ),
                    center = Offset(x = size.width * 0.28f, y = size.height * 0.22f),
                    radius = size.minDimension * 0.4f
                )

                val strokeWidth = size.minDimension * 0.06f

                onDrawBehind {
                    drawRoundRect(brush = volumeBrush, cornerRadius = cornerRadius)
                    drawRoundRect(brush = specularBrush, cornerRadius = cornerRadius)
                    drawRoundRect(brush = vignetteBrush, cornerRadius = cornerRadius)
                    drawRoundRect(
                        color = Color.White.copy(alpha = 0.12f),
                        cornerRadius = cornerRadius,
                        style = Stroke(width = strokeWidth)
                    )
                }
            }
    )
}
