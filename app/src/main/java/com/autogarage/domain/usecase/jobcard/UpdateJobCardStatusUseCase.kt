package com.autogarage.domain.usecase.jobcard

import com.autogarage.domain.repository.JobCardRepository
import com.autogarage.domain.model.JobCard
import com.autogarage.domain.model.JobCardStatus
import com.autogarage.domain.usecase.base.UseCase
import java.text.SimpleDateFormat
import java.util.*
import javax.inject.Inject

class UpdateJobCardStatusUseCase @Inject constructor(
    private val jobCardRepository: JobCardRepository
) : UseCase<UpdateJobCardStatusUseCase.Params, Unit>() {

    data class Params(
        val jobCard: JobCard,
        val newStatus: JobCardStatus
    )

    override suspend fun execute(params: Params) {
        val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        val currentDate = dateFormat.format(Date())

        val updatedJobCard = when (params.newStatus) {
            JobCardStatus.IN_PROGRESS -> {
                params.jobCard.copy(status = params.newStatus)
            }
            JobCardStatus.COMPLETED -> {
                params.jobCard.copy(
                    status = params.newStatus,
                    actualCompletionDate = currentDate
                )
            }
            JobCardStatus.DELIVERED -> {
                require(params.jobCard.status == JobCardStatus.COMPLETED) {
                    "Job card must be completed before delivery"
                }
                params.jobCard.copy(
                    status = params.newStatus,
                    deliveryDate = currentDate
                )
            }
            JobCardStatus.CANCELLED -> {
                params.jobCard.copy(status = params.newStatus)
            }
            else -> params.jobCard.copy(status = params.newStatus)
        }

        jobCardRepository.updateJobCard(updatedJobCard)
    }
}