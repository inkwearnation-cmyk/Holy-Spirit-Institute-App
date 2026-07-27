package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.ui.Modifier
import com.example.navigation.AppNavigation
import com.example.ui.theme.MyApplicationTheme
import com.example.ui.viewmodel.SchoolViewModel
import com.example.ui.viewmodel.SchoolViewModelFactory

class MainActivity : ComponentActivity() {

    // Initialize SchoolViewModel with Factory passing Application Context
    private val schoolViewModel: SchoolViewModel by viewModels {
        SchoolViewModelFactory(application)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            MyApplicationTheme {
                AppNavigation(
                    viewModel = schoolViewModel,
                    modifier = Modifier.fillMaxSize()
                )
            }
        }
    }
}

