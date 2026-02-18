package com.autogarage.domain.usecase.worker

import com.autogarage.domain.model.WorkerStatus
import com.autogarage.domain.repository.WorkerRepository
import com.autogarage.domain.usecase.base.UseCase
import kotlinx.coroutines.Dispatchers
import javax.inject.Inject

class UpdateWorkerStatusUseCase @Inject constructor(
    private val repository: WorkerRepository
) : UseCase<UpdateWorkerStatusUseCase.Params, Unit>() {

    data class Params(
        val workerId: Long,
        val status: WorkerStatus
    )

    override suspend fun execute(params: Params) {
        repository.updateWorkerStatus(params.workerId, params.status)
    }
}