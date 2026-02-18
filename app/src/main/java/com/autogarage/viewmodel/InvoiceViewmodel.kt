package com.autogarage.viewmodel

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.autogarage.domain.model.Invoice
import com.autogarage.domain.model.PaymentMode
import com.autogarage.domain.usecase.invoice.GetInvoiceByIdUseCase
import com.autogarage.domain.usecase.invoice.UpdateInvoicePaymentUseCase
import com.autogarage.domain.usecase.invoice.CancelInvoiceUseCase
import com.autogarage.domain.usecase.invoice.GenerateInvoicePdfUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.io.File
import javax.inject.Inject

@HiltViewModel
class InvoiceViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val getInvoiceByIdUseCase: GetInvoiceByIdUseCase,
    private val updateInvoicePaymentUseCase: UpdateInvoicePaymentUseCase,
    private val cancelInvoiceUseCase: CancelInvoiceUseCase,
    private val generateInvoicePdfUseCase: GenerateInvoicePdfUseCase
) : ViewModel() {

    private val invoiceId: Long = savedStateHandle.get<Long>("invoiceId") ?: 0L

    private val _uiState = MutableStateFlow(InvoiceUiState())
    val uiState: StateFlow<InvoiceUiState> = _uiState.asStateFlow()

    private val _uiEvent = MutableSharedFlow<InvoiceUiEvent>()
    val uiEvent: SharedFlow<InvoiceUiEvent> = _uiEvent.asSharedFlow()

    init {
        loadInvoice()
    }

    private fun loadInvoice() {
        viewModelScope.launch {
            getInvoiceByIdUseCase(invoiceId).collect { invoice ->
                if (invoice != null) {
                    _uiState.update {
                        it.copy(
                            invoice = invoice,
                            isLoading = false,
                            error = null
                        )
                    }
                } else {
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            error = "Invoice not found"
                        )
                    }
                }
            }
        }
    }

    fun onPaymentModeChange(mode: PaymentMode) {
        _uiState.update { it.copy(selectedPaymentMode = mode) }
    }

    fun onTransactionIdChange(transactionId: String) {
        _uiState.update { it.copy(transactionId = transactionId) }
    }

    fun onRecordPaymentClick() {
        _uiState.update { it.copy(showPaymentDialog = true) }
    }

    fun onPaymentDialogDismiss() {
        _uiState.update {
            it.copy(
                showPaymentDialog = false,
                transactionId = ""
            )
        }
    }

    fun onConfirmPayment() {
        val invoice = _uiState.value.invoice ?: return
        val paymentMode = _uiState.value.selectedPaymentMode

        viewModelScope.launch {
            _uiState.update { it.copy(isProcessingPayment = true) }

            val result = updateInvoicePaymentUseCase(
                UpdateInvoicePaymentUseCase.Params(
                    invoiceId = invoice.id,
                    paidAmount = invoice.totalAmount,
                    paymentMode = paymentMode,
                    transactionId = _uiState.value.transactionId.takeIf {
                        it.isNotBlank() && paymentMode == PaymentMode.UPI
                    }
                )
            )

            result.fold(
                onSuccess = {
                    _uiState.update {
                        it.copy(
                            isProcessingPayment = false,
                            showPaymentDialog = false,
                            transactionId = ""
                        )
                    }
                    _uiEvent.emit(InvoiceUiEvent.PaymentRecorded)
                    _uiEvent.emit(InvoiceUiEvent.ShowMessage("Payment recorded successfully"))
                    loadInvoice()
                },
                onFailure = { error ->
                    _uiState.update { it.copy(isProcessingPayment = false) }
                    _uiEvent.emit(
                        InvoiceUiEvent.ShowError(
                            error.message ?: "Failed to record payment"
                        )
                    )
                }
            )
        }
    }

    fun onDownloadPdfClick() {
        viewModelScope.launch {
            _uiState.update { it.copy(isGeneratingPdf = true) }

            val result = generateInvoicePdfUseCase(invoiceId)

            result.fold(
                onSuccess = { file ->
                    _uiState.update { it.copy(isGeneratingPdf = false) }
                    _uiEvent.emit(InvoiceUiEvent.PdfGenerated(file))
                    _uiEvent.emit(InvoiceUiEvent.ShowMessage("PDF saved to ${file.absolutePath}"))
                },
                onFailure = { error ->
                    _uiState.update { it.copy(isGeneratingPdf = false) }
                    _uiEvent.emit(
                        InvoiceUiEvent.ShowError(
                            error.message ?: "Failed to generate PDF"
                        )
                    )
                }
            )
        }
    }


    fun onShareClick() {
        viewModelScope.launch {
            _uiState.update { it.copy(isGeneratingPdf = true) }

            val result = generateInvoicePdfUseCase(invoiceId)

            result.fold(
                onSuccess = { file ->
                    _uiState.update { it.copy(isGeneratingPdf = false) }
                    _uiEvent.emit(InvoiceUiEvent.SharePdf(file))
                },
                onFailure = { error ->
                    _uiState.update { it.copy(isGeneratingPdf = false) }
                    _uiEvent.emit(
                        InvoiceUiEvent.ShowError(
                            error.message ?: "Failed to generate PDF"
                        )
                    )
                }
            )
        }
    }

    fun onPrintClick() {
        viewModelScope.launch {
            _uiState.update { it.copy(isGeneratingPdf = true) }

            val result = generateInvoicePdfUseCase(invoiceId)

            result.fold(
                onSuccess = { file ->
                    _uiState.update { it.copy(isGeneratingPdf = false) }
                    _uiEvent.emit(InvoiceUiEvent.PrintPdf(file))
                },
                onFailure = { error ->
                    _uiState.update { it.copy(isGeneratingPdf = false) }
                    _uiEvent.emit(
                        InvoiceUiEvent.ShowError(
                            error.message ?: "Failed to generate PDF"
                        )
                    )
                }
            )
        }
    }

    fun onCancelInvoiceClick() {
        _uiState.update { it.copy(showCancelDialog = true) }
    }

    fun onCancelDialogDismiss() {
        _uiState.update { it.copy(showCancelDialog = false) }
    }

    fun onConfirmCancel() {
        viewModelScope.launch {
            _uiState.update { it.copy(showCancelDialog = false, isCancelling = true) }

            val result = cancelInvoiceUseCase(invoiceId)

            result.fold(
                onSuccess = {
                    _uiEvent.emit(InvoiceUiEvent.ShowMessage("Invoice cancelled"))
                    loadInvoice()
                },
                onFailure = { error ->
                    _uiState.update { it.copy(isCancelling = false) }
                    _uiEvent.emit(
                        InvoiceUiEvent.ShowError(
                            error.message ?: "Failed to cancel invoice"
                        )
                    )
                }
            )
        }
    }

    fun onNavigateToJobCard() {
        val invoice = _uiState.value.invoice ?: return
        viewModelScope.launch {
            _uiEvent.emit(InvoiceUiEvent.NavigateToJobCard(invoice.jobCard.id))
        }
    }

    fun onRefresh() {
        _uiState.update { it.copy(isLoading = true) }
        loadInvoice()
    }
}

data class InvoiceUiState(
    val invoice: Invoice? = null,
    val isLoading: Boolean = true,
    val isProcessingPayment: Boolean = false,
    val isCancelling: Boolean = false,
    val isGeneratingPdf: Boolean = false,
    val showPaymentDialog: Boolean = false,
    val showCancelDialog: Boolean = false,
    val selectedPaymentMode: PaymentMode = PaymentMode.CASH,
    val transactionId: String = "",
    val error: String? = null
)

sealed class InvoiceUiEvent {
    data class NavigateToJobCard(val jobCardId: Long) : InvoiceUiEvent()
    data object PaymentRecorded : InvoiceUiEvent()
    data class PdfGenerated(val file: File) : InvoiceUiEvent()
    data class SharePdf(val file: File) : InvoiceUiEvent()
    data class PrintPdf(val file: File) : InvoiceUiEvent()
    data class ShowMessage(val message: String) : InvoiceUiEvent()
    data class ShowError(val message: String) : InvoiceUiEvent()
}
