package com.example.heaveyequpments.reports

import android.content.Context
import android.graphics.*
import android.graphics.pdf.PdfDocument
import com.example.heaveyequpments.data.model.equipment.HeavyEquipments
import com.example.heaveyequpments.data.model.maintenance.MaintenanceLogWithDetails
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.*

class MaintenancePdfGenerator(private val context: Context) {

    private fun formatDate(timeInMillis: Long): String {
        val dateFormat = SimpleDateFormat("MMM dd, yyyy", Locale.getDefault())
        return dateFormat.format(Date(timeInMillis))
    }

    fun generateRepairReport(details: MaintenanceLogWithDetails): File? {
        val pdfDocument = PdfDocument()
        val pageInfo = PdfDocument.PageInfo.Builder(595, 842, 1).create()
        val page = pdfDocument.startPage(pageInfo)
        val canvas = page.canvas
        val paint = Paint()

        // --- Header ---
        paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        paint.textSize = 20f
        canvas.drawText("MAINTENANCE REPORT", 40f, 50f, paint)


        val equip = details.equipment
        val equipName = equip?.name ?: "Unknown Equipment"
        val equipNumber = equip?.number ?: "N/A"

        paint.textSize = 14f
        paint.typeface = Typeface.DEFAULT
        canvas.drawText("Equipment: $equipName (#$equipNumber)", 40f, 90f, paint)
        canvas.drawText("Date: ${formatDate(details.maintenance.startTime)}", 40f, 110f, paint)
        canvas.drawText("Task: ${details.maintenance.title}", 40f, 130f, paint)

        canvas.drawLine(40f, 145f, 555f, 145f, paint)

        // --- Parts List ---
        var y = 175f
        paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        canvas.drawText("Parts Replaced:", 40f, y, paint)

        paint.typeface = Typeface.DEFAULT
        details.partsChanged.forEach { part ->
            y += 20f
            canvas.drawText("- ${part.name}: $${part.cost ?: 0.0}", 60f, y, paint)
        }

        // --- Cost Summary (Professional Touch) ---
        y += 40f
        val totalCost = (details.partsChanged.sumOf { it.cost ?: 0.0 }) +
                (details.mechanic.sumOf { it.cost ?: 0.0 }) +
                (details.otherExpenses.sumOf { it.cost ?: 0.0 })

        paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        canvas.drawText("Total Service Cost: $${String.format("%.2f", totalCost)}", 40f, y, paint)

        pdfDocument.finishPage(page)

        // Save to Cache so it's temporary and safe
        val file = File(context.cacheDir, "Report_${details.maintenance.logId}.pdf")
        return try {
            FileOutputStream(file).use { out ->
                pdfDocument.writeTo(out)
            }
            pdfDocument.close()
            file
        } catch (e: Exception) {
            pdfDocument.close()
            null
        }
    }
    fun generateMachineHistoryPdf(
        equipment: HeavyEquipments,
        logs: List<MaintenanceLogWithDetails>,
        start: Long,
        end: Long
    ): File? {
        val pdfDocument = PdfDocument()
        val pageInfo = PdfDocument.PageInfo.Builder(595, 842, 1).create()
        var page = pdfDocument.startPage(pageInfo)
        var canvas = page.canvas
        val paint = Paint()

        paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        paint.textSize = 18f
        canvas.drawText("EQUIPMENT SERVICE HISTORY", 40f, 50f, paint)

        paint.typeface = Typeface.DEFAULT
        paint.textSize = 12f
        canvas.drawText("Machine: ${equipment.name} (#${equipment.number})", 40f, 75f, paint)
        canvas.drawText("Period: ${formatDate(start)} to ${formatDate(end)}", 40f, 95f, paint)

        var y = 130f
        val margin = 40f

        logs.forEach { entry ->
            if (y > 750f) {
                pdfDocument.finishPage(page)
                page = pdfDocument.startPage(pageInfo)
                canvas = page.canvas
                y = 50f
            }
            paint.color = Color.LTGRAY
            canvas.drawRect(margin, y, 555f, y + 2f, paint)
            paint.color = Color.BLACK
            y += 20f
            paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            canvas.drawText(formatDate(entry.maintenance.startTime), margin, y, paint)
            y += 20f
            paint.typeface = Typeface.DEFAULT
            canvas.drawText("Task: ${entry.maintenance.title}", margin, y, paint)
            val partsSummary = entry.partsChanged.joinToString { it.name }
            y += 20f
            paint.textSize = 10f
            canvas.drawText("Parts: $partsSummary", margin + 20f, y, paint)
            y += 40f
        }

        pdfDocument.finishPage(page)
        val file = File(context.cacheDir, "${equipment.name}_History.pdf")
        return try {
            pdfDocument.writeTo(FileOutputStream(file))
            pdfDocument.close()
            file
        } catch (e: Exception) {
            null
        }
    }
}