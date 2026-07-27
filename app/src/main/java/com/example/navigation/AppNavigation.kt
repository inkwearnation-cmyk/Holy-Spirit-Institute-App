package com.example.navigation

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.ui.screens.*
import com.example.ui.viewmodel.SchoolViewModel

/**
 * Navigation routes definitions for the application.
 */
sealed class Screen(val route: String) {
    object Login : Screen("login")
    object AdminDashboard : Screen("admin_dashboard")
    object TeacherDashboard : Screen("teacher_dashboard")
    object StudentDashboard : Screen("student_dashboard")
    object ParentDashboard : Screen("parent_dashboard")
    object Chat : Screen("chat")
}

/**
 * Main NavHost navigation component managing role-based destination routing.
 */
@Composable
fun AppNavigation(
    viewModel: SchoolViewModel,
    modifier: Modifier = Modifier,
    navController: NavHostController = rememberNavController()
) {
    NavHost(
        navController = navController,
        startDestination = Screen.Login.route,
        modifier = modifier
    ) {
        // Portal Authentication Screen
        composable(Screen.Login.route) {
            LoginScreen(
                viewModel = viewModel,
                onLoginSuccess = { role ->
                    val targetRoute = when (role) {
                        "Admin" -> Screen.AdminDashboard.route
                        "Teacher" -> Screen.TeacherDashboard.route
                        "Student" -> Screen.StudentDashboard.route
                        "Parent" -> Screen.ParentDashboard.route
                        else -> Screen.Login.route
                    }
                    if (targetRoute != Screen.Login.route) {
                        navController.navigate(targetRoute) {
                            popUpTo(Screen.Login.route) { inclusive = true }
                        }
                    }
                }
            )
        }

        // Administrator Dashboard Route
        composable(Screen.AdminDashboard.route) {
            AdminDashboard(
                viewModel = viewModel,
                onNavigateToChat = { navController.navigate(Screen.Chat.route) },
                onLogout = {
                    viewModel.logout()
                    navController.navigate(Screen.Login.route) {
                        popUpTo(Screen.AdminDashboard.route) { inclusive = true }
                    }
                }
            )
        }

        // Teacher Dashboard Route
        composable(Screen.TeacherDashboard.route) {
            TeacherDashboard(
                viewModel = viewModel,
                onNavigateToChat = { navController.navigate(Screen.Chat.route) },
                onLogout = {
                    viewModel.logout()
                    navController.navigate(Screen.Login.route) {
                        popUpTo(Screen.TeacherDashboard.route) { inclusive = true }
                    }
                }
            )
        }

        // Student Dashboard Route
        composable(Screen.StudentDashboard.route) {
            StudentDashboard(
                viewModel = viewModel,
                onLogout = {
                    viewModel.logout()
                    navController.navigate(Screen.Login.route) {
                        popUpTo(Screen.StudentDashboard.route) { inclusive = true }
                    }
                }
            )
        }

        // Parent Dashboard Route
        composable(Screen.ParentDashboard.route) {
            ParentDashboard(
                viewModel = viewModel,
                onLogout = {
                    viewModel.logout()
                    navController.navigate(Screen.Login.route) {
                        popUpTo(Screen.ParentDashboard.route) { inclusive = true }
                    }
                }
            )
        }

        // Faculty & Staff Chatroom Route
        composable(Screen.Chat.route) {
            ChatScreen(
                viewModel = viewModel,
                onNavigateBack = { navController.navigateUp() }
            )
        }
    }
}
