package com.example.heaveyequpments.data.repository

import com.example.heaveyequpments.data.model.equipment.HeavyEquipments
import com.example.heaveyequpments.data.model.maintenance.InvoiceImage
import com.example.heaveyequpments.data.model.maintenance.Maintenance
import com.example.heaveyequpments.data.model.maintenance.MaintenanceLogWithDetails
import com.example.heaveyequpments.data.model.maintenance.Mechanic
import com.example.heaveyequpments.data.model.maintenance.OtherExpense
import com.example.heaveyequpments.data.model.maintenance.PartsChanged
import com.example.heaveyequpments.data.model.maintenance.PartsChangedImage
import kotlinx.coroutines.flow.Flow

interface MaintenanceLogRepository {
    fun getAllEquipments(): Flow<List<HeavyEquipments>>
    fun getAllMaintenanceLogsWithDetails(): Flow<List<MaintenanceLogWithDetails>>
    fun getActiveLogs(equipmentId: Long): Flow<List<MaintenanceLogWithDetails>>
    fun getLogDetails(logId: Long): Flow<MaintenanceLogWithDetails?>
    fun searchMaintenanceLogs(query: String): Flow<List<MaintenanceLogWithDetails>>
    fun getLogsByMachineAndDate(id: Long, start: Long, end: Long): Flow<List<MaintenanceLogWithDetails>>

    suspend fun saveNewMaintenanceLog(
        log: Maintenance,
        parts: List<PartsChanged>,
        expenses: List<OtherExpense>,
        mechanics: List<Mechanic>,
        partImages: List<PartsChangedImage>,
        invoiceImages: List<InvoiceImage>
    )

    suspend fun updateLogTransaction(
        log: Maintenance,
        parts: List<PartsChanged>,
        mechanics: List<Mechanic>,
        expenses: List<OtherExpense>,
        partImages: List<PartsChangedImage>,
        invoiceImages: List<InvoiceImage>
    )

    suspend fun deleteLog(log: Maintenance)
    suspend fun updateLog(log: Maintenance)
}