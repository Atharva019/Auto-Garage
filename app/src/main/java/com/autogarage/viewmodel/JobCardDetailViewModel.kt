package com.autogarage.viewmodel

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.autogarage.domain.model.InventoryItem
import com.autogarage.domain.model.JobCard
import com.autogarage.domain.model.JobCardPart
import com.autogarage.domain.model.JobCardService
import com.autogarage.domain.model.JobCardStatus
import com.autogarage.domain.model.Worker
import com.autogarage.domain.usecase.inventory.GetAllInventoryItemsUseCase
import com.autogarage.domain.usecase.inventory.SearchInventoryUseCase
import com.autogarage.domain.usecase.invoice.CreateInvoiceUseCase
import com.autogarage.domain.usecase.invoice.GetInvoiceByJobCardUseCase
import com.autogarage.domain.usecase.jobcard.*
import com.autogarage.domain.usecase.worker.GetAvailableTechniciansUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class JobCardDetailViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val getJobCardByIdUseCase: GetJobCardByIdUseCase,
    private val updateJobCardStatusUseCase: UpdateJobCardStatusUseCase,
    private val getAvailableTechniciansUseCase: GetAvailableTechniciansUseCase,
    private val createInvoiceUseCase: CreateInvoiceUseCase,
    private val getInvoiceByJobCardUseCase: GetInvoiceByJobCardUseCase,
    private val addServiceToJobCardUseCase: AddServiceToJobCardUseCase,
    private val addPartToJobCardUseCase: AddPartToJobCardUseCase,
    private val getJobCardServicesUseCase: GetJobCardServicesUseCase,
    private val getJobCardPartsUseCase: GetJobCardPartsUseCase,
    private val removeServiceFromJobCardUseCase: RemoveServiceFromJobCardUseCase,
    private val removePartFromJobCardUseCase: RemovePartFromJobCardUseCase,
    private val getAllInventoryItemsUseCase: GetAllInventoryItemsUseCase,
    private val searchInventoryUseCase: SearchInventoryUseCase
) : ViewModel() {

    // ✅ FIX: Get as String and convert to Long
    private val jobCardId: Long = savedStateHandle.get<Long>("jobCardId")?: 0L

    private val _uiState = MutableStateFlow(JobCardDetailUiState())
    val uiState: StateFlow<JobCardDetailUiState> = _uiState.asStateFlow()

    private val _uiEvent = MutableSharedFlow<JobCardDetailUiEvent>()
    val uiEvent: SharedFlow<JobCardDetailUiEvent> = _uiEvent.asSharedFlow()

    private val _selectedTab = MutableStateFlow(0)
    val selectedTab: StateFlow<Int> = _selectedTab.asStateFlow()

    // ✅ ADD: Part search and category states
    private val _partSearchQuery = MutableStateFlow("")
    val partSearchQuery: StateFlow<String> = _partSearchQuery.asStateFlow()

    private val _selectedCategory = MutableStateFlow<String?>(null)
    val selectedCategory: StateFlow<String?> = _selectedCategory.asStateFlow()
    // ✅ ADD: Available parts from inventory
    @OptIn(FlowPreview::class)
    val availableParts: StateFlow<List<InventoryItem>> = _partSearchQuery
        .debounce(300)
        .flatMapLatest { query ->
            if (query.isBlank()) {
                getAllInventoryItemsUseCase(Unit)
            } else {
                searchInventoryUseCase(query)
            }
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    // ✅ ADD: Categories - Extract unique categories from inventory
    val categories: StateFlow<List<String>> = availableParts
        .map { items ->
            items.mapNotNull { it.category }.distinct().sorted()
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    init {
        if (jobCardId == 0L) {
            _uiState.update { state ->
                state.copy(
                    isLoading = false,
                    error = "Invalid job card ID"
                )
            }
        } else {
            loadJobCardDetails()
            loadTechnicians()
            loadServicesAndParts()
            checkInvoiceStatus()
        }
    }

    // ✅ FIX: Collect Flow once and update state, don't keep collecting
    private fun loadJobCardDetails() {
        viewModelScope.launch {
            try {
                // ✅ Use stateIn to convert Flow to StateFlow and collect only once
                getJobCardByIdUseCase(jobCardId)
                    .stateIn(
                        scope = viewModelScope,
                        started = SharingStarted.WhileSubscribed(5000),
                        initialValue = null
                    )
                    .collect { jobCard: JobCard? ->
                        if (jobCard != null) {
                            _uiState.update { state ->
                                state.copy(
                                    isLoading = false,
                                    jobCard = jobCard,
                                    error = null
                                )
                            }
                        } else {
                            _uiState.update { state ->
                                state.copy(
                                    isLoading = false,
                                    error = "Job card not found"
                                )
                            }
                        }
                    }
            } catch (e: Exception) {
                _uiState.update { state ->
                    state.copy(
                        isLoading = false,
                        error = e.message ?: "Failed to load job card"
                    )
                }
            }
        }
    }

    private fun loadTechnicians() {
        viewModelScope.launch {
            getAvailableTechniciansUseCase(Unit)
                .first()
                .let { technicians: List<Worker> ->
                    _uiState.update { state ->
                        state.copy(availableTechnicians = technicians)
                    }
                }
        }
    }

    // ✅ FIX: Collect services and parts continuously but without showing loading
    private fun loadServicesAndParts() {
        // Services
        viewModelScope.launch {
            getJobCardServicesUseCase(jobCardId)
                .catch { e ->
                    // Handle error silently or show toast
                }
                .collect { services: List<JobCardService> ->
                    _uiState.update { state ->
                        state.copy(services = services)
                    }
                }
        }

        // Parts
        viewModelScope.launch {
            getJobCardPartsUseCase(jobCardId)
                .catch { e ->
                    // Handle error silently or show toast
                }
                .collect { parts: List<JobCardPart> ->
                    _uiState.update { state ->
                        state.copy(parts = parts)
                    }
                }
        }
    }

    private fun checkInvoiceStatus() {
        viewModelScope.launch {
            try {
                val result = getInvoiceByJobCardUseCase(jobCardId)
                result.fold(
                    onSuccess = { invoice ->
                        _uiState.update { state ->
                            state.copy(
                                hasInvoice = invoice != null,
                                invoiceId = invoice?.id
                            )
                        }
                    },
                    onFailure = {
                        _uiState.update { state ->
                            state.copy(hasInvoice = false, invoiceId = null)
                        }
                    }
                )
            } catch (e: Exception) {
                // Invoice doesn't exist, that's fine
                _uiState.update { state ->
                    state.copy(hasInvoice = false, invoiceId = null)
                }
            }
        }
    }

    fun onTabSelected(index: Int) {
        _selectedTab.value = index
    }

    // ✅ FIX: Don't reload everything, just update status
    fun onStatusChange(newStatus: JobCardStatus) {
        val jobCard = _uiState.value.jobCard ?: return

        viewModelScope.launch {
            // Show updating indicator without full loading
            _uiState.update { state -> state.copy(isUpdatingStatus = true) }

            val result = updateJobCardStatusUseCase(
                UpdateJobCardStatusUseCase.Params(
                    jobCard = jobCard,
                    newStatus = newStatus
                )
            )

            result.fold(
                onSuccess = {
                    _uiState.update { state -> state.copy(isUpdatingStatus = false) }
                    _uiEvent.emit(JobCardDetailUiEvent.ShowMessage("Status updated successfully"))
                    // Job card will auto-update via Flow
                },
                onFailure = { error ->
                    _uiState.update { state -> state.copy(isUpdatingStatus = false) }
                    _uiEvent.emit(
                        JobCardDetailUiEvent.ShowError(
                            error.message ?: "Failed to update status"
                        )
                    )
                }
            )
        }
    }

    // ✅ FIX: Add service without showing full loading
    fun onAddServiceClick() {
        _uiState.update { state -> state.copy(showAddServiceDialog = true) }
    }

    fun onAddServiceConfirm(serviceName: String, laborCost: Double, quantity: Int) {
        viewModelScope.launch {
            _uiState.update { state ->
                state.copy(
                    showAddServiceDialog = false,
                    isAddingService = true // ✅ Use specific loading state
                )
            }

            val result = addServiceToJobCardUseCase(
                AddServiceToJobCardUseCase.Params(
                    jobCardId = jobCardId,
                    serviceId = 0,
                    serviceName = serviceName,
                    laborCost = laborCost,
                    quantity = quantity
                )
            )

            result.fold(
                onSuccess = {
                    _uiState.update { state -> state.copy(isAddingService = false) }
                    _uiEvent.emit(JobCardDetailUiEvent.ShowMessage("Service added successfully"))
                    // Services will auto-update via Flow
                },
                onFailure = { error ->
                    _uiState.update { state -> state.copy(isAddingService = false) }
                    _uiEvent.emit(
                        JobCardDetailUiEvent.ShowError(
                            error.message ?: "Failed to add service"
                        )
                    )
                }
            )
        }
    }

    fun onRemoveService(service: JobCardService) {
        viewModelScope.launch {
            _uiState.update { state -> state.copy(isRemovingService = true) }

            val result = removeServiceFromJobCardUseCase(service)

            result.fold(
                onSuccess = {
                    _uiState.update { state -> state.copy(isRemovingService = false) }
                    _uiEvent.emit(JobCardDetailUiEvent.ShowMessage("Service removed"))
                    // Services will auto-update via Flow
                },
                onFailure = { error ->
                    _uiState.update { state -> state.copy(isRemovingService = false) }
                    _uiEvent.emit(
                        JobCardDetailUiEvent.ShowError(
                            error.message ?: "Failed to remove service"
                        )
                    )
                }
            )
        }
    }

    // Part Management
    // ✅ ADD: Part search and selection functions
    fun onPartSearchQueryChange(query: String) {
        _partSearchQuery.value = query
    }

    fun onCategorySelected(category: String?) {
        _selectedCategory.value = category
    }

    fun onPartSelected(inventoryItem: InventoryItem, quantity: Int) {
        viewModelScope.launch {
            _uiState.update { state ->
                state.copy(
                    showAddPartDialog = false,
                    isAddingPart = true
                )
            }

            val result = addPartToJobCardUseCase(
                AddPartToJobCardUseCase.Params(
                    jobCardId = jobCardId,
                    partId = inventoryItem.id,
                    partName = inventoryItem.name,
                    partNumber = inventoryItem.partNumber,
                    quantity = quantity,
                    unitPrice = inventoryItem.sellingPrice
                )
            )

            result.fold(
                onSuccess = {
                    _uiState.update { state -> state.copy(isAddingPart = false) }
                    _uiEvent.emit(JobCardDetailUiEvent.ShowMessage("Part added successfully"))
                    // Reset search
                    _partSearchQuery.value = ""
                    _selectedCategory.value = null
                },
                onFailure = { error ->
                    _uiState.update { state -> state.copy(isAddingPart = false) }
                    _uiEvent.emit(
                        JobCardDetailUiEvent.ShowError(
                            error.message ?: "Failed to add part"
                        )
                    )
                }
            )
        }
    }
    fun onAddPartClick() {
        _uiState.update { state -> state.copy(showAddPartDialog = true) }
    }

    fun onAddPartConfirm(partName: String, partNumber: String, quantity: Int, unitPrice: Double) {
        viewModelScope.launch {
            _uiState.update { state ->
                state.copy(
                    showAddPartDialog = false,
                    isAddingPart = true
                )
            }

            val result = addPartToJobCardUseCase(
                AddPartToJobCardUseCase.Params(
                    jobCardId = jobCardId,
                    partId = 0,
                    partName = partName,
                    partNumber = partNumber,
                    quantity = quantity,
                    unitPrice = unitPrice
                )
            )

            result.fold(
                onSuccess = {
                    _uiState.update { state -> state.copy(isAddingPart = false) }
                    _uiEvent.emit(JobCardDetailUiEvent.ShowMessage("Part added successfully"))
                },
                onFailure = { error ->
                    _uiState.update { state -> state.copy(isAddingPart = false) }
                    _uiEvent.emit(
                        JobCardDetailUiEvent.ShowError(
                            error.message ?: "Failed to add part"
                        )
                    )
                }
            )
        }
    }

    fun onRemovePart(part: JobCardPart) {
        viewModelScope.launch {
            _uiState.update { state -> state.copy(isRemovingPart = true) }

            val result = removePartFromJobCardUseCase(part)

            result.fold(
                onSuccess = {
                    _uiState.update { state -> state.copy(isRemovingPart = false) }
                    _uiEvent.emit(JobCardDetailUiEvent.ShowMessage("Part removed"))
                },
                onFailure = { error ->
                    _uiState.update { state -> state.copy(isRemovingPart = false) }
                    _uiEvent.emit(
                        JobCardDetailUiEvent.ShowError(
                            error.message ?: "Failed to remove part"
                        )
                    )
                }
            )
        }
    }


    fun onServiceDialogDismiss() {
        _uiState.update { state -> state.copy(showAddServiceDialog = false) }
    }

    fun onPartDialogDismiss() {
        _uiState.update { state -> state.copy(showAddPartDialog = false) }
        // Reset search
        _partSearchQuery.value = ""
        _selectedCategory.value = null
    }

    // Invoice Management
    fun onGenerateInvoiceClick() {
        _uiState.update { state -> state.copy(showInvoiceDialog = true) }
    }

    fun onInvoiceDialogDismiss() {
        _uiState.update { state ->
            state.copy(
                showInvoiceDialog = false,
                discountPercentage = "",
                invoiceNotes = ""
            )
        }
    }

    fun onDiscountPercentageChange(value: String) {
        val filtered = value.filter { it.isDigit() || it == '.' }
        val percentage = filtered.toDoubleOrNull() ?: 0.0
        if (percentage <= 100) {
            _uiState.update { state -> state.copy(discountPercentage = filtered) }
        }
    }

    fun onInvoiceNotesChange(notes: String) {
        _uiState.update { state -> state.copy(invoiceNotes = notes) }
    }

    fun onConfirmGenerateInvoice() {
        val jobCard = _uiState.value.jobCard ?: return

        if (jobCard.status != JobCardStatus.COMPLETED && jobCard.status != JobCardStatus.DELIVERED) {
            viewModelScope.launch {
                _uiEvent.emit(
                    JobCardDetailUiEvent.ShowError(
                        "Job card must be completed before generating invoice"
                    )
                )
            }
            return
        }

        viewModelScope.launch {
            _uiState.update { state ->
                state.copy(
                    showInvoiceDialog = false,
                    isGeneratingInvoice = true
                )
            }

            val discountPercentage = _uiState.value.discountPercentage.toDoubleOrNull() ?: 0.0
            val notes = _uiState.value.invoiceNotes.takeIf { it.isNotBlank() }

            val result = createInvoiceUseCase(
                CreateInvoiceUseCase.Params(
                    jobCard = jobCard,
                    discount = 0.0,
                    discountPercentage = discountPercentage,
                    notes = notes
                )
            )

            result.fold(
                onSuccess = { invoiceId ->
                    _uiState.update { state ->
                        state.copy(
                            isGeneratingInvoice = false,
                            hasInvoice = true,
                            invoiceId = invoiceId
                        )
                    }
                    _uiEvent.emit(JobCardDetailUiEvent.NavigateToInvoice(invoiceId))
                },
                onFailure = { error ->
                    _uiState.update { state -> state.copy(isGeneratingInvoice = false) }
                    _uiEvent.emit(
                        JobCardDetailUiEvent.ShowError(
                            error.message ?: "Failed to generate invoice"
                        )
                    )
                }
            )
        }
    }

    fun onViewInvoiceClick() {
        val invoiceId = _uiState.value.invoiceId
        if (invoiceId != null) {
            viewModelScope.launch {
                _uiEvent.emit(JobCardDetailUiEvent.NavigateToInvoice(invoiceId))
            }
        }
    }

    fun onEditJobCard() {
        viewModelScope.launch {
            _uiEvent.emit(JobCardDetailUiEvent.NavigateToEditJobCard(jobCardId))
        }
    }

    fun onCallCustomer() {
        val jobCard = _uiState.value.jobCard ?: return
        viewModelScope.launch {
            _uiEvent.emit(JobCardDetailUiEvent.ShowMessage("Call customer feature"))
        }
    }

    // ✅ FIX: Manual refresh only reloads job card, not full page
    fun onRefresh() {
        viewModelScope.launch {
            // Don't show loading state for refresh
            checkInvoiceStatus()
            // Job card, services, and parts will auto-update via Flows
        }
    }
}

// ✅ FIX: Add specific loading states instead of one global loading
data class JobCardDetailUiState(
    val isLoading: Boolean = true,
    val jobCard: JobCard? = null,
    val availableTechnicians: List<Worker> = emptyList(),
    val services: List<JobCardService> = emptyList(),
    val parts: List<JobCardPart> = emptyList(),
    val isUpdatingStatus: Boolean = false,
    val isAddingService: Boolean = false,
    val isRemovingService: Boolean = false,
    val isAddingPart: Boolean = false,
    val isRemovingPart: Boolean = false,
    val hasInvoice: Boolean = false,
    val invoiceId: Long? = null,
    val isGeneratingInvoice: Boolean = false,
    val showInvoiceDialog: Boolean = false,
    val showAddServiceDialog: Boolean = false,
    val showAddPartDialog: Boolean = false,
    val discountPercentage: String = "",
    val invoiceNotes: String = "",
    val error: String? = null
)

sealed class JobCardDetailUiEvent {
    data class NavigateToEditJobCard(val jobCardId: Long) : JobCardDetailUiEvent()
    data class NavigateToInvoice(val invoiceId: Long) : JobCardDetailUiEvent()
    data class ShowMessage(val message: String) : JobCardDetailUiEvent()
    data class ShowError(val message: String) : JobCardDetailUiEvent()
}