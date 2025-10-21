package com.example.escape_ar.data.repository

import com.example.escape_ar.data.SupabaseConfig
import com.example.escape_ar.data.model.*
import io.github.jan.supabase.postgrest.from
import io.github.jan.supabase.postgrest.query.Columns
import io.github.jan.supabase.postgrest.query.Order
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class LessonRepository {
    private val supabase = SupabaseConfig.client

    /**
     * Get all lessons for a specific module
     */
    suspend fun getLessonsByModule(moduleId: String): List<Lesson> = withContext(Dispatchers.IO) {
        try {
            val response = supabase.from("lessons")
                .select(columns = Columns.ALL) {
                    filter {
                        eq("module_id", moduleId)
                    }
                    order(column = "order_index", order = Order.ASCENDING)
                }
                .decodeList<SupabaseLessonResponse>()
            
            response.map { it.toLesson() }
        } catch (e: Exception) {
            android.util.Log.e("LessonRepository", "Error fetching lessons for module $moduleId", e)
            emptyList()
        }
    }

    /**
     * Get a specific lesson by ID
     */
    suspend fun getLessonById(lessonId: String): Lesson? = withContext(Dispatchers.IO) {
        try {
            val response = supabase.from("lessons")
                .select(columns = Columns.ALL) {
                    filter {
                        eq("id", lessonId)
                    }
                }
                .decodeSingle<SupabaseLessonResponse>()
            
            response.toLesson()
        } catch (e: Exception) {
            android.util.Log.e("LessonRepository", "Error fetching lesson $lessonId", e)
            null
        }
    }

    /**
     * Get all files for a specific lesson
     */
    suspend fun getLessonFiles(lessonId: String): List<LessonFile> = withContext(Dispatchers.IO) {
        try {
            val response = supabase.from("lesson_files")
                .select(columns = Columns.ALL) {
                    filter {
                        eq("lesson_id", lessonId)
                    }
                }
                .decodeList<SupabaseLessonFileResponse>()
            
            response.map { it.toLessonFile() }
        } catch (e: Exception) {
            android.util.Log.e("LessonRepository", "Error fetching lesson files", e)
            emptyList()
        }
    }

    /**
     * Get user's progress for a specific lesson
     */
    suspend fun getLessonProgress(userId: String, lessonId: String): LessonProgress? = withContext(Dispatchers.IO) {
        try {
            val response = supabase.from("lesson_progress")
                .select(columns = Columns.ALL) {
                    filter {
                        eq("user_id", userId)
                        eq("lesson_id", lessonId)
                    }
                }
                .decodeSingleOrNull<SupabaseLessonProgressResponse>()
            
            response?.toLessonProgress()
        } catch (e: Exception) {
            android.util.Log.e("LessonRepository", "Error fetching lesson progress", e)
            null
        }
    }

    /**
     * Update video progress for a lesson
     */
    suspend fun updateVideoProgress(
        userId: String,
        lessonId: String,
        progress: Float
    ): Boolean = withContext(Dispatchers.IO) {
        try {
            // Check if progress record exists
            val existing = getLessonProgress(userId, lessonId)
            
            if (existing != null) {
                // Update existing record
                supabase.from("lesson_progress")
                    .update({
                        set("video_progress", progress)
                        set("last_watched_at", java.time.Instant.now().toString())
                        if (progress >= 0.95f) {
                            set("completed", true)
                            set("completed_at", java.time.Instant.now().toString())
                        }
                    }) {
                        filter {
                            eq("user_id", userId)
                            eq("lesson_id", lessonId)
                        }
                    }
            } else {
                // Create new record
                supabase.from("lesson_progress")
                    .insert(mapOf(
                        "user_id" to userId,
                        "lesson_id" to lessonId,
                        "video_progress" to progress,
                        "completed" to (progress >= 0.95f),
                        "last_watched_at" to java.time.Instant.now().toString(),
                        "completed_at" to if (progress >= 0.95f) java.time.Instant.now().toString() else null
                    ))
            }
            true
        } catch (e: Exception) {
            android.util.Log.e("LessonRepository", "Error updating video progress", e)
            false
        }
    }

    /**
     * Mark lesson as completed
     */
    suspend fun markLessonCompleted(userId: String, lessonId: String): Boolean = withContext(Dispatchers.IO) {
        try {
            val existing = getLessonProgress(userId, lessonId)
            
            if (existing != null) {
                supabase.from("lesson_progress")
                    .update({
                        set("completed", true)
                        set("video_progress", 1.0f)
                        set("completed_at", java.time.Instant.now().toString())
                    }) {
                        filter {
                            eq("user_id", userId)
                            eq("lesson_id", lessonId)
                        }
                    }
            } else {
                supabase.from("lesson_progress")
                    .insert(mapOf(
                        "user_id" to userId,
                        "lesson_id" to lessonId,
                        "completed" to true,
                        "video_progress" to 1.0f,
                        "completed_at" to java.time.Instant.now().toString()
                    ))
            }
            true
        } catch (e: Exception) {
            android.util.Log.e("LessonRepository", "Error marking lesson completed", e)
            false
        }
    }

    /**
     * Get all lessons with user progress
     */
    suspend fun getLessonsWithProgress(userId: String, moduleId: String): List<Pair<Lesson, LessonProgress?>> = 
        withContext(Dispatchers.IO) {
            try {
                val lessons = getLessonsByModule(moduleId)
                lessons.map { lesson ->
                    val progress = getLessonProgress(userId, lesson.id)
                    Pair(lesson, progress)
                }
            } catch (e: Exception) {
                android.util.Log.e("LessonRepository", "Error fetching lessons with progress", e)
                emptyList()
            }
        }
}
