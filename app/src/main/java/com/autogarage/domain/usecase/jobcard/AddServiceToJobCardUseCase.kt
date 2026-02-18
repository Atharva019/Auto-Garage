package com.autogarage.domain.usecase.jobcard

import com.autogarage.domain.model.JobCardService
import com.autogarage.domain.model.JobCardPart
import com.autogarage.domain.repository.JobCardRepository
import com.autogarage.domain.usecase.base.UseCase
import javax.inject.Inject
class AddServiceToJobCardUseCase @Inject constructor(
    private val repository: JobCardRepository
) : UseCase<AddServiceToJobCardUseCase.Params, Long>() {

    data class Params(
        val jobCardId: Long,
        val serviceId: Long,
        val serviceName: String,
        val description: String? = null,
        val laborCost: Double,
        val quantity: Int = 1
    )

    override suspend fun execute(params: Params): Long {
        require(params.laborCost > 0) { "Labor cost must be greater than 0" }
        require(params.quantity > 0) { "Quantity must be greater than 0" }

        val service = JobCardService(
            jobCardId = params.jobCardId,
            serviceId = params.serviceId,
            serviceName = params.serviceName,
            description = params.description,
            laborCost = params.laborCost,
            quantity = params.quantity
        )

        return repository.addServiceToJobCard(service)
    }
}

class AddPartToJobCardUseCase @Inject constructor(
    private val repository: JobCardRepository
) : UseCase<AddPartToJobCardUseCase.Params, Long>() {

    data class Params(
        val jobCardId: Long,
        val partId: Long,
        val partName: String,
        val partNumber: String,
        val quantity: Int,
        val unitPrice: Double,
        val discount: Double = 0.0
    )

    override suspend fun execute(params: Params): Long {
        require(params.quantity > 0) { "Quantity must be greater than 0" }
        require(params.unitPrice > 0) { "Unit price must be greater than 0" }

        val part = JobCardPart(
            jobCardId = params.jobCardId,
            partId = params.partId,
            partName = params.partName,
            partNumber = params.partNumber,
            quantity = params.quantity,
            unitPrice = params.unitPrice,
            discount = params.discount
        )

        return repository.addPartToJobCard(part)
    }
}

class GetJobCardServicesUseCase @Inject constructor(
    private val repository: JobCardRepository
) : com.autogarage.domain.usecase.base.FlowUseCase<Long, List<JobCardService>>() {
    override fun execute(params: Long): kotlinx.coroutines.flow.Flow<List<JobCardService>> {
        return repository.getServicesByJobCard(params)
    }
}

class GetJobCardPartsUseCase @Inject constructor(
    private val repository: JobCardRepository
) : com.autogarage.domain.usecase.base.FlowUseCase<Long, List<JobCardPart>>() {
    override fun execute(params: Long): kotlinx.coroutines.flow.Flow<List<JobCardPart>> {
        return repository.getPartsByJobCard(params)
    }
}

class RemoveServiceFromJobCardUseCase @Inject constructor(
    private val repository: JobCardRepository
) : UseCase<JobCardService, Unit>() {
    override suspend fun execute(params: JobCardService) {
        repository.removeServiceFromJobCard(params)
    }
}

class RemovePartFromJobCardUseCase @Inject constructor(
    private val repository: JobCardRepository
) : UseCase<JobCardPart, Unit>() {
    override suspend fun execute(params: JobCardPart) {
        repository.removePartFromJobCard(params)
    }
}