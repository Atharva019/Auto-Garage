package com.autogarage.domain.usecase.jobcard

import com.autogarage.domain.repository.JobCardRepository
import com.autogarage.domain.model.JobCard
import com.autogarage.domain.usecase.base.FlowUseCase
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class GetJobCardByIdUseCase @Inject constructor(
    private val jobCardRepository: JobCardRepository
) : FlowUseCase<Long, JobCard?>() {
    override fun execute(params: Long): Flow<JobCard?> {
        return jobCardRepository.getJobCardById(params)
    }
}