package com.autogarage.data.mapper

import com.autogarage.data.local.entity.InvoiceEntity
import com.autogarage.domain.model.Customer
import com.autogarage.domain.model.Invoice
import com.autogarage.domain.model.JobCard
import com.autogarage.domain.model.PaymentMode
import com.autogarage.domain.model.PaymentStatus

fun InvoiceEntity.toDomain(
    jobCard: JobCard,
    customer: Customer): Invoice {
    return Invoice(
        id = invoiceId,
        invoiceNumber = invoiceNumber,
        jobCard = jobCard,
        customer = customer,
        invoiceDate = invoiceDate,
        laborCost = laborCost,
        partsCost = partsCost,
        subtotal = subtotal,
        discount = discount,
        discountPercentage = discountPercentage,
        taxableAmount = taxableAmount,
        taxRate = taxRate,
        taxAmount = taxAmount,
        totalAmount = totalAmount,
        paymentStatus = PaymentStatus.valueOf(paymentStatus),
        paymentMode = paymentMode?.let { PaymentMode.valueOf(it) },
        paidAmount = paidAmount,
        pendingAmount = pendingAmount,
        paymentDate = paymentDate,
        transactionId = transactionId,
        notes = notes,
        termsAndConditions = termsAndConditions,
        createdAt = createdAt,
        updatedAt = updatedAt
    )
}

fun Invoice.toEntity(): InvoiceEntity {
    return InvoiceEntity(
        invoiceId = id,
        invoiceNumber = invoiceNumber,
        jobCardId = jobCard.id,
        customerId = customer.id,
        invoiceDate = invoiceDate,
        laborCost = laborCost,
        partsCost = partsCost,
        subtotal = subtotal,
        discount = discount,
        discountPercentage = discountPercentage,
        taxableAmount = taxableAmount,
        taxRate = taxRate,
        taxAmount = taxAmount,
        totalAmount = totalAmount,
        paymentStatus = paymentStatus.name,
        paymentMode = paymentMode?.name,
        paidAmount = paidAmount,
        pendingAmount = pendingAmount,
        paymentDate = paymentDate,
        transactionId = transactionId,
        notes = notes,
        termsAndConditions = termsAndConditions,
        createdAt = createdAt,
        updatedAt = updatedAt
    )
}