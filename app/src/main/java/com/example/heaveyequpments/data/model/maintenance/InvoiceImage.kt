package com.example.heaveyequpments.data.model.maintenance

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.ForeignKey.Companion.CASCADE
import androidx.room.Index
import androidx.room.PrimaryKey


@Entity(tableName="invoice_image",
    foreignKeys = [ForeignKey(
        entity = Maintenance::class,
        parentColumns = ["logId"],
        childColumns = ["logParentId"],
        onDelete = CASCADE
    )],
    indices = [Index(value = ["logParentId"])]
)
data class InvoiceImage(
    @PrimaryKey(autoGenerate = true)
    val invoiceImageId: Long = 0,
    val logParentId: Long,
    val imagePath: String,
    val description: String? = null
)
