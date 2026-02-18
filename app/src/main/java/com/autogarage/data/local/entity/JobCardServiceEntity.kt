package com.autogarage.data.local.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "job_card_services",
    foreignKeys = [
        ForeignKey(
            entity = JobCardEntity::class,
            parentColumns = ["jobCardId"],
            childColumns = ["jobCardId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index("jobCardId")]
)
data class JobCardServiceEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val jobCardId: Long,
    val serviceId: Long,
    val serviceName: String,
    val description: String? = null,
    val laborCost: Double,
    val quantity: Int = 1,
    val totalCost: Double = laborCost * quantity
)
