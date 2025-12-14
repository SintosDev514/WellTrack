package com.example.chronicare.services

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Context
import android.content.Intent
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.os.Build
import android.os.IBinder
import android.util.Log
import androidx.core.app.NotificationCompat
import com.example.chronicare.R
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import java.text.SimpleDateFormat
import java.util.*

class StepCounterService : Service(), SensorEventListener {

    private lateinit var sensorManager: SensorManager
    private var stepCounterSensor: Sensor? = null

    // -1 indicates the value has not been initialized for the current day
    private var stepsAtStartOfDay = -1
    private var stepsToday = 0
    private var lastUploadedSteps = 0 // To track for efficient uploads

    companion object {
        const val NOTIFICATION_ID = 101
        const val CHANNEL_ID = "StepCounterChannel"
        private const val TAG = "StepCounterService" // For logging
    }

    override fun onCreate() {
        super.onCreate()
        sensorManager = getSystemService(Context.SENSOR_SERVICE) as SensorManager
        stepCounterSensor = sensorManager.getDefaultSensor(Sensor.TYPE_STEP_COUNTER)

        if (stepCounterSensor == null) {
            Log.e(TAG, "This device does not have a step counter sensor. Stopping service.")
            stopSelf()
            return
        }

        // **FIX #1:** Load the baseline using the robust date-checking logic
        loadBaselineForToday()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        startForegroundService()
        sensorManager.registerListener(this, stepCounterSensor, SensorManager.SENSOR_DELAY_UI)
        Log.d(TAG, "Service started and listener registered.")

        // **FIX #2:** Upload the last known value when the service starts, in case it was killed.
        if (stepsToday > 0) {
            uploadStepsToFirebase(stepsToday)
        }
        return START_STICKY // Ensures service restarts if killed by the system
    }

    private fun startForegroundService() {
        val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                "Step Counter",
                NotificationManager.IMPORTANCE_LOW
            )
            notificationManager.createNotificationChannel(channel)
        }

        val notification = NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle("Tracking your steps")
            .setContentText("Steps today: $stepsToday")
            .setSmallIcon(R.drawable.ic_directions_walk) // Make sure this drawable exists
            .setOngoing(true)
            .build()

        startForeground(NOTIFICATION_ID, notification)
    }

    override fun onSensorChanged(event: SensorEvent?) {
        if (event?.sensor?.type != Sensor.TYPE_STEP_COUNTER) return

        val totalStepsSinceBoot = event.values[0].toInt()

        // If stepsAtStartOfDay is -1, it means it's a new day or post-reboot.
        // Set the baseline for today.
        if (stepsAtStartOfDay == -1) {
            stepsAtStartOfDay = totalStepsSinceBoot
            saveBaselineForToday(totalStepsSinceBoot)
        }

        // This calculation is now robust against reboots.
        stepsToday = totalStepsSinceBoot - stepsAtStartOfDay

        updateNotification()

        // **FIX #2:** EFFICIENT FIREBASE UPLOADS
        // Only upload every 50 steps to avoid excessive writes.
        if (stepsToday > 0 && stepsToday - lastUploadedSteps >= 50) {
            uploadStepsToFirebase(stepsToday)
            lastUploadedSteps = stepsToday // Update the last uploaded value
        }
    }

    private fun updateNotification() {
        val notification = NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle("Tracking your steps")
            .setContentText("Steps today: $stepsToday")
            .setSmallIcon(R.drawable.ic_directions_walk)
            .setOngoing(true)
            .build()

        val manager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        manager.notify(NOTIFICATION_ID, notification)
    }

    private fun uploadStepsToFirebase(steps: Int) {
        val userId = FirebaseAuth.getInstance().currentUser?.uid
        if (userId == null) {
            Log.e(TAG, "Cannot upload steps: User is not logged in.")
            return // Exit if there's no user to save data for
        }

        val db = FirebaseFirestore.getInstance()
        val todayDateString = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
        val docRef = db.collection("users").document(userId)
            .collection("dailySteps").document(todayDateString)

        val data = mapOf(
            "steps" to steps,
            "lastUpdated" to System.currentTimeMillis() // Changed "timestamp" to a more descriptive name
        )

        docRef.set(data)
            .addOnSuccessListener { Log.d(TAG, "Successfully uploaded $steps steps to Firebase.") }
            .addOnFailureListener { e -> Log.e(TAG, "Error uploading steps to Firebase.", e) }
    }

    // --- START: ROBUST BASELINE LOGIC ---

    private fun saveBaselineForToday(baselineValue: Int) {
        val prefs = getSharedPreferences("stepPrefs", MODE_PRIVATE)
        val todayDateString = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())

        // Save both the baseline value AND the date it was recorded
        prefs.edit()
            .putInt("baselineSteps", baselineValue)
            .putString("baselineDate", todayDateString)
            .apply()
    }

    private fun loadBaselineForToday() {
        val prefs = getSharedPreferences("stepPrefs", MODE_PRIVATE)
        val todayDateString = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
        val savedDate = prefs.getString("baselineDate", null)

        // **THE KEY LOGIC**: Only use the saved baseline if the date matches today's date.
        if (savedDate == todayDateString) {
            // It's the same day, load the saved baseline
            stepsAtStartOfDay = prefs.getInt("baselineSteps", -1)
        } else {
            // It's a new day (or first run), so we reset the baseline.
            // onSensorChanged will then create a new baseline with the first sensor event.
            stepsAtStartOfDay = -1
        }
    }
    // --- END: ROBUST BASELINE LOGIC ---


    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {}

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onDestroy() {
        super.onDestroy()
        Log.d(TAG, "Service is being destroyed. Saving final step count.")
        // **FIX #2:** Save the final count before the service is terminated.
        if (stepsToday > 0) {
            uploadStepsToFirebase(stepsToday)
        }
        sensorManager.unregisterListener(this)
    }
}
