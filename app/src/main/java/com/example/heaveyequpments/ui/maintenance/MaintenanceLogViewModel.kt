package com.example.heaveyequpments.ui.maintenance

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.heaveyequpments.data.model.equipment.HeavyEquipments
import com.example.heaveyequpments.data.model.maintenance.InvoiceImage
import com.example.heaveyequpments.data.model.maintenance.Maintenance
import com.example.heaveyequpments.data.model.maintenance.MaintenanceLogWithDetails
import com.example.heaveyequpments.data.model.maintenance.Mechanic
import com.example.heaveyequpments.data.model.maintenance.OtherExpense
import com.example.heaveyequpments.data.model.maintenance.PartsChanged
import com.example.heaveyequpments.data.model.maintenance.PartsChangedImage
import com.example.heaveyequpments.data.repository.MaintenanceLogRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class MaintenanceLogViewModel @Inject constructor(
    private val repository: MaintenanceLogRepository
) : ViewModel() {

    // --- 1. STATE MANAGEMENT ---

    private val _searchQuery = MutableStateFlow("")

    private val _logDetailsForEditing = MutableStateFlow<MaintenanceLogWithDetails?>(null)
    val logDetailsForEditing = _logDetailsForEditing.asStateFlow()
    private val _addEditState = MutableStateFlow(AddEditMaintenanceUiState())
    val addEditState = _addEditState.asStateFlow()
    private val _isSaving = MutableStateFlow(false)
    val isSaving = _isSaving.asStateFlow()

    @OptIn(ExperimentalCoroutinesApi::class)
    val uiState: StateFlow<MaintenanceListUiState> = _searchQuery
        .debounce(300) // Professional touch: Wait for user to stop typing
        .flatMapLatest { query ->
            if (query.isEmpty()) repository.getAllMaintenanceLogsWithDetails()
            else repository.searchMaintenanceLogs("%$query%")
        }
        .map { logs ->
            if (logs.isEmpty()) MaintenanceListUiState.Empty
            else MaintenanceListUiState.Success(logs)
        }
        .catch { e -> emit(MaintenanceListUiState.Error(e.localizedMessage ?: "Unknown Error")) }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.Companion.WhileSubscribed(5000),
            initialValue = MaintenanceListUiState.Loading
        )

    val allEquipments: StateFlow<List<HeavyEquipments>> = repository.getAllEquipments()
        .stateIn(viewModelScope, SharingStarted.Companion.WhileSubscribed(5000), emptyList())

    // 2. ONE-TIME UI EVENTS

    private val _uiEvent = MutableSharedFlow<UiEvent>()
    val uiEvent = _uiEvent.asSharedFlow()

    sealed class UiEvent {
        data class ShowSnackbar(val message: String) : UiEvent()
        object NavigateBack : UiEvent()
    }

    // 3. INTENTS

    fun onSearchQueryChanged(query: String) {
        _searchQuery.value = query
    }

    fun loadLogForEditing(logId: Long) {
        if (logId <= 0L) return
        viewModelScope.launch {
            repository.getLogDetails(logId)
                .catch { e -> _uiEvent.emit(UiEvent.ShowSnackbar("Error loading details")) }
                .collect { details ->
                    _addEditState.update { it.copy(logDetails = details) }
                }
        }
    }

    fun saveOrUpdateLog(
        log: Maintenance,
        parts: List<PartsChanged>,
        mechanics: List<Mechanic>,
        expenses: List<OtherExpense>,
        partsImages: List<PartsChangedImage>,
        invoiceImages: List<InvoiceImage>
    ) {
        // Check validation
        if (log.equipmentId == 0L) {
            _addEditState.update { it.copy(errorMessage = "Please select a machine") }
            return
        }

        viewModelScope.launch {
            _addEditState.update { it.copy(isLoading = true) }
            try {
                if (log.logId > 0L) {
                    repository.updateLogTransaction(log, parts, mechanics, expenses, partsImages, invoiceImages)
                } else {
                    repository.saveNewMaintenanceLog(log, parts, expenses, mechanics, partsImages, invoiceImages)
                }
                _addEditState.update { it.copy(isEntrySaved = true, isLoading = false) }
            } catch (e: Exception) {
                _addEditState.update { it.copy(errorMessage = e.localizedMessage ?: "Operation failed", isLoading = false) }
            }
        }
    }

    fun resetAddEditState() {
        _addEditState.value = AddEditMaintenanceUiState()
    }
    fun deleteLog(log: Maintenance) {
        viewModelScope.launch {
            try {
                repository.deleteLog(log)
                _uiEvent.emit(UiEvent.ShowSnackbar("Deleted successfully"))
            } catch (e: Exception) {
                _uiEvent.emit(UiEvent.ShowSnackbar("Delete failed"))
            }
        }
    }

    fun closeLog(log: Maintenance, endTime: Long) {
        viewModelScope.launch {
            try {
                repository.updateLog(log.copy(endTime = endTime, isClosed = true))
                _uiEvent.emit(UiEvent.ShowSnackbar("Log closed"))
            } catch (e: Exception) {
                _uiEvent.emit(UiEvent.ShowSnackbar("Failed to close log"))
            }
        }
    }

    // Helper for Report Generation - Still returns Flow for Repo consistency
    fun getLogsByMachineAndDate(id: Long, start: Long, end: Long): Flow<List<MaintenanceLogWithDetails>> {
        return repository.getLogsByMachineAndDate(id, start, end)
    }
}