package com.timbre.dsp.audio

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

object SleepTimerManager {

    private val scope = CoroutineScope(Dispatchers.Main)
    private var timerJob: Job? = null

    private val _remainingSeconds = MutableStateFlow(0)
    val remainingSeconds: StateFlow<Int> = _remainingSeconds.asStateFlow()

    private val _isTimerRunning = MutableStateFlow(false)
    val isTimerRunning: StateFlow<Boolean> = _isTimerRunning.asStateFlow()

    fun startTimer(durationMinutes: Int, onTimerExpired: () -> Unit) {
        cancelTimer()
        if (durationMinutes <= 0) return

        val totalSeconds = durationMinutes * 60
        _remainingSeconds.value = totalSeconds
        _isTimerRunning.value = true

        timerJob = scope.launch {
            var currentSec = totalSeconds
            while (isActive && currentSec > 0) {
                delay(1000L)
                currentSec--
                _remainingSeconds.value = currentSec
            }

            if (currentSec <= 0) {
                _isTimerRunning.value = false
                _remainingSeconds.value = 0
                onTimerExpired()
            }
        }
    }

    fun cancelTimer() {
        timerJob?.cancel()
        timerJob = null
        _isTimerRunning.value = false
        _remainingSeconds.value = 0
    }
}
