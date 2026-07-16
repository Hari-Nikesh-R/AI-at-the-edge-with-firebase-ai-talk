package com.agenticedge.shopdemo.ui.dashboard

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.Divider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.agenticedge.shopdemo.agent.BackendLogEntry
import com.agenticedge.shopdemo.data.event.EventType
import com.agenticedge.shopdemo.ui.AppViewModel
import com.agenticedge.shopdemo.ui.theme.personaColor

@Composable
fun AgentDashboardScreen(appViewModel: AppViewModel) {
    val agentState by appViewModel.agentState.collectAsState()
    val backendLog by appViewModel.backendLog.collectAsState()
    val notification by appViewModel.notificationSuggestion.collectAsState()

    LaunchedEffect(Unit) { appViewModel.refreshNotificationSuggestion() }

    Scaffold(topBar = { TopAppBar(title = { Text("Frontend Agent") }) }) { padding ->
        LazyColumn(
            modifier = Modifier.padding(padding).padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            item {
                Card {
                    Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        Text("Live Agent State", style = MaterialTheme.typography.titleLarge)
                        Text(
                            "Persona: ${agentState.persona}",
                            style = MaterialTheme.typography.titleMedium,
                            color = personaColor(agentState.persona),
                            fontWeight = FontWeight.Bold
                        )
                        Text("Confidence: ${agentState.personaConfidence}%")
                        Text("Engagement: ${agentState.engagement}")
                        Text("Purchase intent: ${agentState.purchaseIntent}")
                        Text("Predicted next screen: ${agentState.predictedNextScreen} (${agentState.nextScreenConfidence}%)")
                        Text("Events this session: ${agentState.eventCount}", style = MaterialTheme.typography.labelSmall)
                        Text(
                            "All inference above runs on-device via TensorFlow Lite — no network call.",
                            style = MaterialTheme.typography.labelSmall
                        )
                    }
                }
            }

            item {
                Text("Simulate activity (for the live demo)", style = MaterialTheme.typography.titleMedium)
            }
            item {
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedButton(onClick = { appViewModel.agentRepository.onEvent(EventType.SEARCH, "Dashboard") }) { Text("+Search") }
                    OutlinedButton(onClick = { appViewModel.agentRepository.onEvent(EventType.COMPARE, "Dashboard") }) { Text("+Compare") }
                    OutlinedButton(onClick = { appViewModel.agentRepository.onEvent(EventType.REVIEW_READ, "Dashboard") }) { Text("+Review") }
                }
            }

            item { Divider() }

            item {
                Card {
                    Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        Text("Smart Notification Agent", style = MaterialTheme.typography.titleMedium)
                        Text(notification.title, fontWeight = FontWeight.Bold)
                        Text(notification.body, style = MaterialTheme.typography.bodyMedium)
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            OutlinedButton(onClick = { appViewModel.recordPromoIgnored() }) { Text("Ignore promo") }
                            OutlinedButton(onClick = { appViewModel.recordEducationalRead() }) { Text("Read guide") }
                        }
                    }
                }
            }

            item {
                Button(
                    onClick = { appViewModel.agentRepository.endSession(topCategory = "Electronics") },
                    modifier = Modifier.fillMaxWidth()
                ) { Text("End Session → Send Summary to \"Firebase\"") }
            }

            item {
                Text("Sent to backend (mock Firebase)", style = MaterialTheme.typography.titleMedium)
            }
            items(backendLog) { entry -> BackendLogRow(entry) }
        }
    }
}

@Composable
private fun BackendLogRow(entry: BackendLogEntry) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(Modifier.padding(10.dp)) {
            Text(entry.kind, style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold)
            Text(entry.summary, style = MaterialTheme.typography.bodyMedium)
        }
    }
}
