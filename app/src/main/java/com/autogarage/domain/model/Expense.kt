package com.autogarage.domain.model

data class Expense(
    val id: Long = 0,
    val category: ExpenseCategory,
    val amount: Double,
    val description: String,
    val paymentMode: PaymentMode,
    val receiptImageUrl: String? = null,
    val expenseDate: String,
    val notes: String? = null,
    val createdAt: Long = System.currentTimeMillis()
)

enum class ExpenseCategory {
    RENT, ELECTRICITY, WATER, TOOLS, SALARY, MAINTENANCE,
    OFFICE_SUPPLIES, MARKETING, INSURANCE, TAX, FUEL, OTHER
}
