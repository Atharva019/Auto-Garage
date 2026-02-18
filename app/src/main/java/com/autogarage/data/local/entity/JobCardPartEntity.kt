package com.autogarage.data.local.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "job_card_parts",
    foreignKeys = [
        ForeignKey(
            entity = JobCardEntity::class,
            parentColumns = ["jobCardId"],
            childColumns = ["jobCardId"],
            onDelete = ForeignKey.CASCADE
        ),
        ForeignKey(
            entity = InventoryItemEntity::class,
            parentColumns = ["itemId"],
            childColumns = ["itemId"],
            onDelete = ForeignKey.RESTRICT
        )
    ],
    indices = [
        Index(value = ["jobCardId"]),
        Index(value = ["itemId"])
    ]
)
data class JobCardPartEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val jobCardId: Long,
    val itemId: Long,
    val partName: String? = null,
    val partNumber: String? = null, // ← Links to inventory
    val quantity: Int,
    val unitPrice: Double,
    val totalPrice: Double,     // ← Note: totalPrice (not totalCost)
    val notes: String? = null
)
