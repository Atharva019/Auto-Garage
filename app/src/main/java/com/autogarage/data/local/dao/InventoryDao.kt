package com.autogarage.data.local.dao

import androidx.room.*
import com.autogarage.data.local.entity.InventoryItemEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface InventoryDao {
    @Query("SELECT * FROM inventory_items ORDER BY name ASC")
    suspend fun getAllItemsSync(): List<InventoryItemEntity>
    @Query("SELECT * FROM inventory_items WHERE itemId = :itemId")
    suspend fun getInventoryItemByIdSync(itemId: Long): InventoryItemEntity?
    @Query("SELECT * FROM inventory_items WHERE isActive = 1 ORDER BY name ASC")
    fun getAllActiveItems(): Flow<List<InventoryItemEntity>>

    @Query("SELECT * FROM inventory_items WHERE currentStock <= minimumStock AND isActive = 1")
    fun getLowStockItems(): Flow<List<InventoryItemEntity>>

    @Query("SELECT * FROM inventory_items WHERE category = :category AND isActive = 1")
    fun getItemsByCategory(category: String): Flow<List<InventoryItemEntity>>

    @Query("SELECT * FROM inventory_items WHERE itemId = :itemId")
    fun getItemById(itemId: Long): Flow<InventoryItemEntity?>
    @Query("SELECT * FROM inventory_items WHERE itemId = :itemId")
    suspend fun getItemByIdSync(itemId: Long): InventoryItemEntity?


    @Query("SELECT * FROM inventory_items WHERE name LIKE '%' || :query || '%' OR partNumber LIKE '%' || :query || '%'")
    fun searchItems(query: String): Flow<List<InventoryItemEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertItem(item: InventoryItemEntity): Long

    @Update
    suspend fun updateItem(item: InventoryItemEntity)

    @Delete
    suspend fun deleteItem(item: InventoryItemEntity)

    @Query("UPDATE inventory_items SET currentStock = :quantity, updatedAt = :timestamp WHERE itemId = :itemId")
    suspend fun updateStockWithTimestamp(itemId: Long, quantity: Int, timestamp: Long)

    // ✅ OR USE THIS SIMPLER VERSION
    @Query("UPDATE inventory_items SET currentStock = :newStock WHERE itemId = :itemId")
    suspend fun updateStock(itemId: Long, newStock: Int)

    @Query("SELECT * FROM inventory_items")
    fun getAllItems(): Flow<List<InventoryItemEntity>>
    @Query("""
    SELECT 
        jcp.itemId as partId,
        ii.name as partName,
        COUNT(*) as usageCount,
        SUM(jcp.quantity * jcp.unitPrice) as totalValue
    FROM job_card_parts jcp
    INNER JOIN job_cards jc ON jcp.jobCardId = jc.jobCardId
    INNER JOIN inventory_items ii ON jcp.itemId = ii.itemId
    WHERE jc.createdAt >= :startDate AND jc.createdAt <= :endDate
    GROUP BY jcp.itemId, ii.name
    ORDER BY usageCount DESC
    LIMIT :limit
""")
    suspend fun getTopUsedPartsDto(
        startDate: Long,
        endDate: Long,
        limit: Int
    ): List<PartUsageDto>
}