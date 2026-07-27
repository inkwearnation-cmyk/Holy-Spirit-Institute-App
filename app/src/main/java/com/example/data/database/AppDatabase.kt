package com.example.data.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.example.data.dao.AppDao
import com.example.data.entity.*

@Database(
    entities = [
        UserEntity::class,
        TeacherEntity::class,
        StudentEntity::class,
        ParentEntity::class,
        ClassSectionEntity::class,
        SubjectEntity::class,
        TeacherAssignmentEntity::class,
        TimetableEntity::class,
        NoticeEntity::class,
        EventHolidayEntity::class,
        AttendanceEntity::class,
        HomeworkEntity::class,
        MarkEntity::class,
        MessageEntity::class,
        LeaveRequestEntity::class,
        AuditLogEntity::class
    ],
    version = 1,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun appDao(): AppDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "holy_spirit_erp_database"
                )
                .fallbackToDestructiveMigration()
                .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
