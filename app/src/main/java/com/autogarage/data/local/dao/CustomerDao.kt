package com.autogarage.data.local.dao

import androidx.paging.PagingSource
import androidx.room.*
import com.autogarage.data.local.entity.CustomerEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface CustomerDao {
    @Query("""
    SELECT DISTINCT c.* FROM customers c
    INNER JOIN vehicles v ON c.customerId = v.customerId
    INNER JOIN job_cards j ON v.vehicleId = j.vehicleId
    WHERE j.createdAt >= :startDate AND j.createdAt <= :endDate
    ORDER BY c.name ASC
""")
    suspend fun getActiveCustomersInPeriod(startDate: Long, endDate: Long): List<CustomerEntity>

    @Query("SELECT * FROM customers ORDER BY totalSpent DESC LIMIT :limit")
    suspend fun getTopCustomersByRevenue(limit: Int): List<CustomerEntity>

    @Query("SELECT vehicleId FROM vehicles WHERE customerId = :customerId")
    suspend fun getCustomerVehicleIds(customerId: Long): List<Long>

    // ✅ ADD THIS METHOD - Get job card count for a customer
    @Query("""
        SELECT COUNT(*) 
        FROM job_cards jc
        INNER JOIN vehicles v ON jc.vehicleId = v.vehicleId
        WHERE v.customerId = :customerId
    """)
    suspend fun getCustomerJobCardCount(customerId: Long): Int

    // ✅ OPTIMIZATION: Use Paging for large customer lists
    @Query("SELECT * FROM customers ORDER BY name ASC")
    fun getCustomersPaged(): PagingSource<Int, CustomerEntity>

    // ✅ OPTIMIZATION: Add LIMIT to queries
    @Query("SELECT * FROM customers WHERE name LIKE '%' || :query || '%' OR phone LIKE '%' || :query || '%' LIMIT 50")
    fun searchCustomers(query: String): Flow<List<CustomerEntity>>
    @Query("SELECT * FROM customers ORDER BY name ASC")
    fun getAllCustomers(): Flow<List<CustomerEntity>>
    @Query("SELECT * FROM customers ORDER BY totalSpent DESC")
    suspend fun getAllCustomersSync(): List<CustomerEntity>
    @Query("SELECT * FROM customers WHERE customerId = :customerId")
    fun getCustomerById(customerId: Long): Flow<CustomerEntity?>

    @Query("SELECT * FROM customers WHERE phone = :phone")
    suspend fun getCustomerByPhone(phone: String): CustomerEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCustomer(customer: CustomerEntity): Long

    @Update
    suspend fun updateCustomer(customer: CustomerEntity)

    @Delete
    suspend fun deleteCustomer(customer: CustomerEntity)

    @Query("UPDATE customers SET totalSpent = totalSpent + :amount WHERE customerId = :customerId")
    suspend fun updateTotalSpent(customerId: Long, amount: Double)

    @Query("UPDATE customers SET totalSpent = :totalSpent, updatedAt = :updatedAt WHERE customerId = :customerId")
    suspend fun updateCustomerTotalSpent(
        customerId: Long,
        totalSpent: Double,
        updatedAt: Long = System.currentTimeMillis()
    )

    @Query("SELECT * FROM customers WHERE customerId = :id")
    suspend fun getCustomerByIdSync(id: Long): CustomerEntity?

    @Query("SELECT * FROM customers ORDER BY createdAt DESC LIMIT :limit")
    fun getRecentCustomers(limit: Int): Flow<List<CustomerEntity>>

}