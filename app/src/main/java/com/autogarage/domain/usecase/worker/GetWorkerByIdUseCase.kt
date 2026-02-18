package com.autogarage.domain.usecase.worker

import com.autogarage.domain.model.Worker
import com.autogarage.domain.repository.WorkerRepository
import com.autogarage.domain.usecase.base.FlowUseCase
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class GetWorkerByIdUseCase @Inject constructor(
    private val workerRepository: WorkerRepository
) : FlowUseCase<Long, Worker?>() {

    override fun execute(params: Long): Flow<Worker?> {
        return workerRepository.getWorkerByIdFlow(params)
    }
}