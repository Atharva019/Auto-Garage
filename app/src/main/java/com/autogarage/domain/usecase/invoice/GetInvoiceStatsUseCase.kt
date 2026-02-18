package com.autogarage.domain.usecase.invoice

import com.autogarage.domain.repository.InvoiceRepository
import com.autogarage.domain.usecase.base.UseCase
import javax.inject.Inject

data class InvoiceStats(
    val totalRevenue: Double,
    val totalPending: Double,
    val paidInvoicesCount: Int,
    val unpaidInvoicesCount: Int
)

class GetInvoiceStatsUseCase @Inject constructor(
    private val invoiceRepository: InvoiceRepository
) : UseCase<Unit, InvoiceStats>() {

    override suspend fun execute(params: Unit): InvoiceStats {
        val totalRevenue = invoiceRepository.getTotalRevenue()
        val totalPending = invoiceRepository.getTotalPending()

        // Note: You'd need to add count methods in repository
        // For now, returning basic stats
        return InvoiceStats(
            totalRevenue = totalRevenue,
            totalPending = totalPending,
            paidInvoicesCount = 0, // Implement in repository
            unpaidInvoicesCount = 0  // Implement in repository
        )
    }
}