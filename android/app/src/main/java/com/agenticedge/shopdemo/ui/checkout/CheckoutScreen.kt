package com.agenticedge.shopdemo.ui.checkout

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.agenticedge.shopdemo.ui.AppViewModel

private const val FIELD_ADDRESS_LINE_2 = "Address Line 2"

/** README capability #8, Self-Healing Forms. */
@Composable
fun CheckoutScreen(appViewModel: AppViewModel, onOrderPlaced: () -> Unit) {
    var name by remember { mutableStateOf("") }
    var addressLine1 by remember { mutableStateOf("") }
    var addressLine2 by remember { mutableStateOf("") }
    var city by remember { mutableStateOf("") }
    var pincode by remember { mutableStateOf("") }
    var addressLine2Hint by remember { mutableStateOf(false) }
    var addressLine2Optional by remember { mutableStateOf(false) }
    var addressLine2HadFocus by remember { mutableStateOf(false) }

    val suggestion by appViewModel.formSuggestion.collectAsState()

    if (suggestion?.field == FIELD_ADDRESS_LINE_2) {
        addressLine2Hint = true
        addressLine2Optional = true
    }

    Scaffold(topBar = { TopAppBar(title = { Text("Checkout") }) }) { padding ->
        Column(
            modifier = Modifier.padding(padding).padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            suggestion?.let {
                Card {
                    Column(Modifier.padding(12.dp)) {
                        Text("Self-Healing Form Agent", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                        Text(it.reason, style = MaterialTheme.typography.bodyMedium)
                        Text("Action: ${it.action}", style = MaterialTheme.typography.labelSmall)
                    }
                }
            }

            OutlinedTextField(
                value = name, onValueChange = { name = it },
                label = { Text("Full name") }, modifier = Modifier.fillMaxWidth()
            )
            OutlinedTextField(
                value = addressLine1, onValueChange = { addressLine1 = it },
                label = { Text("Address Line 1") }, modifier = Modifier.fillMaxWidth()
            )
            OutlinedTextField(
                value = addressLine2,
                onValueChange = { addressLine2 = it },
                label = { Text("Address Line 2" + if (addressLine2Optional) " (optional)" else "") },
                placeholder = { if (addressLine2Hint) Text("e.g. Apartment, floor, landmark") },
                modifier = Modifier
                    .fillMaxWidth()
                    .onFocusChanged { focusState ->
                        if (addressLine2HadFocus && !focusState.isFocused && addressLine2.isBlank()) {
                            appViewModel.recordFieldAbandon(FIELD_ADDRESS_LINE_2)
                        }
                        addressLine2HadFocus = focusState.isFocused
                    }
            )
            OutlinedTextField(
                value = city, onValueChange = { city = it },
                label = { Text("City") }, modifier = Modifier.fillMaxWidth()
            )
            OutlinedTextField(
                value = pincode, onValueChange = { pincode = it },
                label = { Text("Pincode") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                modifier = Modifier.fillMaxWidth()
            )

            OutlinedButton(
                onClick = {
                    repeat(10) {
                        appViewModel.recordCheckoutAttempt()
                        appViewModel.recordFieldAbandon(FIELD_ADDRESS_LINE_2)
                    }
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Simulate 10 more checkouts abandoning here")
            }

            Button(
                onClick = {
                    appViewModel.recordCheckoutAttempt()
                    appViewModel.clearCart()
                    onOrderPlaced()
                },
                modifier = Modifier.fillMaxWidth()
            ) { Text("Place Order") }
        }
    }
}
