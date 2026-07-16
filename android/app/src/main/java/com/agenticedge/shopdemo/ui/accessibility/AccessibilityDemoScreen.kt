package com.agenticedge.shopdemo.ui.accessibility

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.agenticedge.shopdemo.ui.AppViewModel

/** README capability #9, Accessibility Agent. */
@Composable
fun AccessibilityDemoScreen(appViewModel: AppViewModel, onBack: () -> Unit) {
    val adjustments by appViewModel.accessibilityAdjustments.collectAsState()
    val largeTextMode by appViewModel.largeTextMode.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Accessibility Agent") },
                navigationIcon = { IconButton(onClick = onBack) { Icon(Icons.Filled.ArrowBack, contentDescription = "Back") } }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier.padding(padding).padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Card {
                Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text(
                        "The agent watches for repeated zoom / font-increase actions. After 3, it auto-switches the whole app to a larger-text, higher-contrast layout.",
                        style = MaterialTheme.typography.bodyMedium
                    )
                    Text("Adjustments observed: $adjustments", fontWeight = FontWeight.Bold)
                    Text(
                        if (largeTextMode) "Large text mode: ON (agent-activated)" else "Large text mode: off",
                        style = MaterialTheme.typography.titleMedium
                    )
                }
            }

            OutlinedButton(onClick = { appViewModel.recordAccessibilityAdjustment() }, modifier = Modifier.fillMaxWidth()) {
                Text("Zoom In / Increase Font")
            }

            Button(onClick = { appViewModel.resetAccessibility() }, modifier = Modifier.fillMaxWidth()) {
                Text("Reset")
            }
        }
    }
}
