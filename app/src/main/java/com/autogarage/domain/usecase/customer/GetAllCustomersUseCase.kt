package com.autogarage.domain.usecase.customer

import com.autogarage.domain.repository.CustomerRepository
import com.autogarage.domain.model.Customer
import com.autogarage.domain.usecase.base.FlowUseCase
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class GetAllCustomersUseCase @Inject constructor(
    private val customerRepository: CustomerRepository
) : FlowUseCase<Unit, List<Customer>>() {

    override fun execute(params: Unit): Flow<List<Customer>> {
        return customerRepository.getAllCustomers()
    }
}