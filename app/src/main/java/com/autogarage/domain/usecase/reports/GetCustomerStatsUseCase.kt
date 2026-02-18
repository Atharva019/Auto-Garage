package com.autogarage.domain.usecase.reports

import android.util.Log
import com.autogarage.domain.repository.CustomerRepository
import com.autogarage.domain.usecase.base.UseCase
import kotlinx.coroutines.Dispatchers
import javax.inject.Inject
import kotlinx.coroutines.flow.first

data class CustomerStats(
    val totalCustomers: Int,
    val newCustomers: Int, // in selected period
    val activeCustomers: Int, // had job cards in period
    val topCustomers: List<TopCustomer>,
    val customerRetentionRate: Double,
    val averageCustomerValue: Double,
    val totalLoyaltyPoints: Int
)

data class TopCustomer(
    val customerId: Long,
    val customerName: String,
    val totalSpent: Double,
    val jobCardCount: Int
)

class GetCustomerStatsUseCase @Inject constructor(
    private val customerRepository: CustomerRepository
) : UseCase<GetCustomerStatsUseCase.Params, CustomerStats>() {

    data class Params(
        val startDate: Long,
        val endDate: Long
    )

    override val dispatcher = Dispatchers.IO

    override suspend fun execute(params: Params): CustomerStats {
        val allCustomers = customerRepository.getAllCustomersSync() // Use sync version
        allCustomers.take(5).forEach { customer ->
            android.util.Log.d("CustomerStats",
                "Customer: ${customer.name}, TotalSpent: ${customer.totalSpent}")
        }

        val topCustomers = allCustomers
            .map { customer ->
                // ✅ Calculate REAL total spent from invoices
                val actualTotalSpent = customerRepository.getCustomerTotalSpent(customer.id)
                val jobCardCount = customerRepository.getCustomerJobCardCount(customer.id)

                // ✅ Update the customer record with actual spending
                if (actualTotalSpent != customer.totalSpent) {
                    customerRepository.updateCustomerTotalSpent(customer.id, actualTotalSpent)
                }

                Log.d("CustomerStats",
                    "Customer: ${customer.name}, " +
                            "Stored: ${customer.totalSpent}, " +
                            "Actual: $actualTotalSpent, " +
                            "Jobs: $jobCardCount"
                )

                TopCustomer(
                    customerId = customer.id,
                    customerName = customer.name,
                    totalSpent = actualTotalSpent, // ✅ Use REAL spending
                    jobCardCount = jobCardCount
                )
            }
            .filter { it.totalSpent > 0 } // ✅ Only show customers who have spent money
            .sortedByDescending { it.totalSpent }
            .take(10)

        // ✅ Calculate new customers in period
        val newCustomers = allCustomers.filter {
            it.createdAt >= params.startDate && it.createdAt <= params.endDate
        }

        // ✅ Get active customers (who had job cards in period)
        val activeCustomers = customerRepository.getActiveCustomersInPeriod(
            params.startDate,
            params.endDate
        )

        // ✅ Calculate average using REAL spending
        val totalRealSpending = topCustomers.sumOf { it.totalSpent }
        val averageCustomerValue = if (allCustomers.isNotEmpty()) {
            totalRealSpending / allCustomers.size
        } else 0.0

        val totalLoyaltyPoints = allCustomers.sumOf { it.loyaltyPoints }

        // ✅ Retention rate
        val retentionRate = if (allCustomers.isNotEmpty()) {
            (activeCustomers.size.toDouble() / allCustomers.size) * 100
        } else 0.0

        return CustomerStats(
            totalCustomers = allCustomers.size,
            newCustomers = newCustomers.size,
            activeCustomers = activeCustomers.size,
            topCustomers = topCustomers,
            customerRetentionRate = retentionRate,
            averageCustomerValue = averageCustomerValue,
            totalLoyaltyPoints = totalLoyaltyPoints
        )
    }
}