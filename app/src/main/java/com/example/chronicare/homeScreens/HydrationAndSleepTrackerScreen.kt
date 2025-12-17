package com.example.chronicare.homeScreens

import android.Manifest
import android.content.Intent
import android.os.Build
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.LocalDrink
import androidx.compose.material.icons.outlined.LocalDrink
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.chronicare.screens.SharedData
import com.example.chronicare.services.StepCounterService
import com.example.chronicare.ui.theme.ChroniCareTheme

@Composable
fun HealthTrackingApp(
    navController: NavController,
    sharedData: SharedData
) {
    // ---------------- STATE ----------------
    var sleepInput by remember { mutableStateOf("") }
    var currentSleep by remember { mutableStateOf(0f) }
    var currentWater by remember { mutableStateOf(0f) }
    var isStepTracking by remember { mutableStateOf(false) }

    val sleepGoal = 8f
    val waterGoal = 2000f
    val glassSize = 250f

    val context = LocalContext.current
    val accentColor = Color(0xFF007F7A)

    // ---------------- PERMISSION ----------------
    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            val intent = Intent(context, StepCounterService::class.java)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                context.startForegroundService(intent)
            } else {
                context.startService(intent)
            }
            isStepTracking = true
        } else {
            Toast.makeText(
                context,
                "Activity Recognition permission required",
                Toast.LENGTH_SHORT
            ).show()
        }
    }

    // ---------------- UI ----------------
    ChroniCareTheme {
        Surface(
            modifier = Modifier.fillMaxSize(),
            color = MaterialTheme.colorScheme.background
        ) {
            Column(
                modifier = Modifier
                    .padding(20.dp)
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(24.dp)
            ) {

                Text(
                    text = "Daily Health Tracker",
                    style = MaterialTheme.typography.headlineSmall.copy(
                        fontWeight = FontWeight.Bold
                    ),
                    color = accentColor
                )

                // ---------------- SLEEP TRACKER ----------------
                TrackerCard(
                    title = "Sleep Tracker",
                    goalText = "Goal: 8 hours",
                    currentValueText = "Current: $currentSleep hrs",
                    progress = currentSleep / sleepGoal,
                    progressColor = accentColor,
                    inputValue = sleepInput,
                    onInputChange = { sleepInput = it },
                    buttonLabel = "Log Sleep",
                    onButtonClick = {
                        val value = sleepInput.toFloatOrNull()
                        if (value != null && value >= 0) {
                            currentSleep = value
                            sharedData.saveSleepData(value)
                            sleepInput = ""
                            Toast.makeText(
                                context,
                                "Sleep logged: $value hrs",
                                Toast.LENGTH_SHORT
                            ).show()
                        } else {
                            Toast.makeText(
                                context,
                                "Invalid sleep hours",
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                    },
                    inputLabel = "Enter sleep hours"
                )

                // ---------------- WATER TRACKER ----------------
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    elevation = CardDefaults.cardElevation(6.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Text(
                            "Water Intake Tracker",
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.SemiBold
                            )
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
                            "Current: ${currentWater.toInt()} ml",
                            color = accentColor,
                            fontWeight = FontWeight.Medium
                        )

                        WaterGlassesRow(
                            currentWater = currentWater,
                            waterGoal = waterGoal,
                            glassSize = glassSize
                        ) {
                            currentWater =
                                (currentWater + glassSize).coerceAtMost(waterGoal)
                            sharedData.addWaterData(glassSize)
                        }
                    }
                }

                // ---------------- STEP TRACKER ----------------
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    elevation = CardDefaults.cardElevation(6.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Text(
                            "Step Tracker",
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.SemiBold
                            )
                        )

                        Text(
                            if (isStepTracking)
                                "Step counter is running"
                            else
                                "Tap to start counting steps"
                        )

                        Button(
                            onClick = {
                                val intent =
                                    Intent(context, StepCounterService::class.java)

                                if (!isStepTracking) {
                                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                                        permissionLauncher.launch(
                                            Manifest.permission.ACTIVITY_RECOGNITION
                                        )
                                    } else {
                                        context.startService(intent)
                                        isStepTracking = true
                                    }
                                } else {
                                    context.stopService(intent)
                                    isStepTracking = false
                                }
                            },
                            modifier = Modifier.fillMaxWidth(),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = if (isStepTracking)
                                    Color.Red
                                else accentColor
                            )
                        ) {
                            Text(
                                if (isStepTracking)
                                    "Stop Step Tracking"
                                else
                                    "Start Step Tracking",
                                color = Color.White
                            )
                        }
                    }
                }
            }
        }
    }
}

/* ---------------- HELPERS ---------------- */

@Composable
fun WaterGlassesRow(
    currentWater: Float,
    waterGoal: Float,
    glassSize: Float,
    onGlassClick: () -> Unit
) {
    val total = (waterGoal / glassSize).toInt()
    val filled = (currentWater / glassSize).toInt()

    Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
        repeat(total) { index ->
            IconButton(onClick = onGlassClick) {
                Icon(
                    imageVector =
                        if (index < filled) Icons.Filled.LocalDrink
                        else Icons.Outlined.LocalDrink,
                    contentDescription = "Water glass",
                    tint =
                        if (index < filled) Color(0xFF007F7A)
                        else Color.Gray,
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
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(6.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(title, fontWeight = FontWeight.SemiBold)
            Text(goalText)

            LinearProgressIndicator(
                progress = progress.coerceIn(0f, 1f),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(8.dp),
                color = progressColor
            )

            Text(currentValueText, color = progressColor)

            OutlinedTextField(
                value = inputValue,
                onValueChange = onInputChange,
                label = { Text(inputLabel) },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )

            Button(
                onClick = onButtonClick,
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(
                    containerColor = progressColor
                )
            ) {
                Text(buttonLabel, color = Color.White)
            }
        }
    }
}
