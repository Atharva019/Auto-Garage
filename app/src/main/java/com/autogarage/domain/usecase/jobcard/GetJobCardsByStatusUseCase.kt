package com.autogarage.domain.usecase.jobcard

import com.autogarage.domain.repository.JobCardRepository
import com.autogarage.domain.model.JobCard
import com.autogarage.domain.model.JobCardStatus
import com.autogarage.domain.usecase.base.FlowUseCase
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class GetJobCardsByStatusUseCase @Inject constructor(
    private val jobCardRepository: JobCardRepository
) : FlowUseCase<JobCardStatus, List<JobCard>>() {

    override fun execute(params: JobCardStatus): Flow<List<JobCard>> {
        return jobCardRepository.getJobCardsByStatus(params)
    }
}