package com.autogarage.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.autogarage.domain.model.InventoryItem
import com.autogarage.domain.usecase.inventory.GetAllInventoryItemsUseCase
import com.autogarage.domain.usecase.inventory.GetLowStockItemsUseCase
import com.autogarage.domain.usecase.inventory.SearchInventoryUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class InventoryViewModel @Inject constructor(
    private val getAllInventoryItemsUseCase: GetAllInventoryItemsUseCase,
    private val getLowStockItemsUseCase: GetLowStockItemsUseCase,
    private val searchInventoryUseCase: SearchInventoryUseCase
) : ViewModel() {

    private val _selectedFilter = MutableStateFlow<InventoryFilter>(InventoryFilter.All)
    val selectedFilter: StateFlow<InventoryFilter> = _selectedFilter.asStateFlow()

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    private val _uiState = MutableStateFlow(InventoryUiState())
    val uiState: StateFlow<InventoryUiState> = _uiState.asStateFlow()

    private val _uiEvent = MutableSharedFlow<InventoryUiEvent>()
    val uiEvent: SharedFlow<InventoryUiEvent> = _uiEvent.asSharedFlow()

    init {
        loadInventory()
    }

    @OptIn(FlowPreview::class)
    private fun loadInventory() {
        viewModelScope.launch {
            combine(
                _selectedFilter,
                _searchQuery
            ) { filter, query ->
                Pair(filter, query)
            }.debounce(300)
                .flatMapLatest { (filter, query) ->
                    val itemsFlow = when (filter) {
                        InventoryFilter.All -> {
                            if (query.isBlank()) {
                                getAllInventoryItemsUseCase(Unit)
                            } else {
                                searchInventoryUseCase(query)
                            }
                        }
                        InventoryFilter.LowStock -> getLowStockItemsUseCase(Unit)
                    }

                    itemsFlow.map { items ->
                        if (filter == InventoryFilter.LowStock && query.isNotBlank()) {
                            items.filter { item ->
                                item.name.contains(query, ignoreCase = true) ||
                                        item.partNumber.contains(query, ignoreCase = true) ||
                                        item.category.contains(query, ignoreCase = true)
                            }
                        } else {
                            items
                        }
                    }
                }.catch { error ->
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            error = error.message ?: "Failed to load inventory"
                        )
                    }
                }.collect { items ->
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            items = items,
                            error = null
                        )
                    }
                }
        }
    }

    fun onFilterSelected(filter: InventoryFilter) {
        _selectedFilter.value = filter
    }

    fun onSearchQueryChange(query: String) {
        _searchQuery.value = query
    }

    fun onItemClick(itemId: Long) {
        viewModelScope.launch {
            _uiEvent.emit(InventoryUiEvent.NavigateToItemDetail(itemId))
        }
    }

    fun onAddItemClick() {
        viewModelScope.launch {
            _uiEvent.emit(InventoryUiEvent.NavigateToAddItem)
        }
    }

    fun onRefresh() {
        loadInventory()
    }
}

enum class InventoryFilter(val displayName: String) {
    All("All Items"),
    LowStock("Low Stock")
}

data class InventoryUiState(
    val isLoading: Boolean = true,
    val items: List<InventoryItem> = emptyList(),
    val error: String? = null
)

sealed class InventoryUiEvent {
    data class NavigateToItemDetail(val itemId: Long) : InventoryUiEvent()
    data object NavigateToAddItem : InventoryUiEvent()
    data class ShowMessage(val message: String) : InventoryUiEvent()
}
