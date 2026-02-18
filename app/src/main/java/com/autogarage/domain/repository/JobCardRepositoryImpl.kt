package com.autogarage.domain.repository

import androidx.room.withTransaction
import com.autogarage.data.local.dao.*
import com.autogarage.data.local.database.GarageMasterDatabase
import com.autogarage.data.local.entity.JobCardPartEntity
import com.autogarage.data.local.entity.JobCardServiceEntity
import com.autogarage.data.mapper.toDomain
import com.autogarage.data.mapper.toDomainModel
import com.autogarage.data.mapper.toEntity
import com.autogarage.data.mapper.toJobCardDomain
import com.autogarage.domain.model.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext
import java.util.*
import javax.inject.Inject

class JobCardRepositoryImpl @Inject constructor(
    private val jobCardDao: JobCardDao,
    private val vehicleDao: VehicleDao,
    private val workerDao: WorkerDao,
    private val jobCardServiceDao: JobCardServiceDao,      // Add this
    private val jobCardPartDao: JobCardPartDao,            // Add this
    private val inventoryDao: InventoryDao,             // Add this (or whatever your inventory DAO is called)
    private val database: GarageMasterDatabase
    ) : JobCardRepository {

    // ✅ OPTIMIZATION: Prevent duplicate requests
    private val loadingMutex = Mutex()
    private val activeRequests = mutableMapOf<String, Flow<JobCard?>>()
    override suspend fun getJobCardsByDateRange(
        startDate: Long,
        endDate: Long
    ): List<JobCard> {
        return withContext(Dispatchers.IO) {
            val jobCardEntities = jobCardDao.getJobCardsByDateRange(startDate, endDate)
            val vehicleEntities = vehicleDao.getAllVehiclesSync()
            val workerEntities = workerDao.getAllWorkersSync()

            jobCardEntities.map { jobCardEntity ->
                val vehicle = vehicleEntities.find { it.vehicleId == jobCardEntity.vehicleId }
                    ?.toDomain() ?: throw IllegalStateException("Vehicle not found")

                val technician = jobCardEntity.assignedTechnicianId?.let { techId ->
                    workerEntities.find { it.id == techId }?.toDomain()
                }

                jobCardEntity.toDomain(vehicle, technician)
            }
        }
    }

    override suspend fun getJobCardsByTechnician(
        technicianId: Long,
        startDate: Long,
        endDate: Long
    ): List<JobCard> {
        return withContext(Dispatchers.IO) {
            jobCardDao.getJobCardsByTechnicianAndDateRange(
                technicianId = technicianId,
                startDate = startDate,
                endDate = endDate
            ).map { it.toJobCardDomain() } // ✅ Uses JobCardWithDetails.toJobCardDomain()
        }
    }

    override suspend fun getJobCardsByTechnicianSync(technicianId: Long): List<JobCard> {
        return withContext(Dispatchers.IO) {
            jobCardDao.getJobCardsByTechnicianSync(technicianId)
                .map { it.toJobCardDomain() } // ✅ Uses JobCardWithDetails.toJobCardDomain()
        }
    }


    override fun getAllJobCards(): Flow<List<JobCard>> {
        return combine(
            jobCardDao.getAllJobCards(),
            vehicleDao.getAllVehicles(),
            workerDao.getActiveWorkers()
        ) { jobCardEntities, vehicleEntities, workerEntities ->
            jobCardEntities.map { jobCardEntity ->
                val vehicle = vehicleEntities.find { it.vehicleId == jobCardEntity.vehicleId }
                    ?.toDomain()
                    ?: throw IllegalStateException("Vehicle not found")

                val technician = jobCardEntity.assignedTechnicianId?.let { techId ->
                    workerEntities.find { it.id == techId }?.toDomain()
                }

                jobCardEntity.toDomain(
                    vehicle = vehicle,
                    technician = technician
                )
            }
        }
    }

    override fun getJobCardsByStatus(status: JobCardStatus): Flow<List<JobCard>> {
        return jobCardDao.getJobCardsByStatus(status.name)
            .map { jobCardsWithDetails ->
                jobCardsWithDetails.map { it.toDomainModel() }
            }
    }

    override fun getJobCardsByVehicle(vehicleId: Long): Flow<List<JobCard>> {
        return combine(
            jobCardDao.getJobCardsByVehicle(vehicleId),
            vehicleDao.getVehicleById(vehicleId),
            workerDao.getActiveWorkers()
        ) { jobCardEntities, vehicleEntity, workerEntities ->
            val vehicle = vehicleEntity?.toDomain()
                ?: throw IllegalStateException("Vehicle not found: $vehicleId")

            jobCardEntities.map { jobCardEntity ->
                val technician = jobCardEntity.assignedTechnicianId?.let { techId ->
                    workerEntities.find { it.id == techId }?.toDomain()
                }

                jobCardEntity.toDomain(
                    vehicle = vehicle,
                    technician = technician
                )
            }
        }
    }

    override fun getJobCardById(jobCardId: Long): Flow<JobCard?> {
        val key = "jobcard_$jobCardId"

        return flow {
            loadingMutex.withLock {
                val existing = activeRequests[key]
                if (existing != null) {
                    existing.collect { emit(it) }
                } else {
                    val newFlow = jobCardDao.getJobCardById(jobCardId)
                        .map { jobCardWithDetails ->
                            // ✅ NOW WORKS: toDomainModel() is defined
                            jobCardWithDetails?.toDomainModel()
                        }
                        .onCompletion {
                            loadingMutex.withLock {
                                activeRequests.remove(key)
                            }
                        }
                        .shareIn(
                            scope = kotlinx.coroutines.CoroutineScope(Dispatchers.IO),
                            started = SharingStarted.WhileSubscribed(5000),
                            replay = 1
                        )

                    activeRequests[key] = newFlow
                    newFlow.collect { emit(it) }
                }
            }
        }
    }
//        return combine(
//            jobCardDao.getJobCardById(jobCardId),
//            vehicleDao.getAllVehicles(),
//            workerDao.getActiveWorkers()
//        ) { jobCardEntity, vehicleEntities, workerEntities ->
//            jobCardEntity?.let { entity ->
//                val vehicle = vehicleEntities.find { it.vehicleId == entity.vehicleId }
//                    ?.toDomain()
//                    ?: throw IllegalStateException("Vehicle not found for job card ${entity.jobCardId}")
//
//                val technician = entity.assignedTechnicianId?.let { techId ->
//                    workerEntities.find { it.id == techId }?.toDomain()
//                }
//
//                entity.toDomain(
//                    vehicle = vehicle,
//                    technician = technician
//                )
//            }
//        }


    override fun getActiveJobCardsByTechnician(technicianId: Long): Flow<List<JobCard>> {
        return combine(
            jobCardDao.getActiveJobCardsByTechnician(technicianId),
            vehicleDao.getAllVehicles()
        ) { jobCardEntities, vehicleEntities ->
            jobCardEntities.map { jobCardEntity ->
                val vehicle = vehicleEntities.find { it.vehicleId == jobCardEntity.vehicleId }
                    ?.toDomain()
                    ?: throw IllegalStateException("Vehicle not found for job card ${jobCardEntity.jobCardId}")

                jobCardEntity.toDomain(vehicle = vehicle)
            }
        }.flatMapLatest { jobCards ->
            flow {
                val technician = workerDao.getWorkerById(technicianId)?.toDomain()
                    ?: throw IllegalStateException("Technician not found: $technicianId")

                emit(jobCards.map { it.copy() })
            }
        }
    }

    override suspend fun insertJobCard(jobCard: JobCard): Long {
        return jobCardDao.insertJobCard(jobCard.toEntity())
    }

    override suspend fun updateJobCard(jobCard: JobCard) {
        jobCardDao.updateJobCard(jobCard.toEntity())
    }

    override suspend fun deleteJobCard(jobCard: JobCard) {
        jobCardDao.deleteJobCard(jobCard.toEntity())
    }

    override fun getJobCardCountByStatus(status: JobCardStatus): Flow<Int> {
        return jobCardDao.getJobCardCountByStatus(status.name)
    }

    override suspend fun generateJobCardNumber(): String {
        val calendar = Calendar.getInstance()
        val year = calendar.get(Calendar.YEAR)
        val month = calendar.get(Calendar.MONTH) + 1
        val day = calendar.get(Calendar.DAY_OF_MONTH)

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

        val todayCount = jobCardDao.getJobCardCountBetween(startOfDay, endOfDay)
        val nextNumber = (todayCount + 1).toString().padStart(4, '0')

        return "JC-$datePrefix-$nextNumber"
    }

    // ========== NEW: Services Implementation ==========

    override suspend fun addServiceToJobCard(service: JobCardService): Long {
        val serviceId = jobCardServiceDao.insertService(service.toEntity())

        // Update job card costs after adding service
        updateJobCardCosts(service.jobCardId)

        return serviceId
    }

    override suspend fun removeServiceFromJobCard(service: JobCardService) {
        jobCardServiceDao.deleteService(service.toEntity())

        // Update job card costs after removing service
        updateJobCardCosts(service.jobCardId)
    }

    override fun getServicesByJobCard(jobCardId: Long): Flow<List<JobCardService>> {
        return jobCardServiceDao.getServicesByJobCard(jobCardId).map { serviceEntities ->
            serviceEntities.map { it.toDomain() }
        }
    }

    // ========== NEW: Parts Implementation ==========

    override suspend fun addPartToJobCard(part: JobCardPart): Long {
        return withContext(Dispatchers.IO) {
            database.withTransaction {
                // 1. Add part to job card
                val partId = jobCardPartDao.insertPart(part.toEntity())

                // 2. ✅ DEDUCT from inventory stock
                val currentItem = inventoryDao.getItemByIdSync(part.partId)
                if (currentItem != null) {
                    val newStock = currentItem.currentStock - part.quantity

                    if (newStock < 0) {
                        throw IllegalStateException(
                            "Insufficient stock for ${currentItem.name}. " +
                                    "Available: ${currentItem.currentStock}, Required: ${part.quantity}"
                        )
                    }

                    // Update inventory stock
                    inventoryDao.updateStock(part.partId, newStock)
                }

                // 3. Update job card costs
                updateJobCardCosts(part.jobCardId)

                partId
            }
        }
    }

    override suspend fun removePartFromJobCard(part: JobCardPart) {
        withContext(Dispatchers.IO) {
            database.withTransaction {
                // 1. Remove part from job card
                jobCardPartDao.deletePart(part.toEntity())

                // 2. ✅ RESTORE inventory stock
                val currentItem = inventoryDao.getItemByIdSync(part.partId)
                if (currentItem != null) {
                    val newStock = currentItem.currentStock + part.quantity
                    inventoryDao.updateStock(part.partId, newStock)
                }

                // 3. Update job card costs
                updateJobCardCosts(part.jobCardId)
            }
        }
    }

    override fun getPartsByJobCard(jobCardId: Long): Flow<List<JobCardPart>> {
        return combine(
            jobCardPartDao.getPartsByJobCard(jobCardId),
            inventoryDao.getAllItems()
        ) { partEntities, inventoryItems ->
            partEntities.map { partEntity ->
                // Find the inventory item to get name and part number
                val inventoryItem = inventoryItems.find { it.itemId == partEntity.itemId }

                // Map entity to domain with inventory details
                JobCardPart(
                    id = partEntity.id,
                    jobCardId = partEntity.jobCardId,
                    partId = partEntity.itemId,
                    partName = inventoryItem?.name ?: "Unknown Part",
                    partNumber = inventoryItem?.partNumber ?: "N/A",
                    quantity = partEntity.quantity,
                    unitPrice = partEntity.unitPrice,
                    discount = 0.0, // Not stored in entity
                    totalCost = partEntity.totalPrice
                )
            }
        }
    }

    // ========== NEW: Update Job Card Costs ==========

    override suspend fun updateJobCardCosts(jobCardId: Long) {
        // Get current job card
        val jobCard = jobCardDao.getJobCardByIdSync(jobCardId) ?: return

        // Calculate total labor cost from services
        val services = jobCardServiceDao.getServicesByJobCardSync(jobCardId)
        val laborCost = services.sumOf { it.totalCost }

        // Calculate total parts cost
        val parts = jobCardPartDao.getPartsByJobCardSync(jobCardId)
        val partsCost = parts.sumOf { it.totalPrice }

        // Calculate totals
        val totalCost = laborCost + partsCost
        val finalAmount = totalCost - jobCard.discount

        // Update job card with new costs
        val updatedJobCard = jobCard.copy(
            laborCost = laborCost,
            partsCost = partsCost,
            totalCost = totalCost,
            finalAmount = finalAmount
        )

        jobCardDao.updateJobCard(updatedJobCard)
    }
}

// ===========================================================================
// Add These Methods to Your DAOs
// ===========================================================================

/**
 * Add to JobCardDao.kt:
 *
 * @Query("SELECT * FROM job_cards WHERE jobCardId = :jobCardId")
 * suspend fun getJobCardByIdSync(jobCardId: Long): JobCardEntity?
 */

/**
 * Add to JobCardServiceDao.kt:
 *
 * @Query("SELECT * FROM job_card_services WHERE jobCardId = :jobCardId")
 * suspend fun getServicesByJobCardSync(jobCardId: Long): List<JobCardServiceEntity>
 */

/**
 * Add to JobCardPartDao.kt:
 *
 * @Query("SELECT * FROM job_card_parts WHERE jobCardId = :jobCardId")
 * suspend fun getPartsByJobCardSync(jobCardId: Long): List<JobCardPartEntity>
 */

/**
 * Add to InventoryItemDao.kt (if not already present):
 *
 * @Query("SELECT * FROM inventory_items")
 * fun getAllItems(): Flow<List<InventoryItemEntity>>
 */

// ===========================================================================
// Mapper Extensions (add to your mappers file)
// ===========================================================================

/**
 * Add to data/mapper/Mappers.kt or create ServiceMappers.kt:
 */

// Service Entity -> Domain
fun JobCardServiceEntity.toDomain(): JobCardService {
    return JobCardService(
        id = this.id,
        jobCardId = this.jobCardId,
        serviceId = this.serviceId,
        serviceName = this.serviceName,
        description = this.description,
        laborCost = this.laborCost,
        quantity = this.quantity,
        totalCost = this.totalCost
    )
}

// Service Domain -> Entity
fun JobCardService.toEntity(): JobCardServiceEntity {
    return JobCardServiceEntity(
        id = this.id,
        jobCardId = this.jobCardId,
        serviceId = this.serviceId,
        serviceName = this.serviceName,
        description = this.description,
        laborCost = this.laborCost,
        quantity = this.quantity,
        totalCost = this.totalCost
    )
}

// Part Domain -> Entity
fun JobCardPart.toEntity(): JobCardPartEntity {
    return JobCardPartEntity(
        id = this.id,
        jobCardId = this.jobCardId,
        itemId = this.partId,
        quantity = this.quantity,
        unitPrice = this.unitPrice,
        totalPrice = this.totalCost,
        notes = null
    )
}