package com.example

import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.model.Invoice
import com.example.data.model.JobStatus
import com.example.data.model.PaymentStatus
import com.example.data.model.ServiceJob
import com.example.data.model.UserRole
import com.example.ui.components.*
import com.example.ui.screens.*
import com.example.ui.theme.MyApplicationTheme
import com.example.ui.viewmodel.MainViewModel
import com.example.ui.viewmodel.NavTab

class MainActivity : ComponentActivity() {

    private val viewModel: MainViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            val isDarkMode by viewModel.isDarkMode.collectAsStateWithLifecycle()
            val isLoggedIn by viewModel.isLoggedIn.collectAsStateWithLifecycle()
            val userRole by viewModel.userRole.collectAsStateWithLifecycle()
            val currentTab by viewModel.currentTab.collectAsStateWithLifecycle()
            val currentLanguage by viewModel.currentLanguage.collectAsStateWithLifecycle()
            val userPhone by viewModel.userPhone.collectAsStateWithLifecycle()
            val userName by viewModel.userName.collectAsStateWithLifecycle()
            val selectedInvoice by viewModel.selectedInvoiceForDetails.collectAsStateWithLifecycle()

            val technicians by viewModel.technicians.collectAsStateWithLifecycle()
            val jobs by viewModel.jobs.collectAsStateWithLifecycle()
            val invoices by viewModel.invoices.collectAsStateWithLifecycle()
            val reviews by viewModel.reviews.collectAsStateWithLifecycle()
            val inquiries by viewModel.inquiries.collectAsStateWithLifecycle()
            val notifications by viewModel.notifications.collectAsStateWithLifecycle()

            val unreadCount = remember(notifications) { notifications.count { !it.isRead } }

            // Dialog and Sheet States
            var showScheduleDialog by remember { mutableStateOf(false) }
            var showCreateInvoiceDialog by remember { mutableStateOf(false) }
            var showReviewDialog by remember { mutableStateOf(false) }
            var showLanguageDialog by remember { mutableStateOf(false) }
            var showNotificationsSheet by remember { mutableStateOf(false) }
            var stripeInvoiceToPay by remember { mutableStateOf<Invoice?>(null) }
            var jobForInvoiceCreation by remember { mutableStateOf<ServiceJob?>(null) }

            MyApplicationTheme(darkTheme = isDarkMode) {
                if (!isLoggedIn) {
                    LoginScreen(
                        onLoginSuccess = { phone, pass, role ->
                            viewModel.login(phone, pass, role)
                        },
                        onBiometricLogin = {
                            viewModel.loginWithBiometrics()
                        }
                    )
                } else {
                    BackHandler(enabled = currentTab != NavTab.DASHBOARD && currentTab != NavTab.CUSTOMER_PORTAL) {
                        viewModel.setTab(if (userRole == UserRole.ADMIN_DISPATCHER) NavTab.DASHBOARD else NavTab.CUSTOMER_PORTAL)
                    }

                    Scaffold(
                        modifier = Modifier.fillMaxSize(),
                        topBar = {
                            AppTopBar(
                                role = userRole,
                                currentLang = currentLanguage,
                                isDarkMode = isDarkMode,
                                unreadNotifCount = unreadCount,
                                onRoleToggle = {
                                    val nextRole = if (userRole == UserRole.ADMIN_DISPATCHER) UserRole.CUSTOMER else UserRole.ADMIN_DISPATCHER
                                    viewModel.setUserRole(nextRole)
                                    viewModel.setTab(if (nextRole == UserRole.ADMIN_DISPATCHER) NavTab.DASHBOARD else NavTab.CUSTOMER_PORTAL)
                                },
                                onLanguageClick = { showLanguageDialog = true },
                                onDarkModeToggle = { viewModel.toggleDarkMode() },
                                onNotifClick = {
                                    showNotificationsSheet = true
                                    viewModel.markAllNotificationsRead()
                                }
                            )
                        },
                        bottomBar = {
                            AppBottomBar(
                                currentTab = currentTab,
                                role = userRole,
                                currentLang = currentLanguage,
                                onTabSelected = { viewModel.setTab(it) }
                            )
                        }
                    ) { innerPadding ->
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(innerPadding)
                        ) {
                            if (userRole == UserRole.CUSTOMER) {
                                when (currentTab) {
                                    NavTab.SCHEDULE -> {
                                        ScheduleScreen(
                                            jobs = jobs,
                                            onScheduleNewClick = { showScheduleDialog = true },
                                            onUpdateStatus = { job, status -> viewModel.updateJobStatus(job, status) },
                                            onCreateInvoiceForJob = { job ->
                                                jobForInvoiceCreation = job
                                                showCreateInvoiceDialog = true
                                            }
                                        )
                                    }
                                    NavTab.INVOICES -> {
                                        InvoicesScreen(
                                            invoices = invoices,
                                            onCreateInvoiceClick = { showCreateInvoiceDialog = true },
                                            onInvoiceClick = { viewModel.selectInvoice(it) }
                                        )
                                    }
                                    NavTab.REVIEWS -> {
                                        ReviewsAndInquiriesScreen(
                                            reviews = reviews,
                                            inquiries = inquiries,
                                            onAddReviewClick = { showReviewDialog = true }
                                        )
                                    }
                                    else -> {
                                        CustomerPortalScreen(
                                            customerName = userName,
                                            customerPhone = userPhone,
                                            jobs = jobs,
                                            invoices = invoices,
                                            onEmergencySosClick = {
                                                viewModel.scheduleJob(
                                                    customerName = userName,
                                                    customerPhone = userPhone,
                                                    customerAddress = "Current Verified Location",
                                                    acBrand = "Daikin Inverter AC",
                                                    acType = "Split AC",
                                                    serviceCategory = "Emergency Breakdown / MCB Trip Fix",
                                                    date = "Today",
                                                    timeSlot = "Within 30 Mins",
                                                    technicianId = technicians.firstOrNull { it.status == com.example.data.model.TechnicianStatus.AVAILABLE }?.id,
                                                    technicianName = technicians.firstOrNull { it.status == com.example.data.model.TechnicianStatus.AVAILABLE }?.name ?: "Emergency Dispatcher",
                                                    isEmergency = true,
                                                    issue = "SOS: Urgent AC tripping & burning smell"
                                                )
                                                Toast.makeText(this@MainActivity, "🚨 Emergency technician dispatched! Arrival in 25-30 minutes.", Toast.LENGTH_LONG).show()
                                            },
                                            onBookServiceClick = { showScheduleDialog = true },
                                            onInvoiceClick = { viewModel.selectInvoice(it) },
                                            onSubmitInquiry = { name, phone, date, service, msg ->
                                                viewModel.submitInquiry(name, phone, date, service, msg)
                                            }
                                        )
                                    }
                                }
                            } else {
                                // ADMIN DISPATCHER MODE
                                when (currentTab) {
                                    NavTab.DASHBOARD -> {
                                        DashboardScreen(
                                            technicians = technicians,
                                            jobs = jobs,
                                            invoices = invoices,
                                            currentLang = currentLanguage,
                                            onNavigateToSchedule = { viewModel.setTab(NavTab.SCHEDULE) },
                                            onNavigateToInvoices = { viewModel.setTab(NavTab.INVOICES) },
                                            onNavigateToTechnicians = { viewModel.setTab(NavTab.TECHNICIANS) },
                                            onNavigateToDispatchHub = { viewModel.setTab(NavTab.DISPATCH_HUB) },
                                            onScheduleNewClick = { showScheduleDialog = true },
                                            onJobClick = { job ->
                                                viewModel.setTab(NavTab.SCHEDULE)
                                            }
                                        )
                                    }
                                    NavTab.DISPATCH_HUB -> {
                                        TechnicianDispatchScreen(
                                            technicians = technicians,
                                            jobs = jobs,
                                            onUpdateTechnicianStatus = { id, status ->
                                                viewModel.updateTechnicianStatus(id, status)
                                            },
                                            onUpdateTechnicianArea = { id, area ->
                                                viewModel.updateTechnicianArea(id, area)
                                            },
                                            onAssignTechnician = { job, tech, setEnRoute ->
                                                viewModel.assignTechnicianToJob(job, tech, setEnRoute)
                                            },
                                            onUnassignTechnician = { job ->
                                                viewModel.unassignTechnicianFromJob(job)
                                            }
                                        )
                                    }
                                    NavTab.SCHEDULE -> {
                                        ScheduleScreen(
                                            jobs = jobs,
                                            onScheduleNewClick = { showScheduleDialog = true },
                                            onUpdateStatus = { job, status -> viewModel.updateJobStatus(job, status) },
                                            onCreateInvoiceForJob = { job ->
                                                jobForInvoiceCreation = job
                                                showCreateInvoiceDialog = true
                                            }
                                        )
                                    }
                                    NavTab.TECHNICIANS -> {
                                        TechniciansScreen(
                                            technicians = technicians,
                                            onUpdateTechnicianStatus = { id, status ->
                                                viewModel.updateTechnicianStatus(id, status)
                                            },
                                            onNavigateToDispatchHub = { viewModel.setTab(NavTab.DISPATCH_HUB) }
                                        )
                                    }
                                    NavTab.INVOICES -> {
                                        InvoicesScreen(
                                            invoices = invoices,
                                            onCreateInvoiceClick = { showCreateInvoiceDialog = true },
                                            onInvoiceClick = { viewModel.selectInvoice(it) }
                                        )
                                    }
                                    NavTab.REVIEWS -> {
                                        ReviewsAndInquiriesScreen(
                                            reviews = reviews,
                                            inquiries = inquiries,
                                            onAddReviewClick = { showReviewDialog = true }
                                        )
                                    }
                                    NavTab.CUSTOMER_PORTAL -> {
                                        CustomerPortalScreen(
                                            customerName = userName,
                                            customerPhone = userPhone,
                                            jobs = jobs,
                                            invoices = invoices,
                                            onEmergencySosClick = {
                                                showScheduleDialog = true
                                            },
                                            onBookServiceClick = { showScheduleDialog = true },
                                            onInvoiceClick = { viewModel.selectInvoice(it) },
                                            onSubmitInquiry = { name, phone, date, service, msg ->
                                                viewModel.submitInquiry(name, phone, date, service, msg)
                                            }
                                        )
                                    }
                                }
                            }
                        }
                    }

                    // Dialogs
                    if (showScheduleDialog) {
                        ScheduleJobDialog(
                            technicians = technicians,
                            onDismiss = { showScheduleDialog = false },
                            onSchedule = { name, phone, address, brand, type, service, date, slot, techId, techName, emergency, issue ->
                                viewModel.scheduleJob(
                                    name, phone, address, brand, type, service,
                                    date, slot, techId, techName, emergency, issue
                                )
                                Toast.makeText(this@MainActivity, "AC Service scheduled & technician assigned!", Toast.LENGTH_SHORT).show()
                            }
                        )
                    }

                    if (showCreateInvoiceDialog) {
                        CreateInvoiceDialog(
                            jobs = jobs,
                            onDismiss = {
                                showCreateInvoiceDialog = false
                                jobForInvoiceCreation = null
                            },
                            onCreate = { jobId, name, phone, address, items, tax, discount ->
                                viewModel.createInvoice(jobId, name, phone, address, items, tax, discount)
                                Toast.makeText(this@MainActivity, "Digital Invoice & BHIM QR created!", Toast.LENGTH_SHORT).show()
                            }
                        )
                    }

                    if (showReviewDialog) {
                        SubmitReviewDialog(
                            technicians = technicians,
                            onDismiss = { showReviewDialog = false },
                            onSubmit = { techName, overall, cooling, punctuality, cleanliness, comment ->
                                viewModel.submitReview(0L, userName, techName, overall, cooling, punctuality, cleanliness, comment)
                                Toast.makeText(this@MainActivity, "Thank you for rating Sri Vel Enterprises!", Toast.LENGTH_SHORT).show()
                            }
                        )
                    }

                    if (showLanguageDialog) {
                        LanguageSelectionDialog(
                            currentLang = currentLanguage,
                            onDismiss = { showLanguageDialog = false },
                            onSelectLanguage = { viewModel.setLanguage(it) }
                        )
                    }

                    if (showNotificationsSheet) {
                        NotificationsSheet(
                            notifications = notifications,
                            onDismiss = { showNotificationsSheet = false },
                            onClearAll = { viewModel.markAllNotificationsRead() }
                        )
                    }

                    // Selected Invoice Bottom Sheet with BHIM UPI QR Code
                    selectedInvoice?.let { invoice ->
                        InvoiceDetailSheet(
                            invoice = invoice,
                            onDismiss = { viewModel.selectInvoice(null) },
                            onMarkPaid = { inv, status, ref ->
                                viewModel.markInvoicePaid(inv.id, status, ref)
                                viewModel.selectInvoice(null)
                            },
                            onStripePayClicked = { inv ->
                                stripeInvoiceToPay = inv
                            }
                        )
                    }

                    // Stripe Card Checkout Modal
                    stripeInvoiceToPay?.let { inv ->
                        StripePaymentDialog(
                            invoice = inv,
                            onDismiss = { stripeInvoiceToPay = null },
                            onSuccess = {
                                viewModel.markInvoicePaid(inv.id, PaymentStatus.PAID_STRIPE, "STRIPE-CH_${System.currentTimeMillis() % 1000000}")
                                stripeInvoiceToPay = null
                                viewModel.selectInvoice(null)
                                Toast.makeText(this@MainActivity, "Stripe card payment successful! Digital receipt generated.", Toast.LENGTH_LONG).show()
                            }
                        )
                    }
                }
            }
        }
    }
}
