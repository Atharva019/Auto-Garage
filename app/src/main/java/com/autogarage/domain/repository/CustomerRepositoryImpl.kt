package com.autogarage.domain.repository

import android.R.attr.id
import androidx.room.withTransaction
import com.autogarage.data.cache.CustomerCache
import com.autogarage.data.local.dao.CustomerDao
import com.autogarage.data.local.dao.InvoiceDao
import com.autogarage.data.local.dao.JobCardDao
import com.autogarage.data.local.dao.VehicleDao
import com.autogarage.data.local.database.GarageMasterDatabase
import com.autogarage.data.mapper.toDomain
import com.autogarage.data.mapper.toEntity
import com.autogarage.domain.model.Customer
import com.autogarage.util.PerformanceMonitor
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import javax.inject.Inject

class CustomerRepositoryImpl @Inject constructor(
    private val customerDao: CustomerDao,
    private val invoiceDao: InvoiceDao,
    private val jobCardDao: JobCardDao,
    private val vehicleDao: VehicleDao,
    private val database: GarageMasterDatabase,
    private val cache: CustomerCache
) : CustomerRepository {

    // ✅ OPTIMIZATION: Use withContext(Dispatchers.IO) for background operations
    override suspend fun insertCustomer(customer: Customer): Long {
        return PerformanceMonitor.measureSuspendOperation("createCustomer") {
            withContext(Dispatchers.IO) {
                customerDao.insertCustomer(customer.toEntity())
                cache.put(id.toLong(), customer.copy(id = id.toLong()))  // ✅ Cache immediately
                id.toLong()
            }
        }
    }

    // ✅ OPTIMIZATION: Batch operations in a single transaction
    suspend fun createCustomersInBatch(customers: List<Customer>): List<Long> {
        return withContext(Dispatchers.IO) {
            database.withTransaction {
                customers.map { customer ->
                    customerDao.insertCustomer(customer.toEntity())
                }
            }
        }
    }

    // ✅ NEW: Calculate actual total spent from paid invoices
    override suspend fun getCustomerTotalSpent(customerId: Long): Double {
        return withContext(Dispatchers.IO) {
            // Get all vehicles for this customer
            val vehicles = vehicleDao.getVehiclesByCustomerSync(customerId)

            // Sum up all paid invoices for these vehicles
            vehicles.sumOf { vehicle ->
                invoiceDao.getTotalPaidByVehicle(vehicle.vehicleId)
            }
        }
    }

    // ✅ NEW: Update customer's total spent
    override suspend fun updateCustomerTotalSpent(customerId: Long, totalSpent: Double) {
        withContext(Dispatchers.IO) {
            customerDao.updateCustomerTotalSpent(customerId, totalSpent)
        }
    }

    override suspend fun getActiveCustomersInPeriod(
        startDate: Long,
        endDate: Long
    ): List<Customer> {
        return withContext(Dispatchers.IO){
            customerDao.getActiveCustomersInPeriod(startDate, endDate).map { it.toDomain() }
        }
    }

    override suspend fun getTopCustomersByRevenue(limit: Int): List<Customer> {
        return withContext(Dispatchers.IO) {
            customerDao.getTopCustomersByRevenue(limit).map { it.toDomain() }
        }
    }

    override suspend fun getCustomerJobCardCount(customerId: Long): Int {
        return withContext(Dispatchers.IO){
            customerDao.getCustomerJobCardCount(customerId)
        }
    }

    override fun getAllCustomers(): Flow<List<Customer>> {
        return customerDao.getAllCustomers().map { entities ->
            entities.map { it.toDomain() }
        }
    }
    override suspend fun getAllCustomersSync(): List<Customer> {
        return withContext(Dispatchers.IO) {
            customerDao.getAllCustomersSync().map { it.toDomain() }
        }
    }

    override fun getCustomerById(customerId: Long): Flow<Customer?> {
        return customerDao.getCustomerById(customerId).map { it?.toDomain() }
    }

    override suspend fun getCustomerByPhone(phone: String): Customer? {
        return customerDao.getCustomerByPhone(phone)?.toDomain()
    }

    override fun searchCustomers(query: String): Flow<List<Customer>> {
        return customerDao.searchCustomers(query).map { entities ->
            entities.map { it.toDomain() }
        }
    }

//    override suspend fun insertCustomer(customer: Customer): Long {
//        return customerDao.insertCustomer(customer.toEntity())
//    }

    override suspend fun updateCustomer(customer: Customer) {
        customerDao.updateCustomer(customer.toEntity())
    }

    override suspend fun deleteCustomer(customer: Customer) {
        customerDao.deleteCustomer(customer.toEntity())
    }

    override suspend fun updateTotalSpent(customerId: Long, amount: Double) {
        customerDao.updateTotalSpent(customerId, amount)
    }

    override suspend fun getCustomerByIdSync(id: Long): Customer? {
        return customerDao.getCustomerByIdSync(id)?.toDomain()
    }

    override fun getRecentCustomers(limit: Int): Flow<List<Customer>> {
        return customerDao.getRecentCustomers(limit)
            .map { entities -> entities.map { it.toDomain() } }
    }

    // ✅ ADD THIS METHOD - Recalculate customer's total spent
    suspend fun recalculateCustomerTotalSpent(customerId: Long) {
        withContext(Dispatchers.IO) {
            database.withTransaction {
                // Get all paid invoices for this customer
                val paidInvoices = invoiceDao.getPaidInvoicesByCustomer(customerId)
                val totalSpent = paidInvoices.sumOf { it.paidAmount }

                // Update customer's total spent
                customerDao.updateTotalSpent(customerId, totalSpent)
            }
        }
    }

    // ✅ ADD THIS METHOD - Recalculate all customers
    suspend fun recalculateAllCustomersTotalSpent() {
        withContext(Dispatchers.IO) {
            val allCustomers = customerDao.getAllCustomersSync()

            allCustomers.forEach { customer ->
                val paidInvoices = invoiceDao.getPaidInvoicesByCustomer(customer.customerId)
                val totalSpent = paidInvoices.sumOf { it.paidAmount }
                customerDao.updateTotalSpent(customer.customerId, totalSpent)
            }
        }
    }
}