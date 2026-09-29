package com.example.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.db.AppDatabase
import com.example.data.model.*
import com.example.data.repository.AppRepository
import com.example.util.NotificationHelper
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class MainViewModel(application: Application) : AndroidViewModel(application) {

    private val repository = AppRepository(AppDatabase.getInstance(application))

    // UI and Session State
    private val _currentTab = MutableStateFlow(NavTab.DASHBOARD)
    val currentTab: StateFlow<NavTab> = _currentTab.asStateFlow()

    private val _userRole = MutableStateFlow(UserRole.ADMIN_DISPATCHER)
    val userRole: StateFlow<UserRole> = _userRole.asStateFlow()

    private val _currentLanguage = MutableStateFlow(AppLanguage.ENGLISH)
    val currentLanguage: StateFlow<AppLanguage> = _currentLanguage.asStateFlow()

    private val _isDarkMode = MutableStateFlow(false)
    val isDarkMode: StateFlow<Boolean> = _isDarkMode.asStateFlow()

    private val _isLoggedIn = MutableStateFlow(true) // Start authenticated for smooth preview
    val isLoggedIn: StateFlow<Boolean> = _isLoggedIn.asStateFlow()

    private val _userPhone = MutableStateFlow("9840011223")
    val userPhone: StateFlow<String> = _userPhone.asStateFlow()

    private val _userName = MutableStateFlow("Sri Vel Admin")
    val userName: StateFlow<String> = _userName.asStateFlow()

    private val _selectedInvoiceForDetails = MutableStateFlow<Invoice?>(null)
    val selectedInvoiceForDetails: StateFlow<Invoice?> = _selectedInvoiceForDetails.asStateFlow()

    // Database reactive streams
    val technicians: StateFlow<List<Technician>> = repository.allTechnicians
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val jobs: StateFlow<List<ServiceJob>> = repository.allJobs
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val invoices: StateFlow<List<Invoice>> = repository.allInvoices
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val reviews: StateFlow<List<CustomerReview>> = repository.allReviews
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val inquiries: StateFlow<List<ServiceInquiry>> = repository.allInquiries
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val notifications: StateFlow<List<AppNotification>> = repository.allNotifications
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    init {
        NotificationHelper.createNotificationChannel(application)
        viewModelScope.launch {
            repository.checkAndSeedInitialData()
        }
    }

    fun setTab(tab: NavTab) {
        _currentTab.value = tab
    }

    fun setUserRole(role: UserRole) {
        _userRole.value = role
        if (role == UserRole.CUSTOMER) {
            _userName.value = "Arun Ramanathan"
            _userPhone.value = "9840011223"
        } else {
            _userName.value = "Sri Vel Dispatch Admin"
            _userPhone.value = "9840123456"
        }
    }

    fun setLanguage(lang: AppLanguage) {
        _currentLanguage.value = lang
    }

    fun toggleDarkMode() {
        _isDarkMode.value = !_isDarkMode.value
    }

    fun selectInvoice(invoice: Invoice?) {
        _selectedInvoiceForDetails.value = invoice
    }

    fun login(phone: String, pass: String, role: UserRole) {
        _userPhone.value = phone
        _userRole.value = role
        _userName.value = if (role == UserRole.ADMIN_DISPATCHER) "Admin Dispatcher" else "Customer ($phone)"
        _isLoggedIn.value = true
    }

    fun loginWithBiometrics() {
        _isLoggedIn.value = true
        _userName.value = "Sri Vel Admin (Biometric)"
    }

    fun logout() {
        _isLoggedIn.value = false
    }

    fun scheduleJob(
        customerName: String,
        customerPhone: String,
        customerAddress: String,
        acBrand: String,
        acType: String,
        serviceCategory: String,
        date: String,
        timeSlot: String,
        technicianId: Long?,
        technicianName: String?,
        isEmergency: Boolean,
        issue: String
    ) {
        viewModelScope.launch {
            val bookingNum = "SV-${SimpleDateFormat("yyyyMMdd-HHmm", Locale.getDefault()).format(Date())}"
            val newJob = ServiceJob(
                bookingNumber = bookingNum,
                customerName = customerName,
                customerPhone = customerPhone,
                customerAddress = customerAddress,
                acBrand = acBrand,
                acType = acType,
                serviceCategory = serviceCategory,
                scheduledDate = date,
                scheduledTimeSlot = timeSlot,
                assignedTechnicianId = technicianId,
                assignedTechnicianName = technicianName,
                status = JobStatus.SCHEDULED,
                isEmergency = isEmergency,
                issueDescription = issue
            )
            repository.insertJob(newJob)

            val notif = AppNotification(
                title = if (isEmergency) "EMERGENCY: Job $bookingNum Scheduled" else "New Job Scheduled: $bookingNum",
                message = "$customerName requested $serviceCategory for $acBrand. Assigned to ${technicianName ?: "Pending Assignment"}.",
                category = if (isEmergency) "EMERGENCY" else "APPOINTMENT"
            )
            repository.insertNotification(notif)
            NotificationHelper.showNotification(
                getApplication(),
                notif.title,
                notif.message
            )
        }
    }

    fun updateJobStatus(job: ServiceJob, newStatus: JobStatus) {
        viewModelScope.launch {
            val completedTime = if (newStatus == JobStatus.COMPLETED) System.currentTimeMillis() else null
            repository.updateJobStatus(job.id, newStatus, completedTime)

            // If en route, update technician status
            if (newStatus == JobStatus.EN_ROUTE && job.assignedTechnicianId != null) {
                repository.updateTechnicianStatus(job.assignedTechnicianId, TechnicianStatus.EN_ROUTE)
            } else if (newStatus == JobStatus.IN_PROGRESS && job.assignedTechnicianId != null) {
                repository.updateTechnicianStatus(job.assignedTechnicianId, TechnicianStatus.ON_JOB)
            } else if (newStatus == JobStatus.COMPLETED && job.assignedTechnicianId != null) {
                repository.updateTechnicianStatus(job.assignedTechnicianId, TechnicianStatus.AVAILABLE)
            }

            val notif = AppNotification(
                title = "Job #${job.bookingNumber} Status Updated",
                message = "Status changed to: ${newStatus.label} for customer ${job.customerName}.",
                category = "STATUS"
            )
            repository.insertNotification(notif)
            NotificationHelper.showNotification(
                getApplication(),
                notif.title,
                notif.message
            )
        }
    }

    fun updateTechnicianStatus(techId: Long, status: TechnicianStatus) {
        viewModelScope.launch {
            repository.updateTechnicianStatus(techId, status)
        }
    }

    fun updateTechnicianArea(techId: Long, newArea: String) {
        viewModelScope.launch {
            repository.updateTechnicianArea(techId, newArea)
        }
    }

    fun assignTechnicianToJob(job: ServiceJob, technician: Technician, setEnRouteImmediately: Boolean = false) {
        viewModelScope.launch {
            val targetJobStatus = if (setEnRouteImmediately) JobStatus.EN_ROUTE else JobStatus.SCHEDULED
            repository.assignTechnicianToJob(job.id, technician.id, technician.name, targetJobStatus)

            // If technician was available, update status
            if (setEnRouteImmediately) {
                repository.updateTechnicianStatus(technician.id, TechnicianStatus.EN_ROUTE)
            } else if (technician.status == TechnicianStatus.AVAILABLE) {
                repository.updateTechnicianStatus(technician.id, TechnicianStatus.ON_JOB)
            }

            val notif = AppNotification(
                title = "Technician Assigned: ${technician.name}",
                message = "${technician.name} assigned to #${job.bookingNumber} for ${job.customerName} (${job.serviceCategory}).",
                category = "STATUS"
            )
            repository.insertNotification(notif)
            NotificationHelper.showNotification(
                getApplication(),
                notif.title,
                notif.message
            )
        }
    }

    fun unassignTechnicianFromJob(job: ServiceJob) {
        viewModelScope.launch {
            repository.assignTechnicianToJob(job.id, null, null, JobStatus.SCHEDULED)
            val notif = AppNotification(
                title = "Job Unassigned",
                message = "Job #${job.bookingNumber} unassigned and returned to pending dispatch queue.",
                category = "STATUS"
            )
            repository.insertNotification(notif)
        }
    }

    fun createInvoice(
        jobId: Long,
        customerName: String,
        customerPhone: String,
        customerAddress: String,
        items: List<InvoiceLineItem>,
        taxRate: Double = 18.0,
        discount: Double = 0.0
    ) {
        viewModelScope.launch {
            val subtotal = items.sumOf { it.total }
            val tax = subtotal * (taxRate / 100.0)
            val total = (subtotal + tax - discount).coerceAtLeast(0.0)
            val invNumber = "INV-SV-${System.currentTimeMillis() % 100000}"
            val dateStr = SimpleDateFormat("dd MMM yyyy", Locale.getDefault()).format(Date())

            val invoice = Invoice(
                invoiceNumber = invNumber,
                jobId = jobId,
                customerName = customerName,
                customerPhone = customerPhone,
                customerAddress = customerAddress,
                invoiceDate = dateStr,
                lineItemsJson = InvoiceLineItemSerializer.serialize(items),
                subtotal = subtotal,
                taxRatePercent = taxRate,
                taxAmount = tax,
                discountAmount = discount,
                totalAmount = total,
                paymentStatus = PaymentStatus.PENDING,
                paymentMethod = "BHIM UPI / Stripe"
            )
            repository.insertInvoice(invoice)

            val notif = AppNotification(
                title = "Invoice $invNumber Generated",
                message = "Digital invoice of ₹$total created for $customerName. BHIM QR ready.",
                category = "INVOICE"
            )
            repository.insertNotification(notif)
            NotificationHelper.showNotification(
                getApplication(),
                notif.title,
                notif.message
            )
        }
    }

    fun markInvoicePaid(invoiceId: Long, status: PaymentStatus, ref: String?) {
        viewModelScope.launch {
            repository.markInvoicePaid(invoiceId, status, ref)
            val notif = AppNotification(
                title = "Payment Confirmed!",
                message = "Invoice successfully settled via ${status.label}. Reference: ${ref ?: "N/A"}",
                category = "INVOICE"
            )
            repository.insertNotification(notif)
            NotificationHelper.showNotification(
                getApplication(),
                notif.title,
                notif.message
            )
        }
    }

    fun submitReview(
        jobId: Long,
        customerName: String,
        technicianName: String,
        overall: Float,
        cooling: Float,
        punctuality: Float,
        cleanliness: Float,
        comment: String
    ) {
        viewModelScope.launch {
            val review = CustomerReview(
                jobId = jobId,
                customerName = customerName,
                technicianName = technicianName,
                overallRating = overall,
                coolingQualityRating = cooling,
                punctualityRating = punctuality,
                cleanlinessRating = cleanliness,
                comment = comment
            )
            repository.insertReview(review)
            val notif = AppNotification(
                title = "New 5-Star Feedback Received",
                message = "$customerName rated $technicianName $overall stars: \"$comment\"",
                category = "STATUS"
            )
            repository.insertNotification(notif)
        }
    }

    fun submitInquiry(
        name: String,
        phone: String,
        preferredDate: String,
        service: String,
        msg: String
    ) {
        viewModelScope.launch {
            val inquiry = ServiceInquiry(
                name = name,
                phone = phone,
                preferredDate = preferredDate,
                serviceRequired = service,
                message = msg
            )
            repository.insertInquiry(inquiry)
            val notif = AppNotification(
                title = "New Customer Inquiry",
                message = "$name requested $service: \"$msg\"",
                category = "APPOINTMENT"
            )
            repository.insertNotification(notif)
        }
    }

    fun markAllNotificationsRead() {
        viewModelScope.launch {
            repository.markAllNotificationsAsRead()
        }
    }
}

enum class NavTab {
    DASHBOARD,
    SCHEDULE,
    DISPATCH_HUB,
    TECHNICIANS,
    INVOICES,
    REVIEWS,
    CUSTOMER_PORTAL
}
