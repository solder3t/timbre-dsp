package com.timbre.dsp.service

import android.content.Intent
import android.service.quicksettings.Tile
import android.service.quicksettings.TileService
import com.timbre.dsp.DSPForegroundService
import com.timbre.dsp.audio.AudioEffectManager
import com.timbre.dsp.model.DSPSettings

class DSPTileService : TileService() {

    override fun onStartListening() {
        super.onStartListening()
        updateTileState(isDspEnabled)
    }

    override fun onClick() {
        super.onClick()
        val newState = !isDspEnabled
        isDspEnabled = newState
        
        val settings = currentSettings.copy(isEnabled = newState)
        currentSettings = settings
        AudioEffectManager.getInstance(this).updateSettings(settings)
        
        if (newState) {
            val intent = Intent(this, DSPForegroundService::class.java)
            startForegroundService(intent)
        }
        
        updateTileState(newState)
    }

    private fun updateTileState(enabled: Boolean) {
        val tile = qsTile ?: return
        tile.state = if (enabled) Tile.STATE_ACTIVE else Tile.STATE_INACTIVE
        tile.label = "Timbre DSP"
        tile.subtitle = if (enabled) "Active" else "Bypassed"
        tile.updateTile()
    }

    companion object {
        var isDspEnabled: Boolean = true
        var currentSettings: DSPSettings = DSPSettings()
    }
}
