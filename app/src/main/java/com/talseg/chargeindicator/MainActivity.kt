package com.talseg.chargeindicator

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
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
    }
}
