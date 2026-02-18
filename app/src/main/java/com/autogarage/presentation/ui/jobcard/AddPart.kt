package com.autogarage.presentation.ui.jobcard

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
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
import com.autogarage.presentation.ui.components.*
import com.autogarage.presentation.ui.theme.Spacing

//@OptIn(ExperimentalMaterial3Api::class)
//@Composable
//fun AddPartDialog(
//    availableParts: List<InventoryItem>,
//    categories: List<String>,
//    searchQuery: String,
//    selectedCategory: String?,
//    onSearchQueryChange: (String) -> Unit,
//    onCategorySelected: (String?) -> Unit,
//    onPartSelected: (InventoryItem, Int) -> Unit,
//    onDismiss: () -> Unit
//) {
//    var selectedPart by remember { mutableStateOf<InventoryItem?>(null) }
//    var quantity by remember { mutableStateOf("1") }
//    var showCategoryMenu by remember { mutableStateOf(false) }
//
//    AlertDialog(
//        onDismissRequest = onDismiss,
//        modifier = Modifier
//            .fillMaxWidth()
//            .fillMaxHeight(0.9f)
//    ) {
//        Surface(
//            modifier = Modifier.fillMaxSize(),
//            shape = RoundedCornerShape(28.dp),
//            color = MaterialTheme.colorScheme.surface
//        ) {
//            Column(
//                modifier = Modifier
//                    .fillMaxSize()
//                    .padding(24.dp)
//            ) {
//                // Header
//                Row(
//                    modifier = Modifier.fillMaxWidth(),
//                    horizontalArrangement = Arrangement.SpaceBetween,
//                    verticalAlignment = Alignment.CenterVertically
//                ) {
//                    Text(
//                        text = "Add Part",
//                        style = MaterialTheme.typography.headlineSmall,
//                        fontWeight = FontWeight.Bold
//                    )
//                    IconButton(onClick = onDismiss) {
//                        Icon(Icons.Default.Close, contentDescription = "Close")
//                    }
//                }
//
//                Spacer(modifier = Modifier.height(16.dp))
//
//                // Search Bar
//                GMSearchBar(
//                    query = searchQuery,
//                    onQueryChange = onSearchQueryChange,
//                    placeholder = "Search parts...",
//                    modifier = Modifier.fillMaxWidth()
//                )
//
//                Spacer(modifier = Modifier.height(12.dp))
//
//                // Category Filter
//                Row(
//                    modifier = Modifier.fillMaxWidth(),
//                    horizontalArrangement = Arrangement.spacedBy(8.dp),
//                    verticalAlignment = Alignment.CenterVertically
//                ) {
//                    Text(
//                        text = "Category:",
//                        style = MaterialTheme.typography.labelLarge,
//                        modifier = Modifier.padding(end = 8.dp)
//                    )
//
//                    Box {
//                        OutlinedButton(
//                            onClick = { showCategoryMenu = true },
//                            modifier = Modifier.fillMaxWidth()
//                        ) {
//                            Icon(
//                                Icons.Default.FilterList,
//                                contentDescription = null,
//                                modifier = Modifier.size(18.dp)
//                            )
//                            Spacer(modifier = Modifier.width(8.dp))
//                            Text(selectedCategory ?: "All Categories")
//                            Spacer(modifier = Modifier.width(8.dp))
//                            Icon(
//                                Icons.Default.ArrowDropDown,
//                                contentDescription = null
//                            )
//                        }
//
//                        DropdownMenu(
//                            expanded = showCategoryMenu,
//                            onDismissRequest = { showCategoryMenu = false }
//                        ) {
//                            DropdownMenuItem(
//                                text = { Text("All Categories") },
//                                onClick = {
//                                    onCategorySelected(null)
//                                    showCategoryMenu = false
//                                },
//                                leadingIcon = {
//                                    if (selectedCategory == null) {
//                                        Icon(Icons.Default.Check, contentDescription = null)
//                                    }
//                                }
//                            )
//                            Divider()
//                            categories.forEach { category ->
//                                DropdownMenuItem(
//                                    text = { Text(category) },
//                                    onClick = {
//                                        onCategorySelected(category)
//                                        showCategoryMenu = false
//                                    },
//                                    leadingIcon = {
//                                        if (selectedCategory == category) {
//                                            Icon(Icons.Default.Check, contentDescription = null)
//                                        }
//                                    }
//                                )
//                            }
//                        }
//                    }
//
//                    if (selectedCategory != null) {
//                        IconButton(
//                            onClick = { onCategorySelected(null) }
//                        ) {
//                            Icon(
//                                Icons.Default.Clear,
//                                contentDescription = "Clear filter",
//                                tint = MaterialTheme.colorScheme.error
//                            )
//                        }
//                    }
//                }
//
//                Spacer(modifier = Modifier.height(16.dp))
//
//                // Parts List
//                if (availableParts.isEmpty()) {
//                    Box(
//                        modifier = Modifier
//                            .fillMaxWidth()
//                            .weight(1f),
//                        contentAlignment = Alignment.Center
//                    ) {
//                        Column(
//                            horizontalAlignment = Alignment.CenterHorizontally,
//                            verticalArrangement = Arrangement.spacedBy(8.dp)
//                        ) {
//                            Icon(
//                                Icons.Default.Inventory,
//                                contentDescription = null,
//                                modifier = Modifier.size(48.dp),
//                                tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
//                            )
//                            Text(
//                                text = "No parts found",
//                                style = MaterialTheme.typography.bodyLarge,
//                                color = MaterialTheme.colorScheme.onSurfaceVariant
//                            )
//                        }
//                    }
//                } else {
//                    LazyColumn(
//                        modifier = Modifier.weight(1f),
//                        verticalArrangement = Arrangement.spacedBy(8.dp)
//                    ) {
//                        items(availableParts) { part ->
//                            PartItemCard(
//                                part = part,
//                                isSelected = selectedPart?.id == part.id,
//                                onClick = { selectedPart = part }
//                            )
//                        }
//                    }
//                }
//
//                // Selected Part Details & Quantity
//                if (selectedPart != null) {
//                    Spacer(modifier = Modifier.height(16.dp))
//
//                    Card(
//                        modifier = Modifier.fillMaxWidth(),
//                        colors = CardDefaults.cardColors(
//                            containerColor = MaterialTheme.colorScheme.primaryContainer
//                        )
//                    ) {
//                        Column(
//                            modifier = Modifier
//                                .fillMaxWidth()
//                                .padding(16.dp),
//                            verticalArrangement = Arrangement.spacedBy(12.dp)
//                        ) {
//                            Text(
//                                text = "Selected: ${selectedPart!!.name}",
//                                style = MaterialTheme.typography.titleMedium,
//                                fontWeight = FontWeight.Bold,
//                                color = MaterialTheme.colorScheme.onPrimaryContainer
//                            )
//
//                            Row(
//                                modifier = Modifier.fillMaxWidth(),
//                                horizontalArrangement = Arrangement.SpaceBetween
//                            ) {
//                                Text(
//                                    text = "Part #: ${selectedPart!!.partNumber}",
//                                    style = MaterialTheme.typography.bodyMedium
//                                )
//                                Text(
//                                    text = "Price: ₹${String.format("%.2f", selectedPart!!.sellingPrice)}",
//                                    style = MaterialTheme.typography.bodyMedium,
//                                    fontWeight = FontWeight.Bold
//                                )
//                            }
//
//                            GMTextField(
//                                value = quantity,
//                                onValueChange = {
//                                    val filtered = it.filter { char -> char.isDigit() }
//                                    if (filtered.isEmpty() || filtered.toIntOrNull() != null) {
//                                        quantity = filtered
//                                    }
//                                },
//                                label = "Quantity",
//                                placeholder = "Enter quantity",
//                                leadingIcon = Icons.Default.ShoppingCart,
//                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
//                                supportingText = "Available: ${selectedPart!!.maximumStock} units"
//                            )
//
//                            val totalCost = (quantity.toIntOrNull() ?: 0) * selectedPart!!.sellingPrice
//                            if (totalCost > 0) {
//                                Divider()
//                                Row(
//                                    modifier = Modifier.fillMaxWidth(),
//                                    horizontalArrangement = Arrangement.SpaceBetween,
//                                    verticalAlignment = Alignment.CenterVertically
//                                ) {
//                                    Text(
//                                        text = "Total Cost:",
//                                        style = MaterialTheme.typography.titleMedium,
//                                        fontWeight = FontWeight.Bold
//                                    )
//                                    Text(
//                                        text = "₹${String.format("%.2f", totalCost)}",
//                                        style = MaterialTheme.typography.titleLarge,
//                                        fontWeight = FontWeight.Bold,
//                                        color = MaterialTheme.colorScheme.primary
//                                    )
//                                }
//                            }
//                        }
//                    }
//                }
//
//                Spacer(modifier = Modifier.height(16.dp))
//
//                // Action Buttons
//                Row(
//                    modifier = Modifier.fillMaxWidth(),
//                    horizontalArrangement = Arrangement.spacedBy(12.dp)
//                ) {
//                    GMButton(
//                        text = "Cancel",
//                        onClick = onDismiss,
//                        type = ButtonType.OUTLINED,
//                        modifier = Modifier.weight(1f)
//                    )
//
//                    GMButton(
//                        text = "Add Part",
//                        onClick = {
//                            selectedPart?.let { part ->
//                                val qty = quantity.toIntOrNull() ?: 1
//                                if (qty > 0 && qty <= part.currentStock) {
//                                    onPartSelected(part, qty)
//                                }
//                            }
//                        },
//                        type = ButtonType.PRIMARY,
//                        enabled = selectedPart != null &&
//                                quantity.toIntOrNull()?.let { it > 0 && it <= (selectedPart?.currentStock ?: 0) } == true,
//                        modifier = Modifier.weight(1f)
//                    )
//                }
//            }
//        }
//    }
//}

@Composable
private fun PartItemCard(
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
            androidx.compose.foundation.BorderStroke(
                2.dp,
                MaterialTheme.colorScheme.primary
            )
        } else null
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Text(
                    text = part.name,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold
                )
                Text(
                    text = "Part #: ${part.partNumber}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = "Category: ${part.category}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                // Stock indicator
                val stockColor = when {
                    part.currentStock == 0 -> MaterialTheme.colorScheme.error
                    part.currentStock <= part.minimumStock -> MaterialTheme.colorScheme.tertiary
                    else -> MaterialTheme.colorScheme.primary
                }

                Surface(
                    shape = MaterialTheme.shapes.small,
                    color = stockColor.copy(alpha = 0.1f)
                ) {
                    Text(
                        text = "Stock: ${part.currentStock} units",
                        style = MaterialTheme.typography.labelSmall,
                        color = stockColor,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }
            }

            Column(
                horizontalAlignment = Alignment.End,
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Text(
                    text = "₹${String.format("%.2f", part.sellingPrice)}",
                    style = MaterialTheme.typography.titleLarge,
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