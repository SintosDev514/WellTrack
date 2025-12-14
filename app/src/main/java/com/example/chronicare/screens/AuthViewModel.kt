// Make sure this file is in the correct package, e.g., com.example.chronicare.screens
package com.example.chronicare.screens

import androidx.lifecycle.ViewModel
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase

class AuthViewModel : ViewModel() {

    private val auth: FirebaseAuth = Firebase.auth

    // --- SIGN IN FUNCTION ---
    // This function MUST match the call in your LoginScreen
    fun signIn(
        email: String,
        password: String,
        onSuccess: () -> Unit,
        onFailure: (String) -> Unit // This parameter is crucial
    ) {
        auth.signInWithEmailAndPassword(email, password)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    // Sign in success, trigger the onSuccess callback
                    onSuccess()
                } else {
                    // If sign in fails, pass the error message to the onFailure callback
                    val errorMessage = task.exception?.message ?: "An unknown error occurred."
                    onFailure(errorMessage)
                }
            }
    }

    // --- SIGN UP FUNCTION ---
    // This function should already be correct for your SignUpScreen
    fun signUp(
        email: String,
        password: String,
        onSuccess: () -> Unit
        // You could add an onFailure callback here too for consistency
    ) {
        auth.createUserWithEmailAndPassword(email, password)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    // Sign up success
                    onSuccess()
                } else {
                    // Handle errors if needed
                }
            }
    }
}
