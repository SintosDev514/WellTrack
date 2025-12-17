package com.example.chronicare.homeScreens

import androidx.compose.material.icons.filled.SportsBar
import androidx.compose.material.icons.outlined.SportsBar
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
import androidx.compose.material.icons.filled.Opacity
import androidx.compose.material.icons.filled.WaterDrop
import androidx.compose.material.icons.outlined.LocalDrink
import androidx.compose.material.icons.outlined.WaterDrop
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
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

    // 💧 DIFFERENT WATER ICON AMOUNTS
    val waterGlasses = listOf(
        WaterGlass( // Small cup
            emptyIcon = Icons.Outlined.LocalDrink,
            filledIcon = Icons.Filled.LocalDrink,
            amount = 150f
        ),
        WaterGlass( // Glass cup
            emptyIcon = Icons.Outlined.LocalDrink,
            filledIcon = Icons.Filled.LocalDrink,
            amount = 250f
        ),
        WaterGlass( // Plastic bottle
            emptyIcon = Icons.Outlined.LocalDrink,
            filledIcon = Icons.Filled.Opacity,
            amount = 350f
        ),
        WaterGlass( // Large bottle
            emptyIcon = Icons.Outlined.SportsBar,
            filledIcon = Icons.Filled.SportsBar,
            amount = 500f
        )
    )


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
        Surface(modifier = Modifier.fillMaxSize()) {
            Column(
                modifier = Modifier
                    .padding(20.dp)
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(24.dp)
            ) {

                Text(
                    "Daily Health Tracker",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
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
                        sleepInput.toFloatOrNull()?.let {
                            currentSleep = it
                            sharedData.saveSleepData(it)
                            sleepInput = ""
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
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.SemiBold
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
                            waterGlasses = waterGlasses
                        ) { amount ->
                            currentWater =
                                (currentWater + amount).coerceAtMost(waterGoal)
                            sharedData.addWaterData(amount)
                            Toast.makeText(
                                context,
                                "Added ${amount.toInt()} ml",
                                Toast.LENGTH_SHORT
                            ).show()
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

                        Text("Step Tracker", fontWeight = FontWeight.SemiBold)

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

/* ---------------- WATER MODEL ---------------- */

data class WaterGlass(
    val emptyIcon: androidx.compose.ui.graphics.vector.ImageVector,
    val filledIcon: androidx.compose.ui.graphics.vector.ImageVector,
    val amount: Float
)

/* ---------------- WATER ROW ---------------- */

@Composable
fun WaterGlassesRow(
    currentWater: Float,
    waterGoal: Float,
    waterGlasses: List<WaterGlass>,
    onGlassClick: (Float) -> Unit
) {
    var consumed = 0f

    Row(horizontalArrangement = Arrangement.spacedBy(18.dp)) {
        waterGlasses.forEach { glass ->
            val isFilled = currentWater >= consumed + glass.amount
            consumed += glass.amount

            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                IconButton(onClick = { onGlassClick(glass.amount) }) {
                    Icon(
                        imageVector =
                            if (isFilled) glass.filledIcon
                            else glass.emptyIcon,
                        contentDescription = "Water",
                        tint =
                            if (isFilled) Color(0xFF007F7A)
                            else Color.Gray,
                        modifier = Modifier.size(42.dp)
                    )
                }

                Text(
                    "${glass.amount.toInt()} ml",
                    style = MaterialTheme.typography.labelSmall
                )
            }
        }
    }
}

/* ---------------- TRACKER CARD ---------------- */

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
                modifier = Modifier.fillMaxWidth(),
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
                colors = ButtonDefaults.buttonColors(containerColor = progressColor)
            ) {
                Text(buttonLabel, color = Color.White)
            }
        }
    }
}
