package com.autogarage.domain.repository

import com.autogarage.domain.model.Customer
import kotlinx.coroutines.flow.Flow

interface CustomerRepository {
    suspend fun getActiveCustomersInPeriod(startDate: Long, endDate: Long): List<Customer>
    suspend fun getTopCustomersByRevenue(limit: Int): List<Customer>
    suspend fun getCustomerJobCardCount(customerId: Long): Int
    suspend fun getAllCustomersSync(): List<Customer>
    fun getAllCustomers(): Flow<List<Customer>>
    fun getCustomerById(customerId: Long): Flow<Customer?>
    suspend fun getCustomerByPhone(phone: String): Customer?
    // ✅ ADD: Calculate actual total spent from invoices
    suspend fun getCustomerTotalSpent(customerId: Long): Double
    // ✅ ADD: Update customer's total spent field
    suspend fun updateCustomerTotalSpent(customerId: Long, totalSpent: Double)
    fun searchCustomers(query: String): Flow<List<Customer>>
    suspend fun insertCustomer(customer: Customer): Long
    suspend fun updateCustomer(customer: Customer)
    suspend fun deleteCustomer(customer: Customer)
    suspend fun updateTotalSpent(customerId: Long, amount: Double)
    suspend fun getCustomerByIdSync(id: Long): Customer?
    fun getRecentCustomers(limit: Int = 50): Flow<List<Customer>>
}