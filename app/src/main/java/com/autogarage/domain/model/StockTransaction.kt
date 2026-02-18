package com.autogarage.domain.model

data class StockTransaction(
    val id: Long = 0,
    val item: InventoryItem,
    val transactionType: TransactionType,
    val quantity: Int,
    val unitPrice: Double? = null,
    val supplier: Supplier? = null,
    val referenceNumber: String? = null,
    val notes: String? = null,
    val transactionDate: Long = System.currentTimeMillis()
)

enum class TransactionType {
    PURCHASE, SALE, ADJUSTMENT, RETURN
}