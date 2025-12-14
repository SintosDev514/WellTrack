package com.example.chronicare.homeScreens

import android.Manifest // <-- ADDED
import android.content.Intent // <-- ADDED
import android.os.Build // <-- ADDED
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.LocalDrink
import androidx.compose.material.icons.outlined.LocalDrink
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.chronicare.screens.SharedData
import com.example.chronicare.ui.theme.ChroniCareTheme
import com.example.chronicare.services.StepCounterService
import androidx.compose.foundation.rememberScrollState // Already have this
import androidx.compose.foundation.verticalScroll // <-- ADD THIS LINE



@Composable
fun HealthTrackingApp(navController: NavController, sharedData: SharedData) {

    var sleepInput by remember { mutableStateOf("") }
    var currentSleep by remember { mutableStateOf(0.0f) }

    var currentWater by remember { mutableStateOf(0f) }

    val sleepGoal = 8f
    val waterGoal = 2000f
    val glassSize = 250f // per glass

    val context = LocalContext.current // <-- FIXED: Only one instance now
    val accentColor = Color(0xFF007F7A)

    // 1. Create a permission launcher for Activity Recognition
    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted: Boolean ->
        if (isGranted) {
            // Permission granted, start the service
            val serviceIntent = Intent(context, StepCounterService::class.java)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                context.startForegroundService(serviceIntent)
            } else {
                context.startService(serviceIntent)
            }
        } else {
            // Permission denied
            Toast.makeText(context, "Activity Recognition permission is required to track steps.", Toast.LENGTH_SHORT).show()
        }
    }

    ChroniCareTheme {
        Surface(
            modifier = Modifier.fillMaxSize(),
            color = MaterialTheme.colorScheme.background
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(20.dp)
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(24.dp)
            ) {
                Text(
                    text = "Daily Health Tracker",
                    style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold),
                    color = accentColor,
                    modifier = Modifier.padding(vertical = 8.dp)
                )

                // ---------------------------
                // SLEEP TRACKER
                // ---------------------------
                TrackerCard(
                    title = "Sleep Tracker",
                    goalText = "Goal: 8 hours",
                    currentValueText = "Current: ${currentSleep} hrs",
                    progress = currentSleep / sleepGoal,
                    progressColor = accentColor,
                    inputValue = sleepInput,
                    onInputChange = { sleepInput = it },
                    buttonLabel = "Log Sleep",
                    onButtonClick = {
                        val sleepHours = sleepInput.toFloatOrNull()
                        if (sleepHours != null && sleepHours >= 0) {
                            currentSleep = sleepHours
                            sharedData.saveSleepData(sleepHours)
                            Toast.makeText(context, "Sleep logged: $sleepHours hours", Toast.LENGTH_SHORT).show()
                            sleepInput = ""
                        } else {
                            Toast.makeText(context, "Enter valid sleep hours", Toast.LENGTH_SHORT).show()
                        }
                    },
                    inputLabel = "Enter sleep hours"
                )

                // ---------------------------
                // WATER TRACKER (GLASS ICON VERSION)
                // ---------------------------
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                    shape = RoundedCornerShape(16.dp),
                    elevation = CardDefaults.cardElevation(defaultElevation = 6.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Text(
                            text = "Water Intake Tracker",
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold),
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = "Goal: 2000ml (8 glasses)",
                            style = MaterialTheme.typography.bodyMedium
                        )
                        LinearProgressIndicator(
                            progress = (currentWater / waterGoal).coerceIn(0f, 1f),
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(8.dp)
                                .clip(RoundedCornerShape(4.dp)),
                            color = accentColor
                        )
                        Text(
                            text = "Current: ${currentWater.toInt()} ml",
                            style = MaterialTheme.typography.bodyLarge.copy(
                                color = accentColor,
                                fontWeight = FontWeight.Medium
                            )
                        )
                        // GLASS ICONS ROW
                        WaterGlassesRow(
                            currentWater = currentWater,
                            waterGoal = waterGoal,
                            glassSize = glassSize,
                            onGlassClick = {
                                val newAmount = (currentWater + glassSize).coerceAtMost(waterGoal)
                                currentWater = newAmount
                                sharedData.addWaterData(glassSize)
                                Toast.makeText(context, "Logged 250ml", Toast.LENGTH_SHORT).show()
                            }
                        )
                    }
                }

                // ---------------------------
// STEP TRACKER CARD
// ---------------------------
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                    shape = RoundedCornerShape(16.dp),
                    elevation = CardDefaults.cardElevation(defaultElevation = 6.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {

                        Text(
                            text = "Step Tracker",
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold),
                            color = MaterialTheme.colorScheme.onSurface
                        )

                        Text(
                            text = "Tap the button to start counting steps.",
                            style = MaterialTheme.typography.bodyMedium
                        )

                        // Start Button
                        Button(
                            onClick = {
                                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                                    permissionLauncher.launch(Manifest.permission.ACTIVITY_RECOGNITION)
                                } else {
                                    val serviceIntent = Intent(context, StepCounterService::class.java)
                                    context.startService(serviceIntent)
                                }
                            },
                            modifier = Modifier.fillMaxWidth(),
                            colors = ButtonDefaults.buttonColors(containerColor = accentColor)
                        ) {
                            Text("Start Step Tracking", color = Color.White)
                        }
                    }
                }




            }
        }
    }
}



// ... (The rest of your code: WaterGlassesRow and TrackerCard are unchanged and correct) ...
@Composable
fun WaterGlassesRow(
    currentWater: Float,
    waterGoal: Float,
    glassSize: Float,
    onGlassClick: () -> Unit
) {
    val totalGlasses = (waterGoal / glassSize).toInt()
    val filledGlasses = (currentWater / glassSize).toInt()

    Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
        repeat(totalGlasses) { index ->
            val isFilled = index < filledGlasses

            IconButton(onClick = onGlassClick) {
                Icon(
                    imageVector = if (isFilled) Icons.Filled.LocalDrink else Icons.Outlined.LocalDrink,
                    contentDescription = "Glass of water",
                    tint = if (isFilled) Color(0xFF007F7A) else Color.Gray,
                    modifier = Modifier.size(40.dp)
                )
            }
        }
    }
}

@Composable
fun TrackerCard(
    title: String,
    goalText: String,
    currentValueText: String,
    progress: Float,
    progressColor: Color,
    inputValue: String,
    onInputChange: (String) -> Unit,
    buttonLabel: String,
    onButtonClick: () -> Unit,
    inputLabel: String
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 6.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold),
                color = MaterialTheme.colorScheme.onSurface
            )

            Text(goalText, style = MaterialTheme.typography.bodyMedium)

            LinearProgressIndicator(
                progress = { progress.coerceIn(0f, 1f) },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(8.dp)
                    .clip(RoundedCornerShape(4.dp)),
                color = progressColor
            )

            Text(
                text = currentValueText,
                style = MaterialTheme.typography.bodyLarge.copy(
                    color = progressColor,
                    fontWeight = FontWeight.Medium
                )
            )

            OutlinedTextField(
                value = inputValue,
                onValueChange = onInputChange,
                label = { Text(inputLabel) },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                shape = RoundedCornerShape(10.dp)
            )

            Button(
                onClick = onButtonClick,
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(containerColor = progressColor)
            ) {
                Text(buttonLabel, color = Color.White)
            }
        }
    }
}
