package com.autogarage.data.local.dao

import androidx.room.*
import com.autogarage.data.local.entity.JobCardEntity
import com.autogarage.data.local.entity.JobCardPartEntity
import com.autogarage.data.local.entity.JobCardServiceEntity
import com.autogarage.data.local.entity.VehicleEntity
import com.autogarage.data.local.entity.WorkerEntity
import com.autogarage.domain.model.JobCard
import kotlinx.coroutines.flow.Flow

@Dao
interface JobCardDao {
    @Transaction
    @Query("""
    SELECT * FROM job_cards 
    WHERE createdAt >= :startDate AND createdAt <= :endDate
    ORDER BY createdAt DESC
    """)
    suspend fun getJobCardsByDateRange(startDate: Long, endDate: Long): List<JobCardEntity>
    @Transaction
    @Query("""
        SELECT * FROM job_cards 
        WHERE assignedTechnicianId = :technicianId 
        AND createdAt >= :startDate 
        AND createdAt <= :endDate
        ORDER BY createdAt DESC
    """)
    suspend fun getJobCardsByTechnicianAndDateRange(
        technicianId: Long,
        startDate: Long,
        endDate: Long
    ): List<JobCardWithDetails>

    @Transaction
    @Query("""
        SELECT * FROM job_cards 
        WHERE assignedTechnicianId = :technicianId
        ORDER BY createdAt DESC
    """)
    suspend fun getJobCardsByTechnicianSync(technicianId: Long): List<JobCardWithDetails>

    @Transaction
    @Query("SELECT * FROM job_cards ORDER BY createdAt DESC")
    fun getAllJobCards(): Flow<List<JobCardEntity>>

    // ✅ OPTIMIZATION: Use @Transaction for complex queries
    @Transaction
    @Query("SELECT * FROM job_cards WHERE status = :status ORDER BY createdAt DESC")
    fun getJobCardsByStatus(status: String): Flow<List<JobCardWithDetails>>

    // ✅ OPTIMIZATION: Use @Transaction for related data
    @Transaction
    @Query("SELECT * FROM job_cards WHERE jobCardId = :id")
    suspend fun getJobCardWithDetails(id: Long): JobCardWithDetails?

    @Query("SELECT * FROM job_cards WHERE vehicleId = :vehicleId ORDER BY createdAt DESC")
    fun getJobCardsByVehicle(vehicleId: Long): Flow<List<JobCardEntity>>
    @Transaction
    @Query("SELECT * FROM job_cards WHERE jobCardId = :jobCardId")
    fun getJobCardById(jobCardId: Long): Flow<JobCardWithDetails?>

    @Query("SELECT * FROM job_cards WHERE assignedTechnicianId = :technicianId AND status != 'COMPLETED' AND status != 'CANCELLED'")
    fun getActiveJobCardsByTechnician(technicianId: Long): Flow<List<JobCardEntity>>

    // ✅ ADD: Count job cards by customer (through vehicle)
    @Query("""
        SELECT COUNT(*) FROM job_cards j
        INNER JOIN vehicles v ON j.vehicleId = v.vehicleId
        WHERE v.customerId = :customerId
    """)
    suspend fun getJobCardCountByCustomer(customerId: Long): Int

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertJobCard(jobCard: JobCardEntity): Long

    @Update
    suspend fun updateJobCard(jobCard: JobCardEntity)

    @Delete
    suspend fun deleteJobCard(jobCard: JobCardEntity)

    @Query("SELECT COUNT(*) FROM job_cards WHERE status = :status")
    fun getJobCardCountByStatus(status: String): Flow<Int>

    @Query("SELECT COUNT(*) FROM job_cards WHERE createdAt >= :startTime AND createdAt <= :endTime")
    suspend fun getJobCardCountBetween(startTime: Long, endTime: Long): Int

    @Query("SELECT * FROM job_cards WHERE jobCardId = :jobCardId")
    suspend fun getJobCardByIdSync(jobCardId: Long): JobCardEntity?

    @Query("DELETE FROM job_cards WHERE createdAt < :timestamp AND status IN ('COMPLETED', 'DELIVERED', 'CANCELLED')")
    suspend fun deleteOldJobCards(timestamp: Long): Int

}

data class JobCardWithDetails(
    @Embedded val jobCard: JobCardEntity,

    @Relation(
        parentColumn = "vehicleId",
        entityColumn = "vehicleId"
    )
    val vehicle: VehicleEntity,

    @Relation(
        parentColumn = "assignedTechnicianId",
        entityColumn = "id"
    )
    val technician: WorkerEntity?,

    @Relation(
        parentColumn = "jobCardId",
        entityColumn = "jobCardId",
        entity = JobCardServiceEntity::class
    )
    val services: List<JobCardServiceEntity>,

    @Relation(
        parentColumn = "jobCardId",
        entityColumn = "jobCardId",
        entity = JobCardPartEntity::class
    )
    val parts: List<JobCardPartEntity>
)