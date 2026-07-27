package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.ui.Modifier
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.ui.screens.*
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
                val navController = rememberNavController()

                NavHost(
                    navController = navController,
                    startDestination = "login",
                    modifier = Modifier.fillMaxSize()
                ) {
                    // Portal Login Authentication Screen
                    composable("login") {
                        LoginScreen(
                            viewModel = schoolViewModel,
                            onLoginSuccess = { role ->
                                when (role) {
                                    "Admin" -> navController.navigate("admin_dashboard") {
                                        popUpTo("login") { inclusive = true }
                                    }
                                    "Teacher" -> navController.navigate("teacher_dashboard") {
                                        popUpTo("login") { inclusive = true }
                                    }
                                    "Student" -> navController.navigate("student_dashboard") {
                                        popUpTo("login") { inclusive = true }
                                    }
                                    "Parent" -> navController.navigate("parent_dashboard") {
                                        popUpTo("login") { inclusive = true }
                                    }
                                }
                            }
                        )
                    }

                    // Admin Dashboard Module
                    composable("admin_dashboard") {
                        AdminDashboard(
                            viewModel = schoolViewModel,
                            onNavigateToChat = { navController.navigate("chat") },
                            onLogout = {
                                schoolViewModel.logout()
                                navController.navigate("login") {
                                    popUpTo("admin_dashboard") { inclusive = true }
                                }
                            }
                        )
                    }

                    // Teacher Dashboard Module
                    composable("teacher_dashboard") {
                        TeacherDashboard(
                            viewModel = schoolViewModel,
                            onNavigateToChat = { navController.navigate("chat") },
                            onLogout = {
                                schoolViewModel.logout()
                                navController.navigate("login") {
                                    popUpTo("teacher_dashboard") { inclusive = true }
                                }
                            }
                        )
                    }

                    // Student Dashboard Module
                    composable("student_dashboard") {
                        StudentDashboard(
                            viewModel = schoolViewModel,
                            onLogout = {
                                schoolViewModel.logout()
                                navController.navigate("login") {
                                    popUpTo("student_dashboard") { inclusive = true }
                                }
                            }
                        )
                    }

                    // Parent Dashboard Module
                    composable("parent_dashboard") {
                        ParentDashboard(
                            viewModel = schoolViewModel,
                            onLogout = {
                                schoolViewModel.logout()
                                navController.navigate("login") {
                                    popUpTo("parent_dashboard") { inclusive = true }
                                }
                            }
                        )
                    }

                    // Faculty & Staff Chatroom Screen
                    composable("chat") {
                        ChatScreen(
                            viewModel = schoolViewModel,
                            onNavigateBack = { navController.navigateUp() }
                        )
                    }
                }
            }
        }
    }
}
