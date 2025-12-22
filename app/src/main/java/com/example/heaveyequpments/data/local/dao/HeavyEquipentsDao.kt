package com.example.heaveyequpments.data.local.dao

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import com.example.heaveyequpments.data.model.equipment.HeavyEquipments
import kotlinx.coroutines.flow.Flow

@Dao
interface HeavyEquipentsDao {

    @Insert
    suspend fun insert(heavyEquipments: HeavyEquipments)

    @Delete
    suspend fun delete(heavyEquipments: HeavyEquipments)

    @Query("SELECT * FROM heavy_equipments")
    fun readAllData(): LiveData<List<HeavyEquipments>>

    @Query("SELECT * FROM heavy_equipments WHERE name LIKE :searchQuery OR number LIKE :searchQuery")
    fun searchEquipments(searchQuery: String): Flow<List<HeavyEquipments>>

    @Query("SELECT * FROM heavy_equipments")
    fun getAll(): List<HeavyEquipments>
    @Query("SELECT * FROM heavy_equipments")
    fun getAllEquipments(): Flow<List<HeavyEquipments>>

    @Query("SELECT * FROM heavy_equipments WHERE id = :id")
    fun getById(id: Long): HeavyEquipments

    @Query("DELETE FROM heavy_equipments WHERE id = :id")
    fun deleteById(id: Long)
    @Update
    fun update(heavyEquipments: HeavyEquipments)


}