package com.autogarage.data.local.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "attendance",
    foreignKeys = [
        ForeignKey(
            entity = WorkerEntity::class,
            parentColumns = ["id"],
            childColumns = ["workerId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [
        Index(value = ["workerId"]),
        Index(value = ["date"])
    ]
)
data class AttendanceEntity(
    @PrimaryKey(autoGenerate = true)
    val attendanceId: Long = 0,
    val workerId: Long,
    val date: String, // Format: yyyy-MM-dd
    val checkInTime: Long? = null,
    val checkOutTime: Long? = null,
    val status: String, // PRESENT, ABSENT, HALF_DAY, LEAVE, HOLIDAY
    val notes: String? = null,
    val createdAt: Long = System.currentTimeMillis()
)