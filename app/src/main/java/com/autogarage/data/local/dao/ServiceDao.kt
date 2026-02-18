package com.autogarage.data.local.dao

import androidx.room.*
import com.autogarage.data.local.entity.ServiceEntity
import kotlinx.coroutines.flow.Flow
@Dao
interface ServiceDao {
    @Query("SELECT * FROM services WHERE isActive = 1 ORDER BY name ASC")
    fun getAllActiveServices(): Flow<List<ServiceEntity>>

    @Query("SELECT * FROM services WHERE serviceId = :serviceId")
    fun getServiceById(serviceId: Long): Flow<ServiceEntity?>

    @Query("SELECT * FROM services WHERE category = :category AND isActive = 1")
    fun getServicesByCategory(category: String): Flow<List<ServiceEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertService(service: ServiceEntity): Long

    @Update
    suspend fun updateService(service: ServiceEntity)

    @Delete
    suspend fun deleteService(service: ServiceEntity)

    @Query("SELECT DISTINCT category FROM services WHERE isActive = 1 ORDER BY category ASC")
    fun getAllCategories(): Flow<List<String>>
}
