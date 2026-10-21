package com.talseg.chargeindicator.ui.main

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.talseg.chargeindicator.battery.ChargingSpeed
import com.talseg.chargeindicator.oem.XiaomiAutostart
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

    private val autostartState = MutableStateFlow(XiaomiAutostart.State.UNKNOWN)

    // "Later" hides the dialog until the app is opened again.
    private val autostartDialogDismissed = MutableStateFlow(false)

    val uiState: StateFlow<MainUiState> =
        combine(
            settingsRepository.settings,
            previewSpeed,
            autostartState,
            autostartDialogDismissed
        ) { settings, speed, autostart, dismissed ->
            MainUiState(
                previewSpeed = speed,
                iconSizeDp = settings.iconSizeDp,
                showCircleBackground = settings.showCircleBackground,
                // Only when we know it's off; an unreadable state never nags.
                showAutostartDialog = autostart == XiaomiAutostart.State.DISABLED && !dismissed
            )
        }.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = MainUiState()
        )

    /** Re-reads system settings the user may have changed elsewhere (called on resume). */
    fun refreshSystemSettings() {
        autostartState.value = XiaomiAutostart.getState(getApplication())
    }

    fun onAutostartDialogDismiss() {
        autostartDialogDismissed.value = true
    }

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
