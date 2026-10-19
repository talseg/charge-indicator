package com.talseg.chargeindicator

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.talseg.chargeindicator.battery.BatteryMonitor
import com.talseg.chargeindicator.battery.BatteryState
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn

class MainViewModel(application: Application) : AndroidViewModel(application) {

    // null until the first battery broadcast arrives (which happens immediately on subscribe).
    val batteryState: StateFlow<BatteryState?> =
        BatteryMonitor(application).batteryState
            .stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(5_000),
                initialValue = null
            )
}
