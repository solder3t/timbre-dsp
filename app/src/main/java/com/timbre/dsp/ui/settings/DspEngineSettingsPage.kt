package com.timbre.dsp.ui.settings

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Equalizer
import androidx.compose.material.icons.filled.GraphicEq
import androidx.compose.material.icons.filled.Router
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.timbre.dsp.R
import com.timbre.dsp.model.DSPSettings
import com.timbre.dsp.model.RoutingMode
import dev.chrisbanes.haze.HazeState

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DspEngineSettingsPage(
    settings: DSPSettings,
    onSetRoutingMode: (RoutingMode) -> Unit,
    onToggleLimiter: (Boolean) -> Unit,
    onToggleVisualizer: (Boolean) -> Unit,
    onBack: () -> Unit,
    hazeState: HazeState? = null
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 16.dp)
    ) {
        TopAppBar(
            title = {
                Text(
                    text = stringResource(R.string.settings_section_engine),
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )
            },
            navigationIcon = {
                IconButton(onClick = onBack) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = stringResource(R.string.btn_close)
                    )
                }
            },
            colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Transparent)
        )

        Spacer(modifier = Modifier.height(8.dp))

        // 1. Audio Routing Mode Card
        SettingsCard(hazeState = hazeState) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Router, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                    Spacer(modifier = Modifier.width(8.dp))
                    Column {
                        Text(
                            text = stringResource(R.string.engine_routing_title),
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.SemiBold
                        )
                        Text(
                            text = stringResource(R.string.engine_routing_desc),
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    listOf(
                        RoutingMode.AUTO to "Auto",
                        RoutingMode.SHIZUKU to "Shizuku",
                        RoutingMode.ROOT to "Root",
                        RoutingMode.BROADCAST to "Session"
                    ).forEach { (mode, label) ->
                        val isSelected = settings.routingMode == mode
                        FilterChip(
                            selected = isSelected,
                            onClick = { onSetRoutingMode(mode) },
                            label = { Text(label) },
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // 2. Audio Processing Controls Card
        SettingsCard(hazeState = hazeState) {
            Column(modifier = Modifier.padding(16.dp)) {
                // Headroom Peak Limiter
                SettingsSwitchRow(
                    icon = Icons.Default.Speed,
                    title = stringResource(R.string.engine_limiter_title),
                    subtitle = stringResource(R.string.engine_limiter_desc),
                    checked = settings.limiterEnabled,
                    onCheckedChange = onToggleLimiter
                )

                HorizontalDivider(
                    modifier = Modifier.padding(vertical = 12.dp),
                    color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f)
                )

                // Live Audio Visualizer
                SettingsSwitchRow(
                    icon = Icons.Default.GraphicEq,
                    title = stringResource(R.string.engine_visualizer_title),
                    subtitle = stringResource(R.string.engine_visualizer_desc),
                    checked = settings.isVisualizerEnabled,
                    onCheckedChange = onToggleVisualizer
                )
            }
        }

        Spacer(modifier = Modifier.height(100.dp))
    }
}
