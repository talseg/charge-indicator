package com.talseg.chargeindicator.ui.main

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
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

    // Screen-only state, not persisted: which charging speed the preview shows.
    private val previewSpeed = MutableStateFlow(DEFAULT_PREVIEW_SPEED)

    val uiState: StateFlow<MainUiState> =
        combine(settingsRepository.settings, previewSpeed) { settings, speed ->
            MainUiState(
                previewSpeed = speed,
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

    fun onPreviewSpeedChange(speed: ChargingSpeed) {
        previewSpeed.value = speed
    }

    companion object {
        val DEFAULT_PREVIEW_SPEED = ChargingSpeed.FAST
    }
}
