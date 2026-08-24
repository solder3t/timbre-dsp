package com.timbre.dsp.ui.main

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.GraphicEq
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.timbre.dsp.ui.dashboard.DashboardScreen
import com.timbre.dsp.ui.effects.EffectsScreen
import com.timbre.dsp.ui.sessions.SessionsScreen
import com.timbre.dsp.ui.setup.PermissionSetupSheet
import dev.chrisbanes.haze.HazeState
import dev.chrisbanes.haze.hazeEffect
import dev.chrisbanes.haze.hazeSource
import dev.chrisbanes.haze.materials.ExperimentalHazeMaterialsApi
import dev.chrisbanes.haze.materials.HazeMaterials

data class NavTabItem(
    val title: String,
    val icon: ImageVector
)

@OptIn(ExperimentalHazeMaterialsApi::class)
@Composable
fun MainScreen(
    modifier: Modifier = Modifier,
    viewModel: MainScreenViewModel = viewModel()
) {
    var selectedTab by remember { mutableIntStateOf(0) }
    val hazeState = remember { HazeState() }
    val haptic = LocalHapticFeedback.current

    val settings by viewModel.settings.collectAsStateWithLifecycle()
    val permissionStatus by viewModel.permissionStatus.collectAsStateWithLifecycle()
    val routingStatus by viewModel.routingStatus.collectAsStateWithLifecycle()
    val activeSessions by viewModel.activeSessions.collectAsStateWithLifecycle()
    val presets by viewModel.presets.collectAsStateWithLifecycle()
    val currentDevice by viewModel.currentDevice.collectAsStateWithLifecycle()
    val fftMagnitudes by viewModel.fftMagnitudes.collectAsStateWithLifecycle()
    val peakLevels by viewModel.peakLevels.collectAsStateWithLifecycle()
    val appProfiles by viewModel.appProfiles.collectAsStateWithLifecycle()
    val irProfiles by viewModel.irProfiles.collectAsStateWithLifecycle()
    val isSleepTimerRunning by viewModel.isSleepTimerRunning.collectAsStateWithLifecycle()
    val sleepTimerSeconds by viewModel.sleepTimerSeconds.collectAsStateWithLifecycle()

    val navTabs = listOf(
        NavTabItem("Equalizer", Icons.Default.GraphicEq),
        NavTabItem("Effects", Icons.Default.Tune),
        NavTabItem("Sessions", Icons.Default.MusicNote),
        NavTabItem("Setup", Icons.Default.Security)
    )

    val primary = MaterialTheme.colorScheme.primary
    val primaryContainer = MaterialTheme.colorScheme.primaryContainer
    val onPrimaryContainer = MaterialTheme.colorScheme.onPrimaryContainer
    val onSurface = MaterialTheme.colorScheme.onSurface
    val onSurfaceVariant = MaterialTheme.colorScheme.onSurfaceVariant

    Scaffold(
        modifier = modifier.fillMaxSize(),
        bottomBar = {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .hazeEffect(
                        state = hazeState,
                        style = HazeMaterials.thin(MaterialTheme.colorScheme.surface.copy(alpha = 0.72f))
                    )
            ) {
                // Shimmer top divider line (matches Musaic Player)
                Box(
                    modifier = Modifier
                        .matchParentSize()
                        .drawBehind {
                            drawLine(
                                brush = Brush.horizontalGradient(
                                    colors = listOf(
                                        Color.Transparent,
                                        onSurface.copy(alpha = 0.12f),
                                        primary.copy(alpha = 0.35f),
                                        onSurface.copy(alpha = 0.12f),
                                        Color.Transparent
                                    )
                                ),
                                start = Offset(0f, 0f),
                                end = Offset(size.width, 0f),
                                strokeWidth = 1.dp.toPx()
                            )
                        }
                )

                NavigationBar(
                    containerColor = Color.Transparent,
                    contentColor = onSurface,
                    tonalElevation = 0.dp,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(76.dp)
                ) {
                    navTabs.forEachIndexed { index, tab ->
                        val isSelected = selectedTab == index

                        val scale by animateFloatAsState(
                            targetValue = if (isSelected) 1.18f else 1.0f,
                            animationSpec = spring(
                                dampingRatio = Spring.DampingRatioMediumBouncy,
                                stiffness = Spring.StiffnessMedium
                            ),
                            label = "tab_icon_scale"
                        )

                        val pillColor by animateColorAsState(
                            targetValue = if (isSelected) primaryContainer.copy(alpha = 0.90f) else Color.Transparent,
                            animationSpec = tween(durationMillis = 200),
                            label = "tab_pill_color"
                        )

                        val iconTint by animateColorAsState(
                            targetValue = if (isSelected) onPrimaryContainer else onSurfaceVariant.copy(alpha = 0.70f),
                            animationSpec = tween(durationMillis = 200),
                            label = "tab_icon_tint"
                        )

                        val labelColor by animateColorAsState(
                            targetValue = if (isSelected) primary else onSurfaceVariant.copy(alpha = 0.65f),
                            animationSpec = tween(durationMillis = 200),
                            label = "tab_label_color"
                        )

                        NavigationBarItem(
                            selected = isSelected,
                            onClick = {
                                if (selectedTab != index) {
                                    haptic.performHapticFeedback(HapticFeedbackType.VirtualKey)
                                    selectedTab = index
                                }
                            },
                            icon = {
                                Box(
                                    modifier = Modifier
                                        .size(width = 56.dp, height = 30.dp)
                                        .clip(RoundedCornerShape(15.dp))
                                        .background(pillColor),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = tab.icon,
                                        contentDescription = tab.title,
                                        tint = iconTint,
                                        modifier = Modifier
                                            .size(24.dp)
                                            .graphicsLayer {
                                                scaleX = scale
                                                scaleY = scale
                                            }
                                    )
                                }
                            },
                            label = {
                                Text(
                                    text = tab.title,
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                    color = labelColor,
                                    maxLines = 1
                                )
                            },
                            alwaysShowLabel = true,
                            colors = NavigationBarItemDefaults.colors(
                                selectedIconColor = onPrimaryContainer,
                                selectedTextColor = primary,
                                indicatorColor = Color.Transparent,
                                unselectedIconColor = onSurfaceVariant.copy(alpha = 0.70f),
                                unselectedTextColor = onSurfaceVariant.copy(alpha = 0.65f)
                            )
                        )
                    }
                }
            }
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .hazeSource(state = hazeState)
                .padding(innerPadding)
        ) {
            when (selectedTab) {
                0 -> DashboardScreen(
                    settings = settings,
                    permissionStatus = permissionStatus,
                    routingStatus = routingStatus,
                    presets = presets,
                    currentDevice = currentDevice,
                    fftMagnitudes = fftMagnitudes,
                    peakLevels = peakLevels,
                    onToggleMaster = { viewModel.toggleMaster(it) },
                    onSetEQMode = { viewModel.setEQMode(it) },
                    onBandGainChange = { index, gain -> viewModel.setBandGain(index, gain) },
                    onUpdateParametricBand = { viewModel.updateParametricBand(it) },
                    onAddParametricBand = { viewModel.addParametricBand() },
                    onDeleteParametricBand = { viewModel.deleteParametricBand(it) },
                    onResetBands = { viewModel.resetBands() },
                    onSelectPreset = { viewModel.selectPreset(it) },
                    onApplyAutoEq = { viewModel.applyAutoEq(it) },
                    onApplyImportedPreset = { viewModel.applyImportedPreset(it) },
                    onSaveCustomPreset = { viewModel.saveCustomPreset(it) },
                    onBindCurrentDevice = { viewModel.bindPresetToCurrentDevice(settings.currentPresetId) },
                    onSetTargetCurve = { viewModel.setTargetCurve(it) },
                    onNavigateToSetup = { selectedTab = 3 },
                    onApplyAiSettings = { viewModel.applyAiSettings(it) },
                    hazeState = hazeState
                )
                1 -> EffectsScreen(
                    settings = settings,
                    irProfiles = irProfiles,
                    onPreampGainChange = { viewModel.setPreampGain(it) },
                    onAutoPreampChange = { viewModel.setAutoPreamp(it) },
                    onChannelBalanceChange = { viewModel.setChannelBalance(it) },
                    onMonoChange = { viewModel.setMono(it) },
                    onLimiterChange = { viewModel.setLimiter(it) },
                    onBassBoostChange = { enabled, gain, cutoff -> viewModel.setBassBoost(enabled, gain, cutoff) },
                    onCrossfeedChange = { enabled, strength -> viewModel.setCrossfeed(enabled, strength) },
                    onVirtualizerChange = { enabled, strength -> viewModel.setVirtualizer(enabled, strength) },
                    onClarityChange = { enabled, gain -> viewModel.setClarity(enabled, gain) },
                    onConvolutionChange = { enabled, profileId, wetDry -> viewModel.setConvolution(enabled, profileId, wetDry) },
                    onImportCustomIR = { uri, name -> viewModel.importCustomIR(uri, name) },
                    onApplyHearingAudiogram = { audiogram, preset -> viewModel.applyHearingAudiogram(audiogram, preset) },
                    hazeState = hazeState
                )
                2 -> SessionsScreen(
                    activeSessions = activeSessions,
                    appProfiles = appProfiles,
                    installedApps = viewModel.getInstalledMediaApps(),
                    presets = presets,
                    onRescan = { viewModel.rescanSessions() },
                    onBindAppPreset = { pkg, name, presetId -> viewModel.bindAppToPreset(pkg, name, presetId) },
                    onToggleAppProfile = { pkg, enabled -> viewModel.toggleAppProfile(pkg, enabled) },
                    onUpdateAppProfile = { pkg, presetId, enabled -> viewModel.updateAppProfile(pkg, presetId, enabled) },
                    onRemoveAppProfile = { pkg -> viewModel.removeAppProfile(pkg) },
                    hazeState = hazeState
                )
                3 -> PermissionSetupSheet(
                    permissionStatus = permissionStatus,
                    isSleepTimerRunning = isSleepTimerRunning,
                    sleepTimerSeconds = sleepTimerSeconds,
                    onRequestShizuku = { viewModel.requestShizukuPermission() },
                    onGrantDumpShizuku = { viewModel.grantDumpViaShizuku() },
                    onGrantDumpRoot = { viewModel.grantDumpViaRoot() },
                    onOpenNotificationSettings = { viewModel.openNotificationSettings() },
                    onRequestBatteryOptimization = { viewModel.requestBatteryOptimization() },
                    onStartSleepTimer = { viewModel.startSleepTimer(it) },
                    onCancelSleepTimer = { viewModel.cancelSleepTimer() },
                    onExportBackup = { viewModel.exportFullBackup(it) },
                    onImportBackup = { viewModel.importFullBackup(it) },
                    onRefresh = { viewModel.refreshPermissions() },
                    hazeState = hazeState
                )
            }
        }
    }
}
