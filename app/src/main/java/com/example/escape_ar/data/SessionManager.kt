package com.example.escape_ar.data

import android.content.Context
import android.content.SharedPreferences
import android.util.Log
import com.example.escape_ar.data.model.UserInfo
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.serialization.json.Json
import kotlinx.serialization.encodeToString
import kotlinx.serialization.decodeFromString

class SessionManager private constructor(context: Context) {
    private val prefs: SharedPreferences = context.getSharedPreferences("escape_ar_session", Context.MODE_PRIVATE)
    
    private val _currentUser = MutableStateFlow<UserInfo?>(null)
    val currentUser: StateFlow<UserInfo?> = _currentUser.asStateFlow()
    
    private val _isAuthenticated = MutableStateFlow(false)
    val isAuthenticated: StateFlow<Boolean> = _isAuthenticated.asStateFlow()
    
    // Persisted auth tokens so repositories recreated in a new process can still perform REST calls
    @Volatile private var accessToken: String? = null
    @Volatile private var refreshToken: String? = null
    
    companion object {
        @Volatile
        private var INSTANCE: SessionManager? = null
        
        fun getInstance(context: Context): SessionManager {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: SessionManager(context.applicationContext).also { INSTANCE = it }
            }
        }
    }
    
    init {
        loadStoredSession()
    }
    
    private fun loadStoredSession() {
        try {
            val userJson = prefs.getString("current_user", null)
            accessToken = prefs.getString("access_token", null)
            refreshToken = prefs.getString("refresh_token", null)
            if (userJson != null) {
                val user = Json.decodeFromString<UserInfo>(userJson)
                _currentUser.value = user
                _isAuthenticated.value = true
                Log.d("SessionManager", "Restored session for: ${user.fullName}; tokenPresent=${!accessToken.isNullOrBlank()}")
            } else {
                Log.d("SessionManager", "No stored session found")
            }
        } catch (e: Exception) {
            Log.e("SessionManager", "Error loading stored session", e)
            clearSession()
        }
    }
    
    fun saveSession(user: UserInfo) {
        try {
            val userJson = Json.encodeToString(user)
            prefs.edit()
                .putString("current_user", userJson)
                .putBoolean("is_authenticated", true)
                .apply()
            
            _currentUser.value = user
            _isAuthenticated.value = true
            Log.d("SessionManager", "Session saved for: ${user.fullName}")
        } catch (e: Exception) {
            Log.e("SessionManager", "Error saving session", e)
        }
    }
    
    fun saveAuthSession(user: UserInfo, accessToken: String?, refreshToken: String?) {
        try {
            val userJson = Json.encodeToString(user)
            prefs.edit()
                .putString("current_user", userJson)
                .putBoolean("is_authenticated", true)
                .putString("access_token", accessToken)
                .putString("refresh_token", refreshToken)
                .apply()
            this.accessToken = accessToken
            this.refreshToken = refreshToken
            _currentUser.value = user
            _isAuthenticated.value = true
            Log.d("SessionManager", "Auth session saved (tokenPresent=${!accessToken.isNullOrBlank()}) for: ${user.fullName}")
        } catch (e: Exception) {
            Log.e("SessionManager", "Error saving auth session", e)
        }
    }
    
    fun clearSession() {
        prefs.edit().clear().apply()
        _currentUser.value = null
        _isAuthenticated.value = false
        accessToken = null
        refreshToken = null
        Log.d("SessionManager", "Session cleared")
    }
    
    fun getCurrentUser(): UserInfo? = _currentUser.value
    
    fun isLoggedIn(): Boolean = _isAuthenticated.value && _currentUser.value != null
    
    fun getAccessToken(): String? = accessToken
    fun getRefreshToken(): String? = refreshToken
}
