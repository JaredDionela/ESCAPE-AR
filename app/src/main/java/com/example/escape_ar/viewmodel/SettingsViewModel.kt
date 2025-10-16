package com.example.escape_ar.viewmodel

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.escape_ar.data.model.UserSettings
import com.example.escape_ar.data.repository.SettingsRepository
import com.example.escape_ar.data.SupabaseConfig
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

/**
 * ViewModel for managing user settings (volume controls, captions, etc.)
 */
class SettingsViewModel(application: Application) : AndroidViewModel(application) {
    
    private val repository = SettingsRepository(SupabaseConfig.client)
    
    // UI State
    private val _settings = MutableStateFlow<UserSettings?>(null)
    val settings: StateFlow<UserSettings?> = _settings.asStateFlow()
    
    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()
    
    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage.asStateFlow()
    
    private val _saveSuccess = MutableStateFlow(false)
    val saveSuccess: StateFlow<Boolean> = _saveSuccess.asStateFlow()
    
    init {
        loadSettings()
    }
    
    /**
     * Load user settings from Supabase
     */
    fun loadSettings() {
        viewModelScope.launch {
            _isLoading.value = true
            _errorMessage.value = null
            
            try {
                val result = repository.getUserSettings()
                result.onSuccess { userSettings ->
                    _settings.value = userSettings
                    Log.d("SettingsViewModel", "Settings loaded: $userSettings")
                }.onFailure { error ->
                    _errorMessage.value = error.message ?: "Failed to load settings"
                    Log.e("SettingsViewModel", "Error loading settings", error)
                    // Load default settings on error
                    _settings.value = UserSettings.DEFAULT
                }
            } catch (e: Exception) {
                _errorMessage.value = e.message ?: "Unknown error"
                Log.e("SettingsViewModel", "Exception loading settings", e)
                _settings.value = UserSettings.DEFAULT
            } finally {
                _isLoading.value = false
            }
        }
    }
    
    /**
     * Update music volume (0.0 to 1.0)
     */
    fun updateMusicVolume(volume: Float) {
        viewModelScope.launch {
            _settings.value?.let { currentSettings ->
                val updatedSettings = currentSettings.copy(musicVolume = volume)
                _settings.value = updatedSettings
                saveSettings(updatedSettings)
            }
        }
    }
    
    /**
     * Update sound effects volume (0.0 to 1.0)
     */
    fun updateSfxVolume(volume: Float) {
        viewModelScope.launch {
            _settings.value?.let { currentSettings ->
                val updatedSettings = currentSettings.copy(sfxVolume = volume)
                _settings.value = updatedSettings
                saveSettings(updatedSettings)
            }
        }
    }
    
    /**
     * Toggle captions on/off
     */
    fun toggleCaptions(enabled: Boolean) {
        viewModelScope.launch {
            _settings.value?.let { currentSettings ->
                val updatedSettings = currentSettings.copy(captionsEnabled = enabled)
                _settings.value = updatedSettings
                saveSettings(updatedSettings)
            }
        }
    }
    
    /**
     * Reset all settings to defaults
     */
    fun resetToDefaults() {
        viewModelScope.launch {
            _isLoading.value = true
            _errorMessage.value = null
            
            try {
                val result = repository.resetToDefaults()
                result.onSuccess { defaultSettings ->
                    _settings.value = defaultSettings
                    _saveSuccess.value = true
                    Log.d("SettingsViewModel", "Settings reset to defaults")
                }.onFailure { error ->
                    _errorMessage.value = error.message ?: "Failed to reset settings"
                    Log.e("SettingsViewModel", "Error resetting settings", error)
                }
            } catch (e: Exception) {
                _errorMessage.value = e.message ?: "Unknown error"
                Log.e("SettingsViewModel", "Exception resetting settings", e)
            } finally {
                _isLoading.value = false
            }
        }
    }
    
    /**
     * Save settings to Supabase
     */
    private suspend fun saveSettings(settings: UserSettings) {
        try {
            val result = repository.updateSettings(
                musicVolume = settings.musicVolume,
                sfxVolume = settings.sfxVolume,
                captionsEnabled = settings.captionsEnabled
            )
            result.onSuccess {
                _saveSuccess.value = true
                Log.d("SettingsViewModel", "Settings saved successfully")
            }.onFailure { error ->
                _errorMessage.value = error.message ?: "Failed to save settings"
                Log.e("SettingsViewModel", "Error saving settings", error)
            }
        } catch (e: Exception) {
            _errorMessage.value = e.message ?: "Unknown error"
            Log.e("SettingsViewModel", "Exception saving settings", e)
        }
    }
    
    /**
     * Clear error message
     */
    fun clearError() {
        _errorMessage.value = null
    }
    
    /**
     * Clear success message
     */
    fun clearSuccess() {
        _saveSuccess.value = false
    }
}
