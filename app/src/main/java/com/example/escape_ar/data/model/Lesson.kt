package com.example.escape_ar.data.model

import kotlinx.serialization.Serializable

@Serializable
data class Lesson(
    val id: String,
    val moduleId: String,
    val title: String,
    val description: String,
    val youtubeVideoId: String,
    val thumbnailUrl: String?,
    val durationMinutes: Int,
    val orderIndex: Int,
    val createdAt: String,
    val updatedAt: String
)

@Serializable
data class LessonFile(
    val id: String,
    val lessonId: String,
    val fileName: String,
    val fileUrl: String,
    val fileType: String, // "pdf", "pptx", "docx", "image"
    val fileSize: Long, // in bytes
    val uploadedAt: String
)

@Serializable
data class LessonProgress(
    val id: String,
    val userId: String,
    val lessonId: String,
    val completed: Boolean = false,
    val videoProgress: Float = 0f, // 0.0 to 1.0
    val lastWatchedAt: String?,
    val completedAt: String?
)

// Supabase response models
@Serializable
data class SupabaseLessonResponse(
    val id: String,
    val module_id: String,
    val title: String,
    val description: String,
    val youtube_video_id: String,
    val thumbnail_url: String?,
    val duration_minutes: Int,
    val order_index: Int,
    val created_at: String,
    val updated_at: String
) {
    fun toLesson() = Lesson(
        id = id,
        moduleId = module_id,
        title = title,
        description = description,
        youtubeVideoId = youtube_video_id,
        thumbnailUrl = thumbnail_url,
        durationMinutes = duration_minutes,
        orderIndex = order_index,
        createdAt = created_at,
        updatedAt = updated_at
    )
}

@Serializable
data class SupabaseLessonFileResponse(
    val id: String,
    val lesson_id: String,
    val file_name: String,
    val file_url: String,
    val file_type: String,
    val file_size: Long,
    val uploaded_at: String
) {
    fun toLessonFile() = LessonFile(
        id = id,
        lessonId = lesson_id,
        fileName = file_name,
        fileUrl = file_url,
        fileType = file_type,
        fileSize = file_size,
        uploadedAt = uploaded_at
    )
}

@Serializable
data class SupabaseLessonProgressResponse(
    val id: String,
    val user_id: String,
    val lesson_id: String,
    val completed: Boolean,
    val video_progress: Float,
    val last_watched_at: String?,
    val completed_at: String?
) {
    fun toLessonProgress() = LessonProgress(
        id = id,
        userId = user_id,
        lessonId = lesson_id,
        completed = completed,
        videoProgress = video_progress,
        lastWatchedAt = last_watched_at,
        completedAt = completed_at
    )
}
