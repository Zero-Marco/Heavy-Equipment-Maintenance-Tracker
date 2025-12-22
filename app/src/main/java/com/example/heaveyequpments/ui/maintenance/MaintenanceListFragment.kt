package com.example.heaveyequpments.ui.maintenance

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.FileProvider
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
import com.example.heaveyequpments.adapters.MaintenanceLogAdapter
import com.example.heaveyequpments.ui.maintenance.MaintenanceListUiState
import com.example.heaveyequpments.data.model.maintenance.MaintenanceLogWithDetails
import com.example.heaveyequpments.databinding.FragmentMaintenanceListBinding
import com.example.heaveyequpments.reports.MaintenancePdfGenerator
import com.example.heaveyequpments.utils.gone
import com.example.heaveyequpments.utils.visible
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint // REQUIRED: Enables Hilt injection
class MaintenanceListFragment : Fragment() {

    private var _binding: FragmentMaintenanceListBinding? = null
    private val binding get() = _binding!!

    private val viewModel: MaintenanceLogViewModel by viewModels()
    private lateinit var logAdapter: MaintenanceLogAdapter

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentMaintenanceListBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupRecyclerView()
        setupSearch()
        observeUiState()
        setupSwipeToDelete()

        binding.fbaAddTicket.setOnClickListener {
            val action = MaintenanceListFragmentDirections.actionListToAddLog(logId = -1L)
            findNavController().navigate(action)
        }
    }

    private fun setupRecyclerView() {
        logAdapter = MaintenanceLogAdapter().apply {
            onItemClick = { details ->
                val action = MaintenanceListFragmentDirections.actionListToAddLog(logId = details.maintenance.logId)
                findNavController().navigate(action)
            }
            onItemLongClick = { shareMaintenancePdf(it) }
        }

        binding.recyclerViewMaintenance.apply {
            adapter = logAdapter
            layoutManager = LinearLayoutManager(requireContext())
        }
    }

    private fun setupSearch() {
        binding.etSearchMaintenance.doOnTextChanged { text, _, _, _ ->
            viewModel.onSearchQueryChanged(text.toString())
        }
    }

    private fun observeUiState() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                // Collect the single state object we built in the ViewModel
                viewModel.uiState.collect { state ->
                    render(state)
                }
            }
        }
    }

    private fun render(state: MaintenanceListUiState) {
        when (state) {
            is MaintenanceListUiState.Loading -> {

            }
            is MaintenanceListUiState.Empty -> {
                binding.tvEmptyState.visible()
                binding.recyclerViewMaintenance.gone()
            }
            is MaintenanceListUiState.Success -> {
                binding.tvEmptyState.gone()
                binding.recyclerViewMaintenance.visible()
                logAdapter.submitList(state.logs)
            }
            is MaintenanceListUiState.Error -> {

            }
        }
    }

    private fun setupSwipeToDelete() {
        val swipeHandler = object : ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.LEFT) {
            override fun onMove(r: RecyclerView, vh: RecyclerView.ViewHolder, t: RecyclerView.ViewHolder) = false
            override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
                val position = viewHolder.adapterPosition
                showDeleteConfirmation(logAdapter.currentList[position], position)
            }
        }
        ItemTouchHelper(swipeHandler).attachToRecyclerView(binding.recyclerViewMaintenance)
    }

    private fun showDeleteConfirmation(details: MaintenanceLogWithDetails, position: Int) {
        MaterialAlertDialogBuilder(requireContext())
            .setTitle("Delete Service Record?")
            .setMessage("Are you sure you want to delete '${details.maintenance.title}'?")
            .setNegativeButton("Cancel") { _, _ -> logAdapter.notifyItemChanged(position) }
            .setPositiveButton("Delete") { _, _ -> viewModel.deleteLog(details.maintenance) }
            .show()
    }

    private fun shareMaintenancePdf(details: MaintenanceLogWithDetails) {
        val generator = MaintenancePdfGenerator(requireContext())
        generator.generateRepairReport(details)?.let { file ->
            val uri = FileProvider.getUriForFile(requireContext(), "${requireContext().packageName}.provider", file)
            val intent = Intent(Intent.ACTION_SEND).apply {
                type = "application/pdf"
                putExtra(Intent.EXTRA_STREAM, uri)
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            }
            startActivity(Intent.createChooser(intent, "Share/Print Report"))
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}