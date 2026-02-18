package com.autogarage.domain.repository

import com.autogarage.domain.model.InventoryItem
import com.autogarage.domain.usecase.reports.PartUsage
import kotlinx.coroutines.flow.Flow

interface InventoryRepository {
    suspend fun getAllItems(): List<InventoryItem>
    suspend fun getTopUsedParts(startDate: Long, endDate: Long, limit: Int): List<PartUsage>

    fun getAllActiveItems(): Flow<List<InventoryItem>>
    fun getLowStockItems(): Flow<List<InventoryItem>>
    fun getItemsByCategory(category: String): Flow<List<InventoryItem>>
    fun getItemById(itemId: Long): Flow<InventoryItem?>
    fun searchItems(query: String): Flow<List<InventoryItem>>
    suspend fun insertItem(item: InventoryItem): Long
    suspend fun updateItem(item: InventoryItem)
    suspend fun deleteItem(item: InventoryItem)
    suspend fun updateStock(itemId: Long, quantity: Int)

}