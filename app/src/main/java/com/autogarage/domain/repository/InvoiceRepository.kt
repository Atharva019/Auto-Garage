package com.autogarage.domain.repository

import com.autogarage.domain.model.Invoice
import com.autogarage.domain.model.PaymentMode
import com.autogarage.domain.model.PaymentStatus
import kotlinx.coroutines.flow.Flow

interface InvoiceRepository {
    suspend fun getInvoicesByDateRange(startDate: Long, endDate: Long): List<Invoice>
    fun getAllInvoices(): Flow<List<Invoice>>
    fun getInvoiceById(invoiceId: Long): Flow<Invoice?>
    suspend fun getInvoiceByIdSync(invoiceId: Long): Invoice?
    suspend fun getInvoiceByJobCard(jobCardId: Long): Invoice?
    fun getInvoicesByPaymentStatus(status: PaymentStatus): Flow<List<Invoice>>
    fun getInvoicesBetweenDates(startDate: Long, endDate: Long): Flow<List<Invoice>>
    suspend fun getTotalRevenue(): Double
    suspend fun getTotalPending(): Double
    suspend fun createInvoice(invoice: Invoice): Long
    suspend fun updateInvoice(invoice: Invoice)
    suspend fun deleteInvoice(invoice: Invoice)
    suspend fun generateInvoiceNumber(): String
    //suspend fun getInvoicesByDateRange(startDate: Long, endDate: Long): List<Invoice>
    suspend fun updatePaymentStatus(
        invoiceId: Long,
        status: PaymentStatus,
        paidAmount: Double,
        paymentMode: PaymentMode?,
        transactionId: String?
    )
}