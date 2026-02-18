package com.autogarage.data.local.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import com.autogarage.domain.model.Worker
import com.autogarage.domain.model.WorkerRole
import com.autogarage.domain.model.WorkerStatus
import java.time.LocalDate
import java.time.LocalDateTime

@Entity(tableName = "workers")
data class WorkerEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val name: String,
    val phone: String,
    val email: String?,
    val role: String,
    val specialization: String?,
    val dateOfJoining: String, // Store as ISO string
    val salary: Double,
    val address: String?,
    val emergencyContact: String?,
    val status: String,
    val rating: Double,
    val completedJobs: Int,
    val activeJobs: Int = 0,
    //val aadharNumber: String?,
    //val panNumber: String?,
    val createdAt: String,
    val updatedAt: String
)

