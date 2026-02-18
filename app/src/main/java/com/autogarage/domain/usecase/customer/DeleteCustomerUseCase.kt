package com.autogarage.domain.usecase.customer

import com.autogarage.domain.model.Customer
import com.autogarage.domain.repository.CustomerRepository
import com.autogarage.domain.usecase.base.UseCase
import kotlinx.coroutines.Dispatchers
import javax.inject.Inject

class DeleteCustomerUseCase @Inject constructor(
    private val customerRepository: CustomerRepository
) : UseCase<Customer, Unit>() {

    override val dispatcher = Dispatchers.IO

    override suspend fun execute(params: Customer) {
        customerRepository.deleteCustomer(params)
    }
}