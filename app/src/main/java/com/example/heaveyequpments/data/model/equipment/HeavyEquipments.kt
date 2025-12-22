package com.example.heaveyequpments.data.model.equipment

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "heavy_equipments")
data class HeavyEquipments(
    @PrimaryKey(autoGenerate = true) val id: Long =0L,
    @ColumnInfo(name = "name") val name: String,
    @ColumnInfo(name = "description") val description: String,
    @ColumnInfo(name = "number") val number: Int,
    @ColumnInfo(name = "image") val image: String? = null
)