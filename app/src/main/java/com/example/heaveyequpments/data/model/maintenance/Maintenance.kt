package com.example.heaveyequpments.data.model.maintenance

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "maintenance_log")
data class Maintenance(
    @PrimaryKey(autoGenerate = true)
    val logId: Long = 0,
    val equipmentId: Long,
    val title: String,
    val startTime: Long,
    val description: String? = null,
    val endTime: Long? = null,
    val isClosed: Boolean = false

)
