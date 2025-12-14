package com.example.chronicare.homeScreens

import android.util.Log
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.chronicare.model.HealthInsight
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import java.text.SimpleDateFormat
import java.util.*

// -------------------- DATA MODELS --------------------
data class LogEntry(
    val date: String,
    val steps: Long = 0,
    val sleepHours: Float = 0f,
    val waterMl: Float = 0f,
    val medications: List<MedicationReminder> = emptyList()
)

class LogViewModel : ViewModel() {

    val healthInsights = mutableStateOf<List<HealthInsight>>(emptyList())
    private val _dailyLogs = mutableStateOf<List<LogEntry>>(emptyList())
    val dailyLogs = _dailyLogs
    val isLoading = mutableStateOf(true)

    private val displayFormatter = SimpleDateFormat("MMM dd, yyyy", Locale.getDefault())
    private val keyFormatter = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())

    init {
        fetchDailyLogs()
    }

    private fun fetchDailyLogs() {
        viewModelScope.launch {
            try {
                val userId = FirebaseAuth.getInstance().currentUser?.uid ?: run {
                    isLoading.value = false
                    return@launch
                }

                val firestore = FirebaseFirestore.getInstance()
                val realtimeDb = FirebaseDatabase.getInstance().reference

                // -------------------- FETCH STEPS (Firestore) --------------------
                val stepDocs = firestore.collection("users")
                    .document(userId)
                    .collection("dailySteps")
                    .orderBy(
                        com.google.firebase.firestore.FieldPath.documentId(),
                        Query.Direction.DESCENDING
                    )
                    .get()
                    .await()

                val stepMap = mutableMapOf<String, Long>()
                for (doc in stepDocs) {
                    val steps: Long = doc.getLong("steps")
                        ?: doc.getDouble("steps")?.toLong()
                        ?: doc.getString("steps")?.toLongOrNull()
                        ?: 0L

                    val docDate = try {
                        val parsed = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
                            .parse(doc.id) ?: Date()
                        keyFormatter.format(parsed) // keep key format consistent
                    } catch (e: Exception) {
                        doc.id
                    }

                    stepMap[docDate] = steps
                }

                // -------------------- FETCH SLEEP & WATER (Realtime DB) --------------------
                val healthSnapshot = realtimeDb
                    .child("users")
                    .child(userId)
                    .child("health_logs")
                    .get()
                    .await()

                val sleepMap = mutableMapOf<String, Float>()
                val waterMap = mutableMapOf<String, Float>()

                for (dateSnap in healthSnapshot.children) {
                    val rawDate = dateSnap.key ?: continue
                    val dateKey = rawDate // keys in yyyy-MM-dd format

                    val sleepValue = dateSnap.child("sleepHours").value
                    val sleepFloat = when (sleepValue) {
                        is Long -> sleepValue.toFloat()
                        is Double -> sleepValue.toFloat()
                        is String -> sleepValue.toFloatOrNull() ?: 0f
                        else -> 0f
                    }
                    sleepMap[dateKey] = sleepFloat

                    val waterValue = dateSnap.child("waterIntakeML").value
                    val waterFloat = when (waterValue) {
                        is Long -> waterValue.toFloat()
                        is Double -> waterValue.toFloat()
                        is String -> waterValue.toFloatOrNull() ?: 0f
                        else -> 0f
                    }
                    waterMap[dateKey] = waterFloat

                    Log.d("LogViewModel", "date=$dateKey sleep=$sleepFloat water=$waterFloat")
                }

                // -------------------- FETCH MEDICATIONS (Realtime DB) --------------------
                val medsSnapshot = realtimeDb
                    .child("users")
                    .child(userId)
                    .child("medication_reminders")
                    .get()
                    .await()

                val medMap = mutableMapOf<String, MutableList<MedicationReminder>>()
                for (medSnap in medsSnapshot.children) {
                    val med = MedicationReminder(
                        id = medSnap.child("id").getValue(String::class.java) ?: "",
                        medicationName = medSnap.child("medicationName").getValue(String::class.java) ?: "",
                        dosage = medSnap.child("dosage").getValue(String::class.java) ?: "",
                        status = medSnap.child("status").getValue(String::class.java) ?: "",
                        dateTime = medSnap.child("dateTime").getValue(String::class.java) ?: ""
                    )

                    val medDate = try {
                        val parsed = SimpleDateFormat("MMM dd, yyyy, hh:mm a", Locale.getDefault())
                            .parse(med.dateTime) ?: Date()
                        keyFormatter.format(parsed)
                    } catch (e: Exception) {
                        med.dateTime.substringBefore(",")
                    }

                    val list = medMap.getOrDefault(medDate, mutableListOf())
                    list.add(med)
                    medMap[medDate] = list
                }

                // -------------------- MERGE BY DATE --------------------
                val allDates = (stepMap.keys + sleepMap.keys + waterMap.keys + medMap.keys)
                    .distinct()
                    .sortedDescending()

                val logs = allDates.map { dateKey ->
                    LogEntry(
                        date = displayFormatter.format(keyFormatter.parse(dateKey) ?: Date()),
                        steps = stepMap[dateKey] ?: 0,
                        sleepHours = sleepMap[dateKey] ?: 0f,
                        waterMl = waterMap[dateKey] ?: 0f,
                        medications = medMap[dateKey] ?: emptyList()
                    )
                }

                _dailyLogs.value = logs
                buildHealthInsights(logs)

            } catch (e: Exception) {
                Log.e("LogViewModel", "Error fetching logs", e)
            } finally {
                isLoading.value = false
            }
        }
    }

    private fun buildHealthInsights(logs: List<LogEntry>) {
        if (logs.isEmpty()) return
        val latest = logs.first()

        healthInsights.value = listOf(
            HealthInsight(
                title = "Steps Walked",
                value = latest.steps.toString(),
                description = "Steps recorded for the latest day"
            ),
            HealthInsight(
                title = "Sleep Duration",
                value = "${latest.sleepHours} hrs",
                description = "Sleep duration from last night"
            ),
            HealthInsight(
                title = "Water Intake",
                value = "${latest.waterMl / 1000} L",
                description = "Water consumed"
            )
        )
    }
}
