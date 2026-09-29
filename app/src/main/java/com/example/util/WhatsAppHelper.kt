package com.example.util

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.widget.Toast
import com.example.data.model.Invoice
import com.example.data.model.ServiceJob
import java.net.URLEncoder

object WhatsAppHelper {

    fun sendAppointmentReminder(context: Context, job: ServiceJob) {
        val cleanPhone = formatPhoneNumber(job.customerPhone)
        val message = """
            ❄️ *SRI VEL ENTERPRISES - AC SERVICES & REPAIRS*
            
            Hello ${job.customerName},
            This is an appointment reminder for your upcoming AC service:
            
            📅 *Date:* ${job.scheduledDate}
            ⏰ *Time Slot:* ${job.scheduledTimeSlot}
            🔧 *Service:* ${job.serviceCategory}
            🏢 *AC Unit:* ${job.acBrand} (${job.acType})
            👨‍🔧 *Assigned Technician:* ${job.assignedTechnicianName ?: "Sri Vel Certified Technician"}
            
            Our technician will call prior to arrival. For any adjustments, reply directly or call +91 98401 23456.
        """.trimIndent()

        openWhatsApp(context, cleanPhone, message)
    }

    fun sendTechnicianEnRoute(context: Context, job: ServiceJob, techName: String) {
        val cleanPhone = formatPhoneNumber(job.customerPhone)
        val message = """
            🚗 *TECHNICIAN EN ROUTE - SRI VEL ENTERPRISES*
            
            Hello ${job.customerName},
            Your technician *${techName}* has departed and is en route to your location:
            📍 ${job.customerAddress}
            
            Job Reference: #${job.bookingNumber}
            Expected Arrival: Within 25-35 minutes.
            
            Need assistance? Contact Sri Vel Support: +91 98401 23456.
        """.trimIndent()

        openWhatsApp(context, cleanPhone, message)
    }

    fun sendInvoicePaymentReminder(context: Context, invoice: Invoice) {
        val cleanPhone = formatPhoneNumber(invoice.customerPhone)
        val message = """
            🧾 *DIGITAL INVOICE & PAYMENT - SRI VEL ENTERPRISES*
            
            Dear ${invoice.customerName},
            Your AC service has been successfully completed!
            
            📄 *Invoice No:* ${invoice.invoiceNumber}
            💰 *Total Amount Due:* ₹${invoice.totalAmount}
            📊 *Status:* ${invoice.paymentStatus.label}
            
            *Fast Pay via BHIM / Google Pay / PhonePe / Paytm:*
            UPI ID: `srivelenterprises@icici`
            (Or scan the QR code in your Sri Vel mobile portal)
            
            🛡️ *Warranty:* Includes 30-Day Cooling & Gas Warranty.
            Thank you for choosing Sri Vel Enterprises AC Services!
        """.trimIndent()

        openWhatsApp(context, cleanPhone, message)
    }

    private fun formatPhoneNumber(phone: String): String {
        val digits = phone.filter { it.isDigit() }
        return if (digits.length == 10) "91$digits" else digits
    }

    private fun openWhatsApp(context: Context, formattedPhone: String, message: String) {
        try {
            val encodedMsg = URLEncoder.encode(message, "UTF-8")
            val uri = Uri.parse("https://api.whatsapp.com/send?phone=$formattedPhone&text=$encodedMsg")
            val intent = Intent(Intent.ACTION_VIEW, uri).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK
            }
            context.startActivity(intent)
        } catch (e: Exception) {
            Toast.makeText(context, "Could not open WhatsApp: ${e.localizedMessage}", Toast.LENGTH_SHORT).show()
        }
    }
}
