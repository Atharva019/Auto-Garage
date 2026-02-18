package com.autogarage.domain.usecase.inventory

import com.autogarage.domain.repository.InventoryRepository
import com.autogarage.domain.model.InventoryItem
import com.autogarage.domain.usecase.base.FlowUseCase
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class GetLowStockItemsUseCase @Inject constructor(
    private val inventoryRepository: InventoryRepository
) : FlowUseCase<Unit, List<InventoryItem>>() {
    override fun execute(params: Unit): Flow<List<InventoryItem>> {
        return inventoryRepository.getLowStockItems()
    }
}