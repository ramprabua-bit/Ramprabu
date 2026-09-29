package com.example.ui.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.data.model.*
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ScheduleJobDialog(
    technicians: List<Technician>,
    onDismiss: () -> Unit,
    onSchedule: (
        name: String,
        phone: String,
        address: String,
        brand: String,
        type: String,
        service: String,
        date: String,
        timeSlot: String,
        techId: Long?,
        techName: String?,
        isEmergency: Boolean,
        issue: String
    ) -> Unit
) {
    var name by remember { mutableStateOf("") }
    var phone by remember { mutableStateOf("") }
    var address by remember { mutableStateOf("") }
    var brand by remember { mutableStateOf("Daikin") }
    var acType by remember { mutableStateOf("1.5 Ton Split AC") }
    var serviceCategory by remember { mutableStateOf("Deep Jet Wash & Filter Cleaning") }
    var date by remember { mutableStateOf("2026-09-30") }
    var timeSlot by remember { mutableStateOf("10:00 AM - 12:00 PM") }
    var selectedTechId by remember { mutableStateOf<Long?>(technicians.firstOrNull()?.id) }
    var isEmergency by remember { mutableStateOf(false) }
    var issue by remember { mutableStateOf("") }

    val serviceOptions = listOf(
        "Deep Jet Wash & Filter Cleaning",
        "Gas Refill & Leak Detection (R32/R410A)",
        "PCB Motherboard Repair",
        "New AC Installation / Mounting",
        "Old AC Uninstallation & Shifting",
        "Annual Maintenance Contract (AMC)",
        "Compressor Diagnostics & Replacement"
    )

    val timeSlots = listOf(
        "09:00 AM - 11:00 AM",
        "11:30 AM - 01:30 PM",
        "02:30 PM - 04:30 PM",
        "05:00 PM - 07:00 PM"
    )

    Dialog(onDismissRequest = onDismiss) {
        Card(
            shape = RoundedCornerShape(16.dp),
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight(0.9f)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(20.dp)
                    .verticalScroll(rememberScrollState())
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Schedule AC Service",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    IconButton(onClick = onDismiss) {
                        Icon(Icons.Filled.Close, contentDescription = "Close")
                    }
                }

                HorizontalDivider()
                Spacer(modifier = Modifier.height(12.dp))

                // Emergency toggle
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Switch(
                        checked = isEmergency,
                        onCheckedChange = { isEmergency = it }
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Emergency Breakdown Request",
                        fontWeight = FontWeight.Bold,
                        color = if (isEmergency) Color(0xFFD32F2F) else MaterialTheme.colorScheme.onSurface
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Customer Name *") },
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(8.dp))

                OutlinedTextField(
                    value = phone,
                    onValueChange = { phone = it },
                    label = { Text("Customer Mobile Number *") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(8.dp))

                OutlinedTextField(
                    value = address,
                    onValueChange = { address = it },
                    label = { Text("Service Location / Address *") },
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(8.dp))

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = brand,
                        onValueChange = { brand = it },
                        label = { Text("AC Brand") },
                        modifier = Modifier.weight(1f)
                    )
                    OutlinedTextField(
                        value = acType,
                        onValueChange = { acType = it },
                        label = { Text("AC Type") },
                        modifier = Modifier.weight(1f)
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                // Service category selector
                Text("Select Service Category:", fontWeight = FontWeight.SemiBold, fontSize = 12.sp)
                var expandedService by remember { mutableStateOf(false) }
                ExposedDropdownMenuBox(
                    expanded = expandedService,
                    onExpandedChange = { expandedService = !expandedService }
                ) {
                    OutlinedTextField(
                        value = serviceCategory,
                        onValueChange = {},
                        readOnly = true,
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expandedService) },
                        modifier = Modifier
                            .menuAnchor()
                            .fillMaxWidth()
                    )
                    ExposedDropdownMenu(
                        expanded = expandedService,
                        onDismissRequest = { expandedService = false }
                    ) {
                        serviceOptions.forEach { opt ->
                            DropdownMenuItem(
                                text = { Text(opt) },
                                onClick = {
                                    serviceCategory = opt
                                    expandedService = false
                                }
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = date,
                        onValueChange = { date = it },
                        label = { Text("Date (YYYY-MM-DD)") },
                        modifier = Modifier.weight(1f)
                    )
                    OutlinedTextField(
                        value = timeSlot,
                        onValueChange = { timeSlot = it },
                        label = { Text("Time Slot") },
                        modifier = Modifier.weight(1f)
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                // Technician assignment
                Text("Assign Technician:", fontWeight = FontWeight.SemiBold, fontSize = 12.sp)
                var expandedTech by remember { mutableStateOf(false) }
                val selectedTech = technicians.firstOrNull { it.id == selectedTechId }
                ExposedDropdownMenuBox(
                    expanded = expandedTech,
                    onExpandedChange = { expandedTech = !expandedTech }
                ) {
                    OutlinedTextField(
                        value = selectedTech?.let { "${it.name} (${it.status.label})" } ?: "Unassigned",
                        onValueChange = {},
                        readOnly = true,
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expandedTech) },
                        modifier = Modifier
                            .menuAnchor()
                            .fillMaxWidth()
                    )
                    ExposedDropdownMenu(
                        expanded = expandedTech,
                        onDismissRequest = { expandedTech = false }
                    ) {
                        technicians.forEach { tech ->
                            DropdownMenuItem(
                                text = { Text("${tech.name} - ${tech.status.label} (★${tech.rating})") },
                                onClick = {
                                    selectedTechId = tech.id
                                    expandedTech = false
                                }
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                OutlinedTextField(
                    value = issue,
                    onValueChange = { issue = it },
                    label = { Text("Issue / Service Notes") },
                    minLines = 2,
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(16.dp))

                Button(
                    onClick = {
                        if (name.isNotBlank() && phone.isNotBlank() && address.isNotBlank()) {
                            val tech = technicians.firstOrNull { it.id == selectedTechId }
                            onSchedule(
                                name, phone, address, brand, acType, serviceCategory,
                                date, timeSlot, tech?.id, tech?.name, isEmergency, issue
                            )
                            onDismiss()
                        }
                    },
                    modifier = Modifier.fillMaxWidth(),
                    enabled = name.isNotBlank() && phone.isNotBlank()
                ) {
                    Icon(Icons.Filled.EventAvailable, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Confirm Appointment & Dispatch")
                }
            }
        }
    }
}

@Composable
fun CreateInvoiceDialog(
    jobs: List<ServiceJob>,
    onDismiss: () -> Unit,
    onCreate: (jobId: Long, name: String, phone: String, address: String, items: List<InvoiceLineItem>, tax: Double, discount: Double) -> Unit
) {
    var selectedJobId by remember { mutableStateOf(jobs.firstOrNull()?.id ?: 0L) }
    val selectedJob = jobs.firstOrNull { it.id == selectedJobId }

    var custName by remember { mutableStateOf(selectedJob?.customerName ?: "") }
    var custPhone by remember { mutableStateOf(selectedJob?.customerPhone ?: "") }
    var custAddress by remember { mutableStateOf(selectedJob?.customerAddress ?: "") }

    val lineItems = remember {
        mutableStateListOf(
            InvoiceLineItem("AC Deep Jet Foam Wash Service", 1, 799.0),
            InvoiceLineItem("Gas Pressure Check & Top-Up", 1, 650.0)
        )
    }

    var newItemDesc by remember { mutableStateOf("") }
    var newItemQty by remember { mutableStateOf("1") }
    var newItemPrice by remember { mutableStateOf("") }

    var taxRate by remember { mutableStateOf("18.0") }
    var discount by remember { mutableStateOf("0.0") }

    Dialog(onDismissRequest = onDismiss) {
        Card(
            shape = RoundedCornerShape(16.dp),
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight(0.92f)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(20.dp)
                    .verticalScroll(rememberScrollState())
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Create Digital Invoice",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    IconButton(onClick = onDismiss) {
                        Icon(Icons.Filled.Close, contentDescription = "Close")
                    }
                }

                HorizontalDivider()
                Spacer(modifier = Modifier.height(10.dp))

                OutlinedTextField(
                    value = custName,
                    onValueChange = { custName = it },
                    label = { Text("Customer Name") },
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(6.dp))
                OutlinedTextField(
                    value = custPhone,
                    onValueChange = { custPhone = it },
                    label = { Text("Mobile Number") },
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(6.dp))
                OutlinedTextField(
                    value = custAddress,
                    onValueChange = { custAddress = it },
                    label = { Text("Customer Address") },
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(14.dp))
                Text("Line Items & Spare Parts:", fontWeight = FontWeight.Bold, fontSize = 13.sp)

                // List of items
                lineItems.forEachIndexed { index, item ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(item.description, fontWeight = FontWeight.SemiBold, fontSize = 12.sp)
                            Text("Qty: ${item.quantity} × ₹${item.unitPrice} = ₹${item.total}", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                        IconButton(
                            onClick = { lineItems.removeAt(index) },
                            modifier = Modifier.size(28.dp)
                        ) {
                            Icon(Icons.Filled.Delete, contentDescription = "Remove", tint = Color.Red, modifier = Modifier.size(16.dp))
                        }
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                // Add item inputs
                Text("Add New Item / Service:", fontWeight = FontWeight.SemiBold, fontSize = 12.sp)
                OutlinedTextField(
                    value = newItemDesc,
                    onValueChange = { newItemDesc = it },
                    label = { Text("Description (e.g. Copper Flare Nut)") },
                    modifier = Modifier.fillMaxWidth()
                )
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = newItemQty,
                        onValueChange = { newItemQty = it },
                        label = { Text("Qty") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.weight(1f)
                    )
                    OutlinedTextField(
                        value = newItemPrice,
                        onValueChange = { newItemPrice = it },
                        label = { Text("Unit Price (₹)") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        modifier = Modifier.weight(1.5f)
                    )
                    Button(
                        onClick = {
                            val price = newItemPrice.toDoubleOrNull() ?: 0.0
                            val qty = newItemQty.toIntOrNull() ?: 1
                            if (newItemDesc.isNotBlank() && price > 0) {
                                lineItems.add(InvoiceLineItem(newItemDesc, qty, price))
                                newItemDesc = ""
                                newItemQty = "1"
                                newItemPrice = ""
                            }
                        },
                        modifier = Modifier.align(Alignment.CenterVertically)
                    ) {
                        Text("Add")
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = taxRate,
                        onValueChange = { taxRate = it },
                        label = { Text("GST %") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        modifier = Modifier.weight(1f)
                    )
                    OutlinedTextField(
                        value = discount,
                        onValueChange = { discount = it },
                        label = { Text("Discount (₹)") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        modifier = Modifier.weight(1f)
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                val subtotal = lineItems.sumOf { it.total }
                val taxVal = subtotal * ((taxRate.toDoubleOrNull() ?: 18.0) / 100.0)
                val discVal = discount.toDoubleOrNull() ?: 0.0
                val grandTotal = (subtotal + taxVal - discVal).coerceAtLeast(0.0)

                Card(
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("Total Amount Due:", fontWeight = FontWeight.Bold)
                        Text(
                            "₹${String.format(Locale.US, "%.2f", grandTotal)}",
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary,
                            fontSize = 16.sp
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                Button(
                    onClick = {
                        if (custName.isNotBlank() && lineItems.isNotEmpty()) {
                            onCreate(
                                selectedJobId,
                                custName,
                                custPhone,
                                custAddress,
                                lineItems.toList(),
                                taxRate.toDoubleOrNull() ?: 18.0,
                                discVal
                            )
                            onDismiss()
                        }
                    },
                    modifier = Modifier.fillMaxWidth(),
                    enabled = custName.isNotBlank() && lineItems.isNotEmpty()
                ) {
                    Icon(Icons.Filled.Receipt, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Generate Digital Invoice & BHIM QR")
                }
            }
        }
    }
}

@Composable
fun SubmitReviewDialog(
    technicians: List<Technician>,
    onDismiss: () -> Unit,
    onSubmit: (techName: String, overall: Float, cooling: Float, punctuality: Float, cleanliness: Float, comment: String) -> Unit
) {
    var selectedTechName by remember { mutableStateOf(technicians.firstOrNull()?.name ?: "Karthik Raja") }
    var overallRating by remember { mutableStateOf(5f) }
    var coolingRating by remember { mutableStateOf(5f) }
    var punctualityRating by remember { mutableStateOf(5f) }
    var cleanlinessRating by remember { mutableStateOf(5f) }
    var comment by remember { mutableStateOf("") }

    Dialog(onDismissRequest = onDismiss) {
        Card(
            shape = RoundedCornerShape(16.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp)
                    .verticalScroll(rememberScrollState())
            ) {
                Text(
                    text = "Customer Feedback Survey",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "Help us maintain 5-star AC repair standards at Sri Vel Enterprises",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Spacer(modifier = Modifier.height(14.dp))

                Text("Technician: $selectedTechName", fontWeight = FontWeight.SemiBold, fontSize = 13.sp)

                Spacer(modifier = Modifier.height(10.dp))

                RatingSlider(label = "Overall Service Experience", value = overallRating, onValueChange = { overallRating = it })
                RatingSlider(label = "AC Cooling Performance", value = coolingRating, onValueChange = { coolingRating = it })
                RatingSlider(label = "Punctuality & Timing", value = punctualityRating, onValueChange = { punctualityRating = it })
                RatingSlider(label = "Cleanliness (No Water Spills)", value = cleanlinessRating, onValueChange = { cleanlinessRating = it })

                Spacer(modifier = Modifier.height(10.dp))

                OutlinedTextField(
                    value = comment,
                    onValueChange = { comment = it },
                    label = { Text("Your Review / Comments") },
                    minLines = 3,
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(16.dp))

                Button(
                    onClick = {
                        onSubmit(selectedTechName, overallRating, coolingRating, punctualityRating, cleanlinessRating, comment)
                        onDismiss()
                    },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(Icons.Filled.Send, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Submit Verified Review")
                }
            }
        }
    }
}

@Composable
private fun RatingSlider(label: String, value: Float, onValueChange: (Float) -> Unit) {
    Column(modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(label, fontSize = 12.sp, fontWeight = FontWeight.Medium)
            Text("★ ${String.format(Locale.US, "%.1f", value)}", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color(0xFFF57C00))
        }
        Slider(
            value = value,
            onValueChange = onValueChange,
            valueRange = 1f..5f,
            steps = 7
        )
    }
}

@Composable
fun LanguageSelectionDialog(
    currentLang: AppLanguage,
    onDismiss: () -> Unit,
    onSelectLanguage: (AppLanguage) -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Select Language / மொழியை மாற்றுக") },
        text = {
            Column {
                AppLanguage.values().forEach { lang ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                onSelectLanguage(lang)
                                onDismiss()
                            }
                            .padding(vertical = 12.dp, horizontal = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        RadioButton(
                            selected = (lang == currentLang),
                            onClick = {
                                onSelectLanguage(lang)
                                onDismiss()
                            }
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text(lang.displayName, fontWeight = FontWeight.Bold)
                            Text(lang.nativeName, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.primary)
                        }
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("Close")
            }
        }
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NotificationsSheet(
    notifications: List<AppNotification>,
    onDismiss: () -> Unit,
    onClearAll: () -> Unit
) {
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Notification Center",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                TextButton(onClick = onClearAll) {
                    Text("Clear All")
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            if (notifications.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(32.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text("No new notifications.", color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            } else {
                LazyColumn(modifier = Modifier.fillMaxWidth()) {
                    items(notifications) { notif ->
                        Card(
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)),
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp)
                        ) {
                            Column(modifier = Modifier.padding(12.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text(
                                        notif.title,
                                        fontWeight = FontWeight.Bold,
                                        style = MaterialTheme.typography.bodyMedium
                                    )
                                    Text(
                                        notif.category,
                                        fontSize = 10.sp,
                                        color = MaterialTheme.colorScheme.primary,
                                        fontWeight = FontWeight.SemiBold
                                    )
                                }
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    notif.message,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun StripePaymentDialog(
    invoice: Invoice,
    onDismiss: () -> Unit,
    onSuccess: () -> Unit
) {
    var cardNumber by remember { mutableStateOf("4242 •••• •••• 4242") }
    var expiry by remember { mutableStateOf("12/28") }
    var cvc by remember { mutableStateOf("321") }
    var isProcessing by remember { mutableStateOf(false) }

    Dialog(onDismissRequest = onDismiss) {
        Card(
            shape = RoundedCornerShape(16.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Filled.CreditCard, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Stripe Secure Payment", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleSmall)
                    }
                    IconButton(onClick = onDismiss) {
                        Icon(Icons.Filled.Close, contentDescription = "Close")
                    }
                }

                Text(
                    "Encrypted 256-bit SSL transaction for Sri Vel Enterprises",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Spacer(modifier = Modifier.height(14.dp))

                OutlinedTextField(
                    value = cardNumber,
                    onValueChange = { cardNumber = it },
                    label = { Text("Card Number") },
                    leadingIcon = { Icon(Icons.Filled.Lock, contentDescription = null) },
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(8.dp))

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = expiry,
                        onValueChange = { expiry = it },
                        label = { Text("MM/YY") },
                        modifier = Modifier.weight(1f)
                    )
                    OutlinedTextField(
                        value = cvc,
                        onValueChange = { cvc = it },
                        label = { Text("CVC") },
                        modifier = Modifier.weight(1f)
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                Button(
                    onClick = {
                        isProcessing = true
                        onSuccess()
                    },
                    modifier = Modifier.fillMaxWidth(),
                    enabled = !isProcessing
                ) {
                    if (isProcessing) {
                        CircularProgressIndicator(modifier = Modifier.size(18.dp), color = Color.White)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Processing...")
                    } else {
                        Text("Pay ₹${String.format(Locale.US, "%.2f", invoice.totalAmount)} with Stripe")
                    }
                }
            }
        }
    }
}
