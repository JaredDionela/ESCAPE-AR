package com.example.escape_ar.data.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class User(
    val id: String,
    val email: String,
    @SerialName("full_name")
    val fullName: String = "Student",
    @SerialName("created_at")
    val createdAt: String,
    val role: String = "student"
)

@Serializable
data class QuizQuestion(
    val id: String,
    @SerialName("module_id")
    val moduleId: String,
    @SerialName("question_text")
    val questionText: String,
    @SerialName("option_a")
    val optionA: String,
    @SerialName("option_b")
    val optionB: String,
    @SerialName("option_c")
    val optionC: String,
    @SerialName("option_d")
    val optionD: String,
    @SerialName("correct_answer")
    val correctAnswer: String, // "A", "B", "C", or "D"
    @SerialName("order_index")
    val orderIndex: Int,
    @SerialName("created_at")
    val createdAt: String? = null
) {
    // Helper function to get options as a list
    fun getOptions(): List<String> = listOf(optionA, optionB, optionC, optionD)
    
    // Helper function to get correct answer index (0-3)
    fun getCorrectAnswerIndex(): Int = when (correctAnswer) {
        "A" -> 0
        "B" -> 1
        "C" -> 2
        "D" -> 3
        else -> 0
    }
}

@Serializable
data class QuizResult(
    val id: String? = null,
    @SerialName("user_id")
    val userId: String,
    @SerialName("question_id")
    val questionId: String? = null, // Optional for summary results
    @SerialName("module_id")
    val moduleId: String,
    @SerialName("selected_answer")
    val selectedAnswer: String? = null, // "A", "B", "C", or "D" - Optional for summary
    @SerialName("is_correct")
    val isCorrect: Boolean = false,
    @SerialName("score_percentage")
    val scorePercentage: Float? = null, // Flexible scoring
    @SerialName("total_questions")
    val totalQuestions: Int? = null, // How many questions in this quiz
    @SerialName("correct_answers")
    val correctAnswers: Int? = null, // How many correct
    @SerialName("created_at")
    val createdAt: String? = null
)

@Serializable
data class QuizProgress(
    val id: String? = null,
    @SerialName("user_id")
    val userId: String,
    val module: String,
    val score: Float,
    @SerialName("completed_at")
    val completedAt: String? = null,
    @SerialName("questions_answered")
    val questionsAnswered: Int = 0,
    @SerialName("total_questions")
    val totalQuestions: Int = 10
)

@Serializable
data class StudentProgress(
    val id: String,
    val email: String,
    @SerialName("full_name")
    val fullName: String = "Student",
    @SerialName("completed_modules")
    val completedModules: Int = 0,
    @SerialName("total_modules")
    val totalModules: Int = 4,
    @SerialName("average_score")
    val averageScore: Float = 0f,
    @SerialName("last_active")
    val lastActive: String? = null
)

data class Module(
    val id: String,
    val name: String,
    val description: String,
    val totalQuestions: Int = 10,
    val isCompleted: Boolean = false,
    val score: Float? = null
)

@Serializable
data class AdminStats(
    @SerialName("total_students")
    val totalStudents: Int,
    @SerialName("active_students")
    val activeStudents: Int,
    @SerialName("completed_modules")
    val completedModules: Int,
    @SerialName("average_score")
    val averageScore: Float
)

@Serializable
data class ModuleStats(
    val name: String,
    val completions: Int,
    @SerialName("average_score")
    val averageScore: Float,
    @SerialName("success_rate")
    val successRate: Float
)

@Serializable
data class UserInfo(
    val id: String,
    val email: String,
    @SerialName("full_name")
    val fullName: String,
    val role: String = "student"
)

@Serializable
data class UserProgress(
    @SerialName("user_id")
    val userId: String,
    val module: String,
    val completed: Boolean = false,
    @SerialName("best_score")
    val bestScore: Float = 0f
)

// Authentication related classes
sealed class AuthState {
    object Initial : AuthState()
    object Loading : AuthState()
    data class Success(val user: UserInfo) : AuthState()
    data class Error(val message: String) : AuthState()
}
