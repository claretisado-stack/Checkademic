package com.example.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.data.model.AcademicTask
import com.example.data.model.SubjectEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface TaskDao {
    @Query("SELECT * FROM academic_tasks ORDER BY isCompleted ASC, dueDateMillis ASC")
    fun observeAllTasks(): Flow<List<AcademicTask>>

    @Query("SELECT * FROM academic_tasks WHERE id = :id LIMIT 1")
    suspend fun getTaskById(id: Long): AcademicTask?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTask(task: AcademicTask): Long

    @Update
    suspend fun updateTask(task: AcademicTask)

    @Query("UPDATE academic_tasks SET isCompleted = :completed, completedAtMillis = :completedAtMillis WHERE id = :id")
    suspend fun setTaskCompleted(id: Long, completed: Boolean, completedAtMillis: Long?)

    @Query("UPDATE academic_tasks SET subjectName = :newName, subjectColorIndex = :newColorIndex WHERE subjectId = :subjectId")
    suspend fun syncSubjectMetadataForTasks(subjectId: Long, newName: String, newColorIndex: Int)

    @Query("DELETE FROM academic_tasks WHERE id = :id")
    suspend fun deleteTaskById(id: Long)
}

@Dao
interface SubjectDao {
    @Query("SELECT * FROM subjects ORDER BY name COLLATE NOCASE ASC")
    fun observeAllSubjects(): Flow<List<SubjectEntity>>

    @Query("SELECT * FROM subjects WHERE LOWER(name) = LOWER(:name) LIMIT 1")
    suspend fun getSubjectByName(name: String): SubjectEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSubject(subject: SubjectEntity): Long

    @Update
    suspend fun updateSubject(subject: SubjectEntity)

    @Query("DELETE FROM subjects WHERE id = :id")
    suspend fun deleteSubjectById(id: Long)
}
