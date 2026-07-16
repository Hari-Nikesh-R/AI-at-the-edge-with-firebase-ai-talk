package com.agenticedge.shopdemo.ui.search

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Divider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SuggestionChip
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.agenticedge.shopdemo.data.event.EventType
import com.agenticedge.shopdemo.data.model.Product
import com.agenticedge.shopdemo.data.model.ProductCatalog
import com.agenticedge.shopdemo.ui.AppViewModel
import com.agenticedge.shopdemo.ui.components.ProductCard
import com.google.mlkit.nl.entityextraction.Entity
import com.google.mlkit.nl.entityextraction.EntityExtraction
import com.google.mlkit.nl.entityextraction.EntityExtractionParams
import com.google.mlkit.nl.entityextraction.EntityExtractorOptions
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.delay

/**
 * README capability #7, Smart Search Assistant: suggests likely follow-up
 * queries before the backend is ever called. ML Kit's on-device Entity
 * Extraction detects whether the user already typed a price/quantity so we
 * don't suggest a redundant "under X" template.
 */
@Composable
fun SearchScreen(appViewModel: AppViewModel, onProductClick: (String) -> Unit) {
    var query by remember { mutableStateOf("") }
    var suggestions by remember { mutableStateOf(listOf<String>()) }
    var results by remember { mutableStateOf(listOf<Product>()) }

    val extractor = remember {
        EntityExtraction.getClient(
            EntityExtractorOptions.Builder(EntityExtractorOptions.ENGLISH).build()
        )
    }
    DisposableEffect(Unit) {
        onDispose { extractor.close() }
    }
    LaunchedEffect(Unit) {
        runCatching { extractor.downloadModelIfNeeded().await() }
    }

    LaunchedEffect(query) {
        delay(300)
        val trimmed = query.trim()
        results = if (trimmed.isEmpty()) emptyList() else ProductCatalog.all.filter {
            it.name.contains(trimmed, ignoreCase = true) || it.category.contains(trimmed, ignoreCase = true)
        }

        if (trimmed.length < 2) {
            suggestions = emptyList()
            return@LaunchedEffect
        }

        appViewModel.agentRepository.onEvent(EventType.SEARCH, "Search", trimmed)

        val detectedTypes = runCatching {
            extractor.annotate(EntityExtractionParams.Builder(trimmed).build()).await()
        }.getOrNull().orEmpty().flatMap { it.entities }.map { it.type }.toSet()

        suggestions = buildList {
            if (Entity.TYPE_MONEY !in detectedTypes) add("$trimmed under 50000")
            add("$trimmed battery comparison")
            add("best $trimmed for photography")
        }
    }

    Scaffold(topBar = { TopAppBar(title = { Text("Search") }) }) { padding ->
        Column(modifier = Modifier.padding(padding).padding(16.dp)) {
            OutlinedTextField(
                value = query,
                onValueChange = { query = it },
                modifier = Modifier.fillMaxWidth(),
                label = { Text("Search products") },
                leadingIcon = { Icon(Icons.Filled.Search, contentDescription = null) },
                singleLine = true
            )

            if (suggestions.isNotEmpty()) {
                Text(
                    "Suggested before you finish typing (on-device):",
                    style = MaterialTheme.typography.labelSmall,
                    modifier = Modifier.padding(top = 12.dp, bottom = 4.dp)
                )
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    suggestions.forEach { suggestion ->
                        SuggestionChip(onClick = { query = suggestion }, label = { Text(suggestion) })
                    }
                }
            }

            Divider(modifier = Modifier.padding(vertical = 12.dp))

            LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                items(results, key = { it.id }) { product ->
                    ProductCard(product = product, onClick = { onProductClick(product.id) })
                }
            }
        }
    }
}
