package com.autogarage.domain.repository

import com.autogarage.data.local.dao.WorkerDao
import com.autogarage.data.mapper.toDomain
import com.autogarage.data.mapper.toEntity
import com.autogarage.domain.model.Worker
import com.autogarage.domain.model.WorkerRole
import com.autogarage.domain.model.WorkerStatus
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
class WorkerRepositoryImpl @Inject constructor(
    private val workerDao: WorkerDao
) : WorkerRepository {

    override fun getAllWorkers(): Flow<List<Worker>> {
        return workerDao.getAllWorkers().map { entities ->
            entities.map { it.toDomain() }
        }
    }

    override fun getActiveWorkers(): Flow<List<Worker>> {
        return workerDao.getActiveWorkers().map { entities ->
            entities.map { it.toDomain() }
        }
    }

    override suspend fun getWorkerById(workerId: Long): Worker? {
        return workerDao.getWorkerById(workerId)?.toDomain()
    }

    override fun getWorkerByIdFlow(workerId: Long): Flow<Worker?> {
        return workerDao.getWorkerByIdFlow(workerId).map { it?.toDomain() }
    }

    override fun searchWorkers(query: String): Flow<List<Worker>> {
        return workerDao.searchWorkers(query).map { entities ->
            entities.map { it.toDomain() }
        }
    }

    override fun getWorkersByRole(role: WorkerRole): Flow<List<Worker>> {
        return workerDao.getWorkersByRole(role.name).map { entities ->
            entities.map { it.toDomain() }
        }
    }

    override fun getActiveWorkersCount(): Flow<Int> {
        return workerDao.getActiveWorkersCount()
    }

    override suspend fun getAverageSalary(): Double {
        return workerDao.getAverageSalary() ?: 0.0
    }

    override suspend fun createWorker(worker: Worker): Long {
        return workerDao.insertWorker(worker.toEntity())
    }

    override suspend fun updateWorker(worker: Worker) {
        workerDao.updateWorker(worker.toEntity())
    }

    override suspend fun deleteWorker(worker: Worker) {
        workerDao.deleteWorker(worker.toEntity())
    }

    override suspend fun updateWorkerStatus(workerId: Long, status: WorkerStatus) {
        workerDao.updateWorkerStatus(workerId, status.name)
    }

    override suspend fun updateWorkerStats(workerId: Long, rating: Double) {
        workerDao.updateWorkerStats(workerId, rating)
    }
}