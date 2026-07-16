package com.agenticedge.shopdemo.ui.home

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Divider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.agenticedge.shopdemo.data.model.ProductCatalog
import com.agenticedge.shopdemo.ui.AppViewModel
import com.agenticedge.shopdemo.ui.components.AgentStatusBar
import com.agenticedge.shopdemo.ui.components.ProductCard

@Composable
fun HomeScreen(
    appViewModel: AppViewModel,
    onProductClick: (String) -> Unit,
    onOpenAccessibilityDemo: () -> Unit,
    onOpenFraudDemo: () -> Unit
) {
    val agentState by appViewModel.agentState.collectAsState()
    val offline by appViewModel.offlineMode.collectAsState()
    val recommendations by appViewModel.offlineRecommendations.collectAsState()

    LaunchedEffect(Unit) { appViewModel.refreshOfflineRecommendations() }

    Scaffold(
        topBar = {
            Column {
                TopAppBar(title = { Text("EdgeShop") })
                AgentStatusBar(agentState)
            }
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .padding(padding)
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text("Airplane Mode", style = MaterialTheme.typography.titleMedium)
                        Text(
                            "Recommendations below still work — they come from an on-device cache, not the network.",
                            style = MaterialTheme.typography.labelSmall
                        )
                    }
                    Switch(checked = offline, onCheckedChange = { appViewModel.setOfflineMode(it) })
                }
            }

            item {
                Text("Recommended for you" + if (offline) " (offline)" else "", style = MaterialTheme.typography.titleLarge)
            }
            items(recommendations.chunked(2), key = { row -> row.joinToString("-") { it.id } }) { row ->
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    row.forEach { product ->
                        ProductCard(
                            product = product,
                            modifier = Modifier.weight(1f),
                            onClick = { onProductClick(product.id) }
                        )
                    }
                    if (row.size == 1) Spacer(modifier = Modifier.weight(1f))
                }
            }

            item { Divider(modifier = Modifier.padding(vertical = 8.dp)) }

            item { Text("All Products", style = MaterialTheme.typography.titleLarge) }
            items(ProductCatalog.all, key = { it.id }) { product ->
                ProductCard(product = product, onClick = { onProductClick(product.id) })
            }

            item { Divider(modifier = Modifier.padding(vertical = 8.dp)) }

            item {
                Text("Demo extras", style = MaterialTheme.typography.titleMedium)
            }
            item {
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedButton(onClick = onOpenAccessibilityDemo) { Text("Accessibility Agent") }
                    OutlinedButton(onClick = onOpenFraudDemo) { Text("Fraud Risk Check") }
                }
            }
        }
    }
}
