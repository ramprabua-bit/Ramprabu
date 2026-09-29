package com.example.ui.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
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
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.data.model.AppLanguage
import com.example.data.model.UserRole
import com.example.ui.viewmodel.NavTab
import com.example.util.LocalizationHelper

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppTopBar(
    role: UserRole,
    currentLang: AppLanguage,
    isDarkMode: Boolean,
    unreadNotifCount: Int,
    onRoleToggle: () -> Unit,
    onLanguageClick: () -> Unit,
    onDarkModeToggle: () -> Unit,
    onNotifClick: () -> Unit
) {
    TopAppBar(
        colors = TopAppBarDefaults.topAppBarColors(
            containerColor = MaterialTheme.colorScheme.surface,
            titleContentColor = MaterialTheme.colorScheme.onSurface
        ),
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(38.dp)
                ) {
                    Image(
                        painter = painterResource(id = R.drawable.ic_app_logo_1790664723136),
                        contentDescription = "Sri Vel Logo",
                        modifier = Modifier.fillMaxSize()
                    )
                }

                Spacer(modifier = Modifier.width(10.dp))

                Column {
                    Text(
                        text = LocalizationHelper.getString(currentLang, "app_title"),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp,
                        maxLines = 1
                    )
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = LocalizationHelper.getString(currentLang, "business_subtitle"),
                            style = MaterialTheme.typography.bodySmall,
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.primary,
                            fontWeight = FontWeight.SemiBold
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Box(
                            modifier = Modifier
                                .background(Color(0xFF2E7D32).copy(alpha = 0.15f), RoundedCornerShape(4.dp))
                                .padding(horizontal = 4.dp, vertical = 1.dp)
                        ) {
                            Text(
                                text = "● Offline Sync",
                                fontSize = 9.sp,
                                color = Color(0xFF2E7D32),
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }
        },
        actions = {
            // Mode toggle chip (Admin vs Customer)
            AssistChip(
                onClick = onRoleToggle,
                label = {
                    Text(
                        text = if (role == UserRole.ADMIN_DISPATCHER) "Admin" else "Customer",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold
                    )
                },
                leadingIcon = {
                    Icon(
                        if (role == UserRole.ADMIN_DISPATCHER) Icons.Filled.Engineering else Icons.Filled.Person,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp)
                    )
                },
                modifier = Modifier.padding(end = 4.dp)
            )

            // Language Selector
            IconButton(onClick = onLanguageClick) {
                Box(
                    modifier = Modifier
                        .size(28.dp)
                        .background(MaterialTheme.colorScheme.surfaceVariant, CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = currentLang.code.uppercase(),
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }

            // Dark Mode toggle
            IconButton(onClick = onDarkModeToggle) {
                Icon(
                    imageVector = if (isDarkMode) Icons.Filled.LightMode else Icons.Filled.DarkMode,
                    contentDescription = "Toggle Dark Mode",
                    tint = MaterialTheme.colorScheme.onSurface
                )
            }

            // Notifications Bell with Badge
            IconButton(onClick = onNotifClick) {
                BadgedBox(
                    badge = {
                        if (unreadNotifCount > 0) {
                            Badge {
                                Text("$unreadNotifCount")
                            }
                        }
                    }
                ) {
                    Icon(
                        imageVector = Icons.Outlined.Notifications,
                        contentDescription = "Notifications",
                        tint = MaterialTheme.colorScheme.onSurface
                    )
                }
            }
        }
    )
}

@Composable
fun AppBottomBar(
    currentTab: NavTab,
    role: UserRole,
    currentLang: AppLanguage,
    onTabSelected: (NavTab) -> Unit
) {
    NavigationBar(
        containerColor = MaterialTheme.colorScheme.surface,
        tonalElevation = 6.dp
    ) {
        if (role == UserRole.ADMIN_DISPATCHER) {
            NavigationBarItem(
                selected = currentTab == NavTab.DASHBOARD,
                onClick = { onTabSelected(NavTab.DASHBOARD) },
                icon = { Icon(if (currentTab == NavTab.DASHBOARD) Icons.Filled.Dashboard else Icons.Outlined.Dashboard, contentDescription = null) },
                label = { Text("Dashboard", fontSize = 11.sp) }
            )
            NavigationBarItem(
                selected = currentTab == NavTab.DISPATCH_HUB,
                onClick = { onTabSelected(NavTab.DISPATCH_HUB) },
                icon = { Icon(if (currentTab == NavTab.DISPATCH_HUB) Icons.Filled.AssignmentInd else Icons.Outlined.AssignmentInd, contentDescription = null) },
                label = { Text("Dispatch", fontSize = 11.sp) }
            )
            NavigationBarItem(
                selected = currentTab == NavTab.SCHEDULE,
                onClick = { onTabSelected(NavTab.SCHEDULE) },
                icon = { Icon(if (currentTab == NavTab.SCHEDULE) Icons.Filled.CalendarMonth else Icons.Outlined.CalendarMonth, contentDescription = null) },
                label = { Text("Schedule", fontSize = 11.sp) }
            )
            NavigationBarItem(
                selected = currentTab == NavTab.TECHNICIANS,
                onClick = { onTabSelected(NavTab.TECHNICIANS) },
                icon = { Icon(if (currentTab == NavTab.TECHNICIANS) Icons.Filled.Engineering else Icons.Outlined.Engineering, contentDescription = null) },
                label = { Text("Techs", fontSize = 11.sp) }
            )
            NavigationBarItem(
                selected = currentTab == NavTab.INVOICES,
                onClick = { onTabSelected(NavTab.INVOICES) },
                icon = { Icon(if (currentTab == NavTab.INVOICES) Icons.Filled.ReceiptLong else Icons.Outlined.ReceiptLong, contentDescription = null) },
                label = { Text("Invoices", fontSize = 11.sp) }
            )
        } else {
            NavigationBarItem(
                selected = currentTab == NavTab.CUSTOMER_PORTAL,
                onClick = { onTabSelected(NavTab.CUSTOMER_PORTAL) },
                icon = { Icon(Icons.Filled.HomeRepairService, contentDescription = null) },
                label = { Text(LocalizationHelper.getString(currentLang, "tab_customer_portal"), fontSize = 11.sp) }
            )
            NavigationBarItem(
                selected = currentTab == NavTab.SCHEDULE,
                onClick = { onTabSelected(NavTab.SCHEDULE) },
                icon = { Icon(Icons.Filled.History, contentDescription = null) },
                label = { Text("My Service History", fontSize = 11.sp) }
            )
            NavigationBarItem(
                selected = currentTab == NavTab.INVOICES,
                onClick = { onTabSelected(NavTab.INVOICES) },
                icon = { Icon(Icons.Filled.Receipt, contentDescription = null) },
                label = { Text("My Invoices & UPI", fontSize = 11.sp) }
            )
            NavigationBarItem(
                selected = currentTab == NavTab.REVIEWS,
                onClick = { onTabSelected(NavTab.REVIEWS) },
                icon = { Icon(Icons.Filled.RateReview, contentDescription = null) },
                label = { Text("Rate Service", fontSize = 11.sp) }
            )
        }
    }
}
