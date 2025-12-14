package com.example.chronicare.homeScreens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel

// -------------------------------
// DATA CLASS FOR METRICS
// -------------------------------
data class ProgressMetric(
    val title: String,
    val goal: Int,
    val current: Int
)

// -------------------------------
// ACCENT COLOR
// -------------------------------
val accentColorProgress = Color(0xFF007F7A)

// -------------------------------
// MAIN PROGRESS TRACKING SCREEN
// -------------------------------
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProgressTrackingScreen(logViewModel: LogViewModel = viewModel()) {
    val dailyLogs by logViewModel.dailyLogs
    val isLoading by logViewModel.isLoading

    val metrics = if (dailyLogs.isNotEmpty()) {
        val latest = dailyLogs.first()
        listOf(
            ProgressMetric("Steps Walked", goal = 10000, current = latest.steps.toInt()),
            ProgressMetric("Sleep Hours", goal = 8, current = latest.sleepHours.toInt()),
            ProgressMetric("Water Intake (ml)", goal = 2000, current = latest.waterMl.toInt())
        )
    } else emptyList()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        // Screen Title
        Text(
            text = "Progress Tracking",
            style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold),
            color = accentColorProgress,
            modifier = Modifier.padding(bottom = 16.dp)
        )

        if (isLoading) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
        } else if (metrics.isEmpty()) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text(
                    text = "No data available.",
                    color = Color.Gray
                )
            }
        } else {
            // Display each progress metric
            metrics.forEach { metric ->
                ProgressMetricCard(progressMetric = metric)
                Spacer(modifier = Modifier.height(12.dp))
            }
        }
    }
}

// -------------------------------
// CARD COMPONENT FOR EACH METRIC
// -------------------------------
@Composable
fun ProgressMetricCard(progressMetric: ProgressMetric) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 6.dp),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            // Title and Current / Goal
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = progressMetric.title,
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold)
                )
                Text(
                    text = "${progressMetric.current} / ${progressMetric.goal}",
                    style = MaterialTheme.typography.bodyLarge,
                    color = Color(0xFF00796B)
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Progress bar
            val progress = (progressMetric.current.toFloat() / progressMetric.goal.toFloat()).coerceIn(0f, 1f)
            LinearProgressIndicator(
                progress = progress,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(8.dp),
                color = Color(0xFF10B981),
                trackColor = Color.Gray.copy(alpha = 0.2f)
            )

            Spacer(modifier = Modifier.height(8.dp))

            // Status text
            Text(
                text = "Goal Progress: ${(progress * 100).toInt()}%",
                style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold),
                color = if (progress >= 1f) Color(0xFF10B981) else Color.Gray
            )

            Spacer(modifier = Modifier.height(12.dp))
        }
    }
}


