package com.mustafa.musicplayer.player

import android.media.audiofx.Equalizer
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

data class EqualizerBand(
    val index: Short,
    val centerFreqHz: Int,
    val levelMb: Short
)

data class EqualizerState(
    val isAvailable: Boolean = false,
    val isEnabled: Boolean = false,
    val minBandLevel: Short = -1500,
    val maxBandLevel: Short = 1500,
    val bands: List<EqualizerBand> = emptyList(),
    val presets: List<String> = emptyList(),
    val selectedPreset: Int = -1
)

object EqualizerManager {
    private var equalizer: Equalizer? = null
    private var attachedSessionId: Int = 0

    private val _state = MutableStateFlow(EqualizerState())
    val state: StateFlow<EqualizerState> = _state.asStateFlow()

    fun attachToSession(audioSessionId: Int) {
        if (audioSessionId <= 0 || audioSessionId == attachedSessionId) return

        val previousEnabled = _state.value.isEnabled
        releaseInternal()

        runCatching {
            val eq = Equalizer(0, audioSessionId)
            eq.enabled = previousEnabled
            equalizer = eq
            attachedSessionId = audioSessionId
            publishState(eq, selectedPreset = -1)
        }.onFailure {
            attachedSessionId = 0
            _state.value = EqualizerState(isAvailable = false)
        }
    }

    fun setEnabled(enabled: Boolean) {
        val eq = equalizer ?: return
        runCatching {
            eq.enabled = enabled
            publishState(eq, selectedPreset = _state.value.selectedPreset)
        }
    }

    fun setBandLevel(bandIndex: Short, levelMb: Short) {
        val eq = equalizer ?: return
        runCatching {
            val range = eq.bandLevelRange
            val clamped = levelMb.coerceIn(range[0], range[1])
            eq.setBandLevel(bandIndex, clamped)
            publishState(eq, selectedPreset = -1)
        }
    }

    fun usePreset(presetIndex: Int) {
        val eq = equalizer ?: return
        runCatching {
            if (presetIndex in 0 until eq.numberOfPresets.toInt()) {
                eq.usePreset(presetIndex.toShort())
                if (!eq.enabled) {
                    eq.enabled = true
                }
                publishState(eq, selectedPreset = presetIndex)
            }
        }
    }

    fun release() {
        releaseInternal()
        _state.value = EqualizerState(isAvailable = false)
    }

    private fun releaseInternal() {
        runCatching { equalizer?.release() }
        equalizer = null
        attachedSessionId = 0
    }

    private fun publishState(eq: Equalizer, selectedPreset: Int) {
        val range = eq.bandLevelRange
        val bandCount = eq.numberOfBands.toInt()
        val bands = (0 until bandCount).map { i ->
            val idx = i.toShort()
            EqualizerBand(
                index = idx,
                centerFreqHz = eq.getCenterFreq(idx) / 1000,
                levelMb = eq.getBandLevel(idx)
            )
        }
        val presetCount = eq.numberOfPresets.toInt()
        val presets = (0 until presetCount).map { i ->
            eq.getPresetName(i.toShort())
        }

        _state.value = EqualizerState(
            isAvailable = true,
            isEnabled = eq.enabled,
            minBandLevel = range[0],
            maxBandLevel = range[1],
            bands = bands,
            presets = presets,
            selectedPreset = selectedPreset
        )
    }
}
