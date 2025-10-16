package com.example.escape_ar.data.repository

import android.util.Log
import com.example.escape_ar.data.SupabaseConfig
import com.example.escape_ar.data.model.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class QuizRepository {
    private val supabase = SupabaseConfig.client
    
    suspend fun submitQuizResult(userId: String, module: String, score: Float, questionsAnswered: Int): Result<QuizProgress> = withContext(Dispatchers.IO) {
        try {
            // Mock implementation
            val mockProgress = QuizProgress(
                id = "mock-progress-${System.currentTimeMillis()}",
                userId = userId,
                module = module,
                score = score,
                completedAt = "2024-01-15T10:30:00Z",
                questionsAnswered = questionsAnswered,
                totalQuestions = 10
            )
            Result.success(mockProgress)
        } catch (e: Exception) {
            Log.e("QuizRepository", "Error submitting quiz result", e)
            Result.failure(e)
        }
    }
    
    suspend fun getUserQuizProgress(userId: String): Result<List<QuizProgress>> = withContext(Dispatchers.IO) {
        try {
            // Mock implementation
            val mockProgress = listOf(
                QuizProgress(
                    id = "1",
                    userId = userId,
                    module = "decantation",
                    score = 85.0f,
                    completedAt = "2024-01-10T14:20:00Z",
                    questionsAnswered = 10,
                    totalQuestions = 10
                ),
                QuizProgress(
                    id = "2",
                    userId = userId,
                    module = "organ_system",
                    score = 92.0f,
                    completedAt = "2024-01-12T16:45:00Z",
                    questionsAnswered = 10,
                    totalQuestions = 10
                )
            )
            Result.success(mockProgress)
        } catch (e: Exception) {
            Log.e("QuizRepository", "Error fetching user quiz progress", e)
            Result.failure(e)
        }
    }
    
    suspend fun getModuleProgress(userId: String, module: String): Result<QuizProgress?> = withContext(Dispatchers.IO) {
        try {
            // Mock implementation - return progress for specific module
            val mockProgress = if (module == "decantation") {
                QuizProgress(
                    id = "1",
                    userId = userId,
                    module = module,
                    score = 85.0f,
                    completedAt = "2024-01-10T14:20:00Z",
                    questionsAnswered = 10,
                    totalQuestions = 10
                )
            } else null
            
            Result.success(mockProgress)
        } catch (e: Exception) {
            Log.e("QuizRepository", "Error fetching module progress", e)
            Result.failure(e)
        }
    }
}
