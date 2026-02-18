package com.autogarage.data.mapper

import com.autogarage.data.local.entity.VehicleEntity
import com.autogarage.domain.model.*

fun VehicleEntity.toDomain(): Vehicle {
    return Vehicle(
        id = vehicleId,
        customerId = customerId,
        registrationNumber = registrationNumber,
        make = make,
        model = model,
        year = year,
        color = color,
        engineNumber = engineNumber,
        chassisNumber = chassisNumber,
        fuelType = fuelType?.let { FuelType.valueOf(it) },
        transmission = transmission?.let { TransmissionType.valueOf(it) },
        currentKilometers = currentKilometers,
        insuranceExpiryDate = insuranceExpiryDate,
        pucExpiryDate = pucExpiryDate,
        lastServiceDate = lastServiceDate,
        nextServiceDue = nextServiceDue,
        vehicleImageUrl = vehicleImageUrl,
        notes = notes,
        createdAt = createdAt,
        updatedAt = updatedAt
    )
}

fun Vehicle.toEntity(): VehicleEntity {
    return VehicleEntity(
        vehicleId = id,
        customerId = customerId,
        registrationNumber = registrationNumber,
        make = make,
        model = model,
        year = year,
        color = color,
        engineNumber = engineNumber,
        chassisNumber = chassisNumber,
        fuelType = fuelType?.name,
        transmission = transmission?.name,
        currentKilometers = currentKilometers,
        insuranceExpiryDate = insuranceExpiryDate,
        pucExpiryDate = pucExpiryDate,
        lastServiceDate = lastServiceDate,
        nextServiceDue = nextServiceDue,
        vehicleImageUrl = vehicleImageUrl,
        notes = notes,
        createdAt = createdAt,
        updatedAt = updatedAt
    )
}