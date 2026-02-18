package com.autogarage.data.local.dao

import androidx.room.*
import com.autogarage.data.local.entity.JobCardServiceEntity
import com.autogarage.data.local.entity.JobCardPartEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface JobCardPartDao {
    @Query("""
        SELECT 
            jp.itemId as partId,
            jp.partName,
            COUNT(*) as usageCount,
            SUM(jp.quantity * jp.unitPrice) as totalValue
        FROM job_card_parts jp
        INNER JOIN job_cards jc ON jp.jobCardId = jc.jobCardId
        WHERE jc.createdAt >= :startDate AND jc.createdAt <= :endDate
        GROUP BY jp.itemId, jp.partName
        ORDER BY usageCount DESC
        LIMIT :limit
    """)
    suspend fun getTopUsedParts(startDate: Long, endDate: Long, limit: Int): List<PartUsageDto>
    @Query("SELECT * FROM job_card_parts WHERE jobCardId = :jobCardId")
    fun getPartsByJobCard(jobCardId: Long): Flow<List<JobCardPartEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPart(part: JobCardPartEntity): Long

    @Delete
    suspend fun deletePart(part: JobCardPartEntity)

    @Query("DELETE FROM job_card_parts WHERE jobCardId = :jobCardId")
    suspend fun deleteAllByJobCard(jobCardId: Long)

    @Query("SELECT * FROM job_card_parts WHERE jobCardId = :jobCardId")
    suspend fun getPartsByJobCardSync(jobCardId: Long): List<JobCardPartEntity>
}

data class PartUsageDto(
    val partId: Long,
    val partName: String,
    val usageCount: Int,
    val totalValue: Double
)