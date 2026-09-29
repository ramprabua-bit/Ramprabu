package com.example.ui.screens

import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.JobStatus
import com.example.data.model.ServiceJob
import com.example.ui.components.JobStatusChip
import com.example.util.CalendarHelper
import com.example.util.WhatsAppHelper

@Composable
fun ScheduleScreen(
    jobs: List<ServiceJob>,
    onScheduleNewClick: () -> Unit,
    onUpdateStatus: (ServiceJob, JobStatus) -> Unit,
    onCreateInvoiceForJob: (ServiceJob) -> Unit
) {
    val context = LocalContext.current
    var selectedFilter by remember { mutableStateOf("ALL") }

    val filteredJobs = remember(jobs, selectedFilter) {
        when (selectedFilter) {
            "SCHEDULED" -> jobs.filter { it.status == JobStatus.SCHEDULED }
            "ACTIVE" -> jobs.filter { it.status == JobStatus.EN_ROUTE || it.status == JobStatus.IN_PROGRESS }
            "COMPLETED" -> jobs.filter { it.status == JobStatus.COMPLETED }
            "EMERGENCY" -> jobs.filter { it.isEmergency }
            else -> jobs
        }
    }

    Scaffold(
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = onScheduleNewClick,
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = Color.White
            ) {
                Icon(Icons.Filled.Add, contentDescription = null)
                Spacer(modifier = Modifier.width(6.dp))
                Text("Schedule Service", fontWeight = FontWeight.Bold)
            }
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            // Filter chips
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp)
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                FilterChip(
                    selected = selectedFilter == "ALL",
                    onClick = { selectedFilter = "ALL" },
                    label = { Text("All (${jobs.size})") }
                )
                FilterChip(
                    selected = selectedFilter == "SCHEDULED",
                    onClick = { selectedFilter = "SCHEDULED" },
                    label = { Text("Scheduled (${jobs.count { it.status == JobStatus.SCHEDULED }})") }
                )
                FilterChip(
                    selected = selectedFilter == "ACTIVE",
                    onClick = { selectedFilter = "ACTIVE" },
                    label = { Text("Active (${jobs.count { it.status == JobStatus.EN_ROUTE || it.status == JobStatus.IN_PROGRESS }})") }
                )
                FilterChip(
                    selected = selectedFilter == "COMPLETED",
                    onClick = { selectedFilter = "COMPLETED" },
                    label = { Text("Completed (${jobs.count { it.status == JobStatus.COMPLETED }})") }
                )
                FilterChip(
                    selected = selectedFilter == "EMERGENCY",
                    onClick = { selectedFilter = "EMERGENCY" },
                    label = { Text("🚨 Emergency (${jobs.count { it.isEmergency }})") }
                )
            }

            if (filteredJobs.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(32.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(
                            Icons.Outlined.EventBusy,
                            contentDescription = null,
                            modifier = Modifier.size(48.dp),
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            "No bookings found in this category.",
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            } else {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    contentPadding = PaddingValues(bottom = 80.dp, top = 6.dp)
                ) {
                    items(filteredJobs, key = { it.id }) { job ->
                        JobScheduleCard(
                            job = job,
                            onDialPhone = {
                                val intent = Intent(Intent.ACTION_DIAL, Uri.parse("tel:${job.customerPhone}"))
                                context.startActivity(intent)
                            },
                            onWhatsAppReminder = {
                                WhatsAppHelper.sendAppointmentReminder(context, job)
                            },
                            onAddToCalendar = {
                                CalendarHelper.addServiceAppointmentToCalendar(context, job)
                            },
                            onUpdateStatus = { nextStatus ->
                                onUpdateStatus(job, nextStatus)
                            },
                            onCreateInvoice = {
                                onCreateInvoiceForJob(job)
                            }
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun JobScheduleCard(
    job: ServiceJob,
    onDialPhone: () -> Unit,
    onWhatsAppReminder: () -> Unit,
    onAddToCalendar: () -> Unit,
    onUpdateStatus: (JobStatus) -> Unit,
    onCreateInvoice: () -> Unit
) {
    Card(
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            // Card Top Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "#${job.bookingNumber}",
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary,
                    fontSize = 14.sp
                )
                JobStatusChip(status = job.status, isEmergency = job.isEmergency)
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Customer Name & Phone
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = job.customerName,
                        fontWeight = FontWeight.Bold,
                        style = MaterialTheme.typography.titleMedium
                    )
                    Text(
                        text = job.customerAddress,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                IconButton(
                    onClick = onDialPhone,
                    colors = IconButtonDefaults.iconButtonColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
                ) {
                    Icon(Icons.Filled.Phone, contentDescription = "Call Customer", tint = MaterialTheme.colorScheme.primary)
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Service details box
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f), RoundedCornerShape(10.dp))
                    .padding(10.dp)
            ) {
                Column {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Filled.AcUnit, contentDescription = null, modifier = Modifier.size(16.dp), tint = MaterialTheme.colorScheme.primary)
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "${job.acBrand} • ${job.acType}",
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 12.sp
                        )
                    }
                    Text(
                        text = "Service: ${job.serviceCategory}",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.Medium
                    )
                    if (job.issueDescription.isNotBlank()) {
                        Text(
                            text = "Issue: ${job.issueDescription}",
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Date & Technician
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Filled.CalendarMonth, contentDescription = null, modifier = Modifier.size(14.dp), tint = Color.Gray)
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("${job.scheduledDate} (${job.scheduledTimeSlot})", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurface)
                }
                Text(
                    text = "Tech: ${job.assignedTechnicianName ?: "Pending"}",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.primary
                )
            }

            Spacer(modifier = Modifier.height(12.dp))
            HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.2f))
            Spacer(modifier = Modifier.height(10.dp))

            // Action Buttons
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                // Reminder via WhatsApp
                OutlinedButton(
                    onClick = onWhatsAppReminder,
                    modifier = Modifier.weight(1f),
                    contentPadding = PaddingValues(horizontal = 4.dp, vertical = 6.dp)
                ) {
                    Icon(Icons.Filled.Share, contentDescription = null, modifier = Modifier.size(14.dp), tint = Color(0xFF25D366))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Reminder", fontSize = 11.sp)
                }

                // Add to Calendar
                OutlinedButton(
                    onClick = onAddToCalendar,
                    modifier = Modifier.weight(1f),
                    contentPadding = PaddingValues(horizontal = 4.dp, vertical = 6.dp)
                ) {
                    Icon(Icons.Filled.Event, contentDescription = null, modifier = Modifier.size(14.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Calendar", fontSize = 11.sp)
                }

                // Status Transition
                when (job.status) {
                    JobStatus.SCHEDULED -> {
                        Button(
                            onClick = { onUpdateStatus(JobStatus.EN_ROUTE) },
                            modifier = Modifier.weight(1.2f),
                            contentPadding = PaddingValues(horizontal = 4.dp, vertical = 6.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFF57C00))
                        ) {
                            Text("En Route", fontSize = 11.sp)
                        }
                    }
                    JobStatus.EN_ROUTE -> {
                        Button(
                            onClick = { onUpdateStatus(JobStatus.IN_PROGRESS) },
                            modifier = Modifier.weight(1.2f),
                            contentPadding = PaddingValues(horizontal = 4.dp, vertical = 6.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF0277BD))
                        ) {
                            Text("Start Service", fontSize = 11.sp)
                        }
                    }
                    JobStatus.IN_PROGRESS -> {
                        Button(
                            onClick = { onUpdateStatus(JobStatus.COMPLETED) },
                            modifier = Modifier.weight(1.2f),
                            contentPadding = PaddingValues(horizontal = 4.dp, vertical = 6.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2E7D32))
                        ) {
                            Text("Complete", fontSize = 11.sp)
                        }
                    }
                    JobStatus.COMPLETED -> {
                        Button(
                            onClick = onCreateInvoice,
                            modifier = Modifier.weight(1.2f),
                            contentPadding = PaddingValues(horizontal = 4.dp, vertical = 6.dp)
                        ) {
                            Text("Invoice", fontSize = 11.sp)
                        }
                    }
                    else -> {}
                }
            }
        }
    }
}
