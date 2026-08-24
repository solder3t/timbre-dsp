package com.timbre.dsp.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import dev.chrisbanes.haze.HazeState
import dev.chrisbanes.haze.hazeEffect
import dev.chrisbanes.haze.materials.ExperimentalHazeMaterialsApi
import dev.chrisbanes.haze.materials.HazeMaterials

@OptIn(ExperimentalHazeMaterialsApi::class)
@Composable
fun GlassmorphicCard(
    modifier: Modifier = Modifier,
    hazeState: HazeState? = null,
    shape: Shape = RoundedCornerShape(16.dp),
    containerColor: Color = MaterialTheme.colorScheme.surface.copy(alpha = 0.55f),
    borderColor: Color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.12f),
    borderWidth: Dp = 1.dp,
    content: @Composable BoxScope.() -> Unit
) {
    val hazeModifier = if (hazeState != null) {
        Modifier.hazeEffect(
            state = hazeState,
            style = HazeMaterials.thin(containerColor)
        )
    } else {
        Modifier.background(containerColor)
    }

    val gradientBorder = Brush.verticalGradient(
        colors = listOf(
            borderColor.copy(alpha = 0.35f),
            borderColor.copy(alpha = 0.08f)
        )
    )

    Surface(
        modifier = modifier
            .clip(shape)
            .border(BorderStroke(borderWidth, gradientBorder), shape)
            .then(hazeModifier),
        shape = shape,
        color = Color.Transparent
    ) {
        Box(content = content)
    }
}

@OptIn(ExperimentalHazeMaterialsApi::class)
@Composable
fun Modifier.glassmorphicEffect(
    hazeState: HazeState?,
    containerColor: Color,
    shape: Shape = RoundedCornerShape(16.dp),
    borderColor: Color = Color.White.copy(alpha = 0.15f)
): Modifier {
    val gradientBorder = Brush.verticalGradient(
        colors = listOf(
            borderColor.copy(alpha = 0.3f),
            borderColor.copy(alpha = 0.05f)
        )
    )

    return this
        .clip(shape)
        .border(BorderStroke(1.dp, gradientBorder), shape)
        .then(
            if (hazeState != null) {
                Modifier.hazeEffect(
                    state = hazeState,
                    style = HazeMaterials.thin(containerColor)
                )
            } else {
                Modifier.background(containerColor)
            }
        )
}
