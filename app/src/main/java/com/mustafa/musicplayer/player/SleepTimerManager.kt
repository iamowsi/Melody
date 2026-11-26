package com.mustafa.musicplayer.player

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

object SleepTimerManager {
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Main)
    private var timerJob: Job? = null
    private var onExpireAction: (() -> Unit)? = null

    private val _remainingMs = MutableStateFlow(0L)
    val remainingMs: StateFlow<Long> = _remainingMs.asStateFlow()

    fun bindExpireAction(action: (() -> Unit)?) {
        onExpireAction = action
    }

    fun start(minutes: Int) {
        cancel()
        if (minutes <= 0) return
        val endTime = System.currentTimeMillis() + minutes * 60_000L
        _remainingMs.value = minutes * 60_000L

        timerJob = scope.launch {
            while (true) {
                val left = (endTime - System.currentTimeMillis()).coerceAtLeast(0L)
                _remainingMs.value = left
                if (left <= 0L) {
                    onExpireAction?.invoke()
                    break
                }
                delay(1000L)
            }
        }
    }

    fun cancel() {
        timerJob?.cancel()
        timerJob = null
        _remainingMs.value = 0L
    }
}
