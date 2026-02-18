package com.autogarage.viewmodel

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.autogarage.domain.usecase.inventory.GetInventoryItemByIdUseCase
import com.autogarage.domain.usecase.inventory.UpdateInventoryItemUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class EditInventoryItemViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val getInventoryItemByIdUseCase: GetInventoryItemByIdUseCase,
    private val updateInventoryItemUseCase: UpdateInventoryItemUseCase
) : ViewModel() {

    private val itemId: Long = savedStateHandle.get<Long>("itemId") ?: 0L

    private val _uiState = MutableStateFlow(EditInventoryItemUiState())
    val uiState: StateFlow<EditInventoryItemUiState> = _uiState.asStateFlow()

    private val _uiEvent = MutableSharedFlow<EditInventoryItemUiEvent>()
    val uiEvent: SharedFlow<EditInventoryItemUiEvent> = _uiEvent.asSharedFlow()

    init {
        loadInventoryItem()
    }

    private fun loadInventoryItem() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }

            getInventoryItemByIdUseCase(itemId).collect { item ->
                if (item != null) {
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            partNumber = item.partNumber,
                            name = item.name,
                            description = item.description ?: "",
                            category = item.category,
                            brand = item.brand ?: "",
                            currentStock = item.currentStock.toString(),
                            minimumStock = item.minimumStock.toString(),
                            unit = item.unit,
                            purchasePrice = item.purchasePrice.toString(),
                            sellingPrice = item.sellingPrice.toString(),
                            location = item.location ?: "",
                            originalItem = item,
                            error = null
                        )
                    }
                } else {
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            error = "Item not found"
                        )
                    }
                }
            }
        }
    }

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

            val result = updateInventoryItemUseCase(
                UpdateInventoryItemUseCase.Params(
                    itemId = itemId,
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
                onSuccess = {
                    _uiEvent.emit(EditInventoryItemUiEvent.ItemUpdated)
                },
                onFailure = { error ->
                    _uiState.update { it.copy(isSaving = false) }
                    _uiEvent.emit(
                        EditInventoryItemUiEvent.ShowError(
                            error.message ?: "Failed to update item"
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
        } else if (state.sellingPrice.toDoubleOrNull()?.let {
                it < state.purchasePrice.toDoubleOrNull() ?: 0.0
            } == true) {
            _uiState.update {
                it.copy(sellingPriceError = "Selling price should be ≥ purchase price")
            }
            isValid = false
        }

        return isValid
    }

    fun hasUnsavedChanges(): Boolean {
        val state = _uiState.value
        val original = state.originalItem ?: return false

        return state.partNumber != original.partNumber ||
                state.name != original.name ||
                state.description != (original.description ?: "") ||
                state.category != original.category ||
                state.brand != (original.brand ?: "") ||
                state.currentStock != original.currentStock.toString() ||
                state.minimumStock != original.minimumStock.toString() ||
                state.purchasePrice != original.purchasePrice.toString() ||
                state.sellingPrice != original.sellingPrice.toString() ||
                state.location != (original.location ?: "")
    }
}

data class EditInventoryItemUiState(
    val isLoading: Boolean = true,
    val originalItem: com.autogarage.domain.model.InventoryItem? = null,
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
    val isSaving: Boolean = false,
    val error: String? = null
)

sealed class EditInventoryItemUiEvent {
    data object ItemUpdated : EditInventoryItemUiEvent()
    data class ShowError(val message: String) : EditInventoryItemUiEvent()
}