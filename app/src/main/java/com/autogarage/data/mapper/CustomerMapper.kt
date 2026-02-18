package com.autogarage.data.mapper

import com.autogarage.data.local.entity.CustomerEntity
import com.autogarage.domain.model.Customer


fun CustomerEntity.toDomain(): Customer {
    return Customer(
        id = customerId,
        name = name,
        phone = phone,
        email = email,
        address = address,
        gstNumber = gstNumber,
        loyaltyPoints = loyaltyPoints,
        totalSpent = totalSpent,
        dateOfBirth = dateOfBirth,
        anniversary = anniversary,
        notes = notes,
        createdAt = createdAt,
        updatedAt = updatedAt
    )
}

fun Customer.toEntity(): CustomerEntity {
    return CustomerEntity(
        customerId = id,
        name = name,
        phone = phone,
        email = email,
        address = address,
        gstNumber = gstNumber,
        loyaltyPoints = loyaltyPoints,
        totalSpent = totalSpent,
        dateOfBirth = dateOfBirth,
        anniversary = anniversary,
        notes = notes,
        createdAt = createdAt,
        updatedAt = updatedAt
    )
}