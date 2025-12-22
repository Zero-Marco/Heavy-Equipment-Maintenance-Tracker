package com.example.heaveyequpments.ui.equipment

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.heaveyequpments.data.model.equipment.HeavyEquipments
import com.example.heaveyequpments.data.repository.HeavyEquipmentsRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
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
class HeavyEquipmentsViewModel @Inject constructor(
    private val repository: HeavyEquipmentsRepository
) : ViewModel() {
    private val _uiEvent = MutableSharedFlow<EquipmentUiEvent>()
    val uiEvent = _uiEvent.asSharedFlow()

    sealed class EquipmentUiEvent {
        data class ShowSnackbar(val message: String) : EquipmentUiEvent()
        object NavigateBack : EquipmentUiEvent()
    }
    // --- 1. LIST STATE ---
    private val _searchQuery = MutableStateFlow("")

    @OptIn(ExperimentalCoroutinesApi::class)
    val uiState: StateFlow<EquipmentUiState> = _searchQuery
        .debounce(300)
        .flatMapLatest { query ->
            if (query.isEmpty()) repository.allEquipments
            else repository.searchEquipments("%$query%")
        }
        .map { list ->
            if (list.isEmpty()) EquipmentUiState.Empty
            else EquipmentUiState.Success(list)
        }
        .catch { emit(EquipmentUiState.Error(it.localizedMessage ?: "Error")) }
        .stateIn(viewModelScope, SharingStarted.Companion.WhileSubscribed(5000), EquipmentUiState.Loading)

    // --- 2. ADD/EDIT STATE (For Clean Code) ---
    // This handles the loading, saving, and errors of the Add Fragment
    private val _addEditState = MutableStateFlow(AddEditUiState())
    val addEditState = _addEditState.asStateFlow()

    private val _equipmentToEdit = MutableStateFlow<HeavyEquipments?>(null)
    val equipmentToEdit = _equipmentToEdit.asStateFlow()

    // --- 3. ACTIONS ---

    fun onSearchQueryChanged(query: String) {
        _searchQuery.value = query
    }


    fun saveEquipment(id: Long, name: String, desc: String, numStr: String, image: String?) {
        if (name.isBlank() || desc.isBlank() || numStr.isBlank()) {
            _addEditState.update { it.copy(errorMessage = "All fields are required") }
            return
        }

        val number = numStr.toIntOrNull() ?: run {
            _addEditState.update { it.copy(errorMessage = "Invalid ID number") }
            return
        }

        viewModelScope.launch {
            _addEditState.update { it.copy(isLoading = true) }
            try {
                val equipment = HeavyEquipments(id, name, desc, number, image)
                if (id == 0L) repository.insert(equipment) else repository.update(equipment)


                _addEditState.update { it.copy(isEntrySaved = true, isLoading = false) }
            } catch (e: Exception) {
                _addEditState.update { it.copy(errorMessage = "Save failed", isLoading = false) }
            }
        }
    }

    fun loadEquipmentDetails(id: Long) {
        if (id == 0L) return
        viewModelScope.launch {
            val equipment = repository.getById(id)
            _equipmentToEdit.value = equipment
        }
    }


    fun delete(equipment: HeavyEquipments) = viewModelScope.launch {
        try {
            repository.delete(equipment)

            _uiEvent.emit(EquipmentUiEvent.ShowSnackbar("${equipment.name} deleted"))
        } catch (e: Exception) {
            _uiEvent.emit(EquipmentUiEvent.ShowSnackbar("Delete failed"))
        }
    }

    fun resetAddEditState() {
        _addEditState.value = AddEditUiState()
    }
}