package com.autogarage.domain.usecase.jobcard

import com.autogarage.domain.repository.JobCardRepository
import com.autogarage.domain.model.JobCardStatus
import com.autogarage.domain.usecase.base.FlowUseCase
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import javax.inject.Inject

data class DashboardStats(
    val totalJobCards: Int,
    val pendingCount: Int,
    val inProgressCount: Int,
    val completedCount: Int
)

class GetDashboardStatsUseCase @Inject constructor(
    private val jobCardRepository: JobCardRepository
) : FlowUseCase<Unit, DashboardStats>() {
    override fun execute(params: Unit): Flow<DashboardStats> {
        return combine(
            jobCardRepository.getAllJobCards(),
            jobCardRepository.getJobCardCountByStatus(JobCardStatus.PENDING),
            jobCardRepository.getJobCardCountByStatus(JobCardStatus.IN_PROGRESS),
            jobCardRepository.getJobCardCountByStatus(JobCardStatus.COMPLETED)
        ) { allJobs, pending, inProgress, completed ->
            DashboardStats(
                totalJobCards = allJobs.size,
                pendingCount = pending,
                inProgressCount = inProgress,
                completedCount = completed
            )
        }
    }
}