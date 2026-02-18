package com.autogarage.domain.model

import java.time.LocalDate
import java.time.LocalDateTime

data class Worker(
    val id: Long = 0,
    val name: String,
    val phone: String,
    val email: String? = null,
    val role: WorkerRole,
    val specialization: String? = null,
    val dateOfJoining: LocalDate,
    val salary: Double,
    val address: String? = null,
    val emergencyContact: String? = null,
    val status: WorkerStatus = WorkerStatus.ACTIVE,
    val rating: Double = 0.0,
    val completedJobs: Int = 0,
    //val aadharNumber: String? = null,
   // val panNumber: String? = null,
    val createdAt: LocalDateTime = LocalDateTime.now(),
    val updatedAt: LocalDateTime = LocalDateTime.now(),
    val activeJobs: Int
)

enum class WorkerRole {
    MECHANIC,
    ELECTRICIAN,
    PAINTER,
    WELDER,
    HELPER,
    SUPERVISOR,
    MANAGER
}

enum class WorkerStatus {
    ACTIVE,
    ON_LEAVE,
    RESIGNED,
    TERMINATED,
    INACTIVE
}