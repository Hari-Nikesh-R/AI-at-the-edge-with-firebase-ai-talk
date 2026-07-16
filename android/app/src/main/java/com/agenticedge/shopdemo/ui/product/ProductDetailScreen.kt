package com.agenticedge.shopdemo.ui.product

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.Divider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.agenticedge.shopdemo.data.event.EventType
import com.agenticedge.shopdemo.data.model.Product
import com.agenticedge.shopdemo.data.model.ProductCatalog
import com.agenticedge.shopdemo.ui.AppViewModel
import com.agenticedge.shopdemo.ui.components.AgentStatusBar
import com.agenticedge.shopdemo.ui.theme.EdgeGreen

@Composable
fun ProductDetailScreen(
    productId: String,
    appViewModel: AppViewModel,
    onBack: () -> Unit,
    onGoToCart: () -> Unit
) {
    val product = ProductCatalog.byId(productId) ?: return
    val agentState by appViewModel.agentState.collectAsState()
    val preloadedCart by appViewModel.preloadedCart.collectAsState()

    var reviewsExpanded by remember { mutableStateOf(false) }
    var compareTarget by remember { mutableStateOf<Product?>(null) }

    LaunchedEffect(productId) {
        appViewModel.recordProductView(product)
    }

    Scaffold(
        topBar = {
            Column {
                TopAppBar(
                    title = { Text(product.name) },
                    navigationIcon = {
                        IconButton(onClick = onBack) {
                            Icon(Icons.Filled.ArrowBack, contentDescription = "Back")
                        }
                    }
                )
                AgentStatusBar(agentState)
            }
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .padding(padding)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            if (preloadedCart) {
                item {
                    Card {
                        Text(
                            "🔮 Predictive Navigation: the agent expects you're heading to Cart next — its data is already preloaded.",
                            modifier = Modifier.padding(12.dp),
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }
                }
            }

            when {
                agentState.isImpulseBuyer -> item {
                    ImpulseBuyerHero(product, onAddToCart = {
                        appViewModel.addToCart(product)
                        onGoToCart()
                    })
                }
                else -> item {
                    Text(product.emoji, style = MaterialTheme.typography.headlineSmall)
                    val discounted = product.discountedPrice
                    Text("₹$discounted", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                    Text("★ ${product.rating} (${product.reviewCount} reviews)", style = MaterialTheme.typography.labelSmall)
                }
            }

            if (agentState.isResearcher || !agentState.isImpulseBuyer) {
                item { Divider() }
                item { Text("Specifications", style = MaterialTheme.typography.titleMedium) }
                items(product.specs) { spec -> Text("• $spec") }

                item {
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedButton(onClick = {
                            appViewModel.agentRepository.onEvent(EventType.COMPARE, "ProductDetail", product.category)
                            compareTarget = ProductCatalog.byCategory(product.category).firstOrNull { it.id != product.id }
                        }) { Text("Compare") }

                        OutlinedButton(onClick = {
                            reviewsExpanded = !reviewsExpanded
                            if (reviewsExpanded) {
                                appViewModel.agentRepository.onEvent(EventType.REVIEW_READ, "ProductDetail", product.category)
                            }
                        }) { Text(if (reviewsExpanded) "Hide reviews" else "Read reviews") }
                    }
                }

                compareTarget?.let { other ->
                    item {
                        Card {
                            Column(Modifier.padding(12.dp)) {
                                Text("Comparing with ${other.name}", style = MaterialTheme.typography.titleMedium)
                                Text("${other.emoji} ₹${other.price} · ★ ${other.rating}")
                                other.specs.take(2).forEach { Text("• $it") }
                            }
                        }
                    }
                }

                if (reviewsExpanded) {
                    item { Text("Reviews", style = MaterialTheme.typography.titleMedium) }
                    items(product.reviews) { review -> Text("“$review”", style = MaterialTheme.typography.bodyMedium) }
                }
            }

            if (!agentState.isImpulseBuyer) {
                item {
                    Button(onClick = { appViewModel.addToCart(product) }, modifier = Modifier.fillMaxWidth()) {
                        Text("Add to Cart")
                    }
                }
            }
        }
    }
}

@Composable
private fun ImpulseBuyerHero(product: Product, onAddToCart: () -> Unit) {
    Card {
        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text("⚡ ${product.discountPercent}% OFF — today only", style = MaterialTheme.typography.titleMedium, color = EdgeGreen)
            Text(product.emoji, style = MaterialTheme.typography.headlineSmall)
            val discounted = product.discountedPrice
            Text("₹$discounted", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
            Text("was ₹${product.price}", style = MaterialTheme.typography.labelSmall)
            Button(onClick = onAddToCart, modifier = Modifier.fillMaxWidth()) {
                Text("Buy Now")
            }
        }
    }
}
