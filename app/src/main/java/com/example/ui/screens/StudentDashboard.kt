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
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
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
fun StudentDashboard(
    viewModel: SchoolViewModel,
    onLogout: () -> Unit
) {
    val studentUser by viewModel.currentUser.collectAsState()
    val studentDetails by viewModel.currentStudent.collectAsState()
    val classId = studentDetails?.classId ?: "5A"
    val timetable by viewModel.getTimetableForClass(classId).collectAsState(emptyList())
    val homeworks by viewModel.getHomeworkForClass(classId).collectAsState(emptyList())
    val marks by viewModel.getPublishedMarksForStudent(studentUser?.id ?: "").collectAsState(emptyList())
    val notices by viewModel.getNoticesForUser("Student", classId).collectAsState(emptyList())
    val eventsHolidays by viewModel.allEventsHolidays.collectAsState()
    val alerts by viewModel.notifications.collectAsState()

    // 0: Feed, 1: ID Card, 2: Timetable, 3: Homework, 4: Grades / PDF, 5: Apply Leave
    var currentSubModuleIndex by remember { mutableStateOf(0) }
    val tabNames = listOf("Home", "ID Card", "Timetable", "Homework", "Report Card", "Leave Request")

    var showNotificationSheet by remember { mutableStateOf(false) }
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
                                text = studentUser?.name?.take(2)?.uppercase() ?: "ST",
                                color = Color.White,
                                fontWeight = FontWeight.Bold,
                                fontSize = 20.sp
                            )
                        }
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = studentUser?.name ?: "Student Portal",
                            fontSize = 18.sp,
                            color = LightTextPrimary,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Class $classId | Admission: ${studentDetails?.admissionNumber ?: "HS-001"}",
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
                    Icons.Default.AccountBox,
                    Icons.Default.DateRange,
                    Icons.Default.Edit,
                    Icons.Default.Star,
                    Icons.Default.Send
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
                        .testTag("student_logout_btn")
                )
                Spacer(modifier = Modifier.height(24.dp))
            }
        }
    ) {
        GoaAmbientBackground {
            Column(modifier = Modifier.fillMaxSize()) {
                
                // Header
                GlassHeader(
                    title = studentUser?.name ?: "Student Portal",
                    subtitle = "Class: $classId | Roll: ${studentDetails?.rollNumber ?: 12} | Holy Spirit",
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

                // Screen Content Pane
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp)
                ) {
                    when (currentSubModuleIndex) {
                        0 -> StudentFeedModule(notices = notices, events = eventsHolidays.take(4))
                        1 -> StudentIDCardModule(student = studentUser, studentDetails = studentDetails)
                        2 -> StudentTimetableModule(timetable = timetable)
                        3 -> StudentHomeworkModule(homeworks = homeworks)
                        4 -> StudentGradesModule(marks = marks)
                        5 -> StudentLeaveModule(viewModel = viewModel)
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
// STUDENT MODULE: FEED & NOTICES
// ===============================================
@Composable
fun StudentFeedModule(notices: List<NoticeEntity>, events: List<EventHolidayEntity>) {
    var searchQuery by remember { mutableStateOf("") }
    var selectedCategory by remember { mutableStateOf("All") }
    val categories = listOf("All", "Academic", "General", "Everyone")

    LazyColumn(
        verticalArrangement = Arrangement.spacedBy(16.dp),
        contentPadding = PaddingValues(bottom = 32.dp)
    ) {
        // Welcome and Quick Stats
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                StatItem(
                    label = "Attendance Index",
                    value = "96.4%",
                    icon = Icons.Default.HowToReg,
                    iconColor = SchoolSuccess,
                    modifier = Modifier.weight(1f)
                )
                StatItem(
                    label = "Term GPA",
                    value = "4.1 / 5",
                    icon = Icons.Default.Star,
                    iconColor = SchoolWarning,
                    modifier = Modifier.weight(1f)
                )
            }
        }

        // Search and Filters Bar
        item {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(18.dp))
                    .background(Color.White)
                    .border(1.dp, BorderLight, RoundedCornerShape(18.dp))
                    .padding(16.dp)
            ) {
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    placeholder = { Text("Search notices...", fontSize = 13.sp, color = LightTextSecondary) },
                    leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, tint = SchoolPrimary) },
                    singleLine = true,
                    shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = SchoolPrimary,
                        unfocusedBorderColor = BorderLight,
                        focusedLabelColor = SchoolPrimary,
                        unfocusedLabelColor = LightTextSecondary
                    ),
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(12.dp))
                Row(
                    modifier = Modifier.horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    categories.forEach { cat ->
                        val isSelected = selectedCategory == cat
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .background(if (isSelected) SchoolPrimary else LightBackground)
                                .clickable { selectedCategory = cat }
                                .padding(horizontal = 12.dp, vertical = 6.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = cat,
                                color = if (isSelected) Color.White else LightTextSecondary,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }
        }

        // Active Notices Title
        item {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(top = 4.dp)
            ) {
                Icon(Icons.Default.Campaign, contentDescription = null, tint = SchoolPrimary, modifier = Modifier.size(20.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text("Official Circular Board", fontWeight = FontWeight.Bold, fontSize = 16.sp, color = LightTextPrimary)
            }
        }

        // Notices List
        val filteredNotices = notices.filter {
            (selectedCategory == "All" || it.targetType.equals(selectedCategory, ignoreCase = true)) &&
            (it.title.contains(searchQuery, ignoreCase = true) || it.content.contains(searchQuery, ignoreCase = true))
        }

        if (filteredNotices.isEmpty()) {
            item {
                GlassCard(modifier = Modifier.fillMaxWidth()) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 24.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(Icons.Default.Inbox, contentDescription = null, tint = LightTextSecondary, modifier = Modifier.size(40.dp))
                            Spacer(modifier = Modifier.height(8.dp))
                            Text("No current circular updates found.", fontSize = 13.sp, color = LightTextSecondary)
                        }
                    }
                }
            }
        } else {
            items(filteredNotices) { notice ->
                var expanded by remember { mutableStateOf(false) }
                
                GlassCard(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { expanded = !expanded }
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                            Box(
                                modifier = Modifier
                                    .size(36.dp)
                                    .clip(CircleShape)
                                    .background(SchoolPrimary.copy(alpha = 0.08f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(Icons.Default.Announcement, contentDescription = null, tint = SchoolPrimary, modifier = Modifier.size(18.dp))
                            }
                            Spacer(modifier = Modifier.width(12.dp))
                            Column {
                                Text(notice.title, fontWeight = FontWeight.Bold, fontSize = 14.sp, color = LightTextPrimary)
                                Text(
                                    text = "From: ${notice.senderName} | Date: ${notice.date}",
                                    fontSize = 11.sp,
                                    color = LightTextSecondary
                                )
                            }
                        }
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(6.dp))
                                .background(SchoolSecondary.copy(alpha = 0.1f))
                                .padding(horizontal = 8.dp, vertical = 4.dp)
                        ) {
                            Text(notice.targetType.uppercase(), color = SchoolSecondary, fontSize = 9.sp, fontWeight = FontWeight.ExtraBold)
                        }
                    }
                    Spacer(modifier = Modifier.height(10.dp))
                    Text(
                        text = notice.content,
                        fontSize = 13.sp,
                        color = LightTextPrimary,
                        maxLines = if (expanded) Int.MAX_VALUE else 2,
                        overflow = TextOverflow.Ellipsis,
                        lineHeight = 18.sp
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = if (expanded) "Show less" else "Read more...",
                        color = SchoolPrimary,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.align(Alignment.End)
                    )
                }
            }
        }

        // Upcoming events schedule
        item {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(top = 8.dp)
            ) {
                Icon(Icons.Default.EventNote, contentDescription = null, tint = SchoolPrimary, modifier = Modifier.size(20.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text("Upcoming School Events", fontWeight = FontWeight.Bold, fontSize = 16.sp, color = LightTextPrimary)
            }
        }

        if (events.isEmpty()) {
            item {
                GlassCard(modifier = Modifier.fillMaxWidth()) {
                    Text("No upcoming events logged.", fontSize = 13.sp, color = LightTextSecondary)
                }
            }
        } else {
            items(events) { event ->
                val isHoliday = event.type == "HOLIDAY"
                GlassCard(modifier = Modifier.fillMaxWidth()) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .clip(RoundedCornerShape(10.dp))
                                .background(if (isHoliday) SchoolWarning.copy(alpha = 0.1f) else SchoolSecondary.copy(alpha = 0.1f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = if (isHoliday) Icons.Default.BeachAccess else Icons.Default.Event,
                                contentDescription = null,
                                tint = if (isHoliday) SchoolWarning else SchoolSecondary,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(16.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(event.title, fontWeight = FontWeight.Bold, fontSize = 14.sp, color = LightTextPrimary)
                            Text(
                                "Date: ${event.date} | ${event.description}",
                                fontSize = 11.sp,
                                color = LightTextSecondary
                            )
                        }
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(6.dp))
                                .background(if (isHoliday) SchoolWarning.copy(alpha = 0.1f) else SchoolSecondary.copy(alpha = 0.1f))
                                .padding(horizontal = 8.dp, vertical = 4.dp)
                        ) {
                            Text(
                                text = event.type,
                                color = if (isHoliday) SchoolWarning else SchoolSecondary,
                                fontSize = 9.sp,
                                fontWeight = FontWeight.ExtraBold
                            )
                        }
                    }
                }
            }
        }
    }
}

// ===============================================
// STUDENT MODULE: QR-CODE ID CARD VISUALIZER
// ===============================================
@Composable
fun StudentIDCardModule(student: UserEntity?, studentDetails: StudentEntity?) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState()),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Top
    ) {
        Spacer(modifier = Modifier.height(16.dp))
        
        // Premium vertical corporate badge identity card
        Card(
            modifier = Modifier
                .width(310.dp)
                .shadow(16.dp, RoundedCornerShape(24.dp)),
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Crest header band
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center,
                    modifier = Modifier.padding(bottom = 20.dp)
                ) {
                    Icon(Icons.Default.School, contentDescription = null, tint = SchoolPrimary, modifier = Modifier.size(24.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("HOLY SPIRIT ERP", fontWeight = FontWeight.ExtraBold, fontSize = 13.sp, color = SchoolPrimary, letterSpacing = 1.sp)
                }

                // Student avatar placeholder
                Box(
                    modifier = Modifier
                        .size(108.dp)
                        .clip(CircleShape)
                        .background(Brush.linearGradient(listOf(SchoolPrimary.copy(alpha = 0.2f), SchoolSecondary.copy(alpha = 0.2f))))
                        .border(2.dp, SchoolPrimary, CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = student?.name?.split(" ")?.mapNotNull { it.firstOrNull() }?.take(2)?.joinToString("")?.uppercase() ?: "ST",
                        fontSize = 28.sp,
                        fontWeight = FontWeight.Bold,
                        color = SchoolPrimary
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Name & details
                Text(
                    text = student?.name ?: "Ethan D'Costa",
                    fontWeight = FontWeight.Bold,
                    fontSize = 20.sp,
                    color = LightTextPrimary,
                    textAlign = TextAlign.Center
                )
                Text(
                    text = "STUDENT IDENTITY CARD",
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                    color = SchoolSecondary,
                    letterSpacing = 1.sp,
                    modifier = Modifier.padding(top = 4.dp)
                )

                Spacer(modifier = Modifier.height(20.dp))

                // Technical meta
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(LightBackground, RoundedCornerShape(12.dp))
                        .padding(14.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text("Class Stream:", fontSize = 12.sp, color = LightTextSecondary, fontWeight = FontWeight.Medium)
                        Text(studentDetails?.classId ?: "5A", fontWeight = FontWeight.Bold, fontSize = 12.sp, color = LightTextPrimary)
                    }
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text("Roll Number:", fontSize = 12.sp, color = LightTextSecondary, fontWeight = FontWeight.Medium)
                        Text(studentDetails?.rollNumber?.toString() ?: "12", fontWeight = FontWeight.Bold, fontSize = 12.sp, color = LightTextPrimary)
                    }
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text("Blood Group:", fontSize = 12.sp, color = LightTextSecondary, fontWeight = FontWeight.Medium)
                        Text(studentDetails?.bloodGroup ?: "O+", fontWeight = FontWeight.Bold, fontSize = 12.sp, color = LightTextPrimary)
                    }
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text("Admission ID:", fontSize = 12.sp, color = LightTextSecondary, fontWeight = FontWeight.Medium)
                        Text(studentDetails?.admissionNumber ?: "HS-2026-084", fontWeight = FontWeight.Bold, fontSize = 12.sp, color = LightTextPrimary, fontFamily = FontFamily.Monospace)
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))

                // Zero-dependency Canvas drawn custom security QR Code representation
                Text("SECURE ACCESS CODE", fontSize = 9.sp, fontWeight = FontWeight.Bold, color = LightTextSecondary, modifier = Modifier.padding(bottom = 8.dp))
                Canvas(
                    modifier = Modifier
                        .size(110.dp)
                        .border(1.dp, BorderLight, RoundedCornerShape(8.dp))
                        .padding(10.dp)
                ) {
                    val sideSize = size.width
                    val cols = 8
                    val cellSize = sideSize / cols

                    val qrMatrix = listOf(
                        listOf(1,1,1,1,0,1,1,1),
                        listOf(1,0,0,1,1,0,0,1),
                        listOf(1,0,0,1,0,1,0,1),
                        listOf(1,1,1,1,1,1,1,1),
                        listOf(0,1,0,1,1,0,1,0),
                        listOf(1,0,1,0,0,1,0,1),
                        listOf(1,0,0,1,1,0,0,1),
                        listOf(1,1,1,1,0,1,1,1)
                    )

                    qrMatrix.forEachIndexed { r, row ->
                        row.forEachIndexed { c, cell ->
                            if (cell == 1) {
                                drawRect(
                                    color = SchoolPrimary,
                                    topLeft = Offset(c * cellSize, r * cellSize),
                                    size = Size(cellSize, cellSize)
                                )
                            }
                        }
                    }
                }
            }
        }
        Spacer(modifier = Modifier.height(40.dp))
    }
}

// ===============================================
// STUDENT MODULE: CLASS TIMETABLE
// ===============================================
@Composable
fun StudentTimetableModule(timetable: List<TimetableEntity>) {
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
            Text("Class Schedule Planner", fontWeight = FontWeight.Bold, fontSize = 16.sp, color = LightTextPrimary)
        }
        Spacer(modifier = Modifier.height(12.dp))

        // Weekday selector Row
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
                    Text(
                        text = day.take(3),
                        color = if (isSel) Color.White else LightTextSecondary,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }

        if (activePeriodRows.isEmpty()) {
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth(),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(Icons.Default.SentimentSatisfied, contentDescription = null, tint = LightTextSecondary, modifier = Modifier.size(48.dp))
                    Spacer(modifier = Modifier.height(8.dp))
                    Text("No classes scheduled for $currentDay.", color = LightTextSecondary, fontSize = 14.sp)
                }
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
                                    Text(
                                        text = item.subjectId,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 15.sp,
                                        color = LightTextPrimary
                                    )
                                    Spacer(modifier = Modifier.height(2.dp))
                                    Text(
                                        text = "Instructor Ref: ${item.teacherId} | Room 102",
                                        fontSize = 12.sp,
                                        color = LightTextSecondary
                                    )
                                }
                            }
                            Column(horizontalAlignment = Alignment.End) {
                                Text(
                                    text = "${item.startTime} - ${item.endTime}",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = SchoolPrimary
                                )
                                Spacer(modifier = Modifier.height(2.dp))
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(6.dp))
                                        .background(bgCol)
                                        .padding(horizontal = 6.dp, vertical = 2.dp)
                                ) {
                                    Text("Period ${item.period}", color = textCol, fontSize = 9.sp, fontWeight = FontWeight.Bold)
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
// STUDENT MODULE: HOMEWORK ASSIGNMENTS
// ===============================================
@Composable
fun StudentHomeworkModule(homeworks: List<HomeworkEntity>) {
    Column(modifier = Modifier.fillMaxSize()) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.fillMaxWidth()
        ) {
            Icon(Icons.Default.MenuBook, contentDescription = null, tint = SchoolPrimary, modifier = Modifier.size(20.dp))
            Spacer(modifier = Modifier.width(8.dp))
            Text("Assigned Homework & Projects", fontWeight = FontWeight.Bold, fontSize = 16.sp, color = LightTextPrimary)
        }
        Spacer(modifier = Modifier.height(12.dp))

        if (homeworks.isEmpty()) {
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth(),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(Icons.Default.Celebration, contentDescription = null, tint = SchoolSuccess, modifier = Modifier.size(56.dp))
                    Spacer(modifier = Modifier.height(8.dp))
                    Text("No homework assigned! Enjoy your day.", color = LightTextSecondary, fontSize = 14.sp)
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(12.dp),
                contentPadding = PaddingValues(bottom = 24.dp)
            ) {
                items(homeworks) { item ->
                    var isCompleted by remember(item.id) { mutableStateOf(false) }

                    GlassCard(
                        modifier = Modifier
                            .fillMaxWidth()
                            .shadow(if (isCompleted) 1.dp else 3.dp, RoundedCornerShape(18.dp))
                    ) {
                        Column {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.Top
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                                    Box(
                                        modifier = Modifier
                                            .size(40.dp)
                                            .clip(CircleShape)
                                            .background(
                                                if (isCompleted) SchoolSuccess.copy(alpha = 0.1f) else SchoolPrimary.copy(alpha = 0.1f)
                                            )
                                            .clickable { isCompleted = !isCompleted },
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(
                                            imageVector = if (isCompleted) Icons.Default.CheckCircle else Icons.Default.RadioButtonUnchecked,
                                            contentDescription = "Status",
                                            tint = if (isCompleted) SchoolSuccess else SchoolPrimary,
                                            modifier = Modifier.size(24.dp)
                                        )
                                    }
                                    Spacer(modifier = Modifier.width(12.dp))
                                    Column {
                                        Text(
                                            text = item.title,
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 15.sp,
                                            color = if (isCompleted) LightTextSecondary else LightTextPrimary
                                        )
                                        Text(
                                            text = "Subject: ${item.subjectId}",
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = SchoolPrimary
                                        )
                                    }
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
                            
                            Spacer(modifier = Modifier.height(12.dp))
                            HorizontalDivider(color = BorderLight)
                            Spacer(modifier = Modifier.height(12.dp))

                            Text(
                                text = item.instructions,
                                fontSize = 13.sp,
                                color = if (isCompleted) LightTextSecondary.copy(alpha = 0.6f) else LightTextPrimary,
                                lineHeight = 18.sp
                            )

                            Spacer(modifier = Modifier.height(14.dp))

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Box(
                                        modifier = Modifier
                                            .size(28.dp)
                                            .clip(CircleShape)
                                            .background(SchoolSecondary.copy(alpha = 0.15f)),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(
                                            text = item.teacherName.take(2).uppercase(),
                                            fontSize = 10.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = SchoolSecondary
                                        )
                                    }
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = item.teacherName,
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Medium,
                                        color = LightTextSecondary
                                    )
                                }

                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                                ) {
                                    IconButton(
                                        onClick = {},
                                        modifier = Modifier
                                            .size(36.dp)
                                            .clip(RoundedCornerShape(8.dp))
                                            .background(LightBackground)
                                    ) {
                                        Icon(Icons.Default.AttachFile, contentDescription = "Attachments", tint = LightTextSecondary, modifier = Modifier.size(16.dp))
                                    }
                                    
                                    Box(
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(8.dp))
                                            .background(
                                                if (isCompleted) SchoolSuccess.copy(alpha = 0.15f) else SchoolWarning.copy(alpha = 0.15f)
                                            )
                                            .padding(horizontal = 10.dp, vertical = 6.dp)
                                    ) {
                                        Text(
                                            text = if (isCompleted) "Submitted" else "Due: ${item.dueDate}",
                                            color = if (isCompleted) SchoolSuccess else SchoolWarning,
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

// ===============================================
// STUDENT MODULE: GRADES & REPORT CARD
// ===============================================
@Composable
fun StudentGradesModule(marks: List<MarkEntity>) {
    val scrollState = rememberScrollState()
    var pdfStatusMsg by remember { mutableStateOf<String?>(null) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(scrollState),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        
        // Report card simulation banner
        GlassCard(modifier = Modifier.fillMaxWidth()) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.Badge, contentDescription = null, tint = SchoolPrimary, modifier = Modifier.size(24.dp))
                Spacer(modifier = Modifier.width(10.dp))
                Text("Performance Report Card", fontWeight = FontWeight.Bold, fontSize = 16.sp, color = LightTextPrimary)
            }
            Spacer(modifier = Modifier.height(4.dp))
            Text("Grades displayed are officially published by school administration. Download certified PDF report card.", fontSize = 12.sp, color = LightTextSecondary)
            
            Button(
                onClick = {
                    pdfStatusMsg = "Holy_Spirit_Institute_Report_Ethan_2026.pdf generated and saved to /downloads successfully using PDFKit engine!"
                },
                colors = ButtonDefaults.buttonColors(containerColor = SchoolPrimary),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 16.dp)
                    .height(48.dp)
                    .testTag("download_pdf_report_btn")
            ) {
                Icon(Icons.Default.Download, contentDescription = null, modifier = Modifier.size(18.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text("DOWNLOAD CERTIFIED REPORT CARD (PDF)", fontWeight = FontWeight.Bold, fontSize = 12.sp)
            }

            if (pdfStatusMsg != null) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 12.dp)
                        .background(SchoolSuccess.copy(alpha = 0.1f), RoundedCornerShape(8.dp))
                        .border(1.dp, SchoolSuccess.copy(alpha = 0.2f), RoundedCornerShape(8.dp))
                        .padding(12.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.CheckCircle, contentDescription = null, tint = SchoolSuccess, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(pdfStatusMsg ?: "", color = SchoolSuccess, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }

        // Live Analytics Chart
        if (marks.isNotEmpty()) {
            GlassCard(modifier = Modifier.fillMaxWidth()) {
                Text("Subject Comparison Trend", fontWeight = FontWeight.Bold, fontSize = 14.sp, color = LightTextPrimary)
                Spacer(modifier = Modifier.height(14.dp))
                val chartData = marks.map { Pair(it.subjectId, (it.marksObtained / it.maxMarks).toFloat()) }
                SchoolPerformanceBarChart(data = chartData)
            }
        }

        // List grades sheet
        GlassCard(modifier = Modifier.fillMaxWidth()) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                Icon(Icons.Default.AutoGraph, contentDescription = null, tint = SchoolPrimary, modifier = Modifier.size(20.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text("Subject-wise Performance Record", fontWeight = FontWeight.Bold, fontSize = 14.sp, color = LightTextPrimary)
            }
            Spacer(modifier = Modifier.height(14.dp))

            if (marks.isEmpty()) {
                Text("Gradesheet is currently private. Wait for admin publication.", fontSize = 13.sp, color = LightTextSecondary)
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
                                Spacer(modifier = Modifier.height(4.dp))
                                Text("Exam: ${mark.examType} | Remarks: ${mark.remarks ?: "Keep it up"}", fontSize = 12.sp, color = LightTextSecondary)
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
                                        text = "${percent.toInt()}% (${if (percent >= 85) "A" else if (percent >= 70) "B" else "C"})",
                                        fontSize = 10.sp,
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
// STUDENT MODULE: SUBMIT LEAVE REQUEST
// ===============================================
@Composable
fun StudentLeaveModule(viewModel: SchoolViewModel) {
    var startDate by remember { mutableStateOf("2026-07-20") }
    var endDate by remember { mutableStateOf("2026-07-22") }
    var reason by remember { mutableStateOf("") }
    var feedbackMsg by remember { mutableStateOf<String?>(null) }

    GlassCard(modifier = Modifier.fillMaxWidth()) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(Icons.Default.RequestQuote, contentDescription = null, tint = SchoolPrimary, modifier = Modifier.size(24.dp))
            Spacer(modifier = Modifier.width(10.dp))
            Text("Medical / Leave Application", fontWeight = FontWeight.Bold, fontSize = 16.sp, color = LightTextPrimary)
        }
        Spacer(modifier = Modifier.height(4.dp))
        Text("Leave requests will be reviewed and approved by the Principal's administration office.", fontSize = 12.sp, color = LightTextSecondary)
        
        Spacer(modifier = Modifier.height(16.dp))

        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            OutlinedTextField(
                value = startDate,
                onValueChange = { startDate = it },
                label = { Text("Start Date") },
                singleLine = true,
                leadingIcon = { Icon(Icons.Default.CalendarToday, contentDescription = null, tint = SchoolPrimary, modifier = Modifier.size(18.dp)) },
                shape = RoundedCornerShape(12.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = SchoolPrimary,
                    unfocusedBorderColor = BorderLight,
                    focusedLabelColor = SchoolPrimary,
                    unfocusedLabelColor = LightTextSecondary
                ),
                modifier = Modifier.weight(1f)
            )
            OutlinedTextField(
                value = endDate,
                onValueChange = { endDate = it },
                label = { Text("End Date") },
                singleLine = true,
                leadingIcon = { Icon(Icons.Default.CalendarToday, contentDescription = null, tint = SchoolPrimary, modifier = Modifier.size(18.dp)) },
                shape = RoundedCornerShape(12.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = SchoolPrimary,
                    unfocusedBorderColor = BorderLight,
                    focusedLabelColor = SchoolPrimary,
                    unfocusedLabelColor = LightTextSecondary
                ),
                modifier = Modifier.weight(1f)
            )
        }
        Spacer(modifier = Modifier.height(14.dp))

        OutlinedTextField(
            value = reason,
            onValueChange = { reason = it },
            label = { Text("Reason for Leave (e.g. Health fever)") },
            shape = RoundedCornerShape(12.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = SchoolPrimary,
                unfocusedBorderColor = BorderLight,
                focusedLabelColor = SchoolPrimary,
                unfocusedLabelColor = LightTextSecondary
            ),
            modifier = Modifier
                .fillMaxWidth()
                .height(110.dp)
                .testTag("leave_reason_input")
        )

        Spacer(modifier = Modifier.height(16.dp))

        Button(
            onClick = {
                viewModel.submitLeaveRequest(startDate, endDate, reason)
                reason = ""
                feedbackMsg = "Leave Request submitted to school board successfully!"
            },
            colors = ButtonDefaults.buttonColors(containerColor = SchoolPrimary),
            shape = RoundedCornerShape(12.dp),
            modifier = Modifier
                .fillMaxWidth()
                .height(48.dp)
                .testTag("apply_leave_btn")
        ) {
            Icon(Icons.Default.Send, contentDescription = null, modifier = Modifier.size(18.dp))
            Spacer(modifier = Modifier.width(8.dp))
            Text("SUBMIT APPLICATION FOR REVIEW", fontWeight = FontWeight.Bold, fontSize = 13.sp)
        }

        if (feedbackMsg != null) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 12.dp)
                    .background(SchoolSuccess.copy(alpha = 0.1f), RoundedCornerShape(8.dp))
                    .border(1.dp, SchoolSuccess.copy(alpha = 0.2f), RoundedCornerShape(8.dp))
                    .padding(12.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.CheckCircle, contentDescription = null, tint = SchoolSuccess, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(feedbackMsg ?: "", color = SchoolSuccess, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}
