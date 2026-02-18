package com.autogarage.di

import com.autogarage.domain.repository.CustomerRepository
import com.autogarage.domain.repository.CustomerRepositoryImpl
import com.autogarage.domain.repository.InventoryRepository
import com.autogarage.domain.repository.InventoryRepositoryImpl
import com.autogarage.domain.repository.InvoiceRepository
import com.autogarage.domain.repository.InvoiceRepositoryImpl
import com.autogarage.domain.repository.JobCardRepository
import com.autogarage.domain.repository.JobCardRepositoryImpl
import com.autogarage.domain.repository.SettingsRepository
import com.autogarage.domain.repository.SettingsRepositoryImpl
import com.autogarage.domain.repository.VehicleRepository
import com.autogarage.domain.repository.VehicleRepositoryImpl
import com.autogarage.domain.repository.WorkerRepository
import com.autogarage.domain.repository.WorkerRepositoryImpl
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton
@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryModule {
    @Binds
    @Singleton
    abstract fun bindCustomerRepository(
        impl: CustomerRepositoryImpl
    ): CustomerRepository

    @Binds
    @Singleton
    abstract fun bindVehicleRepository(
        impl: VehicleRepositoryImpl
    ): VehicleRepository

    @Binds
    @Singleton
    abstract fun bindJobCardRepository(
        impl: JobCardRepositoryImpl
    ): JobCardRepository

    @Binds
    @Singleton
    abstract fun bindInventoryRepository(
        impl: InventoryRepositoryImpl
    ): InventoryRepository

    @Binds
    @Singleton
    abstract fun bindWorkerRepository(
        impl: WorkerRepositoryImpl
    ): WorkerRepository

    @Binds
    @Singleton
    abstract fun bindSettingsRepository(
        settingsRepositoryImpl: SettingsRepositoryImpl
    ): SettingsRepository

    @Binds
    @Singleton
    abstract fun bindInvoiceRepository(
        invoiceRepositoryImpl: InvoiceRepositoryImpl
    ): InvoiceRepository
}