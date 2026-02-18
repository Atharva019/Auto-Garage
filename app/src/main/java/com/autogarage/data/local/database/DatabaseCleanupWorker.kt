package com.autogarage.data.local.database

import android.content.Context
import androidx.hilt.work.HiltWorker
import androidx.room.withTransaction
import androidx.work.*
import androidx.work.CoroutineWorker
import com.autogarage.data.local.dao.*
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import java.util.concurrent.TimeUnit

@HiltWorker
class DatabaseCleanupWorker @AssistedInject constructor(
    @Assisted context: Context,
    @Assisted params: WorkerParameters,
    private val jobCardDao: JobCardDao,
    private val database: GarageMasterDatabase
) : CoroutineWorker(context, params) {

    override suspend fun doWork(): Result {
        return try {
            // ✅ OPTIMIZATION: Clean up old data periodically
            val sixMonthsAgo = System.currentTimeMillis() - (180L * 24 * 60 * 60 * 1000)

            // Delete old completed job cards
            jobCardDao.deleteOldJobCards(sixMonthsAgo)

            // Clean up orphaned records
            // ... add cleanup logic

            database.withTransaction {
                // Clean up orphaned services (services without job cards)
                database.openHelper.writableDatabase.execSQL(
                    """
                    DELETE FROM job_card_services 
                    WHERE jobCardId NOT IN (SELECT jobCardId FROM job_cards)
                    """
                )

                // Clean up orphaned parts (parts without job cards)
                database.openHelper.writableDatabase.execSQL(
                    """
                    DELETE FROM job_card_parts 
                    WHERE jobCardId NOT IN (SELECT jobCardId FROM job_cards)
                    """
                )

                // Clean up orphaned invoices (invoices without job cards)
                database.openHelper.writableDatabase.execSQL(
                    """
                    DELETE FROM invoices 
                    WHERE jobCardId NOT IN (SELECT jobCardId FROM job_cards)
                    """
                )
            }

            Result.success()
        } catch (e: Exception) {
            Result.retry()
        }
    }

    companion object {
        fun schedule(context: Context) {
            val constraints = Constraints.Builder()
                .setRequiresBatteryNotLow(true)
                .setRequiresDeviceIdle(true)
                .build()

            val cleanupRequest = PeriodicWorkRequestBuilder<DatabaseCleanupWorker>(
                7, TimeUnit.DAYS // Run weekly
            )
                .setConstraints(constraints)
                .build()

            WorkManager.getInstance(context)
                .enqueueUniquePeriodicWork(
                    "database_cleanup",
                    ExistingPeriodicWorkPolicy.KEEP,
                    cleanupRequest
                )
        }
    }
}