package com.autogarage.data.local.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "customers",
    indices = [
        Index(value = ["phone"], unique = true),
        Index(value = ["email"]),
        Index(value = ["name"]),            // ✅ Add for name searches
        Index(value = ["createdAt"])
    ]
)
data class CustomerEntity(
    @PrimaryKey(autoGenerate = true)
    val customerId: Long = 0,
    val name: String,
    val phone: String,
    val email: String? = null,
    val address: String? = null,
    val gstNumber: String? = null,
    val loyaltyPoints: Int = 0,
    val totalSpent: Double = 0.0,
    val dateOfBirth: String? = null, // Format: yyyy-MM-dd
    val anniversary: String? = null, // Format: yyyy-MM-dd
    val notes: String? = null,
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
)