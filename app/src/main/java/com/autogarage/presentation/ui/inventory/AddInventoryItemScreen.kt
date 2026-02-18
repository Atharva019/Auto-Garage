package com.autogarage.presentation.ui.inventory

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
import com.autogarage.viewmodel.AddInventoryItemUiEvent
import com.autogarage.viewmodel.AddInventoryItemViewModel
import kotlinx.coroutines.flow.collectLatest

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddInventoryItemScreen(
    viewModel: AddInventoryItemViewModel = hiltViewModel(),
    onNavigateBack: () -> Unit,
    onItemAdded: (Long) -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(Unit) {
        viewModel.uiEvent.collectLatest { event ->
            when (event) {
                is AddInventoryItemUiEvent.ItemAdded -> {
                    snackbarHostState.showSnackbar("Item added successfully")
                    onItemAdded(event.itemId)
                }
                is AddInventoryItemUiEvent.ShowError -> {
                    snackbarHostState.showSnackbar(event.message)
                }
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Add Inventory Item") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(Spacing.medium),
            verticalArrangement = Arrangement.spacedBy(Spacing.medium)
        ) {
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
                leadingIcon = Icons.Default.BrandingWatermark,
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

            // Show profit margin if both prices are entered
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
                text = "Add Item",
                onClick = viewModel::onSaveClick,
                type = ButtonType.PRIMARY,
                size = ButtonSize.LARGE,
                icon = Icons.Default.Save,
                loading = uiState.isSaving,
                modifier = Modifier.fillMaxWidth()
            )

            // Info Card
            InfoCard(
                title = "Required Fields",
                description = "Fields marked with * are mandatory",
                type = InfoType.INFO,
                icon = Icons.Default.Info
            )
        }
    }
}
