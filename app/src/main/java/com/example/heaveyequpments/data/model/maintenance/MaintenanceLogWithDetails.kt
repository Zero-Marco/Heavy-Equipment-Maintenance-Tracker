package com.example.heaveyequpments.data.model.maintenance

import androidx.room.Embedded
import androidx.room.Relation
import com.example.heaveyequpments.data.model.equipment.HeavyEquipments

data class MaintenanceLogWithDetails(
    @Embedded
    val maintenance: Maintenance,

    @Relation(
        parentColumn = "equipmentId", // The ID in MaintenanceLog
        entityColumn = "id"           // The ID in HeavyEquipents
    )
    val equipment: HeavyEquipments?,
    @Relation(
        parentColumn = "logId",
        entityColumn = "logParentId"
            ) val partsChanged: List<PartsChanged>,
    @Relation(
        parentColumn = "logId",
        entityColumn = "logParentId")
    val otherExpenses: List<OtherExpense>,
    @Relation(
        parentColumn = "logId",
        entityColumn = "logParentId")
    val mechanic: List<Mechanic>,
    @Relation(
        parentColumn = "logId",
        entityColumn = "logParentId")
    val partsChangedImage: List<PartsChangedImage>,
    @Relation(
        parentColumn = "logId",
        entityColumn = "logParentId")
    val invoiceImage: List<InvoiceImage>,

    ){
    val totalCost: Double
        get() {
            val partsSum = partsChanged.sumOf { it.cost ?: 0.0 }
            val mechanicSum = mechanic.sumOf { it.cost ?: 0.0 }
            val expensesSum = otherExpenses.sumOf { it.cost ?: 0.0 }
            return partsSum + mechanicSum + expensesSum
        }

}

