package com.autogarage.domain.usecase.invoice

import com.autogarage.domain.model.Invoice
import com.autogarage.domain.repository.InvoiceRepository
import com.autogarage.domain.usecase.base.FlowUseCase
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class GetInvoiceByIdUseCase @Inject constructor(
    private val invoiceRepository: InvoiceRepository
) : FlowUseCase<Long, Invoice?>() {

    override fun execute(params: Long): Flow<Invoice?> {
        return invoiceRepository.getInvoiceById(params)
    }
}