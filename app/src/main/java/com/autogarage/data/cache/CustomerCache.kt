package com.autogarage.data.cache

import android.util.LruCache
import com.autogarage.domain.model.Customer
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class CustomerCache @Inject constructor() {

    // ✅ OPTIMIZATION: Cache frequently accessed customers
    private val cache = LruCache<Long, Customer>(50) // Cache last 50 customers

    fun get(id: Long): Customer? = cache.get(id)

    fun put(id: Long, customer: Customer) {
        cache.put(id, customer)
    }

    fun remove(id: Long) {
        cache.remove(id)
    }

    fun clear() {
        cache.evictAll()
    }
}
