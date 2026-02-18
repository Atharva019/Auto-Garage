package com.autogarage.domain.usecase.customer

import com.autogarage.domain.repository.CustomerRepository
import com.autogarage.domain.model.Customer
import com.autogarage.domain.usecase.base.FlowUseCase
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class SearchCustomersUseCase @Inject constructor(
    private val customerRepository: CustomerRepository
) : FlowUseCase<String, List<Customer>>() {

    override fun execute(params: String): Flow<List<Customer>> {
        return customerRepository.searchCustomers(params)
    }
}