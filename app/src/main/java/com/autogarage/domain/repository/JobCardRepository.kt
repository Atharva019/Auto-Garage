package com.autogarage.domain.repository

import com.autogarage.domain.model.JobCard
import com.autogarage.domain.model.JobCardPart
import com.autogarage.domain.model.JobCardService
import com.autogarage.domain.model.JobCardStatus
import kotlinx.coroutines.flow.Flow

interface JobCardRepository {
    suspend fun getJobCardsByDateRange(startDate: Long, endDate: Long): List<JobCard>
    suspend fun getJobCardsByTechnician(
        technicianId: Long,
        startDate: Long,
        endDate: Long
    ): List<JobCard>
    fun getAllJobCards(): Flow<List<JobCard>>

    suspend fun getJobCardsByTechnicianSync(technicianId: Long): List<JobCard>
    fun getJobCardsByStatus(status: JobCardStatus): Flow<List<JobCard>>
    fun getJobCardById(jobCardId: Long): Flow<JobCard?>
    fun getJobCardsByVehicle(vehicleId: Long): Flow<List<JobCard>>
    fun getActiveJobCardsByTechnician(technicianId: Long): Flow<List<JobCard>>
    fun getJobCardCountByStatus(status: JobCardStatus): Flow<Int>

    suspend fun insertJobCard(jobCard: JobCard): Long
    suspend fun updateJobCard(jobCard: JobCard)
    suspend fun deleteJobCard(jobCard: JobCard)
    suspend fun generateJobCardNumber(): String

    // Services & Parts
    suspend fun addServiceToJobCard(service: JobCardService): Long
    suspend fun removeServiceFromJobCard(service: JobCardService)
    fun getServicesByJobCard(jobCardId: Long): Flow<List<JobCardService>>

    suspend fun addPartToJobCard(part: JobCardPart): Long
    suspend fun removePartFromJobCard(part: JobCardPart)
    fun getPartsByJobCard(jobCardId: Long): Flow<List<JobCardPart>>

    suspend fun updateJobCardCosts(jobCardId: Long)
}