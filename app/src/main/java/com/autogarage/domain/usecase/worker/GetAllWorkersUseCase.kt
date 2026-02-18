package com.autogarage.domain.usecase.worker

import com.autogarage.domain.repository.WorkerRepository
import com.autogarage.domain.model.Worker
import com.autogarage.domain.model.WorkerRole
import com.autogarage.domain.model.WorkerStatus
import com.autogarage.domain.usecase.base.UseCase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import java.time.LocalDate
import javax.inject.Inject

class GetAllWorkersUseCase @Inject constructor(
    private val repository: WorkerRepository
) {
    operator fun invoke(activeOnly: Boolean = false): Flow<List<Worker>> {
        return if (activeOnly) {
            repository.getActiveWorkers()
        } else {
            repository.getAllWorkers()
        }
    }
}

class SearchWorkersUseCase @Inject constructor(
    private val repository: WorkerRepository
) {
    operator fun invoke(query: String): Flow<List<Worker>> {
        return repository.searchWorkers(query)
    }
}

//class CreateWorkerUseCase @Inject constructor(
//    private val repository: WorkerRepository
//) {
//    data class Params(
//        val name: String,
//        val phone: String,
//        val email: String?,
//        val role: WorkerRole,
//        val specialization: String?,
//        val dateOfJoining: LocalDate,
//        val salary: Double,
//        val address: String?,
//        val emergencyContact: String?
//    )
//
//    suspend operator fun invoke(params: Params): Result<Long> {
//        return try {
//            val worker = Worker(
//                name = params.name,
//                phone = params.phone,
//                email = params.email,
//                role = params.role,
//                specialization = params.specialization,
//                dateOfJoining = params.dateOfJoining,
//                salary = params.salary,
//                address = params.address,
//                emergencyContact = params.emergencyContact,
//            )
//            val id = repository.createWorker(worker)
//            Result.success(id)
//        } catch (e: Exception) {
//            Result.failure(e)
//        }
//    }
//}

class CreateWorkerUseCase @Inject constructor(
    private val workerRepository: WorkerRepository
) : UseCase<Worker, Long>() {

    override val dispatcher = Dispatchers.IO

    override suspend fun execute(params: Worker): Long {
        // Validation
        require(params.name.isNotBlank()) { "Worker name cannot be empty" }
        require(params.phone.length == 10) { "Phone number must be 10 digits" }
        require(params.salary > 0) { "Salary must be greater than 0" }

        // Create worker
        return workerRepository.createWorker(params)
    }
}

class GetWorkerStatsUseCase @Inject constructor(
    private val repository: WorkerRepository
) {
    data class WorkerStats(
        val totalWorkers: Int,
        val activeWorkers: Int,
        val averageSalary: Double
    )

    suspend operator fun invoke(): Result<WorkerStats> {
        return try {
            val averageSalary = repository.getAverageSalary()
            // Note: For totalWorkers and activeWorkers, you'd need to collect from Flow
            // This is a simplified version
            Result.success(
                WorkerStats(
                    totalWorkers = 0, // Will be calculated in ViewModel
                    activeWorkers = 0,
                    averageSalary = averageSalary
                )
            )
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
