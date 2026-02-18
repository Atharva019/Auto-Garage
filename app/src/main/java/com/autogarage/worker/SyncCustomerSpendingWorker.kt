package com.autogarage.worker

import android.content.Context
import androidx.hilt.work.HiltWorker
import androidx.work.*
import com.autogarage.domain.repository.CustomerRepository
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.util.concurrent.TimeUnit

@HiltWorker
class SyncCustomerSpendingWorker @AssistedInject constructor(
    @Assisted context: Context,
    @Assisted params: WorkerParameters,
    private val customerRepository: CustomerRepository
) : CoroutineWorker(context, params) {

    override suspend fun doWork(): Result {
        return withContext(Dispatchers.IO) {
            try {
                // Get all customers
                val customers = customerRepository.getAllCustomersSync()

                // Update spending for each customer
                customers.forEach { customer ->
                    val actualSpent = customerRepository.getCustomerTotalSpent(customer.id)
                    if (actualSpent != customer.totalSpent) {
                        customerRepository.updateCustomerTotalSpent(customer.id, actualSpent)
                    }
                }

                Result.success()
            } catch (e: Exception) {
                Result.retry()
            }
        }
    }

    companion object {
        const val WORK_NAME = "sync_customer_spending"

        fun schedule(context: Context) {
            val constraints = Constraints.Builder()
                .setRequiresBatteryNotLow(true)
                .build()

            val syncRequest = PeriodicWorkRequestBuilder<SyncCustomerSpendingWorker>(
                1, TimeUnit.DAYS // Run daily
            )
                .setConstraints(constraints)
                .build()

            WorkManager.getInstance(context)
                .enqueueUniquePeriodicWork(
                    WORK_NAME,
                    ExistingPeriodicWorkPolicy.KEEP,
                    syncRequest
                )
        }
    }
}
