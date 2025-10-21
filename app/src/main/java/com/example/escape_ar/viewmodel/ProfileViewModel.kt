package com.example.escape_ar.viewmodel

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.escape_ar.data.repository.UserRepository
import com.example.escape_ar.data.model.UserInfo
import com.example.escape_ar.data.model.Module
import com.example.escape_ar.data.model.QuizProgress
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class ProfileViewModel(application: Application) : AndroidViewModel(application) {
    
    private val userRepository = UserRepository(application.applicationContext)
    
    private val _uiState = MutableStateFlow(ProfileUiState())
    val uiState: StateFlow<ProfileUiState> = _uiState.asStateFlow()
    
    private val _currentUser = MutableStateFlow<UserInfo?>(null)
    val currentUser: StateFlow<UserInfo?> = _currentUser.asStateFlow()
    
    private val _modules = MutableStateFlow<List<Module>>(emptyList())
    val modules: StateFlow<List<Module>> = _modules.asStateFlow()
    
    private val _progress = MutableStateFlow<List<QuizProgress>>(emptyList())
    val progress: StateFlow<List<QuizProgress>> = _progress.asStateFlow()
    
    init {
        loadProfile()
    }
    
    fun loadProfile() {
        viewModelScope.launch {
            try {
                _uiState.value = _uiState.value.copy(isLoading = true, error = null)
                Log.d("ProfileViewModel", "Starting profile load...")
                
                // Get current user
                val user = userRepository.getCurrentUser()
                if (user == null) {
                    Log.w("ProfileViewModel", "getCurrentUser() returned null")
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        error = "No user session found. Please sign in again."
                    )
                    return@launch
                }
                
                _currentUser.value = user
                Log.d("ProfileViewModel", "Loaded user: ${user.fullName} (${user.email})")
                
                // Load extended profile (teacher name, section)
                var teacherName: String? = null
                var section: String? = null
                try {
                    val extendedProfileResult = userRepository.getExtendedUserProfile(user.id)
                    extendedProfileResult.onSuccess { profile ->
                        teacherName = profile.teacherName
                        section = profile.section
                        Log.d("ProfileViewModel", "Loaded extended profile: teacher=${profile.teacherName}, section=${profile.section}")
                    }.onFailure { e ->
                        Log.w("ProfileViewModel", "Could not load extended profile: ${e.message}")
                    }
                } catch (e: Exception) {
                    Log.w("ProfileViewModel", "Exception loading extended profile", e)
                }
                
                // Load modules with progress
                val modulesWithProgress = userRepository.getUserModulesWithProgress(user.id)
                _modules.value = modulesWithProgress
                Log.d("ProfileViewModel", "Loaded ${modulesWithProgress.size} modules")
                
                // Load detailed progress
                val userProgress = userRepository.getUserQuizProgress(user.id)
                _progress.value = userProgress
                Log.d("ProfileViewModel", "Loaded ${userProgress.size} progress records")
                
                // Update UI state with dynamic scoring based on actual questions
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    userName = user.fullName,
                    userEmail = user.email,
                    teacherName = teacherName,
                    section = section,
                    modules = modulesWithProgress.map { module ->
                        // Score is stored as percentage (0-100) in database
                        val scorePercentage = ((module.score ?: 0f).coerceIn(0f, 100f)).toInt()
                        ModuleProgress(
                            name = module.name,
                            score = scorePercentage,
                            maxScore = 100, // All modules scored out of 100%
                            isCompleted = module.isCompleted,
                            totalQuestions = module.totalQuestions
                        )
                    }
                )
                
                Log.d("ProfileViewModel", "Profile UI state updated: userName=${user.fullName}, teacher=$teacherName, section=$section, modules=${modulesWithProgress.size}")
                
            } catch (e: Exception) {
                Log.e("ProfileViewModel", "Error loading profile", e)
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    error = "Failed to load profile: ${e.message}"
                )
            }
        }
    }
    
    fun refreshData() {
        loadProfile()
    }
    
    fun clearError() {
        _uiState.value = _uiState.value.copy(error = null)
    }
    
    // Analytics functions
    fun getTotalScore(): Int = _uiState.value.modules.sumOf { it.score }
    
    fun getMaxPossibleScore(): Int = _uiState.value.modules.sumOf { it.maxScore }
    
    fun getCompletedModules(): Int = _uiState.value.modules.count { it.isCompleted }
    
    fun getTotalModules(): Int = _uiState.value.modules.size
    
    fun getOverallProgressPercentage(): Int {
        val total = getTotalModules()
        return if (total > 0) (getCompletedModules() * 100) / total else 0
    }
    
    fun getAverageScore(): Float {
        val modules = _uiState.value.modules
        return if (modules.isNotEmpty()) {
            modules.map { it.score.toFloat() }.average().toFloat()
        } else 0f
    }
}

data class ProfileUiState(
    val isLoading: Boolean = false,
    val error: String? = null,
    val userName: String = "Loading...",
    val userEmail: String = "Loading...",
    val teacherName: String? = null,
    val section: String? = null,
    val modules: List<ModuleProgress> = emptyList()
)

data class ModuleProgress(
    val name: String,
    val score: Int,          // Percentage score (0-100)
    val maxScore: Int,       // Always 100 (percentage-based)
    val isCompleted: Boolean,
    val totalQuestions: Int = 0  // Actual number of questions in this module
)
