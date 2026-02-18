package com.autogarage.domain.usecase.invoice

import com.autogarage.domain.model.PaymentStatus
import com.autogarage.domain.repository.InvoiceRepository
import com.autogarage.domain.usecase.base.UseCase
import javax.inject.Inject

class CancelInvoiceUseCase @Inject constructor(
    private val invoiceRepository: InvoiceRepository
) : UseCase<Long, Unit>() {

    override suspend fun execute(params: Long) {
        val invoice = invoiceRepository.getInvoiceByIdSync(params)
            ?: throw IllegalStateException("Invoice not found")

        require(invoice.paymentStatus == PaymentStatus.UNPAID) {
            "Cannot cancel paid invoice"
        }

        invoiceRepository.updatePaymentStatus(
            invoiceId = params,
            status = PaymentStatus.CANCELLED,
            paidAmount = 0.0,
            paymentMode = null,
            transactionId = null
        )
    }
}