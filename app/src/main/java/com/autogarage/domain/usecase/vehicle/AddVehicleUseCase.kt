package com.autogarage.domain.usecase.vehicle

import com.autogarage.domain.repository.VehicleRepository
import com.autogarage.domain.model.FuelType
import com.autogarage.domain.model.TransmissionType
import com.autogarage.domain.model.Vehicle
import com.autogarage.domain.usecase.base.UseCase
import javax.inject.Inject

class AddVehicleUseCase @Inject constructor(
    private val vehicleRepository: VehicleRepository
) : UseCase<AddVehicleUseCase.Params, Long>() {

    data class Params(
        val customerId: Long,
        val registrationNumber: String,
        val make: String,
        val model: String,
        val year: Int,
        val color: String? = null,
        val fuelType: FuelType? = null,
        val transmission: TransmissionType? = null,
        val currentKilometers: Int = 0
    )

    override suspend fun execute(params: Params): Long {
        // Validation
        require(params.registrationNumber.isNotBlank()) {
            "Registration number cannot be empty"
        }
        require(params.make.isNotBlank()) { "Make cannot be empty" }
        require(params.model.isNotBlank()) { "Model cannot be empty" }
        require(params.year in 1900..2100) { "Invalid year" }
        require(params.currentKilometers >= 0) { "Kilometers cannot be negative" }

        // Check if vehicle already exists
        val existing = vehicleRepository.getVehicleByRegNumber(params.registrationNumber)
        if (existing != null) {
            throw IllegalStateException(
                "Vehicle with registration ${params.registrationNumber} already exists"
            )
        }

        val vehicle = Vehicle(
            customerId = params.customerId,
            registrationNumber = params.registrationNumber.uppercase(),
            make = params.make,
            model = params.model,
            year = params.year,
            color = params.color,
            fuelType = params.fuelType,
            transmission = params.transmission,
            currentKilometers = params.currentKilometers
        )

        return vehicleRepository.insertVehicle(vehicle)
    }
}
