package com.talseg.chargeindicator.settings

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
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
    /** Saved window position in pixels; null until the user first drags the icon. */
    val positionX: Int? = null,
    val positionY: Int? = null
) {
    companion object {
        const val DEFAULT_ICON_SIZE_DP = 56
        const val MIN_ICON_SIZE_DP = 32
        const val MAX_ICON_SIZE_DP = 128
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
                iconSizeDp = prefs[ICON_SIZE_DP] ?: OverlaySettings.DEFAULT_ICON_SIZE_DP,
                positionX = prefs[POSITION_X],
                positionY = prefs[POSITION_Y]
            )
        }

    suspend fun setIconSizeDp(sizeDp: Int) {
        dataStore.edit { it[ICON_SIZE_DP] = sizeDp }
    }

    suspend fun setPosition(x: Int, y: Int) {
        dataStore.edit {
            it[POSITION_X] = x
            it[POSITION_Y] = y
        }
    }

    private companion object {
        val ICON_SIZE_DP = intPreferencesKey("icon_size_dp")
        val POSITION_X = intPreferencesKey("position_x")
        val POSITION_Y = intPreferencesKey("position_y")
    }
}

// One DataStore instance per file per process: declared once, at top level.
private val Context.overlaySettingsDataStore: DataStore<Preferences>
    by preferencesDataStore(name = "overlay_settings")
