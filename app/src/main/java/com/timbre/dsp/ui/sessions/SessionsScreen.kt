package com.timbre.dsp.ui.sessions

import android.media.AudioAttributes
import android.media.AudioFormat
import android.media.AudioTrack
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Apps
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.timbre.dsp.DSPEngine
import com.timbre.dsp.R
import com.timbre.dsp.audio.InstalledAppItem
import com.timbre.dsp.model.AppProfile
import com.timbre.dsp.model.AudioSessionInfo
import com.timbre.dsp.model.EQPreset
import com.timbre.dsp.ui.components.AddAppProfileDialog
import com.timbre.dsp.ui.components.GlassmorphicCard
import dev.chrisbanes.haze.HazeState
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import java.util.Locale
import kotlin.math.sin

@Composable
fun SessionsScreen(
    activeSessions: List<AudioSessionInfo>,
    appProfiles: List<AppProfile>,
    installedApps: List<InstalledAppItem>,
    presets: List<EQPreset>,
    onRescan: () -> Unit,
    onBindAppPreset: (packageName: String, appName: String, presetId: String) -> Unit,
    onToggleAppProfile: (packageName: String, isEnabled: Boolean) -> Unit,
    onUpdateAppProfile: (packageName: String, presetId: String, isEnabled: Boolean) -> Unit,
    onRemoveAppProfile: (packageName: String) -> Unit,
    hazeState: HazeState? = null,
    modifier: Modifier = Modifier
) {
    val scope = rememberCoroutineScope()
    var isTestPlaying by remember { mutableStateOf(false) }
    var playbackJob by remember { mutableStateOf<Job?>(null) }
    var audioTrack by remember { mutableStateOf<AudioTrack?>(null) }
    var showAddAppDialog by remember { mutableStateOf(false) }

    if (showAddAppDialog) {
        AddAppProfileDialog(
            installedApps = installedApps,
            presets = presets,
            onDismiss = { showAddAppDialog = false },
            onAddProfile = { pkg, name, presetId ->
                onBindAppPreset(pkg, name, presetId)
                showAddAppDialog = false
            }
        )
    }

    DisposableEffect(Unit) {
        onDispose {
            playbackJob?.cancel()
            try {
                audioTrack?.stop()
                audioTrack?.release()
            } catch (e: Exception) {}
        }
    }

    Box(
        modifier = modifier.fillMaxSize(),
        contentAlignment = Alignment.TopCenter
    ) {
        LazyColumn(
            modifier = Modifier
                .widthIn(max = 840.dp)
                .fillMaxSize(),
            contentPadding = androidx.compose.foundation.layout.PaddingValues(
                start = 16.dp,
                end = 16.dp,
                top = 16.dp,
                bottom = 96.dp
            ),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = stringResource(R.string.sessions_header_title),
                        style = MaterialTheme.typography.headlineSmall
                    )
                    Text(
                        text = stringResource(R.string.sessions_count_format, activeSessions.size),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                IconButton(onClick = onRescan) {
                    Icon(Icons.Default.Refresh, contentDescription = stringResource(R.string.sessions_rescan))
                }
            }
        }

        // 1. Built-in In-App Test Sound Generator Card
        item {
            GlassmorphicCard(
                modifier = Modifier.fillMaxWidth(),
                hazeState = hazeState,
                containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.35f)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "In-App Audio Real-Time Test Player",
                        style = MaterialTheme.typography.titleMedium
                    )
                    Text(
                        text = "Generates a multi-harmonic musical chord processed directly through Timbre DSP engine to test EQ settings instantly.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Button(
                        onClick = {
                            if (isTestPlaying) {
                                playbackJob?.cancel()
                                try {
                                    audioTrack?.stop()
                                    audioTrack?.release()
                                } catch (e: Exception) {}
                                audioTrack = null
                                isTestPlaying = false
                            } else {
                                isTestPlaying = true
                                playbackJob = scope.launch(Dispatchers.IO) {
                                    val sampleRate = 48000
                                    val bufferSize = AudioTrack.getMinBufferSize(
                                        sampleRate,
                                        AudioFormat.CHANNEL_OUT_STEREO,
                                        AudioFormat.ENCODING_PCM_FLOAT
                                    ).coerceAtLeast(4096)

                                    val track = AudioTrack.Builder()
                                        .setAudioAttributes(
                                            AudioAttributes.Builder()
                                                .setUsage(AudioAttributes.USAGE_MEDIA)
                                                .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                                                .build()
                                        )
                                        .setAudioFormat(
                                            AudioFormat.Builder()
                                                .setEncoding(AudioFormat.ENCODING_PCM_FLOAT)
                                                .setSampleRate(sampleRate)
                                                .setChannelMask(AudioFormat.CHANNEL_OUT_STEREO)
                                                .build()
                                        )
                                        .setBufferSizeInBytes(bufferSize * 4)
                                        .setTransferMode(AudioTrack.MODE_STREAM)
                                        .build()

                                    audioTrack = track
                                    track.play()

                                    val numFrames = 1024
                                    val buffer = FloatArray(numFrames * 2)
                                    var phase1 = 0.0
                                    var phase2 = 0.0
                                    var phase3 = 0.0
                                    val f1 = 220.0 // A3
                                    val f2 = 277.18 // C#4
                                    val f3 = 329.63 // E4

                                    try {
                                        while (isActive && isTestPlaying) {
                                            for (i in 0 until numFrames) {
                                                val sample = (sin(phase1) * 0.2 + sin(phase2) * 0.15 + sin(phase3) * 0.15).toFloat()
                                                buffer[i * 2] = sample
                                                buffer[i * 2 + 1] = sample

                                                phase1 += 2.0 * Math.PI * f1 / sampleRate
                                                phase2 += 2.0 * Math.PI * f2 / sampleRate
                                                phase3 += 2.0 * Math.PI * f3 / sampleRate
                                            }

                                            DSPEngine.process(buffer)

                                            track.write(buffer, 0, buffer.size, AudioTrack.WRITE_BLOCKING)
                                        }
                                    } catch (e: Exception) {}
                                }
                            }
                        },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (isTestPlaying) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary
                        ),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(
                            imageVector = if (isTestPlaying) Icons.Default.Stop else Icons.Default.PlayArrow,
                            contentDescription = null
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(if (isTestPlaying) "Stop Test Audio" else "Play Test Audio (Triad Chord)")
                    }
                }
            }
        }

        // 2. Discovered Active Player Sessions List
        item {
            Text(
                text = stringResource(R.string.sessions_active_header, activeSessions.size),
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier.padding(top = 4.dp)
            )
        }

        if (activeSessions.isEmpty()) {
            item {
                GlassmorphicCard(
                    modifier = Modifier.fillMaxWidth(),
                    hazeState = hazeState,
                    shape = RoundedCornerShape(16.dp),
                    containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)
                ) {
                    Column(
                        modifier = Modifier.fillMaxWidth().padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Icon(
                            Icons.Default.MusicNote,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(bottom = 8.dp)
                        )
                        Text(
                            text = stringResource(R.string.sessions_no_active_title),
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = stringResource(R.string.sessions_no_active_desc),
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                            modifier = Modifier.padding(top = 4.dp)
                        )
                    }
                }
            }
        } else {
            val groupedSessions = activeSessions.groupBy { it.packageName }.values.toList()
            items(groupedSessions) { sessionsForApp ->
                val primarySession = sessionsForApp.first()
                SessionItemCard(
                    session = primarySession,
                    sessionCount = sessionsForApp.size,
                    presets = presets,
                    hazeState = hazeState,
                    onBindPreset = { presetId ->
                        onBindAppPreset(primarySession.packageName, primarySession.appName, presetId)
                    }
                )
            }
        }

        // 3. User-Selectable Per-App Profile Rules List
        item {
            Spacer(modifier = Modifier.height(8.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    modifier = Modifier
                        .weight(1f)
                        .padding(end = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(Icons.Default.Apps, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = stringResource(R.string.sessions_configure_app_profiles_title, appProfiles.size),
                        style = MaterialTheme.typography.titleMedium,
                        maxLines = 2,
                        overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis
                    )
                }

                OutlinedButton(
                    onClick = { showAddAppDialog = true },
                    contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 12.dp, vertical = 6.dp)
                ) {
                    Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = stringResource(R.string.sessions_add_rule),
                        style = MaterialTheme.typography.labelMedium,
                        maxLines = 1,
                        softWrap = false
                    )
                }
            }
        }

        items(appProfiles) { profile ->
            ConfigureAppProfileCard(
                profile = profile,
                presets = presets,
                hazeState = hazeState,
                onToggleEnabled = { onToggleAppProfile(profile.packageName, it) },
                onSelectPreset = { onUpdateAppProfile(profile.packageName, it, profile.isEnabled) },
                onDelete = { onRemoveAppProfile(profile.packageName) }
            )
        }

        item {
            Spacer(modifier = Modifier.height(32.dp))
        }
    }
}
}

@Composable
private fun ConfigureAppProfileCard(
    profile: AppProfile,
    presets: List<EQPreset>,
    hazeState: HazeState? = null,
    onToggleEnabled: (Boolean) -> Unit,
    onSelectPreset: (String) -> Unit,
    onDelete: () -> Unit
) {
    var dropdownExpanded by remember { mutableStateOf(false) }
    val currentPresetName = remember(profile.presetId, presets) {
        presets.find { it.id == profile.presetId }?.name ?: profile.presetId.replace("_", " ").replaceFirstChar { it.titlecase(Locale.ROOT) }
    }

    GlassmorphicCard(
        modifier = Modifier.fillMaxWidth(),
        hazeState = hazeState,
        shape = RoundedCornerShape(12.dp),
        containerColor = if (profile.isEnabled)
            MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
        else
            MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.25f)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = profile.appName,
                    style = MaterialTheme.typography.bodyLarge,
                    color = if (profile.isEnabled) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = profile.packageName,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(6.dp))

                // Select Preset Chip
                Row(verticalAlignment = Alignment.CenterVertically) {
                    OutlinedButton(
                        onClick = { dropdownExpanded = true },
                        modifier = Modifier.height(32.dp),
                        contentPadding = ButtonDefaults.TextButtonContentPadding
                    ) {
                        Icon(Icons.Default.Tune, contentDescription = null, modifier = Modifier.height(14.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(currentPresetName, style = MaterialTheme.typography.labelSmall)
                    }

                    DropdownMenu(
                        expanded = dropdownExpanded,
                        onDismissRequest = { dropdownExpanded = false }
                    ) {
                        presets.forEach { preset ->
                            DropdownMenuItem(
                                text = { Text(preset.name) },
                                onClick = {
                                    onSelectPreset(preset.id)
                                    dropdownExpanded = false
                                }
                            )
                        }
                    }
                }
            }

            Row(verticalAlignment = Alignment.CenterVertically) {
                Switch(
                    checked = profile.isEnabled,
                    onCheckedChange = onToggleEnabled
                )
                IconButton(onClick = onDelete) {
                    Icon(
                        Icons.Default.Delete,
                        contentDescription = stringResource(R.string.sessions_delete_profile),
                        tint = MaterialTheme.colorScheme.error.copy(alpha = 0.8f)
                    )
                }
            }
        }
    }
}

@Composable
private fun SessionItemCard(
    session: AudioSessionInfo,
    sessionCount: Int = 1,
    presets: List<EQPreset>,
    hazeState: HazeState? = null,
    onBindPreset: (String) -> Unit
) {
    var dropdownExpanded by remember { mutableStateOf(false) }

    GlassmorphicCard(
        modifier = Modifier.fillMaxWidth(),
        hazeState = hazeState,
        shape = RoundedCornerShape(12.dp),
        containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.55f)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                Icon(Icons.Default.MusicNote, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                Spacer(modifier = Modifier.width(12.dp))
                Column {
                    Text(
                        text = session.appName,
                        style = MaterialTheme.typography.titleMedium
                    )
                    val streamText = if (sessionCount > 1) "$sessionCount active audio streams" else "Session #${session.sessionId}"
                    Text(
                        text = "${session.packageName} • $streamText",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            Column {
                OutlinedButton(
                    onClick = { dropdownExpanded = true }
                ) {
                    Icon(Icons.Default.Tune, contentDescription = null)
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(stringResource(R.string.sessions_auto_profile_tag))
                }

                DropdownMenu(
                    expanded = dropdownExpanded,
                    onDismissRequest = { dropdownExpanded = false }
                ) {
                    presets.forEach { preset ->
                        DropdownMenuItem(
                            text = { Text(preset.name) },
                            onClick = {
                                onBindPreset(preset.id)
                                dropdownExpanded = false
                            }
                        )
                    }
                }
            }
        }
    }
}
