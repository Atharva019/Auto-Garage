package com.autogarage.domain.usecase.vehicle

import com.autogarage.domain.repository.VehicleRepository
import com.autogarage.domain.model.Vehicle
import com.autogarage.domain.usecase.base.FlowUseCase
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class GetVehicleByIdUseCase @Inject constructor(
    private val vehicleRepository: VehicleRepository
) : FlowUseCase<Long, Vehicle?>() {
    override fun execute(params: Long): Flow<Vehicle?> {
        return vehicleRepository.getVehicleById(params)
    }
}
