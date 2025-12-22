package com.example.heaveyequpments.ui.equipment

import com.example.heaveyequpments.data.model.equipment.HeavyEquipments

data class AddEditUiState(val isLoading: Boolean = false,
                               val isEntrySaved: Boolean = false,
                               val errorMessage: String? = null,
                               val initialEquipment: HeavyEquipments? = null)
