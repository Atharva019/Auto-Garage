package com.autogarage.domain.usecase.reports

import com.autogarage.domain.repository.InventoryRepository
import com.autogarage.domain.usecase.base.UseCase
import kotlinx.coroutines.Dispatchers
import javax.inject.Inject

data class InventoryStats(
    val totalItems: Int,
    val inStockItems: Int,
    val lowStockItems: Int,
    val outOfStockItems: Int,
    val totalInventoryValue: Double,
    val topUsedParts: List<PartUsage>,
    val stockAlerts: List<StockAlert>
)

data class PartUsage(
    val partId: Long,
    val partName: String,
    val usageCount: Int,
    val totalValue: Double
)

data class StockAlert(
    val itemId: Long,
    val itemName: String,
    val currentStock: Int,
    val minimumStock: Int,
    val alertLevel: String // "LOW", "OUT"
)

class GetInventoryStatsUseCase @Inject constructor(
    private val inventoryRepository: InventoryRepository
) : UseCase<GetInventoryStatsUseCase.Params, InventoryStats>() {

    data class Params(
        val startDate: Long,
        val endDate: Long
    )

    override val dispatcher = Dispatchers.IO

    override suspend fun execute(params: Params): InventoryStats {
        val allItems = inventoryRepository.getAllItems()

        val inStockItems = allItems.count { it.stockStatus.toString() == "IN_STOCK" }
        val lowStockItems = allItems.count { it.stockStatus.toString() == "LOW_STOCK" }
        val outOfStockItems = allItems.count { it.stockStatus.toString() == "OUT_OF_STOCK" }

        val totalInventoryValue = allItems.sumOf { 
            it.sellingPrice * it.currentStock
        }

        val topUsedParts = inventoryRepository.getTopUsedParts(
            params.startDate,
            params.endDate,
            limit = 10
        )

        val stockAlerts = allItems
            .filter { it.currentStock <= it.minimumStock }
            .map { item ->
                StockAlert(
                    itemId = item.id,
                    itemName = item.name,
                    currentStock = item.currentStock,
                    minimumStock = item.minimumStock,
                    alertLevel = if (item.currentStock == 0) "OUT" else "LOW"
                )
            }
            .sortedBy { it.currentStock }

        return InventoryStats(
            totalItems = allItems.size,
            inStockItems = inStockItems,
            lowStockItems = lowStockItems,
            outOfStockItems = outOfStockItems,
            totalInventoryValue = totalInventoryValue,
            topUsedParts = topUsedParts,
            stockAlerts = stockAlerts
        )
    }
}