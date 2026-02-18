package com.autogarage.domain.repository

import com.autogarage.domain.model.Worker
import com.autogarage.domain.model.WorkerRole
import com.autogarage.domain.model.WorkerStatus
import kotlinx.coroutines.flow.Flow

interface WorkerRepository {
    fun getAllWorkers(): Flow<List<Worker>>
    fun getActiveWorkers(): Flow<List<Worker>>
    suspend fun getWorkerById(workerId: Long): Worker?
    fun getWorkerByIdFlow(workerId: Long): Flow<Worker?>
    fun searchWorkers(query: String): Flow<List<Worker>>
    fun getWorkersByRole(role: WorkerRole): Flow<List<Worker>>
    fun getActiveWorkersCount(): Flow<Int>
    suspend fun getAverageSalary(): Double
    suspend fun createWorker(worker: Worker): Long
    suspend fun updateWorker(worker: Worker)
    suspend fun deleteWorker(worker: Worker)
    suspend fun updateWorkerStatus(workerId: Long, status: WorkerStatus)
    suspend fun updateWorkerStats(workerId: Long, rating: Double)
}