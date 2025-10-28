package com.example.escape_ar.unity

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import com.unity3d.player.UnityPlayerGameActivity

/**
 * Unity Activity that extends UnityPlayerGameActivity.
 * This is the recommended way to integrate Unity into an Android app.
 */
class UnityHolderActivity : UnityPlayerGameActivity() {
    
    companion object {
        private const val TAG = "UnityHolderActivity"
        const val UNITY_EXIT_CODE = 999
    }
    
    override fun onCreate(savedInstanceState: Bundle?) {
        Log.d(TAG, "UnityHolderActivity onCreate called")
        super.onCreate(savedInstanceState)
        
        try {
            Log.d(TAG, "Unity initialized successfully!")
        } catch (e: Exception) {
            Log.e(TAG, "Failed to initialize Unity", e)
            Toast.makeText(
                this,
                "Error loading Unity: ${e.message}",
                Toast.LENGTH_LONG
            ).show()
            finish()
        }
    }
    
    override fun onBackPressed() {
        // Exit Unity and return to main app
        Log.d(TAG, "Back pressed - returning to main app")
        exitUnity()
    }
    
    /**
     * Exit Unity and return to main app
     */
    fun exitUnity() {
        setResult(UNITY_EXIT_CODE)
        finish()
    }
}
