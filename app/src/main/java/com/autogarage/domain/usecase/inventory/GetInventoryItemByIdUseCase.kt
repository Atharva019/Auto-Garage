package com.autogarage.domain.usecase.inventory

import com.autogarage.domain.model.InventoryItem
import com.autogarage.domain.repository.InventoryRepository
import com.autogarage.domain.usecase.base.FlowUseCase
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class GetInventoryItemByIdUseCase @Inject constructor(
    private val inventoryRepository: InventoryRepository
) : FlowUseCase<Long, InventoryItem?>() {
    override fun execute(params: Long): Flow<InventoryItem?> {
        return inventoryRepository.getItemById(params)
    }
}