package com.talseg.chargeindicator.ui.main

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.talseg.chargeindicator.battery.BatteryMonitor
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn

class MainViewModel(application: Application) : AndroidViewModel(application) {

    val uiState: StateFlow<MainUiState> =
        BatteryMonitor(application).batteryState
            .map { battery -> MainUiState(chargingSpeed = battery.chargingSpeed) }
            .stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(5_000),
                initialValue = MainUiState()
            )
}
