package com.timbre.dsp.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.unit.dp
import com.timbre.dsp.model.EQBand
import com.timbre.dsp.model.FilterType
import com.timbre.dsp.model.TargetCurve
import kotlin.math.exp
import kotlin.math.log10
import kotlin.math.pow

@Composable
fun EQCurveVisualizer(
    bands: List<EQBand>,
    preampGain: Float = 0f,
    targetCurve: TargetCurve = TargetCurve.NONE,
    fftMagnitudes: FloatArray? = null,
    peakLevels: Pair<Float, Float>? = null,
    onBandGainChange: (index: Int, gain: Float) -> Unit,
    onTargetCurveChange: ((TargetCurve) -> Unit)? = null,
    modifier: Modifier = Modifier,
    isInteractive: Boolean = true
) {
    val primaryColor = MaterialTheme.colorScheme.primary
    val primaryContainer = MaterialTheme.colorScheme.primaryContainer
    val secondaryColor = MaterialTheme.colorScheme.secondary
    val tertiaryColor = MaterialTheme.colorScheme.tertiary
    val onSurfaceVariant = MaterialTheme.colorScheme.onSurfaceVariant
    val targetCurveColor = Color(0xFFFFB74D) // Amber accent for target reference

    val minFreq = 20f
    val maxFreq = 20000f
    val minDb = -15f
    val maxDb = 15f

    val logMinFreq = remember { log10(minFreq) }
    val logMaxFreq = remember { log10(maxFreq) }
    var targetMenuExpanded by remember { mutableStateOf(false) }

    Column(modifier = modifier.fillMaxWidth()) {
        // Target Curve Selector & Legend
        if (onTargetCurveChange != null) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 4.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Surface(
                        shape = RoundedCornerShape(2.dp),
                        color = primaryColor,
                        modifier = Modifier
                            .width(14.dp)
                            .height(3.dp)
                    ) {}
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Current EQ", style = MaterialTheme.typography.labelSmall)

                    if (targetCurve != TargetCurve.NONE) {
                        Spacer(modifier = Modifier.width(12.dp))
                        Surface(
                            shape = RoundedCornerShape(2.dp),
                            color = targetCurveColor,
                            modifier = Modifier
                                .width(14.dp)
                                .height(2.dp)
                        ) {}
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Target: ${targetCurve.displayName}", style = MaterialTheme.typography.labelSmall, color = targetCurveColor)
                    }
                }

                Box {
                    OutlinedButton(
                        onClick = { targetMenuExpanded = true },
                        modifier = Modifier.height(28.dp),
                        contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 8.dp, vertical = 2.dp)
                    ) {
                        Icon(Icons.Default.Tune, contentDescription = null, modifier = Modifier.height(12.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Target Curve", style = MaterialTheme.typography.labelSmall)
                    }

                    DropdownMenu(
                        expanded = targetMenuExpanded,
                        onDismissRequest = { targetMenuExpanded = false }
                    ) {
                        TargetCurve.values().forEach { curve ->
                            DropdownMenuItem(
                                text = { Text(curve.displayName) },
                                onClick = {
                                    onTargetCurveChange(curve)
                                    targetMenuExpanded = false
                                }
                            )
                        }
                    }
                }
            }
        }

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(200.dp)
                .padding(vertical = 4.dp)
        ) {
            Canvas(
                modifier = Modifier
                    .matchParentSize()
                    .then(
                        if (isInteractive) {
                            Modifier.pointerInput(bands) {
                                detectDragGestures { change, _ ->
                                    val x = change.position.x
                                    val y = change.position.y
                                    val width = size.width
                                    val height = size.height

                                    if (width > 0 && height > 0) {
                                        val logFreq = logMinFreq + (x / width) * (logMaxFreq - logMinFreq)
                                        val freq = 10f.pow(logFreq)

                                        val closestBand = bands.minByOrNull { kotlin.math.abs(it.frequency - freq) }
                                        if (closestBand != null) {
                                            val normY = (y / height).coerceIn(0f, 1f)
                                            val gain = maxDb - normY * (maxDb - minDb)
                                            val clampedGain = gain.coerceIn(-15f, 15f)
                                            onBandGainChange(closestBand.index, (clampedGain * 2).toInt() / 2f)
                                        }
                                    }
                                }
                            }
                        } else Modifier
                    )
            ) {
                val width = size.width
                val height = size.height

                fun freqToX(f: Float): Float {
                    val logF = log10(f.coerceIn(minFreq, maxFreq))
                    return ((logF - logMinFreq) / (logMaxFreq - logMinFreq)) * width
                }

                fun dbToY(db: Float): Float {
                    val clamped = db.coerceIn(minDb, maxDb)
                    return height - ((clamped - minDb) / (maxDb - minDb)) * height
                }

                // 1. Draw Grid Lines (dB)
                val dbSteps = listOf(-10f, -5f, 0f, 5f, 10f)
                for (db in dbSteps) {
                    val y = dbToY(db)
                    val isZero = db == 0f
                    drawLine(
                        color = if (isZero) onSurfaceVariant.copy(alpha = 0.35f) else onSurfaceVariant.copy(alpha = 0.12f),
                        start = Offset(0f, y),
                        end = Offset(width, y),
                        strokeWidth = if (isZero) 1.5.dp.toPx() else 1.dp.toPx()
                    )
                }

                // Frequency grid markers (100Hz, 1kHz, 10kHz)
                val freqMarkers = listOf(100f, 1000f, 10000f)
                for (f in freqMarkers) {
                    val x = freqToX(f)
                    drawLine(
                        color = onSurfaceVariant.copy(alpha = 0.12f),
                        start = Offset(x, 0f),
                        end = Offset(x, height),
                        strokeWidth = 1.dp.toPx()
                    )
                }

                // 2. Draw Live FFT Spectrum Bars
                if (fftMagnitudes != null && fftMagnitudes.isNotEmpty()) {
                    val numBars = fftMagnitudes.size
                    val barWidth = (width / numBars) * 0.75f
                    val barSpacing = (width / numBars) * 0.25f

                    for (i in 0 until numBars) {
                        val mag = fftMagnitudes[i].coerceIn(0f, 1f)
                        val barH = mag * height * 0.75f
                        val bx = i * (barWidth + barSpacing) + barSpacing / 2f
                        val by = height - barH

                        drawRoundRect(
                            brush = Brush.verticalGradient(
                                colors = listOf(
                                    tertiaryColor.copy(alpha = 0.45f),
                                    secondaryColor.copy(alpha = 0.15f)
                                ),
                                startY = by,
                                endY = height
                            ),
                            topLeft = Offset(bx, by),
                            size = Size(barWidth, barH),
                            cornerRadius = CornerRadius(2.dp.toPx(), 2.dp.toPx())
                        )
                    }
                }

                // 3. Draw Target Curve Reference Line (Dashed)
                if (targetCurve != TargetCurve.NONE) {
                    val targetPath = Path()
                    val numPoints = 120
                    var firstTargetPoint = true

                    for (i in 0..numPoints) {
                        val normX = i / numPoints.toFloat()
                        val logF = logMinFreq + normX * (logMaxFreq - logMinFreq)
                        val f = 10f.pow(logF)

                        val targetGain = calculateTargetCurveGain(targetCurve, f)
                        val x = normX * width
                        val y = dbToY(targetGain)

                        if (firstTargetPoint) {
                            targetPath.moveTo(x, y)
                            firstTargetPoint = false
                        } else {
                            targetPath.lineTo(x, y)
                        }
                    }

                    drawPath(
                        path = targetPath,
                        color = targetCurveColor.copy(alpha = 0.85f),
                        style = Stroke(
                            width = 2.dp.toPx(),
                            pathEffect = PathEffect.dashPathEffect(floatArrayOf(12f, 10f), 0f)
                        )
                    )
                }

                // 4. Calculate Combined Frequency Response Curve
                val path = Path()
                val fillPath = Path()
                val numPoints = 120
                val zeroY = dbToY(0f)

                var firstPoint = true

                for (i in 0..numPoints) {
                    val normX = i / numPoints.toFloat()
                    val logF = logMinFreq + normX * (logMaxFreq - logMinFreq)
                    val f = 10f.pow(logF)

                    var totalGainDb = preampGain
                    for (band in bands) {
                        if (!band.enabled || band.gain == 0f) continue
                        val f0 = band.frequency
                        val q = band.q.coerceAtLeast(0.1f)
                        val octaves = kotlin.math.ln(f / f0) / kotlin.math.ln(2.0f)

                        val response = when (band.type) {
                            FilterType.PEAK -> band.gain / (1.0f + (octaves * q).pow(2))
                            FilterType.LOW_SHELF -> if (f <= f0) band.gain else band.gain / (1.0f + (octaves * q).pow(2))
                            FilterType.HIGH_SHELF -> if (f >= f0) band.gain else band.gain / (1.0f + (octaves * q).pow(2))
                            FilterType.LOW_PASS -> if (f > f0) -24f * kotlin.math.log2(f / f0) else 0f
                            FilterType.HIGH_PASS -> if (f < f0) -24f * kotlin.math.log2(f0 / f) else 0f
                            FilterType.NOTCH -> if (kotlin.math.abs(f - f0) < f0 / (2 * q)) -24f else 0f
                            FilterType.BAND_PASS -> band.gain / (1.0f + (octaves * q).pow(2))
                        }
                        totalGainDb += response
                    }

                    val x = normX * width
                    val y = dbToY(totalGainDb)

                    if (firstPoint) {
                        path.moveTo(x, y)
                        fillPath.moveTo(x, zeroY)
                        fillPath.lineTo(x, y)
                        firstPoint = false
                    } else {
                        path.lineTo(x, y)
                        fillPath.lineTo(x, y)
                    }
                }

                fillPath.lineTo(width, zeroY)
                fillPath.close()

                // Draw Area Fill Gradient
                drawPath(
                    path = fillPath,
                    brush = Brush.verticalGradient(
                        colors = listOf(
                            primaryColor.copy(alpha = 0.35f),
                            primaryContainer.copy(alpha = 0.05f)
                        ),
                        startY = 0f,
                        endY = height
                    )
                )

                // Draw Active Curve Line
                drawPath(
                    path = path,
                    color = primaryColor,
                    style = Stroke(
                        width = 3.dp.toPx(),
                        cap = StrokeCap.Round
                    )
                )

                // 5. Draw Band Control Nodes
                for (band in bands) {
                    val x = freqToX(band.frequency)
                    val y = dbToY(band.gain + preampGain)

                    drawCircle(
                        color = primaryColor.copy(alpha = 0.25f),
                        radius = 8.dp.toPx(),
                        center = Offset(x, y)
                    )
                    drawCircle(
                        color = if (band.enabled) primaryColor else onSurfaceVariant,
                        radius = 4.5.dp.toPx(),
                        center = Offset(x, y)
                    )
                    drawCircle(
                        color = Color.White,
                        radius = 2.dp.toPx(),
                        center = Offset(x, y)
                    )
                }
            }
        }

        // Stereo Peak VU Meter Bar
        if (peakLevels != null) {
            val (levelL, levelR) = peakLevels
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 2.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(text = "L", style = MaterialTheme.typography.labelSmall, modifier = Modifier.width(14.dp))
                VuMeter(level = levelL, modifier = Modifier.weight(1f))
                Spacer(modifier = Modifier.width(8.dp))
                Text(text = "R", style = MaterialTheme.typography.labelSmall, modifier = Modifier.width(14.dp))
                VuMeter(level = levelR, modifier = Modifier.weight(1f))
            }
        }
    }
}

private fun calculateTargetCurveGain(target: TargetCurve, freq: Float): Float {
    return when (target) {
        TargetCurve.HARMAN_OVER_EAR -> {
            // Bass shelf +5dB tapering down to 0 at 200Hz
            val bass = if (freq < 200f) 5.5f * (1f - freq / 200f).pow(1.5f) else 0f
            // Ear gain around 3kHz (+8.5dB)
            val earGain = 8.5f * exp(-0.5f * (log10(freq / 3000f) / 0.25f).pow(2))
            // Treble shelf drop above 10kHz
            val treble = if (freq > 10000f) -4.0f * (freq - 10000f) / 10000f else 0f
            (bass + earGain + treble).coerceIn(-15f, 15f)
        }
        TargetCurve.HARMAN_IN_EAR -> {
            // Bass shelf +9dB tapering to 0 at 200Hz
            val bass = if (freq < 200f) 9.0f * (1f - freq / 200f).pow(1.5f) else 0f
            // Ear gain around 2.8kHz (+10.5dB)
            val earGain = 10.5f * exp(-0.5f * (log10(freq / 2800f) / 0.22f).pow(2))
            val treble = if (freq > 10000f) -3.0f * (freq - 10000f) / 10000f else 0f
            (bass + earGain + treble).coerceIn(-15f, 15f)
        }
        TargetCurve.IEF_NEUTRAL -> {
            // Flat bass + 7.5dB ear gain at 3kHz
            7.5f * exp(-0.5f * (log10(freq / 3000f) / 0.28f).pow(2))
        }
        TargetCurve.DIFFUSE_FIELD -> {
            // Diffuse field +11dB pinna resonance
            11.0f * exp(-0.5f * (log10(freq / 3000f) / 0.25f).pow(2)) - 2.0f
        }
        TargetCurve.FREE_FIELD -> {
            // Free field +13dB pinna resonance
            13.0f * exp(-0.5f * (log10(freq / 2800f) / 0.22f).pow(2)) - 3.0f
        }
        TargetCurve.NONE -> 0f
    }
}

@Composable
private fun VuMeter(level: Float, modifier: Modifier = Modifier) {
    val clamped = level.coerceIn(0f, 1.2f)
    val isClipping = clamped >= 1.0f
    val primary = MaterialTheme.colorScheme.primary
    val error = MaterialTheme.colorScheme.error

    Surface(
        modifier = modifier.height(6.dp),
        shape = RoundedCornerShape(3.dp),
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
    ) {
        Box(modifier = Modifier.fillMaxWidth()) {
            Canvas(modifier = Modifier.matchParentSize()) {
                val fillWidth = size.width * clamped.coerceAtMost(1f)
                drawRoundRect(
                    brush = Brush.horizontalGradient(
                        colors = listOf(
                            primary,
                            if (isClipping) error else primary
                        )
                    ),
                    size = Size(fillWidth, size.height),
                    cornerRadius = CornerRadius(3.dp.toPx(), 3.dp.toPx())
                )
            }
        }
    }
}
