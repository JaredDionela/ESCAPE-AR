package com.example.escape_ar.viewmodel

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.escape_ar.data.model.Module
import com.example.escape_ar.data.model.QuizProgress
import com.example.escape_ar.data.model.QuizQuestion
import com.example.escape_ar.data.repository.UserRepository
import com.example.escape_ar.data.repository.QuizRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

class QuizViewModel(application: Application) : AndroidViewModel(application) {
    private val userRepository = UserRepository(application.applicationContext)
    private val quizRepository = QuizRepository()
    
    private val _modules = MutableStateFlow<List<Module>>(emptyList())
    val modules: StateFlow<List<Module>> = _modules.asStateFlow()
    
    private val _userProgress = MutableStateFlow<List<QuizProgress>>(emptyList())
    val userProgress: StateFlow<List<QuizProgress>> = _userProgress.asStateFlow()
    
    private val _quizQuestions = MutableStateFlow<List<QuizQuestion>>(emptyList())
    val quizQuestions: StateFlow<List<QuizQuestion>> = _quizQuestions.asStateFlow()
    
    private val _isLoadingQuestions = MutableStateFlow(false)
    val isLoadingQuestions: StateFlow<Boolean> = _isLoadingQuestions.asStateFlow()
    
    private val _loadError = MutableStateFlow<String?>(null)
    val loadError: StateFlow<String?> = _loadError.asStateFlow()
    
    private val _currentUser = MutableStateFlow<String?>(null)
    
    init {
        getCurrentUser()
    }
    
    private fun getCurrentUser() {
        viewModelScope.launch {
            val user = userRepository.getCurrentUser()
            _currentUser.value = user?.id
            if (user != null) {
                loadModules()
                loadUserProgress()
            }
        }
    }
    
    fun loadModules() {
        viewModelScope.launch {
            val userId = _currentUser.value ?: return@launch
            val modulesWithProgress = userRepository.getUserModulesWithProgress(userId)
            _modules.value = modulesWithProgress
        }
    }
    
    /**
     * Load quiz questions for a specific module from database
     */
    fun loadQuizQuestionsForModule(moduleId: String) {
        viewModelScope.launch {
            _isLoadingQuestions.value = true
            _loadError.value = null
            
            Log.d("QuizViewModel", "Loading quiz questions for module: $moduleId")
            
            val result = quizRepository.getQuizQuestionsByModule(moduleId)
            result.onSuccess { questions ->
                _quizQuestions.value = questions
                Log.d("QuizViewModel", "Loaded ${questions.size} questions for module: $moduleId")
            }.onFailure { error ->
                _loadError.value = "Failed to load questions: ${error.message}"
                Log.e("QuizViewModel", "Error loading quiz questions for $moduleId", error)
            }
            
            _isLoadingQuestions.value = false
        }
    }
    
    /**
     * Get quiz questions directly (for use in composables)
     * This is a suspend function that can be called from LaunchedEffect
     */
    suspend fun getQuizQuestionsByModule(moduleId: String): Result<List<QuizQuestion>> {
        Log.d("QuizViewModel", "getQuizQuestionsByModule called for: $moduleId")
        return quizRepository.getQuizQuestionsByModule(moduleId)
    }
    
    /**
     * Clear loaded questions (e.g., when leaving quiz screen)
     */
    fun clearQuestions() {
        _quizQuestions.value = emptyList()
        _loadError.value = null
    }
    
    fun loadUserProgress() {
        viewModelScope.launch {
            val userId = _currentUser.value ?: return@launch
            val progress = userRepository.getUserQuizProgress(userId)
            _userProgress.value = progress
        }
    }
    
    fun startModule(moduleId: String) {
        // This would typically navigate to a detailed quiz screen
        // For now, we'll just mark it as started
        viewModelScope.launch {
            // Implementation for starting a specific module
        }
    }
    
    fun completeModule(moduleId: String, score: Float, questionsAnswered: Int, totalQuestions: Int) {
        Log.d("QuizViewModel", "completeModule called: moduleId=$moduleId, score=$score, questionsAnswered=$questionsAnswered, totalQuestions=$totalQuestions")
        
        viewModelScope.launch {
            val userId = _currentUser.value
            if (userId == null) {
                Log.e("QuizViewModel", "No current user ID available for saving quiz progress")
                return@launch
            }
            
            Log.d("QuizViewModel", "Creating QuizProgress for userId=$userId")
            
            val quizProgress = QuizProgress(
                userId = userId,
                module = moduleId,
                score = score,
                completedAt = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(Date()),
                questionsAnswered = questionsAnswered,
                totalQuestions = totalQuestions
            )
            
            Log.d("QuizViewModel", "Calling userRepository.saveQuizProgress for module $moduleId")
            
            // Save to database
            userRepository.saveQuizProgress(quizProgress)
            
            // ALSO save to quiz_results table with flexible scoring
            Log.d("QuizViewModel", "Submitting quiz completion to quiz_results table")
            val quizRepo = QuizRepository()
            val result = quizRepo.submitQuizCompletion(
                userId = userId,
                moduleId = moduleId,
                correctAnswers = questionsAnswered,
                totalQuestions = totalQuestions
            )
            
            result.onSuccess {
                Log.d("QuizViewModel", "Successfully saved quiz result with flexible scoring")
            }.onFailure { error ->
                Log.e("QuizViewModel", "Failed to save quiz result: ${error.message}")
            }
            
            // Refresh local data to reflect changes
            loadUserProgress()
            loadModules()
            
            Log.d("QuizViewModel", "Module $moduleId completion process finished")
        }
    }
    
    fun getModuleProgress(moduleId: String): QuizProgress? {
        return _userProgress.value.find { it.module == moduleId }
    }
    
    fun isModuleCompleted(moduleId: String): Boolean {
        return getModuleProgress(moduleId) != null
    }
    
    fun getOverallProgress(): Float {
        val completedModules = _userProgress.value.size
        val totalModules = _modules.value.size
        return if (totalModules > 0) (completedModules.toFloat() / totalModules) * 100f else 0f
    }
    
    fun getAverageScore(): Float {
        val scores = _userProgress.value.map { it.score }
        return if (scores.isNotEmpty()) scores.average().toFloat() else 0f
    }
}
