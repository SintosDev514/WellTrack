package com.example.chronicare.homeScreens

import android.Manifest
import android.annotation.SuppressLint
import android.app.*
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat
import com.example.chronicare.screens.SharedData
import java.text.SimpleDateFormat
import java.util.*

/* -------------------------------
   DATA CLASS
-------------------------------- */
data class MedicationReminder(
    val id: String = "",
    val medicationName: String = "",
    val dosage: String = "",
    val dateTime: String = "",
    var status: String = "Due"
)

/* -------------------------------
   NOTIFICATION RECEIVER
-------------------------------- */
class ReminderReceiver : BroadcastReceiver() {

    @SuppressLint("MissingPermission")
    override fun onReceive(context: Context, intent: Intent) {

        val notificationId =
            (intent.getStringExtra("notificationId") ?: UUID.randomUUID().toString()).hashCode()

        val medicationName = intent.getStringExtra("medicationName") ?: "Medication"
        val dosage = intent.getStringExtra("dosage") ?: ""

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                "reminder_channel",
                "Medication Reminders",
                NotificationManager.IMPORTANCE_HIGH
            )
            notificationManager(context).createNotificationChannel(channel)
        }

        val notification = NotificationCompat.Builder(context, "reminder_channel")
            .setSmallIcon(android.R.drawable.ic_dialog_info)
            .setContentTitle("Medication Reminder")
            .setContentText("Time to take $medicationName ($dosage)")
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)
            .build()

        if (
            ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.POST_NOTIFICATIONS
            ) == PackageManager.PERMISSION_GRANTED || Build.VERSION.SDK_INT < 33
        ) {
            notificationManager(context).notify(notificationId, notification)
        }
    }
}

private fun notificationManager(context: Context) =
    context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

/* -------------------------------
   SCHEDULE REMINDER
-------------------------------- */
fun scheduleReminder(context: Context, reminder: MedicationReminder) {

    val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
    val sdf = SimpleDateFormat("MMM dd, yyyy, hh:mm a", Locale.getDefault())
    val calendar = Calendar.getInstance()

    calendar.time = sdf.parse(reminder.dateTime) ?: return

    val intent = Intent(context, ReminderReceiver::class.java).apply {
        putExtra("notificationId", reminder.id)
        putExtra("medicationName", reminder.medicationName)
        putExtra("dosage", reminder.dosage)
    }

    val pendingIntent = PendingIntent.getBroadcast(
        context,
        reminder.id.hashCode(),
        intent,
        PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
    )

    alarmManager.setExactAndAllowWhileIdle(
        AlarmManager.RTC_WAKEUP,
        calendar.timeInMillis,
        pendingIntent
    )
}

/* -------------------------------
   CANCEL REMINDER (DELETE)
-------------------------------- */
fun cancelReminder(context: Context, reminder: MedicationReminder) {

    val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager

    val intent = Intent(context, ReminderReceiver::class.java)

    val pendingIntent = PendingIntent.getBroadcast(
        context,
        reminder.id.hashCode(),
        intent,
        PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
    )

    alarmManager.cancel(pendingIntent)
}

/* -------------------------------
   ACCENT COLOR
-------------------------------- */
val accentColorReminder = Color(0xFF007F7A)

/* -------------------------------
   MAIN SCREEN
-------------------------------- */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MedicationTreatmentReminderScreen(sharedData: SharedData) {

    val context = LocalContext.current
    var showAddDialog by remember { mutableStateOf(false) }

    val launcher =
        rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) { }

    LaunchedEffect(true) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            launcher.launch(Manifest.permission.POST_NOTIFICATIONS)
        }
    }

    Scaffold(
        floatingActionButton = {
            FloatingActionButton(
                onClick = { showAddDialog = true },
                containerColor = accentColorReminder
            ) {
                Icon(Icons.Filled.Add, contentDescription = "Add", tint = Color.White)
            }
        }
    ) { padding ->

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp)
        ) {

            Text(
                "Medication Reminders",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                color = accentColorReminder
            )

            Spacer(modifier = Modifier.height(16.dp))

            LazyColumn(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                items(sharedData.medicationReminders.reversed(), key = { it.id }) { reminder ->
                    MedicationReminderCard(
                        medication = reminder,
                        onStatusChange = {
                            sharedData.updateMedicationStatus(reminder.id, it)
                        },
                        onDelete = {
                            cancelReminder(context, reminder)
                            sharedData.removeMedicationReminder(reminder.id)
                        }
                    )
                }
            }
        }
    }

    if (showAddDialog) {
        AddReminderDialog(
            onDismiss = { showAddDialog = false },
            onAdd = { name, dosage, dateTime ->
                val reminder = MedicationReminder(
                    id = UUID.randomUUID().toString(),
                    medicationName = name,
                    dosage = dosage,
                    dateTime = dateTime
                )
                sharedData.addMedicationReminder(reminder)
                scheduleReminder(context, reminder)
                showAddDialog = false
            }
        )
    }
}

/* -------------------------------
   REMINDER CARD
-------------------------------- */
@Composable
fun MedicationReminderCard(
    medication: MedicationReminder,
    onStatusChange: (String) -> Unit,
    onDelete: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(6.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(medication.medicationName, fontWeight = FontWeight.Bold)
                Text(medication.dateTime, color = accentColorReminder)
            }

            Spacer(modifier = Modifier.height(6.dp))
            Text("Dosage: ${medication.dosage}")

            Spacer(modifier = Modifier.height(10.dp))
            StatusBadge(medication.status)

            if (medication.status == "Due") {
                Spacer(modifier = Modifier.height(10.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {

                    Button(
                        onClick = { onStatusChange("Taken") },
                        modifier = Modifier.weight(1f)
                    ) { Text("Taken") }

                    Button(
                        onClick = { onStatusChange("Missed") },
                        colors = ButtonDefaults.buttonColors(containerColor = Color.Red),
                        modifier = Modifier.weight(1f)
                    ) { Text("Missed") }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            TextButton(
                onClick = onDelete,
                colors = ButtonDefaults.textButtonColors(contentColor = Color.Red),
                modifier = Modifier.align(Alignment.End)
            ) {
                Text("Delete")
            }
        }
    }
}

/* -------------------------------
   STATUS BADGE
-------------------------------- */
@Composable
fun StatusBadge(status: String) {

    val color = when (status) {
        "Taken" -> Color(0xFF10B981)
        "Missed" -> Color.Red
        else -> Color(0xFFFFA500)
    }

    Surface(color = color.copy(alpha = 0.15f), shape = RoundedCornerShape(50)) {
        Text(
            status,
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp),
            color = color,
            fontWeight = FontWeight.Bold
        )
    }
}

/* -------------------------------
   ADD REMINDER DIALOG
-------------------------------- */
@Composable
fun AddReminderDialog(
    onDismiss: () -> Unit,
    onAdd: (String, String, String) -> Unit
) {
    val context = LocalContext.current
    var name by remember { mutableStateOf("") }
    var dosage by remember { mutableStateOf("") }
    var dateTime by remember { mutableStateOf("") }

    val calendar = remember { Calendar.getInstance() }

    Dialog(onDismissRequest = onDismiss) {
        Card(
            shape = RoundedCornerShape(16.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {

                Text(
                    text = "Add Reminder",
                    fontWeight = FontWeight.Bold,
                    style = MaterialTheme.typography.titleLarge
                )

                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Medication") },
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = dosage,
                    onValueChange = { dosage = it },
                    label = { Text("Dosage") },
                    modifier = Modifier.fillMaxWidth()
                )

                /* ✅ FIXED DATE & TIME PICKER */
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable {
                            DatePickerDialog(
                                context,
                                { _, year, month, day ->
                                    calendar.set(year, month, day)

                                    TimePickerDialog(
                                        context,
                                        { _, hour, minute ->
                                            calendar.set(Calendar.HOUR_OF_DAY, hour)
                                            calendar.set(Calendar.MINUTE, minute)

                                            dateTime = SimpleDateFormat(
                                                "MMM dd, yyyy, hh:mm a",
                                                Locale.getDefault()
                                            ).format(calendar.time)
                                        },
                                        calendar.get(Calendar.HOUR_OF_DAY),
                                        calendar.get(Calendar.MINUTE),
                                        false
                                    ).show()
                                },
                                calendar.get(Calendar.YEAR),
                                calendar.get(Calendar.MONTH),
                                calendar.get(Calendar.DAY_OF_MONTH)
                            ).show()
                        }
                ) {
                    OutlinedTextField(
                        value = dateTime,
                        onValueChange = {},
                        label = { Text("Date & Time") },
                        enabled = false,   // IMPORTANT
                        modifier = Modifier.fillMaxWidth()
                    )
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    TextButton(onClick = onDismiss) {
                        Text("Cancel")
                    }

                    Button(onClick = {
                        if (name.isNotBlank() && dosage.isNotBlank() && dateTime.isNotBlank()) {
                            onAdd(name, dosage, dateTime)
                        } else {
                            Toast.makeText(
                                context,
                                "Please fill all fields and select date & time",
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                    }) {
                        Text("Add")
                    }
                }
            }
        }
    }
}

