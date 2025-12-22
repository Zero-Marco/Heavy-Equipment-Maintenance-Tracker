package com.example.heaveyequpments.ui.maintenance

import com.example.heaveyequpments.data.model.maintenance.MaintenanceLogWithDetails

// MaintenanceUiState.kt
sealed class MaintenanceUiState {
    object Loading : MaintenanceUiState()
    object Empty : MaintenanceUiState()
    data class Success(val logs: List<MaintenanceLogWithDetails>) : MaintenanceUiState()
    data class Error(val message: String) : MaintenanceUiState()
}

// AddEditMaintenanceUiState.kt
data class AddEditMaintenanceUiState(
    val isLoading: Boolean = false,
    val isEntrySaved: Boolean = false,
    val errorMessage: String? = null,
    val logDetails: MaintenanceLogWithDetails?=null
)