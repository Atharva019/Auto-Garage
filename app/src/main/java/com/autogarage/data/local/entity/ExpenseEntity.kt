package com.autogarage.data.local.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "expenses",
    indices = [
        Index(value = ["category"]),
        Index(value = ["expenseDate"])
    ]
)
data class ExpenseEntity(
    @PrimaryKey(autoGenerate = true)
    val expenseId: Long = 0,
    val category: String, // RENT, ELECTRICITY, TOOLS, SALARY, MAINTENANCE, etc.
    val amount: Double,
    val description: String,
    val paymentMode: String, // CASH, CARD, UPI, BANK_TRANSFER
    val receiptImageUrl: String? = null,
    val expenseDate: String, // Format: yyyy-MM-dd
    val notes: String? = null,
    val createdAt: Long = System.currentTimeMillis()
)