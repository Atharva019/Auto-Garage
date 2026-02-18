package com.autogarage.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.autogarage.domain.usecase.inventory.AddInventoryItemUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class AddInventoryItemViewModel @Inject constructor(
    private val addInventoryItemUseCase: AddInventoryItemUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(AddInventoryItemUiState())
    val uiState: StateFlow<AddInventoryItemUiState> = _uiState.asStateFlow()

    private val _uiEvent = MutableSharedFlow<AddInventoryItemUiEvent>()
    val uiEvent: SharedFlow<AddInventoryItemUiEvent> = _uiEvent.asSharedFlow()

    fun onPartNumberChange(partNumber: String) {
        _uiState.update { it.copy(partNumber = partNumber, partNumberError = null) }
    }

    fun onNameChange(name: String) {
        _uiState.update { it.copy(name = name, nameError = null) }
    }

    fun onDescriptionChange(description: String) {
        _uiState.update { it.copy(description = description) }
    }

    fun onCategoryChange(category: String) {
        _uiState.update { it.copy(category = category, categoryError = null) }
    }

    fun onBrandChange(brand: String) {
        _uiState.update { it.copy(brand = brand) }
    }

    fun onCurrentStockChange(stock: String) {
        val filtered = stock.filter { it.isDigit() }
        _uiState.update { it.copy(currentStock = filtered, currentStockError = null) }
    }

    fun onMinimumStockChange(stock: String) {
        val filtered = stock.filter { it.isDigit() }
        _uiState.update { it.copy(minimumStock = filtered) }
    }

    fun onUnitChange(unit: String) {
        _uiState.update { it.copy(unit = unit) }
    }

    fun onPurchasePriceChange(price: String) {
        val filtered = price.filter { it.isDigit() || it == '.' }
        _uiState.update { it.copy(purchasePrice = filtered, purchasePriceError = null) }
    }

    fun onSellingPriceChange(price: String) {
        val filtered = price.filter { it.isDigit() || it == '.' }
        _uiState.update { it.copy(sellingPrice = filtered, sellingPriceError = null) }
    }

    fun onLocationChange(location: String) {
        _uiState.update { it.copy(location = location) }
    }

    fun onSaveClick() {
        if (!validateForm()) return

        viewModelScope.launch {
            _uiState.update { it.copy(isSaving = true) }

            val result = addInventoryItemUseCase(
                AddInventoryItemUseCase.Params(
                    partNumber = _uiState.value.partNumber.trim(),
                    name = _uiState.value.name.trim(),
                    description = _uiState.value.description.trim().takeIf { it.isNotBlank() },
                    category = _uiState.value.category.trim(),
                    brand = _uiState.value.brand.trim().takeIf { it.isNotBlank() },
                    currentStock = _uiState.value.currentStock.toInt(),
                    minimumStock = _uiState.value.minimumStock.toIntOrNull() ?: 10,
                    purchasePrice = _uiState.value.purchasePrice.toDouble(),
                    sellingPrice = _uiState.value.sellingPrice.toDouble(),
                    location = _uiState.value.location.trim().takeIf { it.isNotBlank() }
                )
            )

            result.fold(
                onSuccess = { itemId ->
                    _uiEvent.emit(AddInventoryItemUiEvent.ItemAdded(itemId))
                },
                onFailure = { error ->
                    _uiState.update { it.copy(isSaving = false) }
                    _uiEvent.emit(
                        AddInventoryItemUiEvent.ShowError(
                            error.message ?: "Failed to add item"
                        )
                    )
                }
            )
        }
    }

    private fun validateForm(): Boolean {
        val state = _uiState.value
        var isValid = true

        if (state.partNumber.isBlank()) {
            _uiState.update { it.copy(partNumberError = "Part number is required") }
            isValid = false
        }

        if (state.name.isBlank()) {
            _uiState.update { it.copy(nameError = "Item name is required") }
            isValid = false
        }

        if (state.category.isBlank()) {
            _uiState.update { it.copy(categoryError = "Category is required") }
            isValid = false
        }

        if (state.currentStock.isBlank()) {
            _uiState.update { it.copy(currentStockError = "Current stock is required") }
            isValid = false
        }

        if (state.purchasePrice.isBlank()) {
            _uiState.update { it.copy(purchasePriceError = "Purchase price is required") }
            isValid = false
        }

        if (state.sellingPrice.isBlank()) {
            _uiState.update { it.copy(sellingPriceError = "Selling price is required") }
            isValid = false
        } else if (state.sellingPrice.toDoubleOrNull()?.let { it < state.purchasePrice.toDoubleOrNull() ?: 0.0 } == true) {
            _uiState.update { it.copy(sellingPriceError = "Selling price should be ≥ purchase price") }
            isValid = false
        }

        return isValid
    }
}

data class AddInventoryItemUiState(
    val partNumber: String = "",
    val name: String = "",
    val description: String = "",
    val category: String = "",
    val brand: String = "",
    val currentStock: String = "",
    val minimumStock: String = "10",
    val unit: String = "PCS",
    val purchasePrice: String = "",
    val sellingPrice: String = "",
    val location: String = "",
    val partNumberError: String? = null,
    val nameError: String? = null,
    val categoryError: String? = null,
    val currentStockError: String? = null,
    val purchasePriceError: String? = null,
    val sellingPriceError: String? = null,
    val isSaving: Boolean = false
)

sealed class AddInventoryItemUiEvent {
    data class ItemAdded(val itemId: Long) : AddInventoryItemUiEvent()
    data class ShowError(val message: String) : AddInventoryItemUiEvent()
}
