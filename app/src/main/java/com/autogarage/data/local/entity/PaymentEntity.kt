package com.autogarage.data.local.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "payments",
    foreignKeys = [
        ForeignKey(
            entity = InvoiceEntity::class,
            parentColumns = ["invoiceId"],
            childColumns = ["invoiceId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index(value = ["invoiceId"])]
)
data class PaymentEntity(
    @PrimaryKey(autoGenerate = true)
    val paymentId: Long = 0,
    val invoiceId: Long,
    val amount: Double,
    val paymentMode: String, // CASH, CARD, UPI, BANK_TRANSFER
    val transactionId: String? = null,
    val notes: String? = null,
    val paymentDate: Long = System.currentTimeMillis()
)