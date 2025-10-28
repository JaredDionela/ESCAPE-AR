package com.example.escape_ar.unity

import android.app.Activity
import android.content.Intent
import android.util.Log
import androidx.activity.result.ActivityResultLauncher

/**
 * Bridge class for communication between Kotlin app and Unity game.
 * Provides methods to launch Unity and receive callbacks.
 */
object UnityBridge {
    
    private const val TAG = "UnityBridge"
    
    /**
     * Launch Unity activity from any context
     * @param activity Current activity
     * @param launcher ActivityResultLauncher to handle Unity exit
     */
    fun launchUnity(activity: Activity, launcher: ActivityResultLauncher<Intent>? = null) {
        try {
            val intent = Intent(activity, UnityHolderActivity::class.java)
            
            if (launcher != null) {
                launcher.launch(intent)
                Log.d(TAG, "Unity launched with result launcher")
            } else {
                activity.startActivity(intent)
                Log.d(TAG, "Unity launched without result launcher")
            }
        } catch (e: Exception) {
            Log.e(TAG, "Failed to launch Unity", e)
        }
    }
    
    /**
     * Check if Unity is available
     */
    fun isUnityAvailable(): Boolean {
        return try {
            Class.forName("com.unity3d.player.UnityPlayer")
            true
        } catch (e: ClassNotFoundException) {
            false
        }
    }
    
    /**
     * Send message to Unity (when Unity is running)
     * This is typically called from within UnityHolderActivity
     * @param gameObject Name of the GameObject in Unity
     * @param method Name of the method to call
     * @param message Message parameter
     */
    fun sendToUnity(gameObject: String, method: String, message: String = "") {
        try {
            val unityPlayerClass = Class.forName("com.unity3d.player.UnityPlayer")
            val sendMessageMethod = unityPlayerClass.getMethod(
                "UnitySendMessage",
                String::class.java,
                String::class.java,
                String::class.java
            )
            sendMessageMethod.invoke(null, gameObject, method, message)
            Log.d(TAG, "Message sent to Unity: $gameObject.$method($message)")
        } catch (e: Exception) {
            Log.e(TAG, "Failed to send message to Unity", e)
        }
    }
}
