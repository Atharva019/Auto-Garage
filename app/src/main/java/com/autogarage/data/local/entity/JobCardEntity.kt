package com.autogarage.data.local.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "job_cards",
    foreignKeys = [
        ForeignKey(
            entity = VehicleEntity::class,
            parentColumns = ["vehicleId"],
            childColumns = ["vehicleId"],
            onDelete = ForeignKey.RESTRICT
        ),
        ForeignKey(
            entity = WorkerEntity::class,
            parentColumns = ["id"],
            childColumns = ["assignedTechnicianId"],
            onDelete = ForeignKey.SET_NULL
        )
    ],
    indices = [
        Index(value = ["vehicleId"]),
        Index(value = ["assignedTechnicianId"]),
        Index(value = ["status"]),
        Index(value = ["jobCardNumber"], unique = true),
        Index(value = ["jobCardId"]),
        Index(value = ["createdAt"]),        // ✅ Add for date sorting
        Index(value = ["status", "createdAt"]) // ✅ Composite index for filtered sorting
    ]
)
data class JobCardEntity(
    @PrimaryKey(autoGenerate = true)
    val jobCardId: Long = 0,
    val jobCardNumber: String, // JC-2024-001
    val vehicleId: Long,
    val assignedTechnicianId: Long? = null,
    val status: String, // PENDING, IN_PROGRESS, COMPLETED, CANCELLED, DELIVERED
    val currentKilometers: Int,
    val estimatedCompletionDate: String? = null, // Format: yyyy-MM-dd
    val actualCompletionDate: String? = null, // Format: yyyy-MM-dd
    val deliveryDate: String? = null, // Format: yyyy-MM-dd
    val customerComplaints: String? = null,
    val mechanicObservations: String? = null,
    val beforeServicePhotos: String? = null, // JSON array of photo URLs
    val afterServicePhotos: String? = null, // JSON array of photo URLs
    val laborCost: Double = 0.0,
    val partsCost: Double = 0.0,
    val totalCost: Double = 0.0,
    val discount: Double = 0.0,
    val finalAmount: Double = 0.0,
    val priority: String = "NORMAL", // LOW, NORMAL, HIGH, URGENT
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
)
