package com.talseg.chargeindicator

import android.os.Bundle
import android.provider.Settings
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.lifecycle.lifecycleScope
import com.talseg.chargeindicator.overlay.ChargeOverlay
import com.talseg.chargeindicator.ui.colorArgb
import com.talseg.chargeindicator.ui.main.MainScreen
import com.talseg.chargeindicator.ui.main.MainViewModel
import com.talseg.chargeindicator.ui.theme.ChargeIndicatorTheme
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {

    private val viewModel: MainViewModel by viewModels()

    // TEMPORARY: the overlay is hosted here until the foreground service exists.
    private var overlay: ChargeOverlay? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            ChargeIndicatorTheme {
                MainScreen(viewModel = viewModel, onClose = { finish() })
            }
        }
        startOverlay()
    }

    override fun onDestroy() {
        overlay?.hide()
        super.onDestroy()
    }

    private fun startOverlay() {
        if (!Settings.canDrawOverlays(this)) {
            Log.w(TAG, "No 'Display over other apps' permission; overlay not shown")
            return
        }
        val overlay = ChargeOverlay(applicationContext).also { this.overlay = it }
        overlay.show()
        lifecycleScope.launch {
            viewModel.uiState.collect { state ->
                overlay.setColor(state.chargingSpeed.colorArgb())
            }
        }
    }

    private companion object {
        const val TAG = "MainActivity"
    }
}
