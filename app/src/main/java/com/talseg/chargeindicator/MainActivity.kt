package com.talseg.chargeindicator

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.Settings
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.lifecycle.lifecycleScope
import com.talseg.chargeindicator.oem.MiuiPermissions
import com.talseg.chargeindicator.overlay.ChargeOverlayService
import com.talseg.chargeindicator.ui.main.MainScreen
import com.talseg.chargeindicator.ui.main.MainViewModel
import com.talseg.chargeindicator.ui.main.SetupStep
import com.talseg.chargeindicator.ui.theme.ChargeIndicatorTheme
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {

    private val viewModel: MainViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            ChargeIndicatorTheme {
                MainScreen(
                    viewModel = viewModel,
                    onOpenSetupSettings = ::openSetupSettings,
                    onClose = { finish() }
                )
            }
        }
    }

    override fun onResume() {
        super.onResume()
        // The user may have just changed a setting in another screen and come back.
        viewModel.refreshSystemSettings()
        // Starting an already-running service is harmless, so this also covers the
        // moment the overlay permission has just been granted.
        lifecycleScope.launch { ChargeOverlayService.startIfEnabled(this@MainActivity) }
    }

    private fun openSetupSettings(step: SetupStep) {
        when (step) {
            SetupStep.XIAOMI_WINDOWS -> MiuiPermissions.openOtherPermissions(this)
            SetupStep.XIAOMI_AUTOSTART -> MiuiPermissions.openAutostartSettings(this)
            SetupStep.OVERLAY -> startActivity(
                Intent(
                    Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                    Uri.fromParts("package", packageName, null)
                )
            )
        }
    }
}
