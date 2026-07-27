package com.example.ui.screens

import androidx.compose.animation.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.entity.*
import com.example.ui.components.GlassCard
import com.example.ui.components.GlassHeader
import com.example.ui.components.GoaAmbientBackground
import com.example.ui.components.SchoolPerformanceBarChart
import com.example.ui.components.StatItem
import com.example.ui.theme.*
import com.example.ui.viewmodel.SchoolViewModel
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ParentDashboard(
    viewModel: SchoolViewModel,
    onLogout: () -> Unit
) {
    val parentUser by viewModel.currentUser.collectAsState()
    val parentDetails by viewModel.currentParent.collectAsState()
    val children by viewModel.getStudentsByParent(parentUser?.id ?: "PR-301").collectAsState(emptyList())
    val users by viewModel.allUsers.collectAsState()
    val alerts by viewModel.notifications.collectAsState()

    var selectedChildIndex by remember { mutableStateOf(0) }
    var showNotificationSheet by remember { mutableStateOf(false) }

    // 0: Child Overview, 1: Attendance, 2: Class Homework, 3: Exam Results & Remarks, 4: Class Timetable
    var currentSubModuleIndex by remember { mutableStateOf(0) }
    val tabNames = listOf("Overview", "Attendance", "Homework", "Report Cards", "Timetable")

    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val scope = rememberCoroutineScope()

    ModalNavigationDrawer(
        drawerState = drawerState,
        drawerContent = {
            ModalDrawerSheet(
                drawerContainerColor = Color.White,
                modifier = Modifier.width(310.dp)
            ) {
                // Header of Drawer with Avatar, Name, and Role
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(
                            Brush.verticalGradient(
                                listOf(SchoolPrimary.copy(alpha = 0.08f), Color.Transparent)
                            )
                        )
                        .padding(24.dp)
                ) {
                    Column {
                        Box(
                            modifier = Modifier
                                .size(64.dp)
                                .clip(RoundedCornerShape(16.dp))
                                .background(Brush.linearGradient(listOf(SchoolPrimary, SchoolSecondary))),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = parentUser?.name?.take(2)?.uppercase() ?: "PR",
                                color = Color.White,
                                fontWeight = FontWeight.Bold,
                                fontSize = 20.sp
                            )
                        }
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = parentUser?.name ?: "Parent Portal",
                            fontSize = 18.sp,
                            color = LightTextPrimary,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Observer Terminal | Multi-child Account",
                            fontSize = 12.sp,
                            color = LightTextSecondary,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }

                HorizontalDivider(
                    modifier = Modifier.padding(horizontal = 24.dp),
                    color = BorderLight
                )
                Spacer(modifier = Modifier.height(12.dp))

                // Navigation Items
                val icons = listOf(
                    Icons.Default.Home,
                    Icons.Default.HowToReg,
                    Icons.Default.Edit,
                    Icons.Default.Star,
                    Icons.Default.DateRange
                )

                tabNames.forEachIndexed { index, title ->
                    NavigationDrawerItem(
                        icon = { Icon(icons.getOrElse(index) { Icons.Default.Menu }, contentDescription = null, modifier = Modifier.size(20.dp)) },
                        label = { Text(title, fontWeight = FontWeight.SemiBold, fontSize = 14.sp) },
                        selected = currentSubModuleIndex == index,
                        onClick = {
                            currentSubModuleIndex = index
                            scope.launch { drawerState.close() }
                        },
                        colors = NavigationDrawerItemDefaults.colors(
                            selectedContainerColor = SchoolPrimary.copy(alpha = 0.08f),
                            selectedIconColor = SchoolPrimary,
                            selectedTextColor = SchoolPrimary,
                            unselectedIconColor = LightTextSecondary,
                            unselectedTextColor = LightTextSecondary
                        ),
                        modifier = Modifier
                            .padding(horizontal = 16.dp, vertical = 2.dp)
                            .testTag("drawer_item_$title")
                    )
                }

                Spacer(modifier = Modifier.weight(1f))

                HorizontalDivider(
                    modifier = Modifier.padding(horizontal = 24.dp),
                    color = BorderLight
                )
                Spacer(modifier = Modifier.height(12.dp))

                // Logout Button in Drawer
                NavigationDrawerItem(
                    icon = { Icon(Icons.Default.ExitToApp, contentDescription = "Logout", tint = SchoolDanger, modifier = Modifier.size(20.dp)) },
                    label = { Text("Logout", color = SchoolDanger, fontWeight = FontWeight.Bold, fontSize = 14.sp) },
                    selected = false,
                    onClick = {
                        scope.launch { drawerState.close() }
                        onLogout()
                    },
                    modifier = Modifier
                        .padding(horizontal = 16.dp)
                        .testTag("parent_logout_btn")
                )
                Spacer(modifier = Modifier.height(24.dp))
            }
        }
    ) {
        GoaAmbientBackground {
            Column(modifier = Modifier.fillMaxSize()) {
                
                // Header
                GlassHeader(
                    title = parentUser?.name ?: "Parent Portal",
                    subtitle = "Observer Terminal | Holy Spirit ERP",
                    onNotificationClick = { showNotificationSheet = true },
                    notificationCount = alerts.size,
                    navigationIcon = {
                        IconButton(
                            onClick = { scope.launch { drawerState.open() } },
                            modifier = Modifier
                                .size(40.dp)
                                .clip(RoundedCornerShape(12.dp))
                                .background(Color.White)
                                .border(1.dp, BorderLight, RoundedCornerShape(12.dp))
                                .testTag("hamburger_menu_btn")
                        ) {
                            Icon(Icons.Default.Menu, contentDescription = "Open Navigation Menu", tint = SchoolPrimary)
                        }
                    }
                )

                // Multi-child switcher band
                if (children.isNotEmpty()) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 6.dp)
                            .background(Color.White, RoundedCornerShape(14.dp))
                            .border(1.dp, BorderLight, RoundedCornerShape(14.dp))
                            .padding(10.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Text("WARD SELECTOR:", fontWeight = FontWeight.ExtraBold, fontSize = 11.sp, color = SchoolPrimary, letterSpacing = 0.5.sp)
                        children.forEachIndexed { idx, child ->
                            val childUser = users.firstOrNull { it.id == child.studentId }
                            val isSelected = selectedChildIndex == idx
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(if (isSelected) SchoolPrimary else LightBackground)
                                    .clickable { selectedChildIndex = idx }
                                    .padding(horizontal = 12.dp, vertical = 6.dp)
                            ) {
                                Text(
                                    text = childUser?.name?.split(" ")?.firstOrNull() ?: child.studentId,
                                    color = if (isSelected) Color.White else LightTextPrimary,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                }

                // Active child specific state flows
                if (children.isNotEmpty() && selectedChildIndex < children.size) {
                    val currentChild = children[selectedChildIndex]
                    val currentChildUser = users.firstOrNull { it.id == currentChild.studentId }

                    val childAttendance by viewModel.getAttendanceHistoryForUser(currentChild.studentId).collectAsState(emptyList())
                    val childHomeworks by viewModel.getHomeworkForClass(currentChild.classId).collectAsState(emptyList())
                    val childMarks by viewModel.getPublishedMarksForStudent(currentChild.studentId).collectAsState(emptyList())
                    val childTimetable by viewModel.getTimetableForClass(currentChild.classId).collectAsState(emptyList())

                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp)
                    ) {
                        when (currentSubModuleIndex) {
                            0 -> ParentOverviewModule(
                                childName = currentChildUser?.name ?: "Ethan D'Costa",
                                childDetails = currentChild,
                                parentDetails = parentDetails,
                                homeworksCount = childHomeworks.size,
                                recentRemarks = childMarks.filter { it.remarks != null }.map { it.remarks!! }
                            )
                            1 -> ParentAttendanceModule(attendanceList = childAttendance)
                            2 -> ParentHomeworkModule(homeworks = childHomeworks)
                            3 -> ParentGradesModule(marks = childMarks)
                            4 -> ParentTimetableModule(timetable = childTimetable)
                        }
                    }
                } else {
                    Box(modifier = Modifier.weight(1f).fillMaxWidth(), contentAlignment = Alignment.Center) {
                        Text("No registered children mapped to parent profile.", color = LightTextSecondary, fontSize = 13.sp)
                    }
                }
            }
        }
    }

    if (showNotificationSheet) {
        ModalBottomSheet(
            onDismissRequest = { showNotificationSheet = false },
            containerColor = Color.White
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Notifications Center", fontWeight = FontWeight.Bold, fontSize = 18.sp, color = SchoolPrimary)
                    IconButton(onClick = { showNotificationSheet = false }) {
                        Icon(Icons.Default.Close, contentDescription = "Close")
                    }
                }
                Spacer(modifier = Modifier.height(16.dp))
                if (alerts.isEmpty()) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 32.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(Icons.Default.NotificationsNone, contentDescription = null, tint = LightTextSecondary, modifier = Modifier.size(48.dp))
                            Spacer(modifier = Modifier.height(8.dp))
                            Text("All caught up! No new notifications.", color = LightTextSecondary, fontSize = 13.sp)
                        }
                    }
                } else {
                    LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        items(alerts) { alert ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .background(SchoolPrimary.copy(alpha = 0.05f), RoundedCornerShape(12.dp))
                                    .border(1.dp, SchoolPrimary.copy(alpha = 0.1f), RoundedCornerShape(12.dp))
                                    .padding(14.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(36.dp)
                                        .clip(CircleShape)
                                        .background(SchoolPrimary.copy(alpha = 0.1f)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(Icons.Default.NotificationsActive, contentDescription = null, tint = SchoolPrimary, modifier = Modifier.size(18.dp))
                                }
                                Spacer(modifier = Modifier.width(12.dp))
                                Text(alert, fontSize = 13.sp, color = LightTextPrimary, fontWeight = FontWeight.Medium)
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
// PARENT SUB MODULE: CHILD OVERVIEW
// ===============================================
@Composable
fun ParentOverviewModule(
    childName: String,
    childDetails: StudentEntity,
    parentDetails: ParentEntity?,
    homeworksCount: Int,
    recentRemarks: List<String>
) {
    LazyColumn(
        verticalArrangement = Arrangement.spacedBy(16.dp),
        contentPadding = PaddingValues(bottom = 32.dp)
    ) {
        // Dynamic stats info cards
        item {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                StatItem(
                    label = "Academic Standing",
                    value = "Excellent",
                    icon = Icons.Default.VerifiedUser,
                    iconColor = SchoolSuccess,
                    modifier = Modifier.weight(1f)
                )
                StatItem(
                    label = "Homework Queue",
                    value = "$homeworksCount Assigned",
                    icon = Icons.Default.MenuBook,
                    iconColor = SchoolSecondary,
                    modifier = Modifier.weight(1f)
                )
            }
        }

        // Child Ward general information bio card
        item {
            GlassCard(modifier = Modifier.fillMaxWidth()) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Badge, contentDescription = null, tint = SchoolPrimary, modifier = Modifier.size(20.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Ward Bio & Enrollment Record", fontWeight = FontWeight.Bold, fontSize = 15.sp, color = LightTextPrimary)
                }
                Spacer(modifier = Modifier.height(14.dp))
                HorizontalDivider(color = BorderLight)
                Spacer(modifier = Modifier.height(12.dp))
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text("Full Name", fontSize = 12.sp, color = LightTextSecondary, fontWeight = FontWeight.Medium)
                        Text(childName, fontWeight = FontWeight.Bold, fontSize = 13.sp, color = LightTextPrimary)
                    }
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text("Class Stream", fontSize = 12.sp, color = LightTextSecondary, fontWeight = FontWeight.Medium)
                        Text(childDetails.classId, fontWeight = FontWeight.Bold, fontSize = 13.sp, color = SchoolSecondary)
                    }
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text("Roll Number", fontSize = 12.sp, color = LightTextSecondary, fontWeight = FontWeight.Medium)
                        Text(childDetails.rollNumber.toString(), fontWeight = FontWeight.Bold, fontSize = 13.sp, color = LightTextPrimary)
                    }
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text("Admission ID Code", fontSize = 12.sp, color = LightTextSecondary, fontWeight = FontWeight.Medium)
                        Text(childDetails.admissionNumber, fontWeight = FontWeight.Bold, fontSize = 13.sp, color = LightTextPrimary)
                    }
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text("Emergency Phone", fontSize = 12.sp, color = LightTextSecondary, fontWeight = FontWeight.Medium)
                        Text(childDetails.emergencyContact, fontWeight = FontWeight.Bold, fontSize = 13.sp, color = LightTextPrimary)
                    }
                }
            }
        }

        // Live remarks observer list
        item {
            GlassCard(modifier = Modifier.fillMaxWidth()) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.RecordVoiceOver, contentDescription = null, tint = SchoolPrimary, modifier = Modifier.size(20.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Recent Faculty Remarks & Behavior", fontWeight = FontWeight.Bold, fontSize = 15.sp, color = LightTextPrimary)
                }
                Spacer(modifier = Modifier.height(14.dp))
                HorizontalDivider(color = BorderLight)
                Spacer(modifier = Modifier.height(12.dp))

                if (recentRemarks.isEmpty()) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(LightBackground, RoundedCornerShape(12.dp))
                            .padding(16.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text("No faculty feedback remarks logged.", fontSize = 13.sp, color = LightTextSecondary)
                    }
                } else {
                    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        recentRemarks.forEach { remark ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(LightBackground)
                                    .border(1.dp, BorderLight, RoundedCornerShape(12.dp))
                                    .padding(12.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(32.dp)
                                        .clip(CircleShape)
                                        .background(SchoolPrimary.copy(alpha = 0.08f)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(Icons.Default.Comment, contentDescription = null, tint = SchoolPrimary, modifier = Modifier.size(16.dp))
                                }
                                Spacer(modifier = Modifier.width(12.dp))
                                Text(remark, fontSize = 12.sp, color = LightTextPrimary, fontWeight = FontWeight.Medium, modifier = Modifier.weight(1f))
                            }
                        }
                    }
                }
            }
        }
    }
}

// ===============================================
// PARENT SUB MODULE: ATTENDANCE
// ===============================================
@Composable
fun ParentAttendanceModule(attendanceList: List<AttendanceEntity>) {
    Column(modifier = Modifier.fillMaxSize()) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.fillMaxWidth()
        ) {
            Icon(Icons.Default.HowToReg, contentDescription = null, tint = SchoolPrimary, modifier = Modifier.size(20.dp))
            Spacer(modifier = Modifier.width(8.dp))
            Text("Ward Daily Attendance History", fontWeight = FontWeight.Bold, fontSize = 16.sp, color = LightTextPrimary)
        }
        Spacer(modifier = Modifier.height(14.dp))

        if (attendanceList.isEmpty()) {
            Box(modifier = Modifier.weight(1f).fillMaxWidth(), contentAlignment = Alignment.Center) {
                Text("No attendance records indexed yet.", fontSize = 13.sp, color = LightTextSecondary)
            }
        } else {
            LazyColumn(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(10.dp),
                contentPadding = PaddingValues(bottom = 24.dp)
            ) {
                items(attendanceList) { item ->
                    val isPresent = item.status == "Present"
                    val isAbsent = item.status == "Absent"
                    
                    GlassCard(modifier = Modifier.fillMaxWidth()) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(
                                    modifier = Modifier
                                        .size(36.dp)
                                        .clip(CircleShape)
                                        .background(
                                            if (isPresent) SchoolSuccess.copy(alpha = 0.08f)
                                            else if (isAbsent) SchoolDanger.copy(alpha = 0.08f)
                                            else SchoolWarning.copy(alpha = 0.08f)
                                        ),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = if (isPresent) Icons.Default.CheckCircle else Icons.Default.Cancel,
                                        contentDescription = null,
                                        tint = if (isPresent) SchoolSuccess else if (isAbsent) SchoolDanger else SchoolWarning,
                                        modifier = Modifier.size(18.dp)
                                    )
                                }
                                Spacer(modifier = Modifier.width(12.dp))
                                Column {
                                    Text(item.date, fontWeight = FontWeight.Bold, fontSize = 14.sp, color = LightTextPrimary)
                                    if (item.reason != null) {
                                        Text("Reason: ${item.reason}", fontSize = 11.sp, color = LightTextSecondary)
                                    }
                                }
                            }
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(
                                        if (isPresent) SchoolSuccess.copy(alpha = 0.15f)
                                        else if (isAbsent) SchoolDanger.copy(alpha = 0.15f)
                                        else SchoolWarning.copy(alpha = 0.15f)
                                    )
                                    .padding(horizontal = 10.dp, vertical = 6.dp)
                            ) {
                                Text(
                                    text = item.status,
                                    color = if (isPresent) SchoolSuccess else if (isAbsent) SchoolDanger else SchoolWarning,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 11.sp
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
// PARENT SUB MODULE: HOMEWORK WATCH
// ===============================================
@Composable
fun ParentHomeworkModule(homeworks: List<HomeworkEntity>) {
    Column(modifier = Modifier.fillMaxSize()) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.fillMaxWidth()
        ) {
            Icon(Icons.Default.MenuBook, contentDescription = null, tint = SchoolPrimary, modifier = Modifier.size(20.dp))
            Spacer(modifier = Modifier.width(8.dp))
            Text("Homework Assignments Roster (Read-only)", fontWeight = FontWeight.Bold, fontSize = 16.sp, color = LightTextPrimary)
        }
        Spacer(modifier = Modifier.height(14.dp))

        if (homeworks.isEmpty()) {
            Box(modifier = Modifier.weight(1f).fillMaxWidth(), contentAlignment = Alignment.Center) {
                Text("No homework currently assigned.", color = LightTextSecondary, fontSize = 13.sp)
            }
        } else {
            LazyColumn(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(12.dp),
                contentPadding = PaddingValues(bottom = 24.dp)
            ) {
                items(homeworks) { item ->
                    GlassCard(modifier = Modifier.fillMaxWidth()) {
                        Column {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.Top
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(item.title, fontWeight = FontWeight.Bold, fontSize = 15.sp, color = LightTextPrimary)
                                    Text(
                                        text = "Subject: ${item.subjectId} | Assigned by: ${item.teacherName}",
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = SchoolPrimary,
                                        modifier = Modifier.padding(top = 2.dp)
                                    )
                                }
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(
                                            if (item.priority == "High") SchoolDanger.copy(alpha = 0.1f) else SchoolSecondary.copy(alpha = 0.1f)
                                        )
                                        .padding(horizontal = 8.dp, vertical = 4.dp)
                                ) {
                                    Text(
                                        text = item.priority,
                                        color = if (item.priority == "High") SchoolDanger else SchoolSecondary,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 10.sp
                                    )
                                }
                            }
                            Spacer(modifier = Modifier.height(10.dp))
                            HorizontalDivider(color = BorderLight)
                            Spacer(modifier = Modifier.height(10.dp))
                            Text(item.instructions, fontSize = 13.sp, color = LightTextPrimary, lineHeight = 18.sp)
                            Spacer(modifier = Modifier.height(12.dp))
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(Icons.Default.CalendarToday, contentDescription = null, tint = LightTextSecondary, modifier = Modifier.size(14.dp))
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("Due Date: ${item.dueDate}", fontSize = 11.sp, color = LightTextSecondary, fontWeight = FontWeight.Medium)
                                }
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(6.dp))
                                        .background(SchoolWarning.copy(alpha = 0.15f))
                                        .padding(horizontal = 8.dp, vertical = 4.dp)
                                ) {
                                    Text("Pending Tracker", color = SchoolWarning, fontSize = 10.sp, fontWeight = FontWeight.Bold)
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
// PARENT SUB MODULE: GRADES DISPLAY
// ===============================================
@Composable
fun ParentGradesModule(marks: List<MarkEntity>) {
    val scrollState = rememberScrollState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(scrollState),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Performance Bar Chart
        if (marks.isNotEmpty()) {
            GlassCard(modifier = Modifier.fillMaxWidth()) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.AutoGraph, contentDescription = null, tint = SchoolPrimary, modifier = Modifier.size(20.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Subject Performance Progress", fontWeight = FontWeight.Bold, fontSize = 14.sp, color = LightTextPrimary)
                }
                Spacer(modifier = Modifier.height(14.dp))
                val chartData = marks.map { Pair(it.subjectId, (it.marksObtained / it.maxMarks).toFloat()) }
                SchoolPerformanceBarChart(data = chartData)
            }
        }

        // Core score cards list
        GlassCard(modifier = Modifier.fillMaxWidth()) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.Badge, contentDescription = null, tint = SchoolPrimary, modifier = Modifier.size(20.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text("Certified Semester Score Sheets", fontWeight = FontWeight.Bold, fontSize = 14.sp, color = LightTextPrimary)
            }
            Spacer(modifier = Modifier.height(14.dp))
            HorizontalDivider(color = BorderLight)
            Spacer(modifier = Modifier.height(12.dp))

            if (marks.isEmpty()) {
                Text("Results are currently unpublished. Check back later.", fontSize = 13.sp, color = LightTextSecondary)
            } else {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    marks.forEach { mark ->
                        val percent = (mark.marksObtained / mark.maxMarks) * 100
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(12.dp))
                                .background(LightBackground)
                                .border(1.dp, BorderLight, RoundedCornerShape(12.dp))
                                .padding(14.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text(mark.subjectId, fontWeight = FontWeight.Bold, fontSize = 15.sp, color = LightTextPrimary)
                                Spacer(modifier = Modifier.height(2.dp))
                                Text("Remarks: ${mark.remarks ?: "Keep it up"}", fontSize = 12.sp, color = LightTextSecondary)
                            }
                            Column(horizontalAlignment = Alignment.End) {
                                Text("${mark.marksObtained.toInt()} / ${mark.maxMarks.toInt()}", fontWeight = FontWeight.Bold, fontSize = 15.sp, color = SchoolPrimary)
                                Spacer(modifier = Modifier.height(2.dp))
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(6.dp))
                                        .background(if (percent >= 80) SchoolSuccess.copy(alpha = 0.1f) else SchoolWarning.copy(alpha = 0.1f))
                                        .padding(horizontal = 6.dp, vertical = 2.dp)
                                ) {
                                    Text(
                                        text = "${percent.toInt()}%",
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = if (percent >= 80) SchoolSuccess else SchoolWarning
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
        Spacer(modifier = Modifier.height(30.dp))
    }
}

// ===============================================
// PARENT SUB MODULE: CLASS SCHEDULE
// ===============================================
@Composable
fun ParentTimetableModule(timetable: List<TimetableEntity>) {
    var currentDay by remember { mutableStateOf("Monday") }
    val days = listOf("Monday", "Tuesday", "Wednesday", "Thursday", "Friday")

    val activePeriodRows = timetable.filter { it.dayOfWeek == currentDay }.sortedBy { it.period }

    fun getSubjectColor(subj: String): Color {
        return when (subj.uppercase()) {
            "MATHS", "MATHEMATICS", "MATH" -> Color(0xFFEFF6FF)
            "ENGLISH", "ENG" -> Color(0xFFF0FDF4)
            "SCIENCE", "SCI" -> Color(0xFFFFF7ED)
            "HISTORY", "HIS" -> Color(0xFFFAF5FF)
            "GEOGRAPHY", "GEO" -> Color(0xFFECFEFF)
            else -> Color(0xFFF1F5F9)
        }
    }

    fun getSubjectTextColor(subj: String): Color {
        return when (subj.uppercase()) {
            "MATHS", "MATHEMATICS", "MATH" -> Color(0xFF2563EB)
            "ENGLISH", "ENG" -> Color(0xFF16A34A)
            "SCIENCE", "SCI" -> Color(0xFFD97706)
            "HISTORY", "HIS" -> Color(0xFF9333EA)
            "GEOGRAPHY", "GEO" -> Color(0xFF0891B2)
            else -> Color(0xFF475569)
        }
    }

    Column(modifier = Modifier.fillMaxSize()) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.fillMaxWidth()
        ) {
            Icon(Icons.Default.Schedule, contentDescription = null, tint = SchoolPrimary, modifier = Modifier.size(20.dp))
            Spacer(modifier = Modifier.width(8.dp))
            Text("Daily Class Lesson Schedule", fontWeight = FontWeight.Bold, fontSize = 16.sp, color = LightTextPrimary)
        }
        Spacer(modifier = Modifier.height(14.dp))

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            days.forEach { day ->
                val isSel = currentDay == day
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(10.dp))
                        .background(if (isSel) SchoolPrimary else Color.White)
                        .border(1.dp, if (isSel) SchoolPrimary else BorderLight, RoundedCornerShape(10.dp))
                        .clickable { currentDay = day }
                        .padding(vertical = 10.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(day.take(3), color = if (isSel) Color.White else LightTextSecondary, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                }
            }
        }

        if (activePeriodRows.isEmpty()) {
            Box(modifier = Modifier.weight(1f).fillMaxWidth(), contentAlignment = Alignment.Center) {
                Text("No classes scheduled for $currentDay.", color = LightTextSecondary, fontSize = 14.sp)
            }
        } else {
            LazyColumn(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(10.dp),
                contentPadding = PaddingValues(bottom = 24.dp)
            ) {
                items(activePeriodRows) { item ->
                    val bgCol = getSubjectColor(item.subjectId)
                    val textCol = getSubjectTextColor(item.subjectId)
                    
                    GlassCard(modifier = Modifier.fillMaxWidth()) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(
                                    modifier = Modifier
                                        .size(44.dp)
                                        .clip(RoundedCornerShape(12.dp))
                                        .background(bgCol),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = item.period.toString(),
                                        fontWeight = FontWeight.Bold,
                                        color = textCol,
                                        fontSize = 16.sp
                                    )
                                }
                                Spacer(modifier = Modifier.width(14.dp))
                                Column {
                                    Text(text = item.subjectId, fontWeight = FontWeight.Bold, fontSize = 15.sp, color = LightTextPrimary)
                                    Spacer(modifier = Modifier.height(2.dp))
                                    Text(text = "Instructor Ref: ${item.teacherId}", fontSize = 12.sp, color = LightTextSecondary)
                                }
                            }
                            Text(
                                text = "${item.startTime} - ${item.endTime}",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = SchoolPrimary
                            )
                        }
                    }
                }
            }
        }
    }
}
