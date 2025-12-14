package com.example.chronicare.screens

import android.util.Log
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.chronicare.homeScreens.MedicationReminder
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class SharedData : ViewModel() {
    var username by mutableStateOf("")

    // --- Firebase Instances ---
    private val auth = FirebaseAuth.getInstance()
    private val database = FirebaseDatabase.getInstance().getReference("users")

    // --- NEW: Medication Reminder State ---
    val medicationReminders = mutableStateListOf<MedicationReminder>()

    init {
        // Load data when the ViewModel is created
        loadMedicationReminders()
    }

    // --- NEW: Medication Data Functions ---

    /**
     * Loads all medication reminders for the current user from Firebase.
     */
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
                Log.d("Firebase", "Medication reminders loaded: ${medicationReminders.size} items.")
            }

            override fun onCancelled(error: DatabaseError) {
                Log.e("Firebase", "Failed to load medication reminders.", error.toException())
            }
        })
    }

    /**
     * Adds a new medication reminder to Firebase.
     */
    fun addMedicationReminder(reminder: MedicationReminder) {
        val userId = auth.currentUser?.uid ?: return
        database.child(userId).child("medication_reminders").child(reminder.id).setValue(reminder)
    }

    /**
     * Updates the status of an existing medication reminder in Firebase.
     */
    fun updateMedicationStatus(reminderId: String, newStatus: String) {
        val userId = auth.currentUser?.uid ?: return
        database.child(userId).child("medication_reminders").child(reminderId).child("status").setValue(newStatus)
    }


    // --- Existing Health Log Functions ---

    fun saveSleepData(sleepHours: Float) {
        val userId = auth.currentUser?.uid ?: return
        val currentDate = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
        database.child(userId).child("health_logs").child(currentDate).child("sleepHours").setValue(sleepHours)
    }

    fun addWaterData(waterML: Float) {
        val userId = auth.currentUser?.uid ?: return
        val currentDate = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
        val waterLogRef = database.child(userId).child("health_logs").child(currentDate).child("waterIntakeML")

        waterLogRef.get().addOnSuccessListener { dataSnapshot ->
            val currentWater = dataSnapshot.getValue(Float::class.java) ?: 0f
            val newTotalWater = currentWater + waterML
            waterLogRef.setValue(newTotalWater)
        }
    }
}
