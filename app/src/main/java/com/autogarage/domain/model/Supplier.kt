package com.autogarage.domain.model

data class Supplier(
    val id: Long = 0,
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