package com.example.heaveyequpments.ui.equipment

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.bumptech.glide.Glide
import com.example.heaveyequpments.data.model.equipment.HeavyEquipments
import com.example.heaveyequpments.databinding.FragmentAddHeavyeEqupmentBinding
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class AddEquipmentFragment : Fragment() {

    private val viewModel: HeavyEquipmentsViewModel by viewModels()
    private val args: AddEquipmentFragmentArgs by navArgs()
    private var _binding: FragmentAddHeavyeEqupmentBinding? = null
    private val binding get() = _binding!!

    private var currentImageUri: Uri? = null

    private val pickImageLauncher = registerForActivityResult(ActivityResultContracts.OpenDocument()) { uri ->
        uri?.let { handleImageSelection(it) }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentAddHeavyeEqupmentBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val equipmentId = args.equipmentId.toLong()
        setupInitialUI(equipmentId)
        collectUiState()

        binding.buttonAddEquipments.setOnClickListener {
            viewModel.saveEquipment(
                id = equipmentId,
                name = binding.editTextName.text.toString().trim(),
                desc = binding.editTextModel.text.toString().trim(),
                numStr = binding.editTextServiceDate.text.toString().trim(),
                image = currentImageUri?.toString()
            )
        }

        binding.buttonAddImage.setOnClickListener { pickImageLauncher.launch(arrayOf("image/*")) }
    }

    private fun setupInitialUI(id: Long) {
        binding.headerTitle.text = args.title
        if (id != 0L) {
            binding.buttonAddEquipments.text = "Update Equipment"
            viewModel.loadEquipmentDetails(id) // Fetch existing data
        }
    }

    private fun collectUiState() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                launch {
                    viewModel.addEditState.collect { state ->
                        binding.buttonAddEquipments.isEnabled = !state.isLoading
                        if (state.isEntrySaved) findNavController().popBackStack()
                        state.errorMessage?.let {
                            Toast.makeText(context, it, Toast.LENGTH_SHORT).show()
                            viewModel.resetAddEditState()
                        }
                    }
                }

                launch {
                    viewModel.equipmentToEdit.collect { equipment ->
                        equipment?.let { populateFields(it) }
                    }
                }
            }
        }
    }

    private fun populateFields(equipment: HeavyEquipments) {
        with(binding) {
            editTextName.setText(equipment.name)
            editTextModel.setText(equipment.description)
            editTextServiceDate.setText(equipment.number.toString())
            equipment.image?.let {
                currentImageUri = Uri.parse(it)
                updateImageThumbnail(currentImageUri)
            }
        }
    }

    private fun handleImageSelection(uri: Uri) {
        try {
            val takeFlags = Intent.FLAG_GRANT_READ_URI_PERMISSION
            requireContext().contentResolver.takePersistableUriPermission(uri, takeFlags)
            currentImageUri = uri
            updateImageThumbnail(uri)
        } catch (e: Exception) {
            Toast.makeText(context, "Permission Error", Toast.LENGTH_SHORT).show()
        }
    }

    private fun updateImageThumbnail(uri: Uri?) {
        Glide.with(this).load(uri).into(binding.imageEquipmentPhoto)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}