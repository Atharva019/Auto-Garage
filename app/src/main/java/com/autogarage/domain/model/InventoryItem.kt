package com.autogarage.domain.model

data class InventoryItem(
    val id: Long = 0,
    val partNumber: String,
    val name: String,
    val description: String? = null,
    val category: String,
    val brand: String? = null,
    val currentStock: Int = 0,
    val minimumStock: Int = 10,
    val maximumStock: Int? = null,
    val unit: String = "PCS",
    val purchasePrice: Double,
    val sellingPrice: Double,
    val supplierId: Long? = null,
    val location: String? = null,
    val barcode: String? = null,
    val imageUrl: String? = null,
    val isActive: Boolean = true,
    val lastRestockDate: String? = null,
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
) {
    val stockStatus: StockStatus
        get() = when {
            currentStock <= 0 -> StockStatus.OUT_OF_STOCK
            currentStock <= minimumStock -> StockStatus.LOW_STOCK
            else -> StockStatus.IN_STOCK
        }

    val profitMargin: Double
        get() = ((sellingPrice - purchasePrice) / purchasePrice) * 100
}

enum class StockStatus {
    IN_STOCK, LOW_STOCK, OUT_OF_STOCK
}

//data class JobCardPart(
//         val id: Long = 0,
//         val jobCardId: Long,
//         val partId: Long,
//         val partName: String,
//         val partNumber: String,
//         val quantity: Int = 1,
//         val unitPrice: Double,
//         val discount: Double = 0.0,
//         val totalCost: Double = (unitPrice * quantity) - discount
//    )
