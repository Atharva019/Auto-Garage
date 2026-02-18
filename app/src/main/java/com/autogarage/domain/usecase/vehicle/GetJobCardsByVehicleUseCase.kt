package com.autogarage.domain.usecase.vehicle

import com.autogarage.domain.repository.JobCardRepository
import com.autogarage.domain.model.JobCard
import com.autogarage.domain.usecase.base.FlowUseCase
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class GetJobCardsByVehicleUseCase @Inject constructor(
    private val jobCardRepository: JobCardRepository
) : FlowUseCase<Long, List<JobCard>>() {
    override fun execute(params: Long): Flow<List<JobCard>> {
        return jobCardRepository.getJobCardsByVehicle(params)
    }
}