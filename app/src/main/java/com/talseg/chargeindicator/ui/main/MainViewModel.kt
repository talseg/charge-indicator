package com.talseg.chargeindicator.ui.main

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.talseg.chargeindicator.battery.BatteryMonitor
import com.talseg.chargeindicator.battery.ChargingSpeed
import com.talseg.chargeindicator.settings.OverlaySettingsRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class MainViewModel(application: Application) : AndroidViewModel(application) {

    private val settingsRepository = OverlaySettingsRepository(application)

    // Screen-only state, not persisted: which charging state the preview is simulating.
    private val simulatedSpeed = MutableStateFlow<ChargingSpeed?>(null)

    val uiState: StateFlow<MainUiState> =
        combine(
            BatteryMonitor(application).batteryState,
            settingsRepository.settings,
            simulatedSpeed
        ) { battery, settings, simulated ->
            MainUiState(
                chargingSpeed = simulated ?: battery.chargingSpeed,
                simulatedSpeed = simulated,
                iconSizeDp = settings.iconSizeDp,
                showCircleBackground = settings.showCircleBackground
            )
        }.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = MainUiState()
        )

    fun onIconSizeChange(sizeDp: Int) {
        viewModelScope.launch { settingsRepository.setIconSizeDp(sizeDp) }
    }

    fun onShowCircleBackgroundChange(show: Boolean) {
        viewModelScope.launch { settingsRepository.setShowCircleBackground(show) }
    }

    /** Null returns the preview to the live charging state. */
    fun onSimulatedSpeedChange(speed: ChargingSpeed?) {
        simulatedSpeed.value = speed
    }
}
