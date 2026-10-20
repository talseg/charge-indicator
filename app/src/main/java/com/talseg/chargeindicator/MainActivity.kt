package com.talseg.chargeindicator

import android.os.Bundle
import android.provider.Settings
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import com.talseg.chargeindicator.overlay.ChargeOverlayService
import com.talseg.chargeindicator.ui.main.MainScreen
import com.talseg.chargeindicator.ui.main.MainViewModel
import com.talseg.chargeindicator.ui.theme.ChargeIndicatorTheme

class MainActivity : ComponentActivity() {

    private val viewModel: MainViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            ChargeIndicatorTheme {
                MainScreen(viewModel = viewModel, onClose = { finish() })
            }
        }

        if (Settings.canDrawOverlays(this)) {
            ChargeOverlayService.start(this)
        } else {
            Log.w(TAG, "No 'Display over other apps' permission; overlay service not started")
        }
    }

    private companion object {
        const val TAG = "MainActivity"
    }
}
