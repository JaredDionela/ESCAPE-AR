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
