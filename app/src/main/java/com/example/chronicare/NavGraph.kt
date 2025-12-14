package com.example.chronicare

import androidx.compose.runtime.Composable
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.chronicare.homeScreens.DashboardScreen

import com.example.chronicare.homeScreens.SettingsPreferences
// Import the AuthViewModel and HomeScreenMain
import com.example.chronicare.screens.AuthViewModel
import com.example.chronicare.screens.HomeScreenMain
import com.example.chronicare.screens.LoginScreen
import com.example.chronicare.screens.SharedData
import com.example.chronicare.screens.SignUpScreen

@Composable
fun AppNavGraph(
    navController: NavHostController = rememberNavController()
) {
    // Get an instance of each ViewModel. Compose will manage their lifecycle.
    val authViewModel: AuthViewModel = viewModel()
    val sharedData: SharedData = viewModel()

    NavHost(navController = navController, startDestination = "login") {
        // Authentication Screens
        composable("login") {
            // LoginScreen needs the AuthViewModel
            LoginScreen(navController = navController, authViewModel = authViewModel)
        }
        composable("signup") {
            // SignUpScreen also needs the AuthViewModel
            SignUpScreen(navController = navController, authViewModel = authViewModel)
        }

        // Main App Screens (after login)
        composable("home") {
            HomeScreenMain(navController = navController, sharedData = sharedData)
        }
        composable("dashboard") {
            DashboardScreen(navController = navController, )
        }
        composable("SettingsPreferences") {
            // Pass both if needed, or just the one it uses
            SettingsPreferences(navController = navController, sharedData = sharedData)
        }
    }
}
