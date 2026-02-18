package com.autogarage.domain.usecase.worker

import com.autogarage.domain.model.Worker
import com.autogarage.domain.repository.WorkerRepository
import com.autogarage.domain.usecase.base.UseCase
import kotlinx.coroutines.Dispatchers
import javax.inject.Inject

class DeleteWorkerUseCase @Inject constructor(
    private val workerRepository: WorkerRepository
) : UseCase<Worker, Unit>() {

    override val dispatcher = Dispatchers.IO

    override suspend fun execute(params: Worker) {
        // Check if worker has any active job cards
        // This check should be in repository or added as needed
        workerRepository.deleteWorker(params)
    }
}
