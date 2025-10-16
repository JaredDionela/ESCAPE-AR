package com.example.escape_ar.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.escape_ar.data.repository.UserRepository
import com.example.escape_ar.data.model.UserInfo
import com.example.escape_ar.data.SessionManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class AuthViewModel(application: Application) : AndroidViewModel(application) {
    
    private val userRepository = UserRepository(application.applicationContext)
    private val sessionManager = SessionManager.getInstance(application.applicationContext)
    
    private val _authState = MutableStateFlow<AuthState>(AuthState.Initial)
    val authState: StateFlow<AuthState> = _authState.asStateFlow()
    
    sealed class AuthState {
        object Initial : AuthState()
        object Loading : AuthState()
        data class Success(val user: UserInfo) : AuthState()
        data class Error(val message: String) : AuthState()
    }
    
    init {
        checkExistingSession()
    }
    
    private fun checkExistingSession() {
        viewModelScope.launch {
            val currentUser = userRepository.getCurrentUser()
            if (currentUser != null) {
                _authState.value = AuthState.Success(currentUser)
            }
        }
    }
    
    fun signIn(email: String, password: String) {
        viewModelScope.launch {
            _authState.value = AuthState.Loading
            
            userRepository.signIn(email, password).fold(
                onSuccess = { userInfo ->
                    _authState.value = AuthState.Success(userInfo)
                },
                onFailure = { error ->
                    _authState.value = AuthState.Error(
                        error.message ?: "Sign in failed. Please check your credentials."
                    )
                }
            )
        }
    }
    
    fun signUp(email: String, password: String, fullName: String) {
        viewModelScope.launch {
            _authState.value = AuthState.Loading
            
            userRepository.signUp(email, password, fullName).fold(
                onSuccess = { userInfo ->
                    _authState.value = AuthState.Success(userInfo)
                },
                onFailure = { error ->
                    _authState.value = AuthState.Error(
                        error.message ?: "Sign up failed. Please try again."
                    )
                }
            )
        }
    }
    
    fun signOut() {
        viewModelScope.launch {
            userRepository.signOut()
            _authState.value = AuthState.Initial
        }
    }
    
    fun resetAuthState() {
        _authState.value = AuthState.Initial
    }
}
