package com.autogarage.domain.usecase.jobcard

import com.autogarage.domain.repository.JobCardRepository
import com.autogarage.domain.model.JobCard
import com.autogarage.domain.usecase.base.FlowUseCase
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class GetAllJobCardsUseCase @Inject constructor(
    private val jobCardRepository: JobCardRepository
) : FlowUseCase<Unit, List<JobCard>>() {

    override fun execute(params: Unit): Flow<List<JobCard>> {
        return jobCardRepository.getAllJobCards()
    }
}