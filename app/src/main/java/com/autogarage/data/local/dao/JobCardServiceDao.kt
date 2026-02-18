package com.autogarage.data.local.dao
import androidx.room.*
import com.autogarage.data.local.entity.JobCardServiceEntity
import com.autogarage.data.local.entity.JobCardPartEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface JobCardServiceDao {
    @Query("SELECT * FROM job_card_services WHERE jobCardId = :jobCardId")
    fun getServicesByJobCard(jobCardId: Long): Flow<List<JobCardServiceEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertService(service: JobCardServiceEntity): Long

    @Delete
    suspend fun deleteService(service: JobCardServiceEntity)

    @Query("DELETE FROM job_card_services WHERE jobCardId = :jobCardId")
    suspend fun deleteAllByJobCard(jobCardId: Long)

    @Query("SELECT * FROM job_card_services WHERE jobCardId = :jobCardId")
    suspend fun getServicesByJobCardSync(jobCardId: Long): List<JobCardServiceEntity>
}