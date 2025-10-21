package com.example.escape_ar.data.repository

import android.util.Log
import com.example.escape_ar.data.SupabaseConfig
import com.example.escape_ar.data.model.*
import io.github.jan.supabase.postgrest.from
import io.github.jan.supabase.postgrest.query.Order
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class QuizRepository {
    private val supabase = SupabaseConfig.client
    
    /**
     * Fetch quiz questions for a specific module from Supabase
     */
    suspend fun getQuizQuestionsByModule(moduleId: String): Result<List<QuizQuestion>> = withContext(Dispatchers.IO) {
        try {
            Log.d("QuizRepository", "========================================")
            Log.d("QuizRepository", "Fetching quiz questions for module: $moduleId")
            Log.d("QuizRepository", "Supabase URL: ${supabase.supabaseUrl}")
            Log.d("QuizRepository", "========================================")
            
            val questions = supabase
                .from("quiz_questions")
                .select()
                {
                    filter {
                        eq("module_id", moduleId)
                    }
                    order(column = "order_index", order = Order.ASCENDING)
                }
                .decodeList<QuizQuestion>()
            
            Log.d("QuizRepository", "========================================")
            Log.d("QuizRepository", "Successfully fetched ${questions.size} questions for module: $moduleId")
            Log.d("QuizRepository", "========================================")
            
            if (questions.isEmpty()) {
                Log.w("QuizRepository", "⚠️ NO QUESTIONS FOUND! Troubleshooting:")
                Log.w("QuizRepository", "1. Check if questions exist in database for module_id: '$moduleId'")
                Log.w("QuizRepository", "2. Check RLS policies allow reading quiz_questions")
                Log.w("QuizRepository", "3. Module ID must match EXACTLY (case-sensitive): '$moduleId'")
                Log.w("QuizRepository", "4. Run this query in Supabase: SELECT * FROM quiz_questions WHERE module_id = '$moduleId';")
            } else {
                Log.d("QuizRepository", "✅ Question IDs: ${questions.map { it.id }}")
                Log.d("QuizRepository", "✅ First question: ${questions.firstOrNull()?.questionText}")
            }
            
            Result.success(questions)
        } catch (e: Exception) {
            Log.e("QuizRepository", "========================================")
            Log.e("QuizRepository", "❌ ERROR fetching quiz questions for module: $moduleId", e)
            Log.e("QuizRepository", "❌ Error message: ${e.message}")
            Log.e("QuizRepository", "❌ Error type: ${e.javaClass.simpleName}")
            Log.e("QuizRepository", "❌ Stack trace: ${e.stackTraceToString()}")
            Log.e("QuizRepository", "========================================")
            Result.failure(e)
        }
    }
    
    /**
     * Fetch all quiz questions from Supabase
     */
    suspend fun getAllQuizQuestions(): Result<List<QuizQuestion>> = withContext(Dispatchers.IO) {
        try {
            val questions = supabase
                .from("quiz_questions")
                .select()
                {
                    order(column = "module_id", order = Order.ASCENDING)
                    order(column = "order_index", order = Order.ASCENDING)
                }
                .decodeList<QuizQuestion>()
            
            Log.d("QuizRepository", "Fetched ${questions.size} total questions")
            Result.success(questions)
        } catch (e: Exception) {
            Log.e("QuizRepository", "Error fetching all quiz questions", e)
            Result.failure(e)
        }
    }
    
    /**
     * Submit a quiz result for a single question
     */
    suspend fun submitQuizAnswer(
        userId: String,
        questionId: String,
        moduleId: String,
        selectedAnswer: String,
        isCorrect: Boolean
    ): Result<QuizResult> = withContext(Dispatchers.IO) {
        try {
            val result = QuizResult(
                userId = userId,
                questionId = questionId,
                moduleId = moduleId,
                selectedAnswer = selectedAnswer,
                isCorrect = isCorrect
            )
            
            val inserted = supabase
                .from("quiz_results")
                .insert(result)
                .decodeSingle<QuizResult>()
            
            Log.d("QuizRepository", "Submitted quiz answer for question: $questionId")
            Result.success(inserted)
        } catch (e: Exception) {
            Log.e("QuizRepository", "Error submitting quiz answer", e)
            Result.failure(e)
        }
    }
    
    /**
     * Submit complete quiz result with flexible scoring
     * This saves a summary result with total score, regardless of question count
     */
    suspend fun submitQuizCompletion(
        userId: String,
        moduleId: String,
        correctAnswers: Int,
        totalQuestions: Int
    ): Result<QuizResult> = withContext(Dispatchers.IO) {
        try {
            val scorePercentage = if (totalQuestions > 0) {
                (correctAnswers.toFloat() / totalQuestions.toFloat()) * 100f
            } else {
                0f
            }
            
            val result = QuizResult(
                userId = userId,
                moduleId = moduleId,
                scorePercentage = scorePercentage,
                totalQuestions = totalQuestions,
                correctAnswers = correctAnswers,
                isCorrect = correctAnswers >= (totalQuestions / 2) // Pass if >= 50%
            )
            
            val inserted = supabase
                .from("quiz_results")
                .insert(result)
                .decodeSingle<QuizResult>()
            
            Log.d("QuizRepository", "Submitted quiz completion for module: $moduleId - Score: $scorePercentage% ($correctAnswers/$totalQuestions)")
            Result.success(inserted)
        } catch (e: Exception) {
            Log.e("QuizRepository", "Error submitting quiz completion", e)
            Result.failure(e)
        }
    }
    
    /**
     * Get quiz results for a user and module
     */
    suspend fun getQuizResultsByUserAndModule(userId: String, moduleId: String): Result<List<QuizResult>> = withContext(Dispatchers.IO) {
        try {
            val results = supabase
                .from("quiz_results")
                .select()
                {
                    filter {
                        eq("user_id", userId)
                        eq("module_id", moduleId)
                    }
                }
                .decodeList<QuizResult>()
            
            Log.d("QuizRepository", "Fetched ${results.size} quiz results for user: $userId, module: $moduleId")
            Result.success(results)
        } catch (e: Exception) {
            Log.e("QuizRepository", "Error fetching quiz results", e)
            Result.failure(e)
        }
    }
    
    /**
     * Calculate quiz score for a module
     */
    suspend fun calculateModuleScore(userId: String, moduleId: String): Result<Float> = withContext(Dispatchers.IO) {
        try {
            val results = getQuizResultsByUserAndModule(userId, moduleId).getOrNull() ?: emptyList()
            
            if (results.isEmpty()) {
                return@withContext Result.success(0f)
            }
            
            val correctAnswers = results.count { it.isCorrect }
            val totalQuestions = results.size
            val score = (correctAnswers.toFloat() / totalQuestions.toFloat()) * 100f
            
            Log.d("QuizRepository", "Calculated score for module $moduleId: $score% ($correctAnswers/$totalQuestions)")
            Result.success(score)
        } catch (e: Exception) {
            Log.e("QuizRepository", "Error calculating module score", e)
            Result.failure(e)
        }
    }
}