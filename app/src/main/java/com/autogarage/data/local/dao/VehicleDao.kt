package com.autogarage.data.local.dao

import androidx.room.*
import com.autogarage.data.local.entity.VehicleEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface VehicleDao {
    @Query("SELECT * FROM vehicles WHERE customerId = :customerId")
    fun getVehiclesByCustomer(customerId: Long): Flow<List<VehicleEntity>>

    @Query("SELECT * FROM vehicles WHERE customerId = :customerId")
    fun getVehiclesByCustomerSync(customerId: Long): List<VehicleEntity>

    @Query("SELECT * FROM vehicles")
    fun getAllVehicles(): Flow<List<VehicleEntity>>

    @Query("SELECT * FROM vehicles")
    suspend fun getAllVehiclesSync(): List<VehicleEntity>


    @Query("SELECT * FROM vehicles WHERE vehicleId = :vehicleId")
    fun getVehicleById(vehicleId: Long): Flow<VehicleEntity?>

    @Query("SELECT * FROM vehicles WHERE registrationNumber = :regNumber")
    suspend fun getVehicleByRegNumber(regNumber: String): VehicleEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertVehicle(vehicle: VehicleEntity): Long

    @Update
    suspend fun updateVehicle(vehicle: VehicleEntity)

    @Delete
    suspend fun deleteVehicle(vehicle: VehicleEntity)

    @Query("SELECT * FROM vehicles WHERE vehicleId = :id")
    suspend fun getVehicleByIdSync(id: Long): VehicleEntity?

    @Query("SELECT * FROM vehicles ORDER BY createdAt DESC LIMIT :limit")
    fun getRecentVehicles(limit: Int): Flow<List<VehicleEntity>>

}