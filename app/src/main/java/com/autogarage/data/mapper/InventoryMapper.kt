package com.autogarage.data.mapper

import com.autogarage.data.local.entity.InventoryItemEntity
import com.autogarage.domain.model.InventoryItem
import com.autogarage.domain.model.StockStatus

fun InventoryItemEntity.toDomain(): InventoryItem {
    return InventoryItem(
        id = itemId,
        partNumber = partNumber,
        name = name,
        description = description,
        category = category,
        brand = brand,
        currentStock = currentStock,
        minimumStock = minimumStock,
        maximumStock = maximumStock,
        unit = unit,
        purchasePrice = purchasePrice,
        sellingPrice = sellingPrice,
        supplierId = supplierId,
        location = location,
        barcode = barcode,
        imageUrl = imageUrl,
//        stockStatus = StockStatus,
        isActive = isActive,
        lastRestockDate = lastRestockDate,
        createdAt = createdAt,
        updatedAt = updatedAt
    )
}

fun InventoryItem.toEntity(): InventoryItemEntity {
    return InventoryItemEntity(
        itemId = id,
        partNumber = partNumber,
        name = name,
        description = description,
        category = category,
        brand = brand,
        currentStock = currentStock,
        minimumStock = minimumStock,
        maximumStock = maximumStock,
        unit = unit,
        purchasePrice = purchasePrice,
        sellingPrice = sellingPrice,
        supplierId = supplierId,
        location = location,
        barcode = barcode,
        imageUrl = imageUrl,
        isActive = isActive,
        lastRestockDate = lastRestockDate,
        createdAt = createdAt,
        updatedAt = updatedAt
    )
}