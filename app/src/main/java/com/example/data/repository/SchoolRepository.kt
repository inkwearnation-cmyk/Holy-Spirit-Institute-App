package com.example.data.repository

import com.example.data.dao.AppDao
import com.example.data.entity.*
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.firstOrNull

class SchoolRepository(private val appDao: AppDao) {

    // --- Flows for Live Data ---
    val allUsers: Flow<List<UserEntity>> = appDao.getAllUsersFlow()
    val allTeachers: Flow<List<TeacherEntity>> = appDao.getAllTeachersFlow()
    val allStudents: Flow<List<StudentEntity>> = appDao.getAllStudentsFlow()
    val allParents: Flow<List<ParentEntity>> = appDao.getAllParentsFlow()
    val allClassSections: Flow<List<ClassSectionEntity>> = appDao.getAllClassSectionsFlow()
    val allSubjects: Flow<List<SubjectEntity>> = appDao.getAllSubjectsFlow()
    val allTeacherAssignments: Flow<List<TeacherAssignmentEntity>> = appDao.getAllTeacherAssignmentsFlow()
    val allNotices: Flow<List<NoticeEntity>> = appDao.getAllNoticesFlow()
    val allEventsHolidays: Flow<List<EventHolidayEntity>> = appDao.getAllEventsHolidaysFlow()
    val allLeaveRequests: Flow<List<LeaveRequestEntity>> = appDao.getAllLeaveRequestsFlow()
    val allAuditLogs: Flow<List<AuditLogEntity>> = appDao.getAllAuditLogsFlow()

    // --- User Methods ---
    suspend fun getUserById(id: String): UserEntity? = appDao.getUserById(id)
    fun getUserByIdFlow(id: String): Flow<UserEntity?> = appDao.getUserByIdFlow(id)
    suspend fun getUserByEmail(email: String): UserEntity? = appDao.getUserByEmail(email)
    suspend fun insertUser(user: UserEntity) = appDao.insertUser(user)
    suspend fun updateUser(user: UserEntity) = appDao.updateUser(user)
    suspend fun deleteUser(id: String) {
        appDao.deleteUserById(id)
    }

    // --- Role Entities ---
    suspend fun insertTeacher(teacher: TeacherEntity) = appDao.insertTeacher(teacher)
    suspend fun getTeacherById(id: String): TeacherEntity? = appDao.getTeacherById(id)
    
    suspend fun insertStudent(student: StudentEntity) = appDao.insertStudent(student)
    suspend fun getStudentById(id: String): StudentEntity? = appDao.getStudentById(id)
    fun getStudentsByClass(classId: String): Flow<List<StudentEntity>> = appDao.getStudentsByClassFlow(classId)
    fun getStudentsByParent(parentId: String): Flow<List<StudentEntity>> = appDao.getStudentsByParentFlow(parentId)

    suspend fun insertParent(parent: ParentEntity) = appDao.insertParent(parent)
    suspend fun getParentById(id: String): ParentEntity? = appDao.getParentById(id)

    // --- Attendance ---
    suspend fun insertAttendance(attendance: AttendanceEntity) = appDao.insertAttendance(attendance)
    suspend fun getAttendanceForUserOnDate(userId: String, date: String): AttendanceEntity? =
        appDao.getAttendanceForUserOnDate(userId, date)
    fun getAttendanceHistoryForUser(userId: String): Flow<List<AttendanceEntity>> =
        appDao.getAttendanceHistoryForUserFlow(userId)
    fun getAttendanceForDate(date: String): Flow<List<AttendanceEntity>> =
        appDao.getAttendanceForDateFlow(date)

    // --- Homework ---
    suspend fun insertHomework(homework: HomeworkEntity) = appDao.insertHomework(homework)
    fun getHomeworkForClass(classId: String): Flow<List<HomeworkEntity>> = appDao.getHomeworkForClassFlow(classId)
    fun getHomeworkByTeacher(teacherId: String): Flow<List<HomeworkEntity>> = appDao.getHomeworkByTeacherFlow(teacherId)

    // --- Marks ---
    suspend fun insertMark(mark: MarkEntity) = appDao.insertMark(mark)
    fun getMarksForStudent(studentId: String): Flow<List<MarkEntity>> = appDao.getMarksForStudentFlow(studentId)
    fun getPublishedMarksForStudent(studentId: String): Flow<List<MarkEntity>> = appDao.getPublishedMarksForStudentFlow(studentId)
    fun getMarksForStudentAndSubject(studentId: String, subjectId: String): Flow<List<MarkEntity>> =
        appDao.getMarksForStudentAndSubjectFlow(studentId, subjectId)
    fun getAllMarks(): Flow<List<MarkEntity>> = appDao.getAllMarksFlow()

    // --- Notices & Holidays ---
    suspend fun insertNotice(notice: NoticeEntity) = appDao.insertNotice(notice)
    fun getNoticesForUser(role: String, classId: String?): Flow<List<NoticeEntity>> =
        appDao.getNoticesForUserFlow(role, classId)
    
    suspend fun insertEventHoliday(item: EventHolidayEntity) = appDao.insertEventHoliday(item)
    suspend fun deleteEventHoliday(id: Int) = appDao.deleteEventHolidayById(id)

    // --- Timetables ---
    suspend fun insertTimetable(timetable: TimetableEntity) = appDao.insertTimetable(timetable)
    fun getTimetableForClass(classId: String): Flow<List<TimetableEntity>> = appDao.getTimetableForClassFlow(classId)
    fun getTimetableForTeacher(teacherId: String): Flow<List<TimetableEntity>> = appDao.getTimetableForTeacherFlow(teacherId)

    // --- Assignments ---
    suspend fun insertTeacherAssignment(assignment: TeacherAssignmentEntity) = appDao.insertTeacherAssignment(assignment)
    fun getTeacherAssignments(teacherId: String): Flow<List<TeacherAssignmentEntity>> = appDao.getTeacherAssignmentsFlow(teacherId)
    suspend fun deleteTeacherAssignment(id: Int) = appDao.deleteTeacherAssignment(id)

    // --- Messages / Chat ---
    suspend fun insertMessage(message: MessageEntity) = appDao.insertMessage(message)
    fun getChatMessages(userId1: String, userId2: String): Flow<List<MessageEntity>> = appDao.getChatMessagesFlow(userId1, userId2)
    suspend fun deleteMessage(id: Int) = appDao.deleteMessageById(id)

    // --- Leave Requests ---
    suspend fun insertLeaveRequest(request: LeaveRequestEntity) = appDao.insertLeaveRequest(request)
    fun getLeaveRequestsForUser(userId: String): Flow<List<LeaveRequestEntity>> = appDao.getLeaveRequestsForUserFlow(userId)

    // --- Audit Logs ---
    suspend fun insertAuditLog(log: AuditLogEntity) = appDao.insertAuditLog(log)

    // --- Class Sections & Subjects ---
    suspend fun insertClassSection(classSection: ClassSectionEntity) = appDao.insertClassSection(classSection)
    suspend fun insertSubject(subject: SubjectEntity) = appDao.insertSubject(subject)

    // --- Seeding Data ---
    suspend fun seedInitialDataIfEmpty() {
        val users = allUsers.firstOrNull() ?: emptyList()
        if (users.isNotEmpty()) return

        // Seed Class Sections
        val classes = listOf("5A", "6A", "8B", "9A")
        classes.forEach {
            appDao.insertClassSection(ClassSectionEntity(it, "Class ${it.dropLast(1)}", it.takeLast(1)))
        }

        // Seed Subjects
        val subjects = listOf(
            SubjectEntity("ENG", "English"),
            SubjectEntity("HIS", "History"),
            SubjectEntity("MAT", "Mathematics"),
            SubjectEntity("SCI", "Science"),
            SubjectEntity("GEO", "Geography")
        )
        subjects.forEach { appDao.insertSubject(it) }

        // Seed Users & Roles

        // 1. Admin
        val adminUser = UserEntity(
            id = "AD-001",
            name = "Fr. Lawrence D'Souza",
            email = "admin@school.com",
            passwordHash = "admin123", // For simplification of local testing
            role = "Admin",
            phone = "+91 98234 56789",
            address = "Holy Spirit Church Presbytery, Margao, Goa",
            photoUrl = "https://images.unsplash.com/photo-1544005313-94ddf0286df2",
            status = "Active"
        )
        appDao.insertUser(adminUser)

        // 2. Class Teacher - Mrs Fernandes
        val teacherUser1 = UserEntity(
            id = "TC-101",
            name = "Mrs. Maria Fernandes",
            email = "fernandes@school.com",
            passwordHash = "teacher123",
            role = "Teacher",
            phone = "+91 98901 23456",
            address = "Aquem, Margao, Goa",
            photoUrl = "https://images.unsplash.com/photo-1573496359142-b8d87734a5a2",
            status = "Active"
        )
        appDao.insertUser(teacherUser1)
        appDao.insertTeacher(
            TeacherEntity(
                teacherId = "TC-101",
                qualification = "M.A. English, B.Ed",
                experience = "12 Years",
                joiningDate = "2014-06-01",
                isClassTeacherOfClassId = "5A"
            )
        )

        // Teacher 2 - Mr. Naik (Math/Science)
        val teacherUser2 = UserEntity(
            id = "TC-102",
            name = "Mr. Ramesh Naik",
            email = "naik@school.com",
            passwordHash = "teacher123",
            role = "Teacher",
            phone = "+91 98221 23456",
            address = "Fatorda, Margao, Goa",
            photoUrl = "https://images.unsplash.com/photo-1560250097-0b93528c311a",
            status = "Active"
        )
        appDao.insertUser(teacherUser2)
        appDao.insertTeacher(
            TeacherEntity(
                teacherId = "TC-102",
                qualification = "M.Sc. Mathematics, B.Ed",
                experience = "8 Years",
                joiningDate = "2018-06-15",
                isClassTeacherOfClassId = null
            )
        )

        // 3. Parent - Mr. John D'Costa
        val parentUser = UserEntity(
            id = "PR-301",
            name = "Mr. John D'Costa",
            email = "parent@school.com",
            passwordHash = "parent123",
            role = "Parent",
            phone = "+91 98231 98765",
            address = "Gogol, Margao, Goa",
            photoUrl = "https://images.unsplash.com/photo-1472099645785-5658abf4ff4e",
            status = "Active"
        )
        appDao.insertUser(parentUser)
        appDao.insertParent(
            ParentEntity(
                parentId = "PR-301",
                motherName = "Mrs. Clara D'Costa",
                occupation = "Software Engineer"
            )
        )

        // 4. Student - Ethan D'Costa
        val studentUser = UserEntity(
            id = "ST-201",
            name = "Ethan D'Costa",
            email = "student@school.com",
            passwordHash = "student123",
            role = "Student",
            phone = "+91 98231 98765",
            address = "Gogol, Margao, Goa",
            photoUrl = "https://images.unsplash.com/photo-1503919545889-aef636e10ad4",
            status = "Active"
        )
        appDao.insertUser(studentUser)
        appDao.insertStudent(
            StudentEntity(
                studentId = "ST-201",
                admissionNumber = "HS-2026-084",
                rollNumber = 12,
                dob = "2015-04-12",
                gender = "Male",
                bloodGroup = "O+",
                classId = "5A",
                parentId = "PR-301",
                emergencyContact = "+91 98231 98765",
                admissionDate = "2021-06-05"
            )
        )

        // Seed Assignments: Mrs Fernandes teaches ENG and HIS to 5A, 6A, 8B
        val assignments = listOf(
            TeacherAssignmentEntity(teacherId = "TC-101", classId = "5A", subjectId = "ENG"),
            TeacherAssignmentEntity(teacherId = "TC-101", classId = "5A", subjectId = "HIS"),
            TeacherAssignmentEntity(teacherId = "TC-101", classId = "6A", subjectId = "ENG"),
            TeacherAssignmentEntity(teacherId = "TC-101", classId = "8B", subjectId = "ENG"),
            TeacherAssignmentEntity(teacherId = "TC-101", classId = "8B", subjectId = "HIS"),
            TeacherAssignmentEntity(teacherId = "TC-102", classId = "5A", subjectId = "MAT"),
            TeacherAssignmentEntity(teacherId = "TC-102", classId = "5A", subjectId = "SCI")
        )
        assignments.forEach { appDao.insertTeacherAssignment(it) }

        // Seed Notices
        appDao.insertNotice(
            NoticeEntity(
                senderId = "AD-001",
                senderName = "Fr. Lawrence D'Souza",
                targetType = "Everyone",
                targetClassId = null,
                title = "Welcome to Academic Year 2026-27",
                content = "Holy Spirit Institute welcomes back all students, teachers, and staff. We look forward to a successful academic year filled with excellence and growth.",
                date = "2026-06-01"
            )
        )
        appDao.insertNotice(
            NoticeEntity(
                senderId = "TC-101",
                senderName = "Mrs. Maria Fernandes",
                targetType = "SpecificClass",
                targetClassId = "5A",
                title = "English Poetry Recitation Competition",
                content = "Dear students of Class 5A, the poetry recitation competition is scheduled for next Monday. Prepare your selected poems accordingly.",
                date = "2026-07-15"
            )
        )

        // Seed Events and Holidays
        appDao.insertEventHoliday(
            EventHolidayEntity(
                title = "Feast of the Holy Spirit",
                description = "Annual feast celebration of the Holy Spirit Church and school patron saint.",
                date = "2026-05-24",
                type = "HOLIDAY"
            )
        )
        appDao.insertEventHoliday(
            EventHolidayEntity(
                title = "Science and Tech Exhibition",
                description = "Annual school science fair. Students of class 5th to 10th to exhibit projects.",
                date = "2026-08-10",
                type = "EVENT"
            )
        )
        appDao.insertEventHoliday(
            EventHolidayEntity(
                title = "Goa Revolution Day",
                description = "Regional state holiday observing the revolution movement.",
                date = "2026-06-18",
                type = "HOLIDAY"
            )
        )

        // Seed Timetable for Class 5A
        val timetables = listOf(
            TimetableEntity(classId = "5A", dayOfWeek = "Monday", period = 1, subjectId = "ENG", teacherId = "TC-101", startTime = "08:15", endTime = "09:00"),
            TimetableEntity(classId = "5A", dayOfWeek = "Monday", period = 2, subjectId = "MAT", teacherId = "TC-102", startTime = "09:00", endTime = "09:45"),
            TimetableEntity(classId = "5A", dayOfWeek = "Monday", period = 3, subjectId = "SCI", teacherId = "TC-102", startTime = "09:45", endTime = "10:30"),
            TimetableEntity(classId = "5A", dayOfWeek = "Monday", period = 4, subjectId = "HIS", teacherId = "TC-101", startTime = "11:00", endTime = "11:45"),
            
            TimetableEntity(classId = "5A", dayOfWeek = "Tuesday", period = 1, subjectId = "MAT", teacherId = "TC-102", startTime = "08:15", endTime = "09:00"),
            TimetableEntity(classId = "5A", dayOfWeek = "Tuesday", period = 2, subjectId = "ENG", teacherId = "TC-101", startTime = "09:00", endTime = "09:45"),
            TimetableEntity(classId = "5A", dayOfWeek = "Tuesday", period = 3, subjectId = "GEO", teacherId = "TC-102", startTime = "09:45", endTime = "10:30")
        )
        timetables.forEach { appDao.insertTimetable(it) }

        // Seed Homework
        appDao.insertHomework(
            HomeworkEntity(
                classId = "5A",
                subjectId = "ENG",
                teacherId = "TC-101",
                teacherName = "Mrs. Maria Fernandes",
                title = "Noun and Pronoun Worksheets",
                instructions = "Complete page 12 to 15 in the grammar workbook. Draw illustrative charts if needed.",
                dueDate = "2026-07-25",
                priority = "High"
            )
        )

        // Seed Marks for Ethan D'Costa
        val marks = listOf(
            MarkEntity(studentId = "ST-201", subjectId = "ENG", examType = "Mid Term", marksObtained = 42.5, maxMarks = 50.0, remarks = "Excellent reading skills", date = "2026-05-15", isPublished = true),
            MarkEntity(studentId = "ST-201", subjectId = "MAT", examType = "Mid Term", marksObtained = 39.0, maxMarks = 50.0, remarks = "Improved accuracy", date = "2026-05-16", isPublished = true),
            MarkEntity(studentId = "ST-201", subjectId = "SCI", examType = "Mid Term", marksObtained = 45.0, maxMarks = 50.0, remarks = "Outstanding scientist", date = "2026-05-17", isPublished = true),
            MarkEntity(studentId = "ST-201", subjectId = "HIS", examType = "Mid Term", marksObtained = 41.0, maxMarks = 50.0, remarks = "Good historical retention", date = "2026-05-18", isPublished = true)
        )
        marks.forEach { appDao.insertMark(it) }

        // Seed Audit log
        appDao.insertAuditLog(
            AuditLogEntity(
                userId = "AD-001",
                userName = "Fr. Lawrence D'Souza",
                action = "System Initialized",
                details = "Holy Spirit Institute ERP was successfully provisioned and default academic structures seeded."
            )
        )
    }
}
