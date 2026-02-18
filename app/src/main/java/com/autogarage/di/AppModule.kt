package com.autogarage.di

import android.content.Context
import android.content.SharedPreferences
import com.autogarage.domain.repository.*
import com.autogarage.domain.repository.InvoiceRepository
import com.autogarage.domain.repository.JobCardRepository
import com.autogarage.domain.repository.SettingsRepository
import com.autogarage.domain.usecase.reports.*
import com.autogarage.domain.usecase.reports.GetJobCardStatsUseCase
import com.autogarage.domain.usecase.reports.GetRevenueReportUseCase
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    @Provides
    @Singleton
    fun provideSharedPreferences(@ApplicationContext context: Context): SharedPreferences {
        return context.getSharedPreferences("your_preference_file_name", Context.MODE_PRIVATE)
    }

    @Provides
    fun provideGetRevenueReportUseCase(
        invoiceRepository: InvoiceRepository,
        settingsRepository: SettingsRepository
    ) = GetRevenueReportUseCase(invoiceRepository)//, settingsRepository)

    @Provides
    fun provideGetJobCardStatsUseCase(
        jobCardRepository: JobCardRepository
    ) = GetJobCardStatsUseCase(jobCardRepository)

    @Provides
    fun provideGetCustomerStatsUseCase(
        customerRepository: CustomerRepository
    ) = GetCustomerStatsUseCase(customerRepository)

    @Provides
    fun provideGetInventoryStatsUseCase(
        inventoryRepository: InventoryRepository
    ) = GetInventoryStatsUseCase(inventoryRepository)

    @Provides
    fun provideGetWorkerPerformanceUseCase(
        workerRepository: WorkerRepository,
        jobCardRepository: JobCardRepository
    ) = GetWorkerPerformanceUseCase(workerRepository, jobCardRepository)

    @Provides
    fun provideGetDashboardSummaryUseCase(
        getRevenueReportUseCase: GetRevenueReportUseCase,
        getJobCardStatsUseCase: GetJobCardStatsUseCase,
        getCustomerStatsUseCase: GetCustomerStatsUseCase,
        getInventoryStatsUseCase: GetInventoryStatsUseCase
    ) = GetDashboardSummaryUseCase(
        getRevenueReportUseCase,
        getJobCardStatsUseCase,
        getCustomerStatsUseCase,
        getInventoryStatsUseCase
    )
}
