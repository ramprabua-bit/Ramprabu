package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.*
import com.example.ui.components.JobStatusChip
import com.example.ui.components.TechnicianStatusChip
import com.example.util.LocalizationHelper
import java.util.Locale

@Composable
fun DashboardScreen(
    technicians: List<Technician>,
    jobs: List<ServiceJob>,
    invoices: List<Invoice>,
    currentLang: AppLanguage,
    onNavigateToSchedule: () -> Unit,
    onNavigateToInvoices: () -> Unit,
    onNavigateToTechnicians: () -> Unit,
    onNavigateToDispatchHub: () -> Unit,
    onScheduleNewClick: () -> Unit,
    onJobClick: (ServiceJob) -> Unit
) {
    val totalRevenue = invoices.filter { it.paymentStatus != PaymentStatus.PENDING }.sumOf { it.totalAmount }
    val pendingRevenue = invoices.filter { it.paymentStatus == PaymentStatus.PENDING }.sumOf { it.totalAmount }
    val completedJobsCount = jobs.count { it.status == JobStatus.COMPLETED }
    val activeJobsCount = jobs.count { it.status == JobStatus.IN_PROGRESS || it.status == JobStatus.EN_ROUTE }
    val emergencyJobsCount = jobs.count { it.isEmergency && it.status != JobStatus.COMPLETED }
    val unassignedJobsCount = jobs.count { it.status != JobStatus.COMPLETED && it.assignedTechnicianId == null }

    val availableTechs = technicians.count { it.status == TechnicianStatus.AVAILABLE }
    val onJobTechs = technicians.count { it.status == TechnicianStatus.ON_JOB }
    val enRouteTechs = technicians.count { it.status == TechnicianStatus.EN_ROUTE }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        contentPadding = PaddingValues(vertical = 14.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // Business Header Welcome Card
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primary),
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(18.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = "Sri Vel Enterprises",
                                color = Color.White,
                                fontWeight = FontWeight.Bold,
                                fontSize = 18.sp
                            )
                            Text(
                                text = LocalizationHelper.getString(currentLang, "tagline"),
                                color = Color.White.copy(alpha = 0.85f),
                                fontSize = 11.sp
                            )
                        }
                        Surface(
                            shape = CircleShape,
                            color = Color.White.copy(alpha = 0.2f),
                            modifier = Modifier.size(38.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(Icons.Filled.AcUnit, contentDescription = null, tint = Color.White)
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        HeaderMetric(label = "Total Revenue", value = "₹${String.format(Locale.US, "%.0f", totalRevenue)}")
                        HeaderMetric(label = "Pending Dues", value = "₹${String.format(Locale.US, "%.0f", pendingRevenue)}")
                        HeaderMetric(label = "Jobs Done", value = "$completedJobsCount")
                    }
                }
            }
        }

        // Quick Actions Row
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Button(
                    onClick = onScheduleNewClick,
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Icon(Icons.Filled.Add, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("New Booking", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                }

                OutlinedButton(
                    onClick = onNavigateToInvoices,
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Icon(Icons.Filled.ReceiptLong, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Invoices & QR", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                }
            }
        }

        // Technician Availability & Request Assignment Hub Banner
        item {
            Card(
                colors = CardDefaults.cardColors(
                    containerColor = if (unassignedJobsCount > 0) Color(0xFFFFF3E0) else MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f)
                ),
                shape = RoundedCornerShape(14.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(14.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                Icons.Filled.AssignmentInd,
                                contentDescription = null,
                                tint = if (unassignedJobsCount > 0) Color(0xFFE65100) else MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "Dispatch & Availability Hub",
                                fontWeight = FontWeight.Bold,
                                style = MaterialTheme.typography.titleSmall,
                                color = if (unassignedJobsCount > 0) Color(0xFFE65100) else MaterialTheme.colorScheme.primary
                            )
                        }
                        Text(
                            text = if (unassignedJobsCount > 0) "$unassignedJobsCount active request(s) waiting for technician assignment" else "All active requests assigned • Manage tech rosters & zones",
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    Spacer(modifier = Modifier.width(8.dp))

                    Button(
                        onClick = onNavigateToDispatchHub,
                        shape = RoundedCornerShape(10.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (unassignedJobsCount > 0) Color(0xFFE65100) else MaterialTheme.colorScheme.primary
                        )
                    ) {
                        Text("Dispatch Hub", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }

        // Real-Time Technician Status Board
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)),
                shape = RoundedCornerShape(14.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Filled.Engineering, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(20.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Technician Status Dispatch", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleSmall)
                        }
                        TextButton(onClick = onNavigateToTechnicians) {
                            Text("View All (${technicians.size})", fontSize = 12.sp)
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceAround
                    ) {
                        StatusCountBadge("Available", availableTechs, Color(0xFF2E7D32))
                        StatusCountBadge("On Job", onJobTechs, Color(0xFF0288D1))
                        StatusCountBadge("En Route", enRouteTechs, Color(0xFFF57C00))
                        StatusCountBadge("Emergency", emergencyJobsCount, Color(0xFFD32F2F))
                    }
                }
            }
        }

        // Revenue & Category Analytics
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                shape = RoundedCornerShape(14.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Text(
                        text = "Revenue & Service Breakdown",
                        fontWeight = FontWeight.Bold,
                        style = MaterialTheme.typography.titleSmall
                    )
                    Spacer(modifier = Modifier.height(10.dp))

                    CategoryBar("Deep Jet Cleaning", 0.42f, "₹38,500 (42%)", Color(0xFF0277BD))
                    CategoryBar("Gas Charging (R32/410)", 0.28f, "₹25,600 (28%)", Color(0xFF00897B))
                    CategoryBar("PCB & Electrical Repair", 0.18f, "₹16,400 (18%)", Color(0xFFF57C00))
                    CategoryBar("New Installation & AMC", 0.12f, "₹11,000 (12%)", Color(0xFF7B1FA2))

                    Spacer(modifier = Modifier.height(12.dp))
                    HorizontalDivider()
                    Spacer(modifier = Modifier.height(8.dp))

                    // Engagement insights
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column {
                            Text("Repeat Client Rate", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            Text("84.2%", fontWeight = FontWeight.Bold, color = Color(0xFF2E7D32), fontSize = 14.sp)
                        }
                        Column {
                            Text("Avg Response Time", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            Text("28 mins", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary, fontSize = 14.sp)
                        }
                        Column {
                            Text("Customer Rating", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            Text("★ 4.92 / 5", fontWeight = FontWeight.Bold, color = Color(0xFFF57C00), fontSize = 14.sp)
                        }
                    }
                }
            }
        }

        // Live Active Service Jobs
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Live Active Service Bookings",
                    fontWeight = FontWeight.Bold,
                    style = MaterialTheme.typography.titleSmall
                )
                TextButton(onClick = onNavigateToSchedule) {
                    Text("View Schedule (${jobs.size})", fontSize = 12.sp)
                }
            }
        }

        items(jobs.take(4)) { job ->
            Card(
                onClick = { onJobClick(job) },
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = job.bookingNumber,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary,
                            fontSize = 13.sp
                        )
                        JobStatusChip(status = job.status, isEmergency = job.isEmergency)
                    }

                    Spacer(modifier = Modifier.height(4.dp))

                    Text(
                        text = job.customerName,
                        fontWeight = FontWeight.SemiBold,
                        style = MaterialTheme.typography.bodyMedium
                    )
                    Text(
                        text = "${job.acBrand} • ${job.serviceCategory}",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    Spacer(modifier = Modifier.height(6.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Filled.Schedule, contentDescription = null, modifier = Modifier.size(14.dp), tint = Color.Gray)
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(job.scheduledTimeSlot, fontSize = 11.sp, color = Color.Gray)
                        }
                        Text(
                            text = "Tech: ${job.assignedTechnicianName ?: "Unassigned"}",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun HeaderMetric(label: String, value: String) {
    Column {
        Text(label, color = Color.White.copy(alpha = 0.8f), fontSize = 11.sp)
        Text(value, color = Color.White, fontWeight = FontWeight.Bold, fontSize = 16.sp)
    }
}

@Composable
private fun StatusCountBadge(label: String, count: Int, color: Color) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Surface(
            shape = CircleShape,
            color = color.copy(alpha = 0.15f),
            modifier = Modifier.size(36.dp)
        ) {
            Box(contentAlignment = Alignment.Center) {
                Text(
                    text = "$count",
                    fontWeight = FontWeight.Bold,
                    color = color,
                    fontSize = 15.sp
                )
            }
        }
        Spacer(modifier = Modifier.height(4.dp))
        Text(label, fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurface)
    }
}

@Composable
private fun CategoryBar(title: String, fraction: Float, amountText: String, color: Color) {
    Column(modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(title, fontSize = 11.sp, fontWeight = FontWeight.Medium)
            Text(amountText, fontSize = 11.sp, fontWeight = FontWeight.SemiBold, color = color)
        }
        Spacer(modifier = Modifier.height(3.dp))
        LinearProgressIndicator(
            progress = { fraction },
            modifier = Modifier
                .fillMaxWidth()
                .height(6.dp)
                .clip(RoundedCornerShape(3.dp)),
            color = color,
            trackColor = color.copy(alpha = 0.15f)
        )
    }
}
