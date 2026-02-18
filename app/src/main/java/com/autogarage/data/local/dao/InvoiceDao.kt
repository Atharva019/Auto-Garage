package com.autogarage.data.local.dao

import androidx.room.*
import com.autogarage.data.local.entity.InvoiceEntity
import com.autogarage.domain.usecase.reports.PartUsage
import kotlinx.coroutines.flow.Flow

@Dao
interface InvoiceDao {

    @Query("SELECT * FROM invoices ORDER BY invoiceDate DESC")
    fun getAllInvoices(): Flow<List<InvoiceEntity>>

    @Query("SELECT * FROM invoices WHERE invoiceId = :invoiceId")
    fun getInvoiceById(invoiceId: Long): Flow<InvoiceEntity?>

    @Query("SELECT * FROM invoices WHERE invoiceId = :invoiceId")
    suspend fun getInvoiceByIdSync(invoiceId: Long): InvoiceEntity?

    @Query("SELECT * FROM invoices WHERE jobCardId = :jobCardId")
    suspend fun getInvoiceByJobCard(jobCardId: Long): InvoiceEntity?

    @Query("SELECT * FROM invoices WHERE paymentStatus = :status ORDER BY invoiceDate DESC")
    fun getInvoicesByPaymentStatus(status: String): Flow<List<InvoiceEntity>>

    @Query("SELECT * FROM invoices WHERE invoiceDate >= :startDate AND invoiceDate <= :endDate")
    fun getInvoicesBetweenDates(startDate: Long, endDate: Long): Flow<List<InvoiceEntity>>

    @Query("SELECT SUM(totalAmount) FROM invoices WHERE paymentStatus = 'PAID'")
    suspend fun getTotalRevenue(): Double?

    @Query("SELECT SUM(pendingAmount) FROM invoices WHERE paymentStatus = 'UNPAID'")
    suspend fun getTotalPending(): Double?

    @Query("SELECT COUNT(*) FROM invoices WHERE invoiceDate >= :startTime AND invoiceDate <= :endTime")
    suspend fun getInvoiceCountBetween(startTime: Long, endTime: Long): Int

    @Query("""
        SELECT * FROM invoices 
        WHERE customerId = :customerId 
        AND paymentStatus = 'PAID'
    """)
    suspend fun getPaidInvoicesByCustomer(customerId: Long): List<InvoiceEntity>

    // ✅ ADD: Get total paid amount for a vehicle
    @Query("""
        SELECT COALESCE(SUM(paidAmount), 0.0)
        FROM invoices i
        INNER JOIN job_cards j ON i.jobCardId = j.jobCardId
        WHERE j.vehicleId = :vehicleId 
        AND i.paymentStatus = 'PAID'
    """)
    suspend fun getTotalPaidByVehicle(vehicleId: Long): Double

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertInvoice(invoice: InvoiceEntity): Long

    @Update
    suspend fun updateInvoice(invoice: InvoiceEntity)

    @Delete
    suspend fun deleteInvoice(invoice: InvoiceEntity)

    @Query("""
        SELECT * FROM invoices 
        WHERE invoiceDate >= :startDate AND invoiceDate <= :endDate
        ORDER BY invoiceDate DESC
    """)
    suspend fun getInvoicesByDateRange(startDate: Long, endDate: Long): List<InvoiceEntity>

    @Query("""
        SELECT 
            jcp.itemId as partId,
            ii.name as partName,
            COUNT(*) as usageCount,
            SUM(jcp.quantity * jcp.unitPrice) as totalValue
        FROM job_card_parts jcp
        INNER JOIN job_cards jc ON jcp.jobCardId = jc.jobCardId
        INNER JOIN inventory_items ii ON jcp.itemId = ii.itemId
        WHERE jc.createdAt >= :startDate AND jc.createdAt <= :endDate
        GROUP BY jcp.itemId, ii.name
        ORDER BY usageCount DESC
        LIMIT :limit
    """)
    suspend fun getTopUsedParts(
        startDate: Long,
        endDate: Long,
        limit: Int
    ): List<PartUsage>

    @Query("UPDATE invoices SET paymentStatus = :status, paidAmount = :paidAmount, pendingAmount = :pendingAmount, paymentMode = :paymentMode, paymentDate = :paymentDate, transactionId = :transactionId WHERE invoiceId = :invoiceId")
    suspend fun updatePaymentStatus(
        invoiceId: Long,
        status: String,
        paidAmount: Double,
        pendingAmount: Double,
        paymentMode: String?,
        paymentDate: Long?,
        transactionId: String?
    )
}