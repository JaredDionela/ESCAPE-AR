package com.example.escape_ar.data.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * Extended user profile data model for the new features (video lessons, settings, etc.)
 * This extends the basic UserProfile with teacher and section information
 * MUST match all columns in the Supabase profiles table
 */
@Serializable
data class ExtendedUserProfile(
    @SerialName("id") val id: String,
    @SerialName("email") val email: String? = null,
    @SerialName("full_name") val fullName: String? = null,
    @SerialName("display_name") val displayName: String,
    @SerialName("teacher_name") val teacherName: String? = null,
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
 * User settings model matching Supabase user_settings table
 */
@Serializable
data class UserSettings(
    @SerialName("id") val id: String? = null,
    @SerialName("user_id") val userId: String,
    @SerialName("music_volume") val musicVolume: Float = 0.7f,
    @SerialName("sfx_volume") val sfxVolume: Float = 0.7f,
    @SerialName("captions_enabled") val captionsEnabled: Boolean = true,
    @SerialName("updated_at") val updatedAt: String? = null
) {
    companion object {
        val DEFAULT = UserSettings(
            userId = "",
            musicVolume = 0.7f,
            sfxVolume = 0.7f,
            captionsEnabled = true
        )
    }
}

/**
 * Request model for updating user profile
 */
@Serializable
data class UpdateProfileRequest(
    @SerialName("display_name") val displayName: String,
    @SerialName("teacher_name") val teacherName: String? = null,
    @SerialName("section") val section: String? = null
)

/**
 * Request model for updating user settings
 */
@Serializable
data class UpdateSettingsRequest(
    @SerialName("music_volume") val musicVolume: Float,
    @SerialName("sfx_volume") val sfxVolume: Float,
    @SerialName("captions_enabled") val captionsEnabled: Boolean
)
