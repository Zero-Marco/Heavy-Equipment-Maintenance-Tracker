package com.example.heaveyequpments.adapters

import android.net.Uri
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.heaveyequpments.data.model.equipment.HeavyEquipments
import com.example.heaveyequpments.databinding.EqupmentCardBinding

/**
 * Professional ListAdapter using DiffUtil for high performance and smooth animations.
 */
class MainFragmentAdapter(
    private val onEditClick: (HeavyEquipments) -> Unit,

) : ListAdapter<HeavyEquipments, MainFragmentAdapter.EquipmentViewHolder>(EquipmentDiffCallback) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): EquipmentViewHolder {
        val binding = EqupmentCardBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return EquipmentViewHolder(binding)
    }

    override fun onBindViewHolder(holder: EquipmentViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    inner class EquipmentViewHolder(private val binding: EqupmentCardBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(equipment: HeavyEquipments) {
            binding.NameEquipments.text = equipment.name
            binding.DescriptionEquipments.text = equipment.description
            binding.numberEquipments.text = equipment.number.toString()

            //  Image Loading
            Glide.with(binding.root.context)
                .load(equipment.image?.let { Uri.parse(it) })
                .override(300,300)
                .centerCrop()
                .placeholder(android.R.drawable.ic_menu_gallery)
                .error(android.R.drawable.ic_dialog_alert)
                .into(binding.imageEquipments)

            // Click listener for editing
            binding.root.setOnClickListener {
                onEditClick(equipment)
            }
        }
    }

    //  list for swipe-to-delete logic
    fun getEquipmentAt(position: Int): HeavyEquipments = getItem(position)


     //list updates.

    object EquipmentDiffCallback : DiffUtil.ItemCallback<HeavyEquipments>() {
        override fun areItemsTheSame(oldItem: HeavyEquipments, newItem: HeavyEquipments): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: HeavyEquipments, newItem: HeavyEquipments): Boolean {
            return oldItem == newItem // Data class handles equality check
        }
    }
}