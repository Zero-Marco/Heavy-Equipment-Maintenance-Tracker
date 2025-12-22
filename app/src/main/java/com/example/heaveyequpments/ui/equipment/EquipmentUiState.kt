package com.example.heaveyequpments.ui.equipment

import com.example.heaveyequpments.data.model.equipment.HeavyEquipments

sealed class EquipmentUiState {
    object Loading : EquipmentUiState()
    object Empty : EquipmentUiState()
    data class Success(val equipments: List<HeavyEquipments>) : EquipmentUiState()
    data class Error(val message: String) : EquipmentUiState()
}