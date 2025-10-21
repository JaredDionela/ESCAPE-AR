package com.example.escape_ar.data.model

import kotlinx.serialization.Serializable

/**
 * Simplified data models for KYLON RESCUE MISSION
 * 2-table architecture: profiles + progress
 */

// =====================================================
// USER PROFILE DATA
// =====================================================

@Serializable
data class UserProfile(
    val id: String, // UUID from auth.users
    val name: String,
    val email: String,
    val role: String = "student", // student | admin
    val avatarUrl: String? = null,
    val createdAt: String,
    val updatedAt: String
)

// =====================================================
// UNIVERSAL PROGRESS TRACKING  
// =====================================================

@Serializable
data class ProgressEntry(
    val id: String, // UUID
    val user_id: String, // Foreign key to profiles (matches DB column name)
    val module_id: String, // Module identifier (matches DB column name)
    val score: Int = 0,
    val best_score: Int = 0, // Best score achieved (matches DB column name)
    val completed: Boolean = false, // Completion status (matches DB column name)
    val attempts: Int = 0,
    val time_spent_minutes: Int = 0, // Time spent (matches DB column name)
    val started_at: String, // Started timestamp (matches DB column name)
    val completed_at: String? = null, // Completion timestamp (matches DB column name)
    val updated_at: String // Last updated (matches DB column name)
)

// =====================================================
// KYLON MISSION MODULES (Reference Data)
// =====================================================

@Serializable
data class KylonModule(
    val id: String,
    val title: String,
    val description: String,
    val storylineContext: String,
    val difficultyLevel: String = "intermediate",
    val estimatedTimeMinutes: Int = 20,
    
    val topics: List<String> = emptyList(),
    val iconName: String = "science",
    val colorTheme: String = "#00FFFF"
)

// =====================================================
// VIEW MODELS FOR UI
// =====================================================

@Serializable
data class UserProgressSummary(
    val totalModules: Int,
    val completedModules: Int,
    val totalScore: Int,
    val averageScore: Double,
    val totalTimeHours: Double
)

// Hard-coded KYLON modules (no database needed)
object KylonModules {
    val modules = listOf(
        KylonModule(
            id = "decantation",
            title = "Chemical Separation Lab",
            description = "Master decantation and filtration techniques to purify corrupted lab samples.",
            storylineContext = "The Labyrinth has contaminated the water supply. Use your chemistry knowledge to separate clean water from pollutants using decantation principles.",
            difficultyLevel = "beginner",
            estimatedTimeMinutes = 18,
           
            topics = listOf("decantation", "filtration", "separation", "chemistry"),
            iconName = "science",
            colorTheme = "#00FFFF"
        ),
        KylonModule(
            id = "organ_system",
            title = "Human Body Systems",
            description = "Understand how KYLON's biological systems work to ensure optimal performance.",
            storylineContext = "KYLON's bio-mechanical systems are failing. Analyze human organ systems to understand how to repair and enhance KYLON's life support mechanisms.",
            difficultyLevel = "intermediate",
            estimatedTimeMinutes = 22,
            
            topics = listOf("anatomy", "physiology", "organ_system", "biology"),
            iconName = "favorite",
            colorTheme = "#FF4444"
        ),
        KylonModule(
            id = "simple_machines",
            title = "Mechanical Engineering",
            description = "Study simple machines and mechanical principles essential for lab equipment.",
            storylineContext = "The Labyrinth's mechanical traps use complex simple machine combinations. Master these principles to navigate the facility and reach KYLON.",
            difficultyLevel = "intermediate",
            estimatedTimeMinutes = 20,
           
            topics = listOf("simple_machines", "physics", "mechanics", "engineering"),
            iconName = "build",
            colorTheme = "#FFA500"
        ),
        KylonModule(
            id = "solar_system",
            title = "Astronomical Navigation",
            description = "Master solar system knowledge for space-based rescue missions.",
            storylineContext = "KYLON contains star maps essential for humanity's future. Learn the solar system to decode KYLON's astronomical databases and plan the escape route.",
            difficultyLevel = "advanced",
            estimatedTimeMinutes = 28,
            
            topics = listOf("astronomy", "solar_system", "space", "navigation"),
            iconName = "public",
            colorTheme = "#9933FF"
        )
    )
    
    fun getModule(id: String): KylonModule? = modules.find { it.id == id }
}
