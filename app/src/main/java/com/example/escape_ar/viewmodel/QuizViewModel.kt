package com.example.escape_ar.viewmodel

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.escape_ar.data.model.Module
import com.example.escape_ar.data.model.QuizProgress
import com.example.escape_ar.data.repository.UserRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

class QuizViewModel(application: Application) : AndroidViewModel(application) {
    private val userRepository = UserRepository(application.applicationContext)
    
    private val _modules = MutableStateFlow<List<Module>>(emptyList())
    val modules: StateFlow<List<Module>> = _modules.asStateFlow()
    
    private val _userProgress = MutableStateFlow<List<QuizProgress>>(emptyList())
    val userProgress: StateFlow<List<QuizProgress>> = _userProgress.asStateFlow()
    
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
