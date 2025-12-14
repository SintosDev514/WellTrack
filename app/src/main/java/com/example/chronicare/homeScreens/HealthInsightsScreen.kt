package com.example.chronicare.homeScreens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DirectionsWalk
import androidx.compose.material.icons.filled.LocalDrink
import androidx.compose.material.icons.filled.NightsStay
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.chronicare.model.HealthInsight

val accentColorHealth = Color(0xFF007F7A)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HealthInsightsScreen(
    logViewModel: LogViewModel = viewModel()
) {
    val insights = logViewModel.healthInsights.value
    val isLoading = logViewModel.isLoading.value

    if (isLoading) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            CircularProgressIndicator(color = accentColorHealth)
        }
        return
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {

        item {
            Column {
                Text(
                    text = "Health Insights",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                    color = accentColorHealth
                )
                Text(
                    text = "Your latest health summary",
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color.Gray
                )
            }
        }

        items(insights) { insight ->
            HealthInsightCard(
                healthInsight = insight,
                icon = insightIcon(insight.title)
            )
        }
    }
}

@Composable
fun HealthInsightCard(
    healthInsight: HealthInsight,
    icon: ImageVector
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        )
    ) {
        Row(
            modifier = Modifier
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {

            // Icon container
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .background(
                        color = accentColorHealth.copy(alpha = 0.12f),
                        shape = RoundedCornerShape(12.dp)
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = accentColorHealth
                )
            }

            Spacer(modifier = Modifier.width(16.dp))

            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = healthInsight.title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold
                )

                Text(
                    text = healthInsight.description,
                    style = MaterialTheme.typography.bodySmall,
                    color = Color.Gray
                )
            }

            Spacer(modifier = Modifier.width(8.dp))

            Text(
                text = healthInsight.value,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = accentColorHealth
            )
        }
    }
}

/**
 * Simple icon mapping based on insight title
 */
fun insightIcon(title: String): ImageVector {
    return when {
        title.contains("step", ignoreCase = true) -> Icons.Default.DirectionsWalk
        title.contains("sleep", ignoreCase = true) -> Icons.Default.NightsStay
        title.contains("water", ignoreCase = true) -> Icons.Default.LocalDrink
        else -> Icons.Default.DirectionsWalk
    }
}

@Preview(showBackground = true)
@Composable
fun HealthInsightsScreenPreview() {
    HealthInsightsScreen()
}
