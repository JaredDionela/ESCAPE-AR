package com.example.escape_ar.utils

import android.content.Context
import android.media.MediaPlayer
import android.media.SoundPool
import android.media.AudioAttributes
import android.util.Log
import com.example.escape_ar.R

class AudioManager private constructor(private val context: Context) {
    
    private var backgroundMusic: MediaPlayer? = null
    private var soundPool: SoundPool? = null
    private var buttonClickSound: Int = 0
    private var successSound: Int = 0
    private var errorSound: Int = 0
    
    private var musicVolume: Float = 0.5f
    private var effectsVolume: Float = 0.7f
    private var isMusicEnabled: Boolean = true
    private var areEffectsEnabled: Boolean = true
    
    companion object {
        @Volatile
        private var instance: AudioManager? = null
        
        fun getInstance(context: Context): AudioManager {
            return instance ?: synchronized(this) {
                instance ?: AudioManager(context.applicationContext).also { instance = it }
            }
        }
    }
    
    init {
        initializeSoundPool()
    }
    
    private fun initializeSoundPool() {
        val audioAttributes = AudioAttributes.Builder()
            .setUsage(AudioAttributes.USAGE_GAME)
            .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
            .build()
        
        soundPool = SoundPool.Builder()
            .setMaxStreams(5)
            .setAudioAttributes(audioAttributes)
            .build()
        
        // Load sound effects - using system sounds as placeholders
        // You can replace these with your own sound files
        try {
            buttonClickSound = soundPool?.load(context, R.raw.button_click, 1) ?: 0
            successSound = soundPool?.load(context, R.raw.success, 1) ?: 0
            errorSound = soundPool?.load(context, R.raw.error, 1) ?: 0
        } catch (e: Exception) {
            Log.e("AudioManager", "Error loading sounds: ${e.message}")
        }
    }
    
    fun startBackgroundMusic() {
        if (!isMusicEnabled) return
        
        try {
            if (backgroundMusic == null) {
                backgroundMusic = MediaPlayer.create(context, R.raw.background_music)
                backgroundMusic?.apply {
                    isLooping = true
                    setVolume(musicVolume, musicVolume)
                }
            }
            
            if (backgroundMusic?.isPlaying == false) {
                backgroundMusic?.start()
                Log.d("AudioManager", "Background music started")
            }
        } catch (e: Exception) {
            Log.e("AudioManager", "Error starting background music: ${e.message}")
        }
    }
    
    fun stopBackgroundMusic() {
        backgroundMusic?.apply {
            if (isPlaying) {
                pause()
                seekTo(0)
            }
        }
        Log.d("AudioManager", "Background music stopped")
    }
    
    fun pauseBackgroundMusic() {
        if (backgroundMusic?.isPlaying == true) {
            backgroundMusic?.pause()
            Log.d("AudioManager", "Background music paused")
        }
    }
    
    fun resumeBackgroundMusic() {
        if (isMusicEnabled && backgroundMusic?.isPlaying == false) {
            backgroundMusic?.start()
            Log.d("AudioManager", "Background music resumed")
        }
    }
    
    fun setMusicVolume(volume: Float) {
        musicVolume = volume.coerceIn(0f, 1f)
        backgroundMusic?.setVolume(musicVolume, musicVolume)
        Log.d("AudioManager", "Music volume set to: $musicVolume")
    }
    
    fun setEffectsVolume(volume: Float) {
        effectsVolume = volume.coerceIn(0f, 1f)
        Log.d("AudioManager", "Effects volume set to: $effectsVolume")
    }
    
    fun setMusicEnabled(enabled: Boolean) {
        isMusicEnabled = enabled
        if (enabled) {
            startBackgroundMusic()
        } else {
            stopBackgroundMusic()
        }
    }
    
    fun setEffectsEnabled(enabled: Boolean) {
        areEffectsEnabled = enabled
    }
    
    fun playButtonClick() {
        if (areEffectsEnabled && buttonClickSound != 0) {
            soundPool?.play(buttonClickSound, effectsVolume, effectsVolume, 1, 0, 1f)
        }
    }
    
    fun playSuccess() {
        if (areEffectsEnabled && successSound != 0) {
            soundPool?.play(successSound, effectsVolume, effectsVolume, 1, 0, 1f)
        }
    }
    
    fun playError() {
        if (areEffectsEnabled && errorSound != 0) {
            soundPool?.play(errorSound, effectsVolume, effectsVolume, 1, 0, 1f)
        }
    }
    
    fun release() {
        backgroundMusic?.release()
        backgroundMusic = null
        soundPool?.release()
        soundPool = null
        Log.d("AudioManager", "Audio resources released")
    }
}
