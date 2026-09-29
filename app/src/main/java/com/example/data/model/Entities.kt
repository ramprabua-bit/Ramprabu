package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import org.json.JSONArray
import org.json.JSONObject

@Entity(tableName = "technicians")
data class Technician(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val phone: String,
    val email: String,
    val experienceYears: Int,
    val rating: Float,
    val status: TechnicianStatus,
    val specializations: String,
    val completedJobsCount: Int = 0,
    val currentArea: String = "Central Zone"
)

@Entity(tableName = "service_jobs")
data class ServiceJob(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val bookingNumber: String,
    val customerName: String,
    val customerPhone: String,
    val customerAddress: String,
    val acBrand: String,
    val acType: String,
    val serviceCategory: String,
    val scheduledDate: String,
    val scheduledTimeSlot: String,
    val assignedTechnicianId: Long? = null,
    val assignedTechnicianName: String? = null,
    val status: JobStatus = JobStatus.SCHEDULED,
    val isEmergency: Boolean = false,
    val issueDescription: String = "",
    val createdAt: Long = System.currentTimeMillis(),
    val completedAt: Long? = null
)

@Entity(tableName = "invoices")
data class Invoice(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val invoiceNumber: String,
    val jobId: Long,
    val customerName: String,
    val customerPhone: String,
    val customerAddress: String,
    val invoiceDate: String,
    val lineItemsJson: String, // serialized InvoiceLineItem list
    val subtotal: Double,
    val taxRatePercent: Double = 18.0, // Standard GST
    val taxAmount: Double,
    val discountAmount: Double = 0.0,
    val totalAmount: Double,
    val paymentStatus: PaymentStatus = PaymentStatus.PENDING,
    val upiTransactionRef: String? = null,
    val paymentMethod: String = "BHIM UPI / Stripe",
    val paidAt: Long? = null
)

data class InvoiceLineItem(
    val description: String,
    val quantity: Int,
    val unitPrice: Double
) {
    val total: Double get() = quantity * unitPrice
}

object InvoiceLineItemSerializer {
    fun serialize(items: List<InvoiceLineItem>): String {
        val array = JSONArray()
        items.forEach { item ->
            val obj = JSONObject()
            obj.put("desc", item.description)
            obj.put("qty", item.quantity)
            obj.put("price", item.unitPrice)
            array.put(obj)
        }
        return array.toString()
    }

    fun deserialize(json: String): List<InvoiceLineItem> {
        val list = mutableListOf<InvoiceLineItem>()
        try {
            val array = JSONArray(json)
            for (i in 0 until array.length()) {
                val obj = array.getJSONObject(i)
                list.add(
                    InvoiceLineItem(
                        description = obj.optString("desc", "Service"),
                        quantity = obj.optInt("qty", 1),
                        unitPrice = obj.optDouble("price", 0.0)
                    )
                )
            }
        } catch (_: Exception) {
            list.add(InvoiceLineItem("General AC Service", 1, 850.0))
        }
        return list
    }
}

@Entity(tableName = "customer_reviews")
data class CustomerReview(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val jobId: Long,
    val customerName: String,
    val technicianName: String,
    val overallRating: Float,
    val coolingQualityRating: Float,
    val punctualityRating: Float,
    val cleanlinessRating: Float,
    val comment: String,
    val createdAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "service_inquiries")
data class ServiceInquiry(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val phone: String,
    val preferredDate: String,
    val serviceRequired: String,
    val message: String,
    val status: String = "Pending",
    val timestamp: Long = System.currentTimeMillis()
)

@Entity(tableName = "app_notifications")
data class AppNotification(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val title: String,
    val message: String,
    val category: String, // "APPOINTMENT", "STATUS", "INVOICE", "EMERGENCY"
    val timestamp: Long = System.currentTimeMillis(),
    val isRead: Boolean = false,
    val relatedJobId: Long? = null
)
