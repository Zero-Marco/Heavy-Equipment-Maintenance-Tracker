package com.example.heaveyequpments.data.repository

import com.example.heaveyequpments.data.local.dao.HeavyEquipentsDao
import com.example.heaveyequpments.data.model.equipment.HeavyEquipments
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject



class HeavyEquipmentsRepositoryImpl @Inject constructor(
    private val heavyEquipmentsDao: HeavyEquipentsDao
) : HeavyEquipmentsRepository {

    override val allEquipments: Flow<List<HeavyEquipments>> =
        heavyEquipmentsDao.getAllEquipments()

    override fun searchEquipments(query: String): Flow<List<HeavyEquipments>> {
        return heavyEquipmentsDao.searchEquipments(query)
    }

    override suspend fun insert(equipment: HeavyEquipments) {
        heavyEquipmentsDao.insert(equipment)
    }

    override suspend fun update(equipment: HeavyEquipments) {
        heavyEquipmentsDao.update(equipment)
    }

    override suspend fun delete(equipment: HeavyEquipments) {
        heavyEquipmentsDao.delete(equipment)
    }

    override suspend fun deleteById(id: Long) {
        heavyEquipmentsDao.deleteById(id)
    }

    override suspend fun getById(id: Long): HeavyEquipments {
        return heavyEquipmentsDao.getById(id)
    }
}