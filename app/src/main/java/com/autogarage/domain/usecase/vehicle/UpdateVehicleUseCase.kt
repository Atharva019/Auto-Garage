package com.autogarage.domain.usecase.vehicle

import com.autogarage.domain.repository.VehicleRepository
import com.autogarage.domain.model.Vehicle
import com.autogarage.domain.usecase.base.UseCase
import javax.inject.Inject

class UpdateVehicleUseCase @Inject constructor(
    private val vehicleRepository: VehicleRepository
) : UseCase<Vehicle, Unit>() {
    override suspend fun execute(params: Vehicle) {
        vehicleRepository.updateVehicle(params)
    }
}