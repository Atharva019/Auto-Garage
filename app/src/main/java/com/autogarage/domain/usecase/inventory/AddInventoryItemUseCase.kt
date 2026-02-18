package com.autogarage.domain.usecase.inventory

import com.autogarage.domain.repository.InventoryRepository
import com.autogarage.domain.model.InventoryItem
import com.autogarage.domain.usecase.base.UseCase
import javax.inject.Inject

class AddInventoryItemUseCase @Inject constructor(
    private val inventoryRepository: InventoryRepository
) : UseCase<AddInventoryItemUseCase.Params, Long>() {

    data class Params(
        val partNumber: String,
        val name: String,
        val description: String? = null,
        val category: String,
        val brand: String? = null,
        val currentStock: Int,
        val minimumStock: Int = 10,
        val purchasePrice: Double,
        val sellingPrice: Double,
        val supplierId: Long? = null,
        val location: String? = null
    )

    override suspend fun execute(params: Params): Long {
        // Validation
        require(params.partNumber.isNotBlank()) { "Part number cannot be empty" }
        require(params.name.isNotBlank()) { "Item name cannot be empty" }
        require(params.currentStock >= 0) { "Stock cannot be negative" }
        require(params.minimumStock >= 0) { "Minimum stock cannot be negative" }
        require(params.purchasePrice >= 0) { "Purchase price cannot be negative" }
        require(params.sellingPrice >= 0) { "Selling price cannot be negative" }
        require(params.sellingPrice >= params.purchasePrice) {
            "Selling price should be greater than or equal to purchase price"
        }

        val item = InventoryItem(
            partNumber = params.partNumber,
            name = params.name,
            description = params.description,
            category = params.category,
            brand = params.brand,
            currentStock = params.currentStock,
            minimumStock = params.minimumStock,
            purchasePrice = params.purchasePrice,
            sellingPrice = params.sellingPrice,
            supplierId = params.supplierId,
            location = params.location
        )

        return inventoryRepository.insertItem(item)
    }
}