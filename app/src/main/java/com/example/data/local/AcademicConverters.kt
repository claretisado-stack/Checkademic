package com.example.data.local

import androidx.room.TypeConverter
import com.example.data.model.TaskCategory
import com.example.data.model.TaskPriority

class AcademicConverters {
    @TypeConverter
    fun fromTaskCategory(category: TaskCategory): String = category.name

    @TypeConverter
    fun toTaskCategory(value: String): TaskCategory =
        TaskCategory.entries.firstOrNull { it.name == value } ?: TaskCategory.HOMEWORK

    @TypeConverter
    fun fromTaskPriority(priority: TaskPriority): String = priority.name

    @TypeConverter
    fun toTaskPriority(value: String): TaskPriority =
        TaskPriority.entries.firstOrNull { it.name == value } ?: TaskPriority.MEDIUM
}
