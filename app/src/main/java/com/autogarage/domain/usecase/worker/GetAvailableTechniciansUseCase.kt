package com.autogarage.domain.usecase.worker

import com.autogarage.domain.repository.WorkerRepository
import com.autogarage.domain.model.Worker
import com.autogarage.domain.model.WorkerRole
import com.autogarage.domain.usecase.base.FlowUseCase
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class GetAvailableTechniciansUseCase @Inject constructor(
    private val workerRepository: WorkerRepository
) : FlowUseCase<Unit, List<Worker>>() {
    override fun execute(params: Unit): Flow<List<Worker>> {
        return workerRepository.getActiveWorkers()
    }
}