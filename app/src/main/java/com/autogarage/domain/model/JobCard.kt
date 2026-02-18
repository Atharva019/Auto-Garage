package com.autogarage.domain.model

data class JobCard(
    val id: Long = 0,
    val jobCardNumber: String,
    val vehicle: Vehicle,
    val assignedTechnician: Worker? = null,
    val status: JobCardStatus,
    val currentKilometers: Int,
    val estimatedCompletionDate: String? = null,
    val actualCompletionDate: String? = null,
    val deliveryDate: String? = null,
    val customerComplaints: String? = null,
    val mechanicObservations: String? = null,
    val beforeServicePhotos: List<String> = emptyList(),
    val afterServicePhotos: List<String> = emptyList(),
    val services: List<JobCardService> = emptyList(),
    val parts: List<JobCardPart> = emptyList(),
    val laborCost: Double = 0.0,
    val partsCost: Double = 0.0,
    val totalCost: Double = 0.0,
    val discount: Double = 0.0,
    val finalAmount: Double = 0.0,
    val priority: Priority = Priority.NORMAL,
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
)

enum class JobCardStatus {
    PENDING, IN_PROGRESS, COMPLETED, CANCELLED, DELIVERED
}

enum class Priority {
    LOW, NORMAL, HIGH, URGENT
}