package com.autogarage.domain.usecase.inventory

import com.autogarage.domain.model.InventoryItem
import com.autogarage.domain.repository.InventoryRepository
import com.autogarage.domain.usecase.base.FlowUseCase
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class GetInventoryItemsByCategoryUseCase @Inject constructor(
    private val inventoryRepository: InventoryRepository
) : FlowUseCase<String, List<InventoryItem>>() {
    override fun execute(params: String): Flow<List<InventoryItem>> {
        return inventoryRepository.getItemsByCategory(params)
    }
}