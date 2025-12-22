package com.example.heaveyequpments.reports.ui

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.core.content.FileProvider
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import com.example.heaveyequpments.R
import com.example.heaveyequpments.data.model.equipment.HeavyEquipments
import com.example.heaveyequpments.data.model.maintenance.MaintenanceLogWithDetails
import com.example.heaveyequpments.databinding.FragmentReportSelectionBinding
import com.example.heaveyequpments.reports.MaintenancePdfGenerator
import com.example.heaveyequpments.ui.maintenance.MaintenanceLogViewModel
import com.example.heaveyequpments.utils.Constants
import com.google.android.material.datepicker.MaterialDatePicker
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.take
import kotlinx.coroutines.launch
import java.io.File

@AndroidEntryPoint
class ReportSelectionFragment : Fragment(R.layout.fragment_report_selection) {

    private var _binding: FragmentReportSelectionBinding? = null
    private val binding get() = _binding!!

    private val viewModel: MaintenanceLogViewModel by viewModels()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        _binding = FragmentReportSelectionBinding.bind(view)

        setupClickListeners()
    }

    private fun setupClickListeners() {
        // Option 1: Direct navigation to list for single repair report
        binding.cardSingleRepair.setOnClickListener {
            findNavController().navigate(R.id.MaintenanceListFragment)
        }

        // Option 2: Date Range History
        binding.cardDateRange.setOnClickListener {
            showMachinePickerDialog()
        }
    }

    private fun showMachinePickerDialog() {
        // Safely access the current list of equipments from the ViewModel StateFlow
        val equipments = viewModel.allEquipments.value

        if (equipments.isEmpty()) {
            Toast.makeText(context, "No equipment found. Please add one first.", Toast.LENGTH_SHORT).show()
            return
        }

        val machineNames = equipments.map { "${it.name} (#${it.number})" }.toTypedArray()

        MaterialAlertDialogBuilder(requireContext())
            .setTitle("Select Machine for History")
            .setItems(machineNames) { _, which ->
                showDateRangePicker(equipments[which])
            }
            .show()
    }

    private fun showDateRangePicker(equipment: HeavyEquipments) {
        val dateRangePicker = MaterialDatePicker.Builder.dateRangePicker()
            .setTitleText("Select Date Range")
            .setSelection(androidx.core.util.Pair(MaterialDatePicker.todayInUtcMilliseconds(), MaterialDatePicker.todayInUtcMilliseconds()))
            .build()

        dateRangePicker.show(childFragmentManager, "range_picker")

        dateRangePicker.addOnPositiveButtonClickListener { selection ->
            val start = selection.first ?: return@addOnPositiveButtonClickListener
            val end = selection.second ?: return@addOnPositiveButtonClickListener

            fetchAndGenerate(equipment, start, end)
        }
    }

    private fun fetchAndGenerate(equipment: HeavyEquipments, start: Long, end: Long) {
        // FIX: Collecting Kotlin Flow safely using lifecycleScope
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.getLogsByMachineAndDate(equipment.id, start, end)
                    .take(1) // Only take the first list of logs emitted
                    .collect { logs ->
                        if (logs.isEmpty()) {
                            Toast.makeText(context, "No records found for this period", Toast.LENGTH_SHORT).show()
                        } else {
                            generateAndSharePdf(equipment, logs, start, end)
                        }
                    }
            }
        }
    }

    private fun generateAndSharePdf(equipment: HeavyEquipments, logs: List<MaintenanceLogWithDetails>, start: Long, end: Long) {
        val generator = MaintenancePdfGenerator(requireContext())
        val pdfFile = generator.generateMachineHistoryPdf(equipment, logs, start, end)

        if (pdfFile != null && pdfFile.exists()) {
            sharePdfFile(pdfFile)
        } else {
            Toast.makeText(context, "Failed to generate PDF", Toast.LENGTH_SHORT).show()
        }
    }

    private fun sharePdfFile(file: File) {
        try {
            val authority = "${requireContext().packageName}${Constants.FILE_AUTHORITY_SUFFIX}"
            val uri = FileProvider.getUriForFile(requireContext(), authority, file)

            val intent = Intent(Intent.ACTION_SEND).apply {
                type = Constants.MIME_TYPE_PDF
                putExtra(Intent.EXTRA_STREAM, uri)
                // This grants permission to the app that receives the intent
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            }

            val chooser = Intent.createChooser(intent, "Share/Print Machine History")

            // This is a safety step for older Android versions
            chooser.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)

            startActivity(chooser)
        } catch (e: Exception) {
            Toast.makeText(context, "Cannot share file: ${e.message}", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}