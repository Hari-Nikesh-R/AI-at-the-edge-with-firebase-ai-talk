package com.agenticedge.shopdemo.ui.fraud

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.agenticedge.shopdemo.agent.FraudRiskEngine
import com.agenticedge.shopdemo.ui.AppViewModel
import com.agenticedge.shopdemo.ui.theme.riskColor

/** README capability #10, Edge Fraud Detection. */
@Composable
fun FraudDemoScreen(appViewModel: AppViewModel, onBack: () -> Unit) {
    var unknownDevice by remember { mutableStateOf(false) }
    var vpnOrUnknownLocation by remember { mutableStateOf(false) }
    var rapidTransactions by remember { mutableStateOf(false) }
    var result by remember { mutableStateOf<FraudRiskEngine.RiskResult?>(null) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Fraud Risk Check") },
                navigationIcon = { IconButton(onClick = onBack) { Icon(Icons.Filled.ArrowBack, contentDescription = "Back") } }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier.padding(padding).padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                "Simulate a money-transfer request. The risk score below is computed entirely on-device, before any request would reach a backend.",
                style = MaterialTheme.typography.bodyMedium
            )

            ToggleRow("Unknown device", unknownDevice) { unknownDevice = it }
            ToggleRow("VPN / unrecognized location", vpnOrUnknownLocation) { vpnOrUnknownLocation = it }
            ToggleRow("Rapid transaction volume", rapidTransactions) { rapidTransactions = it }

            Button(
                onClick = {
                    result = FraudRiskEngine.computeRiskScore(unknownDevice, vpnOrUnknownLocation, rapidTransactions)
                },
                modifier = Modifier.fillMaxWidth()
            ) { Text("Attempt Transfer") }

            result?.let { r ->
                Card {
                    Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        Text("Risk score: ${r.score}", style = MaterialTheme.typography.titleLarge, color = riskColor(r.score), fontWeight = FontWeight.Bold)
                        r.reasons.forEach { Text("• $it") }
                        if (r.score >= 70) {
                            Text("Transfer blocked pending verification.", fontWeight = FontWeight.Bold)
                        } else {
                            Text("Transfer allowed.")
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun ToggleRow(label: String, checked: Boolean, onCheckedChange: (Boolean) -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(label)
        Switch(checked = checked, onCheckedChange = onCheckedChange)
    }
}
