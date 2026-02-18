package com.autogarage.domain.usecase.invoice

import com.autogarage.domain.model.Invoice
import com.autogarage.domain.repository.InvoiceRepository
import com.autogarage.domain.usecase.base.FlowUseCase
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class GetAllInvoicesUseCase @Inject constructor(
    private val invoiceRepository: InvoiceRepository
) : FlowUseCase<Unit, List<Invoice>>() {

    override fun execute(params: Unit): Flow<List<Invoice>> {
        return invoiceRepository.getAllInvoices()
    }
}