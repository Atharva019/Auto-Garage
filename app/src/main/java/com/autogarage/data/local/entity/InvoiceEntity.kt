package com.autogarage.data.local.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "invoices",
    foreignKeys = [
        ForeignKey(
            entity = JobCardEntity::class,
            parentColumns = ["jobCardId"],
            childColumns = ["jobCardId"],
            onDelete = ForeignKey.RESTRICT
        ),
        ForeignKey(
            entity = CustomerEntity::class,
            parentColumns = ["customerId"],
            childColumns = ["customerId"],
            onDelete = ForeignKey.RESTRICT
        )
    ],
    indices = [
        Index(value = ["invoiceNumber"], unique = true),
        Index(value = ["jobCardId"]),
        Index(value = ["customerId"])
    ]
)
data class InvoiceEntity(
    @PrimaryKey(autoGenerate = true)
    val invoiceId: Long = 0,
    val invoiceNumber: String,
    val jobCardId: Long,
    val customerId: Long,
    val invoiceDate: Long = System.currentTimeMillis(),

    // Cost Breakdown
    val laborCost: Double,
    val partsCost: Double,
    val subtotal: Double,
    val discount: Double,
    val discountPercentage: Double = 0.0,
    val taxableAmount: Double,
    val taxRate: Double,
    val taxAmount: Double,
    val totalAmount: Double,

    // Payment Info
    val paymentStatus: String, // UNPAID, PAID, CANCELLED
    val paymentMode: String? = null, // CASH, UPI
    val paidAmount: Double = 0.0,
    val pendingAmount: Double,
    val paymentDate: Long? = null,
    val transactionId: String? = null,

    // Additional
    val notes: String? = null,
    val termsAndConditions: String? = null,

    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
)