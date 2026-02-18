package com.autogarage.data.mapper

import com.autogarage.data.local.entity.WorkerEntity
import com.autogarage.domain.model.Worker
import com.autogarage.domain.model.WorkerRole
import com.autogarage.domain.model.WorkerStatus
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import java.time.LocalDate
import java.time.LocalDateTime

fun WorkerEntity.toDomain(): Worker {
    return Worker(
        id = id,
        name = name,
        phone = phone,
        email = email,
        role = WorkerRole.valueOf(role),
        specialization = specialization,
        dateOfJoining = LocalDate.parse(dateOfJoining),
        salary = salary,
        address = address,
        emergencyContact = emergencyContact,
        status = WorkerStatus.valueOf(status),
        rating = rating,
        completedJobs = completedJobs,
        activeJobs = activeJobs,
        createdAt = LocalDateTime.parse(createdAt),
        updatedAt = LocalDateTime.parse(updatedAt),
    )
}

fun Worker.toEntity(): WorkerEntity {
    return WorkerEntity(
        id = id,
        name = name,
        phone = phone,
        email = email,
        role = role.name,
        specialization = specialization,
        dateOfJoining = dateOfJoining.toString(),
        salary = salary,
        address = address,
        emergencyContact = emergencyContact,
        status = status.name,
        rating = rating,
        completedJobs = completedJobs,
        activeJobs = activeJobs,
        createdAt = createdAt.toString(),
        updatedAt = updatedAt.toString()
    )
}