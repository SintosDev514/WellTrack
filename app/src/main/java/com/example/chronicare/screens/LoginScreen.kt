package com.example.chronicare.screens

import android.widget.Toast
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.chronicare.screens.AuthViewModel
 // Make sure this import is correct

@Composable
fun LoginScreen(
    navController: NavController,
    authViewModel: AuthViewModel = viewModel()
) {
    // State for the text fields
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }

    // State for loading and error messages
    var isLoading by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    val context = LocalContext.current

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(text = "Welcome Back", style = MaterialTheme.typography.headlineMedium)

        Spacer(modifier = Modifier.height(32.dp))

        OutlinedTextField(
            value = email,
            onValueChange = { email = it },
            label = { Text("Email") },
            modifier = Modifier.fillMaxWidth(),
            isError = errorMessage != null
        )

        Spacer(modifier = Modifier.height(16.dp))

        OutlinedTextField(
            value = password,
            onValueChange = { password = it },
            label = { Text("Password") },
            modifier = Modifier.fillMaxWidth(),
            visualTransformation = PasswordVisualTransformation(),
            isError = errorMessage != null
        )

        // Display error message if login fails
        errorMessage?.let {
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = it,
                color = MaterialTheme.colorScheme.error,
                style = MaterialTheme.typography.bodySmall
            )
        }

        Spacer(modifier = Modifier.height(32.dp))

        Button(
            onClick = {
                if (email.isNotBlank() && password.isNotBlank()) {
                    isLoading = true
                    errorMessage = null // Clear previous errors
                    authViewModel.signIn(email, password,
                        onSuccess = {
                            isLoading = false
                            // Navigate to the home/main screen after successful login
                            navController.navigate("home") { // Assuming "home" is your main screen route
                                // Clear the entire back stack up to the graph's start destination
                                popUpTo(navController.graph.startDestinationId) {
                                    inclusive = true
                                }
                                // Avoid creating multiple copies of the home screen
                                launchSingleTop = true
                            }
                        },
                        onFailure = { error ->
                            isLoading = false
                            errorMessage = error // Show Firebase error to the user
                            // Optional: Show a toast for a more transient message
                            Toast.makeText(context, error, Toast.LENGTH_LONG).show()
                        }
                    )
                }
            },
            // Disable button if fields are empty or if loading
            enabled = email.isNotBlank() && password.isNotBlank() && !isLoading,
            modifier = Modifier.fillMaxWidth()
        ) {
            if (isLoading) {
                CircularProgressIndicator(
                    modifier = Modifier.size(24.dp),
                    color = MaterialTheme.colorScheme.onPrimary
                )
            } else {
                Text("Log In")
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        TextButton(onClick = { navController.navigate("signup") }) { // Assuming "signup" is your route
            Text("Don't have an account? Sign Up")
        }
    }
}
