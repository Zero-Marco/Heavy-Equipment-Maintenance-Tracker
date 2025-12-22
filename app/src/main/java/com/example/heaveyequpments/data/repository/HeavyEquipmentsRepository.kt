package com.example.heaveyequpments.data.repository

import com.example.heaveyequpments.data.model.equipment.HeavyEquipments
import kotlinx.coroutines.flow.Flow

interface HeavyEquipmentsRepository {
    val allEquipments: Flow<List<HeavyEquipments>>
    fun searchEquipments(query: String): Flow<List<HeavyEquipments>>
    suspend fun insert(equipment: HeavyEquipments)
    suspend fun update(equipment: HeavyEquipments)
    suspend fun delete(equipment: HeavyEquipments)
    suspend fun deleteById(id: Long)
    suspend fun getById(id: Long): HeavyEquipments
}