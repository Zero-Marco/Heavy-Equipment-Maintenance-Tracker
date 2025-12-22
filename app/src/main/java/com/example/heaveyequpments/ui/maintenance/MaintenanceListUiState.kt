package com.example.heaveyequpments.ui.maintenance

import com.example.heaveyequpments.data.model.maintenance.MaintenanceLogWithDetails

sealed class MaintenanceListUiState {
    object Loading : MaintenanceListUiState()
    object Empty : MaintenanceListUiState()
    data class Success(
        val logs: List<MaintenanceLogWithDetails>,
    ) : MaintenanceListUiState()
    data class Error(val message: String) : MaintenanceListUiState()
}