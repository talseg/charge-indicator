package com.talseg.chargeindicator.ui.main

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Button
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Slider
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
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
import com.talseg.chargeindicator.overlay.OverlayStyle
import com.talseg.chargeindicator.settings.OverlaySettings
import com.talseg.chargeindicator.ui.colorArgb
import com.talseg.chargeindicator.ui.labelRes
import com.talseg.chargeindicator.ui.theme.ChargeIndicatorTheme
import kotlin.math.roundToInt

/** Connects the ViewModel to the UI. The only composable that knows about [MainViewModel]. */
@Composable
fun MainScreen(viewModel: MainViewModel, onClose: () -> Unit) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    MainContent(
        uiState = uiState,
        onIconSizeChange = viewModel::onIconSizeChange,
        onShowCircleBackgroundChange = viewModel::onShowCircleBackgroundChange,
        onPreviewSpeedChange = viewModel::onPreviewSpeedChange,
        onClose = onClose
    )
}

/** Pure UI: draws [uiState] and reports user actions. No ViewModel, no Android services. */
@Composable
fun MainContent(
    uiState: MainUiState,
    onIconSizeChange: (Int) -> Unit,
    onShowCircleBackgroundChange: (Boolean) -> Unit,
    onPreviewSpeedChange: (ChargingSpeed) -> Unit,
    onClose: () -> Unit
) {
    Scaffold(
        modifier = Modifier.fillMaxSize(),
        containerColor = Color.Blue
    ) { innerPadding ->
        // BoxWithConstraints reveals the available size, so the layout can adapt to it.
        BoxWithConstraints(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 24.dp)
        ) {
            val settings: @Composable () -> Unit = {
                SettingsPanel(
                    uiState = uiState,
                    onIconSizeChange = onIconSizeChange,
                    onShowCircleBackgroundChange = onShowCircleBackgroundChange,
                    onPreviewSpeedChange = onPreviewSpeedChange,
                    onClose = onClose
                )
            }

            if (maxWidth > maxHeight) {
                // Landscape: settings on the left (scrollable), preview on the right.
                Row(modifier = Modifier.fillMaxSize()) {
                    Column(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxHeight()
                            .verticalScroll(rememberScrollState()),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        AppTitle(uiState.previewSpeed)
                        settings()
                    }
                    PreviewPane(
                        uiState = uiState,
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxHeight()
                    )
                }
            } else {
                // Portrait: title on top, preview takes the free space, settings below.
                Column(
                    modifier = Modifier.fillMaxSize(),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    AppTitle(uiState.previewSpeed)
                    PreviewPane(
                        uiState = uiState,
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxWidth()
                    )
                    settings()
                }
            }
        }
    }
}

@Composable
private fun AppTitle(chargingSpeed: ChargingSpeed) {
    Text(
        text = stringResource(R.string.app_name),
        style = MaterialTheme.typography.headlineLarge,
        color = Color(chargingSpeed.colorArgb()),
        modifier = Modifier.padding(top = 24.dp)
    )
}

/** The overlay icon preview with the speed label under it, centered in the given space. */
@Composable
private fun PreviewPane(uiState: MainUiState, modifier: Modifier = Modifier) {
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        OverlayIconPreview(
            chargingSpeed = uiState.previewSpeed,
            sizeDp = uiState.iconSizeDp,
            showCircleBackground = uiState.showCircleBackground
        )
        Spacer(Modifier.height(12.dp))
        Text(
            text = stringResource(uiState.previewSpeed.labelRes()),
            style = MaterialTheme.typography.titleLarge,
            color = Color(uiState.previewSpeed.colorArgb())
        )
    }
}

@Composable
private fun SettingsPanel(
    uiState: MainUiState,
    onIconSizeChange: (Int) -> Unit,
    onShowCircleBackgroundChange: (Boolean) -> Unit,
    onPreviewSpeedChange: (ChargingSpeed) -> Unit,
    onClose: () -> Unit
) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Spacer(Modifier.height(16.dp))
        PreviewStateSelector(
            previewSpeed = uiState.previewSpeed,
            onPreviewSpeedChange = onPreviewSpeedChange
        )
        Spacer(Modifier.height(16.dp))
        IconSizeSetting(
            sizeDp = uiState.iconSizeDp,
            onSizeChange = onIconSizeChange
        )
        ShowCircleBackgroundSetting(
            show = uiState.showCircleBackground,
            onShowChange = onShowCircleBackgroundChange
        )
        Button(
            onClick = onClose,
            modifier = Modifier.padding(vertical = 16.dp)
        ) {
            Text("Close")
        }
    }
}

/**
 * Draws the floating icon the way the overlay will look: same size, colors and circle.
 * Mirrors ChargeOverlay (a View) using the shared [OverlayStyle] constants.
 */
@Composable
private fun OverlayIconPreview(
    chargingSpeed: ChargingSpeed,
    sizeDp: Int,
    showCircleBackground: Boolean,
    modifier: Modifier = Modifier
) {
    val frame = if (showCircleBackground) {
        Modifier
            .size(sizeDp.dp)
            .background(Color(OverlayStyle.CIRCLE_BACKGROUND_COLOR), CircleShape)
            .padding((sizeDp * OverlayStyle.CIRCLE_PADDING_PERCENT / 100).dp)
    } else {
        Modifier.size(width = (sizeDp * OverlayStyle.BATTERY_ASPECT_RATIO).dp, height = sizeDp.dp)
    }
    Box(modifier = modifier.then(frame)) {
        Icon(
            painter = painterResource(R.drawable.ic_overlay_battery),
            contentDescription = null,
            tint = Color(chargingSpeed.colorArgb()),
            modifier = Modifier.fillMaxSize()
        )
    }
}

/** Chips to preview each charging speed. Exactly one is always selected. */
@Composable
private fun PreviewStateSelector(
    previewSpeed: ChargingSpeed,
    onPreviewSpeedChange: (ChargingSpeed) -> Unit
) {
    val chipColors = FilterChipDefaults.filterChipColors(
        labelColor = Color.White,
        selectedLabelColor = Color.Blue,
        selectedContainerColor = Color.White
    )
    Column(modifier = Modifier.fillMaxWidth()) {
        Text(
            text = stringResource(R.string.preview_state_label),
            style = MaterialTheme.typography.titleMedium,
            color = Color.White
        )
        FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            ChargingSpeed.entries.filter { it != ChargingSpeed.NONE }.forEach { speed ->
                FilterChip(
                    selected = previewSpeed == speed,
                    onClick = { onPreviewSpeedChange(speed) },
                    label = { Text(stringResource(speed.labelRes())) },
                    colors = chipColors
                )
            }
        }
    }
}

/**
 * Slider for the overlay icon size. Saves on every snapped step while dragging,
 * so the overlay resizes live; the slider snaps every 8 dp, so that's only a few
 * dozen small writes per drag.
 */
@Composable
private fun IconSizeSetting(
    sizeDp: Int,
    onSizeChange: (Int) -> Unit
) {
    // Local slider state keeps the thumb smooth; reset whenever the saved size changes.
    var sliderValue by remember(sizeDp) { mutableFloatStateOf(sizeDp.toFloat()) }
    val min = OverlaySettings.MIN_ICON_SIZE_DP
    val max = OverlaySettings.MAX_ICON_SIZE_DP
    val step = OverlaySettings.ICON_SIZE_STEP_DP

    Column(modifier = Modifier.fillMaxWidth()) {
        Text(
            text = stringResource(R.string.icon_size_label, sliderValue.roundToInt()),
            style = MaterialTheme.typography.titleMedium,
            color = Color.White
        )
        Slider(
            value = sliderValue,
            onValueChange = { value ->
                sliderValue = value
                val snapped = value.roundToInt()
                if (snapped != sizeDp) onSizeChange(snapped)
            },
            valueRange = min.toFloat()..max.toFloat(),
            steps = (max - min) / step - 1
        )
    }
}

@Composable
private fun ShowCircleBackgroundSetting(
    show: Boolean,
    onShowChange: (Boolean) -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = stringResource(R.string.show_circle_background),
            style = MaterialTheme.typography.titleMedium,
            color = Color.White,
            modifier = Modifier.weight(1f)
        )
        Switch(checked = show, onCheckedChange = onShowChange)
    }
}

/** Feeds every [ChargingSpeed] into the preview below, so each one renders side by side. */
private class ChargingSpeedProvider : PreviewParameterProvider<ChargingSpeed> {
    override val values = ChargingSpeed.entries.asSequence()
}

@Preview(showBackground = true, heightDp = 800)
@Composable
private fun MainContentPreview(
    @PreviewParameter(ChargingSpeedProvider::class) chargingSpeed: ChargingSpeed
) {
    ChargeIndicatorTheme {
        MainContent(
            uiState = MainUiState(previewSpeed =chargingSpeed),
            onIconSizeChange = {},
            onShowCircleBackgroundChange = {},
            onPreviewSpeedChange = {},
            onClose = {}
        )
    }
}

@Preview(name = "Landscape", showBackground = true, widthDp = 800, heightDp = 360)
@Composable
private fun MainContentLandscapePreview() {
    ChargeIndicatorTheme {
        MainContent(
            uiState = MainUiState(previewSpeed =ChargingSpeed.FAST, iconSizeDp = 120),
            onIconSizeChange = {},
            onShowCircleBackgroundChange = {},
            onPreviewSpeedChange = {},
            onClose = {}
        )
    }
}
