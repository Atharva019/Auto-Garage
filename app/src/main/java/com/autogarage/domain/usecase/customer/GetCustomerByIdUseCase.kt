package com.autogarage.domain.usecase.customer

import com.autogarage.domain.repository.CustomerRepository
import com.autogarage.domain.model.Customer
import com.autogarage.domain.usecase.base.FlowUseCase
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class GetCustomerByIdUseCase @Inject constructor(
    private val customerRepository: CustomerRepository
) : FlowUseCase<Long, Customer?>() {
    override fun execute(params: Long): Flow<Customer?> {
        return customerRepository.getCustomerById(params)
    }
}