package com.example.ui.screens

import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.*
import com.example.ui.components.JobStatusChip
import com.example.ui.components.RatingStars
import com.example.ui.components.TechnicianStatusChip
import java.net.URLEncoder

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TechnicianDispatchScreen(
    technicians: List<Technician>,
    jobs: List<ServiceJob>,
    onUpdateTechnicianStatus: (Long, TechnicianStatus) -> Unit,
    onUpdateTechnicianArea: (Long, String) -> Unit,
    onAssignTechnician: (ServiceJob, Technician, Boolean) -> Unit,
    onUnassignTechnician: (ServiceJob) -> Unit
) {
    val context = LocalContext.current
    var selectedScreenTab by remember { mutableStateOf(0) } // 0: Assign Requests, 1: Manage Availability
    var selectedZoneFilter by remember { mutableStateOf("ALL") }

    // Job Assignment Sheet state
    var jobToAssign by remember { mutableStateOf<ServiceJob?>(null) }

    val activeJobs = remember(jobs) {
        jobs.filter { it.status != JobStatus.COMPLETED && it.status != JobStatus.CANCELLED }
    }

    val unassignedCount = activeJobs.count { it.assignedTechnicianId == null }
    val emergencyCount = activeJobs.count { it.isEmergency }
    val availableTechsCount = technicians.count { it.status == TechnicianStatus.AVAILABLE }
    val onJobTechsCount = technicians.count { it.status == TechnicianStatus.ON_JOB }

    val chennaiZones = listOf("ALL", "Anna Nagar", "T. Nagar", "Velachery", "Adyar", "Tambaram")

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        // Dispatch Metrics Header
        Card(
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)),
            shape = RoundedCornerShape(bottomStart = 16.dp, bottomEnd = 16.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "Dispatch & Availability Hub",
                            fontWeight = FontWeight.Bold,
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = "Real-time technician scheduling & active service assignment",
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    if (unassignedCount > 0) {
                        Surface(
                            color = Color(0xFFD32F2F),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Text(
                                text = "$unassignedCount Unassigned",
                                color = Color.White,
                                fontWeight = FontWeight.Bold,
                                fontSize = 11.sp,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // KPI counters
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    MetricMiniBadge("Available Techs", "$availableTechsCount", Color(0xFF2E7D32))
                    MetricMiniBadge("On Job", "$onJobTechsCount", Color(0xFF0277BD))
                    MetricMiniBadge("Active Jobs", "${activeJobs.size}", MaterialTheme.colorScheme.primary)
                    MetricMiniBadge("Emergency SOS", "$emergencyCount", Color(0xFFD32F2F))
                }
            }
        }

        // Main Navigation Tabs (Assign Requests vs Manage Availability)
        TabRow(
            selectedTabIndex = selectedScreenTab,
            containerColor = MaterialTheme.colorScheme.surface
        ) {
            Tab(
                selected = selectedScreenTab == 0,
                onClick = { selectedScreenTab = 0 },
                text = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Filled.AssignmentInd, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Active Requests (${activeJobs.size})")
                    }
                }
            )
            Tab(
                selected = selectedScreenTab == 1,
                onClick = { selectedScreenTab = 1 },
                text = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Filled.PeopleAlt, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Tech Availability (${technicians.size})")
                    }
                }
            )
        }

        // Zone filter chips
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp)
                .horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("Zone:", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurfaceVariant)
            chennaiZones.forEach { zone ->
                FilterChip(
                    selected = selectedZoneFilter == zone,
                    onClick = { selectedZoneFilter = zone },
                    label = { Text(zone, fontSize = 11.sp) }
                )
            }
        }

        if (selectedScreenTab == 0) {
            // TAB 0: ACTIVE SERVICE REQUESTS & ASSIGNMENT
            val filteredJobs = remember(activeJobs, selectedZoneFilter) {
                if (selectedZoneFilter == "ALL") activeJobs
                else activeJobs.filter { it.customerAddress.contains(selectedZoneFilter, ignoreCase = true) }
            }

            if (filteredJobs.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(32.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(Icons.Outlined.CheckCircle, contentDescription = null, tint = Color(0xFF2E7D32), modifier = Modifier.size(48.dp))
                        Spacer(modifier = Modifier.height(8.dp))
                        Text("All active service requests are assigned!", fontWeight = FontWeight.Bold)
                        Text("No pending dispatch in $selectedZoneFilter.", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
            } else {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    contentPadding = PaddingValues(top = 4.dp, bottom = 80.dp)
                ) {
                    items(filteredJobs, key = { it.id }) { job ->
                        ServiceRequestAssignmentCard(
                            job = job,
                            technicians = technicians,
                            onAssignClick = { jobToAssign = job },
                            onUnassignClick = { onUnassignTechnician(job) },
                            onNotifyWhatsApp = { tech ->
                                sendWhatsAppDispatchMessage(context, job, tech)
                            }
                        )
                    }
                }
            }
        } else {
            // TAB 1: MANAGE TECHNICIAN AVAILABILITY & SHIFTS
            val filteredTechs = remember(technicians, selectedZoneFilter) {
                if (selectedZoneFilter == "ALL") technicians
                else technicians.filter { it.currentArea.contains(selectedZoneFilter, ignoreCase = true) }
            }

            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
                contentPadding = PaddingValues(top = 4.dp, bottom = 80.dp)
            ) {
                items(filteredTechs, key = { it.id }) { tech ->
                    val techActiveJobs = activeJobs.filter { it.assignedTechnicianId == tech.id }
                    TechnicianAvailabilityCard(
                        technician = tech,
                        assignedJobs = techActiveJobs,
                        onStatusChange = { newStatus -> onUpdateTechnicianStatus(tech.id, newStatus) },
                        onAreaChange = { newArea -> onUpdateTechnicianArea(tech.id, newArea) }
                    )
                }
            }
        }
    }

    // Modal Sheet to Assign Technician to Job
    jobToAssign?.let { job ->
        AssignTechnicianBottomSheet(
            job = job,
            technicians = technicians,
            activeJobs = activeJobs,
            onDismiss = { jobToAssign = null },
            onAssign = { selectedTech, setEnRoute ->
                onAssignTechnician(job, selectedTech, setEnRoute)
                jobToAssign = null
                Toast.makeText(context, "Assigned ${selectedTech.name} to Job #${job.bookingNumber}", Toast.LENGTH_SHORT).show()
            }
        )
    }
}

@Composable
private fun MetricMiniBadge(title: String, count: String, color: Color) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(count, fontWeight = FontWeight.Bold, fontSize = 16.sp, color = color)
        Text(title, fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}

@Composable
private fun ServiceRequestAssignmentCard(
    job: ServiceJob,
    technicians: List<Technician>,
    onAssignClick: () -> Unit,
    onUnassignClick: () -> Unit,
    onNotifyWhatsApp: (Technician) -> Unit
) {
    val isAssigned = job.assignedTechnicianId != null
    val assignedTech = technicians.firstOrNull { it.id == job.assignedTechnicianId }

    // Recommendation logic: find available technician in same area or matching brand
    val recommendedTech = remember(job, technicians) {
        technicians.firstOrNull {
            it.status == TechnicianStatus.AVAILABLE &&
                    (job.customerAddress.contains(it.currentArea.split("&").first().trim(), ignoreCase = true) ||
                            it.specializations.contains(job.serviceCategory.split(" ").first(), ignoreCase = true))
        } ?: technicians.firstOrNull { it.status == TechnicianStatus.AVAILABLE }
    }

    Card(
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            // Header Row: Booking #, Emergency Badge, Time
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = "#${job.bookingNumber}",
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary,
                        fontSize = 14.sp
                    )
                    if (job.isEmergency) {
                        Spacer(modifier = Modifier.width(6.dp))
                        Surface(
                            color = Color(0xFFFFEBEE),
                            shape = RoundedCornerShape(6.dp)
                        ) {
                            Text(
                                text = "🚨 EMERGENCY",
                                color = Color(0xFFD32F2F),
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                            )
                        }
                    }
                }

                JobStatusChip(status = job.status, isEmergency = false)
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Customer Name & Location
            Text(
                text = job.customerName,
                fontWeight = FontWeight.Bold,
                style = MaterialTheme.typography.titleMedium
            )
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Filled.Place, contentDescription = null, modifier = Modifier.size(14.dp), tint = Color.Gray)
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = job.customerAddress,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            // AC and Service Details
            Surface(
                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f),
                shape = RoundedCornerShape(8.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(10.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = "${job.acBrand} • ${job.acType}",
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 12.sp
                        )
                        Text(
                            text = "Slot: ${job.scheduledTimeSlot}",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Medium,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                    Text(
                        text = "Service: ${job.serviceCategory}",
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.Bold
                    )
                    if (job.issueDescription.isNotBlank()) {
                        Text(
                            text = "Note: ${job.issueDescription}",
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Current Assignment or Recommendation
            if (isAssigned && assignedTech != null) {
                Surface(
                    color = Color(0xFFE8F5E9),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Filled.CheckCircle, contentDescription = null, tint = Color(0xFF2E7D32), modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Column {
                                Text(
                                    text = "Assigned to: ${assignedTech.name}",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 12.sp,
                                    color = Color(0xFF1B5E20)
                                )
                                Text(
                                    text = "Status: ${assignedTech.status.label} • ${assignedTech.currentArea}",
                                    fontSize = 10.sp,
                                    color = Color(0xFF2E7D32)
                                )
                            }
                        }

                        IconButton(
                            onClick = onUnassignClick,
                            modifier = Modifier.size(28.dp)
                        ) {
                            Icon(Icons.Filled.Close, contentDescription = "Unassign", tint = Color.Gray, modifier = Modifier.size(16.dp))
                        }
                    }
                }
            } else {
                Surface(
                    color = Color(0xFFFFF3E0),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(10.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Filled.WarningAmber, contentDescription = null, tint = Color(0xFFE65100), modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "Unassigned Active Request",
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFFE65100),
                                fontSize = 12.sp
                            )
                        }
                        if (recommendedTech != null) {
                            Text(
                                text = "💡 Match: ${recommendedTech.name} is available in ${recommendedTech.currentArea}",
                                fontSize = 11.sp,
                                color = Color(0xFFBF360C)
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Action Buttons
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Button(
                    onClick = onAssignClick,
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (isAssigned) MaterialTheme.colorScheme.surfaceVariant else MaterialTheme.colorScheme.primary,
                        contentColor = if (isAssigned) MaterialTheme.colorScheme.onSurfaceVariant else Color.White
                    ),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Icon(Icons.Filled.PersonAdd, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(if (isAssigned) "Reassign Tech" else "Assign Technician", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                }

                if (isAssigned && assignedTech != null) {
                    Button(
                        onClick = { onNotifyWhatsApp(assignedTech) },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF25D366)),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Icon(Icons.Filled.Share, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Dispatch WhatsApp", fontSize = 11.sp)
                    }
                }
            }
        }
    }
}

@Composable
private fun TechnicianAvailabilityCard(
    technician: Technician,
    assignedJobs: List<ServiceJob>,
    onStatusChange: (TechnicianStatus) -> Unit,
    onAreaChange: (String) -> Unit
) {
    val context = LocalContext.current
    var showAreaDialog by remember { mutableStateOf(false) }

    Card(
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            // Profile & Current Status
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Surface(
                    shape = CircleShape,
                    color = MaterialTheme.colorScheme.primary.copy(alpha = 0.15f),
                    modifier = Modifier.size(46.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Text(
                            text = technician.name.take(2).uppercase(),
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary,
                            fontSize = 16.sp
                        )
                    }
                }

                Spacer(modifier = Modifier.width(12.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = technician.name,
                        fontWeight = FontWeight.Bold,
                        style = MaterialTheme.typography.titleMedium
                    )
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Filled.LocationOn, contentDescription = null, modifier = Modifier.size(12.dp), tint = Color.Gray)
                        Spacer(modifier = Modifier.width(2.dp))
                        Text(
                            text = technician.currentArea,
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                TechnicianStatusChip(status = technician.status)
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Workload Counter
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f), RoundedCornerShape(8.dp))
                    .padding(horizontal = 10.dp, vertical = 6.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Current Active Workload: ${assignedJobs.size} Jobs",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = if (assignedJobs.isEmpty()) Color(0xFF2E7D32) else MaterialTheme.colorScheme.primary
                )

                TextButton(
                    onClick = { showAreaDialog = true },
                    contentPadding = PaddingValues(0.dp)
                ) {
                    Text("Change Zone", fontSize = 11.sp)
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Instant Availability Switcher
            Text("Update Availability:", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Spacer(modifier = Modifier.height(4.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                TechnicianStatus.values().forEach { st ->
                    val isSelected = technician.status == st
                    val btnBg = when (st) {
                        TechnicianStatus.AVAILABLE -> if (isSelected) Color(0xFF2E7D32) else Color(0xFFE8F5E9)
                        TechnicianStatus.ON_JOB -> if (isSelected) Color(0xFF0288D1) else Color(0xFFE1F5FE)
                        TechnicianStatus.EN_ROUTE -> if (isSelected) Color(0xFFF57C00) else Color(0xFFFFF3E0)
                        TechnicianStatus.OFF_DUTY -> if (isSelected) Color.DarkGray else Color(0xFFEEEEEE)
                    }
                    val textCol = if (isSelected) Color.White else Color.Black

                    Button(
                        onClick = { onStatusChange(st) },
                        modifier = Modifier.weight(1f),
                        contentPadding = PaddingValues(horizontal = 2.dp, vertical = 4.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = btnBg),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text(
                            text = st.label,
                            fontSize = 10.sp,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                            color = textCol,
                            maxLines = 1
                        )
                    }
                }
            }

            // If technician has assigned jobs, show quick summary
            if (assignedJobs.isNotEmpty()) {
                Spacer(modifier = Modifier.height(8.dp))
                HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.2f))
                Spacer(modifier = Modifier.height(6.dp))
                Text("Assigned Active Jobs:", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                assignedJobs.forEach { j ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 2.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "• #${j.bookingNumber} (${j.customerName})",
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = j.status.label,
                            fontSize = 10.sp,
                            color = MaterialTheme.colorScheme.primary,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }
            }
        }
    }

    if (showAreaDialog) {
        val zones = listOf("Anna Nagar & Kilpauk", "T. Nagar & Nungambakkam", "Velachery & OMR", "Adyar & Thiruvanmiyur", "Tambaram & Chromepet", "Central Zone")
        AlertDialog(
            onDismissRequest = { showAreaDialog = false },
            title = { Text("Assign Working Zone for ${technician.name}") },
            text = {
                Column {
                    zones.forEach { z ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    onAreaChange(z)
                                    showAreaDialog = false
                                }
                                .padding(vertical = 10.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            RadioButton(selected = technician.currentArea == z, onClick = {
                                onAreaChange(z)
                                showAreaDialog = false
                            })
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(z, fontSize = 13.sp)
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showAreaDialog = false }) {
                    Text("Close")
                }
            }
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun AssignTechnicianBottomSheet(
    job: ServiceJob,
    technicians: List<Technician>,
    activeJobs: List<ServiceJob>,
    onDismiss: () -> Unit,
    onAssign: (Technician, Boolean) -> Unit
) {
    var setEnRouteImmediately by remember { mutableStateOf(job.isEmergency) }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp)
                .padding(bottom = 36.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "Assign Technician",
                        fontWeight = FontWeight.Bold,
                        style = MaterialTheme.typography.titleMedium
                    )
                    Text(
                        text = "Job #${job.bookingNumber} • ${job.customerName}",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                IconButton(onClick = onDismiss) {
                    Icon(Icons.Filled.Close, contentDescription = "Close")
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            Surface(
                color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.4f),
                shape = RoundedCornerShape(8.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(10.dp)) {
                    Text("Service: ${job.serviceCategory}", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                    Text("Location: ${job.customerAddress}", fontSize = 11.sp)
                    Text("Time Slot: ${job.scheduledTimeSlot}", fontSize = 11.sp)
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Checkbox for instant En Route dispatch
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Checkbox(
                    checked = setEnRouteImmediately,
                    onCheckedChange = { setEnRouteImmediately = it }
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = "Dispatch immediately (Set status to En Route)",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.SemiBold
                )
            }

            Spacer(modifier = Modifier.height(10.dp))
            Text("Select Available Technician:", fontWeight = FontWeight.Bold, fontSize = 13.sp)
            Spacer(modifier = Modifier.height(6.dp))

            LazyColumn(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(technicians, key = { it.id }) { tech ->
                    val workload = activeJobs.count { it.assignedTechnicianId == tech.id }
                    Card(
                        onClick = { onAssign(tech, setEnRouteImmediately) },
                        shape = RoundedCornerShape(10.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text(tech.name, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                                    Spacer(modifier = Modifier.width(6.dp))
                                    TechnicianStatusChip(status = tech.status)
                                }
                                Text("Zone: ${tech.currentArea}", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                Text("Active Jobs: $workload • ★ ${tech.rating}", fontSize = 11.sp, color = MaterialTheme.colorScheme.primary)
                            }

                            Button(
                                onClick = { onAssign(tech, setEnRouteImmediately) },
                                shape = RoundedCornerShape(8.dp)
                            ) {
                                Text("Select", fontSize = 11.sp)
                            }
                        }
                    }
                }
            }
        }
    }
}

private fun sendWhatsAppDispatchMessage(context: android.content.Context, job: ServiceJob, tech: Technician) {
    try {
        val cleanPhone = tech.phone.filter { it.isDigit() }
        val targetPhone = if (cleanPhone.length == 10) "91$cleanPhone" else cleanPhone
        val message = """
            🔧 *DISPATCH ORDER - SRI VEL ENTERPRISES*
            
            Hello ${tech.name},
            You have been assigned to service booking *#${job.bookingNumber}*:
            
            👤 *Customer:* ${job.customerName}
            📞 *Customer Contact:* +91 ${job.customerPhone}
            📍 *Address:* ${job.customerAddress}
            ❄️ *AC Unit:* ${job.acBrand} (${job.acType})
            ⚙️ *Service Required:* ${job.serviceCategory}
            ⏰ *Scheduled Time:* ${job.scheduledTimeSlot}
            ${if (job.issueDescription.isNotBlank()) "📝 *Notes:* ${job.issueDescription}" else ""}
            
            Please call customer before departing. Open Sri Vel app to update status.
        """.trimIndent()

        val encoded = URLEncoder.encode(message, "UTF-8")
        val uri = Uri.parse("https://api.whatsapp.com/send?phone=$targetPhone&text=$encoded")
        val intent = Intent(Intent.ACTION_VIEW, uri).apply { flags = Intent.FLAG_ACTIVITY_NEW_TASK }
        context.startActivity(intent)
    } catch (e: Exception) {
        Toast.makeText(context, "Could not open WhatsApp: ${e.message}", Toast.LENGTH_SHORT).show()
    }
}
