package com.autogarage.domain.usecase.reports

import com.autogarage.domain.model.PaymentMode
import com.autogarage.domain.repository.InvoiceRepository
import com.autogarage.domain.usecase.base.UseCase
import kotlinx.coroutines.Dispatchers
import java.text.SimpleDateFormat
import java.util.*
import javax.inject.Inject

data class RevenueReport(
    val totalRevenue: Double,
    val paidAmount: Double,
    val pendingAmount: Double,
    val totalInvoices: Int,
    val paidInvoices: Int,
    val unpaidInvoices: Int,
    val averageInvoiceValue: Double,
    val dailyRevenue: List<DailyRevenue>,
    val paymentModeBreakdown: Map<PaymentMode, Double>
)

data class DailyRevenue(
    val date: String,
    val revenue: Double,
    val invoiceCount: Int
)

class GetRevenueReportUseCase @Inject constructor(
    private val invoiceRepository: InvoiceRepository
) : UseCase<GetRevenueReportUseCase.Params, RevenueReport>() {

    data class Params(
        val startDate: Long,
        val endDate: Long
    )

    override val dispatcher = Dispatchers.IO

    override suspend fun execute(params: Params): RevenueReport {
        val invoices = invoiceRepository.getInvoicesByDateRange(
            params.startDate,
            params.endDate
        )

        val totalRevenue = invoices.sumOf { it.totalAmount }
        val paidAmount = invoices.filter { it.paymentStatus.toString() == "PAID" }
            .sumOf { it.paidAmount }
        val pendingAmount = invoices.filter { it.paymentStatus.toString() == "UNPAID" }
            .sumOf { it.totalAmount }

        val paidInvoices = invoices.count { it.paymentStatus.toString() == "PAID" }
        val unpaidInvoices = invoices.count { it.paymentStatus.toString() == "UNPAID" }

        val averageInvoiceValue = if (invoices.isNotEmpty()) {
            totalRevenue / invoices.size
        } else 0.0

        // Group by date for daily revenue
        val dailyRevenue = invoices
            .groupBy { formatDate(it.invoiceDate) }
            .map { (date, dayInvoices) ->
                DailyRevenue(
                    date = date,
                    revenue = dayInvoices.sumOf { it.totalAmount },
                    invoiceCount = dayInvoices.size
                )
            }
            .sortedBy { it.date }

        // Payment mode breakdown
        val paymentModeBreakdown = invoices
            .filter { it.paymentStatus.toString() == "PAID" && it.paymentMode != null }
            .groupBy { it.paymentMode!! }
            .mapValues { (_, invoices) -> invoices.sumOf { it.paidAmount } }

        return RevenueReport(
            totalRevenue = totalRevenue,
            paidAmount = paidAmount,
            pendingAmount = pendingAmount,
            totalInvoices = invoices.size,
            paidInvoices = paidInvoices,
            unpaidInvoices = unpaidInvoices,
            averageInvoiceValue = averageInvoiceValue,
            dailyRevenue = dailyRevenue,
            paymentModeBreakdown = paymentModeBreakdown
        )
    }

    private fun formatDate(timestamp: Long): String {
        val format = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        return format.format(Date(timestamp))
    }
}
