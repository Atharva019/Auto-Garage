package com.autogarage.data.mapper

import androidx.room.Relation
import com.autogarage.data.local.entity.JobCardEntity
import com.autogarage.data.local.entity.JobCardPartEntity
import com.autogarage.data.local.entity.JobCardServiceEntity
import com.autogarage.domain.model.*
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.autogarage.data.local.dao.JobCardWithDetails
import com.autogarage.data.local.entity.VehicleEntity
import com.autogarage.data.local.entity.WorkerEntity

val gson = Gson()
fun JobCardWithDetails.toDomainModel(): JobCard {
    val beforePhotos = this.jobCard.beforeServicePhotos?.let {
        gson.fromJson<List<String>>(it, object : TypeToken<List<String>>() {}.type)
    } ?: emptyList()

    val afterPhotos = this.jobCard.afterServicePhotos?.let {
        gson.fromJson<List<String>>(it, object : TypeToken<List<String>>() {}.type)
    } ?: emptyList()

    return JobCard(
        id = this.jobCard.jobCardId,
        jobCardNumber = this.jobCard.jobCardNumber,
        vehicle = this.vehicle.toDomain(),
        assignedTechnician = this.technician?.toDomain(),
        status = JobCardStatus.valueOf(this.jobCard.status),
        currentKilometers = this.jobCard.currentKilometers,
        estimatedCompletionDate = this.jobCard.estimatedCompletionDate,
        actualCompletionDate = this.jobCard.actualCompletionDate,
        deliveryDate = this.jobCard.deliveryDate,
        customerComplaints = this.jobCard.customerComplaints,
        mechanicObservations = this.jobCard.mechanicObservations,
        beforeServicePhotos = beforePhotos,
        afterServicePhotos = afterPhotos,
        laborCost = this.jobCard.laborCost,
        partsCost = this.jobCard.partsCost,
        totalCost = this.jobCard.totalCost,
        discount = this.jobCard.discount,
        finalAmount = this.jobCard.finalAmount,
        priority = Priority.valueOf(this.jobCard.priority),
        createdAt = this.jobCard.createdAt,
        updatedAt = this.jobCard.updatedAt
    )
}

fun JobCardWithDetails.toJobCardDomain(): JobCard {
    return JobCard(
        id = jobCard.jobCardId,
        jobCardNumber = jobCard.jobCardNumber,
        vehicle = vehicle.toDomain(), // Uses vehicle from @Relation
        assignedTechnician = technician?.toDomain(),
        status = JobCardStatus.valueOf(jobCard.status),
        currentKilometers = jobCard.currentKilometers,
        estimatedCompletionDate = jobCard.estimatedCompletionDate,
        actualCompletionDate = jobCard.actualCompletionDate,
        deliveryDate = jobCard.deliveryDate,
        customerComplaints = jobCard.customerComplaints,
        mechanicObservations = jobCard.mechanicObservations,
        beforeServicePhotos = jobCard.beforeServicePhotos?.split(",") ?: emptyList(),
        afterServicePhotos = jobCard.afterServicePhotos?.split(",") ?: emptyList(),
        laborCost = jobCard.laborCost,
        partsCost = jobCard.partsCost,
        totalCost = jobCard.totalCost,
        discount = jobCard.discount,
        finalAmount = jobCard.finalAmount,
        priority = Priority.valueOf(jobCard.priority),
        createdAt = jobCard.createdAt,
        updatedAt = jobCard.updatedAt
    )
}

fun JobCardEntity.toDomain(
    vehicle: Vehicle,
    technician: Worker? = null
): JobCard {
    val beforePhotos = beforeServicePhotos?.let {
        gson.fromJson<List<String>>(it, object : TypeToken<List<String>>() {}.type)
    } ?: emptyList()

    val afterPhotos = afterServicePhotos?.let {
        gson.fromJson<List<String>>(it, object : TypeToken<List<String>>() {}.type)
    } ?: emptyList()

    return JobCard(
        id = jobCardId,
        jobCardNumber = jobCardNumber,
        vehicle = vehicle,
        assignedTechnician = technician,
        status = JobCardStatus.valueOf(status),
        currentKilometers = currentKilometers,
        estimatedCompletionDate = estimatedCompletionDate,
        actualCompletionDate = actualCompletionDate,
        deliveryDate = deliveryDate,
        customerComplaints = customerComplaints,
        mechanicObservations = mechanicObservations,
        beforeServicePhotos = beforePhotos,
        afterServicePhotos = afterPhotos,
        laborCost = laborCost,
        partsCost = partsCost,
        totalCost = totalCost,
        discount = discount,
        finalAmount = finalAmount,
        priority = Priority.valueOf(priority),
        createdAt = createdAt,
        updatedAt = updatedAt
    )
}


fun JobCard.toEntity(): JobCardEntity {
    val beforePhotosJson = if (beforeServicePhotos.isNotEmpty()) {
        gson.toJson(beforeServicePhotos)
    } else null

    val afterPhotosJson = if (afterServicePhotos.isNotEmpty()) {
        gson.toJson(afterServicePhotos)
    } else null

    return JobCardEntity(
        jobCardId = id,
        jobCardNumber = jobCardNumber,
        vehicleId = vehicle.id,
        assignedTechnicianId = assignedTechnician?.id,
        status = status.name,
        currentKilometers = currentKilometers,
        estimatedCompletionDate = estimatedCompletionDate,
        actualCompletionDate = actualCompletionDate,
        deliveryDate = deliveryDate,
        customerComplaints = customerComplaints,
        mechanicObservations = mechanicObservations,
        beforeServicePhotos = beforePhotosJson,
        afterServicePhotos = afterPhotosJson,
        laborCost = laborCost,
        partsCost = partsCost,
        totalCost = totalCost,
        discount = discount,
        finalAmount = finalAmount,
        priority = priority.name,
        createdAt = createdAt,
        updatedAt = updatedAt
    )
}


fun JobCardPartEntity.toDomain(inventoryItem: InventoryItem?): JobCardPart {
    return JobCardPart(
        id = this.id,
        jobCardId = this.jobCardId,
        partId = this.itemId,
        partName = inventoryItem?.name ?: "Unknown Part",
        partNumber = inventoryItem?.partNumber ?: "N/A",
        quantity = this.quantity,
        unitPrice = this.unitPrice,
        discount = 0.0,  // Not stored in entity
        totalCost = this.totalPrice
    )
}

// Mapper: Domain -> Entity
fun JobCardPart.toEntity(): JobCardPartEntity {
    return JobCardPartEntity(
        id = this.id,
        jobCardId = this.jobCardId,
        itemId = this.partId,
        partName = this.partName,
        partNumber = this.partNumber,
        quantity = this.quantity,
        unitPrice = this.unitPrice,
        totalPrice = this.totalCost,
        notes = null
    )
}

fun JobCardService.toEntity(): JobCardServiceEntity {
    return JobCardServiceEntity(
        id = this.id,
        jobCardId = this.jobCardId,
        serviceId = this.serviceId,
        serviceName = this.serviceName,
        description = this.description,
        laborCost = this.laborCost,
        quantity = this.quantity,
        totalCost = this.totalCost
    )
}


