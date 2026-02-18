package com.autogarage.domain.usecase.invoice

import com.autogarage.domain.model.Invoice
import com.autogarage.domain.repository.InvoiceRepository
import com.autogarage.domain.usecase.base.UseCase
import javax.inject.Inject

class GetInvoiceByJobCardUseCase @Inject constructor(
    private val invoiceRepository: InvoiceRepository
) : UseCase<Long, Invoice?>() {

    override suspend fun execute(params: Long): Invoice? {
        return invoiceRepository.getInvoiceByJobCard(params)
    }
}
