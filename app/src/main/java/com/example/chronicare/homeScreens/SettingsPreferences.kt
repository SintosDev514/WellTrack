package com.example.chronicare.homeScreens // Make sure the package is correct

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavController
import com.example.chronicare.screens.SharedData

@OptIn(ExperimentalMaterial3Api::class)
@Composable
// FIX 1: Add NavController and SharedData to the function signature
fun SettingsPreferences(navController: NavController, sharedData: SharedData) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Settings & Preferences") },
                // FIX 2: Add a back button that uses the NavController
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back"
                        )
                    }
                }
            )
        }
    ) { innerPadding ->
        Column(modifier = Modifier.padding(innerPadding)) {
            //
            // YOUR SETTINGS UI GOES HERE
            // For example:
            //
            // Text("User: ${sharedData.username}")
            // SwitchPreference("Enable Notifications", checked = true, onCheckedChange = {})
            // ClickablePreference("Change Password", onClick = { /* navigate to change password screen */ })
            //
            Text("This is the Settings Screen.")

        }
    }
}

// You can create helper composables for your settings items like this:

@Composable
fun SwitchPreference(title: String, checked: Boolean, onCheckedChange: (Boolean) -> Unit) {
    ListItem(
        headlineContent = { Text(title) },
        trailingContent = {
            Switch(checked = checked, onCheckedChange = onCheckedChange)
        }
    )
}

@Composable
fun ClickablePreference(title: String, onClick: () -> Unit) {
    ListItem(
        headlineContent = { Text(title) },
        modifier = androidx.compose.ui.Modifier.clickable(onClick = onClick)
    )
}
