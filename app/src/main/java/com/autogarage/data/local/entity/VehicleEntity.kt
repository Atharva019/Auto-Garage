package com.autogarage.data.local.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "vehicles",
    foreignKeys = [
        ForeignKey(
            entity = CustomerEntity::class,
            parentColumns = ["customerId"],
            childColumns = ["customerId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [
        Index(value = ["customerId"]),
        Index(value = ["registrationNumber"], unique = true)
    ]
)
data class VehicleEntity(
    @PrimaryKey(autoGenerate = true)
    val vehicleId: Long = 0,
    val customerId: Long,
    val registrationNumber: String,
    val make: String, // Honda, Toyota, etc.
    val model: String, // City, Fortuner, etc.
    val year: Int,
    val color: String? = null,
    val engineNumber: String? = null,
    val chassisNumber: String? = null,
    val fuelType: String? = null, // Petrol, Diesel, Electric, Hybrid
    val transmission: String? = null, // Manual, Automatic
    val currentKilometers: Int = 0,
    val insuranceExpiryDate: String? = null, // Format: yyyy-MM-dd
    val pucExpiryDate: String? = null, // Format: yyyy-MM-dd
    val lastServiceDate: String? = null, // Format: yyyy-MM-dd
    val nextServiceDue: String? = null, // Format: yyyy-MM-dd
    val vehicleImageUrl: String? = null,
    val notes: String? = null,
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
)