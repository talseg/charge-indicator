package com.talseg.chargeindicator.ui.main

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.tooling.preview.PreviewParameterProvider
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.talseg.chargeindicator.R
import com.talseg.chargeindicator.battery.ChargingSpeed
import com.talseg.chargeindicator.ui.colorArgb
import com.talseg.chargeindicator.ui.labelRes
import com.talseg.chargeindicator.ui.theme.ChargeIndicatorTheme

/** Connects the ViewModel to the UI. The only composable that knows about [MainViewModel]. */
@Composable
fun MainScreen(viewModel: MainViewModel, onClose: () -> Unit) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    MainContent(uiState = uiState, onClose = onClose)
}

/** Pure UI: draws [uiState] and reports clicks. No ViewModel, no Android services. */
@Composable
fun MainContent(uiState: MainUiState, onClose: () -> Unit) {
    Scaffold(
        modifier = Modifier.fillMaxSize(),
        containerColor = Color.Blue
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Greeting(
                appName = stringResource(R.string.app_name),
                chargingSpeed = uiState.chargingSpeed,
                modifier = Modifier.weight(1f)
            )
            Button(
                onClick = onClose,
                modifier = Modifier.padding(24.dp)
            ) {
                Text("Close")
            }
        }
    }
}

@Composable
private fun Greeting(
    appName: String,
    chargingSpeed: ChargingSpeed,
    modifier: Modifier = Modifier
) {
    val color = Color(chargingSpeed.colorArgb())
    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = appName,
            style = MaterialTheme.typography.headlineLarge,
            color = color
        )
        Spacer(Modifier.height(24.dp))
        Icon(
            painter = painterResource(R.drawable.ic_battery_charging_full),
            contentDescription = "Battery",
            tint = color,
            modifier = Modifier.size(96.dp)
        )
        Spacer(Modifier.height(8.dp))
        Text(
            text = stringResource(chargingSpeed.labelRes()),
            style = MaterialTheme.typography.titleLarge,
            color = color
        )
    }
}

/** Feeds every [ChargingSpeed] into the preview below, so each one renders side by side. */
private class ChargingSpeedProvider : PreviewParameterProvider<ChargingSpeed> {
    override val values = ChargingSpeed.entries.asSequence()
}

@Preview(showBackground = true)
@Composable
private fun MainContentPreview(
    @PreviewParameter(ChargingSpeedProvider::class) chargingSpeed: ChargingSpeed
) {
    ChargeIndicatorTheme {
        MainContent(uiState = MainUiState(chargingSpeed = chargingSpeed), onClose = {})
    }
}
