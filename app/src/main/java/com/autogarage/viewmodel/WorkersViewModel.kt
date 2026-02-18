package com.autogarage.viewmodel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.autogarage.domain.model.Worker
import com.autogarage.domain.model.WorkerRole
import com.autogarage.domain.usecase.worker.*
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class WorkersViewModel @Inject constructor(
    private val getAllWorkersUseCase: GetAllWorkersUseCase,
    private val searchWorkersUseCase: SearchWorkersUseCase,
    private val deleteWorkerUseCase: DeleteWorkerUseCase
) : ViewModel() {

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    private val _filterRole = MutableStateFlow<WorkerRole?>(null)
    val filterRole: StateFlow<WorkerRole?> = _filterRole.asStateFlow()

    private val _showActiveOnly = MutableStateFlow(true)
    val showActiveOnly: StateFlow<Boolean> = _showActiveOnly.asStateFlow()

    private val _uiState = MutableStateFlow(WorkersUiState())
    val uiState: StateFlow<WorkersUiState> = _uiState.asStateFlow()

    private val _uiEvent = MutableSharedFlow<WorkerUiEvent>()
    val uiEvent: SharedFlow<WorkerUiEvent> = _uiEvent.asSharedFlow()

    init {
        loadWorkers()
    }

    @OptIn(FlowPreview::class)
    private fun loadWorkers() {
        viewModelScope.launch {
            combine(
                _searchQuery.debounce(300),
                _showActiveOnly,
                _filterRole
            ) { query, activeOnly, role ->
                Triple(query, activeOnly, role)
            }.flatMapLatest { (query, activeOnly, role) ->
                if (query.isBlank()) {
                    getAllWorkersUseCase(activeOnly)
                } else {
                    searchWorkersUseCase(query)
                }.map { workers ->
                    if (role != null) {
                        workers.filter { it.role == role }
                    } else {
                        workers
                    }
                }
            }.catch { error ->
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        error = error.message ?: "Unknown error"
                    )
                }
            }.collect { workers ->
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        workers = workers,
                        error = null
                    )
                }
            }
        }
    }

    fun onSearchQueryChange(query: String) {
        _searchQuery.value = query
    }

    fun onFilterRoleChange(role: WorkerRole?) {
        _filterRole.value = role
    }

    fun onShowActiveOnlyChange(showActiveOnly: Boolean) {
        _showActiveOnly.value = showActiveOnly
    }

    fun onWorkerClick(workerId: Long) {
        viewModelScope.launch {
            _uiEvent.emit(WorkerUiEvent.NavigateToWorkerDetail(workerId))
        }
    }

    fun onAddWorkerClick() {
        viewModelScope.launch {
            _uiEvent.emit(WorkerUiEvent.NavigateToAddWorker)
        }
    }

    fun onDeleteWorker(worker: Worker) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            deleteWorkerUseCase(worker).fold(
                onSuccess = {
                    _uiEvent.emit(WorkerUiEvent.ShowMessage("Worker deleted successfully"))
                },
                onFailure = { error ->
                    _uiEvent.emit(
                        WorkerUiEvent.ShowMessage(
                            error.message ?: "Failed to delete worker"
                        )
                    )
                }
            )
            _uiState.update { it.copy(isLoading = false) }
        }
    }

    fun onRefresh() {
        _searchQuery.value = ""
        _filterRole.value = null
        loadWorkers()
    }
}

data class WorkersUiState(
    val isLoading: Boolean = true,
    val workers: List<Worker> = emptyList(),
    val error: String? = null
)

sealed class WorkerUiEvent {
    data class NavigateToWorkerDetail(val workerId: Long) : WorkerUiEvent()
    data object NavigateToAddWorker : WorkerUiEvent()
    data class ShowMessage(val message: String) : WorkerUiEvent()
}

