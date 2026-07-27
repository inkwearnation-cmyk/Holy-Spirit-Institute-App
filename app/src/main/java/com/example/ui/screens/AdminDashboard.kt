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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.entity.*
import kotlinx.coroutines.launch
import com.example.ui.components.GlassCard
import com.example.ui.components.GlassHeader
import com.example.ui.components.GoaAmbientBackground
import com.example.ui.components.SchoolPerformanceBarChart
import com.example.ui.components.StatItem
import com.example.ui.theme.*
import com.example.ui.viewmodel.SchoolViewModel
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminDashboard(
    viewModel: SchoolViewModel,
    onNavigateToChat: () -> Unit,
    onLogout: () -> Unit
) {
    val adminUser by viewModel.currentUser.collectAsState()
    val users by viewModel.allUsers.collectAsState()
    val teachers by viewModel.allTeachers.collectAsState()
    val students by viewModel.allStudents.collectAsState()
    val parents by viewModel.allParents.collectAsState()
    val classSections by viewModel.allClassSections.collectAsState()
    val subjects by viewModel.allSubjects.collectAsState()
    val assignments by viewModel.allTeacherAssignments.collectAsState()
    val notices by viewModel.allNotices.collectAsState()
    val eventsHolidays by viewModel.allEventsHolidays.collectAsState()
    val leaveRequests by viewModel.allLeaveRequests.collectAsState()
    val auditLogs by viewModel.allAuditLogs.collectAsState()
    val alerts by viewModel.notifications.collectAsState()

    // Administrative sub-modules
    // 0: Overview, 1: Teachers, 2: Students, 3: Academics, 4: Notifications, 5: Logs/Backups
    var currentSubModuleIndex by remember { mutableStateOf(0) }
    val modules = listOf("Overview", "Teachers", "Students", "Academics", "Notices", "Backups")

    // Modals & Dialog states
    var showAddTeacherDialog by remember { mutableStateOf(false) }
    var showAddStudentDialog by remember { mutableStateOf(false) }
    var showAddAssignmentDialog by remember { mutableStateOf(false) }
    var showAddEventDialog by remember { mutableStateOf(false) }
    var showNoticeDialog by remember { mutableStateOf(false) }

    // Backup notification snackbar
    var backupResult by remember { mutableStateOf<String?>(null) }
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
                            text = adminUser?.name ?: "Holy Spirit ERP",
                            style = MaterialTheme.typography.titleMedium,
                            color = if (isSystemInDarkTheme()) DarkTextPrimary else LightTextPrimary,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "System Administrator Workspace",
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
                    Icons.Default.Home,
                    Icons.Default.Person,
                    Icons.Default.AccountBox,
                    Icons.Default.List,
                    Icons.Default.Notifications,
                    Icons.Default.Build
                )

                modules.forEachIndexed { index, title ->
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
                        .testTag("admin_logout_btn")
                )
                Spacer(modifier = Modifier.height(16.dp))
            }
        }
    ) {
        GoaAmbientBackground {
            Column(modifier = Modifier.fillMaxSize()) {
                
                // App Header
                GlassHeader(
                    title = adminUser?.name ?: "Holy Spirit ERP",
                    subtitle = "System Administrator Workspace",
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

            // Sub Module Content Loader
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp)
            ) {
                when (currentSubModuleIndex) {
                    0 -> AdminOverviewModule(
                        studentsCount = students.size,
                        teachersCount = teachers.size,
                        parentsCount = parents.size,
                        classesCount = classSections.size,
                        recentLogs = auditLogs.take(5),
                        activeLeaves = leaveRequests.filter { it.status == "Pending" },
                        onApproveLeave = { id -> viewModel.updateLeaveRequestStatus(id, "Approved") },
                        onRejectLeave = { id -> viewModel.updateLeaveRequestStatus(id, "Rejected") },
                        onNavigateToChat = onNavigateToChat
                    )
                    1 -> AdminTeachersModule(
                        teachers = teachers,
                        users = users,
                        classes = classSections,
                        onAddTeacherClick = { showAddTeacherDialog = true }
                    )
                    2 -> AdminStudentsModule(
                        students = students,
                        users = users,
                        parents = parents,
                        classes = classSections,
                        onAddStudentClick = { showAddStudentDialog = true }
                    )
                    3 -> AdminAcademicsModule(
                        classes = classSections,
                        subjects = subjects,
                        assignments = assignments,
                        users = users,
                        eventsHolidays = eventsHolidays,
                        onAddAssignmentClick = { showAddAssignmentDialog = true },
                        onAddEventClick = { showAddEventDialog = true },
                        onPublishGradesClick = { viewModel.publishAllResults() }
                    )
                    4 -> AdminNoticesModule(
                        notices = notices,
                        classes = classSections,
                        onPublishNoticeClick = { showNoticeDialog = true }
                    )
                    5 -> AdminSystemModule(
                        auditLogs = auditLogs,
                        viewModel = viewModel,
                        backupResult = backupResult,
                        onTriggerBackup = {
                            viewModel.backupDatabase { path ->
                                backupResult = path
                            }
                        }
                    )
                }
            }
        }
    }
    }

    // Modal Sheet: Alerts / Local Notification Hub
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
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Instant Notification Center", fontWeight = FontWeight.Bold, fontSize = 18.sp, color = SchoolPrimary)
                    TextButton(onClick = { viewModel.clearNotifications() }) {
                        Text("Clear All")
                    }
                }
                Spacer(modifier = Modifier.height(16.dp))
                if (alerts.isEmpty()) {
                    Text("No instant push alerts received. Safe and silent.", color = LightTextSecondary, fontSize = 13.sp)
                } else {
                    LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        items(alerts) { alert ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(SchoolPrimary.copy(alpha = 0.08f))
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

    // Modal: Add Teacher Profile Form
    if (showAddTeacherDialog) {
        var tName by remember { mutableStateOf("") }
        var tEmail by remember { mutableStateOf("") }
        var tPhone by remember { mutableStateOf("") }
        var tAddress by remember { mutableStateOf("") }
        var tQual by remember { mutableStateOf("") }
        var tExp by remember { mutableStateOf("") }
        var tClassTeacherOf by remember { mutableStateOf("") }

        AlertDialog(
            onDismissRequest = { showAddTeacherDialog = false },
            title = { Text("Hire New Educator Profile", fontWeight = FontWeight.Bold, color = SchoolPrimary) },
            text = {
                Column(
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                    modifier = Modifier.verticalScroll(rememberScrollState())
                ) {
                    OutlinedTextField(value = tName, onValueChange = { tName = it }, label = { Text("Educator Full Name") }, singleLine = true)
                    OutlinedTextField(value = tEmail, onValueChange = { tEmail = it }, label = { Text("Email Address") }, singleLine = true)
                    OutlinedTextField(value = tPhone, onValueChange = { tPhone = it }, label = { Text("Mobile Number") }, singleLine = true)
                    OutlinedTextField(value = tAddress, onValueChange = { tAddress = it }, label = { Text("Residential Address") }, singleLine = true)
                    OutlinedTextField(value = tQual, onValueChange = { tQual = it }, label = { Text("Qualification (e.g. M.A., B.Ed)") }, singleLine = true)
                    OutlinedTextField(value = tExp, onValueChange = { tExp = it }, label = { Text("Teaching Experience (e.g. 5 Years)") }, singleLine = true)
                    OutlinedTextField(value = tClassTeacherOf, onValueChange = { tClassTeacherOf = it }, label = { Text("Class Teacher Assignment (e.g. 5A / blank)") }, singleLine = true)
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.addTeacher(
                            name = tName, email = tEmail, phone = tPhone, address = tAddress,
                            qualification = tQual, experience = tExp, joiningDate = "2026-07-19",
                            isClassTeacherOf = if (tClassTeacherOf.trim().isEmpty()) null else tClassTeacherOf
                        )
                        showAddTeacherDialog = false
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = SchoolPrimary)
                ) {
                    Text("Hire & Provision")
                }
            },
            dismissButton = {
                TextButton(onClick = { showAddTeacherDialog = false }) { Text("Cancel") }
            }
        )
    }

    // Modal: Add Student Enrollment Form
    if (showAddStudentDialog) {
        var sName by remember { mutableStateOf("") }
        var sEmail by remember { mutableStateOf("") }
        var sPhone by remember { mutableStateOf("") }
        var sAddress by remember { mutableStateOf("") }
        var sAdmisNo by remember { mutableStateOf("") }
        var sRollNo by remember { mutableStateOf("") }
        var sDob by remember { mutableStateOf("2015-01-01") }
        var sGender by remember { mutableStateOf("Male") }
        var sBlood by remember { mutableStateOf("O+") }
        var sClassId by remember { mutableStateOf("5A") }
        var sParentId by remember { mutableStateOf("PR-301") }

        AlertDialog(
            onDismissRequest = { showAddStudentDialog = false },
            title = { Text("Enroll New Student", fontWeight = FontWeight.Bold, color = SchoolPrimary) },
            text = {
                Column(
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                    modifier = Modifier.verticalScroll(rememberScrollState())
                ) {
                    OutlinedTextField(value = sName, onValueChange = { sName = it }, label = { Text("Student Full Name") }, singleLine = true)
                    OutlinedTextField(value = sEmail, onValueChange = { sEmail = it }, label = { Text("Student Email") }, singleLine = true)
                    OutlinedTextField(value = sAddress, onValueChange = { sAddress = it }, label = { Text("Goan Residential Address") }, singleLine = true)
                    OutlinedTextField(value = sAdmisNo, onValueChange = { sAdmisNo = it }, label = { Text("Admission Number (e.g. HS-2026-09)") }, singleLine = true)
                    OutlinedTextField(value = sRollNo, onValueChange = { sRollNo = it }, label = { Text("Roll Number") }, singleLine = true)
                    OutlinedTextField(value = sClassId, onValueChange = { sClassId = it }, label = { Text("Class Section (e.g. 5A)") }, singleLine = true)
                    OutlinedTextField(value = sParentId, onValueChange = { sParentId = it }, label = { Text("Parent ID Reference") }, singleLine = true)
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.addStudent(
                            name = sName, email = sEmail, phone = sPhone, address = sAddress,
                            admissionNo = sAdmisNo, rollNo = sRollNo.toIntOrNull() ?: 1,
                            dob = sDob, gender = sGender, bloodGroup = sBlood,
                            classId = sClassId, parentId = sParentId, emergencyPhone = sPhone
                        )
                        showAddStudentDialog = false
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = SchoolPrimary)
                ) {
                    Text("Register & Save")
                }
            },
            dismissButton = {
                TextButton(onClick = { showAddStudentDialog = false }) { Text("Cancel") }
            }
        )
    }

    // Modal: Assign Teacher Subject Form
    if (showAddAssignmentDialog) {
        var assignTeacherId by remember { mutableStateOf("TC-101") }
        var assignClassId by remember { mutableStateOf("5A") }
        var assignSubjectId by remember { mutableStateOf("ENG") }

        AlertDialog(
            onDismissRequest = { showAddAssignmentDialog = false },
            title = { Text("Assign Teacher Subject Mapping", fontWeight = FontWeight.Bold, color = SchoolPrimary) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    OutlinedTextField(value = assignTeacherId, onValueChange = { assignTeacherId = it }, label = { Text("Teacher ID (e.g. TC-101)") }, singleLine = true)
                    OutlinedTextField(value = assignClassId, onValueChange = { assignClassId = it }, label = { Text("Class Section (e.g. 5A)") }, singleLine = true)
                    OutlinedTextField(value = assignSubjectId, onValueChange = { assignSubjectId = it }, label = { Text("Subject ID (e.g. ENG)") }, singleLine = true)
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.assignSubjectTeacher(assignTeacherId, assignClassId, assignSubjectId)
                        showAddAssignmentDialog = false
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = SchoolPrimary)
                ) {
                    Text("Create Assignment")
                }
            },
            dismissButton = {
                TextButton(onClick = { showAddAssignmentDialog = false }) { Text("Cancel") }
            }
        )
    }

    // Modal: Academic Event/Holiday Form
    if (showAddEventDialog) {
        var evTitle by remember { mutableStateOf("") }
        var evDesc by remember { mutableStateOf("") }
        var evDate by remember { mutableStateOf("2026-08-15") }
        var evType by remember { mutableStateOf("EVENT") } // EVENT or HOLIDAY

        AlertDialog(
            onDismissRequest = { showAddEventDialog = false },
            title = { Text("Add Event/Holiday to Calendar", fontWeight = FontWeight.Bold, color = SchoolPrimary) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    OutlinedTextField(value = evTitle, onValueChange = { evTitle = it }, label = { Text("Calendar Title") }, singleLine = true)
                    OutlinedTextField(value = evDesc, onValueChange = { evDesc = it }, label = { Text("Description Details") }, singleLine = true)
                    OutlinedTextField(value = evDate, onValueChange = { evDate = it }, label = { Text("Date (YYYY-MM-DD)") }, singleLine = true)
                    
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            RadioButton(selected = evType == "EVENT", onClick = { evType = "EVENT" })
                            Text("School Event")
                        }
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            RadioButton(selected = evType == "HOLIDAY", onClick = { evType = "HOLIDAY" })
                            Text("Holiday")
                        }
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.createEventHoliday(evTitle, evDesc, evDate, evType)
                        showAddEventDialog = false
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = SchoolPrimary)
                ) {
                    Text("Add Entry")
                }
            },
            dismissButton = {
                TextButton(onClick = { showAddEventDialog = false }) { Text("Cancel") }
            }
        )
    }

    // Modal: Publish Notice Form
    if (showNoticeDialog) {
        var notTitle by remember { mutableStateOf("") }
        var notContent by remember { mutableStateOf("") }
        var notTarget by remember { mutableStateOf("Everyone") } // Everyone, Teachers, Students, Parents, SpecificClass
        var notTargetClass by remember { mutableStateOf("") }

        AlertDialog(
            onDismissRequest = { showNoticeDialog = false },
            title = { Text("Publish Circular Notice Board", fontWeight = FontWeight.Bold, color = SchoolPrimary) },
            text = {
                Column(
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                    modifier = Modifier.verticalScroll(rememberScrollState())
                ) {
                    OutlinedTextField(value = notTitle, onValueChange = { notTitle = it }, label = { Text("Notice Title") }, singleLine = true)
                    OutlinedTextField(
                        value = notContent,
                        onValueChange = { notContent = it },
                        label = { Text("Notice Message / Body") },
                        modifier = Modifier.height(100.dp)
                    )
                    
                    Text("Target Audience Segment", fontWeight = FontWeight.SemiBold, fontSize = 12.sp)
                    val targets = listOf("Everyone", "Teachers", "Students", "Parents", "SpecificClass")
                    targets.forEach { target ->
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { notTarget = target }
                                .padding(vertical = 4.dp)
                        ) {
                            RadioButton(selected = notTarget == target, onClick = { notTarget = target })
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(target, fontSize = 13.sp)
                        }
                    }
                    if (notTarget == "SpecificClass") {
                        OutlinedTextField(value = notTargetClass, onValueChange = { notTargetClass = it }, label = { Text("Target Class Section (e.g. 5A)") }, singleLine = true)
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.publishNotice(
                            title = notTitle,
                            content = notContent,
                            targetType = notTarget,
                            targetClassId = if (notTarget == "SpecificClass") notTargetClass else null
                        )
                        showNoticeDialog = false
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = SchoolPrimary)
                ) {
                    Text("Broadcast notice")
                }
            },
            dismissButton = {
                TextButton(onClick = { showNoticeDialog = false }) { Text("Cancel") }
            }
        )
    }
}

// ==========================================
// SUB MODULE: OVERVIEW SCREEN
// ==========================================
@Composable
fun AdminOverviewModule(
    studentsCount: Int,
    teachersCount: Int,
    parentsCount: Int,
    classesCount: Int,
    recentLogs: List<AuditLogEntity>,
    activeLeaves: List<LeaveRequestEntity>,
    onApproveLeave: (Int) -> Unit,
    onRejectLeave: (Int) -> Unit,
    onNavigateToChat: () -> Unit
) {
    val isDark = isSystemInDarkTheme()
    
    LazyColumn(
        verticalArrangement = Arrangement.spacedBy(16.dp),
        contentPadding = PaddingValues(bottom = 24.dp)
    ) {
        
        // 4 Grid Stats counters
        item {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    StatItem("Enrolled Students", studentsCount.toString(), Icons.Default.Groups, SchoolPrimary, modifier = Modifier.weight(1f))
                    StatItem("Faculty Members", teachersCount.toString(), Icons.Default.Badge, SchoolSecondary, modifier = Modifier.weight(1f))
                }
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    StatItem("Active Parents", parentsCount.toString(), Icons.Default.FamilyRestroom, SchoolSuccess, modifier = Modifier.weight(1f))
                    StatItem("Registered Classes", classesCount.toString(), Icons.Default.CorporateFare, SchoolWarning, modifier = Modifier.weight(1f))
                }
            }
        }

        // Beautiful Analytics Chart
        item {
            GlassCard(modifier = Modifier.fillMaxWidth()) {
                SchoolPerformanceBarChart(
                    data = listOf(
                        Pair("Class 5", 0.88f),
                        Pair("Class 6", 0.74f),
                        Pair("Class 7", 0.92f),
                        Pair("Class 8", 0.81f),
                        Pair("Class 9", 0.85f)
                    )
                )
            }
        }

        // Pending Leaves Approval Panel
        if (activeLeaves.isNotEmpty()) {
            item {
                GlassCard(modifier = Modifier.fillMaxWidth()) {
                    Text("Staff Leave Request Panel", fontWeight = FontWeight.Bold, fontSize = 15.sp, color = SchoolPrimary)
                    Spacer(modifier = Modifier.height(8.dp))
                    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        activeLeaves.forEach { leave ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(if (isDark) Color(0x11FFFFFF) else Color(0x05000000))
                                    .padding(8.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(text = "${leave.userId} (${leave.role})", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                                    Text(text = "Dates: ${leave.startDate} to ${leave.endDate}", fontSize = 11.sp)
                                    Text(text = "Reason: ${leave.reason}", fontSize = 11.sp, color = if (isDark) DarkTextSecondary else LightTextSecondary)
                                }
                                Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                                    IconButton(
                                        onClick = { onApproveLeave(leave.id) },
                                        modifier = Modifier
                                            .size(32.dp)
                                            .background(SchoolSuccess, RoundedCornerShape(6.dp))
                                    ) {
                                        Icon(Icons.Default.Check, contentDescription = "Approve", tint = Color.White, modifier = Modifier.size(16.dp))
                                    }
                                    IconButton(
                                        onClick = { onRejectLeave(leave.id) },
                                        modifier = Modifier
                                            .size(32.dp)
                                            .background(SchoolDanger, RoundedCornerShape(6.dp))
                                    ) {
                                        Icon(Icons.Default.Close, contentDescription = "Reject", tint = Color.White, modifier = Modifier.size(16.dp))
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        // Direct Navigation to Chat Action Banner
        item {
            GlassCard(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onNavigateToChat() }
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Chat, contentDescription = null, tint = SchoolPrimary, modifier = Modifier.size(24.dp))
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text("Teacher & Staff Chat", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                            Text("Open active channels with educators", fontSize = 11.sp, color = if (isDark) DarkTextSecondary else LightTextSecondary)
                        }
                    }
                    Icon(Icons.Default.ChevronRight, contentDescription = null, tint = SchoolPrimary)
                }
            }
        }

        // Recent Audit logs
        item {
            GlassCard(modifier = Modifier.fillMaxWidth()) {
                Text("Chronological Security Logs", fontWeight = FontWeight.Bold, fontSize = 15.sp, color = SchoolPrimary)
                Spacer(modifier = Modifier.height(10.dp))
                if (recentLogs.isEmpty()) {
                    Text("No activities logged yet.", fontSize = 12.sp)
                } else {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        recentLogs.forEach { log ->
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .border(1.dp, if (isDark) BorderDark else BorderLight, RoundedCornerShape(8.dp))
                                    .padding(8.dp)
                            ) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text(log.action, fontWeight = FontWeight.Bold, fontSize = 12.sp, color = SchoolSecondary)
                                    val date = Date(log.timestamp)
                                    val formatted = SimpleDateFormat("hh:mm a", Locale.getDefault()).format(date)
                                    Text(formatted, fontSize = 10.sp, color = if (isDark) DarkTextSecondary else LightTextSecondary)
                                }
                                Text("${log.userName}: ${log.details}", fontSize = 11.sp, modifier = Modifier.padding(top = 2.dp))
                            }
                        }
                    }
                }
            }
        }
    }
}

// ==========================================
// SUB MODULE: TEACHER MANAGEMENT
// ==========================================
@Composable
fun AdminTeachersModule(
    teachers: List<TeacherEntity>,
    users: List<UserEntity>,
    classes: List<ClassSectionEntity>,
    onAddTeacherClick: () -> Unit
) {
    val isDark = isSystemInDarkTheme()
    Column(modifier = Modifier.fillMaxSize()) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("Faculty Roster", fontWeight = FontWeight.Bold, fontSize = 16.sp, color = SchoolPrimary)
            Button(
                onClick = onAddTeacherClick,
                colors = ButtonDefaults.buttonColors(containerColor = SchoolPrimary),
                shape = RoundedCornerShape(8.dp),
                modifier = Modifier.testTag("add_teacher_btn")
            ) {
                Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(4.dp))
                Text("Add", fontSize = 12.sp)
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        if (teachers.isEmpty()) {
            Box(modifier = Modifier.weight(1f).fillMaxWidth(), contentAlignment = Alignment.Center) {
                Text("No faculty profiles registered. Use 'Add' to hire.")
            }
        } else {
            LazyColumn(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(teachers) { teacher ->
                    val user = users.firstOrNull { it.id == teacher.teacherId }
                    if (user != null) {
                        GlassCard(modifier = Modifier.fillMaxWidth()) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(
                                    modifier = Modifier
                                        .size(44.dp)
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(SchoolPrimary.copy(alpha = 0.15f)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(Icons.Default.School, contentDescription = null, tint = SchoolPrimary)
                                }
                                Spacer(modifier = Modifier.width(12.dp))
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(user.name, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                                    Text("${user.id} | Qualification: ${teacher.qualification}", fontSize = 11.sp)
                                    Text("Experience: ${teacher.experience} | Mob: ${user.phone}", fontSize = 11.sp, color = if (isDark) DarkTextSecondary else LightTextSecondary)
                                    if (teacher.isClassTeacherOfClassId != null) {
                                        Box(
                                            modifier = Modifier
                                                .padding(top = 4.dp)
                                                .background(SchoolSuccess.copy(alpha = 0.2f), RoundedCornerShape(4.dp))
                                                .padding(horizontal = 6.dp, vertical = 2.dp)
                                        ) {
                                            Text("Class Teacher: ${teacher.isClassTeacherOfClassId}", fontSize = 9.sp, fontWeight = FontWeight.Bold, color = SchoolSuccess)
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

// ==========================================
// SUB MODULE: STUDENT MANAGEMENT
// ==========================================
@Composable
fun AdminStudentsModule(
    students: List<StudentEntity>,
    users: List<UserEntity>,
    parents: List<ParentEntity>,
    classes: List<ClassSectionEntity>,
    onAddStudentClick: () -> Unit
) {
    val isDark = isSystemInDarkTheme()
    Column(modifier = Modifier.fillMaxSize()) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("Registered Student Profiles", fontWeight = FontWeight.Bold, fontSize = 16.sp, color = SchoolPrimary)
            Button(
                onClick = onAddStudentClick,
                colors = ButtonDefaults.buttonColors(containerColor = SchoolPrimary),
                shape = RoundedCornerShape(8.dp),
                modifier = Modifier.testTag("add_student_btn")
            ) {
                Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(4.dp))
                Text("Enroll", fontSize = 12.sp)
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        if (students.isEmpty()) {
            Box(modifier = Modifier.weight(1f).fillMaxWidth(), contentAlignment = Alignment.Center) {
                Text("No active student records found.")
            }
        } else {
            LazyColumn(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(students) { student ->
                    val user = users.firstOrNull { it.id == student.studentId }
                    if (user != null) {
                        GlassCard(modifier = Modifier.fillMaxWidth()) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(
                                    modifier = Modifier
                                        .size(44.dp)
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(SchoolSecondary.copy(alpha = 0.15f)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(Icons.Default.Face, contentDescription = null, tint = SchoolSecondary)
                                }
                                Spacer(modifier = Modifier.width(12.dp))
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(user.name, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                                    Text("ID: ${user.id} | Adm: ${student.admissionNumber} | Roll: ${student.rollNumber}", fontSize = 11.sp)
                                    Row(horizontalArrangement = Arrangement.spacedBy(10.dp), modifier = Modifier.padding(top = 2.dp)) {
                                        Text("Class: ${student.classId}", fontWeight = FontWeight.Bold, fontSize = 11.sp, color = SchoolPrimary)
                                        Text("Blood Group: ${student.bloodGroup}", fontSize = 11.sp)
                                        Text("Gender: ${student.gender}", fontSize = 11.sp)
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

// ==========================================
// SUB MODULE: ACADEMICS & ASSIGNMENTS
// ==========================================
@Composable
fun AdminAcademicsModule(
    classes: List<ClassSectionEntity>,
    subjects: List<SubjectEntity>,
    assignments: List<TeacherAssignmentEntity>,
    users: List<UserEntity>,
    eventsHolidays: List<EventHolidayEntity>,
    onAddAssignmentClick: () -> Unit,
    onAddEventClick: () -> Unit,
    onPublishGradesClick: () -> Unit
) {
    val isDark = isSystemInDarkTheme()
    LazyColumn(
        verticalArrangement = Arrangement.spacedBy(16.dp),
        contentPadding = PaddingValues(bottom = 24.dp)
    ) {
        
        // Quick Action Row
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Button(
                    onClick = onAddAssignmentClick,
                    colors = ButtonDefaults.buttonColors(containerColor = SchoolPrimary),
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Assign Teacher", fontSize = 11.sp)
                }

                Button(
                    onClick = onAddEventClick,
                    colors = ButtonDefaults.buttonColors(containerColor = SchoolSecondary),
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Icon(Icons.Default.CalendarToday, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Add Event/Holiday", fontSize = 11.sp)
                }
            }
        }

        // Publish Results command action
        item {
            GlassCard(modifier = Modifier.fillMaxWidth()) {
                Text("Exam Administration & Publication", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                Text("Results uploaded by teachers are initially private. Publish them to make them instantly viewable inside Parent/Student portals.", fontSize = 11.sp, color = if (isDark) DarkTextSecondary else LightTextSecondary, modifier = Modifier.padding(vertical = 6.dp))
                Button(
                    onClick = onPublishGradesClick,
                    colors = ButtonDefaults.buttonColors(containerColor = SchoolSuccess),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(Icons.Default.Publish, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("PUBLISH ALL SEMESTER RESULTS")
                }
            }
        }

        // Subject Class Mapping Active Roster
        item {
            GlassCard(modifier = Modifier.fillMaxWidth()) {
                Text("Assigned Subjects & Faculty mapping", fontWeight = FontWeight.Bold, fontSize = 15.sp, color = SchoolPrimary)
                Spacer(modifier = Modifier.height(10.dp))
                if (assignments.isEmpty()) {
                    Text("No subject-teacher associations registered.", fontSize = 12.sp)
                } else {
                    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        assignments.forEach { assign ->
                            val tUser = users.firstOrNull { it.id == assign.teacherId }
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(6.dp))
                                    .background(if (isDark) Color(0x11FFFFFF) else Color(0x05000000))
                                    .padding(8.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column {
                                    Text("${tUser?.name ?: assign.teacherId}", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                                    Text("Subject: ${assign.subjectId} | Class Section: ${assign.classId}", fontSize = 11.sp)
                                }
                                Box(
                                    modifier = Modifier
                                        .background(SchoolSecondary.copy(alpha = 0.15f), RoundedCornerShape(4.dp))
                                        .padding(horizontal = 6.dp, vertical = 2.dp)
                                ) {
                                    Text(assign.classId, fontSize = 10.sp, color = SchoolSecondary, fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                    }
                }
            }
        }

        // Active Academic Calendar List (Holidays & Events)
        item {
            GlassCard(modifier = Modifier.fillMaxWidth()) {
                Text("Academic Calendar & Holidays (2026-27)", fontWeight = FontWeight.Bold, fontSize = 15.sp, color = SchoolPrimary)
                Spacer(modifier = Modifier.height(10.dp))
                if (eventsHolidays.isEmpty()) {
                    Text("Calendar is clean.", fontSize = 12.sp)
                } else {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        eventsHolidays.forEach { item ->
                            val isHoliday = item.type == "HOLIDAY"
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .border(
                                        width = 1.dp,
                                        color = if (isHoliday) SchoolWarning.copy(alpha = 0.3f) else SchoolSecondary.copy(alpha = 0.3f),
                                        shape = RoundedCornerShape(8.dp)
                                    )
                                    .background(if (isHoliday) SchoolWarning.copy(alpha = 0.05f) else SchoolSecondary.copy(alpha = 0.05f))
                                    .padding(10.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(36.dp)
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(if (isHoliday) SchoolWarning.copy(alpha = 0.15f) else SchoolSecondary.copy(alpha = 0.15f)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = if (isHoliday) Icons.Default.BeachAccess else Icons.Default.Event,
                                        contentDescription = null,
                                        tint = if (isHoliday) SchoolWarning else SchoolSecondary
                                    )
                                }
                                Spacer(modifier = Modifier.width(12.dp))
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(item.title, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                                    Text(item.description, fontSize = 11.sp, color = if (isDark) DarkTextSecondary else LightTextSecondary)
                                    Text("Date: ${item.date}", fontSize = 10.sp, fontWeight = FontWeight.Medium, color = SchoolPrimary)
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

// ==========================================
// SUB MODULE: NOTICES BROADCAST
// ==========================================
@Composable
fun AdminNoticesModule(
    notices: List<NoticeEntity>,
    classes: List<ClassSectionEntity>,
    onPublishNoticeClick: () -> Unit
) {
    val isDark = isSystemInDarkTheme()
    Column(modifier = Modifier.fillMaxSize()) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("Circular Notice Board", fontWeight = FontWeight.Bold, fontSize = 16.sp, color = SchoolPrimary)
            Button(
                onClick = onPublishNoticeClick,
                colors = ButtonDefaults.buttonColors(containerColor = SchoolPrimary),
                shape = RoundedCornerShape(8.dp),
                modifier = Modifier.testTag("publish_notice_btn")
            ) {
                Icon(Icons.Default.Publish, contentDescription = null, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(4.dp))
                Text("Broadcast", fontSize = 12.sp)
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        if (notices.isEmpty()) {
            Box(modifier = Modifier.weight(1f).fillMaxWidth(), contentAlignment = Alignment.Center) {
                Text("Circular Board is blank.")
            }
        } else {
            LazyColumn(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(notices) { notice ->
                    GlassCard(modifier = Modifier.fillMaxWidth()) {
                        Column {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(notice.title, fontWeight = FontWeight.Bold, fontSize = 14.sp, color = SchoolPrimary)
                                Box(
                                    modifier = Modifier
                                        .background(SchoolSecondary.copy(alpha = 0.15f), RoundedCornerShape(4.dp))
                                        .padding(horizontal = 6.dp, vertical = 2.dp)
                                ) {
                                    Text(notice.targetType, fontSize = 9.sp, fontWeight = FontWeight.Bold, color = SchoolSecondary)
                                }
                            }
                            Text("By: ${notice.senderName} | Date: ${notice.date}", fontSize = 10.sp, color = if (isDark) DarkTextSecondary else LightTextSecondary, modifier = Modifier.padding(top = 2.dp, bottom = 6.dp))
                            Text(notice.content, fontSize = 12.sp)
                        }
                    }
                }
            }
        }
    }
}

// ==========================================
// SUB MODULE: SYSTEM CONFIG & BACKUPS
// ==========================================
@Composable
fun AdminSystemModule(
    auditLogs: List<AuditLogEntity>,
    viewModel: SchoolViewModel,
    backupResult: String?,
    onTriggerBackup: () -> Unit
) {
    val scrollState = rememberScrollState()
    val isDark = isSystemInDarkTheme()

    // Editable settings fields
    var academicYearText by remember { mutableStateOf(viewModel.academicYear.value) }
    var schoolTimingText by remember { mutableStateOf(viewModel.schoolTiming.value) }
    var schoolNameText by remember { mutableStateOf(viewModel.schoolInfoName.value) }
    var schoolAddressText by remember { mutableStateOf(viewModel.schoolInfoAddress.value) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(scrollState),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        
        // System Settings form
        GlassCard(modifier = Modifier.fillMaxWidth()) {
            Text("ERP Metadata Settings", fontWeight = FontWeight.Bold, fontSize = 15.sp, color = SchoolPrimary)
            Spacer(modifier = Modifier.height(12.dp))
            OutlinedTextField(value = schoolNameText, onValueChange = { schoolNameText = it }, label = { Text("School Name") }, singleLine = true)
            Spacer(modifier = Modifier.height(8.dp))
            OutlinedTextField(value = schoolAddressText, onValueChange = { schoolAddressText = it }, label = { Text("Goa Address & Contact") }, singleLine = true)
            Spacer(modifier = Modifier.height(8.dp))
            OutlinedTextField(value = academicYearText, onValueChange = { academicYearText = it }, label = { Text("Active Academic Year") }, singleLine = true)
            Spacer(modifier = Modifier.height(8.dp))
            OutlinedTextField(value = schoolTimingText, onValueChange = { schoolTimingText = it }, label = { Text("Default Timings") }, singleLine = true)
            Spacer(modifier = Modifier.height(12.dp))
            
            Button(
                onClick = { viewModel.updateSettings(academicYearText, schoolTimingText, schoolNameText, schoolAddressText) },
                colors = ButtonDefaults.buttonColors(containerColor = SchoolPrimary),
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Save Settings Configuration")
            }
        }

        // Database backups panel
        GlassCard(modifier = Modifier.fillMaxWidth()) {
            Text("Disaster Recovery & SQLite Backups", fontWeight = FontWeight.Bold, fontSize = 15.sp, color = SchoolPrimary)
            Text("Back up the entire Room schema locally. This generates a SQL statement dump log containing users, leaves, grades, and attendance tables.", fontSize = 11.sp, color = if (isDark) DarkTextSecondary else LightTextSecondary, modifier = Modifier.padding(vertical = 6.dp))
            
            Button(
                onClick = onTriggerBackup,
                colors = ButtonDefaults.buttonColors(containerColor = SchoolSecondary),
                modifier = Modifier.fillMaxWidth()
            ) {
                Icon(Icons.Default.Backup, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text("TRIGGER INSTANT HOT-BACKUP")
            }

            if (backupResult != null) {
                Box(
                    modifier = Modifier
                        .padding(top = 10.dp)
                        .background(SchoolSuccess.copy(alpha = 0.15f), RoundedCornerShape(8.dp))
                        .padding(10.dp)
                ) {
                    Text(backupResult, color = SchoolSuccess, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                }
            }
        }

        Spacer(modifier = Modifier.height(20.dp))
    }
}
