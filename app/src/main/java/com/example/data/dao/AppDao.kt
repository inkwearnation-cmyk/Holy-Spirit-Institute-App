package com.example.data.dao

import androidx.room.*
import com.example.data.entity.*
import kotlinx.coroutines.flow.Flow

@Dao
interface AppDao {

    // --- USERS ---
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertUser(user: UserEntity)

    @Update
    suspend fun updateUser(user: UserEntity)

    @Query("SELECT * FROM users WHERE id = :id LIMIT 1")
    suspend fun getUserById(id: String): UserEntity?

    @Query("SELECT * FROM users WHERE id = :id LIMIT 1")
    fun getUserByIdFlow(id: String): Flow<UserEntity?>

    @Query("SELECT * FROM users WHERE email = :email LIMIT 1")
    suspend fun getUserByEmail(email: String): UserEntity?

    @Query("SELECT * FROM users WHERE role = :role")
    fun getUsersByRoleFlow(role: String): Flow<List<UserEntity>>

    @Query("SELECT * FROM users")
    fun getAllUsersFlow(): Flow<List<UserEntity>>

    @Query("DELETE FROM users WHERE id = :id")
    suspend fun deleteUserById(id: String)


    // --- TEACHERS ---
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTeacher(teacher: TeacherEntity)

    @Query("SELECT * FROM teachers WHERE teacherId = :teacherId LIMIT 1")
    suspend fun getTeacherById(teacherId: String): TeacherEntity?

    @Query("SELECT * FROM teachers")
    fun getAllTeachersFlow(): Flow<List<TeacherEntity>>


    // --- STUDENTS ---
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertStudent(student: StudentEntity)

    @Query("SELECT * FROM students WHERE studentId = :studentId LIMIT 1")
    suspend fun getStudentById(studentId: String): StudentEntity?

    @Query("SELECT * FROM students WHERE classId = :classId")
    fun getStudentsByClassFlow(classId: String): Flow<List<StudentEntity>>

    @Query("SELECT * FROM students WHERE parentId = :parentId")
    fun getStudentsByParentFlow(parentId: String): Flow<List<StudentEntity>>

    @Query("SELECT * FROM students")
    fun getAllStudentsFlow(): Flow<List<StudentEntity>>


    // --- PARENTS ---
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertParent(parent: ParentEntity)

    @Query("SELECT * FROM parents WHERE parentId = :parentId LIMIT 1")
    suspend fun getParentById(parentId: String): ParentEntity?

    @Query("SELECT * FROM parents")
    fun getAllParentsFlow(): Flow<List<ParentEntity>>


    // --- CLASSES & SECTIONS ---
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertClassSection(classSection: ClassSectionEntity)

    @Query("SELECT * FROM class_sections")
    fun getAllClassSectionsFlow(): Flow<List<ClassSectionEntity>>


    // --- SUBJECTS ---
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSubject(subject: SubjectEntity)

    @Query("SELECT * FROM subjects")
    fun getAllSubjectsFlow(): Flow<List<SubjectEntity>>


    // --- TEACHER ASSIGNMENTS ---
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTeacherAssignment(assignment: TeacherAssignmentEntity)

    @Query("SELECT * FROM teacher_assignments")
    fun getAllTeacherAssignmentsFlow(): Flow<List<TeacherAssignmentEntity>>

    @Query("SELECT * FROM teacher_assignments WHERE teacherId = :teacherId")
    fun getTeacherAssignmentsFlow(teacherId: String): Flow<List<TeacherAssignmentEntity>>

    @Query("DELETE FROM teacher_assignments WHERE id = :id")
    suspend fun deleteTeacherAssignment(id: Int)


    // --- TIMETABLE ---
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTimetable(timetable: TimetableEntity)

    @Query("SELECT * FROM timetable WHERE classId = :classId")
    fun getTimetableForClassFlow(classId: String): Flow<List<TimetableEntity>>

    @Query("SELECT * FROM timetable WHERE teacherId = :teacherId")
    fun getTimetableForTeacherFlow(teacherId: String): Flow<List<TimetableEntity>>

    @Query("DELETE FROM timetable WHERE classId = :classId")
    suspend fun clearTimetableForClass(classId: String)


    // --- NOTICES ---
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertNotice(notice: NoticeEntity)

    @Query("SELECT * FROM notices ORDER BY timestamp DESC")
    fun getAllNoticesFlow(): Flow<List<NoticeEntity>>

    @Query("SELECT * FROM notices WHERE targetType = 'Everyone' OR targetType = :role OR (targetType = 'SpecificClass' AND targetClassId = :classId) ORDER BY timestamp DESC")
    fun getNoticesForUserFlow(role: String, classId: String?): Flow<List<NoticeEntity>>


    // --- EVENTS & HOLIDAYS ---
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertEventHoliday(item: EventHolidayEntity)

    @Query("SELECT * FROM events_holidays ORDER BY date ASC")
    fun getAllEventsHolidaysFlow(): Flow<List<EventHolidayEntity>>

    @Query("DELETE FROM events_holidays WHERE id = :id")
    suspend fun deleteEventHolidayById(id: Int)


    // --- ATTENDANCE ---
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAttendance(attendance: AttendanceEntity)

    @Query("SELECT * FROM attendance WHERE userId = :userId AND date = :date LIMIT 1")
    suspend fun getAttendanceForUserOnDate(userId: String, date: String): AttendanceEntity?

    @Query("SELECT * FROM attendance WHERE userId = :userId ORDER BY date DESC")
    fun getAttendanceHistoryForUserFlow(userId: String): Flow<List<AttendanceEntity>>

    @Query("SELECT * FROM attendance WHERE date = :date")
    fun getAttendanceForDateFlow(date: String): Flow<List<AttendanceEntity>>


    // --- HOMEWORK ---
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertHomework(homework: HomeworkEntity)

    @Query("SELECT * FROM homework WHERE classId = :classId ORDER BY timestamp DESC")
    fun getHomeworkForClassFlow(classId: String): Flow<List<HomeworkEntity>>

    @Query("SELECT * FROM homework WHERE teacherId = :teacherId ORDER BY timestamp DESC")
    fun getHomeworkByTeacherFlow(teacherId: String): Flow<List<HomeworkEntity>>


    // --- MARKS ---
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMark(mark: MarkEntity)

    @Query("SELECT * FROM marks WHERE studentId = :studentId")
    fun getMarksForStudentFlow(studentId: String): Flow<List<MarkEntity>>

    @Query("SELECT * FROM marks WHERE studentId = :studentId AND isPublished = 1")
    fun getPublishedMarksForStudentFlow(studentId: String): Flow<List<MarkEntity>>

    @Query("SELECT * FROM marks WHERE studentId = :studentId AND subjectId = :subjectId")
    fun getMarksForStudentAndSubjectFlow(studentId: String, subjectId: String): Flow<List<MarkEntity>>

    @Query("SELECT * FROM marks")
    fun getAllMarksFlow(): Flow<List<MarkEntity>>


    // --- MESSAGES ---
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMessage(message: MessageEntity)

    @Query("SELECT * FROM messages WHERE (senderId = :userId1 AND receiverId = :userId2) OR (senderId = :userId2 AND receiverId = :userId1) ORDER BY timestamp ASC")
    fun getChatMessagesFlow(userId1: String, userId2: String): Flow<List<MessageEntity>>

    @Query("DELETE FROM messages WHERE id = :id")
    suspend fun deleteMessageById(id: Int)


    // --- LEAVE REQUESTS ---
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertLeaveRequest(request: LeaveRequestEntity)

    @Query("SELECT * FROM leave_requests WHERE userId = :userId ORDER BY id DESC")
    fun getLeaveRequestsForUserFlow(userId: String): Flow<List<LeaveRequestEntity>>

    @Query("SELECT * FROM leave_requests ORDER BY id DESC")
    fun getAllLeaveRequestsFlow(): Flow<List<LeaveRequestEntity>>


    // --- AUDIT LOGS ---
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAuditLog(log: AuditLogEntity)

    @Query("SELECT * FROM audit_logs ORDER BY timestamp DESC")
    fun getAllAuditLogsFlow(): Flow<List<AuditLogEntity>>
}
