package com.example.util

import android.content.Context
import android.content.Intent
import android.provider.CalendarContract
import android.widget.Toast
import com.example.data.model.ServiceJob
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

object CalendarHelper {

    fun addServiceAppointmentToCalendar(context: Context, job: ServiceJob) {
        try {
            val cal = Calendar.getInstance()
            // Attempt to parse scheduled date
            try {
                val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
                val parsed = sdf.parse(job.scheduledDate)
                if (parsed != null) {
                    cal.time = parsed
                    cal.set(Calendar.HOUR_OF_DAY, 10)
                    cal.set(Calendar.MINUTE, 0)
                }
            } catch (_: Exception) {
                cal.add(Calendar.DAY_OF_YEAR, 1)
            }

            val startTime = cal.timeInMillis
            val endTime = startTime + (2 * 60 * 60 * 1000) // 2 hours duration

            val intent = Intent(Intent.ACTION_INSERT)
                .setData(CalendarContract.Events.CONTENT_URI)
                .putExtra(CalendarContract.EXTRA_EVENT_BEGIN_TIME, startTime)
                .putExtra(CalendarContract.EXTRA_EVENT_END_TIME, endTime)
                .putExtra(
                    CalendarContract.Events.TITLE,
                    "Sri Vel AC Service - ${job.customerName} (${job.serviceCategory})"
                )
                .putExtra(
                    CalendarContract.Events.DESCRIPTION,
                    "AC Service by Sri Vel Enterprises.\nBrand: ${job.acBrand} (${job.acType})\nTechnician: ${job.assignedTechnicianName ?: "To be assigned"}\nContact: ${job.customerPhone}\nDetails: ${job.issueDescription}"
                )
                .putExtra(CalendarContract.Events.EVENT_LOCATION, job.customerAddress)
                .putExtra(CalendarContract.Events.AVAILABILITY, CalendarContract.Events.AVAILABILITY_BUSY)
                .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)

            context.startActivity(intent)
        } catch (e: Exception) {
            Toast.makeText(context, "Calendar app not available: ${e.message}", Toast.LENGTH_SHORT).show()
        }
    }
}
