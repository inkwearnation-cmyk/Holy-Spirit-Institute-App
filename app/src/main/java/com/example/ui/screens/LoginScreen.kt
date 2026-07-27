package com.example.ui.screens

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.components.GlassCard
import com.example.ui.components.GoaAmbientBackground
import com.example.ui.theme.*
import com.example.ui.viewmodel.SchoolViewModel

@Composable
fun LoginScreen(
    viewModel: SchoolViewModel,
    onLoginSuccess: (String) -> Unit
) {
    val scrollState = rememberScrollState()
    var email by remember { mutableStateOf("admin@school.com") } // Pre-fill with admin for convenient first run
    var password by remember { mutableStateOf("admin123") }
    var rememberMe by remember { mutableStateOf(true) }
    var showPassword by remember { mutableStateOf(false) }

    // Selected Role Tab (0: Admin, 1: Teacher, 2: Student, 3: Parent)
    var selectedRoleIndex by remember { mutableStateOf(0) }
    val roles = listOf("Admin", "Teacher", "Student", "Parent")
    val roleEmails = listOf("admin@school.com", "fernandes@school.com", "student@school.com", "parent@school.com")
    val rolePasswords = listOf("admin123", "teacher123", "student123", "parent123")

    val loginError by viewModel.loginError.collectAsState()
    var isLoading by remember { mutableStateOf(false) }

    // Dialogs
    var showForgotPasswordDialog by remember { mutableStateOf(false) }
    var forgotEmail by remember { mutableStateOf("") }
    var forgotResultMessage by remember { mutableStateOf<String?>(null) }

    // When the user changes roles, automatically pre-fill credentials for quick and easy testing!
    LaunchedEffect(selectedRoleIndex) {
        email = roleEmails[selectedRoleIndex]
        password = rolePasswords[selectedRoleIndex]
    }

    GoaAmbientBackground {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .navigationBarsPadding()
                .imePadding()
                .verticalScroll(scrollState)
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Spacer(modifier = Modifier.height(40.dp))

            // School Logo Icon & Typography Header
            Box(
                modifier = Modifier
                    .size(80.dp)
                    .clip(RoundedCornerShape(20.dp))
                    .background(
                        Brush.linearGradient(
                            listOf(SchoolPrimary, SchoolSecondary)
                        )
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.School,
                    contentDescription = "School Logo",
                    tint = Color.White,
                    modifier = Modifier.size(44.dp)
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = "HOLY SPIRIT ERP",
                fontSize = 24.sp,
                fontWeight = FontWeight.ExtraBold,
                color = if (isSystemInDarkTheme()) DarkTextPrimary else SchoolPrimary,
                letterSpacing = 2.sp,
                textAlign = TextAlign.Center
            )
            Text(
                text = "Margao, Goa - Estd 1964",
                fontSize = 12.sp,
                fontWeight = FontWeight.Medium,
                color = if (isSystemInDarkTheme()) DarkTextSecondary else LightTextSecondary,
                modifier = Modifier.padding(top = 4.dp),
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(32.dp))

            // Glassmorphic Login Panel
            GlassCard(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("login_card")
            ) {
                Text(
                    text = "Portal Authentication",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = if (isSystemInDarkTheme()) DarkTextPrimary else LightTextPrimary,
                    modifier = Modifier.padding(bottom = 16.dp)
                )

                // Role Segmented Pill Controls
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(10.dp))
                        .background(if (isSystemInDarkTheme()) Color(0x1AFFFFFF) else Color(0xFFE2E8F0))
                        .padding(4.dp),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    roles.forEachIndexed { index, role ->
                        val isSelected = selectedRoleIndex == index
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(8.dp))
                                .background(if (isSelected) SchoolPrimary else Color.Transparent)
                                .clickable { selectedRoleIndex = index }
                                .padding(vertical = 8.dp)
                                .testTag("role_tab_$role"),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = role,
                                color = if (isSelected) Color.White else (if (isSystemInDarkTheme()) DarkTextSecondary else LightTextSecondary),
                                fontSize = 12.sp,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))

                // Email field
                OutlinedTextField(
                    value = email,
                    onValueChange = { email = it },
                    label = { Text("School Email Address") },
                    leadingIcon = { Icon(Icons.Default.Email, contentDescription = null, tint = SchoolPrimary) },
                    singleLine = true,
                    shape = RoundedCornerShape(14.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = SchoolPrimary,
                        unfocusedBorderColor = if (isSystemInDarkTheme()) BorderDark else BorderLight,
                        focusedLabelColor = SchoolPrimary,
                        unfocusedLabelColor = if (isSystemInDarkTheme()) DarkTextSecondary else LightTextSecondary
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("email_input")
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Password field
                OutlinedTextField(
                    value = password,
                    onValueChange = { password = it },
                    label = { Text("Security Password") },
                    leadingIcon = { Icon(Icons.Default.Lock, contentDescription = null, tint = SchoolPrimary) },
                    trailingIcon = {
                        IconButton(onClick = { showPassword = !showPassword }) {
                            Icon(
                                imageVector = if (showPassword) Icons.Default.Visibility else Icons.Default.VisibilityOff,
                                contentDescription = "Toggle password visibility",
                                tint = if (isSystemInDarkTheme()) DarkTextSecondary else LightTextSecondary
                            )
                        }
                    },
                    visualTransformation = if (showPassword) VisualTransformation.None else PasswordVisualTransformation(),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                    singleLine = true,
                    shape = RoundedCornerShape(14.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = SchoolPrimary,
                        unfocusedBorderColor = if (isSystemInDarkTheme()) BorderDark else BorderLight,
                        focusedLabelColor = SchoolPrimary,
                        unfocusedLabelColor = if (isSystemInDarkTheme()) DarkTextSecondary else LightTextSecondary
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("password_input")
                )

                Spacer(modifier = Modifier.height(12.dp))

                // Remember Me & Forgot Password row
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Checkbox(
                            checked = rememberMe,
                            onCheckedChange = { rememberMe = it },
                            modifier = Modifier.testTag("remember_me_checkbox")
                        )
                        Text(
                            text = "Remember me",
                            fontSize = 12.sp,
                            color = if (isSystemInDarkTheme()) DarkTextSecondary else LightTextSecondary
                        )
                    }

                    Text(
                        text = "Forgot Password?",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = SchoolSecondary,
                        modifier = Modifier
                            .clickable {
                                forgotEmail = email
                                forgotResultMessage = null
                                showForgotPasswordDialog = true
                            }
                            .testTag("forgot_password_btn")
                    )
                }

                // Error Feedback
                if (loginError != null) {
                    Text(
                        text = loginError ?: "",
                        color = SchoolDanger,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 8.dp),
                        textAlign = TextAlign.Center
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Authenticate Button
                Button(
                    onClick = {
                        isLoading = true
                        viewModel.login(email, password, rememberMe) { success ->
                            isLoading = false
                            if (success) {
                                onLoginSuccess(roles[selectedRoleIndex])
                            }
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color.Transparent),
                    contentPadding = PaddingValues(),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .testTag("login_button")
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(
                                Brush.horizontalGradient(
                                    listOf(SchoolPrimary, SchoolSecondary)
                                )
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        if (isLoading) {
                            CircularProgressIndicator(color = Color.White, modifier = Modifier.size(24.dp))
                        } else {
                            Text(
                                text = "ACCESS DASHBOARD",
                                color = Color.White,
                                fontWeight = FontWeight.Bold,
                                letterSpacing = 1.sp
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(40.dp))
        }
    }

    // Forgot Password Dialog
    if (showForgotPasswordDialog) {
        AlertDialog(
            onDismissRequest = { showForgotPasswordDialog = false },
            title = {
                Text(
                    text = "Request Password Reset",
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp,
                    color = SchoolPrimary
                )
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text(
                        text = "Enter your registered school email address below to reset your security credentials.",
                        fontSize = 13.sp,
                        color = if (isSystemInDarkTheme()) DarkTextSecondary else LightTextSecondary
                    )

                    OutlinedTextField(
                        value = forgotEmail,
                        onValueChange = { forgotEmail = it },
                        label = { Text("School Email") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )

                    if (forgotResultMessage != null) {
                        Text(
                            text = forgotResultMessage ?: "",
                            color = if (forgotResultMessage!!.contains("successful", true)) SchoolSuccess else SchoolDanger,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(top = 4.dp)
                        )
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.resetPasswordSimulated(forgotEmail) { success, msg ->
                            forgotResultMessage = msg
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = SchoolPrimary)
                ) {
                    Text("Verify & Reset")
                }
            },
            dismissButton = {
                TextButton(onClick = { showForgotPasswordDialog = false }) {
                    Text("Close")
                }
            }
        )
    }
}
