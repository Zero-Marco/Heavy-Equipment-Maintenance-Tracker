package com.example.heaveyequpments.data.model.maintenance

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.ForeignKey.Companion.CASCADE
import androidx.room.Index
import androidx.room.PrimaryKey


@Entity(
    tableName = "parts_changed",
    foreignKeys = [ForeignKey(
        entity = Maintenance::class,
        parentColumns = ["logId"],
        childColumns = ["logParentId"],
        onDelete = CASCADE
    )],
    indices = [Index(value = ["logParentId"])]
    )

data class PartsChanged(
    @PrimaryKey(autoGenerate = true)
    val partChangedId: Long = 0,
    val logParentId: Long,
    val name: String,
    val cost: Double? = null

)
