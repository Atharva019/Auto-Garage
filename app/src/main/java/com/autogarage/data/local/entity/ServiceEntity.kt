package com.autogarage.data.local.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "services",
    indices = [Index(value = ["name"])]
)
data class ServiceEntity(
    @PrimaryKey(autoGenerate = true)
    val serviceId: Long = 0,
    val name: String,
    val description: String? = null,
    val category: String, // Engine, Electrical, Body Work, etc.
    val defaultPrice: Double,
    val defaultDurationMinutes: Int,
    val isActive: Boolean = true,
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
)
