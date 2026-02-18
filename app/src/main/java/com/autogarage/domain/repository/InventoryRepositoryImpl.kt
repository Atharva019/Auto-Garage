package com.autogarage.domain.repository

import com.autogarage.data.local.dao.InventoryDao
import com.autogarage.data.mapper.toDomain
import com.autogarage.data.mapper.toEntity
import com.autogarage.domain.model.InventoryItem
import com.autogarage.domain.usecase.reports.PartUsage
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import javax.inject.Inject
class InventoryRepositoryImpl @Inject constructor(
    private val inventoryDao: InventoryDao
) : InventoryRepository {
    override suspend fun getAllItems(): List<InventoryItem> {
        return withContext(Dispatchers.IO){
            inventoryDao.getAllItems() as List<InventoryItem>
        }
    }

    override suspend fun getTopUsedParts(
        startDate: Long,
        endDate: Long,
        limit: Int
    ): List<PartUsage> {
        return withContext(Dispatchers.IO){
            inventoryDao.getTopUsedPartsDto(startDate, endDate, limit)
                .map { dto ->
                    PartUsage(
                        partId = dto.partId,
                        partName = dto.partName,
                        usageCount = dto.usageCount,
                        totalValue = dto.totalValue
                    )
                }
        }
    }


    override fun getAllActiveItems(): Flow<List<InventoryItem>> {
        return inventoryDao.getAllActiveItems().map { entities ->
            entities.map { it.toDomain() }
        }
    }

    override fun getLowStockItems(): Flow<List<InventoryItem>> {
        return inventoryDao.getLowStockItems().map { entities ->
            entities.map { it.toDomain() }
        }
    }

    override fun getItemsByCategory(category: String): Flow<List<InventoryItem>> {
        return inventoryDao.getItemsByCategory(category).map { entities ->
            entities.map { it.toDomain() }
        }
    }

    override fun getItemById(itemId: Long): Flow<InventoryItem?> {
        return inventoryDao.getItemById(itemId).map { it?.toDomain() }
    }

    override fun searchItems(query: String): Flow<List<InventoryItem>> {
        return inventoryDao.searchItems(query).map { entities ->
            entities.map { it.toDomain() }
        }
    }

    override suspend fun insertItem(item: InventoryItem): Long {
        return inventoryDao.insertItem(item.toEntity())
    }

    override suspend fun updateItem(item: InventoryItem) {
        inventoryDao.updateItem(item.toEntity())
    }

    override suspend fun deleteItem(item: InventoryItem) {
        inventoryDao.deleteItem(item.toEntity())
    }

    override suspend fun updateStock(itemId: Long, quantity: Int) {
        inventoryDao.updateStock(itemId, quantity)
    }
}
