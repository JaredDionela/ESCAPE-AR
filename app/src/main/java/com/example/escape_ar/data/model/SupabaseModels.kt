package com.example.escape_ar.data.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * Extended user profile data model for the new features (video lessons, settings, etc.)
 * This extends the basic UserProfile with teacher and section information
 * MUST match all columns in the Supabase profiles table
 * 
 * NEW SCHEMA: Uses teacher_id (foreign key) instead of teacher_name (text)
 * - role: 'student' or 'teacher'
 * - teacher_id: UUID foreign key to profiles table (for students)
 * - teacher_name: Deprecated (kept for backwards compatibility)
 */
@Serializable
data class ExtendedUserProfile(
    @SerialName("id") val id: String,
    @SerialName("email") val email: String? = null,
    @SerialName("full_name") val fullName: String? = null,
    @SerialName("display_name") val displayName: String,
    @SerialName("role") val role: String? = null, // 'student' or 'teacher'
    @SerialName("teacher_id") val teacherId: String? = null, // UUID - foreign key to teacher's profile
    @SerialName("teacher_name") val teacherName: String? = null, // Deprecated - use teacher_id + JOIN instead
    @SerialName("section") val section: String? = null,
    @SerialName("avatar_url") val avatarUrl: String? = null,
    @SerialName("created_at") val createdAt: String? = null,
    @SerialName("updated_at") val updatedAt: String? = null
)

/**
 * Video lesson data model matching Supabase videos table
 */
@Serializable
data class Video(
    @SerialName("id") val id: String,
    @SerialName("topic") val topic: String, // decantation, organ_system, simple_machines, solar_system
    @SerialName("title") val title: String,
    @SerialName("youtube_id") val youtubeId: String,
    @SerialName("thumbnail_url") val thumbnailUrl: String? = null,
    @SerialName("duration_seconds") val durationSeconds: Int? = null,
    @SerialName("summary") val summary: String? = null,
    @SerialName("created_at") val createdAt: String? = null
)

/**
 * DepEd lesson data model matching Supabase deped_lessons table
 */
@Serializable
data class DepEdLesson(
    @SerialName("id") val id: String,
    @SerialName("topic") val topic: String,
    @SerialName("short_summary") val shortSummary: String,
    @SerialName("full_text") val fullText: String? = null,
    @SerialName("resource_url") val resourceUrl: String? = null,
    @SerialName("visual_elements") val visualElements: String? = null, // JSON string
    @SerialName("created_at") val createdAt: String? = null,
    @SerialName("updated_at") val updatedAt: String? = null
)

/**
 * Video progress tracking model matching Supabase video_progress table
 */
@Serializable
data class VideoProgress(
    @SerialName("id") val id: String? = null,
    @SerialName("user_id") val userId: String,
    @SerialName("video_id") val videoId: String,
    @SerialName("watched_seconds") val watchedSeconds: Int = 0,
    @SerialName("completed") val completed: Boolean = false,
    @SerialName("updated_at") val updatedAt: String? = null
)

/**
 * Request model for updating user profile
 * NEW SCHEMA: Uses teacher_id instead of teacher_name
 */
@Serializable
data class UpdateProfileRequest(
    @SerialName("display_name") val displayName: String,
    @SerialName("teacher_id") val teacherId: String? = null, // UUID of selected teacher
    @SerialName("section") val section: String? = null
)

/**
 * Profile with teacher info (from JOIN query)
 * Used to display student's profile with their teacher's name
 */
@Serializable
data class ProfileWithTeacher(
    @SerialName("id") val id: String,
    @SerialName("email") val email: String? = null,
    @SerialName("display_name") val displayName: String,
    @SerialName("role") val role: String? = null,
    @SerialName("teacher_id") val teacherId: String? = null,
    @SerialName("section") val section: String? = null,
    @SerialName("teacher") val teacher: TeacherInfo? = null // Nested teacher object from JOIN
)

/**
 * Teacher information from JOIN query
 */
@Serializable
data class TeacherInfo(
    @SerialName("display_name") val displayName: String
)
