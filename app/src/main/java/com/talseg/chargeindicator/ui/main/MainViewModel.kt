package com.talseg.chargeindicator.ui.main

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.talseg.chargeindicator.battery.BatteryMonitor
import com.talseg.chargeindicator.settings.OverlaySettingsRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class MainViewModel(application: Application) : AndroidViewModel(application) {

    private val settingsRepository = OverlaySettingsRepository(application)

    val uiState: StateFlow<MainUiState> =
        combine(
            BatteryMonitor(application).batteryState,
            settingsRepository.settings
        ) { battery, settings ->
            MainUiState(
                chargingSpeed = battery.chargingSpeed,
                iconSizeDp = settings.iconSizeDp
            )
        }.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = MainUiState()
        )

    fun onIconSizeChange(sizeDp: Int) {
        viewModelScope.launch { settingsRepository.setIconSizeDp(sizeDp) }
    }
}
