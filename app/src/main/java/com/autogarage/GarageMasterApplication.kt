package com.autogarage

import android.app.Application
import dagger.hilt.android.HiltAndroidApp
//import timber.log.Timber
import androidx.room.RoomDatabase
import com.autogarage.di.DatabaseMaintenance
import com.autogarage.worker.SyncCustomerSpendingWorker
import jakarta.inject.Inject

@HiltAndroidApp
class GarageMasterApplication : Application(){
    @Inject
    lateinit var databaseMaintenance: DatabaseMaintenance
    override fun onCreate() {
        super.onCreate()

        // ✅ Schedule background sync of customer spending
        SyncCustomerSpendingWorker.schedule(this)
        // Perform maintenance on app startup (background thread)
        databaseMaintenance.performMaintenance()

        // ✅ OPTIMIZATION: Enable strict mode in debug builds to catch issues
//        if (BuildConfig.DEBUG) {
//            Timber.plant(Timber.DebugTree())
//
//            // Enable strict mode for detecting slow operations on main thread
//            StrictMode.setThreadPolicy(
//                StrictMode.ThreadPolicy.Builder()
//                    .detectDiskReads()
//                    .detectDiskWrites()
//                    .detectNetwork()
//                    .penaltyLog()
//                    .build()
//            )
//        }
    }
}