package com.autogarage.presentation.ui.inventory

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.hilt.navigation.compose.hiltViewModel
import com.autogarage.presentation.ui.components.*
import com.autogarage.presentation.ui.theme.Spacing
import com.autogarage.viewmodel.EditInventoryItemUiEvent
import com.autogarage.viewmodel.EditInventoryItemViewModel
import kotlinx.coroutines.flow.collectLatest

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditInventoryItemScreen(
    viewModel: EditInventoryItemViewModel = hiltViewModel(),
    onNavigateBack: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }
    var showUnsavedChangesDialog by remember { mutableStateOf(false) }

    // Handle back press with unsaved changes
    BackHandler {
        if (viewModel.hasUnsavedChanges()) {
            showUnsavedChangesDialog = true
        } else {
            onNavigateBack()
        }
    }

    LaunchedEffect(Unit) {
        viewModel.uiEvent.collectLatest { event ->
            when (event) {
                is EditInventoryItemUiEvent.ItemUpdated -> {
                    snackbarHostState.showSnackbar("Item updated successfully")
                    onNavigateBack()
                }
                is EditInventoryItemUiEvent.ShowError -> {
                    snackbarHostState.showSnackbar(event.message)
                }
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Edit Inventory Item") },
                navigationIcon = {
                    IconButton(onClick = {
                        if (viewModel.hasUnsavedChanges()) {
                            showUnsavedChangesDialog = true
                        } else {
                            onNavigateBack()
                        }
                    }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { padding ->
        when {
            uiState.isLoading -> {
                LoadingIndicator(message = "Loading item details...")
            }
            uiState.error != null -> {
                EmptyState(
                    icon = Icons.Default.Error,
                    title = "Error",
                    description = uiState.error ?: "Unknown error",
                    actionText = "Go Back",
                    onActionClick = onNavigateBack
                )
            }
            else -> {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(padding)
                        .verticalScroll(rememberScrollState())
                        .padding(Spacing.medium),
                    verticalArrangement = Arrangement.spacedBy(Spacing.medium)
                ) {
                    // Show changes indicator
                    if (viewModel.hasUnsavedChanges()) {
                        InfoCard(
                            title = "Unsaved Changes",
                            description = "You have unsaved changes. Don't forget to save!",
                            type = InfoType.WARNING,
                            icon = Icons.Default.Warning
                        )
                    }

                    // Basic Information
                    SectionHeader(title = "Basic Information")

                    GMTextField(
                        value = uiState.partNumber,
                        onValueChange = viewModel::onPartNumberChange,
                        label = "Part Number *",
                        placeholder = "Enter unique part number",
                        leadingIcon = Icons.Default.Tag,
                        isError = uiState.partNumberError != null,
                        errorMessage = uiState.partNumberError,
                        enabled = !uiState.isSaving
                    )

                    GMTextField(
                        value = uiState.name,
                        onValueChange = viewModel::onNameChange,
                        label = "Item Name *",
                        placeholder = "Enter item name",
                        leadingIcon = Icons.Default.Inventory,
                        isError = uiState.nameError != null,
                        errorMessage = uiState.nameError,
                        enabled = !uiState.isSaving
                    )

                    GMTextField(
                        value = uiState.description,
                        onValueChange = viewModel::onDescriptionChange,
                        label = "Description",
                        placeholder = "Enter item description (optional)",
                        leadingIcon = Icons.Default.Description,
                        singleLine = false,
                        maxLines = 3,
                        enabled = !uiState.isSaving
                    )

                    GMTextField(
                        value = uiState.category,
                        onValueChange = viewModel::onCategoryChange,
                        label = "Category *",
                        placeholder = "e.g., Engine Parts, Filters",
                        leadingIcon = Icons.Default.Category,
                        isError = uiState.categoryError != null,
                        errorMessage = uiState.categoryError,
                        enabled = !uiState.isSaving
                    )

                    GMTextField(
                        value = uiState.brand,
                        onValueChange = viewModel::onBrandChange,
                        label = "Brand",
                        placeholder = "Enter brand name (optional)",
                        leadingIcon = Icons.Default.Bookmark,
                        enabled = !uiState.isSaving
                    )

                    // Stock Information
                    SectionHeader(title = "Stock Information")

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(Spacing.small)
                    ) {
                        GMTextField(
                            value = uiState.currentStock,
                            onValueChange = viewModel::onCurrentStockChange,
                            label = "Current Stock *",
                            placeholder = "0",
                            leadingIcon = Icons.Default.Inventory2,
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            isError = uiState.currentStockError != null,
                            errorMessage = uiState.currentStockError,
                            enabled = !uiState.isSaving,
                            modifier = Modifier.weight(1f)
                        )

                        GMTextField(
                            value = uiState.minimumStock,
                            onValueChange = viewModel::onMinimumStockChange,
                            label = "Min Stock",
                            placeholder = "10",
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            enabled = !uiState.isSaving,
                            modifier = Modifier.weight(1f)
                        )
                    }

                    GMTextField(
                        value = uiState.unit,
                        onValueChange = viewModel::onUnitChange,
                        label = "Unit",
                        placeholder = "PCS, LITRE, KG",
                        leadingIcon = Icons.Default.Scale,
                        enabled = !uiState.isSaving
                    )

                    GMTextField(
                        value = uiState.location,
                        onValueChange = viewModel::onLocationChange,
                        label = "Storage Location",
                        placeholder = "Shelf/Bin location (optional)",
                        leadingIcon = Icons.Default.LocationOn,
                        enabled = !uiState.isSaving
                    )

                    // Pricing
                    SectionHeader(title = "Pricing")

                    GMTextField(
                        value = uiState.purchasePrice,
                        onValueChange = viewModel::onPurchasePriceChange,
                        label = "Purchase Price *",
                        placeholder = "0.00",
                        leadingIcon = Icons.Default.ShoppingCart,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        isError = uiState.purchasePriceError != null,
                        errorMessage = uiState.purchasePriceError,
                        enabled = !uiState.isSaving
                    )

                    GMTextField(
                        value = uiState.sellingPrice,
                        onValueChange = viewModel::onSellingPriceChange,
                        label = "Selling Price *",
                        placeholder = "0.00",
                        leadingIcon = Icons.Default.AttachMoney,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        isError = uiState.sellingPriceError != null,
                        errorMessage = uiState.sellingPriceError,
                        enabled = !uiState.isSaving
                    )

                    // Show profit margin
                    if (uiState.purchasePrice.toDoubleOrNull() != null &&
                        uiState.sellingPrice.toDoubleOrNull() != null) {
                        val purchase = uiState.purchasePrice.toDouble()
                        val selling = uiState.sellingPrice.toDouble()
                        val margin = if (purchase > 0) ((selling - purchase) / purchase) * 100 else 0.0

                        InfoCard(
                            title = "Profit Margin",
                            description = "${String.format("%.1f", margin)}% profit margin",
                            type = if (margin >= 20) InfoType.SUCCESS else InfoType.WARNING,
                            icon = Icons.Default.TrendingUp
                        )
                    }

                    Spacer(modifier = Modifier.height(Spacing.medium))

                    // Save Button
                    GMButton(
                        text = "Save Changes",
                        onClick = viewModel::onSaveClick,
                        type = ButtonType.PRIMARY,
                        size = ButtonSize.LARGE,
                        icon = Icons.Default.Save,
                        loading = uiState.isSaving,
                        enabled = viewModel.hasUnsavedChanges(),
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }
        }
    }

    // Unsaved Changes Dialog
    if (showUnsavedChangesDialog) {
        AlertDialog(
            onDismissRequest = { showUnsavedChangesDialog = false },
            icon = {
                Icon(
                    Icons.Default.Warning,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.error
                )
            },
            title = { Text("Unsaved Changes") },
            text = {
                Text("You have unsaved changes. Are you sure you want to leave without saving?")
            },
            confirmButton = {
                Button(
                    onClick = {
                        showUnsavedChangesDialog = false
                        onNavigateBack()
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.error
                    )
                ) {
                    Text("Leave")
                }
            },
            dismissButton = {
                TextButton(onClick = { showUnsavedChangesDialog = false }) {
                    Text("Stay")
                }
            }
        )
    }
}
