package com.autogarage.domain.model

data class Invoice(
    val id: Long = 0,
    val invoiceNumber: String,
    val invoiceDate: Long = System.currentTimeMillis(),
    val jobCard: JobCard,
    val customer: Customer, // ADD THIS LINE
    val laborCost: Double,
    val partsCost: Double,
    val subtotal: Double,
    val discount: Double = 0.0,
    val discountPercentage: Double = 0.0,
    val taxableAmount: Double,
    val taxRate: Double,
    val taxAmount: Double,
    val totalAmount: Double,
    val paymentStatus: PaymentStatus,
    val paidAmount: Double = 0.0,
    val pendingAmount: Double,
    val paymentMode: PaymentMode? = null,
    val paymentDate: Long? = null,
    val transactionId: String? = null,
    val notes: String? = null,
    val termsAndConditions: String? = null,
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
)
 {
    // Computed property for display
    val isPaid: Boolean
        get() = paymentStatus == PaymentStatus.PAID

    val isUnpaid: Boolean
        get() = paymentStatus == PaymentStatus.UNPAID
}

enum class PaymentStatus {
    UNPAID,
    PAID,
    CANCELLED
}

enum class PaymentMode {
    CASH,
    UPI
}