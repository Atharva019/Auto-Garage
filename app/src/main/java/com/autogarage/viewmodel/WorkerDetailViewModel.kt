package com.autogarage.viewmodel

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.autogarage.domain.model.Worker
import com.autogarage.domain.model.WorkerStatus
import com.autogarage.domain.usecase.worker.GetWorkerByIdUseCase
import com.autogarage.domain.usecase.worker.UpdateWorkerStatusUseCase
import com.autogarage.domain.usecase.worker.DeleteWorkerUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class WorkerDetailViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val getWorkerByIdUseCase: GetWorkerByIdUseCase,
    private val updateWorkerStatusUseCase: UpdateWorkerStatusUseCase,
    private val deleteWorkerUseCase: DeleteWorkerUseCase
) : ViewModel() {

    private val workerId: Long = savedStateHandle.get<Long>("workerId")?: 0L

    private val _uiState = MutableStateFlow(WorkerDetailUiState())
    val uiState: StateFlow<WorkerDetailUiState> = _uiState.asStateFlow()

    private val _uiEvent = MutableSharedFlow<WorkerDetailUiEvent>()
    val uiEvent: SharedFlow<WorkerDetailUiEvent> = _uiEvent.asSharedFlow()

    init {
        println("✅ WorkerId received: $workerId")
        if (workerId == 0L) {
            println("⚠️ Invalid worker ID - navigation argument may be missing")
        }
        loadWorkerDetails()
    }

    private fun loadWorkerDetails() {
        viewModelScope.launch {
            _uiState.update { state -> state.copy(isLoading = true) }

            try {
                // ✅ FIX: Explicitly specify types
                getWorkerByIdUseCase(workerId).collect { worker ->
                        _uiState.update { state ->
                            state.copy(
                                isLoading = false,
                                worker = worker,
                                error = null
                            )
                        }
                    }
            } catch (e: Exception) {
                _uiState.update { state ->
                    state.copy(
                        isLoading = false,
                        error = "Worker not found: ${e.message}"
                    )
                }
            }
        }
    }

    fun onStatusChange(newStatus: WorkerStatus) {
        viewModelScope.launch {
            _uiState.update { state -> state.copy(isUpdatingStatus = true) }

            // ✅ FIX: Use correct parameter structure
            val result = updateWorkerStatusUseCase(
                params = UpdateWorkerStatusUseCase.Params(
                    workerId = workerId,
                    status = newStatus
                )
            )

            result.fold(
                onSuccess = {
                    _uiState.update { state -> state.copy(isUpdatingStatus = false) }
                    _uiEvent.emit(WorkerDetailUiEvent.ShowMessage("Status updated to ${newStatus.name}"))
                    loadWorkerDetails() // Refresh data
                },
                onFailure = { error ->
                    _uiState.update { state -> state.copy(isUpdatingStatus = false) }
                    _uiEvent.emit(
                        WorkerDetailUiEvent.ShowError(
                            error.message ?: "Failed to update status"
                        )
                    )
                }
            )
        }
    }

    fun onEditWorkerClick() {
        viewModelScope.launch {
            _uiEvent.emit(WorkerDetailUiEvent.NavigateToEditWorker(workerId))
        }
    }

    fun onDeleteWorkerClick() {
        _uiState.update { state -> state.copy(showDeleteDialog = true) }
    }

    fun onDeleteDialogDismiss() {
        _uiState.update { state -> state.copy(showDeleteDialog = false) }
    }

    fun onConfirmDelete() {
        val worker = _uiState.value.worker ?: return

        viewModelScope.launch {
            _uiState.update { state ->
                state.copy(
                    showDeleteDialog = false,
                    isDeleting = true
                )
            }

            val result = deleteWorkerUseCase(worker)

            result.fold(
                onSuccess = {
                    _uiEvent.emit(WorkerDetailUiEvent.WorkerDeleted)
                },
                onFailure = { error ->
                    _uiState.update { state -> state.copy(isDeleting = false) }
                    _uiEvent.emit(
                        WorkerDetailUiEvent.ShowError(
                            error.message ?: "Failed to delete worker"
                        )
                    )
                }
            )
        }
    }

    fun onRefresh() {
        loadWorkerDetails()
    }
}

data class WorkerDetailUiState(
    val isLoading: Boolean = true,
    val worker: Worker? = null,
    val isUpdatingStatus: Boolean = false,
    val showDeleteDialog: Boolean = false,
    val isDeleting: Boolean = false,
    val error: String? = null
)

sealed class WorkerDetailUiEvent {
    data class NavigateToEditWorker(val workerId: Long) : WorkerDetailUiEvent()
    data object WorkerDeleted : WorkerDetailUiEvent()
    data class ShowMessage(val message: String) : WorkerDetailUiEvent()
    data class ShowError(val message: String) : WorkerDetailUiEvent()
}