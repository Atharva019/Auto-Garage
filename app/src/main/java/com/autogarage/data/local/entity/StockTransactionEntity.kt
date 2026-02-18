package com.autogarage.data.local.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "stock_transactions",
    foreignKeys = [
        ForeignKey(
            entity = InventoryItemEntity::class,
            parentColumns = ["itemId"],
            childColumns = ["itemId"],
            onDelete = ForeignKey.CASCADE
        ),
        ForeignKey(
            entity = SupplierEntity::class,
            parentColumns = ["supplierId"],
            childColumns = ["supplierId"],
            onDelete = ForeignKey.SET_NULL
        )
    ],
    indices = [
        Index(value = ["itemId"]),
        Index(value = ["supplierId"]),
        Index(value = ["transactionType"])
    ]
)
data class StockTransactionEntity(
    @PrimaryKey(autoGenerate = true)
    val transactionId: Long = 0,
    val itemId: Long,
    val transactionType: String, // PURCHASE, SALE, ADJUSTMENT, RETURN
    val quantity: Int,
    val unitPrice: Double? = null,
    val supplierId: Long? = null,
    val referenceNumber: String? = null, // Invoice/Bill number
    val notes: String? = null,
    val transactionDate: Long = System.currentTimeMillis()
)