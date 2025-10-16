package com.example.escape_ar.data.repository

import android.util.Log
import com.example.escape_ar.data.model.*
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.gotrue.auth
import io.github.jan.supabase.postgrest.from
import io.github.jan.supabase.postgrest.query.Columns
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
 * Repository for managing video lessons, DepEd lessons, and video progress
 */
class VideoRepository(private val supabase: SupabaseClient) {

    companion object {
        private const val TAG = "VideoRepository"
        private const val TABLE_VIDEOS = "videos"
        private const val TABLE_DEPED_LESSONS = "deped_lessons"
        private const val TABLE_VIDEO_PROGRESS = "video_progress"
    }

    /**
     * Get all videos for a specific topic
     */
    suspend fun getVideosByTopic(topic: String): Result<List<Video>> = withContext(Dispatchers.IO) {
        try {
            val videos = supabase.from(TABLE_VIDEOS)
                .select {
                    filter {
                        eq("topic", topic)
                    }
                }
                .decodeList<Video>()
            
            Log.d(TAG, "Fetched ${videos.size} videos for topic: $topic")
            Result.success(videos)
        } catch (e: Exception) {
            Log.e(TAG, "Error fetching videos for topic $topic", e)
            Result.failure(e)
        }
    }

    /**
     * Get all available videos
     */
    suspend fun getAllVideos(): Result<List<Video>> = withContext(Dispatchers.IO) {
        try {
            val videos = supabase.from(TABLE_VIDEOS)
                .select()
                .decodeList<Video>()
            
            Log.d(TAG, "Fetched ${videos.size} total videos")
            Result.success(videos)
        } catch (e: Exception) {
            Log.e(TAG, "Error fetching all videos", e)
            Result.failure(e)
        }
    }

    /**
     * Get DepEd lesson for a specific topic
     */
    suspend fun getDepEdLessonByTopic(topic: String): Result<DepEdLesson?> = withContext(Dispatchers.IO) {
        try {
            val lessons = supabase.from(TABLE_DEPED_LESSONS)
                .select {
                    filter {
                        eq("topic", topic)
                    }
                }
                .decodeList<DepEdLesson>()
            
            val lesson = lessons.firstOrNull()
            Log.d(TAG, "Fetched DepEd lesson for topic: $topic")
            Result.success(lesson)
        } catch (e: Exception) {
            Log.e(TAG, "Error fetching DepEd lesson for topic $topic", e)
            Result.failure(e)
        }
    }

    /**
     * Get user's video progress for a specific video
     */
    suspend fun getVideoProgress(videoId: String): Result<VideoProgress?> = withContext(Dispatchers.IO) {
        try {
            val userId = supabase.auth.currentUserOrNull()?.id
                ?: return@withContext Result.failure(Exception("User not authenticated"))

            val progressList = supabase.from(TABLE_VIDEO_PROGRESS)
                .select {
                    filter {
                        eq("user_id", userId)
                        eq("video_id", videoId)
                    }
                }
                .decodeList<VideoProgress>()
            
            val progress = progressList.firstOrNull()
            Log.d(TAG, "Fetched video progress for video: $videoId")
            Result.success(progress)
        } catch (e: Exception) {
            Log.e(TAG, "Error fetching video progress for $videoId", e)
            Result.failure(e)
        }
    }

    /**
     * Update or insert video progress
     */
    suspend fun updateVideoProgress(
        videoId: String,
        watchedSeconds: Int,
        completed: Boolean = false
    ): Result<VideoProgress> = withContext(Dispatchers.IO) {
        try {
            val userId = supabase.auth.currentUserOrNull()?.id
                ?: return@withContext Result.failure(Exception("User not authenticated"))

            // Check if progress already exists
            val existingProgress = getVideoProgress(videoId).getOrNull()

            val progress = if (existingProgress != null) {
                // Update existing progress
                val updated = mapOf(
                    "watched_seconds" to watchedSeconds,
                    "completed" to completed
                )
                
                supabase.from(TABLE_VIDEO_PROGRESS)
                    .update(updated) {
                        filter {
                            eq("user_id", userId)
                            eq("video_id", videoId)
                        }
                    }
                
                existingProgress.copy(
                    watchedSeconds = watchedSeconds,
                    completed = completed
                )
            } else {
                // Insert new progress
                val newProgress = VideoProgress(
                    userId = userId,
                    videoId = videoId,
                    watchedSeconds = watchedSeconds,
                    completed = completed
                )
                
                supabase.from(TABLE_VIDEO_PROGRESS)
                    .insert(newProgress)
                    .decodeSingle<VideoProgress>()
            }

            Log.d(TAG, "Updated video progress for video: $videoId (watched: $watchedSeconds, completed: $completed)")
            Result.success(progress)
        } catch (e: Exception) {
            Log.e(TAG, "Error updating video progress for $videoId", e)
            Result.failure(e)
        }
    }

    /**
     * Mark video as completed
     */
    suspend fun markVideoComplete(videoId: String): Result<VideoProgress> {
        return updateVideoProgress(videoId, 0, true)
    }

    /**
     * Get all video progress for current user
     */
    suspend fun getAllUserVideoProgress(): Result<List<VideoProgress>> = withContext(Dispatchers.IO) {
        try {
            val userId = supabase.auth.currentUserOrNull()?.id
                ?: return@withContext Result.failure(Exception("User not authenticated"))

            val progressList = supabase.from(TABLE_VIDEO_PROGRESS)
                .select {
                    filter {
                        eq("user_id", userId)
                    }
                }
                .decodeList<VideoProgress>()
            
            Log.d(TAG, "Fetched ${progressList.size} video progress entries for user")
            Result.success(progressList)
        } catch (e: Exception) {
            Log.e(TAG, "Error fetching user video progress", e)
            Result.failure(e)
        }
    }

    /**
     * Get videos with progress for a topic
     */
    suspend fun getVideosWithProgress(topic: String): Result<List<Pair<Video, VideoProgress?>>> = withContext(Dispatchers.IO) {
        try {
            val videosResult = getVideosByTopic(topic)
            val progressResult = getAllUserVideoProgress()

            if (videosResult.isFailure || progressResult.isFailure) {
                return@withContext Result.failure(
                    videosResult.exceptionOrNull() ?: progressResult.exceptionOrNull() 
                    ?: Exception("Unknown error")
                )
            }

            val videos = videosResult.getOrNull() ?: emptyList()
            val progressList = progressResult.getOrNull() ?: emptyList()
            val progressMap = progressList.associateBy { it.videoId }

            val videosWithProgress = videos.map { video ->
                video to progressMap[video.id]
            }

            Log.d(TAG, "Fetched ${videosWithProgress.size} videos with progress for topic: $topic")
            Result.success(videosWithProgress)
        } catch (e: Exception) {
            Log.e(TAG, "Error fetching videos with progress for topic $topic", e)
            Result.failure(e)
        }
    }
}
