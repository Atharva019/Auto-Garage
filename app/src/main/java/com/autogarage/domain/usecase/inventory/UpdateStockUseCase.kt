package com.autogarage.domain.usecase.inventory

import com.autogarage.domain.repository.InventoryRepository
import com.autogarage.domain.usecase.base.UseCase
import javax.inject.Inject

class UpdateStockUseCase @Inject constructor(
    private val inventoryRepository: InventoryRepository
) : UseCase<UpdateStockUseCase.Params, Unit>() {

    data class Params(
        val itemId: Long,
        val quantity: Int // Positive for add, negative for remove
    )

    override suspend fun execute(params: Params) {
        inventoryRepository.updateStock(params.itemId, params.quantity)
    }
}