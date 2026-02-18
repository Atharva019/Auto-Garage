package com.autogarage.domain.usecase.inventory

import com.autogarage.domain.model.InventoryItem
import com.autogarage.domain.repository.InventoryRepository
import com.autogarage.domain.usecase.base.UseCase
import javax.inject.Inject

class CreateInventoryItemUseCase @Inject constructor(
    private val repository: InventoryRepository
) : UseCase<CreateInventoryItemUseCase.Params, Long>() {

    data class Params(
        val name: String,
        val partNumber: String,
        val category: String,
        val quantity: Int,
        val unitPrice: Double,
        val lowStockThreshold: Int = 10,
        val supplier: Long? = null,
        val location: String? = null,
        val notes: String? = null
    )

    override suspend fun execute(params: Params): Long {
        require(params.name.isNotBlank()) { "Part name cannot be empty" }
        require(params.partNumber.isNotBlank()) { "Part number cannot be empty" }
        require(params.quantity > 0) { "Quantity must be greater than 0" }
        require(params.unitPrice > 0) { "Unit price must be greater than 0" }

        val inventoryItem = InventoryItem(
            name = params.name,
            partNumber = params.partNumber,
            category = params.category,
            currentStock = params.quantity,           // ← Changed from quantity
            purchasePrice = params.unitPrice,          // ← Changed from unitPrice
            sellingPrice = params.unitPrice * 1.2,     // ← Add 20% markup for selling price
            minimumStock = params.lowStockThreshold,  // ← Changed from lowStockThreshold
            supplierId = params.supplier,            // ← Changed from supplier
            location = params.location,         // ← Changed from location
            description = params.notes                 // ← Changed from notes
        )

        return repository.insertItem(inventoryItem)
    }
}
