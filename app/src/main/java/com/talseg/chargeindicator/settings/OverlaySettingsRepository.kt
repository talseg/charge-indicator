package com.talseg.chargeindicator.settings

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.emptyPreferences
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import java.io.IOException

data class OverlaySettings(
    val iconSizeDp: Int = DEFAULT_ICON_SIZE_DP,
    val showCircleBackground: Boolean = true,
    /** Saved center of the battery, in screen pixels; null until the user first drags the icon. */
    val centerX: Int? = null,
    val centerY: Int? = null
) {
    companion object {
        const val DEFAULT_ICON_SIZE_DP = 56
        const val MIN_ICON_SIZE_DP = 32
        const val MAX_ICON_SIZE_DP = 152
        const val ICON_SIZE_STEP_DP = 8
    }
}

/**
 * Persists overlay settings on disk, so they survive app and device restarts.
 * Both the settings screen and the overlay service use it; changes made by one
 * are observed by the other through [settings].
 */
class OverlaySettingsRepository(context: Context) {

    private val dataStore = context.applicationContext.overlaySettingsDataStore

    val settings: Flow<OverlaySettings> = dataStore.data
        .catch { error ->
            // A corrupt or unreadable file falls back to defaults instead of crashing.
            if (error is IOException) emit(emptyPreferences()) else throw error
        }
        .map { prefs ->
            OverlaySettings(
                // Clamped, so a value saved under an older, wider range stays valid.
                iconSizeDp = (prefs[ICON_SIZE_DP] ?: OverlaySettings.DEFAULT_ICON_SIZE_DP)
                    .coerceIn(OverlaySettings.MIN_ICON_SIZE_DP, OverlaySettings.MAX_ICON_SIZE_DP),
                showCircleBackground = prefs[SHOW_CIRCLE_BACKGROUND] ?: true,
                centerX = prefs[CENTER_X],
                centerY = prefs[CENTER_Y]
            )
        }

    suspend fun setIconSizeDp(sizeDp: Int) {
        dataStore.edit { it[ICON_SIZE_DP] = sizeDp }
    }

    suspend fun setShowCircleBackground(show: Boolean) {
        dataStore.edit { it[SHOW_CIRCLE_BACKGROUND] = show }
    }

    suspend fun setCenter(x: Int, y: Int) {
        dataStore.edit {
            it[CENTER_X] = x
            it[CENTER_Y] = y
        }
    }

    private companion object {
        val ICON_SIZE_DP = intPreferencesKey("icon_size_dp")
        val SHOW_CIRCLE_BACKGROUND = booleanPreferencesKey("show_circle_background")
        // New key names: the old "position_*" values were top-left corners, not centers.
        val CENTER_X = intPreferencesKey("center_x")
        val CENTER_Y = intPreferencesKey("center_y")
    }
}

// One DataStore instance per file per process: declared once, at top level.
private val Context.overlaySettingsDataStore: DataStore<Preferences>
    by preferencesDataStore(name = "overlay_settings")
