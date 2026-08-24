package com.timbre.dsp.ui.main

import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.GraphicEq
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.NavigationRail
import androidx.compose.material3.NavigationRailItem
import androidx.compose.material3.NavigationRailItemDefaults
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
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.timbre.dsp.R
import com.timbre.dsp.ui.dashboard.DashboardScreen
import com.timbre.dsp.ui.effects.EffectsScreen
import com.timbre.dsp.ui.sessions.SessionsScreen
import com.timbre.dsp.ui.settings.SettingsScreen
import com.timbre.dsp.ui.utils.WindowWidthClass
import com.timbre.dsp.ui.utils.rememberWindowWidthClass

data class NavTabItem(
    val title: String,
    val icon: ImageVector
)

@Composable
fun MainScreen(
    modifier: Modifier = Modifier,
    viewModel: MainScreenViewModel = viewModel()
) {
    var selectedTab by remember { mutableIntStateOf(0) }
    val haptic = LocalHapticFeedback.current
    val windowWidthClass = rememberWindowWidthClass()
    val isCompact = windowWidthClass == WindowWidthClass.COMPACT

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
    val themeSettings by viewModel.themeSettings.collectAsStateWithLifecycle()

    val navTabs = listOf(
        NavTabItem(stringResource(R.string.nav_equalizer), Icons.Default.GraphicEq),
        NavTabItem(stringResource(R.string.nav_effects), Icons.Default.Tune),
        NavTabItem(stringResource(R.string.nav_sessions), Icons.Default.MusicNote),
        NavTabItem(stringResource(R.string.nav_settings), Icons.Default.Settings)
    )

    val primary = MaterialTheme.colorScheme.primary
    val primaryContainer = MaterialTheme.colorScheme.primaryContainer
    val onPrimaryContainer = MaterialTheme.colorScheme.onPrimaryContainer
    val onSurface = MaterialTheme.colorScheme.onSurface
    val onSurfaceVariant = MaterialTheme.colorScheme.onSurfaceVariant

    @Composable
    fun TabContent() {
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
                onApplyImportedPreset = { preset, saveToLibrary -> viewModel.applyImportedPreset(preset, saveToLibrary) },
                onSaveCustomPreset = { viewModel.saveCustomPreset(it) },
                onDeleteCustomPreset = { viewModel.deleteCustomPreset(it) },
                onBindCurrentDevice = { viewModel.bindPresetToCurrentDevice(settings.currentPresetId) },
                onSetTargetCurve = { viewModel.setTargetCurve(it) },
                onNavigateToSetup = { selectedTab = 3 },
                onApplyAiSettings = { viewModel.applyAiSettings(it) }
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
                onApplyHearingAudiogram = { audiogram, preset -> viewModel.applyHearingAudiogram(audiogram, preset) }
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
                onRemoveAppProfile = { pkg -> viewModel.removeAppProfile(pkg) }
            )
            3 -> SettingsScreen(
                settings = settings,
                themeSettings = themeSettings,
                permissionStatus = permissionStatus,
                isSleepTimerRunning = isSleepTimerRunning,
                sleepTimerSeconds = sleepTimerSeconds,
                onSetThemeMode = { viewModel.setThemeMode(it) },
                onSetDynamicColor = { viewModel.setDynamicColor(it) },
                onSetSeedColor = { viewModel.setSeedColor(it) },
                onSetRoutingMode = { viewModel.setRoutingMode(it) },
                onToggleLimiter = { viewModel.toggleLimiter(it) },
                onToggleVisualizer = { viewModel.toggleVisualizer(it) },
                onRequestShizuku = { viewModel.requestShizukuPermission() },
                onGrantDumpShizuku = { viewModel.grantDumpViaShizuku() },
                onGrantDumpRoot = { viewModel.grantDumpViaRoot() },
                onOpenNotificationSettings = { viewModel.openNotificationSettings() },
                onRequestBatteryOptimization = { viewModel.requestBatteryOptimization() },
                onStartSleepTimer = { viewModel.startSleepTimer(it) },
                onCancelSleepTimer = { viewModel.cancelSleepTimer() },
                onExportBackup = { viewModel.exportFullBackup(it) },
                onImportBackup = { viewModel.importFullBackup(it) },
                onRefresh = { viewModel.refreshPermissions() }
            )
        }
    }

    if (isCompact) {
        // COMPACT: Scaffold with standard solid Material 3 NavigationBar
        Scaffold(
            bottomBar = {
                NavigationBar(
                    containerColor = MaterialTheme.colorScheme.surface,
                    contentColor = onSurface,
                    tonalElevation = 2.dp,
                    modifier = Modifier
                        .fillMaxWidth()
                        .drawBehind {
                            drawLine(
                                color = onSurface.copy(alpha = 0.08f),
                                start = Offset(0f, 0f),
                                end = Offset(size.width, 0f),
                                strokeWidth = 1.dp.toPx()
                            )
                        }
                ) {
                    navTabs.forEachIndexed { index, tab ->
                        val isSelected = selectedTab == index

                        val scale by animateFloatAsState(
                            targetValue = if (isSelected) 1.15f else 1.0f,
                            animationSpec = spring(
                                dampingRatio = Spring.DampingRatioMediumBouncy,
                                stiffness = Spring.StiffnessMedium
                            ),
                            label = "tab_icon_scale"
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
                                Icon(
                                    imageVector = tab.icon,
                                    contentDescription = tab.title,
                                    modifier = Modifier
                                        .size(24.dp)
                                        .graphicsLayer {
                                            scaleX = scale
                                            scaleY = scale
                                        }
                                )
                            },
                            label = {
                                Text(
                                    text = tab.title,
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                    maxLines = 1
                                )
                            },
                            alwaysShowLabel = true,
                            colors = NavigationBarItemDefaults.colors(
                                selectedIconColor = onPrimaryContainer,
                                selectedTextColor = primary,
                                indicatorColor = primaryContainer,
                                unselectedIconColor = onSurfaceVariant.copy(alpha = 0.70f),
                                unselectedTextColor = onSurfaceVariant.copy(alpha = 0.65f)
                            )
                        )
                    }
                }
            },
            modifier = modifier
                .fillMaxSize()
                .statusBarsPadding()
        ) { innerPadding ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
            ) {
                TabContent()
            }
        }
    } else {
        // MEDIUM / EXPANDED (Tablets, Foldables, Landscape): Side Navigation Rail
        Row(
            modifier = modifier
                .fillMaxSize()
                .statusBarsPadding()
                .navigationBarsPadding()
        ) {
            NavigationRail(
                containerColor = MaterialTheme.colorScheme.surface,
                contentColor = onSurface,
                header = {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.padding(vertical = 16.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(48.dp)
                                .clip(CircleShape)
                                .background(MaterialTheme.colorScheme.primary),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                Icons.Default.GraphicEq,
                                contentDescription = "Timbre DSP",
                                tint = MaterialTheme.colorScheme.onPrimary,
                                modifier = Modifier.size(28.dp)
                            )
                        }
                    }
                },
                modifier = Modifier
                    .width(96.dp)
                    .fillMaxHeight()
                    .drawBehind {
                        drawLine(
                            color = onSurface.copy(alpha = 0.08f),
                            start = Offset(size.width, 0f),
                            end = Offset(size.width, size.height),
                            strokeWidth = 1.dp.toPx()
                        )
                    }
            ) {
                Spacer(modifier = Modifier.height(16.dp))
                navTabs.forEachIndexed { index, tab ->
                    val isSelected = selectedTab == index

                    val scale by animateFloatAsState(
                        targetValue = if (isSelected) 1.15f else 1.0f,
                        animationSpec = spring(
                            dampingRatio = Spring.DampingRatioMediumBouncy,
                            stiffness = Spring.StiffnessMedium
                        ),
                        label = "rail_tab_icon_scale"
                    )

                    NavigationRailItem(
                        selected = isSelected,
                        onClick = {
                            if (selectedTab != index) {
                                haptic.performHapticFeedback(HapticFeedbackType.VirtualKey)
                                selectedTab = index
                            }
                        },
                        icon = {
                            Icon(
                                imageVector = tab.icon,
                                contentDescription = tab.title,
                                modifier = Modifier
                                    .size(24.dp)
                                    .graphicsLayer {
                                        scaleX = scale
                                        scaleY = scale
                                    }
                            )
                        },
                        label = {
                            Text(
                                text = tab.title,
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                maxLines = 1
                            )
                        },
                        alwaysShowLabel = true,
                        colors = NavigationRailItemDefaults.colors(
                            selectedIconColor = onPrimaryContainer,
                            selectedTextColor = primary,
                            indicatorColor = primaryContainer,
                            unselectedIconColor = onSurfaceVariant.copy(alpha = 0.70f),
                            unselectedTextColor = onSurfaceVariant.copy(alpha = 0.65f)
                        )
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                }
            }

            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight()
            ) {
                TabContent()
            }
        }
    }
}
