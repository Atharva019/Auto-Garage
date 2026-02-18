package com.autogarage.data.local.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "suppliers",
    indices = [Index(value = ["phone"])]
)
data class SupplierEntity(
    @PrimaryKey(autoGenerate = true)
    val supplierId: Long = 0,
    val name: String,
    val contactPerson: String? = null,
    val phone: String,
    val email: String? = null,
    val address: String? = null,
    val gstNumber: String? = null,
    val paymentTerms: String? = null,
    val notes: String? = null,
    val isActive: Boolean = true,
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
)
