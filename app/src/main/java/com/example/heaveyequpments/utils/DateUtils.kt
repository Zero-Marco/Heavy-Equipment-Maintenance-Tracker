package com.example.heaveyequpments.utils

import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

object DateUtils {
    private const val FULL_DATE_FORMAT = "MMM dd, yyyy - hh:mm a"
    private const val SHORT_DATE_FORMAT = "MMM dd, hh:mm a"
    private const val REPORT_DATE_FORMAT = "dd/MM/yyyy"

    fun formatFullDate(timestamp: Long): String {
        val sdf = SimpleDateFormat(FULL_DATE_FORMAT, Locale.getDefault())
        return sdf.format(Date(timestamp))
    }

    fun formatShortDate(timestamp: Long): String {
        val sdf = SimpleDateFormat(SHORT_DATE_FORMAT, Locale.getDefault())
        return sdf.format(Date(timestamp))
    }

    fun formatReportDate(timestamp: Long): String {
        val sdf = SimpleDateFormat(REPORT_DATE_FORMAT, Locale.getDefault())
        return sdf.format(Date(timestamp))
    }
}

