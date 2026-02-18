package com.autogarage.domain.usecase.reports

import com.autogarage.domain.repository.JobCardRepository
import com.autogarage.domain.usecase.base.UseCase
import kotlinx.coroutines.Dispatchers
import javax.inject.Inject

data class JobCardStats(
    val totalJobCards: Int,
    val pendingJobCards: Int,
    val inProgressJobCards: Int,
    val completedJobCards: Int,
    val deliveredJobCards: Int,
    val cancelledJobCards: Int,
    val averageCompletionTime: Double, // in hours
    val statusBreakdown: Map<String, Int>,
    val priorityBreakdown: Map<String, Int>,
    val technicianWorkload: Map<String, Int>
)

class GetJobCardStatsUseCase @Inject constructor(
    private val jobCardRepository: JobCardRepository
) : UseCase<GetJobCardStatsUseCase.Params, JobCardStats>() {

    data class Params(
        val startDate: Long,
        val endDate: Long
    )

    override val dispatcher = Dispatchers.IO

    override suspend fun execute(params: Params): JobCardStats {
        val jobCards = jobCardRepository.getJobCardsByDateRange(
            params.startDate,
            params.endDate
        )

        val statusBreakdown = jobCards
            .groupBy { it.status.name }
            .mapValues { it.value.size }

        val priorityBreakdown = jobCards
            .groupBy { it.priority.name }
            .mapValues { it.value.size }

        val technicianWorkload = jobCards
            .filter { it.assignedTechnician != null }
            .groupBy { it.assignedTechnician!!.name }
            .mapValues { it.value.size }

        // Calculate average completion time for completed jobs
        val completedJobs = jobCards.filter {
            it.actualCompletionDate != null
        }
        val averageCompletionTime = if (completedJobs.isNotEmpty()) {
            completedJobs.map { job ->
                // Fix: explicit casting to Long to resolve operator ambiguity
                // If 'createdAt' is a Date object, use .time. If it's a Long, just use it.
                // Assuming they are Longs based on standard Room usage:
                val start: Long = job.createdAt
                val end: Long = job.actualCompletionDate!!.toLong()

                (end - start) / (1000.0 * 60 * 60)
            }.average()

        } else 0.0

        return JobCardStats(
            totalJobCards = jobCards.size,
            pendingJobCards = statusBreakdown["PENDING"] ?: 0,
            inProgressJobCards = statusBreakdown["IN_PROGRESS"] ?: 0,
            completedJobCards = statusBreakdown["COMPLETED"] ?: 0,
            deliveredJobCards = statusBreakdown["DELIVERED"] ?: 0,
            cancelledJobCards = statusBreakdown["CANCELLED"] ?: 0,
            averageCompletionTime = averageCompletionTime,
            statusBreakdown = statusBreakdown,
            priorityBreakdown = priorityBreakdown,
            technicianWorkload = technicianWorkload
        )
    }
}