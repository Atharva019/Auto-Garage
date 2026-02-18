package com.autogarage.domain.repository

import com.autogarage.domain.model.Vehicle
import kotlinx.coroutines.flow.Flow

interface VehicleRepository {
    fun getVehiclesByCustomer(customerId: Long): Flow<List<Vehicle>>
    fun getVehicleById(vehicleId: Long): Flow<Vehicle?>
    suspend fun getVehicleByRegNumber(regNumber: String): Vehicle?
    suspend fun insertVehicle(vehicle: Vehicle): Long
    suspend fun updateVehicle(vehicle: Vehicle)
    suspend fun deleteVehicle(vehicle: Vehicle)
    fun getRecentVehicles(limit: Int = 50): Flow<List<Vehicle>>
}