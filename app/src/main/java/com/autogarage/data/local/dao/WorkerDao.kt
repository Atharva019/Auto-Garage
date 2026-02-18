package com.autogarage.data.local.dao

import androidx.room.*
import com.autogarage.data.local.entity.WorkerEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface WorkerDao {
    @Query("SELECT * FROM workers WHERE status = 'ACTIVE' ORDER BY name ASC")
    suspend fun getAllWorkersSync(): List<WorkerEntity>
    @Query("SELECT * FROM workers ORDER BY name ASC")
    fun getAllWorkers(): Flow<List<WorkerEntity>>

    @Query("SELECT * FROM workers WHERE status = 'ACTIVE' ORDER BY name ASC")
    fun getActiveWorkers(): Flow<List<WorkerEntity>>

    @Query("SELECT * FROM workers WHERE id = :workerId")
     fun getWorkerById(workerId: Long): WorkerEntity?

    // In WorkerDao.kt
    @Query("SELECT * FROM workers WHERE id = :workerId")
    fun getWorkerByIdFlow(workerId: Long): Flow<WorkerEntity?>

    @Query("""
        SELECT * FROM workers 
        WHERE name LIKE '%' || :query || '%' 
        OR phone LIKE '%' || :query || '%'
        OR role LIKE '%' || :query || '%'
        ORDER BY name ASC
    """)
    fun searchWorkers(query: String): Flow<List<WorkerEntity>>

    @Query("SELECT * FROM workers WHERE role = :role AND status = 'ACTIVE'")
    fun getWorkersByRole(role: String): Flow<List<WorkerEntity>>

    @Query("SELECT COUNT(*) FROM workers WHERE status = 'ACTIVE'")
    fun getActiveWorkersCount(): Flow<Int>

    @Query("SELECT AVG(salary) FROM workers WHERE status = 'ACTIVE'")
    suspend fun getAverageSalary(): Double?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertWorker(worker: WorkerEntity): Long

    @Update
    suspend fun updateWorker(worker: WorkerEntity)

    @Delete
    suspend fun deleteWorker(worker: WorkerEntity)

    @Query("UPDATE workers SET status = :status WHERE id = :workerId")
    suspend fun updateWorkerStatus(workerId: Long, status: String)

    @Query("UPDATE workers SET rating = :rating, completedJobs = completedJobs + 1 WHERE id = :workerId")
    suspend fun updateWorkerStats(workerId: Long, rating: Double)

    @Query("SELECT * FROM workers WHERE id = :id")
    suspend fun getWorkerByIdSync(id: Long): WorkerEntity?
}
