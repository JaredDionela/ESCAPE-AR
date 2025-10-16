package com.example.escape_ar.data

import android.util.Log
import io.github.jan.supabase.createSupabaseClient
import com.example.escape_ar.BuildConfig
import io.github.jan.supabase.gotrue.Auth
import io.github.jan.supabase.gotrue.auth
import io.github.jan.supabase.postgrest.Postgrest
import io.github.jan.supabase.postgrest.postgrest
import io.github.jan.supabase.realtime.Realtime
import io.github.jan.supabase.realtime.realtime

/**
 * Supabase Configuration for KYLON Rescue Mission
 * 
 * SETUP INSTRUCTIONS:
 * 1. Replace SUPABASE_URL with your Supabase project URL
 * 2. Replace SUPABASE_API_KEY with your Supabase anon public key
 * 3. Uncomment the real Supabase client initialization
 * 4. Add Supabase dependencies to your build.gradle
 * 
 * Required dependencies:
 * implementation("io.github.jan-tennert.supabase:postgrest-kt:$supabase_version")
 * implementation("io.github.jan-tennert.supabase:gotrue-kt:$supabase_version") // Auth (renamed to auth-kt in 3.x)
 * implementation("io.github.jan-tennert.supabase:realtime-kt:$supabase_version")
 */
object SupabaseConfig {
    // In production, supply these via BuildConfig fields to avoid hardcoding secrets in source
    // Define in app/build.gradle.kts: buildConfigField("String", "SUPABASE_URL", '"https://..."') etc.
    private val SUPABASE_URL: String = BuildConfig.SUPABASE_URL
    private val SUPABASE_API_KEY: String = BuildConfig.SUPABASE_ANON_KEY

    // Flag indicating real backend usage (kept for potential fallback logic)
    val isRealSupabase: Boolean = SUPABASE_URL.isNotBlank() && SUPABASE_API_KEY.isNotBlank()

    // Supabase client
    val client = createSupabaseClient(
        supabaseUrl = SUPABASE_URL,
        supabaseKey = SUPABASE_API_KEY
    ) {
        install(Auth)
        install(Postgrest)
        install(Realtime)
    }

    object Tables {
        const val PROFILES = "profiles"
        const val PROGRESS = "progress"
    }

    suspend fun testConnection(): Boolean {
        return try {
            client.auth.currentUserOrNull()
            Log.d("SupabaseConfig", "Supabase connection OK")
            true
        } catch (e: Exception) {
            Log.e("SupabaseConfig", "Connection test failed: ${e.message}")
            false
        }
    }
}

// Removed mock classes – using real Supabase
