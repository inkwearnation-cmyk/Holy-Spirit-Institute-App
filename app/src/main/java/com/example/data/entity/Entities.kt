package com.example.data.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "users")
data class UserEntity(
    @PrimaryKey val id: String, // e.g., "AD-001", "TC-101", "ST-201", "PR-301"
    val name: String,
    val email: String,
    val passwordHash: String,
    val role: String, // "Admin", "Teacher", "Student", "Parent"
    val phone: String,
    val address: String,
    val photoUrl: String,
    val status: String // "Active", "Inactive"
)

@Entity(tableName = "teachers")
data class TeacherEntity(
    @PrimaryKey val teacherId: String, // matches UserEntity.id
    val qualification: String,
    val experience: String, // e.g., "5 years"
    val joiningDate: String,
    val isClassTeacherOfClassId: String? = null // e.g. "5A", "6A"
)

@Entity(tableName = "students")
data class StudentEntity(
    @PrimaryKey val studentId: String, // matches UserEntity.id
    val admissionNumber: String,
    val rollNumber: Int,
    val dob: String,
    val gender: String,
    val bloodGroup: String,
    val classId: String, // e.g. "5A"
    val parentId: String, // matches ParentEntity.parentId
    val emergencyContact: String,
    val admissionDate: String
)

@Entity(tableName = "parents")
data class ParentEntity(
    @PrimaryKey val parentId: String, // matches UserEntity.id
    val motherName: String,
    val occupation: String
)

@Entity(tableName = "class_sections")
data class ClassSectionEntity(
    @PrimaryKey val id: String, // e.g. "5A", "6A", "8B"
    val className: String, // e.g. "Class 5"
    val sectionName: String // e.g. "A"
)

@Entity(tableName = "subjects")
data class SubjectEntity(
    @PrimaryKey val id: String, // e.g. "ENG", "HIS", "MAT"
    val name: String
)

@Entity(tableName = "teacher_assignments")
data class TeacherAssignmentEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val teacherId: String,
    val classId: String, // e.g. "5A"
    val subjectId: String // e.g. "ENG"
)

@Entity(tableName = "timetable")
data class TimetableEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val classId: String,
    val dayOfWeek: String, // "Monday", "Tuesday", etc.
    val period: Int, // 1 to 8
    val subjectId: String,
    val teacherId: String,
    val startTime: String, // "08:15"
    val endTime: String // "09:00"
)

@Entity(tableName = "notices")
data class NoticeEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val senderId: String,
    val senderName: String,
    val targetType: String, // "Everyone", "Teachers", "Students", "Parents", "SpecificClass"
    val targetClassId: String?, // e.g. "5A"
    val title: String,
    val content: String,
    val date: String, // "YYYY-MM-DD"
    val timestamp: Long = System.currentTimeMillis()
)

@Entity(tableName = "events_holidays")
data class EventHolidayEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val title: String,
    val description: String,
    val date: String, // "YYYY-MM-DD"
    val type: String // "EVENT", "HOLIDAY"
)

@Entity(tableName = "attendance")
data class AttendanceEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val date: String, // "YYYY-MM-DD"
    val userId: String, // Student or Teacher ID
    val status: String, // "Present", "Absent", "Leave", "Half Day", "Late"
    val reason: String? = null, // reason for leave or late
    val checkInTime: String? = null, // "07:54 AM"
    val isTeacher: Boolean = false,
    val latitude: Double? = null,
    val longitude: Double? = null
)

@Entity(tableName = "homework")
data class HomeworkEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val classId: String,
    val subjectId: String,
    val teacherId: String,
    val teacherName: String,
    val title: String,
    val instructions: String,
    val dueDate: String, // "YYYY-MM-DD"
    val priority: String, // "High", "Medium", "Low"
    val attachmentUri: String? = null,
    val timestamp: Long = System.currentTimeMillis()
)

@Entity(tableName = "marks")
data class MarkEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val studentId: String,
    val subjectId: String,
    val examType: String, // "Unit Test", "Mid Term", "Final", "Internal", "External"
    val marksObtained: Double,
    val maxMarks: Double,
    val remarks: String? = null,
    val date: String,
    val isPublished: Boolean = false
)

@Entity(tableName = "messages")
data class MessageEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val senderId: String,
    val receiverId: String,
    val content: String,
    val timestamp: Long = System.currentTimeMillis(),
    val status: String = "Sent", // "Sent", "Delivered", "Read"
    val mediaType: String = "Text", // "Text", "Image", "PDF"
    val mediaUrl: String? = null
)

@Entity(tableName = "leave_requests")
data class LeaveRequestEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val userId: String,
    val role: String,
    val startDate: String,
    val endDate: String,
    val reason: String,
    val status: String = "Pending" // "Pending", "Approved", "Rejected"
)

@Entity(tableName = "audit_logs")
data class AuditLogEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val userId: String,
    val userName: String,
    val action: String,
    val details: String,
    val timestamp: Long = System.currentTimeMillis()
)
