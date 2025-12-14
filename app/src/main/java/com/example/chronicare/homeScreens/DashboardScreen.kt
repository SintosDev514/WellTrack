package com.example.chronicare.homeScreens

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Star

// -------------------- DATA MODEL --------------------
data class HealthSummary(
    val steps: Int = 0,
    val stepsGoal: Int = 10000,
    val medicationTaken: Int = 0,
    val medicationTotal: Int = 0,
    val medicationName: String = "N/A",
    val medicationStatus: String = "Missed",
    val waterMl: Int = 0,
    val waterGoal: Int = 2000,
    val sleepHours: Float = 0f,
    val sleepGoal: Float = 8f
)

// -------------------- CONSTANTS --------------------
private object DashboardConstants {
    const val TAGLINE = "Track your fitness progress effortlessly"
    val PRIMARY_COLOR = Color(0xFF006A6A)
    val BACKGROUND_GRADIENT = Brush.verticalGradient(
        colors = listOf(Color(0xFF00BFA5), Color(0xFFE0F2F1))
    )
    val SURFACE_COLOR = Color.White
    val TEXT_PRIMARY = Color(0xFF1E293B)
    val TEXT_SECONDARY = Color(0xFF64748B)
    val PROGRESS_EXCELLENT = Color(0xFF10B981)
    val PROGRESS_GOOD = Color(0xFFFBBF24)
    val PROGRESS_LOW = Color(0xFFEF4444)
}

// -------------------- DASHBOARD SCREEN --------------------
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DashboardScreen(
    navController: NavController,
    logViewModel: LogViewModel = viewModel()
) {
    val dailyLogs by logViewModel.dailyLogs
    val isLoading by logViewModel.isLoading

    // Build HealthSummary dynamically from latest log
    val latestLog = dailyLogs.firstOrNull()
    val healthSummary = if (latestLog != null) {
        HealthSummary(
            steps = latestLog.steps.toInt(),
            stepsGoal = 10000,
            waterMl = latestLog.waterMl.toInt(),
            waterGoal = 2000,
            sleepHours = latestLog.sleepHours,
            sleepGoal = 8f,
            medicationTaken = latestLog.medications.count { it.status == "Taken" },
            medicationTotal = latestLog.medications.size,
            medicationName = latestLog.medications.firstOrNull()?.medicationName ?: "N/A",
            medicationStatus = if (latestLog.medications.all { it.status == "Taken" }) "Taken"
            else if (latestLog.medications.any { it.status == "Taken" }) "Partially Taken"
            else "Missed"
        )
    } else {
        HealthSummary()
    }

    Scaffold(containerColor = Color.Transparent) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(DashboardConstants.BACKGROUND_GRADIENT)
                .padding(padding)
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            if (isLoading) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = DashboardConstants.PRIMARY_COLOR)
                }
            } else {
                HealthScoreSection(healthSummary)
                Spacer(modifier = Modifier.height(32.dp))
                HealthOverviewSection(healthSummary)
            }
        }
    }
}

// -------------------- HEALTH SCORE SECTION --------------------
@Composable
fun HealthScoreSection(healthSummary: HealthSummary, centerIcon: ImageVector = Icons.Default.Star) {
    val score = calculateHealthScore(healthSummary)

    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 17.dp)
            .padding(horizontal = 24.dp)
            .shadow(12.dp, RoundedCornerShape(24.dp)),
        shape = RoundedCornerShape(24.dp),
        color = DashboardConstants.SURFACE_COLOR
    ) {
        Column(
            modifier = Modifier.padding(32.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = DashboardConstants.TAGLINE,
                fontSize = 16.sp,
                fontWeight = FontWeight.Medium,
                color = DashboardConstants.TEXT_SECONDARY,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(24.dp))

            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .size(140.dp)
                    .shadow(8.dp, RoundedCornerShape(70.dp))
                    .clip(RoundedCornerShape(70.dp))
                    .background(DashboardConstants.PRIMARY_COLOR.copy(alpha = 0.1f))
            ) {
                CircularScoreIndicator(score)

                Icon(
                    imageVector = centerIcon,
                    contentDescription = "Score Icon",
                    tint = DashboardConstants.PRIMARY_COLOR,
                    modifier = Modifier.size(48.dp)
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = "Health Score",
                fontSize = 18.sp,
                fontWeight = FontWeight.Medium,
                color = DashboardConstants.TEXT_PRIMARY
            )
            Text(
                text = "$score / 100 pts",
                fontSize = 26.sp,
                fontWeight = FontWeight.Bold,
                color = DashboardConstants.PRIMARY_COLOR
            )
        }
    }
}

// -------------------- CIRCULAR SCORE --------------------
@Composable
fun CircularScoreIndicator(score: Int) {
    val progress = (score.coerceIn(0, 100) / 100f)
    Canvas(modifier = Modifier.size(100.dp)) {
        val strokeWidth = 16f
        drawArc(
            color = Color.Gray.copy(alpha = 0.2f),
            startAngle = -90f,
            sweepAngle = 360f,
            useCenter = false,
            style = androidx.compose.ui.graphics.drawscope.Stroke(strokeWidth, cap = StrokeCap.Round)
        )
        drawArc(
            color = DashboardConstants.PRIMARY_COLOR,
            startAngle = -90f,
            sweepAngle = 360f * progress,
            useCenter = false,
            style = androidx.compose.ui.graphics.drawscope.Stroke(strokeWidth, cap = StrokeCap.Round)
        )
    }
}

// -------------------- HEALTH SCORE CALC --------------------
fun calculateHealthScore(summary: HealthSummary): Int {
    val stepsScore = (summary.steps.toFloat() / summary.stepsGoal).coerceIn(0f, 1f)
    val waterScore = (summary.waterMl.toFloat() / summary.waterGoal).coerceIn(0f, 1f)
    val sleepScore = (summary.sleepHours / summary.sleepGoal).coerceIn(0f, 1f)
    val medicationScore = if (summary.medicationTotal > 0)
        (summary.medicationTaken.toFloat() / summary.medicationTotal).coerceIn(0f, 1f)
    else 1f

    return ((stepsScore * 0.3f + waterScore * 0.25f + sleepScore * 0.25f + medicationScore * 0.2f) * 100).toInt()
}

// -------------------- HEALTH OVERVIEW --------------------
@Composable
fun HealthOverviewSection(summary: HealthSummary) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp),
        shape = RoundedCornerShape(24.dp),
        color = DashboardConstants.SURFACE_COLOR,
        shadowElevation = 8.dp
    ) {
        Column(Modifier.padding(20.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("Health Overview", fontSize = 20.sp, fontWeight = FontWeight.SemiBold)
            }
            Spacer(Modifier.height(16.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                MetricCard("Steps", summary.steps, summary.stepsGoal, "steps", Modifier.weight(1f))
                MetricCard("Water", summary.waterMl, summary.waterGoal, "ml", Modifier.weight(1f))
            }
            Spacer(Modifier.height(12.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                MetricCard("Sleep", summary.sleepHours.toInt(), summary.sleepGoal.toInt(), "h", Modifier.weight(1f))
                MedicationCard(summary, Modifier.weight(1f))
            }
        }
    }
}

// -------------------- METRIC CARD --------------------
@Composable
fun MetricCard(title: String, value: Int, goal: Int, unit: String, modifier: Modifier) {
    val progress = (value.toFloat() / goal).coerceIn(0f, 1f)
    Surface(
        modifier = modifier.height(160.dp),
        shape = RoundedCornerShape(20.dp),
        color = DashboardConstants.SURFACE_COLOR,
        shadowElevation = 6.dp
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Center) {
            Text(title, fontSize = 14.sp, color = DashboardConstants.TEXT_SECONDARY)
            Spacer(Modifier.height(8.dp))
            Box(contentAlignment = Alignment.Center) {
                Canvas(modifier = Modifier.size(80.dp)) {
                    drawArc(
                        color = Color.Gray.copy(alpha = 0.2f),
                        startAngle = -90f,
                        sweepAngle = 360f,
                        useCenter = false,
                        style = androidx.compose.ui.graphics.drawscope.Stroke(14f, cap = StrokeCap.Round)
                    )
                    drawArc(
                        color = DashboardConstants.PRIMARY_COLOR,
                        startAngle = -90f,
                        sweepAngle = 360f * progress,
                        useCenter = false,
                        style = androidx.compose.ui.graphics.drawscope.Stroke(14f, cap = StrokeCap.Round)
                    )
                }
                Text("$value$unit", fontWeight = FontWeight.Bold, fontSize = 12.sp, color = DashboardConstants.TEXT_PRIMARY, textAlign = TextAlign.Center)
            }
            Spacer(Modifier.height(8.dp))
            Text("${(progress * 100).toInt()}%", fontSize = 12.sp, color = DashboardConstants.TEXT_SECONDARY)
        }
    }
}

// -------------------- MEDICATION CARD --------------------
@Composable
fun MedicationCard(summary: HealthSummary, modifier: Modifier) {
    val progress = if (summary.medicationTotal > 0) summary.medicationTaken.toFloat() / summary.medicationTotal else 1f
    Surface(
        modifier = modifier.height(160.dp),
        shape = RoundedCornerShape(20.dp),
        color = DashboardConstants.SURFACE_COLOR,
        shadowElevation = 6.dp
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Text("Medication", fontSize = 14.sp, color = DashboardConstants.TEXT_SECONDARY)
            Text("${summary.medicationTaken}/${summary.medicationTotal}", fontSize = 22.sp, fontWeight = FontWeight.Bold, color = DashboardConstants.TEXT_PRIMARY)
            Text("${summary.medicationName} - ${summary.medicationStatus}", fontSize = 12.sp, color = DashboardConstants.TEXT_SECONDARY)
            LinearProgressIndicator(
                progress = progress,
                color = when {
                    progress >= 1f -> DashboardConstants.PROGRESS_EXCELLENT
                    progress >= 0.5f -> DashboardConstants.PROGRESS_GOOD
                    else -> DashboardConstants.PROGRESS_LOW
                },
                trackColor = Color.Gray.copy(alpha = 0.2f),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(6.dp)
                    .clip(RoundedCornerShape(3.dp))
            )
        }
    }
}
