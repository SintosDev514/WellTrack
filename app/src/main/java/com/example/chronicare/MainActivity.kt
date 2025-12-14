package com.example.chronicare

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import com.example.chronicare.AppNavGraph
import com.example.chronicare.ui.theme.ChroniCareTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            ChroniCareTheme {
                AppNavGraph()
            }
        }
    }
}
