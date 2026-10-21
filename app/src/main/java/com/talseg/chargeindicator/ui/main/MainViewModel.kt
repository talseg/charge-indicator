package com.talseg.chargeindicator.ui.main

import android.app.Application
import android.provider.Settings
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.talseg.chargeindicator.battery.ChargingSpeed
import com.talseg.chargeindicator.oem.MiuiPermissions
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

    // System settings that still need the user, most important first. Refreshed on resume.
    private val missingSetupSteps = MutableStateFlow(emptyList<SetupStep>())

    // Steps the user postponed with "Later"; asked again the next time the app opens.
    private val postponedSetupSteps = MutableStateFlow(emptySet<SetupStep>())

    val uiState: StateFlow<MainUiState> =
        combine(
            settingsRepository.settings,
            previewSpeed,
            missingSetupSteps,
            postponedSetupSteps
        ) { settings, speed, missing, postponed ->
            MainUiState(
                previewSpeed = speed,
                iconSizeDp = settings.iconSizeDp,
                showCircleBackground = settings.showCircleBackground,
                setupPrompt = missing.firstOrNull { it !in postponed }
            )
        }.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = MainUiState()
        )

    /** Re-reads system settings the user may have changed elsewhere (called on resume). */
    fun refreshSystemSettings() {
        val context = getApplication<Application>()
        val canDrawOverlays = Settings.canDrawOverlays(context)

        missingSetupSteps.value = buildList {
            if (MiuiPermissions.isXiaomiDevice()) {
                // Only a definite DISABLED counts; an unreadable MIUI state never nags.
                if (!canDrawOverlays ||
                    MiuiPermissions.backgroundWindowsState(context) == MiuiPermissions.State.DISABLED
                ) {
                    add(SetupStep.XIAOMI_WINDOWS)
                }
                if (MiuiPermissions.autostartState(context) == MiuiPermissions.State.DISABLED) {
                    add(SetupStep.XIAOMI_AUTOSTART)
                }
            } else if (!canDrawOverlays) {
                add(SetupStep.OVERLAY)
            }
        }
    }

    fun onSetupPromptLater(step: SetupStep) {
        postponedSetupSteps.value += step
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
