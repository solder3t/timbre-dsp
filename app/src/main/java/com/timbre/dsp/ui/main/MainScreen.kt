package com.timbre.dsp.ui.main

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.GraphicEq
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.timbre.dsp.ui.dashboard.DashboardScreen
import com.timbre.dsp.ui.effects.EffectsScreen
import com.timbre.dsp.ui.sessions.SessionsScreen
import com.timbre.dsp.ui.setup.PermissionSetupSheet

@Composable
fun MainScreen(
    modifier: Modifier = Modifier,
    viewModel: MainScreenViewModel = viewModel()
) {
    var selectedTab by remember { mutableIntStateOf(0) }

    val settings by viewModel.settings.collectAsStateWithLifecycle()
    val permissionStatus by viewModel.permissionStatus.collectAsStateWithLifecycle()
    val routingStatus by viewModel.routingStatus.collectAsStateWithLifecycle()
    val activeSessions by viewModel.activeSessions.collectAsStateWithLifecycle()
    val presets by viewModel.presets.collectAsStateWithLifecycle()
    val currentDevice by viewModel.currentDevice.collectAsStateWithLifecycle()
    val fftMagnitudes by viewModel.fftMagnitudes.collectAsStateWithLifecycle()
    val peakLevels by viewModel.peakLevels.collectAsStateWithLifecycle()
    val appProfiles by viewModel.appProfiles.collectAsStateWithLifecycle()

    Scaffold(
        modifier = modifier.fillMaxSize(),
        bottomBar = {
            NavigationBar {
                NavigationBarItem(
                    selected = selectedTab == 0,
                    onClick = { selectedTab = 0 },
                    icon = { Icon(Icons.Default.GraphicEq, contentDescription = "Equalizer") },
                    label = { Text("Equalizer") }
                )
                NavigationBarItem(
                    selected = selectedTab == 1,
                    onClick = { selectedTab = 1 },
                    icon = { Icon(Icons.Default.Tune, contentDescription = "Effects") },
                    label = { Text("Effects") }
                )
                NavigationBarItem(
                    selected = selectedTab == 2,
                    onClick = { selectedTab = 2 },
                    icon = { Icon(Icons.Default.MusicNote, contentDescription = "Sessions") },
                    label = { Text("Sessions") }
                )
                NavigationBarItem(
                    selected = selectedTab == 3,
                    onClick = { selectedTab = 3 },
                    icon = { Icon(Icons.Default.Security, contentDescription = "Setup") },
                    label = { Text("Setup") }
                )
            }
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
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
                    onNavigateToSetup = { selectedTab = 3 }
                )
                1 -> EffectsScreen(
                    settings = settings,
                    onPreampGainChange = { viewModel.setPreampGain(it) },
                    onChannelBalanceChange = { viewModel.setChannelBalance(it) },
                    onMonoChange = { viewModel.setMono(it) },
                    onLimiterChange = { viewModel.setLimiter(it) },
                    onBassBoostChange = { enabled, gain, cutoff -> viewModel.setBassBoost(enabled, gain, cutoff) },
                    onCrossfeedChange = { enabled, strength -> viewModel.setCrossfeed(enabled, strength) },
                    onVirtualizerChange = { enabled, strength -> viewModel.setVirtualizer(enabled, strength) },
                    onClarityChange = { enabled, gain -> viewModel.setClarity(enabled, gain) },
                    onApplyHearingAudiogram = { audiogram, preset -> viewModel.applyHearingAudiogram(audiogram, preset) }
                )
                2 -> SessionsScreen(
                    activeSessions = activeSessions,
                    appProfiles = appProfiles,
                    presets = presets,
                    onRescan = { viewModel.rescanSessions() },
                    onBindAppPreset = { pkg, name, presetId -> viewModel.bindAppToPreset(pkg, name, presetId) }
                )
                3 -> PermissionSetupSheet(
                    permissionStatus = permissionStatus,
                    onRequestShizuku = { viewModel.requestShizukuPermission() },
                    onGrantDumpShizuku = { viewModel.grantDumpViaShizuku() },
                    onGrantDumpRoot = { viewModel.grantDumpViaRoot() },
                    onOpenNotificationSettings = { viewModel.openNotificationSettings() },
                    onRequestBatteryOptimization = { viewModel.requestBatteryOptimization() },
                    onRefresh = { viewModel.refreshPermissions() }
                )
            }
        }
    }
}
