package com.autogarage.domain.usecase.jobcard

import com.autogarage.domain.repository.JobCardRepository
import com.autogarage.domain.model.JobCard
import com.autogarage.domain.model.JobCardStatus
import com.autogarage.domain.model.Priority
import com.autogarage.domain.model.Vehicle
import com.autogarage.domain.model.Worker
import com.autogarage.domain.usecase.base.UseCase
import kotlinx.coroutines.Dispatchers
import javax.inject.Inject

class CreateJobCardUseCase @Inject constructor(
    private val jobCardRepository: JobCardRepository
) : UseCase<CreateJobCardUseCase.Params, Long>() {

    // ✅ OPTIMIZATION: Override with IO dispatcher
    override val dispatcher = Dispatchers.IO


    data class Params(
        val vehicle: Vehicle,
        val currentKilometers: Int,
        val customerComplaints: String,
        val assignedTechnician: Worker? = null,
        val priority: Priority = Priority.NORMAL,
        val estimatedCompletionDate: String? = null
    )

    override suspend fun execute(params: Params): Long {
        // Validation
        require(params.customerComplaints.isNotBlank()) {
            "Customer complaints cannot be empty"
        }
        require(params.currentKilometers >= 0) {
            "Kilometers cannot be negative"
        }

        // Generate job card number
        val jobCardNumber = jobCardRepository.generateJobCardNumber()

        val jobCard = JobCard(
            jobCardNumber = jobCardNumber,
            vehicle = params.vehicle,
            assignedTechnician = params.assignedTechnician, // Set later with full worker object
            status = JobCardStatus.PENDING,
            currentKilometers = params.currentKilometers,
            customerComplaints = params.customerComplaints,
            priority = params.priority,
            estimatedCompletionDate = params.estimatedCompletionDate
        )

        return jobCardRepository.insertJobCard(jobCard)
    }
}