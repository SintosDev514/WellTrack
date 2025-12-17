package com.example.chronicare.screens

import android.util.Log
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import com.example.chronicare.homeScreens.MedicationReminder
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class SharedData : ViewModel() {

    // --------------------
    // UI STATE
    // --------------------
    var username by mutableStateOf("")

    // --------------------
    // FIREBASE
    // --------------------
    private val auth = FirebaseAuth.getInstance()
    private val database = FirebaseDatabase.getInstance().getReference("users")

    // --------------------
    // MEDICATION REMINDERS STATE
    // --------------------
    val medicationReminders = mutableStateListOf<MedicationReminder>()

    init {
        loadMedicationReminders()
    }

    // --------------------
    // LOAD REMINDERS
    // --------------------
    private fun loadMedicationReminders() {
        val userId = auth.currentUser?.uid ?: return
        val remindersRef = database.child(userId).child("medication_reminders")

        remindersRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                medicationReminders.clear()

                snapshot.children.forEach { data ->
                    val reminder = data.getValue(MedicationReminder::class.java)
                    if (reminder != null) {
                        medicationReminders.add(reminder)
                    }
                }

                Log.d(
                    "SharedData",
                    "Medication reminders loaded: ${medicationReminders.size}"
                )
            }

            override fun onCancelled(error: DatabaseError) {
                Log.e(
                    "SharedData",
                    "Failed to load medication reminders",
                    error.toException()
                )
            }
        })
    }

    // --------------------
    // ADD REMINDER
    // --------------------
    fun addMedicationReminder(reminder: MedicationReminder) {
        val userId = auth.currentUser?.uid ?: return

        database
            .child(userId)
            .child("medication_reminders")
            .child(reminder.id)
            .setValue(reminder)
    }

    // --------------------
    // UPDATE STATUS
    // --------------------
    fun updateMedicationStatus(reminderId: String, newStatus: String) {
        val userId = auth.currentUser?.uid ?: return

        database
            .child(userId)
            .child("medication_reminders")
            .child(reminderId)
            .child("status")
            .setValue(newStatus)
    }

    // --------------------
    // DELETE REMINDER ✅
    // --------------------
    fun removeMedicationReminder(reminderId: String) {
        val userId = auth.currentUser?.uid ?: return

        database
            .child(userId)
            .child("medication_reminders")
            .child(reminderId)
            .removeValue()
    }

    // --------------------
    // HEALTH LOG FUNCTIONS
    // --------------------
    fun saveSleepData(sleepHours: Float) {
        val userId = auth.currentUser?.uid ?: return
        val currentDate =
            SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())

        database
            .child(userId)
            .child("health_logs")
            .child(currentDate)
            .child("sleepHours")
            .setValue(sleepHours)
    }

    fun addWaterData(waterML: Float) {
        val userId = auth.currentUser?.uid ?: return
        val currentDate =
            SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())

        val waterLogRef = database
            .child(userId)
            .child("health_logs")
            .child(currentDate)
            .child("waterIntakeML")

        waterLogRef.get().addOnSuccessListener { snapshot ->
            val currentWater = snapshot.getValue(Float::class.java) ?: 0f
            waterLogRef.setValue(currentWater + waterML)
        }
    }
}
