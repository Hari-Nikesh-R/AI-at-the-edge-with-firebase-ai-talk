package com.agenticedge.shopdemo.ui.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.agenticedge.shopdemo.data.model.Product

@Composable
fun ProductCard(product: Product, modifier: Modifier = Modifier, onClick: () -> Unit) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Text(product.emoji, style = MaterialTheme.typography.headlineSmall)
            Text(product.name, style = MaterialTheme.typography.titleMedium)
            Text(product.category, style = MaterialTheme.typography.labelSmall)
            val discounted = product.discountedPrice
            Text("₹$discounted", style = MaterialTheme.typography.titleMedium)
            if (product.discountPercent > 0) {
                Text(
                    "₹${product.price} · ${product.discountPercent}% off",
                    style = MaterialTheme.typography.labelSmall
                )
            }
            Text("★ ${product.rating} (${product.reviewCount})", style = MaterialTheme.typography.labelSmall)
        }
    }
}
