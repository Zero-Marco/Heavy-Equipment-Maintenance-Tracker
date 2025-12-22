package com.example.heaveyequpments.ui.maintenance

import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.LinearLayout
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.bumptech.glide.Glide
import com.example.heaveyequpments.R
import com.example.heaveyequpments.data.model.equipment.HeavyEquipments
import com.example.heaveyequpments.data.model.maintenance.InvoiceImage
import com.example.heaveyequpments.data.model.maintenance.Maintenance
import com.example.heaveyequpments.data.model.maintenance.MaintenanceLogWithDetails
import com.example.heaveyequpments.data.model.maintenance.Mechanic
import com.example.heaveyequpments.data.model.maintenance.OtherExpense
import com.example.heaveyequpments.data.model.maintenance.PartsChanged
import com.example.heaveyequpments.data.model.maintenance.PartsChangedImage
import com.example.heaveyequpments.databinding.FragmentAddMaintenanceLogBinding
import com.example.heaveyequpments.utils.Constants
import com.example.heaveyequpments.utils.DateUtils
import com.google.android.material.snackbar.Snackbar
import com.google.android.material.textfield.TextInputEditText
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import java.util.Calendar

@AndroidEntryPoint
class AddMaintenanceLog : Fragment() {

    private var _binding: FragmentAddMaintenanceLogBinding? = null
    private val binding get() = _binding!!

    private val args: AddMaintenanceLogArgs by navArgs()
    private val viewModel: MaintenanceLogViewModel by viewModels()

    private var selectedEquipmentId: Long = -1L
    private var selectedStartTime: Long = System.currentTimeMillis()
    private var selectedEndTime: Long? = null

    // Image URI tracking
    private val selectedPartImageUris = mutableSetOf<Uri>()
    private val selectedInvoiceImageUris = mutableSetOf<Uri>()

    // Launchers
    private val pickPartImages = registerForActivityResult(ActivityResultContracts.GetMultipleContents()) { handleImageSelection(it, true) }
    private val pickInvoiceImages = registerForActivityResult(ActivityResultContracts.GetMultipleContents()) { handleImageSelection(it, false) }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentAddMaintenanceLogBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupInitialUI()
        setupClickListeners()
        collectUiState()

        if (args.logId != -1L) {
            viewModel.loadLogForEditing(args.logId)
        }
    }

    private fun setupInitialUI() {
        updateTimeDisplay(selectedStartTime, true)
        binding.fabSaveLog.text = if (args.logId == -1L) "Save Log" else "Update Log"
    }

    private fun collectUiState() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                // Observe All Equipments for the Dropdown
                launch {
                    viewModel.allEquipments.collect { list -> setupEquipmentDropdown(list) }
                }

                // Observe Add/Edit State (Unified)
                launch {
                    viewModel.addEditState.collect { state ->
                        binding.fabSaveLog.isEnabled = !state.isLoading

                        if (state.isEntrySaved) {
                            findNavController().popBackStack()
                            viewModel.resetAddEditState()
                        }

                        state.errorMessage?.let {
                            Snackbar.make(binding.root, it, Snackbar.LENGTH_LONG).show()
                            viewModel.resetAddEditState()
                        }

                        state.logDetails?.let { populateFields(it) }
                    }
                }
            }
        }
    }

    private fun populateFields(details: MaintenanceLogWithDetails) {
        if (selectedEquipmentId != -1L) return // Already populated or user is interacting

        binding.etLogTitle.setText(details.maintenance.title)
        binding.etLogDescription.setText(details.maintenance.description)
        selectedEquipmentId = details.maintenance.equipmentId

        // Populate dynamic lists
        binding.containerPartsChanged.removeAllViews()
        details.partsChanged.forEach { addPartView(it) }

        binding.containerMechanicEntries.removeAllViews()
        details.mechanic.forEach { addMechanicView(it) }

        binding.containerOtherExpenses.removeAllViews()
        details.otherExpenses.forEach { addExpenseView(it) }

        // Populate Images
        details.partsChangedImage.forEach { handleImageSelection(listOf(Uri.parse(it.imagePath)), true) }
        details.invoiceImage.forEach { handleImageSelection(listOf(Uri.parse(it.imagePath)), false) }
    }

    private fun attemptSave() {
        val log = Maintenance(
            logId = if (args.logId == -1L) 0L else args.logId,
            equipmentId = selectedEquipmentId,
            title = binding.etLogTitle.text.toString().trim(),
            description = binding.etLogDescription.text.toString().trim().ifEmpty { null },
            startTime = selectedStartTime,
            endTime = selectedEndTime,
            isClosed = selectedEndTime != null
        )

        viewModel.saveOrUpdateLog(
            log = log,
            parts = collectPartsFromUI(),
            mechanics = collectMechanicsFromUI(), // Now resolves correctly
            expenses = collectExpensesFromUI(),   // Now resolves correctly
            partsImages = selectedPartImageUris.map { uri ->
                PartsChangedImage(
                    partChangedImageId = 0L, // Usually 0 for new entries
                    logParentId = 0L,        // The ViewModel/Repo will update this with the real ID
                    imagePath = uri.toString(),
                    description = null
                )
            },
            invoiceImages = selectedInvoiceImageUris.map { uri ->
                InvoiceImage(
                    invoiceImageId = 0L,
                    logParentId = 0L,
                    imagePath = uri.toString(),
                    description = null
                )
            }
        )
    }

    // Dynamic View Helpers

    private fun addPartView(data: PartsChanged? = null) {
        val itemBinding = layoutInflater.inflate(R.layout.item_part_input, binding.containerPartsChanged, false)
        data?.let {
            itemBinding.findViewById<TextInputEditText>(R.id.et_part_name).setText(it.name)
            itemBinding.findViewById<TextInputEditText>(R.id.et_part_cost).setText(it.cost?.toString())
        }
        itemBinding.findViewById<ImageButton>(R.id.btn_remove_part).setOnClickListener {
            binding.containerPartsChanged.removeView(itemBinding)
        }
        binding.containerPartsChanged.addView(itemBinding)
    }

    private fun collectPartsFromUI(): List<PartsChanged> {
        val parts = mutableListOf<PartsChanged>()
        for (i in 0 until binding.containerPartsChanged.childCount) {
            val view = binding.containerPartsChanged.getChildAt(i)
            val name = view.findViewById<TextInputEditText>(R.id.et_part_name).text.toString()
            val cost = view.findViewById<TextInputEditText>(R.id.et_part_cost).text.toString().toDoubleOrNull()
            if (name.isNotBlank()) parts.add(PartsChanged(0L, 0L, name,cost))
        }
        return parts
    }



    private fun handleImageSelection(uris: List<Uri>, isPartImage: Boolean) {
        uris.forEach { uri ->
            try {
                requireContext().contentResolver.takePersistableUriPermission(uri, Intent.FLAG_GRANT_READ_URI_PERMISSION)
                if (isPartImage) {
                    if (selectedPartImageUris.add(uri)) addImagePreview(binding.containerPartsImagePreviews, uri, true)
                } else {
                    if (selectedInvoiceImageUris.add(uri)) addImagePreview(binding.containerInvoiceImagePreviews, uri, false)
                }
            } catch (e: Exception) { /* Log error */ }
        }
    }

    private fun addImagePreview(container: LinearLayout, uri: Uri, isPartImage: Boolean) {
        val view = layoutInflater.inflate(R.layout.item_image_preview, container, false)
        val imageView = view.findViewById<ImageView>(R.id.iv_preview)


        Glide.with(this)
            .load(uri)
            .override(200, 200) //
            .centerCrop()
            .into(imageView)

        view.findViewById<ImageButton>(R.id.btn_remove_img).setOnClickListener {
            container.removeView(view)
            if (isPartImage) selectedPartImageUris.remove(uri) else selectedInvoiceImageUris.remove(uri)
        }
        container.addView(view)
    }

    private fun setupEquipmentDropdown(equipments: List<HeavyEquipments>) {
        val adapter = ArrayAdapter(requireContext(), android.R.layout.simple_list_item_1, equipments.map { it.name })
        binding.acEquipmentSearch.setAdapter(adapter)
        binding.acEquipmentSearch.setOnItemClickListener { _, _, position, _ ->
            selectedEquipmentId = equipments[position].id
        }
    }

    private fun setupClickListeners() {
        binding.tilStartTime.setEndIconOnClickListener {
            showDateTimePicker(isStart = true)
        }

        binding.tilEndTime.setEndIconOnClickListener {
            showDateTimePicker(isStart = false)
        }


        binding.etStartTime.setOnClickListener { showDateTimePicker(isStart = true) }
        binding.etEndTime.setOnClickListener { showDateTimePicker(isStart = false) }
        binding.btnAddPart.setOnClickListener { addPartView() }
        binding.btnAddMechanic.setOnClickListener { addMechanicView() }
        binding.btnAddExpense.setOnClickListener { addExpenseView() }
        binding.btnAddPartsImage.setOnClickListener { pickPartImages.launch(Constants.MIME_TYPE_IMAGE) }
        binding.btnAddInvoiceImage.setOnClickListener { pickInvoiceImages.launch(Constants.MIME_TYPE_IMAGE) }
        binding.fabSaveLog.setOnClickListener { attemptSave() }
    }

    private fun updateTimeDisplay(time: Long, isStartTime: Boolean) {
        val formattedDate = DateUtils.formatFullDate(time)
        if (isStartTime) binding.etStartTime.setText(formattedDate)
        else binding.etEndTime.setText(formattedDate)
    }
    private fun addMechanicView(data: Mechanic? = null) {
        val itemBinding = layoutInflater.inflate(R.layout.item_machanic_entry, binding.containerMechanicEntries, false)
        data?.let {
            itemBinding.findViewById<TextInputEditText>(R.id.et_mechanic_name).setText(it.name)
            itemBinding.findViewById<TextInputEditText>(R.id.et_service_cost).setText(it.cost?.toString())
        }
        itemBinding.findViewById<ImageButton>(R.id.btn_remove_mechanic).setOnClickListener {
            binding.containerMechanicEntries.removeView(itemBinding)
        }
        binding.containerMechanicEntries.addView(itemBinding)
    }

    private fun addExpenseView(data: OtherExpense? = null) {
        val itemBinding = layoutInflater.inflate(R.layout.item_other_expense, binding.containerOtherExpenses, false)
        data?.let {
            itemBinding.findViewById<TextInputEditText>(R.id.et_expense_name).setText(it.name)
            itemBinding.findViewById<TextInputEditText>(R.id.et_expense_cost).setText(it.cost?.toString())
            itemBinding.findViewById<TextInputEditText>(R.id.et_expense_description).setText(it.description)
        }
        itemBinding.findViewById<ImageButton>(R.id.btn_remove_expense).setOnClickListener {
            binding.containerOtherExpenses.removeView(itemBinding)
        }
        binding.containerOtherExpenses.addView(itemBinding)
    }
    private fun collectMechanicsFromUI(): List<Mechanic> {
        val list = mutableListOf<Mechanic>()
        for (i in 0 until binding.containerMechanicEntries.childCount) {
            val view = binding.containerMechanicEntries.getChildAt(i)
            val name = view.findViewById<TextInputEditText>(R.id.et_mechanic_name).text.toString().trim()
            val cost = view.findViewById<TextInputEditText>(R.id.et_service_cost).text.toString().toDoubleOrNull()
            if (name.isNotBlank()) list.add(Mechanic(0L, 0L, name,cost))
        }
        return list
    }

    private fun collectExpensesFromUI(): List<OtherExpense> {
        val list = mutableListOf<OtherExpense>()
        for (i in 0 until binding.containerOtherExpenses.childCount) {
            val view = binding.containerOtherExpenses.getChildAt(i)
            val name = view.findViewById<TextInputEditText>(R.id.et_expense_name).text.toString().trim()
            val cost = view.findViewById<TextInputEditText>(R.id.et_expense_cost).text.toString().toDoubleOrNull()
            val desc = view.findViewById<TextInputEditText>(R.id.et_expense_description).text.toString().trim().ifEmpty { null }
            if (name.isNotBlank()) list.add(OtherExpense(0L, 0L, name, desc,cost))
        }
        return list
    }
    private fun showDateTimePicker(isStart: Boolean) {
        val cal = Calendar.getInstance()
        DatePickerDialog(requireContext(), { _, y, m, d ->
            cal.set(y, m, d)
            TimePickerDialog(requireContext(), { _, h, min ->
                cal.set(Calendar.HOUR_OF_DAY, h)
                cal.set(Calendar.MINUTE, min)
                if (isStart) selectedStartTime = cal.timeInMillis else selectedEndTime = cal.timeInMillis
                updateTimeDisplay(cal.timeInMillis, isStart)
            }, cal.get(Calendar.HOUR_OF_DAY), cal.get(Calendar.MINUTE), false).show()
        }, cal.get(Calendar.YEAR), cal.get(Calendar.MONTH), cal.get(Calendar.DAY_OF_MONTH)).show()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}