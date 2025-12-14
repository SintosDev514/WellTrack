package com.example.chronicare.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.chronicare.NavRoutes
import com.example.chronicare.R
import com.example.chronicare.homeScreens.*
import kotlinx.coroutines.launch

@Composable
fun HomeScreenMain(navController: NavController, sharedData: SharedData) {
    DrawerApp(mainNavController = navController, sharedData = sharedData)
}

data class DrawerItem(
    val title: String,
    val route: String,
    val icon: ImageVector
)

val drawerItems = listOf(
    DrawerItem("Dashboard", NavRoutes.Dashboard.route, Icons.Filled.Dashboard),
    DrawerItem("Daily & Weekly Log", NavRoutes.DailyLog.route, Icons.Filled.CalendarToday),
    DrawerItem("Health Insights", NavRoutes.HealthInsights.route, Icons.Filled.FitnessCenter),
    DrawerItem("Medication & Treatment Reminder", NavRoutes.MedicationReminder.route, Icons.Filled.LocalPharmacy),
    DrawerItem("Daily Health Tracker", NavRoutes.HydrationAndSleepTrackerScreen.route, Icons.Filled.AccessTime),
    DrawerItem("Progress Tracking", NavRoutes.ProgressTracking.route, Icons.Filled.ShowChart),
    DrawerItem("Setting & Preferences", NavRoutes.Settings.route, Icons.Filled.Settings)
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DrawerApp(mainNavController: NavController, sharedData: SharedData) {
    val drawerState = rememberDrawerState(DrawerValue.Closed)
    val scope = rememberCoroutineScope()
    val drawerNavController = rememberNavController()
    var selectedRoute by remember { mutableStateOf(NavRoutes.Dashboard.route) }

    ModalNavigationDrawer(
        drawerState = drawerState,
        drawerContent = {
            ModalDrawerSheet {
                Text(
                    "Chronic Care Wellness Tracker",
                    style = MaterialTheme.typography.titleLarge,
                    modifier = Modifier.padding(16.dp)
                )
                drawerItems.forEach { item ->
                    NavigationDrawerItem(
                        label = { Text(item.title) },
                        icon = { Icon(item.icon, contentDescription = item.title) },
                        selected = (item.route == selectedRoute),
                        onClick = {
                            selectedRoute = item.route
                            drawerNavController.navigate(item.route) {
                                launchSingleTop = true
                                restoreState = true
                            }
                            scope.launch { drawerState.close() }
                        }
                    )
                }
            }
        }
    ) {
        Scaffold(
            topBar = {
                TopAppBar(
                    title = {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Image(
                                painter = painterResource(id = R.drawable.logo),
                                contentDescription = "App Logo",
                                modifier = Modifier.size(40.dp).padding(end = 8.dp)
                            )
                            Text(
                                text = "Chronic Care Wellness Tracker",
                                color = Color.White,
                                style = MaterialTheme.typography.titleMedium
                            )
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(containerColor = Color(0xFF007F7A)),
                    navigationIcon = {
                        IconButton(onClick = { scope.launch { drawerState.open() } }) {
                            Icon(Icons.Default.Menu, contentDescription = "Menu", tint = Color.White)
                        }
                    },
                    actions = {
                        IconButton(onClick = {
                            mainNavController.navigate("login") {
                                popUpTo(0) { inclusive = true }
                            }
                        }) {
                            Icon(Icons.Default.ExitToApp, contentDescription = "Logout", tint = Color.White)
                        }
                    }
                )
            }
        ) { innerPadding ->
            NavigationHost(
                navController = drawerNavController,
                modifier = Modifier.padding(innerPadding),
                sharedData = sharedData
            )
        }
    }
}

@Composable
fun NavigationHost(
    navController: NavHostController,
    modifier: Modifier = Modifier,
    sharedData: SharedData
) {
    NavHost(
        navController = navController,
        startDestination = NavRoutes.Dashboard.route,
        modifier = modifier
    ) {
        // The NavHost is now clean and only contains routes from the drawer menu
        composable(NavRoutes.Dashboard.route) { DashboardScreen(navController) }
        composable(NavRoutes.DailyLog.route) { DailyWeeklyLogScreen() }
        composable(NavRoutes.HealthInsights.route) { HealthInsightsScreen() }
        composable(NavRoutes.MedicationReminder.route) { MedicationTreatmentReminderScreen(sharedData = sharedData) }
        composable(NavRoutes.ProgressTracking.route) { ProgressTrackingScreen() }
        composable(NavRoutes.HydrationAndSleepTrackerScreen.route) { HealthTrackingApp(navController, sharedData) }
        composable(NavRoutes.Settings.route) { SettingsPreferences(navController, sharedData) }
    }
}
