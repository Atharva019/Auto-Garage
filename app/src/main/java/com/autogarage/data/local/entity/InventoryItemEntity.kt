package com.autogarage.data.local.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "inventory_items",
    indices = [
        Index(value = ["partNumber"], unique = true),
        Index(value = ["category"])
    ]
)
data class InventoryItemEntity(
    @PrimaryKey(autoGenerate = true)
    val itemId: Long = 0,
    val partNumber: String,
    val name: String,
    val description: String? = null,
    val category: String, // Engine Parts, Electrical, Filters, etc.
    val brand: String? = null,
    val currentStock: Int = 0,
    val minimumStock: Int = 10,
    val maximumStock: Int? = null,
    val unit: String = "PCS", // PCS, LITRE, KG, etc.
    val purchasePrice: Double,
    val sellingPrice: Double,
    val supplierId: Long? = null,
    val location: String? = null, // Shelf/Bin location
    val barcode: String? = null,
    val imageUrl: String? = null,
    val isActive: Boolean = true,
    val lastRestockDate: String? = null, // Format: yyyy-MM-dd
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
)