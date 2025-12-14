package com.example.chronicare

sealed class NavRoutes(val route: String) {

    object Dashboard : NavRoutes("Dashboard")
    object DailyLog : NavRoutes("Daily & Weekly Log")
    object HealthInsights : NavRoutes("Health Insights")
    object MedicationReminder : NavRoutes("Medication & TreatmentReminder")
    object ProgressTracking : NavRoutes("Progress Tracking")
    object HydrationAndSleepTrackerScreen : NavRoutes("Daily Health Tracker")
    object Settings : NavRoutes("Settings & Preferences")
    object Logout : NavRoutes("Logout")

    object Content : NavRoutes("content/{itemId}") {
        fun createRoute(itemId: String) = "content/$itemId"
    }

}