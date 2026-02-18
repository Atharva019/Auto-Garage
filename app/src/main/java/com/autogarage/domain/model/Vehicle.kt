package com.autogarage.domain.model

data class Vehicle(
    val id: Long = 0,
    val customerId: Long,
    val registrationNumber: String,
    val make: String,
    val model: String,
    val year: Int,
    val color: String? = null,
    val engineNumber: String? = null,
    val chassisNumber: String? = null,
    val fuelType: FuelType? = null,
    val transmission: TransmissionType? = null,
    val currentKilometers: Int = 0,
    val insuranceExpiryDate: String? = null,
    val pucExpiryDate: String? = null,
    val lastServiceDate: String? = null,
    val nextServiceDue: String? = null,
    val vehicleImageUrl: String? = null,
    val notes: String? = null,
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
)

enum class FuelType {
    PETROL, DIESEL, ELECTRIC, HYBRID, CNG
}

enum class TransmissionType {
    MANUAL, AUTOMATIC, AMT, CVT, DCT
}
