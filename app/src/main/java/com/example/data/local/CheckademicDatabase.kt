package com.example.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.example.data.model.AcademicTask
import com.example.data.model.SubjectEntity

@Database(
    entities = [AcademicTask::class, SubjectEntity::class],
    version = 1,
    exportSchema = false,
)
@TypeConverters(AcademicConverters::class)
abstract class CheckademicDatabase : RoomDatabase() {
    abstract fun taskDao(): TaskDao
    abstract fun subjectDao(): SubjectDao

    companion object {
        @Volatile
        private var INSTANCE: CheckademicDatabase? = null

        fun getInstance(context: Context): CheckademicDatabase {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: Room.databaseBuilder(
                    context.applicationContext,
                    CheckademicDatabase::class.java,
                    "checkademic.db"
                ).build().also { INSTANCE = it }
            }
        }
    }
}
