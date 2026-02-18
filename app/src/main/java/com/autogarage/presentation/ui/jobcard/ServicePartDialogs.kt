package com.autogarage.presentation.ui.jobcard

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.autogarage.domain.model.InventoryItem
import com.autogarage.presentation.ui.components.ButtonSize
import com.autogarage.presentation.ui.components.ButtonType
import com.autogarage.presentation.ui.components.GMButton
import com.autogarage.presentation.ui.components.GMSearchBar
import com.autogarage.presentation.ui.components.GMTextField
import com.autogarage.presentation.ui.components.StockBadge
import com.autogarage.presentation.ui.components.StockStatus
import com.autogarage.presentation.ui.theme.Spacing

@Composable
fun AddServiceDialog(
    onDismiss: () -> Unit,
    onConfirm: (serviceName: String, laborCost: Double, quantity: Int) -> Unit
) {
    var serviceName by remember { mutableStateOf("") }
    var laborCost by remember { mutableStateOf("") }
    var quantity by remember { mutableStateOf("1") }

    AlertDialog(
        onDismissRequest = onDismiss,
        icon = {
            Icon(
                Icons.Default.Build,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary
            )
        },
        title = { Text("Add Service") },
        text = {
            Column(
                verticalArrangement = Arrangement.spacedBy(Spacing.medium)
            ) {
                GMTextField(
                    value = serviceName,
                    onValueChange = { serviceName = it },
                    label = "Service Name *",
                    placeholder = "e.g., Oil Change",
                    leadingIcon = Icons.Default.Build
                )

                GMTextField(
                    value = laborCost,
                    onValueChange = { laborCost = it },
                    label = "Labor Cost *",
                    placeholder = "0.00",
                    leadingIcon = Icons.Default.Money,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal)
                )

                GMTextField(
                    value = quantity,
                    onValueChange = { quantity = it },
                    label = "Quantity",
                    placeholder = "1",
                    leadingIcon = Icons.Default.Numbers,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                )

                // Total
                val total = (laborCost.toDoubleOrNull() ?: 0.0) * (quantity.toIntOrNull() ?: 1)
                if (total > 0) {
                    Surface(
                        shape = MaterialTheme.shapes.small,
                        color = MaterialTheme.colorScheme.primaryContainer
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(Spacing.small),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("Total:")
                            Text(
                                "₹${String.format("%.2f", total)}",
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val name = serviceName.trim()
                    val cost = laborCost.toDoubleOrNull() ?: 0.0
                    val qty = quantity.toIntOrNull() ?: 1

                    if (name.isNotEmpty() && cost > 0 && qty > 0) {
                        onConfirm(name, cost, qty)
                    }
                },
                enabled = serviceName.isNotBlank() &&
                        (laborCost.toDoubleOrNull() ?: 0.0) > 0 &&
                        (quantity.toIntOrNull() ?: 0) > 0
            ) {
                Text("Add")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}

// ===========================================================================
// AddPartDialog.kt - Dialog for Adding Parts to Job Card
// ===========================================================================
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddPartDialog(
    availableParts: List<InventoryItem>,
    categories: List<String>,
    searchQuery: String,
    selectedCategory: String?,
    onSearchQueryChange: (String) -> Unit,
    onCategorySelected: (String?) -> Unit,
    onPartSelected: (InventoryItem, Int) -> Unit,
    onDismiss: () -> Unit
) {
    var selectedPart by remember { mutableStateOf<InventoryItem?>(null) }
    var quantity by remember { mutableStateOf("1") }

    AlertDialog(
        onDismissRequest = onDismiss,
        modifier = Modifier
            .fillMaxWidth()
            .fillMaxHeight(0.9f)
    ) {
        Surface(
            modifier = Modifier.fillMaxSize(),
            shape = MaterialTheme.shapes.extraLarge,
            color = MaterialTheme.colorScheme.surface
        ) {
            Column(
                modifier = Modifier.fillMaxSize()
            ) {
                // Header
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(Spacing.medium),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        "Add Part from Inventory",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold
                    )
                    IconButton(onClick = onDismiss) {
                        Icon(Icons.Default.Close, contentDescription = "Close")
                    }
                }

                Divider()

                // Search Bar
                GMSearchBar(
                    query = searchQuery,
                    onQueryChange = onSearchQueryChange,
                    placeholder = "Search parts...",
                    modifier = Modifier.padding(horizontal = Spacing.medium, vertical = Spacing.small)
                )

                // Category Filter
                if (categories.isNotEmpty()) {
                    LazyRow(
                        contentPadding = PaddingValues(horizontal = Spacing.medium),
                        horizontalArrangement = Arrangement.spacedBy(Spacing.small)
                    ) {
                        item {
                            FilterChip(
                                selected = selectedCategory == null,
                                onClick = { onCategorySelected(null) },
                                label = { Text("All") }
                            )
                        }
                        items(categories) { category ->
                            FilterChip(
                                selected = selectedCategory == category,
                                onClick = { onCategorySelected(category) },
                                label = { Text(category) }
                            )
                        }
                    }
                }

                Divider()

                // Parts List
                val filteredParts = if (selectedCategory == null) {
                    availableParts
                } else {
                    availableParts.filter { it.category == selectedCategory }
                }

                if (filteredParts.isEmpty()) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(Spacing.small)
                        ) {
                            Icon(
                                Icons.Default.SearchOff,
                                contentDescription = null,
                                modifier = Modifier.size(48.dp),
                                tint = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Text(
                                "No parts found",
                                style = MaterialTheme.typography.bodyLarge,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                } else {
                    LazyColumn(
                        modifier = Modifier.weight(1f),
                        contentPadding = PaddingValues(Spacing.medium),
                        verticalArrangement = Arrangement.spacedBy(Spacing.small)
                    ) {
                        items(filteredParts) { part ->
                            InventoryPartItem(
                                part = part,
                                isSelected = selectedPart?.id == part.id,
                                onClick = { selectedPart = part }
                            )
                        }
                    }
                }

                // Bottom Section - Quantity & Add Button
                if (selectedPart != null) {
                    Divider()

                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(Spacing.medium),
                        verticalArrangement = Arrangement.spacedBy(Spacing.medium)
                    ) {
                        // Selected Part Summary
                        Card(
                            colors = CardDefaults.cardColors(
                                containerColor = MaterialTheme.colorScheme.primaryContainer
                            )
                        ) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(Spacing.medium)
                            ) {
                                Text(
                                    selectedPart!!.name,
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    "₹${String.format("%.2f", selectedPart!!.sellingPrice)}",
                                    style = MaterialTheme.typography.titleLarge,
                                    color = MaterialTheme.colorScheme.primary
                                )
                            }
                        }

                        // Quantity Input
                        GMTextField(
                            value = quantity,
                            onValueChange = { value ->
                                if (value.isEmpty() || value.all { it.isDigit() }) {
                                    quantity = value
                                }
                            },
                            label = "Quantity",
                            placeholder = "1",
                            leadingIcon = Icons.Default.Numbers,
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                        )

                        // Total Price
                        val totalPrice = (quantity.toIntOrNull() ?: 1) * selectedPart!!.sellingPrice
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                "Total:",
                                style = MaterialTheme.typography.titleMedium
                            )
                            Text(
                                "₹${String.format("%.2f", totalPrice)}",
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }

                        // Add Button
                        GMButton(
                            text = "Add Part",
                            onClick = {
                                val qty = quantity.toIntOrNull() ?: 1
                                if (qty > 0) {
                                    onPartSelected(selectedPart!!, qty)
                                }
                            },
                            type = ButtonType.PRIMARY,
                            size = ButtonSize.LARGE,
                            icon = Icons.Default.Add,
                            enabled = quantity.isNotEmpty() && (quantity.toIntOrNull() ?: 0) > 0,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun InventoryPartItem(
    part: InventoryItem,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Card(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = if (isSelected)
                MaterialTheme.colorScheme.primaryContainer
            else
                MaterialTheme.colorScheme.surface
        ),
        border = if (isSelected) {
            CardDefaults.outlinedCardBorder().copy(
                width = 2.dp,
                brush = androidx.compose.ui.graphics.SolidColor(
                    MaterialTheme.colorScheme.primary
                )
            )
        } else null
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(Spacing.medium),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                modifier = Modifier.weight(1f),
                horizontalArrangement = Arrangement.spacedBy(Spacing.medium),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Icon
                Surface(
                    shape = MaterialTheme.shapes.small,
                    color = if (isSelected)
                        MaterialTheme.colorScheme.primary
                    else
                        MaterialTheme.colorScheme.primaryContainer,
                    modifier = Modifier.size(40.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            Icons.Default.Inventory,
                            contentDescription = null,
                            tint = if (isSelected)
                                MaterialTheme.colorScheme.onPrimary
                            else
                                MaterialTheme.colorScheme.onPrimaryContainer
                        )
                    }
                }

                // Part Details
                Column {
                    Text(
                        part.name,
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.SemiBold
                    )
                    Text(
                        part.partNumber,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    // Stock Status
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        StockBadge(
                            stockStatus = when {
                                part.currentStock <= 0 -> StockStatus.OUT_OF_STOCK
                                part.currentStock <= part.minimumStock -> StockStatus.LOW_STOCK
                                else -> StockStatus.IN_STOCK
                            }
                        )
                        Text(
                            "(${part.currentStock} in stock)",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            // Price
            Column(
                horizontalAlignment = Alignment.End
            ) {
                Text(
                    "₹${String.format("%.2f", part.sellingPrice)}",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
                if (isSelected) {
                    Icon(
                        Icons.Default.CheckCircle,
                        contentDescription = "Selected",
                        tint = MaterialTheme.colorScheme.primary
                    )
                }
            }
        }
    }
}


// ===========================================================================
// Additional Icon Extensions (if not already defined)
// ===========================================================================
// Note: Some icons might need to be replaced with available Material Icons
// Here are the icons used:
// - Icons.Default.Build (service)
// - Icons.Default.CurrencyRupee (₹ symbol)
// - Icons.Default.Numbers (quantity)
// - Icons.Default.Inventory (parts)
// - Icons.Default.Tag (part number)
// - Icons.Default.Discount (discount)

// If CurrencyRupee or Numbers are not available, you can use:
// - Icons.Default.AttachMoney instead of CurrencyRupee
// - Icons.Default.Pin or Icons.Default.Looks3 instead of Numbers