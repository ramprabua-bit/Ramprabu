package com.example.data.repository

import com.example.data.db.AppDatabase
import com.example.data.model.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext

class AppRepository(private val database: AppDatabase) {

    private val techDao = database.technicianDao()
    private val jobDao = database.serviceJobDao()
    private val invoiceDao = database.invoiceDao()
    private val reviewDao = database.customerReviewDao()
    private val inquiryDao = database.serviceInquiryDao()
    private val notificationDao = database.appNotificationDao()

    val allTechnicians: Flow<List<Technician>> = techDao.getAllTechnicians()
    val allJobs: Flow<List<ServiceJob>> = jobDao.getAllJobs()
    val allInvoices: Flow<List<Invoice>> = invoiceDao.getAllInvoices()
    val allReviews: Flow<List<CustomerReview>> = reviewDao.getAllReviews()
    val allInquiries: Flow<List<ServiceInquiry>> = inquiryDao.getAllInquiries()
    val allNotifications: Flow<List<AppNotification>> = notificationDao.getAllNotifications()

    fun getCustomerJobs(phone: String): Flow<List<ServiceJob>> = jobDao.getJobsByCustomerPhone(phone)
    fun getCustomerInvoices(phone: String): Flow<List<Invoice>> = invoiceDao.getInvoicesByCustomerPhone(phone)

    suspend fun checkAndSeedInitialData() = withContext(Dispatchers.IO) {
        if (techDao.count() == 0) {
            seedTechnicians()
            seedJobsAndInvoices()
            seedReviews()
            seedInquiries()
            seedNotifications()
        }
    }

    private suspend fun seedTechnicians() {
        val list = listOf(
            Technician(
                name = "Karthik Raja",
                phone = "+91 98401 23456",
                email = "karthik.ac@srivel.com",
                experienceYears = 8,
                rating = 4.9f,
                status = TechnicianStatus.ON_JOB,
                specializations = "Inverter AC, Gas Charging, VRV/VRF",
                completedJobsCount = 428,
                currentArea = "Anna Nagar & Kilpauk"
            ),
            Technician(
                name = "Rajesh Sundaram",
                phone = "+91 98402 34567",
                email = "rajesh.ac@srivel.com",
                experienceYears = 6,
                rating = 4.8f,
                status = TechnicianStatus.EN_ROUTE,
                specializations = "Split AC Deep Jet Wash, PCB Board Fix",
                completedJobsCount = 312,
                currentArea = "T. Nagar & Nungambakkam"
            ),
            Technician(
                name = "Suresh Babu",
                phone = "+91 98403 45678",
                email = "suresh.ac@srivel.com",
                experienceYears = 10,
                rating = 4.95f,
                status = TechnicianStatus.AVAILABLE,
                specializations = "Cassette & Ductable AC, Compressor Overhaul",
                completedJobsCount = 614,
                currentArea = "Velachery & OMR"
            ),
            Technician(
                name = "Vignesh Kumar",
                phone = "+91 98404 56789",
                email = "vignesh.ac@srivel.com",
                experienceYears = 4,
                rating = 4.7f,
                status = TechnicianStatus.AVAILABLE,
                specializations = "Copper Piping, Installation & Dismantling",
                completedJobsCount = 185,
                currentArea = "Adyar & Thiruvanmiyur"
            ),
            Technician(
                name = "Muthu Pandi",
                phone = "+91 98405 67890",
                email = "muthu.ac@srivel.com",
                experienceYears = 5,
                rating = 4.85f,
                status = TechnicianStatus.OFF_DUTY,
                specializations = "Filter Deep Cleansing, Leak Detection",
                completedJobsCount = 270,
                currentArea = "Tambaram & Chromepet"
            )
        )
        techDao.insertTechnicians(list)
    }

    private suspend fun seedJobsAndInvoices() {
        val jobs = listOf(
            ServiceJob(
                bookingNumber = "SV-2026-1042",
                customerName = "Arun Ramanathan",
                customerPhone = "9840011223",
                customerAddress = "Flat 4B, Ceebros Heights, 1st Cross, Anna Nagar",
                acBrand = "Daikin",
                acType = "1.5 Ton 5-Star Split AC",
                serviceCategory = "Deep Jet Cleaning & Filter Wash",
                scheduledDate = "2026-09-29",
                scheduledTimeSlot = "10:00 AM - 12:00 PM",
                assignedTechnicianId = 1L,
                assignedTechnicianName = "Karthik Raja",
                status = JobStatus.IN_PROGRESS,
                isEmergency = false,
                issueDescription = "Cooling slow, foul smell from blower vent"
            ),
            ServiceJob(
                bookingNumber = "SV-2026-1043",
                customerName = "Meenakshi Sundaram",
                customerPhone = "9840022334",
                customerAddress = "12/4, Gandhi Road, T. Nagar",
                acBrand = "Voltas",
                acType = "2.0 Ton Inverter Split AC",
                serviceCategory = "Emergency Breakdown / Gas Leak Fix",
                scheduledDate = "2026-09-29",
                scheduledTimeSlot = "12:30 PM - 02:00 PM",
                assignedTechnicianId = 2L,
                assignedTechnicianName = "Rajesh Sundaram",
                status = JobStatus.EN_ROUTE,
                isEmergency = true,
                issueDescription = "AC tripping MCB instantly, zero cooling in master bedroom"
            ),
            ServiceJob(
                bookingNumber = "SV-2026-1044",
                customerName = "Dr. Vijay Anand",
                customerPhone = "9840033445",
                customerAddress = "Clinic No. 8, OMR Main Road, Kandanchavadi",
                acBrand = "Blue Star",
                acType = "3.0 Ton Cassette AC",
                serviceCategory = "Annual Maintenance & Compressor Inspection",
                scheduledDate = "2026-09-29",
                scheduledTimeSlot = "03:30 PM - 05:30 PM",
                assignedTechnicianId = 3L,
                assignedTechnicianName = "Suresh Babu",
                status = JobStatus.SCHEDULED,
                isEmergency = false,
                issueDescription = "Routine quarterly AMC inspection & coil sanitization"
            ),
            ServiceJob(
                bookingNumber = "SV-2026-1040",
                customerName = "Priya Krishnan",
                customerPhone = "9840044556",
                customerAddress = "7A, Kasturibai Nagar, Adyar",
                acBrand = "LG Dual Inverter",
                acType = "1.5 Ton Split AC",
                serviceCategory = "PCB Board Repair & Sensor Replacement",
                scheduledDate = "2026-09-28",
                scheduledTimeSlot = "02:00 PM - 04:00 PM",
                assignedTechnicianId = 1L,
                assignedTechnicianName = "Karthik Raja",
                status = JobStatus.COMPLETED,
                isEmergency = false,
                issueDescription = "CH21 error code displaying on front LED",
                completedAt = System.currentTimeMillis() - 86400000L
            ),
            ServiceJob(
                bookingNumber = "SV-2026-1045",
                customerName = "Kavitha Narayanan",
                customerPhone = "9840199887",
                customerAddress = "Flat 3A, 4th Avenue, Anna Nagar",
                acBrand = "Daikin",
                acType = "1.5 Ton Split AC",
                serviceCategory = "Gas Leakage & Nitrogen Pressure Test",
                scheduledDate = "2026-09-29",
                scheduledTimeSlot = "Immediate / Urgent",
                assignedTechnicianId = null,
                assignedTechnicianName = null,
                status = JobStatus.SCHEDULED,
                isEmergency = true,
                issueDescription = "Hissing sound from outdoor unit, AC blowing warm air"
            ),
            ServiceJob(
                bookingNumber = "SV-2026-1046",
                customerName = "Manoj Prabhakar",
                customerPhone = "9840588112",
                customerAddress = "Villa 18, Greenways Road, R.A. Puram",
                acBrand = "Hitachi",
                acType = "2.0 Ton Inverter Split AC",
                serviceCategory = "Deep Foam Jet Wash & Coil Sanitization",
                scheduledDate = "2026-09-29",
                scheduledTimeSlot = "04:00 PM - 06:00 PM",
                assignedTechnicianId = null,
                assignedTechnicianName = null,
                status = JobStatus.SCHEDULED,
                isEmergency = false,
                issueDescription = "Water dripping from indoor blower unit on bedroom wall"
            )
        )
        jobDao.insertJobs(jobs)

        val completedInvoiceItems = listOf(
            InvoiceLineItem("LG Inverter PCB Board Diagnostics & Micro-soldering", 1, 1450.0),
            InvoiceLineItem("Thermistor Room Sensor Replacement", 1, 450.0),
            InvoiceLineItem("High Pressure Deep Jet Wash Service", 1, 650.0)
        )
        val subtotal1 = completedInvoiceItems.sumOf { it.total }
        val tax1 = subtotal1 * 0.18
        val total1 = subtotal1 + tax1 - 100.0

        val invoice1 = Invoice(
            invoiceNumber = "INV-SV-2026-8910",
            jobId = 4L,
            customerName = "Priya Krishnan",
            customerPhone = "9840044556",
            customerAddress = "7A, Kasturibai Nagar, Adyar",
            invoiceDate = "28 Sep 2026",
            lineItemsJson = InvoiceLineItemSerializer.serialize(completedInvoiceItems),
            subtotal = subtotal1,
            taxRatePercent = 18.0,
            taxAmount = tax1,
            discountAmount = 100.0,
            totalAmount = total1,
            paymentStatus = PaymentStatus.PAID_UPI,
            upiTransactionRef = "UPI/627192849102@okaxis",
            paymentMethod = "BHIM UPI",
            paidAt = System.currentTimeMillis() - 80000000L
        )

        val pendingInvoiceItems = listOf(
            InvoiceLineItem("High Pressure Foam Jet Cleaning (Indoor & Outdoor)", 1, 799.0),
            InvoiceLineItem("Anti-bacterial Chemical Coil Treatment", 1, 350.0)
        )
        val subtotal2 = pendingInvoiceItems.sumOf { it.total }
        val tax2 = subtotal2 * 0.18
        val total2 = subtotal2 + tax2

        val invoice2 = Invoice(
            invoiceNumber = "INV-SV-2026-8911",
            jobId = 1L,
            customerName = "Arun Ramanathan",
            customerPhone = "9840011223",
            customerAddress = "Flat 4B, Ceebros Heights, 1st Cross, Anna Nagar",
            invoiceDate = "29 Sep 2026",
            lineItemsJson = InvoiceLineItemSerializer.serialize(pendingInvoiceItems),
            subtotal = subtotal2,
            taxRatePercent = 18.0,
            taxAmount = tax2,
            discountAmount = 0.0,
            totalAmount = total2,
            paymentStatus = PaymentStatus.PENDING,
            paymentMethod = "BHIM UPI / Stripe"
        )

        invoiceDao.insertInvoices(listOf(invoice1, invoice2))
    }

    private suspend fun seedReviews() {
        val reviews = listOf(
            CustomerReview(
                jobId = 4L,
                customerName = "Priya Krishnan",
                technicianName = "Karthik Raja",
                overallRating = 5.0f,
                coolingQualityRating = 5.0f,
                punctualityRating = 5.0f,
                cleanlinessRating = 4.8f,
                comment = "Excellent service! Fixed the CH21 error quickly and did a thorough jet wash without splashing water on the wall. Sri Vel Enterprises is highly trustworthy.",
                createdAt = System.currentTimeMillis() - 70000000L
            ),
            CustomerReview(
                jobId = 101L,
                customerName = "Senthil Kumar",
                technicianName = "Suresh Babu",
                overallRating = 4.9f,
                coolingQualityRating = 5.0f,
                punctualityRating = 4.7f,
                cleanlinessRating = 5.0f,
                comment = "Suresh arrived on time with proper pressure jet equipment. The AC is chilling like day one now. BHIM UPI payment was smooth and received invoice PDF immediately!",
                createdAt = System.currentTimeMillis() - 150000000L
            )
        )
        reviewDao.insertReviews(reviews)
    }

    private suspend fun seedInquiries() {
        val inquiries = listOf(
            ServiceInquiry(
                name = "Kavitha Narayanan",
                phone = "9840199887",
                preferredDate = "Tomorrow Morning",
                serviceRequired = "Split AC Uninstallation & Re-installation in new apartment",
                message = "Moving from Anna Nagar to Mogappair, need copper piping extension and bracket fitting.",
                status = "Pending"
            ),
            ServiceInquiry(
                name = "Sridhar V.",
                phone = "9840288776",
                preferredDate = "This Weekend",
                serviceRequired = "Commercial Cassette AC Gas Top-up",
                message = "Office space in Guindy, 4 units need refrigerant pressure check.",
                status = "Contacted"
            )
        )
        inquiryDao.insertInquiries(inquiries)
    }

    private suspend fun seedNotifications() {
        val notifications = listOf(
            AppNotification(
                title = "Emergency Request Received",
                message = "Customer Meenakshi Sundaram requested immediate emergency AC repair in T. Nagar (MCB Tripping).",
                category = "EMERGENCY"
            ),
            AppNotification(
                title = "Technician En Route",
                message = "Rajesh Sundaram has departed for Job #SV-2026-1043.",
                category = "STATUS"
            ),
            AppNotification(
                title = "Payment Received ₹2,401",
                message = "Invoice #INV-SV-2026-8910 successfully paid via BHIM UPI by Priya Krishnan.",
                category = "INVOICE"
            ),
            AppNotification(
                title = "Automated Service Reminder",
                message = "14 AC maintenance reminders dispatched for 6-month preventive service.",
                category = "REMINDER"
            )
        )
        notificationDao.insertNotifications(notifications)
    }

    // Mutating actions
    suspend fun insertJob(job: ServiceJob): Long = jobDao.insertJob(job)
    suspend fun updateJob(job: ServiceJob) = jobDao.updateJob(job)
    suspend fun updateJobStatus(id: Long, status: JobStatus, completedAt: Long? = null) =
        jobDao.updateJobStatus(id, status, completedAt)
    suspend fun deleteJob(job: ServiceJob) = jobDao.deleteJob(job)

    suspend fun updateTechnicianStatus(id: Long, status: TechnicianStatus) =
        techDao.updateTechnicianStatus(id, status)
    suspend fun updateTechnicianArea(id: Long, area: String) =
        techDao.updateTechnicianArea(id, area)
    suspend fun assignTechnicianToJob(jobId: Long, techId: Long?, techName: String?, status: JobStatus) =
        jobDao.assignTechnician(jobId, techId, techName, status)
    suspend fun insertTechnician(technician: Technician): Long =
        techDao.insertTechnician(technician)

    suspend fun insertInvoice(invoice: Invoice): Long = invoiceDao.insertInvoice(invoice)
    suspend fun markInvoicePaid(id: Long, status: PaymentStatus, ref: String?) =
        invoiceDao.markInvoicePaid(id, status, ref, System.currentTimeMillis())

    suspend fun insertReview(review: CustomerReview): Long = reviewDao.insertReview(review)
    suspend fun insertInquiry(inquiry: ServiceInquiry): Long = inquiryDao.insertInquiry(inquiry)

    suspend fun insertNotification(notification: AppNotification): Long =
        notificationDao.insertNotification(notification)
    suspend fun markNotificationAsRead(id: Long) = notificationDao.markAsRead(id)
    suspend fun markAllNotificationsAsRead() = notificationDao.markAllAsRead()
    suspend fun clearNotifications() = notificationDao.clearAll()
}
