package com.example.heaveyequpments.data.repository

import android.util.Log
import com.example.heaveyequpments.data.local.dao.HeavyEquipentsDao
import com.example.heaveyequpments.data.local.dao.MaintenanceLogDao
import com.example.heaveyequpments.data.model.maintenance.InvoiceImage
import com.example.heaveyequpments.data.model.maintenance.Maintenance
import com.example.heaveyequpments.data.model.maintenance.MaintenanceLogWithDetails
import com.example.heaveyequpments.data.model.maintenance.Mechanic
import com.example.heaveyequpments.data.model.maintenance.OtherExpense
import com.example.heaveyequpments.data.model.maintenance.PartsChanged
import com.example.heaveyequpments.data.model.maintenance.PartsChangedImage
import com.example.heaveyequpments.data.model.equipment.HeavyEquipments
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext
import javax.inject.Inject


class MaintenanceLogRepositoryImpl @Inject constructor(
    private val logDao: MaintenanceLogDao,
    private val heavyEquipmentsDao: HeavyEquipentsDao
) : MaintenanceLogRepository {


    override fun getAllEquipments(): Flow<List<HeavyEquipments>> = heavyEquipmentsDao.getAllEquipments()

    override fun getAllMaintenanceLogsWithDetails(): Flow<List<MaintenanceLogWithDetails>> =
        logDao.getAllMaintenanceLogsWithDetails()

    override fun getActiveLogs(equipmentId: Long): Flow<List<MaintenanceLogWithDetails>> =
        logDao.getActiveLogs(equipmentId)

    override fun getLogDetails(logId: Long): Flow<MaintenanceLogWithDetails?> =
        logDao.getLogWithDetails(logId)

    override fun searchMaintenanceLogs(query: String): Flow<List<MaintenanceLogWithDetails>> =
        logDao.searchMaintenanceLogs(query)

    override fun getLogsByMachineAndDate(id: Long, start: Long, end: Long): Flow<List<MaintenanceLogWithDetails>> {
        return logDao.getLogsForMachineInDateRangeFlow(id, start, end)
    }



    override suspend fun saveNewMaintenanceLog(
        log: Maintenance,
        parts: List<PartsChanged>,
        expenses: List<OtherExpense>,
        mechanics: List<Mechanic>,
        partImages: List<PartsChangedImage>,
        invoiceImages: List<InvoiceImage>
    ) {

        withContext(Dispatchers.IO) {
            try {
                val newLogId = logDao.insertLog(log.copy(logId = 0L))

                if (parts.isNotEmpty()) logDao.insertPartsChanged(parts.map { it.copy(logParentId = newLogId) })
                if (expenses.isNotEmpty()) logDao.insertOtherExpenses(expenses.map {
                    it.copy(
                        logParentId = newLogId
                    )
                })
                if (mechanics.isNotEmpty()) logDao.insertMechanicEntries(mechanics.map {
                    it.copy(
                        logParentId = newLogId
                    )
                })
                if (partImages.isNotEmpty()) logDao.insertPartsChangedImages(partImages.map {
                    it.copy(
                        logParentId = newLogId
                    )
                })
                if (invoiceImages.isNotEmpty()) logDao.insertInvoiceImages(invoiceImages.map {
                    it.copy(
                        logParentId = newLogId
                    )
                })

                Log.d("MaintenanceRepo", "Saved log ID: $newLogId")
            } catch (e: Exception) {
                throw e
            }
        }
    }

    override suspend fun updateLogTransaction(
        log: Maintenance,
        parts: List<PartsChanged>,
        mechanics: List<Mechanic>,
        expenses: List<OtherExpense>,
        partImages: List<PartsChangedImage>,
        invoiceImages: List<InvoiceImage>
    ) = withContext(Dispatchers.IO) {
        val logId = log.logId
        logDao.updateLog(log)

        // Clear old data
        logDao.deletePartsByLogId(logId)
        logDao.deleteMechanicsByLogId(logId)
        logDao.deleteExpensesByLogId(logId)
        logDao.deletePartsChangedImagesByLogId(logId)
        logDao.deleteInvoiceImagesByLogId(logId)

        // Insert new data
        logDao.insertPartsChanged(parts.map { it.copy(logParentId = logId) })
        logDao.insertMechanicEntries(mechanics.map { it.copy(logParentId = logId) })
        logDao.insertOtherExpenses(expenses.map { it.copy(logParentId = logId) })
        logDao.insertPartsChangedImages(partImages.map { it.copy(logParentId = logId) })
        logDao.insertInvoiceImages(invoiceImages.map { it.copy(logParentId = logId) })
    }

    override suspend fun deleteLog(log: Maintenance) = logDao.deleteLog(log)

    // Simple update for closing logs
    override suspend fun updateLog(log: Maintenance) = logDao.updateLog(log)
}