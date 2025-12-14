package com.example.chronicare.homeScreens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DirectionsWalk
import androidx.compose.material.icons.filled.LocalDrink
import androidx.compose.material.icons.filled.NightsStay
import androidx.compose.material3.*
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel

// -------------------- CONSTANTS --------------------
object DashboardConstantsOne {
    val PRIMARY_COLOR = Color(0xFF006A6A)
    val TEXT_SECONDARY = Color(0xFF6B7280)
    val SUCCESS_COLOR = Color(0xFF10B981)
    val WARNING_COLOR = Color(0xFFF59E0B)
    val BACKGROUND_COLOR = Color(0xFFF8F9FA)
    val SECTION_SPACING = 24.dp
}

private val accentColor = Color(0xFF007F7A)

enum class TabType { Daily, Weekly }

// -------------------- DAILY/WEEKLY LOG SCREEN --------------------
@Composable
fun DailyWeeklyLogScreen(
    logViewModel: LogViewModel = viewModel()
) {
    var selectedTab by remember { mutableStateOf(TabType.Daily) }
    val dailyLogs = logViewModel.dailyLogs.value
    val isLoading = logViewModel.isLoading.value

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(DashboardConstantsOne.BACKGROUND_COLOR)
            .padding(16.dp)
    ) {
        Text(
            text = "Log Overview",
            style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold),
            color = accentColor
        )

        Spacer(modifier = Modifier.height(16.dp))

        TabRow(
            selectedTabIndex = selectedTab.ordinal,
            containerColor = DashboardConstantsOne.PRIMARY_COLOR.copy(alpha = 0.1f),
            indicator = { tabPositions ->
                TabRowDefaults.Indicator(
                    Modifier.tabIndicatorOffset(tabPositions[selectedTab.ordinal]),
                    color = accentColor
                )
            }
        ) {
            Tab(
                selected = selectedTab == TabType.Daily,
                onClick = { selectedTab = TabType.Daily },
                text = { Text("Daily Logs") }
            )
            Tab(
                selected = selectedTab == TabType.Weekly,
                onClick = { selectedTab = TabType.Weekly },
                text = { Text("Weekly Summary") }
            )
        }

        Spacer(modifier = Modifier.height(DashboardConstantsOne.SECTION_SPACING))

        when {
            selectedTab == TabType.Daily && isLoading -> {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(color = accentColor)
                }
            }

            selectedTab == TabType.Daily -> {
                DailyLogList(logs = dailyLogs)
            }

            else -> WeeklyPlaceholder()
        }
    }
}

// -------------------- DAILY LOG LIST --------------------
@Composable
fun DailyLogList(logs: List<LogEntry>) {
    if (logs.isEmpty()) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "No daily health logs found.",
                color = DashboardConstantsOne.TEXT_SECONDARY
            )
        }
    } else {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            items(logs) { log ->
                LogItem(log = log)
            }
        }
    }
}

// -------------------- LOG ITEM --------------------
@Composable
fun LogItem(log: LogEntry) {
    val completed = log.steps >= 5000 && log.sleepHours >= 8 && log.waterMl >= 2000
    val statusColor = if (completed) DashboardConstantsOne.SUCCESS_COLOR else DashboardConstantsOne.WARNING_COLOR

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {

            // DATE & STATUS
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = log.date,
                    style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                    color = DashboardConstantsOne.PRIMARY_COLOR
                )

                Surface(
                    color = statusColor.copy(alpha = 0.12f),
                    shape = RoundedCornerShape(50)
                ) {
                    Text(
                        text = if (completed) "Completed" else "In Progress",
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp),
                        color = statusColor,
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // METRICS
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                MetricItem(
                    title = "Steps",
                    value = log.steps.toString(),
                    unit = "steps",
                    icon = Icons.Default.DirectionsWalk
                )
                MetricItem(
                    title = "Sleep",
                    value = log.sleepHours.toString(),
                    unit = "hrs",
                    icon = Icons.Default.NightsStay
                )
                MetricItem(
                    title = "Water",
                    value = log.waterMl.toInt().toString(),
                    unit = "ml",
                    icon = Icons.Default.LocalDrink
                )
            }

            // MEDICATIONS
            if (log.medications.isNotEmpty()) {
                Spacer(modifier = Modifier.height(12.dp))
                Column(modifier = Modifier.fillMaxWidth()) {
                    Text(
                        text = "Medications",
                        style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                        color = DashboardConstantsOne.PRIMARY_COLOR
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    log.medications.forEach { med ->
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "${med.medicationName} • ${med.dosage}",
                                style = MaterialTheme.typography.bodySmall
                            )
                            Text(
                                text = med.status,
                                style = MaterialTheme.typography.bodySmall.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = if (med.status == "Missed") DashboardConstantsOne.WARNING_COLOR
                                    else DashboardConstantsOne.SUCCESS_COLOR
                                )
                            )
                        }
                    }
                }
            }
        }
    }
}

// METRIC ITEM
@Composable
fun MetricItem(title: String, value: String, unit: String, icon: ImageVector) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Box(
            modifier = Modifier
                .size(36.dp)
                .background(accentColor.copy(alpha = 0.1f), shape = CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Icon(imageVector = icon, contentDescription = null, tint = accentColor)
        }
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = value,
            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
            color = Color.Black
        )
        Text(
            text = "$title • $unit",
            style = MaterialTheme.typography.labelSmall,
            color = DashboardConstantsOne.TEXT_SECONDARY
        )
    }
}

// WEEKLY PLACEHOLDER
@Composable
fun WeeklyPlaceholder() {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = "Weekly summary coming soon 🚧",
            color = DashboardConstantsOne.TEXT_SECONDARY
        )
    }
}

// PREVIEW
@Composable
fun DailyWeeklyLogScreenPreview() {
    WeeklyPlaceholder()
}
