package com.example.ui.screens

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.foundation.BorderStroke
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.text.style.TextAlign
import com.example.data.entity.*
import kotlinx.coroutines.launch
import com.example.ui.components.GlassCard
import com.example.ui.components.GlassHeader
import com.example.ui.components.GoaAmbientBackground
import com.example.ui.components.StatItem
import com.example.ui.theme.*
import com.example.ui.viewmodel.SchoolViewModel
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TeacherDashboard(
    viewModel: SchoolViewModel,
    onNavigateToChat: () -> Unit,
    onLogout: () -> Unit
) {
    val teacherUser by viewModel.currentUser.collectAsState()
    val teacherDetails by viewModel.currentTeacher.collectAsState()
    val assignments by viewModel.allTeacherAssignments.collectAsState()
    val students by viewModel.allStudents.collectAsState()
    val users by viewModel.allUsers.collectAsState()
    val notices by viewModel.allNotices.collectAsState()
    val alerts by viewModel.notifications.collectAsState()

    // Filter assignments for this specific teacher
    val myAssignments = assignments.filter { it.teacherId == teacherUser?.id }

    // 0: Self Attendance, 1: Student Register, 2: Homework, 3: Exam Grading, 4: Announcements
    var currentSubModuleIndex by remember { mutableStateOf(0) }
    val subModules = listOf("Self Attend", "Student Register", "Homework", "Exam Grading", "Notices")

    var showNotificationSheet by remember { mutableStateOf(false) }
    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val scope = rememberCoroutineScope()

    ModalNavigationDrawer(
        drawerState = drawerState,
        drawerContent = {
            ModalDrawerSheet(
                drawerContainerColor = if (isSystemInDarkTheme()) DarkSurface else LightSurface,
                modifier = Modifier.width(300.dp)
            ) {
                // Header of Drawer with Avatar, Name, and Role
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(
                            androidx.compose.ui.graphics.Brush.verticalGradient(
                                listOf(SchoolPrimary.copy(alpha = 0.15f), Color.Transparent)
                            )
                        )
                        .padding(24.dp)
                ) {
                    Column {
                        // Small avatar or icon
                        Box(
                            modifier = Modifier
                                .size(56.dp)
                                .clip(RoundedCornerShape(16.dp))
                                .background(SchoolPrimary),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Person,
                                contentDescription = null,
                                tint = Color.White,
                                modifier = Modifier.size(28.dp)
                            )
                        }
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            text = teacherUser?.name ?: "Educator Portal",
                            style = MaterialTheme.typography.titleMedium,
                            color = if (isSystemInDarkTheme()) DarkTextPrimary else LightTextPrimary,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "Holy Spirit Faculty Profile | ${teacherDetails?.qualification ?: "B.Ed"}",
                            style = MaterialTheme.typography.bodySmall,
                            color = if (isSystemInDarkTheme()) DarkTextSecondary else LightTextSecondary
                        )
                    }
                }

                HorizontalDivider(
                    modifier = Modifier.padding(horizontal = 16.dp),
                    color = if (isSystemInDarkTheme()) BorderDark else BorderLight
                )
                Spacer(modifier = Modifier.height(8.dp))

                // Navigation Items
                val icons = listOf(
                    Icons.Default.Check,
                    Icons.Default.List,
                    Icons.Default.Edit,
                    Icons.Default.Star,
                    Icons.Default.Notifications
                )

                subModules.forEachIndexed { index, title ->
                    NavigationDrawerItem(
                        icon = { Icon(icons.getOrElse(index) { Icons.Default.Menu }, contentDescription = null) },
                        label = { Text(title, fontWeight = FontWeight.Bold) },
                        selected = currentSubModuleIndex == index,
                        onClick = {
                            currentSubModuleIndex = index
                            scope.launch { drawerState.close() }
                        },
                        colors = NavigationDrawerItemDefaults.colors(
                            selectedContainerColor = SchoolPrimary.copy(alpha = 0.12f),
                            selectedIconColor = SchoolPrimary,
                            selectedTextColor = SchoolPrimary,
                            unselectedIconColor = if (isSystemInDarkTheme()) DarkTextSecondary else LightTextSecondary,
                            unselectedTextColor = if (isSystemInDarkTheme()) DarkTextSecondary else LightTextSecondary
                        ),
                        modifier = Modifier
                            .padding(NavigationDrawerItemDefaults.ItemPadding)
                            .testTag("drawer_item_$title")
                    )
                }

                Spacer(modifier = Modifier.weight(1f))

                HorizontalDivider(
                    modifier = Modifier.padding(horizontal = 16.dp),
                    color = if (isSystemInDarkTheme()) BorderDark else BorderLight
                )
                Spacer(modifier = Modifier.height(8.dp))

                // Logout Button in Drawer!
                NavigationDrawerItem(
                    icon = { Icon(Icons.Default.ExitToApp, contentDescription = "Logout", tint = SchoolDanger) },
                    label = { Text("Logout", color = SchoolDanger, fontWeight = FontWeight.Bold) },
                    selected = false,
                    onClick = {
                        scope.launch { drawerState.close() }
                        onLogout()
                    },
                    modifier = Modifier
                        .padding(NavigationDrawerItemDefaults.ItemPadding)
                        .testTag("teacher_logout_btn")
                )
                Spacer(modifier = Modifier.height(16.dp))
            }
        }
    ) {
        GoaAmbientBackground {
            Column(modifier = Modifier.fillMaxSize()) {
                
                // Header
                GlassHeader(
                    title = teacherUser?.name ?: "Educator Portal",
                    subtitle = "Holy Spirit Faculty Profile | ${teacherDetails?.qualification ?: "B.Ed"}",
                    onNotificationClick = { showNotificationSheet = true },
                    notificationCount = alerts.size,
                    navigationIcon = {
                        IconButton(
                            onClick = { scope.launch { drawerState.open() } },
                            modifier = Modifier.testTag("hamburger_menu_btn")
                        ) {
                            Icon(Icons.Default.Menu, contentDescription = "Open Navigation Menu", tint = SchoolPrimary)
                        }
                    }
                )

            // Chat Floating Action Card Shortcut
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 6.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .background(SchoolSecondary.copy(alpha = 0.12f))
                    .clickable { onNavigateToChat() }
                    .padding(12.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Chat, contentDescription = null, tint = SchoolSecondary, modifier = Modifier.size(20.dp))
                    Spacer(modifier = Modifier.width(10.dp))
                    Text("Staff Room Live Chat Room", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                }
                Icon(Icons.Default.ChevronRight, contentDescription = null, tint = SchoolSecondary, modifier = Modifier.size(16.dp))
            }

            // Sub-modules selector
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp)
            ) {
                when (currentSubModuleIndex) {
                    0 -> TeacherSelfAttendanceModule(viewModel = viewModel, teacherId = teacherUser?.id ?: "")
                    1 -> TeacherStudentRegisterModule(viewModel = viewModel, teacherId = teacherUser?.id ?: "", myAssignments = myAssignments, students = students, users = users)
                    2 -> TeacherHomeworkModule(viewModel = viewModel, teacherId = teacherUser?.id ?: "", myAssignments = myAssignments)
                    3 -> TeacherGradingModule(viewModel = viewModel, teacherId = teacherUser?.id ?: "", myAssignments = myAssignments, students = students, users = users)
                    4 -> TeacherNoticesModule(viewModel = viewModel, teacherId = teacherUser?.id ?: "", myAssignments = myAssignments, notices = notices)
                }
            }
        }
    }
    }

    if (showNotificationSheet) {
        ModalBottomSheet(
            onDismissRequest = { showNotificationSheet = false },
            containerColor = if (isSystemInDarkTheme()) DarkSurface else LightSurface
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp)
            ) {
                Text("Notifications Center", fontWeight = FontWeight.Bold, fontSize = 18.sp, color = SchoolPrimary)
                Spacer(modifier = Modifier.height(16.dp))
                if (alerts.isEmpty()) {
                    Text("No notifications received.", color = LightTextSecondary, fontSize = 13.sp)
                } else {
                    LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        items(alerts) { alert ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .background(SchoolPrimary.copy(alpha = 0.08f), RoundedCornerShape(8.dp))
                                    .padding(12.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(Icons.Default.NotificationsActive, contentDescription = null, tint = SchoolSecondary, modifier = Modifier.size(20.dp))
                                Spacer(modifier = Modifier.width(12.dp))
                                Text(alert, fontSize = 13.sp)
                            }
                        }
                    }
                }
                Spacer(modifier = Modifier.height(30.dp))
            }
        }
    }
}

// ===============================================
// SUB MODULE: GEOLOCATED SELF ATTENDANCE
// ===============================================
@Composable
fun TeacherSelfAttendanceModule(viewModel: SchoolViewModel, teacherId: String) {
    val scrollState = rememberScrollState()
    val scope = rememberCoroutineScope()

    val teacherUser by viewModel.currentUser.collectAsState()
    val teacherDetails by viewModel.currentTeacher.collectAsState()
    val attendanceHistory by viewModel.getAttendanceHistoryForUser(teacherId).collectAsState(initial = emptyList())

    // Live Ticking Clock state
    var liveTime by remember { mutableStateOf(Calendar.getInstance()) }
    LaunchedEffect(Unit) {
        while (true) {
            kotlinx.coroutines.delay(1000)
            liveTime = Calendar.getInstance()
        }
    }

    // Geofence & Location Simulation
    var simulatedAtCampus by remember { mutableStateOf(true) }
    var simulatedLateShift by remember { mutableStateOf(false) } // False = 07:45 AM, True = 08:15 AM
    var attendanceStatusResult by remember { mutableStateOf<String?>(null) }
    var lateReason by remember { mutableStateOf("") }
    var showDemoSettings by remember { mutableStateOf(false) }

    // Coordinates setup
    val currentLat = if (simulatedAtCampus) viewModel.schoolLat else 15.2950
    val currentLng = if (simulatedAtCampus) viewModel.schoolLng else 73.9850

    // Today's check-in status check
    val todayStr = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
    val todayAttendance = attendanceHistory.find { it.date == todayStr }
    val isAlreadyCheckedIn = todayAttendance != null

    val realHour = Calendar.getInstance().get(Calendar.HOUR_OF_DAY)
    val greeting = when {
        realHour < 12 -> "Good Morning"
        realHour < 17 -> "Good Afternoon"
        else -> "Good Evening"
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(scrollState)
            .padding(vertical = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // 1. WELCOME HERO SECTION WITH AVATAR (18dp corner radius)
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(18.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            border = BorderStroke(1.dp, BorderLight)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "$greeting,",
                        fontSize = 14.sp,
                        color = LightTextSecondary,
                        fontWeight = FontWeight.Medium
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = teacherUser?.name ?: "Educator",
                        fontSize = 22.sp,
                        fontWeight = FontWeight.Bold,
                        color = SchoolPrimary
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = teacherDetails?.isClassTeacherOfClassId?.let { "Class Teacher of $it" } ?: "Secondary School Educator",
                        fontSize = 12.sp,
                        color = SchoolSecondary,
                        fontWeight = FontWeight.SemiBold
                    )
                }

                // Teacher Avatar with initials
                Box(
                    modifier = Modifier
                        .size(56.dp)
                        .clip(RoundedCornerShape(18.dp))
                        .background(Brush.linearGradient(listOf(SchoolPrimary, SchoolSecondary))),
                    contentAlignment = Alignment.Center
                ) {
                    val initials = teacherUser?.name?.split(" ")?.mapNotNull { it.firstOrNull() }?.take(2)?.joinToString("") ?: "ED"
                    Text(
                        text = initials.uppercase(),
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp
                    )
                }
            }
        }

        // 2. AUTOMATIC TIME & DATE CARD
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(18.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            border = BorderStroke(1.dp, BorderLight)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text(
                        text = SimpleDateFormat("hh:mm:ss a", Locale.getDefault()).format(liveTime.time),
                        fontSize = 30.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = SchoolPrimary,
                        letterSpacing = (-0.5).sp
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = SimpleDateFormat("EEEE, d MMMM yyyy", Locale.getDefault()).format(liveTime.time),
                        fontSize = 12.sp,
                        color = LightTextSecondary,
                        fontWeight = FontWeight.Bold
                    )
                }

                Box(
                    modifier = Modifier
                        .size(44.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(SchoolPrimary.copy(alpha = 0.08f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Schedule,
                        contentDescription = "Clock",
                        tint = SchoolPrimary,
                        modifier = Modifier.size(22.dp)
                    )
                }
            }
        }

        // 3. GPS VERIFICATION LOCATION CARD
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(18.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            border = BorderStroke(1.dp, BorderLight)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(40.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(if (simulatedAtCampus) SchoolSuccess.copy(alpha = 0.1f) else SchoolDanger.copy(alpha = 0.1f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Place,
                            contentDescription = null,
                            tint = if (simulatedAtCampus) SchoolSuccess else SchoolDanger,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Holy Spirit Campus Area",
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp,
                            color = LightTextPrimary
                        )
                        Text(
                            text = if (simulatedAtCampus) "Verified inside Margao School boundary" else "Outside authorized school perimeter",
                            fontSize = 12.sp,
                            color = LightTextSecondary,
                            fontWeight = FontWeight.Medium
                        )
                    }

                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .background(
                                color = if (simulatedAtCampus) SchoolSuccess.copy(alpha = 0.15f) else SchoolDanger.copy(alpha = 0.15f)
                            )
                            .padding(horizontal = 10.dp, vertical = 6.dp)
                    ) {
                        Text(
                            text = if (simulatedAtCampus) "VERIFIED" else "AWAY",
                            color = if (simulatedAtCampus) SchoolSuccess else SchoolDanger,
                            fontWeight = FontWeight.ExtraBold,
                            fontSize = 10.sp
                        )
                    }
                }

                // If outside school campus, show red alert warning box
                if (!simulatedAtCampus) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(SchoolDanger.copy(alpha = 0.05f), RoundedCornerShape(12.dp))
                            .border(1.dp, SchoolDanger.copy(alpha = 0.2f), RoundedCornerShape(12.dp))
                            .padding(14.dp)
                    ) {
                        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                            Icon(Icons.Default.Warning, contentDescription = null, tint = SchoolDanger, modifier = Modifier.size(18.dp))
                            Column {
                                Text(
                                    text = "Go to School Campus",
                                    color = SchoolDanger,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 13.sp
                                )
                                Spacer(modifier = Modifier.height(2.dp))
                                Text(
                                    text = "Your location is out of range. Attendance reporting is restricted to school boundaries.",
                                    color = LightTextSecondary,
                                    fontSize = 11.sp,
                                    lineHeight = 16.sp
                                )
                            }
                        }
                    }
                }
            }
        }

        // 4. MAIN ACTION INTERFACE
        Crossfade(targetState = isAlreadyCheckedIn, label = "action_state") { checkedIn ->
            if (checkedIn) {
                // SUCCESS CONFIRMATION RECEIPT CARD
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(18.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    border = BorderStroke(1.dp, BorderLight)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(20.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(56.dp)
                                .clip(RoundedCornerShape(18.dp))
                                .background(SchoolSuccess.copy(alpha = 0.1f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.CheckCircle,
                                contentDescription = null,
                                tint = SchoolSuccess,
                                modifier = Modifier.size(32.dp)
                            )
                        }

                        Text(
                            text = "Attendance Marked Successfully",
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp,
                            color = LightTextPrimary
                        )

                        Text(
                            text = "Your secure campus check-in has been validated & synchronized with the administrative registry.",
                            fontSize = 12.sp,
                            color = LightTextSecondary,
                            textAlign = TextAlign.Center,
                            lineHeight = 18.sp,
                            modifier = Modifier.padding(horizontal = 12.dp)
                        )

                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(LightBackground, RoundedCornerShape(12.dp))
                                .border(1.dp, BorderLight, RoundedCornerShape(12.dp))
                                .padding(12.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text("Check-In Time", fontSize = 10.sp, color = LightTextSecondary, fontWeight = FontWeight.Bold)
                                Text(
                                    text = todayAttendance?.checkInTime ?: "07:54 AM",
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = SchoolPrimary
                                )
                            }
                            Column(horizontalAlignment = Alignment.End) {
                                Text("Status Record", fontSize = 10.sp, color = LightTextSecondary, fontWeight = FontWeight.Bold)
                                Text(
                                    text = todayAttendance?.status ?: "Present",
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if ((todayAttendance?.status ?: "Present").contains("Late")) SchoolWarning else SchoolSuccess
                                )
                            }
                        }

                        todayAttendance?.reason?.let { reasonText ->
                            if (reasonText.isNotEmpty() && reasonText != "Checked in on time.") {
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .background(SchoolWarning.copy(alpha = 0.08f), RoundedCornerShape(10.dp))
                                        .border(1.dp, SchoolWarning.copy(alpha = 0.2f), RoundedCornerShape(10.dp))
                                        .padding(10.dp)
                                ) {
                                    Text(
                                        text = "Reason submitted: $reasonText",
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Medium,
                                        color = SchoolWarning
                                    )
                                }
                            }
                        }
                    }
                }
            } else {
                // NOT MARKED YET
                if (simulatedAtCampus) {
                    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        // Late check-in reason field (Automatic past 8:00 AM)
                        if (simulatedLateShift) {
                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(18.dp),
                                colors = CardDefaults.cardColors(containerColor = Color.White),
                                border = BorderStroke(1.dp, BorderLight)
                            ) {
                                Column(
                                    modifier = Modifier.padding(16.dp),
                                    verticalArrangement = Arrangement.spacedBy(10.dp)
                                ) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.AccessTime,
                                            contentDescription = null,
                                            tint = SchoolWarning,
                                            modifier = Modifier.size(18.dp)
                                        )
                                        Text(
                                            text = "Late Check-In Reason Required",
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 13.sp,
                                            color = SchoolWarning
                                        )
                                    }
                                    Text(
                                        text = "The official school start cut-off is 8:00 AM. Please provide a reason to enable check-in.",
                                        fontSize = 11.sp,
                                        color = LightTextSecondary,
                                        lineHeight = 16.sp
                                    )

                                    OutlinedTextField(
                                        value = lateReason,
                                        onValueChange = { lateReason = it },
                                        placeholder = { Text("E.g. Traffic delays near Margao Circle", fontSize = 12.sp) },
                                        singleLine = true,
                                        shape = RoundedCornerShape(10.dp),
                                        colors = OutlinedTextFieldDefaults.colors(
                                            focusedBorderColor = SchoolWarning,
                                            unfocusedBorderColor = BorderLight
                                        ),
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .testTag("late_explanation_input")
                                    )
                                }
                            }
                        }

                        // Large beautiful Check-In Button
                        val buttonColor = if (simulatedLateShift) SchoolWarning else SchoolPrimary
                        Button(
                            onClick = {
                                viewModel.markTeacherAttendance(
                                    latitude = currentLat,
                                    longitude = currentLng,
                                    reason = if (simulatedLateShift) {
                                        if (lateReason.trim().isEmpty()) "Not Specified" else lateReason
                                    } else "Checked in on time."
                                ) { _, msg ->
                                    attendanceStatusResult = msg
                                }
                            },
                            shape = RoundedCornerShape(18.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = buttonColor),
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(54.dp)
                                .testTag("teacher_checkin_btn")
                        ) {
                            Icon(Icons.Default.CheckCircle, contentDescription = null)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = if (simulatedLateShift) "VERIFY & SUBMIT LATE CHECK-IN" else "SUBMIT CAMPUS CHECK-IN",
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.sp
                            )
                        }
                    }
                }
            }
        }

        // 5. ATTENDANCE HISTORIC ARCHIVE LOGS (18dp Card)
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(18.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            border = BorderStroke(1.dp, BorderLight)
        ) {
            Column(
                modifier = Modifier.padding(20.dp)
            ) {
                Text(
                    text = "Weekly Attendance Log Archive",
                    fontWeight = FontWeight.Bold,
                    fontSize = 15.sp,
                    color = LightTextPrimary
                )
                Spacer(modifier = Modifier.height(16.dp))

                val staticLogs = listOf(
                    Pair("2026-07-18", Pair("Present", "07:52 AM")),
                    Pair("2026-07-17", Pair("Present", "07:49 AM")),
                    Pair("2026-07-16", Pair("Late", "08:14 AM")),
                    Pair("2026-07-15", Pair("Present", "07:55 AM"))
                )

                val allLogs = (attendanceHistory.filter { it.date != todayStr }.map {
                    it.date to Pair(it.status, it.checkInTime ?: "07:45 AM")
                } + staticLogs).sortedByDescending { it.first }.take(5)

                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    allLogs.forEach { log ->
                        val dateVal = log.first
                        val statusVal = log.second.first
                        val timeVal = log.second.second

                        val isLate = statusVal.contains("Late", true)
                        val isAbsent = statusVal.contains("Absent", true)

                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(LightBackground, RoundedCornerShape(12.dp))
                                .border(1.dp, BorderLight, RoundedCornerShape(12.dp))
                                .padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(
                                    modifier = Modifier
                                        .size(36.dp)
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(
                                            when {
                                                isAbsent -> SchoolDanger.copy(alpha = 0.08f)
                                                isLate -> SchoolWarning.copy(alpha = 0.08f)
                                                else -> SchoolSuccess.copy(alpha = 0.08f)
                                            }
                                        ),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = when {
                                            isAbsent -> Icons.Default.Close
                                            isLate -> Icons.Default.AccessTime
                                            else -> Icons.Default.Check
                                        },
                                        contentDescription = null,
                                        tint = when {
                                            isAbsent -> SchoolDanger
                                            isLate -> SchoolWarning
                                            else -> SchoolSuccess
                                        },
                                        modifier = Modifier.size(16.dp)
                                    )
                                }
                                Spacer(modifier = Modifier.width(12.dp))
                                Column {
                                    Text(text = dateVal, fontWeight = FontWeight.Bold, fontSize = 13.sp, color = LightTextPrimary)
                                    Text(text = "Checked in at $timeVal", fontSize = 11.sp, color = LightTextSecondary)
                                }
                            }

                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(6.dp))
                                    .background(
                                        when {
                                            isAbsent -> SchoolDanger.copy(alpha = 0.12f)
                                            isLate -> SchoolWarning.copy(alpha = 0.12f)
                                            else -> SchoolSuccess.copy(alpha = 0.12f)
                                        }
                                    )
                                    .padding(horizontal = 8.dp, vertical = 4.dp)
                            ) {
                                Text(
                                    text = statusVal.uppercase(),
                                    color = when {
                                        isAbsent -> SchoolDanger
                                        isLate -> SchoolWarning
                                        else -> SchoolSuccess
                                    },
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                }
            }
        }

        // 6. DEMO SIMULATION ASSISTANT CONTROLS (Tucked cleanly at the bottom)
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(18.dp),
            colors = CardDefaults.cardColors(containerColor = LightBackground),
            border = BorderStroke(1.dp, BorderLight)
        ) {
            Column(modifier = Modifier.padding(14.dp)) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { showDemoSettings = !showDemoSettings },
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Settings, contentDescription = null, tint = LightTextSecondary, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "DEMO SIMULATOR OPTIONS",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = LightTextSecondary,
                            letterSpacing = 0.5.sp
                        )
                    }
                    Icon(
                        imageVector = if (showDemoSettings) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                        contentDescription = null,
                        tint = LightTextSecondary
                    )
                }

                AnimatedVisibility(visible = showDemoSettings) {
                    Column(
                        modifier = Modifier.padding(top = 10.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        HorizontalDivider(color = BorderLight)
                        
                        Text(
                            text = "Change physical context parameters to test conditional check-in branches:",
                            fontSize = 11.sp,
                            color = LightTextSecondary
                        )

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Button(
                                onClick = { simulatedAtCampus = !simulatedAtCampus },
                                modifier = Modifier.weight(1f),
                                shape = RoundedCornerShape(10.dp),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = if (simulatedAtCampus) SchoolPrimary.copy(alpha = 0.1f) else SchoolDanger.copy(alpha = 0.1f)
                                )
                            ) {
                                Text(
                                    text = if (simulatedAtCampus) "🏫 IN GEOFENCE" else "🚗 OUT OF GEOFENCE",
                                    color = if (simulatedAtCampus) SchoolPrimary else SchoolDanger,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }

                            Button(
                                onClick = { simulatedLateShift = !simulatedLateShift },
                                modifier = Modifier.weight(1f),
                                shape = RoundedCornerShape(10.dp),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = if (!simulatedLateShift) SchoolPrimary.copy(alpha = 0.1f) else SchoolWarning.copy(alpha = 0.1f)
                                )
                            ) {
                                Text(
                                    text = if (!simulatedLateShift) "⏱ BEFORE 8:00 AM" else "⏱ AFTER 8:00 AM",
                                    color = if (!simulatedLateShift) SchoolPrimary else SchoolWarning,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

// ===============================================
// SUB MODULE: STUDENT ATTENDANCE REGISTER
// ===============================================
@Composable
fun TeacherStudentRegisterModule(
    viewModel: SchoolViewModel,
    teacherId: String,
    myAssignments: List<TeacherAssignmentEntity>,
    students: List<StudentEntity>,
    users: List<UserEntity>
) {
    val isDark = isSystemInDarkTheme()
    var selectedClassIndex by remember { mutableStateOf(0) }
    
    // Unique classes taught by this teacher
    val assignedClasses = myAssignments.map { it.classId }.distinct()

    if (assignedClasses.isEmpty()) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text("You are not currently assigned to teach any class sections.")
        }
        return
    }

    val selectedClass = assignedClasses[selectedClassIndex]
    val classStudents = students.filter { it.classId == selectedClass }

    Column(modifier = Modifier.fillMaxSize()) {
        Text("Daily Student Attendance Register", fontWeight = FontWeight.Bold, fontSize = 15.sp, color = SchoolPrimary)
        Spacer(modifier = Modifier.height(8.dp))

        // Class selector row
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 12.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            assignedClasses.forEachIndexed { index, classId ->
                val isSelected = selectedClassIndex == index
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .background(if (isSelected) SchoolPrimary else (if (isDark) Color(0x22FFFFFF) else Color(0xFFE2E8F0)))
                        .clickable { selectedClassIndex = index }
                        .padding(horizontal = 12.dp, vertical = 8.dp)
                ) {
                    Text(classId, color = if (isSelected) Color.White else (if (isDark) DarkTextPrimary else LightTextPrimary), fontWeight = FontWeight.Bold)
                }
            }
        }

        if (classStudents.isEmpty()) {
            Box(modifier = Modifier.weight(1f).fillMaxWidth(), contentAlignment = Alignment.Center) {
                Text("No students enrolled in Class Section $selectedClass")
            }
        } else {
            LazyColumn(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(classStudents) { student ->
                    val sUser = users.firstOrNull { it.id == student.studentId }
                    if (sUser != null) {
                        var localStatus by remember(student.studentId) { mutableStateOf("Present") }
                        
                        GlassCard(modifier = Modifier.fillMaxWidth()) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(sUser.name, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                                    Text("Roll Number: ${student.rollNumber} | ID: ${student.studentId}", fontSize = 11.sp, color = if (isDark) DarkTextSecondary else LightTextSecondary)
                                }

                                Row(
                                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    listOf("Present" to SchoolSuccess, "Absent" to SchoolDanger, "Leave" to SchoolWarning).forEach { statusItem ->
                                        val statusName = statusItem.first
                                        val statusColor = statusItem.second
                                        val isSelected = localStatus == statusName

                                        Box(
                                            modifier = Modifier
                                                .clip(RoundedCornerShape(6.dp))
                                                .background(if (isSelected) statusColor else statusColor.copy(alpha = 0.12f))
                                                .clickable {
                                                    localStatus = statusName
                                                    viewModel.markStudentAttendance(student.studentId, statusName)
                                                }
                                                .padding(horizontal = 8.dp, vertical = 6.dp)
                                                .testTag("mark_status_${student.studentId}_$statusName")
                                        ) {
                                            Text(
                                                text = statusName.take(3),
                                                color = if (isSelected) Color.White else statusColor,
                                                fontSize = 11.sp,
                                                fontWeight = FontWeight.Bold
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

// ===============================================
// SUB MODULE: HOMEWORK PLANNER
// ===============================================
@Composable
fun TeacherHomeworkModule(
    viewModel: SchoolViewModel,
    teacherId: String,
    myAssignments: List<TeacherAssignmentEntity>
) {
    val isDark = isSystemInDarkTheme()
    var homeworkTitle by remember { mutableStateOf("") }
    var homeworkInstructions by remember { mutableStateOf("") }
    
    // Select Class & Subject from assignments
    val assignedClasses = myAssignments.map { it.classId }.distinct()
    val assignedSubjects = myAssignments.map { it.subjectId }.distinct()

    if (assignedClasses.isEmpty() || assignedSubjects.isEmpty()) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text("No subject assignments available.")
        }
        return
    }

    var selectedClass by remember { mutableStateOf(assignedClasses.first()) }
    var selectedSubject by remember { mutableStateOf(assignedSubjects.first()) }
    var priority by remember { mutableStateOf("Medium") }
    var dueDate by remember { mutableStateOf("2026-07-28") }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        GlassCard(modifier = Modifier.fillMaxWidth()) {
            Text("Assign Homework Task", fontWeight = FontWeight.Bold, fontSize = 15.sp, color = SchoolPrimary)
            Spacer(modifier = Modifier.height(12.dp))

            // Class Selection
            Text("Class Section Taught", fontSize = 11.sp, fontWeight = FontWeight.Bold)
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.padding(vertical = 4.dp)) {
                assignedClasses.forEach { classId ->
                    val isSelected = selectedClass == classId
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(6.dp))
                            .background(if (isSelected) SchoolPrimary else (if (isDark) Color(0x22FFFFFF) else Color(0xFFE2E8F0)))
                            .clickable { selectedClass = classId }
                            .padding(horizontal = 10.dp, vertical = 6.dp)
                    ) {
                        Text(classId, color = if (isSelected) Color.White else (if (isDark) DarkTextPrimary else LightTextPrimary), fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }

            // Subject Selection
            Text("Subject Category", fontSize = 11.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(top = 8.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.padding(vertical = 4.dp)) {
                assignedSubjects.forEach { subId ->
                    val isSelected = selectedSubject == subId
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(6.dp))
                            .background(if (isSelected) SchoolSecondary else (if (isDark) Color(0x22FFFFFF) else Color(0xFFE2E8F0)))
                            .clickable { selectedSubject = subId }
                            .padding(horizontal = 10.dp, vertical = 6.dp)
                    ) {
                        Text(subId, color = if (isSelected) Color.White else (if (isDark) DarkTextPrimary else LightTextPrimary), fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            OutlinedTextField(
                value = homeworkTitle,
                onValueChange = { homeworkTitle = it },
                label = { Text("Homework Title") },
                singleLine = true,
                shape = RoundedCornerShape(14.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = SchoolPrimary,
                    unfocusedBorderColor = if (isSystemInDarkTheme()) BorderDark else BorderLight,
                    focusedLabelColor = SchoolPrimary,
                    unfocusedLabelColor = if (isSystemInDarkTheme()) DarkTextSecondary else LightTextSecondary
                ),
                modifier = Modifier.fillMaxWidth().testTag("hw_title")
            )
            Spacer(modifier = Modifier.height(8.dp))
            OutlinedTextField(
                value = homeworkInstructions,
                onValueChange = { homeworkInstructions = it },
                label = { Text("Activity instructions & guidelines") },
                shape = RoundedCornerShape(14.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = SchoolPrimary,
                    unfocusedBorderColor = if (isSystemInDarkTheme()) BorderDark else BorderLight,
                    focusedLabelColor = SchoolPrimary,
                    unfocusedLabelColor = if (isSystemInDarkTheme()) DarkTextSecondary else LightTextSecondary
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(100.dp)
                    .testTag("hw_instructions")
            )
            Spacer(modifier = Modifier.height(8.dp))

            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                OutlinedTextField(
                    value = dueDate,
                    onValueChange = { dueDate = it },
                    label = { Text("Due Date") },
                    singleLine = true,
                    shape = RoundedCornerShape(14.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = SchoolPrimary,
                        unfocusedBorderColor = if (isSystemInDarkTheme()) BorderDark else BorderLight,
                        focusedLabelColor = SchoolPrimary,
                        unfocusedLabelColor = if (isSystemInDarkTheme()) DarkTextSecondary else LightTextSecondary
                    ),
                    modifier = Modifier.weight(1f)
                )
                OutlinedTextField(
                    value = priority,
                    onValueChange = { priority = it },
                    label = { Text("Priority") },
                    singleLine = true,
                    shape = RoundedCornerShape(14.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = SchoolPrimary,
                        unfocusedBorderColor = if (isSystemInDarkTheme()) BorderDark else BorderLight,
                        focusedLabelColor = SchoolPrimary,
                        unfocusedLabelColor = if (isSystemInDarkTheme()) DarkTextSecondary else LightTextSecondary
                    ),
                    modifier = Modifier.weight(1f)
                )
            }

            Spacer(modifier = Modifier.height(14.dp))

            Button(
                onClick = {
                    viewModel.createHomework(selectedClass, selectedSubject, homeworkTitle, homeworkInstructions, dueDate, priority)
                    homeworkTitle = ""
                    homeworkInstructions = ""
                },
                colors = ButtonDefaults.buttonColors(containerColor = SchoolPrimary),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("submit_homework_btn")
            ) {
                Icon(Icons.Default.Upload, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text("ASSIGN TASK & TRIGGER ALERT")
            }
        }
    }
}

// ===============================================
// SUB MODULE: GRADING / EXAM MARKS ENTRY
// ===============================================
@Composable
fun TeacherGradingModule(
    viewModel: SchoolViewModel,
    teacherId: String,
    myAssignments: List<TeacherAssignmentEntity>,
    students: List<StudentEntity>,
    users: List<UserEntity>
) {
    val isDark = isSystemInDarkTheme()
    
    val assignedClasses = myAssignments.map { it.classId }.distinct()
    val assignedSubjects = myAssignments.map { it.subjectId }.distinct()

    if (assignedClasses.isEmpty() || assignedSubjects.isEmpty()) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text("No assigned classes for grading.")
        }
        return
    }

    var selectedClass by remember { mutableStateOf(assignedClasses.first()) }
    var selectedSubject by remember { mutableStateOf(assignedSubjects.first()) }
    var examType by remember { mutableStateOf("Mid Term") } // Unit Test, Mid Term, Final, Internal

    val classStudents = students.filter { it.classId == selectedClass }

    Column(modifier = Modifier.fillMaxSize()) {
        Text("Grade Entry Roster", fontWeight = FontWeight.Bold, fontSize = 15.sp, color = SchoolPrimary)
        Spacer(modifier = Modifier.height(8.dp))

        // Class filters Row
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.padding(bottom = 8.dp)) {
            assignedClasses.forEach { cId ->
                val isSel = selectedClass == cId
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(6.dp))
                        .background(if (isSel) SchoolPrimary else (if (isDark) Color(0x22FFFFFF) else Color(0xFFE2E8F0)))
                        .clickable { selectedClass = cId }
                        .padding(horizontal = 10.dp, vertical = 6.dp)
                ) {
                    Text(cId, color = if (isSel) Color.White else (if (isDark) DarkTextPrimary else LightTextPrimary), fontWeight = FontWeight.Bold, fontSize = 11.sp)
                }
            }
        }

        // Subject filters Row
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.padding(bottom = 12.dp)) {
            assignedSubjects.forEach { sId ->
                val isSel = selectedSubject == sId
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(6.dp))
                        .background(if (isSel) SchoolSecondary else (if (isDark) Color(0x22FFFFFF) else Color(0xFFE2E8F0)))
                        .clickable { selectedSubject = sId }
                        .padding(horizontal = 10.dp, vertical = 6.dp)
                ) {
                    Text(sId, color = if (isSel) Color.White else (if (isDark) DarkTextPrimary else LightTextPrimary), fontWeight = FontWeight.Bold, fontSize = 11.sp)
                }
            }
        }

        if (classStudents.isEmpty()) {
            Box(modifier = Modifier.weight(1f).fillMaxWidth(), contentAlignment = Alignment.Center) {
                Text("No students to grade.")
            }
        } else {
            LazyColumn(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(classStudents) { student ->
                    val sUser = users.firstOrNull { it.id == student.studentId }
                    if (sUser != null) {
                        var scoreText by remember(student.studentId) { mutableStateOf("") }
                        var maxScoreText by remember(student.studentId) { mutableStateOf("50") }
                        var remarks by remember(student.studentId) { mutableStateOf("") }
                        var isSaved by remember(student.studentId) { mutableStateOf(false) }

                        GlassCard(modifier = Modifier.fillMaxWidth()) {
                            Column {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column {
                                        Text(sUser.name, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                                        Text("Roll: ${student.rollNumber} | Exam: $examType", fontSize = 11.sp, color = if (isDark) DarkTextSecondary else LightTextSecondary)
                                    }
                                    if (isSaved) {
                                        Box(
                                            modifier = Modifier
                                                .background(SchoolSuccess.copy(alpha = 0.15f), RoundedCornerShape(4.dp))
                                                .padding(horizontal = 6.dp, vertical = 2.dp)
                                        ) {
                                            Text("Saved", color = SchoolSuccess, fontWeight = FontWeight.Bold, fontSize = 10.sp)
                                        }
                                    }
                                }
                                
                                Spacer(modifier = Modifier.height(10.dp))

                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    OutlinedTextField(
                                        value = scoreText,
                                        onValueChange = { scoreText = it },
                                        label = { Text("Obtained Marks") },
                                        singleLine = true,
                                        modifier = Modifier
                                            .weight(1f)
                                            .testTag("score_obtained_${student.studentId}")
                                    )
                                    OutlinedTextField(
                                        value = maxScoreText,
                                        onValueChange = { maxScoreText = it },
                                        label = { Text("Max Marks") },
                                        singleLine = true,
                                        modifier = Modifier.weight(1f)
                                    )
                                }

                                Spacer(modifier = Modifier.height(8.dp))

                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    OutlinedTextField(
                                        value = remarks,
                                        onValueChange = { remarks = it },
                                        label = { Text("Teacher remarks (Good, improve, etc)") },
                                        singleLine = true,
                                        modifier = Modifier.weight(1f)
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    IconButton(
                                        onClick = {
                                            val score = scoreText.toDoubleOrNull() ?: 0.0
                                            val max = maxScoreText.toDoubleOrNull() ?: 50.0
                                            viewModel.enterStudentMark(student.studentId, selectedSubject, examType, score, max, remarks)
                                            isSaved = true
                                        },
                                        modifier = Modifier
                                            .size(44.dp)
                                            .background(SchoolPrimary, RoundedCornerShape(8.dp))
                                    ) {
                                        Icon(Icons.Default.Save, contentDescription = "Save Grade", tint = Color.White)
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

// ===============================================
// SUB MODULE: NOTICES WRITER
// ===============================================
@Composable
fun TeacherNoticesModule(
    viewModel: SchoolViewModel,
    teacherId: String,
    myAssignments: List<TeacherAssignmentEntity>,
    notices: List<NoticeEntity>
) {
    val isDark = isSystemInDarkTheme()
    var title by remember { mutableStateOf("") }
    var content by remember { mutableStateOf("") }
    val assignedClasses = myAssignments.map { it.classId }.distinct()

    if (assignedClasses.isEmpty()) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text("No assigned classes.")
        }
        return
    }

    var targetClass by remember { mutableStateOf(assignedClasses.first()) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        GlassCard(modifier = Modifier.fillMaxWidth()) {
            Text("Publish Classroom Notice", fontWeight = FontWeight.Bold, fontSize = 15.sp, color = SchoolPrimary)
            Text("Teachers can broadcast announcements strictly restricted to their assigned class sections.", fontSize = 11.sp, color = if (isDark) DarkTextSecondary else LightTextSecondary, modifier = Modifier.padding(vertical = 4.dp))
            Spacer(modifier = Modifier.height(10.dp))

            Text("Class Target", fontSize = 11.sp, fontWeight = FontWeight.Bold)
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.padding(vertical = 4.dp)) {
                assignedClasses.forEach { cId ->
                    val isSel = targetClass == cId
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(6.dp))
                            .background(if (isSel) SchoolPrimary else (if (isDark) Color(0x22FFFFFF) else Color(0xFFE2E8F0)))
                            .clickable { targetClass = cId }
                            .padding(horizontal = 10.dp, vertical = 6.dp)
                    ) {
                        Text(cId, color = if (isSel) Color.White else (if (isDark) DarkTextPrimary else LightTextPrimary), fontWeight = FontWeight.Bold, fontSize = 11.sp)
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            OutlinedTextField(value = title, onValueChange = { title = it }, label = { Text("Announcement Title") }, singleLine = true, modifier = Modifier.fillMaxWidth().testTag("teacher_notice_title"))
            Spacer(modifier = Modifier.height(8.dp))
            OutlinedTextField(
                value = content,
                onValueChange = { content = it },
                label = { Text("Circular Announcement Message") },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(100.dp)
                    .testTag("teacher_notice_content")
            )

            Spacer(modifier = Modifier.height(12.dp))

            Button(
                onClick = {
                    viewModel.publishNotice(title, content, "SpecificClass", targetClass)
                    title = ""
                    content = ""
                },
                colors = ButtonDefaults.buttonColors(containerColor = SchoolPrimary),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("teacher_notice_publish_btn")
            ) {
                Icon(Icons.Default.Publish, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text("BROADCAST TO CLASSROOM")
            }
        }
    }
}
