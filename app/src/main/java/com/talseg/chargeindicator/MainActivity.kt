package com.talseg.chargeindicator

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
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
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.talseg.chargeindicator.ui.theme.ChargeIndicatorTheme

class MainActivity : ComponentActivity() {

    private val viewModel: MainViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            ChargeIndicatorTheme {
                val batteryState by viewModel.batteryState.collectAsStateWithLifecycle()
                MainScreen(
                    isPluggedIn = batteryState?.isPluggedIn == true,
                    onClose = { finish() }
                )
            }
        }
    }
}

@Composable
fun MainScreen(isPluggedIn: Boolean, onClose: () -> Unit) {
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
                appName = "Charge Indicator",
                isPluggedIn = isPluggedIn,
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
fun Greeting(appName: String, isPluggedIn: Boolean, modifier: Modifier = Modifier) {
    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = appName,
            style = MaterialTheme.typography.headlineLarge,
            color = Color.Yellow
        )
        Spacer(Modifier.height(24.dp))
        Icon(
            painter = painterResource(R.drawable.ic_battery_charging_full),
            contentDescription = "Battery",
            tint = if (isPluggedIn) Color.Yellow else Color.White,
            modifier = Modifier.size(96.dp)
        )
    }
}

@Preview(showBackground = true)
@Composable
fun MainScreenPreview() {
    ChargeIndicatorTheme {
        MainScreen(isPluggedIn = true, onClose = {})
    }
}
