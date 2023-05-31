package com.plcoding.wearosstopwatch.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch

enum class OverdoseState {
    NOTDETECTED,
    VITALSDROP,
    DETECTED
}

class OODViewModel : ViewModel() {

    private val _bloodOxygenLevel = MutableStateFlow(0L)
    val bloodOxygenLevel: StateFlow<Long> = _bloodOxygenLevel

    private val _respiratoryRate = MutableStateFlow(0L)
    val respiratoryRate: StateFlow<Long> = _respiratoryRate

    private val _overdoseState = MutableStateFlow(OverdoseState.NOTDETECTED)
    val overdoseState: StateFlow<OverdoseState> = _overdoseState

    private val bloodOxygenLevelValues = arrayOf(95, 90, 81, 74, 69, 67, 61, 58)
    private val respiratoryRateValues = arrayOf(12, 11, 9, 8, 7, 7, 5, 4)

    init {
        testDetection()
    }

    private fun testDetection() {
        viewModelScope.launch {
            for (i in bloodOxygenLevelValues.indices) {
                val bloodOxygenLevel = bloodOxygenLevelValues[i]
                val respiratoryRate = respiratoryRateValues[i]

                updateBloodOxygenLevel(bloodOxygenLevel.toLong())
                updateRespiratoryRate(respiratoryRate.toLong())
                checkForOverdose()

                delay(4000) // 2-second delay
            }
        }
    }

    private fun updateBloodOxygenLevel(level: Long) {
        _bloodOxygenLevel.value = level
    }

    private fun updateRespiratoryRate(rate: Long) {
        _respiratoryRate.value = rate
    }

    private fun checkForOverdose() {
        val bloodOxygenLevel = _bloodOxygenLevel.value
        val respiratoryRate = _respiratoryRate.value

        if (bloodOxygenLevel < 70 && respiratoryRate < 8) {
            _overdoseState.value = OverdoseState.DETECTED
        } else if (bloodOxygenLevel < 90 && respiratoryRate < 11){
            _overdoseState.value = OverdoseState.VITALSDROP
        } else {
            _overdoseState.value = OverdoseState.NOTDETECTED
        }
    }
}
