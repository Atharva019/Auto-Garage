package com.autogarage.domain.usecase.customer

import com.autogarage.domain.repository.CustomerRepository
import com.autogarage.domain.model.Customer
import com.autogarage.domain.usecase.base.UseCase
import javax.inject.Inject

class CreateCustomerUseCase @Inject constructor(
    private val customerRepository: CustomerRepository
) : UseCase<CreateCustomerUseCase.Params, Long>() {

    data class Params(
        val name: String,
        val phone: String,
        val email: String? = null,
        val address: String? = null,
        val gstNumber: String? = null,
        val dateOfBirth: String? = null,
        val anniversary: String? = null,
        val notes: String? = null
    )

    override suspend fun execute(params: Params): Long {
        // Validation
        require(params.name.isNotBlank()) { "Customer name cannot be empty" }
        require(params.phone.isNotBlank()) { "Phone number cannot be empty" }
        require(params.phone.matches(Regex("^[0-9]{10}$"))) {
            "Phone number must be 10 digits"
        }

        // Check if customer already exists
        val existingCustomer = customerRepository.getCustomerByPhone(params.phone)
        if (existingCustomer != null) {
            throw IllegalStateException("Customer with phone ${params.phone} already exists")
        }

        val customer = Customer(
            name = params.name,
            phone = params.phone,
            email = params.email,
            address = params.address,
            gstNumber = params.gstNumber,
            dateOfBirth = params.dateOfBirth,
            anniversary = params.anniversary,
            notes = params.notes
        )

        return customerRepository.insertCustomer(customer)
    }
}