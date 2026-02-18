package com.autogarage.domain.model

data class Customer(
    val id: Long = 0,
    val name: String,
    val phone: String,
    val email: String? = null,
    val address: String? = null,
    val gstNumber: String? = null,
    val loyaltyPoints: Int = 0,
    val totalSpent: Double = 0.0,
    val dateOfBirth: String? = null,
    val anniversary: String? = null,
    val notes: String? = null,
    val vehicles: List<Vehicle> = emptyList(),
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
)