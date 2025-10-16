package com.example.escape_ar.data.repository

import android.util.Log
import com.example.escape_ar.data.model.UpdateSettingsRequest
import com.example.escape_ar.data.model.UserSettings
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.gotrue.auth
import io.github.jan.supabase.postgrest.from
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
 * Repository for managing user settings
 */
class SettingsRepository(private val supabase: SupabaseClient) {

    companion object {
        private const val TAG = "SettingsRepository"
        private const val TABLE_USER_SETTINGS = "user_settings"
        
        // Default settings
        const val DEFAULT_MUSIC_VOLUME = 0.7f
        const val DEFAULT_SFX_VOLUME = 0.7f
        const val DEFAULT_CAPTIONS_ENABLED = true
    }

    /**
     * Get user settings from Supabase
     */
    suspend fun getUserSettings(): Result<UserSettings> = withContext(Dispatchers.IO) {
        try {
            val userId = supabase.auth.currentUserOrNull()?.id
                ?: return@withContext Result.failure(Exception("User not authenticated"))

            val settingsList = supabase.from(TABLE_USER_SETTINGS)
                .select {
                    filter {
                        eq("user_id", userId)
                    }
                }
                .decodeList<UserSettings>()

            val settings = settingsList.firstOrNull() ?: run {
                // Create default settings if none exist
                createDefaultSettings(userId).getOrNull()
                    ?: return@withContext Result.failure(Exception("Failed to create default settings"))
            }

            Log.d(TAG, "Fetched user settings")
            Result.success(settings)
        } catch (e: Exception) {
            Log.e(TAG, "Error fetching user settings", e)
            Result.failure(e)
        }
    }

    /**
     * Create default settings for a user
     */
    private suspend fun createDefaultSettings(userId: String): Result<UserSettings> = withContext(Dispatchers.IO) {
        try {
            val defaultSettings = UserSettings(
                userId = userId,
                musicVolume = DEFAULT_MUSIC_VOLUME,
                sfxVolume = DEFAULT_SFX_VOLUME,
                captionsEnabled = DEFAULT_CAPTIONS_ENABLED
            )

            supabase.from(TABLE_USER_SETTINGS)
                .insert(defaultSettings)

            Log.d(TAG, "Created default settings for user")
            Result.success(defaultSettings)
        } catch (e: Exception) {
            Log.e(TAG, "Error creating default settings", e)
            Result.failure(e)
        }
    }

    /**
     * Update user settings
     */
    suspend fun updateSettings(
        musicVolume: Float,
        sfxVolume: Float,
        captionsEnabled: Boolean
    ): Result<UserSettings> = withContext(Dispatchers.IO) {
        try {
            val userId = supabase.auth.currentUserOrNull()?.id
                ?: return@withContext Result.failure(Exception("User not authenticated"))

            // Validate volumes are in range [0, 1]
            val validMusicVolume = musicVolume.coerceIn(0f, 1f)
            val validSfxVolume = sfxVolume.coerceIn(0f, 1f)

            val updateRequest = mapOf(
                "music_volume" to validMusicVolume,
                "sfx_volume" to validSfxVolume,
                "captions_enabled" to captionsEnabled
            )

            supabase.from(TABLE_USER_SETTINGS)
                .update(updateRequest) {
                    filter {
                        eq("user_id", userId)
                    }
                }

            val updatedSettings = UserSettings(
                userId = userId,
                musicVolume = validMusicVolume,
                sfxVolume = validSfxVolume,
                captionsEnabled = captionsEnabled
            )

            Log.d(TAG, "Updated user settings")
            Result.success(updatedSettings)
        } catch (e: Exception) {
            Log.e(TAG, "Error updating user settings", e)
            Result.failure(e)
        }
    }

    /**
     * Reset settings to default values
     */
    suspend fun resetToDefaults(): Result<UserSettings> {
        return updateSettings(
            musicVolume = DEFAULT_MUSIC_VOLUME,
            sfxVolume = DEFAULT_SFX_VOLUME,
            captionsEnabled = DEFAULT_CAPTIONS_ENABLED
        )
    }

    /**
     * Update only music volume
     */
    suspend fun updateMusicVolume(volume: Float): Result<UserSettings> = withContext(Dispatchers.IO) {
        try {
            val currentSettings = getUserSettings().getOrNull()
                ?: return@withContext Result.failure(Exception("Failed to get current settings"))

            updateSettings(
                musicVolume = volume,
                sfxVolume = currentSettings.sfxVolume,
                captionsEnabled = currentSettings.captionsEnabled
            )
        } catch (e: Exception) {
            Log.e(TAG, "Error updating music volume", e)
            Result.failure(e)
        }
    }

    /**
     * Update only SFX volume
     */
    suspend fun updateSfxVolume(volume: Float): Result<UserSettings> = withContext(Dispatchers.IO) {
        try {
            val currentSettings = getUserSettings().getOrNull()
                ?: return@withContext Result.failure(Exception("Failed to get current settings"))

            updateSettings(
                musicVolume = currentSettings.musicVolume,
                sfxVolume = volume,
                captionsEnabled = currentSettings.captionsEnabled
            )
        } catch (e: Exception) {
            Log.e(TAG, "Error updating SFX volume", e)
            Result.failure(e)
        }
    }

    /**
     * Toggle captions on/off
     */
    suspend fun toggleCaptions(enabled: Boolean): Result<UserSettings> = withContext(Dispatchers.IO) {
        try {
            val currentSettings = getUserSettings().getOrNull()
                ?: return@withContext Result.failure(Exception("Failed to get current settings"))

            updateSettings(
                musicVolume = currentSettings.musicVolume,
                sfxVolume = currentSettings.sfxVolume,
                captionsEnabled = enabled
            )
        } catch (e: Exception) {
            Log.e(TAG, "Error toggling captions", e)
            Result.failure(e)
        }
    }
}
