package com.agenticedge.shopdemo.ui.cart

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.Divider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.agenticedge.shopdemo.ui.AppViewModel

@Composable
fun CartScreen(appViewModel: AppViewModel, onCheckout: () -> Unit) {
    val items by appViewModel.cartItems.collectAsState()

    Scaffold(topBar = { TopAppBar(title = { Text("Cart") }) }) { padding ->
        Column(modifier = Modifier.padding(padding).fillMaxSize().padding(16.dp)) {
            if (items.isEmpty()) {
                Text("Your cart is empty.", style = MaterialTheme.typography.bodyLarge)
            } else {
                LazyColumn(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    items(items, key = { it.product.id }) { item ->
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text("${item.product.emoji} ${item.product.name}", style = MaterialTheme.typography.titleMedium)
                                Text("Qty ${item.quantity}", style = MaterialTheme.typography.labelSmall)
                            }
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text("₹${item.product.discountedPrice * item.quantity}")
                                TextButton(onClick = { appViewModel.removeFromCart(item.product.id) }) { Text("Remove") }
                            }
                        }
                    }
                }
                Divider(modifier = Modifier.padding(vertical = 12.dp))
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text("Total", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                    Text("₹${appViewModel.cartTotal()}", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                }
                Button(onClick = onCheckout, modifier = Modifier.fillMaxWidth().padding(top = 12.dp)) {
                    Text("Proceed to Checkout")
                }
            }
        }
    }
}
