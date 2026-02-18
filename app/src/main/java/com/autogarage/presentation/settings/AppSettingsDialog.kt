package com.autogarage.presentation.settings

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import com.autogarage.presentation.ui.components.GMTextField
import com.autogarage.presentation.ui.components.InfoCard
import com.autogarage.presentation.ui.theme.Spacing

@Composable
fun AppSettingsDialog(
    currency: String,
    taxRate: String,
    onCurrencyChange: (String) -> Unit,
    onTaxRateChange: (String) -> Unit,
    onSave: () -> Unit,
    onDismiss: () -> Unit
) {
    var showCurrencyMenu by remember { mutableStateOf(false) }
    val currencies = listOf("₹" to "Indian Rupee (₹)", "$" to "US Dollar ($)", "€" to "Euro (€)", "£" to "Pound (£)")

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(
                horizontalArrangement = Arrangement.spacedBy(Spacing.small),
                modifier = Modifier.fillMaxWidth()
            ) {
                Icon(
                    Icons.Default.Settings,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary
                )
                Text("App Settings")
            }
        },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(Spacing.medium)
            ) {
                // Currency Selection
                Text(
                    text = "Currency",
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Box {
                    OutlinedTextField(
                        value = currencies.find { it.first == currency }?.second ?: currency,
                        onValueChange = {},
                        label = { Text("Currency") },
                        leadingIcon = {
                            Icon(Icons.Default.AttachMoney, contentDescription = null)//AttachMoney
                        },
                        trailingIcon = {
                            Icon(Icons.Default.ArrowDropDown, contentDescription = null)
                        },
                        readOnly = true,
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { showCurrencyMenu = true }
                    )

                    DropdownMenu(
                        expanded = showCurrencyMenu,
                        onDismissRequest = { showCurrencyMenu = false }
                    ) {
                        currencies.forEach { (symbol, name) ->
                            DropdownMenuItem(
                                text = { Text(name) },
                                onClick = {
                                    onCurrencyChange(symbol)
                                    showCurrencyMenu = false
                                },
                                leadingIcon = {
                                    if (symbol == currency) {
                                        Icon(Icons.Default.Check, contentDescription = null)
                                    }
                                }
                            )
                        }
                    }
                }

                // Tax Rate
                GMTextField(
                    value = taxRate,
                    onValueChange = onTaxRateChange,
                    label = "Default Tax Rate (%)",
                    placeholder = "e.g., 18.0",
                    leadingIcon = Icons.Default.Percent,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal)
                )

                InfoCard(
                    title = "Default Settings",
                    description = "These settings will be used as default values when creating new invoices and job cards.",
                    type = com.autogarage.presentation.ui.components.InfoType.INFO,
                    icon = Icons.Default.Info
                )
            }
        },
        confirmButton = {
            Button(onClick = onSave) {
                Text("Save")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}