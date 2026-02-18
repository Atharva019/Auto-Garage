package com.autogarage.util

import android.util.Log
import kotlin.system.measureTimeMillis

object PerformanceMonitor {
    const val TAG = "PerformanceMonitor"

    inline fun <T> measureOperation(
        operationName: String,
        block: () -> T
    ): T {
        var result: T
        val time = measureTimeMillis {
            result = block()
        }

        // ✅ OPTIMIZATION: Log slow operations
        if (time > 500) {
            Log.w(TAG, "$operationName took ${time}ms (SLOW)")
        } else {
            Log.d(TAG, "$operationName took ${time}ms")
        }

        return result
    }

    suspend fun <T> measureSuspendOperation(
        operationName: String,
        block: suspend () -> T
    ): T {
        var result: T
        val time = measureTimeMillis {
            result = block()
        }

        if (time > 500) {
            Log.w(TAG, "$operationName took ${time}ms (SLOW)")
        } else {
            Log.d(TAG, "$operationName took ${time}ms")
        }

        return result
    }
}