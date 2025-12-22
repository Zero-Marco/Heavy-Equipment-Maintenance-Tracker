package com.example.heaveyequpments.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.heaveyequpments.R
import com.example.heaveyequpments.data.model.maintenance.MaintenanceLogWithDetails
import com.example.heaveyequpments.databinding.MaintenanceCardBinding
import com.example.heaveyequpments.utils.DateUtils


class MaintenanceLogAdapter :
    ListAdapter<MaintenanceLogWithDetails, MaintenanceLogAdapter.LogViewHolder>(LogDiffCallback) { // No need for () if using 'object'

    var onItemClick: ((MaintenanceLogWithDetails) -> Unit)? = null
    var onItemLongClick: ((MaintenanceLogWithDetails) -> Unit)? = null

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): LogViewHolder {
        val binding = MaintenanceCardBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
        return LogViewHolder(binding)
    }

    override fun onBindViewHolder(holder: LogViewHolder, position: Int) {
        val item = getItem(position)
        holder.bind(item)
    }

    inner class LogViewHolder(private val binding: MaintenanceCardBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(details: MaintenanceLogWithDetails) {
            val log = details.maintenance


            val equipment = details.equipment
            val name = equipment?.name ?: "Deleted/Unknown"
            val number = equipment?.number ?: "000"

            binding.tvEquipmentNameNumber.text = "$name - #$number"
            binding.titleTextView.text = log.title

            // Status Logic
            if (log.isClosed) {
                binding.completedIcon.visibility = View.VISIBLE
                binding.startTimeText.visibility = View.GONE
            } else {
                binding.completedIcon.visibility = View.GONE
                binding.startTimeText.visibility = View.VISIBLE
                val dateStart = DateUtils.formatShortDate(log.startTime)
                binding.startTimeText.text = binding.root.context.getString(R.string.start_time_short, dateStart)

            }

            // Click Listeners
            binding.root.setOnClickListener { onItemClick?.invoke(details) }
            binding.btnExportPdf.setOnClickListener { onItemLongClick?.invoke(details) }
        }


    }


    companion object LogDiffCallback : DiffUtil.ItemCallback<MaintenanceLogWithDetails>() {
        override fun areItemsTheSame(oldItem: MaintenanceLogWithDetails, newItem: MaintenanceLogWithDetails): Boolean {
            return oldItem.maintenance.logId == newItem.maintenance.logId
        }

        override fun areContentsTheSame(oldItem: MaintenanceLogWithDetails, newItem: MaintenanceLogWithDetails): Boolean {
            return oldItem == newItem
        }
    }
}