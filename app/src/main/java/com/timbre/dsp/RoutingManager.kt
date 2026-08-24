package com.timbre.dsp

import android.content.Context
import android.util.Log
import com.timbre.dsp.audio.AudioEffectManager
import com.timbre.dsp.audio.AudioSessionTracker
import com.timbre.dsp.model.RoutingMode
import com.timbre.dsp.permission.PermissionManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

data class RoutingStatus(
    val selectedMode: RoutingMode = RoutingMode.AUTO,
    val effectiveMode: RoutingMode = RoutingMode.STANDALONE,
    val statusDescription: String = "Initializing",
    val isOperational: Boolean = false
)

class RoutingManager(private val context: Context) {

    private val permissionManager = PermissionManager(context)
    private val _status = MutableStateFlow(RoutingStatus())
    val status: StateFlow<RoutingStatus> = _status.asStateFlow()

    fun initialize() {
        permissionManager.refreshStatus()
        evaluateRoutingMode(RoutingMode.AUTO)
    }

    fun setMode(mode: RoutingMode) {
        evaluateRoutingMode(mode)
    }

    private fun evaluateRoutingMode(desiredMode: RoutingMode) {
        val perm = permissionManager.status.value
        val effective = when (desiredMode) {
            RoutingMode.AUTO -> {
                when {
                    perm.hasRootPermission -> RoutingMode.ROOT
                    perm.hasShizukuPermission || perm.hasDumpPermission -> RoutingMode.SHIZUKU
                    perm.hasNotificationAccess -> RoutingMode.BROADCAST
                    else -> RoutingMode.STANDALONE
                }
            }
            RoutingMode.ROOT -> if (perm.hasRootPermission) RoutingMode.ROOT else RoutingMode.STANDALONE
            RoutingMode.SHIZUKU -> if (perm.hasShizukuPermission || perm.hasDumpPermission) RoutingMode.SHIZUKU else RoutingMode.STANDALONE
            RoutingMode.BROADCAST -> if (perm.hasNotificationAccess) RoutingMode.BROADCAST else RoutingMode.STANDALONE
            RoutingMode.STANDALONE -> RoutingMode.STANDALONE
        }

        val desc = when (effective) {
            RoutingMode.ROOT -> "Root Access Active: System-Wide DSP & AudioFlinger Hook Enabled"
            RoutingMode.SHIZUKU -> "Shizuku Active: High-Privilege AudioFlinger Session Hook Enabled"
            RoutingMode.BROADCAST -> "Notification / Broadcast Mode: Tracking Open Audio Sessions"
            RoutingMode.STANDALONE -> "Standalone Mode: Ready for permissions or In-App testing"
            RoutingMode.AUTO -> "Auto Selected"
        }

        _status.value = RoutingStatus(
            selectedMode = desiredMode,
            effectiveMode = effective,
            statusDescription = desc,
            isOperational = effective != RoutingMode.STANDALONE
        )

        Log.i(TAG, "Routing mode configured: selected=$desiredMode, effective=$effective")
    }

    companion object {
        private const val TAG = "RoutingManager"
    }
}
