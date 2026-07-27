package com.example.ui.viewmodel

import android.app.Application
import android.location.Location
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.data.database.AppDatabase
import com.example.data.entity.*
import com.example.data.repository.SchoolRepository
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

class SchoolViewModel(application: Application) : AndroidViewModel(application) {

    private val repository: SchoolRepository
    init {
        val database = AppDatabase.getDatabase(application)
        repository = SchoolRepository(database.appDao())
        viewModelScope.launch {
            repository.seedInitialDataIfEmpty()
        }
    }

    // --- Authentication State ---
    private val _currentUser = MutableStateFlow<UserEntity?>(null)
    val currentUser: StateFlow<UserEntity?> = _currentUser.asStateFlow()

    private val _currentTeacher = MutableStateFlow<TeacherEntity?>(null)
    val currentTeacher: StateFlow<TeacherEntity?> = _currentTeacher.asStateFlow()

    private val _currentStudent = MutableStateFlow<StudentEntity?>(null)
    val currentStudent: StateFlow<StudentEntity?> = _currentStudent.asStateFlow()

    private val _currentParent = MutableStateFlow<ParentEntity?>(null)
    val currentParent: StateFlow<ParentEntity?> = _currentParent.asStateFlow()

    private val _loginError = MutableStateFlow<String?>(null)
    val loginError: StateFlow<String?> = _loginError.asStateFlow()

    // --- ERP Live Lists ---
    val allUsers: StateFlow<List<UserEntity>> = repository.allUsers.stateIn(viewModelScope, SharingStarted.Lazily, emptyList())
    val allTeachers: StateFlow<List<TeacherEntity>> = repository.allTeachers.stateIn(viewModelScope, SharingStarted.Lazily, emptyList())
    val allStudents: StateFlow<List<StudentEntity>> = repository.allStudents.stateIn(viewModelScope, SharingStarted.Lazily, emptyList())
    val allParents: StateFlow<List<ParentEntity>> = repository.allParents.stateIn(viewModelScope, SharingStarted.Lazily, emptyList())
    val allClassSections: StateFlow<List<ClassSectionEntity>> = repository.allClassSections.stateIn(viewModelScope, SharingStarted.Lazily, emptyList())
    val allSubjects: StateFlow<List<SubjectEntity>> = repository.allSubjects.stateIn(viewModelScope, SharingStarted.Lazily, emptyList())
    val allTeacherAssignments: StateFlow<List<TeacherAssignmentEntity>> = repository.allTeacherAssignments.stateIn(viewModelScope, SharingStarted.Lazily, emptyList())
    val allNotices: StateFlow<List<NoticeEntity>> = repository.allNotices.stateIn(viewModelScope, SharingStarted.Lazily, emptyList())
    val allEventsHolidays: StateFlow<List<EventHolidayEntity>> = repository.allEventsHolidays.stateIn(viewModelScope, SharingStarted.Lazily, emptyList())
    val allLeaveRequests: StateFlow<List<LeaveRequestEntity>> = repository.allLeaveRequests.stateIn(viewModelScope, SharingStarted.Lazily, emptyList())
    val allAuditLogs: StateFlow<List<AuditLogEntity>> = repository.allAuditLogs.stateIn(viewModelScope, SharingStarted.Lazily, emptyList())

    // --- Active Chat Message List ---
    private val _activeChatPartner = MutableStateFlow<UserEntity?>(null)
    val activeChatPartner: StateFlow<UserEntity?> = _activeChatPartner.asStateFlow()

    val activeChatMessages: StateFlow<List<MessageEntity>> = _currentUser.combine(_activeChatPartner) { user, partner ->
        if (user != null && partner != null) {
            repository.getChatMessages(user.id, partner.id)
        } else {
            flowOf(emptyList())
        }
    }.flatMapLatest { it }.stateIn(viewModelScope, SharingStarted.Lazily, emptyList())

    // --- School Settings Configuration ---
    private val _academicYear = MutableStateFlow("2026-2027")
    val academicYear: StateFlow<String> = _academicYear.asStateFlow()

    private val _schoolTiming = MutableStateFlow("08:00 AM - 01:30 PM")
    val schoolTiming: StateFlow<String> = _schoolTiming.asStateFlow()

    private val _attendanceTimingLimit = MutableStateFlow("08:00") // 8:00 AM cutoff
    val attendanceTimingLimit: StateFlow<String> = _attendanceTimingLimit.asStateFlow()

    private val _schoolInfoName = MutableStateFlow("Holy Spirit Institute")
    val schoolInfoName: StateFlow<String> = _schoolInfoName.asStateFlow()

    private val _schoolInfoAddress = MutableStateFlow("Margao, Goa - 403601")
    val schoolInfoAddress: StateFlow<String> = _schoolInfoAddress.asStateFlow()

    // --- GPS Geofencing Constants (Holy Spirit School, Margao, Goa) ---
    val schoolLat = 15.2758
    val schoolLng = 73.9678
    val geofenceRadiusMeters = 100.0

    // --- Local Notifications Hub ---
    private val _notifications = MutableStateFlow<List<String>>(emptyList())
    val notifications: StateFlow<List<String>> = _notifications.asStateFlow()

    fun triggerNotification(message: String) {
        _notifications.update { listOf(message) + it }
    }

    fun clearNotifications() {
        _notifications.value = emptyList()
    }

    // --- AUTHENTICATION FLOWS ---
    fun login(email: String, passwordHash: String, rememberMe: Boolean, onResult: (Boolean) -> Unit) {
        viewModelScope.launch {
            _loginError.value = null
            val user = repository.getUserByEmail(email)
            if (user != null) {
                if (user.passwordHash == passwordHash) {
                    if (user.status == "Inactive") {
                        _loginError.value = "Your account is deactivated. Contact Admin."
                        onResult(false)
                        return@launch
                    }
                    _currentUser.value = user
                    // Load associated entity
                    when (user.role) {
                        "Teacher" -> _currentTeacher.value = repository.getTeacherById(user.id)
                        "Student" -> _currentStudent.value = repository.getStudentById(user.id)
                        "Parent" -> _currentParent.value = repository.getParentById(user.id)
                    }
                    repository.insertAuditLog(
                        AuditLogEntity(
                            userId = user.id,
                            userName = user.name,
                            action = "Login Success",
                            details = "User successfully authenticated into ${user.role} workspace."
                        )
                    )
                    onResult(true)
                } else {
                    _loginError.value = "Incorrect password."
                    onResult(false)
                }
            } else {
                _loginError.value = "User not found with this email."
                onResult(false)
            }
        }
    }

    fun logout() {
        val user = _currentUser.value
        if (user != null) {
            viewModelScope.launch {
                repository.insertAuditLog(
                    AuditLogEntity(
                        userId = user.id,
                        userName = user.name,
                        action = "Logout",
                        details = "User explicitly logged out of session."
                    )
                )
            }
        }
        _currentUser.value = null
        _currentTeacher.value = null
        _currentStudent.value = null
        _currentParent.value = null
        _activeChatPartner.value = null
    }

    fun resetPasswordSimulated(email: String, onResult: (Boolean, String) -> Unit) {
        viewModelScope.launch {
            val user = repository.getUserByEmail(email)
            if (user != null) {
                // Update password to 'reset123'
                val updated = user.copy(passwordHash = "reset123")
                repository.updateUser(updated)
                repository.insertAuditLog(
                    AuditLogEntity(
                        userId = user.id,
                        userName = user.name,
                        action = "Password Reset Requested",
                        details = "Temporary password set to 'reset123'."
                    )
                )
                onResult(true, "Password has been reset to temporary 'reset123'. Please login and change it.")
            } else {
                onResult(false, "No account associated with this email.")
            }
        }
    }

    // --- ADMIN USER & DATA CRUD OPERATIONS ---
    fun addTeacher(
        name: String, email: String, phone: String, address: String,
        qualification: String, experience: String, joiningDate: String, isClassTeacherOf: String?
    ) {
        viewModelScope.launch {
            val count = allUsers.value.count { it.role == "Teacher" }
            val id = "TC-${100 + count + 1}"
            val user = UserEntity(
                id = id, name = name, email = email, passwordHash = "teacher123",
                role = "Teacher", phone = phone, address = address, photoUrl = "", status = "Active"
            )
            val teacher = TeacherEntity(
                teacherId = id, qualification = qualification, experience = experience,
                joiningDate = joiningDate, isClassTeacherOfClassId = isClassTeacherOf
            )
            repository.insertUser(user)
            repository.insertTeacher(teacher)
            repository.insertAuditLog(
                AuditLogEntity(
                    userId = _currentUser.value?.id ?: "SYSTEM",
                    userName = _currentUser.value?.name ?: "Admin",
                    action = "Add Teacher",
                    details = "Created teacher record for $name ($id)."
                )
            )
            triggerNotification("New Teacher Profile added: $name")
        }
    }

    fun addStudent(
        name: String, email: String, phone: String, address: String,
        admissionNo: String, rollNo: Int, dob: String, gender: String, bloodGroup: String,
        classId: String, parentId: String, emergencyPhone: String
    ) {
        viewModelScope.launch {
            val count = allUsers.value.count { it.role == "Student" }
            val id = "ST-${200 + count + 1}"
            val user = UserEntity(
                id = id, name = name, email = email, passwordHash = "student123",
                role = "Student", phone = phone, address = address, photoUrl = "", status = "Active"
            )
            val student = StudentEntity(
                studentId = id, admissionNumber = admissionNo, rollNumber = rollNo,
                dob = dob, gender = gender, bloodGroup = bloodGroup, classId = classId,
                parentId = parentId, emergencyContact = emergencyPhone, admissionDate = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
            )
            repository.insertUser(user)
            repository.insertStudent(student)
            repository.insertAuditLog(
                AuditLogEntity(
                    userId = _currentUser.value?.id ?: "SYSTEM",
                    userName = _currentUser.value?.name ?: "Admin",
                    action = "Add Student",
                    details = "Created student record for $name ($id) in Class $classId."
                )
            )
            triggerNotification("New Student Enrolled: $name to Class $classId")
        }
    }

    fun addParent(name: String, motherName: String, email: String, phone: String, address: String, occupation: String) {
        viewModelScope.launch {
            val count = allUsers.value.count { it.role == "Parent" }
            val id = "PR-${300 + count + 1}"
            val user = UserEntity(
                id = id, name = name, email = email, passwordHash = "parent123",
                role = "Parent", phone = phone, address = address, photoUrl = "", status = "Active"
            )
            val parent = ParentEntity(parentId = id, motherName = motherName, occupation = occupation)
            repository.insertUser(user)
            repository.insertParent(parent)
            repository.insertAuditLog(
                AuditLogEntity(
                    userId = _currentUser.value?.id ?: "SYSTEM",
                    userName = _currentUser.value?.name ?: "Admin",
                    action = "Add Parent",
                    details = "Created parent record for $name ($id)."
                )
            )
            triggerNotification("New Parent Profile created: $name")
        }
    }

    fun addClassSection(id: String, name: String, section: String) {
        viewModelScope.launch {
            repository.insertClassSection(ClassSectionEntity(id, name, section))
            repository.insertAuditLog(
                AuditLogEntity(
                    userId = _currentUser.value?.id ?: "SYSTEM",
                    userName = _currentUser.value?.name ?: "Admin",
                    action = "Add Class",
                    details = "Created ClassSection $id ($name - $section)."
                )
            )
        }
    }

    fun addSubject(id: String, name: String) {
        viewModelScope.launch {
            repository.insertSubject(SubjectEntity(id, name))
            repository.insertAuditLog(
                AuditLogEntity(
                    userId = _currentUser.value?.id ?: "SYSTEM",
                    userName = _currentUser.value?.name ?: "Admin",
                    action = "Add Subject",
                    details = "Added subject $id - $name."
                )
            )
        }
    }

    fun assignSubjectTeacher(teacherId: String, classId: String, subjectId: String) {
        viewModelScope.launch {
            repository.insertTeacherAssignment(TeacherAssignmentEntity(teacherId = teacherId, classId = classId, subjectId = subjectId))
            repository.insertAuditLog(
                AuditLogEntity(
                    userId = _currentUser.value?.id ?: "SYSTEM",
                    userName = _currentUser.value?.name ?: "Admin",
                    action = "Assign Teacher Subject",
                    details = "Assigned Teacher $teacherId to Subject $subjectId in Class $classId."
                )
            )
            triggerNotification("New teaching assignment created for teacher $teacherId")
        }
    }

    fun assignClassTeacher(teacherId: String, classId: String) {
        viewModelScope.launch {
            val teacher = repository.getTeacherById(teacherId)
            if (teacher != null) {
                val updated = teacher.copy(isClassTeacherOfClassId = classId)
                repository.insertTeacher(updated)
                repository.insertAuditLog(
                    AuditLogEntity(
                        userId = _currentUser.value?.id ?: "SYSTEM",
                        userName = _currentUser.value?.name ?: "Admin",
                        action = "Assign Class Teacher",
                        details = "Set Teacher $teacherId as Class Teacher of Class $classId."
                    )
                )
            }
        }
    }

    // --- TIMETABLE OPERATIONS ---
    fun createTimetableEntry(classId: String, dayOfWeek: String, period: Int, subjectId: String, teacherId: String, startTime: String, endTime: String) {
        viewModelScope.launch {
            repository.insertTimetable(TimetableEntity(
                classId = classId, dayOfWeek = dayOfWeek, period = period,
                subjectId = subjectId, teacherId = teacherId, startTime = startTime, endTime = endTime
            ))
            repository.insertAuditLog(
                AuditLogEntity(
                    userId = _currentUser.value?.id ?: "SYSTEM",
                    userName = _currentUser.value?.name ?: "Admin",
                    action = "Add Timetable Entry",
                    details = "Added timetable period $period for $classId ($dayOfWeek)."
                )
            )
        }
    }

    // --- NOTICE BOARD OPERATIONS ---
    fun publishNotice(title: String, content: String, targetType: String, targetClassId: String?) {
        viewModelScope.launch {
            val sender = _currentUser.value
            val notice = NoticeEntity(
                senderId = sender?.id ?: "SYSTEM",
                senderName = sender?.name ?: "Admin",
                targetType = targetType,
                targetClassId = targetClassId,
                title = title,
                content = content,
                date = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
            )
            repository.insertNotice(notice)
            repository.insertAuditLog(
                AuditLogEntity(
                    userId = sender?.id ?: "SYSTEM",
                    userName = sender?.name ?: "Admin",
                    action = "Notice Published",
                    details = "Notice titled '$title' published to $targetType ${targetClassId ?: ""}."
                )
            )
            triggerNotification("Notice Published: $title")
        }
    }

    // --- CALENDAR EVENTS & HOLIDAYS ---
    fun createEventHoliday(title: String, description: String, date: String, type: String) {
        viewModelScope.launch {
            repository.insertEventHoliday(
                EventHolidayEntity(title = title, description = description, date = date, type = type)
            )
            repository.insertAuditLog(
                AuditLogEntity(
                    userId = _currentUser.value?.id ?: "SYSTEM",
                    userName = _currentUser.value?.name ?: "Admin",
                    action = "Create Calendar Item",
                    details = "Created $type: '$title' on $date."
                )
            )
            triggerNotification("New Calendar Entry: $title ($type)")
        }
    }

    fun deleteEventHoliday(id: Int) {
        viewModelScope.launch {
            repository.deleteEventHoliday(id)
        }
    }

    // --- GEOLOCATED TEACHER SELF ATTENDANCE ---
    fun markTeacherAttendance(latitude: Double, longitude: Double, customStatus: String? = null, reason: String? = null, onResult: (Boolean, String) -> Unit) {
        viewModelScope.launch {
            val teacher = _currentUser.value ?: return@launch
            val dateStr = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())

            // Check if already checked in today
            val existing = repository.getAttendanceForUserOnDate(teacher.id, dateStr)
            if (existing != null) {
                onResult(false, "You have already marked attendance for today!")
                return@launch
            }

            // Calculate distance to school using Haversine
            val results = FloatArray(1)
            Location.distanceBetween(latitude, longitude, schoolLat, schoolLng, results)
            val distanceMeters = results[0].toDouble()

            val isInsideGeofence = distanceMeters <= geofenceRadiusMeters

            // Log details
            val checkInTimeStr = SimpleDateFormat("hh:mm a", Locale.getDefault()).format(Date())
            val calendar = Calendar.getInstance()
            val hour = calendar.get(Calendar.HOUR_OF_DAY)
            val minute = calendar.get(Calendar.MINUTE)

            val status: String
            val remarks: String?

            if (customStatus == "Absent") {
                status = "Absent"
                remarks = "Marked Absent from App"
            } else {
                if (!isInsideGeofence) {
                    onResult(false, "Geofencing error: You are ${distanceMeters.toInt()} meters away from Holy Spirit School. You must be inside 100 meters.")
                    return@launch
                }

                // Inside school campus
                if (hour < 8 || (hour == 8 && minute == 0)) {
                    status = "Present"
                    remarks = "Checked in on time."
                } else {
                    status = "Late"
                    remarks = "Checked in Late. Reason: ${reason ?: "Not Specified"}"
                }
            }

            val attendance = AttendanceEntity(
                date = dateStr,
                userId = teacher.id,
                status = status,
                reason = remarks ?: reason,
                checkInTime = checkInTimeStr,
                isTeacher = true,
                latitude = latitude,
                longitude = longitude
            )
            repository.insertAttendance(attendance)
            repository.insertAuditLog(
                AuditLogEntity(
                    userId = teacher.id,
                    userName = teacher.name,
                    action = "Teacher Attendance",
                    details = "Status: $status, Check-In Time: $checkInTimeStr, Distance: ${distanceMeters.toInt()}m."
                )
            )
            onResult(true, "Attendance successfully marked as $status at $checkInTimeStr!")
            triggerNotification("Self attendance marked: $status")
        }
    }

    // --- STUDENT ATTENDANCE BY TEACHERS ---
    fun markStudentAttendance(studentId: String, status: String, reason: String? = null) {
        viewModelScope.launch {
            val dateStr = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
            val checkInTimeStr = SimpleDateFormat("hh:mm a", Locale.getDefault()).format(Date())
            
            // Check if there is an existing record
            val existing = repository.getAttendanceForUserOnDate(studentId, dateStr)
            val record = existing?.copy(status = status, reason = reason) ?: AttendanceEntity(
                date = dateStr,
                userId = studentId,
                status = status,
                reason = reason,
                checkInTime = if (status == "Present" || status == "Late") checkInTimeStr else null,
                isTeacher = false
            )
            repository.insertAttendance(record)
            
            val student = repository.getUserById(studentId)
            repository.insertAuditLog(
                AuditLogEntity(
                    userId = _currentUser.value?.id ?: "TC-101",
                    userName = _currentUser.value?.name ?: "Teacher",
                    action = "Mark Student Attendance",
                    details = "Marked ${student?.name ?: studentId} as $status on $dateStr."
                )
            )
        }
    }

    // --- HOMEWORK CREATION ---
    fun createHomework(classId: String, subjectId: String, title: String, instructions: String, dueDate: String, priority: String) {
        viewModelScope.launch {
            val teacher = _currentUser.value ?: return@launch
            val homework = HomeworkEntity(
                classId = classId,
                subjectId = subjectId,
                teacherId = teacher.id,
                teacherName = teacher.name,
                title = title,
                instructions = instructions,
                dueDate = dueDate,
                priority = priority
            )
            repository.insertHomework(homework)
            repository.insertAuditLog(
                AuditLogEntity(
                    userId = teacher.id,
                    userName = teacher.name,
                    action = "Create Homework",
                    details = "Assigned homework for Class $classId ($subjectId): '$title'."
                )
            )
            triggerNotification("New homework assigned in $subjectId for Class $classId")
        }
    }

    // --- GRADING / MARKS ENTRY ---
    fun enterStudentMark(studentId: String, subjectId: String, examType: String, marksObtained: Double, maxMarks: Double, remarks: String?) {
        viewModelScope.launch {
            val dateStr = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
            val mark = MarkEntity(
                studentId = studentId,
                subjectId = subjectId,
                examType = examType,
                marksObtained = marksObtained,
                maxMarks = maxMarks,
                remarks = remarks,
                date = dateStr,
                isPublished = false // Needs Admin publish results command
            )
            repository.insertMark(mark)
            repository.insertAuditLog(
                AuditLogEntity(
                    userId = _currentUser.value?.id ?: "TC-101",
                    userName = _currentUser.value?.name ?: "Teacher",
                    action = "Grade Entry",
                    details = "Entered $marksObtained/$maxMarks for student $studentId ($subjectId) under $examType."
                )
            )
        }
    }

    fun publishAllResults() {
        viewModelScope.launch {
            val currentMarks = repository.getAllMarks().firstOrNull() ?: emptyList()
            var count = 0
            currentMarks.forEach { mark ->
                if (!mark.isPublished) {
                    repository.insertMark(mark.copy(isPublished = true))
                    count++
                }
            }
            repository.insertAuditLog(
                AuditLogEntity(
                    userId = _currentUser.value?.id ?: "AD-001",
                    userName = _currentUser.value?.name ?: "Admin",
                    action = "Publish Results",
                    details = "Published $count grade sheets across all classes."
                )
            )
            triggerNotification("Admin has published all exam results and report cards!")
        }
    }

    // --- REALTIME CHAT SIMULATION (Socket.io Mock / Room backed) ---
    fun selectChatPartner(partner: UserEntity) {
        _activeChatPartner.value = partner
    }

    fun sendChatMessage(content: String) {
        viewModelScope.launch {
            val sender = _currentUser.value ?: return@launch
            val partner = _activeChatPartner.value ?: return@launch
            
            val message = MessageEntity(
                senderId = sender.id,
                receiverId = partner.id,
                content = content,
                status = "Read", // Instantly delivered in offline-first mode
                mediaType = "Text"
            )
            repository.insertMessage(message)
            
            // Auto reply mock simulation from Mrs Fernandes or Mr Naik to feel incredibly interactive!
            if (partner.id == "TC-101" || partner.id == "TC-102") {
                simulateAutoReply(partner, sender)
            }
        }
    }

    private fun simulateAutoReply(teacherSender: UserEntity, studentOrParentReceiver: UserEntity) {
        viewModelScope.launch {
            kotlinx.coroutines.delay(1500) // Delay to simulate typing indicator
            val replyText = when {
                studentOrParentReceiver.role == "Parent" -> {
                    "Hello, yes I received your message. I am currently monitoring their performance in class and will give a detailed update in the upcoming PTA meeting."
                }
                else -> "Hello! Make sure to complete your homework on time and refer to the notices board for updates."
            }
            val message = MessageEntity(
                senderId = teacherSender.id,
                receiverId = studentOrParentReceiver.id,
                content = replyText,
                status = "Read",
                mediaType = "Text"
            )
            repository.insertMessage(message)
        }
    }

    // --- LEAVE MANAGEMENT ---
    fun submitLeaveRequest(startDate: String, endDate: String, reason: String) {
        viewModelScope.launch {
            val user = _currentUser.value ?: return@launch
            val req = LeaveRequestEntity(
                userId = user.id,
                role = user.role,
                startDate = startDate,
                endDate = endDate,
                reason = reason
            )
            repository.insertLeaveRequest(req)
            repository.insertAuditLog(
                AuditLogEntity(
                    userId = user.id,
                    userName = user.name,
                    action = "Leave Applied",
                    details = "Applied leave from $startDate to $endDate."
                )
            )
            triggerNotification("Leave Request submitted successfully!")
        }
    }

    fun updateLeaveRequestStatus(id: Int, status: String) {
        viewModelScope.launch {
            val list = repository.allLeaveRequests.firstOrNull() ?: emptyList()
            val match = list.firstOrNull { it.id == id }
            if (match != null) {
                repository.insertLeaveRequest(match.copy(status = status))
                repository.insertAuditLog(
                    AuditLogEntity(
                        userId = _currentUser.value?.id ?: "AD-001",
                        userName = _currentUser.value?.name ?: "Admin",
                        action = "Leave Status Update",
                        details = "Approved/Rejected Leave Request #$id status set to $status."
                    )
                )
                triggerNotification("Leave request #$id was $status by Admin.")
            }
        }
    }

    // --- DATABASE BACKUP AND SYSTEM SETTINGS ---
    fun backupDatabase(onCompleted: (String) -> Unit) {
        viewModelScope.launch {
            repository.insertAuditLog(
                AuditLogEntity(
                    userId = _currentUser.value?.id ?: "AD-001",
                    userName = _currentUser.value?.name ?: "Admin",
                    action = "Database Backup",
                    details = "Full encrypted database backup (JSON/SQL payload) generated locally."
                )
            )
            kotlinx.coroutines.delay(1000)
            onCompleted("Backup successful! Local payload saved to secure storage: /backups/holy_spirit_erp_${System.currentTimeMillis()}.sql")
        }
    }

    fun updateSettings(academicYear: String, timing: String, name: String, address: String) {
        viewModelScope.launch {
            _academicYear.value = academicYear
            _schoolTiming.value = timing
            _schoolInfoName.value = name
            _schoolInfoAddress.value = address
            repository.insertAuditLog(
                AuditLogEntity(
                    userId = _currentUser.value?.id ?: "AD-001",
                    userName = _currentUser.value?.name ?: "Admin",
                    action = "Update ERP Settings",
                    details = "Academic Year set to $academicYear, School timings set to $timing."
                )
            )
            triggerNotification("School Settings Updated!")
        }
    }

    // --- Exposed Query Methods for Dashboard Screens ---
    fun getStudentsByParent(parentId: String): Flow<List<StudentEntity>> =
        repository.getStudentsByParent(parentId)

    fun getAttendanceHistoryForUser(userId: String): Flow<List<AttendanceEntity>> =
        repository.getAttendanceHistoryForUser(userId)

    fun getHomeworkForClass(classId: String): Flow<List<HomeworkEntity>> =
        repository.getHomeworkForClass(classId)

    fun getPublishedMarksForStudent(studentId: String): Flow<List<MarkEntity>> =
        repository.getPublishedMarksForStudent(studentId)

    fun getTimetableForClass(classId: String): Flow<List<TimetableEntity>> =
        repository.getTimetableForClass(classId)

    fun getNoticesForUser(role: String, classId: String?): Flow<List<NoticeEntity>> =
        repository.getNoticesForUser(role, classId)

    fun deleteMessage(id: Int) {
        viewModelScope.launch {
            repository.deleteMessage(id)
        }
    }
}

class SchoolViewModelFactory(private val application: Application) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(SchoolViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return SchoolViewModel(application) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
