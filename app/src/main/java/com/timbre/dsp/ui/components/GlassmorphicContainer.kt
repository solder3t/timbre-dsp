package com.timbre.dsp.ui.components

import androidx.compose.foundation.BorderStroke
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

@Composable
fun GlassmorphicCard(
    modifier: Modifier = Modifier,
    hazeState: HazeState? = null,
    shape: Shape = RoundedCornerShape(16.dp),
    containerColor: Color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.55f),
    borderColor: Color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.10f),
    borderWidth: Dp = 1.dp,
    content: @Composable BoxScope.() -> Unit
) {
    val gradientBorder = Brush.verticalGradient(
        colors = listOf(
            borderColor.copy(alpha = 0.25f),
            borderColor.copy(alpha = 0.05f)
        )
    )

    Surface(
        modifier = modifier
            .clip(shape),
        shape = shape,
        color = containerColor,
        border = BorderStroke(borderWidth, gradientBorder)
    ) {
        Box(content = content)
    }
}
