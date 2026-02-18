package com.autogarage.domain.repository

import com.autogarage.data.local.dao.VehicleDao
import com.autogarage.data.mapper.toDomain
import com.autogarage.data.mapper.toEntity
import com.autogarage.domain.model.Vehicle
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class VehicleRepositoryImpl @Inject constructor(
    private val vehicleDao: VehicleDao
) : VehicleRepository {

    override fun getVehiclesByCustomer(customerId: Long): Flow<List<Vehicle>> {
        return vehicleDao.getVehiclesByCustomer(customerId).map { entities ->
            entities.map { it.toDomain() }
        }
    }

    override fun getVehicleById(vehicleId: Long): Flow<Vehicle?> {
        return vehicleDao.getVehicleById(vehicleId).map { it?.toDomain() }
    }

    override suspend fun getVehicleByRegNumber(regNumber: String): Vehicle? {
        return vehicleDao.getVehicleByRegNumber(regNumber)?.toDomain()
    }

    override suspend fun insertVehicle(vehicle: Vehicle): Long {
        return vehicleDao.insertVehicle(vehicle.toEntity())
    }

    override suspend fun updateVehicle(vehicle: Vehicle) {
        vehicleDao.updateVehicle(vehicle.toEntity())
    }

    override suspend fun deleteVehicle(vehicle: Vehicle) {
        vehicleDao.deleteVehicle(vehicle.toEntity())
    }

    override fun getRecentVehicles(limit: Int): Flow<List<Vehicle>> {
        return vehicleDao.getRecentVehicles(limit)
            .map { entities -> entities.map { it.toDomain() } }
    }
}