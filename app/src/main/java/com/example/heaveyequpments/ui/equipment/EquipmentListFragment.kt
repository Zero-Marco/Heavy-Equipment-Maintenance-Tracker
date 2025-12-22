package com.example.heaveyequpments.ui.equipment

import android.os.Bundle
import android.view.View
import androidx.core.view.isVisible
import androidx.core.widget.doOnTextChanged
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.heaveyequpments.R
import com.example.heaveyequpments.adapters.MainFragmentAdapter
import com.example.heaveyequpments.data.model.equipment.HeavyEquipments
import com.example.heaveyequpments.databinding.FragmentMainBinding
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.snackbar.Snackbar
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class EquipmentListFragment : Fragment(R.layout.fragment_main) { // Using constructor inflation

    private var _binding: FragmentMainBinding? = null
    private val binding get() = _binding!!

    private val viewModel: HeavyEquipmentsViewModel by viewModels()
    private lateinit var adapter: MainFragmentAdapter

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        _binding = FragmentMainBinding.bind(view)

        setupRecyclerView()
        setupSearch()
        observeUiState()
        observeEvents()

        binding.floatingActionButton.setOnClickListener {
            findNavController().navigate(R.id.action_list_to_add)
        }
    }

    private fun observeUiState() {
        // Collect StateFlow safely with lifecycle awareness
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.uiState.collect { state ->
                    render(state)
                }
            }
        }
    }

    private fun render(state: EquipmentUiState) {
        binding.progressBar.isVisible = state is EquipmentUiState.Loading
        binding.recyclerViewEquipments.isVisible = state is EquipmentUiState.Success
        binding.textEmptyState.isVisible = state is EquipmentUiState.Empty

        when (state) {
            is EquipmentUiState.Success -> {

                // ListAdapter uses DiffUtil to animate only the items that changed
                adapter.submitList(state.equipments)
            }
            is EquipmentUiState.Error -> {
                Snackbar.make(binding.root, state.message, Snackbar.LENGTH_LONG).show()
            }
            else -> Unit
        }
    }

    private fun observeEvents() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.uiEvent.collect { event ->
                    when (event) {
                        is HeavyEquipmentsViewModel.EquipmentUiEvent.ShowSnackbar -> {
                            Snackbar.make(binding.root, event.message, Snackbar.LENGTH_SHORT).show()
                        }
                        is HeavyEquipmentsViewModel.EquipmentUiEvent.NavigateBack -> {
                            findNavController().popBackStack()
                        }
                    }
                }
            }
        }
    }

    private fun setupSearch() {
        // Instead of triggering a new observer, we just tell the ViewModel the query changed
        binding.etSearch.doOnTextChanged { text, _, _, _ ->
            viewModel.onSearchQueryChanged(text.toString().trim())
        }
    }

    private fun setupRecyclerView() {

        adapter = MainFragmentAdapter(
            onEditClick = { equipment ->
                val action = EquipmentListFragmentDirections.actionListToAdd(
                    equipmentId = equipment.id,
                    title = "Edit ${equipment.name}"
                )
                findNavController().navigate(action)
            }
        )

        binding.recyclerViewEquipments.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = this@EquipmentListFragment.adapter
        }

        val swipeHandler = object : ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.LEFT) {
            override fun onMove(r: RecyclerView, vh: RecyclerView.ViewHolder, t: RecyclerView.ViewHolder) = false

            override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
                val position = viewHolder.adapterPosition
                // Use the helper function we kept in the adapter
                val equipment = adapter.getEquipmentAt(position)
                showDeleteConfirmation(equipment, position)
            }
        }
        ItemTouchHelper(swipeHandler).attachToRecyclerView(binding.recyclerViewEquipments)
    }

    private fun showDeleteConfirmation(equipment: HeavyEquipments, position: Int) {
        MaterialAlertDialogBuilder(requireContext())
            .setTitle("Delete ${equipment.name}?")
            .setMessage("This will permanently delete this machine and ALL its maintenance history.")
            .setNegativeButton("Cancel") { _, _ -> adapter.notifyItemChanged(position) }
            .setPositiveButton("Delete") { _, _ ->
                viewModel.delete(equipment)
            }
            .setOnCancelListener { adapter.notifyItemChanged(position) }
            .show()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}