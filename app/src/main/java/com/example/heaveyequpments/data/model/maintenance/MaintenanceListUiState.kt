package com.example.heaveyequpments.data.model.maintenance

sealed class MaintenanceListUiState {
    object Loading : MaintenanceListUiState()
    data class Success(
        val logs: List<MaintenanceLogWithDetails>,
        val isSearchActive: Boolean = false
    ) : MaintenanceListUiState()
    data class Error(val message: String) : MaintenanceListUiState()
    object Empty : MaintenanceListUiState()
}