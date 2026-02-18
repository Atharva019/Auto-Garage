package com.autogarage.data.local.database

import android.view.View.X
import android.view.View.Y
import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.autogarage.data.local.dao.*
import com.autogarage.data.local.entity.*

@Database(
    entities = [
        CustomerEntity::class,
        VehicleEntity::class,
        JobCardEntity::class,
        ServiceEntity::class,
        JobCardServiceEntity::class,
        InventoryItemEntity::class,
        JobCardPartEntity::class,
        InvoiceEntity::class,
        PaymentEntity::class,
        WorkerEntity::class,
        AttendanceEntity::class,
        SupplierEntity::class,
        StockTransactionEntity::class,
        ExpenseEntity::class
    ],
    version = 7,
    exportSchema = false
)
abstract class GarageMasterDatabase : RoomDatabase() {
    abstract fun customerDao(): CustomerDao
    abstract fun vehicleDao(): VehicleDao
    abstract fun jobCardDao(): JobCardDao
    abstract fun inventoryDao(): InventoryDao
    abstract fun invoiceDao(): InvoiceDao
    abstract fun workerDao(): WorkerDao
    abstract fun jobCardServiceDao(): JobCardServiceDao
    abstract fun jobCardPartDao(): JobCardPartDao
    //abstract fun inventoryItemDao(): InventoryItemDao
    abstract fun serviceDao(): ServiceDao

    companion object {
        const val DATABASE_NAME = "garage_master_db"
    }
}
val MIGRATION_X_Y = object : Migration(6, 7) {
    override fun migrate(database: SupportSQLiteDatabase) {
        // Add customerId column to invoices table
        database.execSQL(
            "ALTER TABLE invoices ADD COLUMN customerId INTEGER NOT NULL DEFAULT 0"
        )
8
        // Create foreign key by recreating table (SQLite doesn't support adding FK)
        // This is a simplified version - you may need to preserve existing data
    }
}