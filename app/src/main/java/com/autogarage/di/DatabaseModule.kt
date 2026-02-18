package com.autogarage.di

import android.content.Context
import androidx.room.Room
import androidx.room.RoomDatabase
import com.autogarage.data.local.database.GarageMasterDatabase
import com.autogarage.data.local.dao.*
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import java.util.concurrent.Executors
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides
    @Singleton
    fun provideGarageMasterDatabase(
        @ApplicationContext context: Context
    ): GarageMasterDatabase {
        return Room.databaseBuilder(
            context,
            GarageMasterDatabase::class.java,
            "garage_master_database"
        )
        // ✅ OPTIMIZATION 1: Enable Write-Ahead Logging (WAL)
        // This allows concurrent reads and writes, significantly improving performance
            .setJournalMode(RoomDatabase.JournalMode.WRITE_AHEAD_LOGGING)

            // ✅ OPTIMIZATION 2: Increase query thread pool
            .setQueryExecutor(Executors.newFixedThreadPool(4))

            // ✅ OPTIMIZATION 3: Set transaction executor
            .setTransactionExecutor(Executors.newSingleThreadExecutor())
            // Remove this in production - it deletes all data on schema changes
            .fallbackToDestructiveMigration()
            .build()
    }

    // ========== Core DAOs ==========

    @Provides
    @Singleton
    fun provideCustomerDao(database: GarageMasterDatabase): CustomerDao {
        return database.customerDao()
    }

    @Provides
    @Singleton
    fun provideVehicleDao(database: GarageMasterDatabase): VehicleDao {
        return database.vehicleDao()
    }

    @Provides
    @Singleton
    fun provideJobCardDao(database: GarageMasterDatabase): JobCardDao {
        return database.jobCardDao()
    }

    @Provides
    @Singleton
    fun provideWorkerDao(database: GarageMasterDatabase): WorkerDao {
        return database.workerDao()
    }

    @Provides
    @Singleton
    fun provideInvoiceDao(database: GarageMasterDatabase): InvoiceDao {
        return database.invoiceDao()
    }

    // ========== Service & Part DAOs ==========

    @Provides
    @Singleton
    fun provideJobCardServiceDao(database: GarageMasterDatabase): JobCardServiceDao {
        return database.jobCardServiceDao()
    }

    @Provides
    @Singleton
    fun provideJobCardPartDao(database: GarageMasterDatabase): JobCardPartDao {
        return database.jobCardPartDao()
    }

    @Provides
    @Singleton
    fun provideInventoryDao(database: GarageMasterDatabase): InventoryDao {
        return database.inventoryDao()
    }
}
