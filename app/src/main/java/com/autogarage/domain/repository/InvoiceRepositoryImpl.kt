package com.autogarage.domain.repository

import androidx.room.withTransaction
import com.autogarage.data.local.dao.CustomerDao
import com.autogarage.data.local.dao.InventoryDao
import com.autogarage.data.local.dao.InvoiceDao
import com.autogarage.data.local.dao.JobCardDao
import com.autogarage.data.local.dao.JobCardPartDao
import com.autogarage.data.local.dao.JobCardServiceDao
import com.autogarage.data.local.dao.VehicleDao
import com.autogarage.data.local.dao.WorkerDao
import com.autogarage.data.local.database.GarageMasterDatabase
import com.autogarage.data.mapper.toDomain
import com.autogarage.data.mapper.toEntity
import com.autogarage.domain.model.Invoice
import com.autogarage.domain.model.JobCardPart
import com.autogarage.domain.model.PaymentMode
import com.autogarage.domain.model.PaymentStatus
import com.autogarage.domain.repository.InvoiceRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import java.util.*
import javax.inject.Inject

class InvoiceRepositoryImpl @Inject constructor(
    private val invoiceDao: InvoiceDao,
    private val jobCardDao: JobCardDao,
    private val vehicleDao: VehicleDao,
    private val workerDao: WorkerDao,
    private val customerDao: CustomerDao,
    private val jobCardServiceDao: JobCardServiceDao,  // ADD THIS
    private val jobCardPartDao: JobCardPartDao,
    private val inventoryDao: InventoryDao,             // ADD THIS
    private val database: GarageMasterDatabase
    ) : InvoiceRepository {

    override fun getAllInvoices(): Flow<List<Invoice>> {
        return combine(
            invoiceDao.getAllInvoices(),
            jobCardDao.getAllJobCards(),
            vehicleDao.getAllVehicles(),
            workerDao.getActiveWorkers(),
            customerDao.getAllCustomers()
        ) { invoices, jobCards, vehicles, workers, customers ->
            invoices.map { invoiceEntity ->
                val jobCardEntity = jobCards.find { it.jobCardId == invoiceEntity.jobCardId }
                    ?: throw IllegalStateException("JobCard not found for invoice ${invoiceEntity.invoiceId}")

                val vehicle = vehicles.find { it.vehicleId == jobCardEntity.vehicleId }?.toDomain()
                    ?: throw IllegalStateException("Vehicle not found")

                val technician = jobCardEntity.assignedTechnicianId?.let { techId ->
                    workers.find { it.id == techId }?.toDomain()
                }

                val customer = customers.find { it.customerId == invoiceEntity.customerId }?.toDomain()
                    ?: throw IllegalStateException("Customer not found for invoice ${invoiceEntity.invoiceId}")

                val jobCard = jobCardEntity.toDomain(vehicle = vehicle, technician = technician)
                invoiceEntity.toDomain(jobCard = jobCard, customer = customer)
            }
        }
    }

    override fun getInvoiceById(invoiceId: Long): Flow<Invoice?> {
        return combine(
            invoiceDao.getInvoiceById(invoiceId),
            jobCardDao.getAllJobCards(),
            vehicleDao.getAllVehicles(),
            workerDao.getActiveWorkers(),
            customerDao.getAllCustomers()  // ADD THIS
        ) { invoiceEntity, jobCards, vehicles, workers, customers ->
            invoiceEntity?.let { entity ->
                val jobCardEntity = jobCards.find { it.jobCardId == entity.jobCardId }
                    ?: throw IllegalStateException("JobCard not found for invoice ${entity.invoiceId}")

                val vehicle = vehicles.find { it.vehicleId == jobCardEntity.vehicleId }?.toDomain()
                    ?: throw IllegalStateException("Vehicle not found")

                val technician = jobCardEntity.assignedTechnicianId?.let { techId ->
                    workers.find { it.id == techId }?.toDomain()
                }

                val customer = customers.find { it.customerId == entity.customerId }?.toDomain()
                    ?: throw IllegalStateException("Customer not found for invoice ${entity.invoiceId}")

                // ✅ Load services and parts
                val services = jobCardServiceDao.getServicesByJobCardSync(jobCardEntity.jobCardId)
                    .map { it.toDomain() }

                // ✅ Load parts WITH inventory details
                val partEntities = jobCardPartDao.getPartsByJobCardSync(jobCardEntity.jobCardId)
                val parts = partEntities.map { partEntity ->
                    val inventoryItem = inventoryDao.getInventoryItemByIdSync(partEntity.itemId)

                    JobCardPart(
                        id = partEntity.id,
                        jobCardId = partEntity.jobCardId,
                        partId = partEntity.itemId,
                        partName = partEntity.partName ?: inventoryItem?.name ?: "Unknown Part",
                        partNumber = partEntity.partNumber ?: inventoryItem?.partNumber ?: "N/A",
                        quantity = partEntity.quantity,
                        unitPrice = partEntity.unitPrice,
                        discount = 0.0,
                        totalCost = partEntity.totalPrice
                    )
                }

                val jobCard = jobCardEntity.toDomain(
                    vehicle = vehicle,
                    technician = technician
                ).copy(
                    services = services,
                    parts = parts
                )

                entity.toDomain(jobCard = jobCard, customer = customer)
            }
        }
    }

    override suspend fun getInvoiceByIdSync(invoiceId: Long): Invoice? {
        val invoiceEntity = invoiceDao.getInvoiceByIdSync(invoiceId) ?: return null

        val jobCardEntity = jobCardDao.getJobCardByIdSync(invoiceEntity.jobCardId)
            ?: return null

        val vehicleEntity = vehicleDao.getVehicleByIdSync(jobCardEntity.vehicleId)
            ?: return null

        val customerEntity = customerDao.getCustomerByIdSync(invoiceEntity.customerId)
            ?: return null

        val technician = jobCardEntity.assignedTechnicianId?.let { techId ->
            workerDao.getWorkerByIdSync(techId)?.toDomain()
        }

        // ✅ CRITICAL: Load services and parts
        val services = jobCardServiceDao.getServicesByJobCardSync(jobCardEntity.jobCardId)
            .map { it.toDomain() }

        // ✅ Load parts WITH inventory details
        val partEntities = jobCardPartDao.getPartsByJobCardSync(jobCardEntity.jobCardId)
        val parts = partEntities.map { partEntity ->
            // Fetch inventory item for each part
            val inventoryItem = inventoryDao.getInventoryItemByIdSync(partEntity.itemId)

            JobCardPart(
                id = partEntity.id,
                jobCardId = partEntity.jobCardId,
                partId = partEntity.itemId,
                partName = partEntity.partName ?: inventoryItem?.name ?: "Unknown Part",
                partNumber = partEntity.partNumber ?: inventoryItem?.partNumber ?: "N/A",
                quantity = partEntity.quantity,
                unitPrice = partEntity.unitPrice,
                discount = 0.0,
                totalCost = partEntity.totalPrice
            )
        }

        val vehicle = vehicleEntity.toDomain()
        val customer = customerEntity.toDomain()

        // ✅ Create JobCard with services and parts
        val jobCard = jobCardEntity.toDomain(
            vehicle = vehicle,
            technician = technician
        ).copy(
            services = services,  // Add services
            parts = parts        // Add parts
        )

        return invoiceEntity.toDomain(jobCard = jobCard, customer = customer)
    }

    override suspend fun getInvoiceByJobCard(jobCardId: Long): Invoice? {
        val invoiceEntity = invoiceDao.getInvoiceByJobCard(jobCardId) ?: return null

        val jobCardEntity = jobCardDao.getJobCardByIdSync(invoiceEntity.jobCardId)
            ?: return null

        val vehicleEntity = vehicleDao.getVehicleByIdSync(jobCardEntity.vehicleId)
            ?: return null

        val customerEntity = customerDao.getCustomerByIdSync(invoiceEntity.customerId)
            ?: return null

        val technician = jobCardEntity.assignedTechnicianId?.let { techId ->
            workerDao.getWorkerByIdSync(techId)?.toDomain()
        }

        // ✅ Load services and parts
        val services = jobCardServiceDao.getServicesByJobCardSync(jobCardEntity.jobCardId)
            .map { it.toDomain() }

        // ✅ Load parts WITH inventory details
        val partEntities = jobCardPartDao.getPartsByJobCardSync(jobCardEntity.jobCardId)
        val parts = partEntities.map { partEntity ->
            val inventoryItem = inventoryDao.getInventoryItemByIdSync(partEntity.itemId)

            JobCardPart(
                id = partEntity.id,
                jobCardId = partEntity.jobCardId,
                partId = partEntity.itemId,
                partName = partEntity.partName ?: inventoryItem?.name ?: "Unknown Part",
                partNumber = partEntity.partNumber ?: inventoryItem?.partNumber ?: "N/A",
                quantity = partEntity.quantity,
                unitPrice = partEntity.unitPrice,
                discount = 0.0,
                totalCost = partEntity.totalPrice
            )
        }
        val vehicle = vehicleEntity.toDomain()
        val customer = customerEntity.toDomain()

        // ✅ Create JobCard with services and parts
        val jobCard = jobCardEntity.toDomain(
            vehicle = vehicle,
            technician = technician
        ).copy(
            services = services,
            parts = parts
        )

        return invoiceEntity.toDomain(jobCard = jobCard, customer = customer)
    }


    override fun getInvoicesByPaymentStatus(status: PaymentStatus): Flow<List<Invoice>> {
        return invoiceDao.getInvoicesByPaymentStatus(status.name).map { entities ->
            // Map with JobCard details
            emptyList() // Implement full logic
        }
    }

    override fun getInvoicesBetweenDates(startDate: Long, endDate: Long): Flow<List<Invoice>> {
        return invoiceDao.getInvoicesBetweenDates(startDate, endDate).map { entities ->
            // Map with JobCard details
            emptyList() // Implement full logic
        }
    }

    override suspend fun getTotalRevenue(): Double {
        return invoiceDao.getTotalRevenue() ?: 0.0
    }

    override suspend fun getTotalPending(): Double {
        return invoiceDao.getTotalPending() ?: 0.0
    }

    override suspend fun createInvoice(invoice: Invoice): Long {
        return invoiceDao.insertInvoice(invoice.toEntity())
    }

    override suspend fun updateInvoice(invoice: Invoice) {
        invoiceDao.updateInvoice(invoice.toEntity())
    }

    override suspend fun deleteInvoice(invoice: Invoice) {
        invoiceDao.deleteInvoice(invoice.toEntity())
    }

    override suspend fun generateInvoiceNumber(): String {
        val calendar = Calendar.getInstance()
        val year = calendar.get(Calendar.YEAR)
        val month = calendar.get(Calendar.MONTH) + 1
        val day = calendar.get(Calendar.DAY_OF_MONTH)

        // Format: INV-YYYY-MMDD-XXXX
        // Example: INV-2024-1215-0001
        val datePrefix = String.format("%04d-%02d%02d", year, month, day)

        val startOfDay = calendar.apply {
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }.timeInMillis

        val endOfDay = calendar.apply {
            set(Calendar.HOUR_OF_DAY, 23)
            set(Calendar.MINUTE, 59)
            set(Calendar.SECOND, 59)
            set(Calendar.MILLISECOND, 999)
        }.timeInMillis

        val todayCount = invoiceDao.getInvoiceCountBetween(startOfDay, endOfDay)
        val nextNumber = (todayCount + 1).toString().padStart(4, '0')

        return "INV-$datePrefix-$nextNumber"
    }

    override suspend fun updatePaymentStatus(
        invoiceId: Long,
        status: PaymentStatus,
        paidAmount: Double,
        paymentMode: PaymentMode?,
        transactionId: String?
    ) {
        withContext(Dispatchers.IO) {
            database.withTransaction {
                // 1. Update invoice
                val invoice = invoiceDao.getInvoiceByIdSync(invoiceId)
                    ?: throw IllegalStateException("Invoice not found")

                val pendingAmount = if (status == PaymentStatus.PAID) 0.0 else invoice.totalAmount - paidAmount
                val paymentDate = if (status == PaymentStatus.PAID) System.currentTimeMillis() else null

                val updatedInvoice = invoice.copy(
                    paymentStatus = status.name,
                    paidAmount = paidAmount,
                    pendingAmount = pendingAmount,
                    paymentMode = paymentMode?.name,
                    paymentDate = paymentDate,
                    transactionId = transactionId,
                    updatedAt = System.currentTimeMillis()
                )

                invoiceDao.updateInvoice(updatedInvoice)

                // 2. ✅ UPDATE CUSTOMER'S TOTAL SPENT
                if (status == PaymentStatus.PAID) {
                    val customer = customerDao.getCustomerByIdSync(invoice.customerId)
                    if (customer != null) {
                        val newTotalSpent = customer.totalSpent + paidAmount
                        customerDao.updateTotalSpent(invoice.customerId, newTotalSpent)
                    }
                }
            }
        }
    }

    override suspend fun getInvoicesByDateRange(startDate: Long, endDate: Long): List<Invoice> {
        return withContext(Dispatchers.IO) {
            val invoiceEntities = invoiceDao.getInvoicesByDateRange(startDate, endDate)
            invoiceEntities.mapNotNull { invoiceEntity ->
                // 1. Fetch JobCard
                val jobCardEntity = jobCardDao.getJobCardByIdSync(invoiceEntity.jobCardId)
                    ?: return@mapNotNull null
                val customerEntity = customerDao.getCustomerByIdSync(invoiceEntity.customerId)
                    ?: return@mapNotNull null
                // 2. Fetch Vehicle
                val vehicleEntity = vehicleDao.getVehicleByIdSync(jobCardEntity.vehicleId)
                    ?: return@mapNotNull null
                val technician = jobCardEntity.assignedTechnicianId?.let { techId ->
                    workerDao.getWorkerByIdSync(techId)?.toDomain()
                }
                val vehicle = vehicleEntity.toDomain()
                val customer = customerEntity.toDomain()

                val jobCard = jobCardEntity.toDomain(
                    vehicle = vehicle,
                    technician = technician
                )
                invoiceEntity.toDomain(jobCard = jobCard, customer = customer)
            }
        }
    }
}
