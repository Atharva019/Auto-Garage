package com.autogarage.domain.usecase.invoice

import com.autogarage.domain.model.PaymentMode
import com.autogarage.domain.model.PaymentStatus
import com.autogarage.domain.repository.InvoiceRepository
import com.autogarage.domain.usecase.base.UseCase
import javax.inject.Inject

class UpdateInvoicePaymentUseCase @Inject constructor(
    private val invoiceRepository: InvoiceRepository
) : UseCase<UpdateInvoicePaymentUseCase.Params, Unit>() {

    data class Params(
        val invoiceId: Long,
        val paidAmount: Double,
        val paymentMode: PaymentMode,
        val transactionId: String? = null
    )

    override suspend fun execute(params: Params) {
        // Validation
        require(params.paidAmount > 0) {
            "Payment amount must be greater than zero"
        }

        // Get invoice to validate
        val invoice = invoiceRepository.getInvoiceByIdSync(params.invoiceId)
            ?: throw IllegalStateException("Invoice not found")

        require(params.paidAmount <= invoice.totalAmount) {
            "Payment amount cannot exceed total amount"
        }

        require(invoice.paymentStatus != PaymentStatus.PAID) {
            "Invoice is already paid"
        }

        require(invoice.paymentStatus != PaymentStatus.CANCELLED) {
            "Cannot update payment for cancelled invoice"
        }

        // Determine new payment status
        val newStatus = if (params.paidAmount >= invoice.totalAmount) {
            PaymentStatus.PAID
        } else {
            // For single payment only, this shouldn't happen
            // But keeping for safety
            PaymentStatus.UNPAID
        }

        // Update payment status
        invoiceRepository.updatePaymentStatus(
            invoiceId = params.invoiceId,
            status = newStatus,
            paidAmount = params.paidAmount,
            paymentMode = params.paymentMode,
            transactionId = params.transactionId
        )
    }
}
