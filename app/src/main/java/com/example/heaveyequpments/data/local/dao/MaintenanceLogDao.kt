package com.example.heaveyequpments.data.local.dao

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Update
import com.example.heaveyequpments.data.model.maintenance.InvoiceImage
import com.example.heaveyequpments.data.model.maintenance.Maintenance
import com.example.heaveyequpments.data.model.maintenance.MaintenanceLogWithDetails
import com.example.heaveyequpments.data.model.maintenance.Mechanic
import com.example.heaveyequpments.data.model.maintenance.OtherExpense
import com.example.heaveyequpments.data.model.maintenance.PartsChanged
import com.example.heaveyequpments.data.model.maintenance.PartsChangedImage
import kotlinx.coroutines.flow.Flow

@Dao
interface MaintenanceLogDao {
    // A. Main Log
    @Insert(onConflict = OnConflictStrategy.Companion.REPLACE)
    suspend fun insertLog(log: Maintenance): Long // Returns the new logId

    @Transaction
    @Query("SELECT * FROM maintenance_log ORDER BY startTime DESC")
    fun getAllMaintenanceLogsWithDetails(): Flow<List<MaintenanceLogWithDetails>>
    // B. Supporting data (use a variable-argument list to save multiple at once)
    @Insert(onConflict = OnConflictStrategy.Companion.REPLACE)
    suspend fun insertPartsChanged(parts: List<PartsChanged>)

    @Insert(onConflict = OnConflictStrategy.Companion.REPLACE)
    suspend fun insertOtherExpenses(expenses: List<OtherExpense>)

    // C. Many-to-Many Join Table
    @Insert(onConflict = OnConflictStrategy.Companion.REPLACE)
    suspend fun insertMechanicEntries(entries: List<Mechanic>)

    // D. Images
    @Insert(onConflict = OnConflictStrategy.Companion.REPLACE)
    suspend fun insertPartsChangedImages(images: List<PartsChangedImage>)

    @Insert(onConflict = OnConflictStrategy.Companion.REPLACE)
    suspend fun insertInvoiceImages(images: List<InvoiceImage>)
    @Transaction
    @Query("""
    SELECT maintenance_log.* FROM maintenance_log 
    INNER JOIN heavy_equipments ON maintenance_log.equipmentId = heavy_equipments.id 
    WHERE maintenance_log.title LIKE '%' || :searchQuery || '%' 
    OR heavy_equipments.name LIKE '%' || :searchQuery || '%' 
    OR heavy_equipments.number LIKE '%' || :searchQuery || '%'
""")
    fun searchMaintenanceLogs(searchQuery: String): Flow<List<MaintenanceLogWithDetails>>

    // --- 2. QUERY METHODS (Retrieving the Data) ---

    // A. Get a SINGLE Log with ALL Details (using the POJO)
    // @Transaction ensures the entire operation is atomic (for querying the relations)
    @Transaction
    @Query("SELECT * FROM maintenance_log WHERE logId = :logId")
    fun getLogWithDetails(logId: Long): Flow<MaintenanceLogWithDetails?> // Use Flow for observing changes

    // B. Get ALL logs for a specific equipment (for the main list/history)
    @Transaction
    @Query("SELECT * FROM maintenance_log WHERE equipmentId = :equipmentId ORDER BY startTime DESC")
    fun getLogsForEquipment(equipmentId: Long): Flow<List<MaintenanceLogWithDetails>>

    // C. Get only the active (unclosed) logs for a specific equipment
    @Transaction
    @Query("SELECT * FROM maintenance_log WHERE equipmentId = :equipmentId AND isClosed = 0 ORDER BY startTime DESC")
    fun getActiveLogsForEquipment(equipmentId: Long): Flow<List<MaintenanceLogWithDetails>>

    @Transaction
    @Query("""
    SELECT * FROM maintenance_log
    WHERE equipmentId = :equipmentId
    AND isClosed = 0
    ORDER BY startTime DESC
""")
    fun getActiveLogs(equipmentId: Long): Flow<List<MaintenanceLogWithDetails>>

    // --- 3. UPDATE/DELETE METHODS ---

    // A. Simple Update (e.g., closing the log by setting endTime and isClosed)
    @Update
    suspend fun updateLog(log: Maintenance)
    @Query("DELETE FROM parts_changed WHERE logParentId = :logId")
    suspend fun deletePartsByLogId(logId: Long)
    @Query("DELETE FROM mechanic_entries WHERE  logParentId= :logId")
    suspend fun deleteMechanicsByLogId(logId: Long)

    @Query("DELETE FROM other_expenses WHERE logParentId = :logId")
    suspend fun deleteExpensesByLogId(logId: Long)
    // NEW QUERIES FOR IMAGE DELETION:
    @Query("DELETE FROM parts_changed_image WHERE logParentId = :logId")
    suspend fun deletePartsChangedImagesByLogId(logId: Long)

    @Query("DELETE FROM invoice_image WHERE logParentId = :logId")
    suspend fun deleteInvoiceImagesByLogId(logId: Long)

    @Delete
    suspend fun deleteLog(log: Maintenance)

    @Transaction
    @Query("SELECT * FROM maintenance_log WHERE startTime >= :start AND startTime <= :end")
    fun getLogsByDateRange(start: Long, end: Long): LiveData<List<MaintenanceLogWithDetails>>
    @Transaction
    @Query("""
    SELECT * FROM maintenance_log
    WHERE equipmentId = :equipId 
    AND startTime >= :startDate 
    AND startTime <= :endDate 
    ORDER BY startTime DESC
""")
    fun getMachineHistory(equipId: Long, startDate: Long, endDate: Long): List<MaintenanceLogWithDetails>
    @Transaction
    @Query("SELECT * FROM maintenance_log WHERE equipmentId = :id AND startTime BETWEEN :start AND :end ORDER BY startTime DESC")
    fun getLogsForMachineInDateRange(id: Long, start: Long, end: Long): LiveData<List<MaintenanceLogWithDetails>>
    @Transaction
    @Query("""
    SELECT * FROM maintenance_log 
    WHERE equipmentId = :id 
    AND startTime BETWEEN :start AND :end 
    ORDER BY startTime DESC
""")
    fun getLogsForMachineInDateRangeFlow(id: Long, start: Long, end: Long): Flow<List<MaintenanceLogWithDetails>>
}