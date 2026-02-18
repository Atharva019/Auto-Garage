package com.autogarage.domain.model

data class Service(
    val id: Long = 0,
    val name: String,
    val description: String? = null,
    val category: String,
    val defaultPrice: Double,
    val defaultDurationMinutes: Int,
    val isActive: Boolean = true,
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
)

data class JobCardService(
    val id: Long = 0,
    val jobCardId: Long,
    val serviceId: Long,
    val serviceName: String,
    val description: String? = null,
    val laborCost: Double,
    val quantity: Int = 1,
    val totalCost: Double = laborCost * quantity
)

data class JobCardPart(
    val id: Long = 0,
    val jobCardId: Long,
    val partId: Long,           // Maps to itemId in entity
    val partName: String,        // Fetched from inventory
    val partNumber: String,      // Fetched from inventory
    val quantity: Int = 1,
    val unitPrice: Double,
    val discount: Double = 0.0,
    val totalCost: Double = (unitPrice * quantity) - discount  // Maps to totalPrice in entity
)