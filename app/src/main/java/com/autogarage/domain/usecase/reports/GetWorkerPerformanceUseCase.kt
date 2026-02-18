package com.autogarage.domain.usecase.reports

import com.autogarage.domain.model.JobCardStatus
import com.autogarage.domain.repository.JobCardRepository
import com.autogarage.domain.repository.WorkerRepository
import com.autogarage.domain.usecase.base.UseCase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import javax.inject.Inject

data class WorkerPerformance(
    val workers: List<WorkerStats>
)

data class WorkerStats(
    val workerId: Long,
    val workerName: String,
    val totalJobsAssigned: Int,
    val completedJobs: Int,
    val pendingJobs: Int,
    val averageCompletionTime: Double, // hours
    val revenueGenerated: Double,
    val completionRate: Double // percentage
)

class GetWorkerPerformanceUseCase @Inject constructor(
    private val workerRepository: WorkerRepository,
    private val jobCardRepository: JobCardRepository
) : UseCase<GetWorkerPerformanceUseCase.Params, WorkerPerformance>() {

    data class Params(
        val startDate: Long,
        val endDate: Long
    )

    override val dispatcher = Dispatchers.IO

    override suspend fun execute(params: Params): WorkerPerformance {
        // ✅ FIX: Get workers list first
        val workersList = workerRepository.getAllWorkers().first()

        // ✅ FIX: Filter only active workers
        val activeWorkers = workersList.filter {
            it.status.name == "ACTIVE"
        }

        if (activeWorkers.isEmpty()) {
            return WorkerPerformance(workers = emptyList())
        }

        val workerStats = activeWorkers.map { worker ->
            try {
                // ✅ FIX: Get job cards for this specific technician
                val jobCards = jobCardRepository.getJobCardsByTechnician(
                    technicianId = worker.id,
                    startDate = params.startDate,
                    endDate = params.endDate
                )

                // ✅ Calculate stats
                val completedJobs = jobCards.count { jobCard ->
                    jobCard.status == JobCardStatus.COMPLETED ||
                            jobCard.status == JobCardStatus.DELIVERED
                }

                val pendingJobs = jobCards.count { jobCard ->
                    jobCard.status == JobCardStatus.PENDING ||
                            jobCard.status == JobCardStatus.IN_PROGRESS
                }

                val completionRate = if (jobCards.isNotEmpty()) {
                    (completedJobs.toDouble() / jobCards.size) * 100
                } else {
                    0.0
                }

                // ✅ Calculate average completion time
                val completedJobsList = jobCards.filter { jobCard ->
                    (jobCard.status == JobCardStatus.COMPLETED ||
                            jobCard.status == JobCardStatus.DELIVERED) &&
                            jobCard.actualCompletionDate != null
                }

                val avgCompletionTime = if (completedJobsList.isNotEmpty()) {
                    completedJobsList.map { job ->
                        // Parse completion date string to timestamp
                        val completionTimestamp = try {
                            // If actualCompletionDate is stored as ISO string
                            java.time.LocalDate.parse(job.actualCompletionDate)
                                .atStartOfDay()
                                .atZone(java.time.ZoneId.systemDefault())
                                .toInstant()
                                .toEpochMilli()
                        } catch (e: Exception) {
                            // If it's already a timestamp string
                            job.actualCompletionDate?.toLongOrNull() ?: job.createdAt
                        }

                        val durationMillis = completionTimestamp - job.createdAt
                        durationMillis / (1000.0 * 60 * 60) // Convert to hours
                    }.average()
                } else {
                    0.0
                }

                // ✅ Calculate revenue generated
                val revenueGenerated = jobCards
                    .filter {
                        it.status == JobCardStatus.COMPLETED ||
                                it.status == JobCardStatus.DELIVERED
                    }
                    .sumOf { it.finalAmount }

                WorkerStats(
                    workerId = worker.id,
                    workerName = worker.name,
                    totalJobsAssigned = jobCards.size,
                    completedJobs = completedJobs,
                    pendingJobs = pendingJobs,
                    averageCompletionTime = avgCompletionTime,
                    revenueGenerated = revenueGenerated,
                    completionRate = completionRate
                )
            } catch (e: Exception) {
                // ✅ Handle errors gracefully - return empty stats for this worker
                WorkerStats(
                    workerId = worker.id,
                    workerName = worker.name,
                    totalJobsAssigned = 0,
                    completedJobs = 0,
                    pendingJobs = 0,
                    averageCompletionTime = 0.0,
                    revenueGenerated = 0.0,
                    completionRate = 0.0
                )
            }
        }

        // ✅ Sort by revenue generated (highest first)
        return WorkerPerformance(
            workers = workerStats.sortedByDescending { it.revenueGenerated }
        )
    }
}
