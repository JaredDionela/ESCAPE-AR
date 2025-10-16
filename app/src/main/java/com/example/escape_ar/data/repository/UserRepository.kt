package com.example.escape_ar.data.repository

import android.content.Context
import android.util.Log
import com.example.escape_ar.data.SupabaseConfig
import com.example.escape_ar.data.SessionManager
import com.example.escape_ar.data.model.*
import kotlinx.coroutines.Dispatchers
import io.github.jan.supabase.gotrue.auth
// Extension functions signUpWith/signInWith unresolved in current environment; using fallback approach
import io.github.jan.supabase.gotrue.providers.builtin.Email
import io.github.jan.supabase.postgrest.postgrest
import kotlinx.coroutines.withContext
import io.ktor.client.*
import io.ktor.client.engine.android.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put
// signInWith/signUpWith extensions appear unavailable in current supabase-kt version; using manual REST only.

// KYLON Mission modules - hard-coded for simplified app architecture
data class KylonModuleData(
    val id: String,
    val title: String,
    val description: String,
    val storylineContext: String,
    val questionCount: Int,
    val difficultyLevel: String = "intermediate",
    val colorHex: String = "#00FFFF"
)

object KylonModules {
    val modules = listOf(
        KylonModuleData(
            id = "decantation",
            title = "Chemical Separation Lab",
            description = "Master decantation and filtration techniques to purify corrupted lab samples.",
            storylineContext = "The Labyrinth has contaminated the water supply. Use your chemistry knowledge to separate clean water from pollutants using decantation principles.",
            questionCount = 12,
            difficultyLevel = "beginner",
            colorHex = "#00FFFF"
        ),
        KylonModuleData(
            id = "organ_system",
            title = "Human Body Systems", 
            description = "Understand how KYLON's biological systems work to ensure optimal performance.",
            storylineContext = "KYLON's bio-mechanical systems are failing. Analyze human organ systems to understand how to repair and enhance KYLON's life support mechanisms.",
            questionCount = 15,
            difficultyLevel = "intermediate",
            colorHex = "#FF4444"
        ),
        KylonModuleData(
            id = "simple_machines",
            title = "Mechanical Engineering",
            description = "Study simple machines and mechanical principles essential for lab equipment.",
            storylineContext = "The Labyrinth's mechanical traps use complex simple machine combinations. Master these principles to navigate the facility and reach KYLON.",
            questionCount = 14,
            difficultyLevel = "intermediate", 
            colorHex = "#FFA500"
        ),
        KylonModuleData(
            id = "solar_system",
            title = "Astronomical Navigation",
            description = "Master solar system knowledge for space-based rescue missions.",
            storylineContext = "KYLON contains star maps essential for humanity's future. Learn the solar system to decode KYLON's astronomical databases and plan the escape route.",
            questionCount = 18,
            difficultyLevel = "advanced",
            colorHex = "#9933FF"
        )
    )
    
    fun getModule(id: String): KylonModuleData? = modules.find { it.id == id }
}

class UserRepository(private val context: Context? = null) {
    private val supabase = SupabaseConfig.client
    private val http = HttpClient(Android)
    private val sessionManager = context?.let { SessionManager.getInstance(it) }
    
    // Access token captured from manual auth; used for authorized Postgrest calls
    private var accessToken: String? = null
    private var currentUserCache: UserInfo? = null

    init {
        // Restore persisted access token if available so REST queries (profile/progress) work after process death
        if (accessToken.isNullOrBlank()) {
            accessToken = sessionManager?.getAccessToken()
            if (!accessToken.isNullOrBlank()) {
                Log.d("UserRepository", "Restored access token from SessionManager")
            } else {
                Log.d("UserRepository", "No stored access token found")
            }
        }
        sessionManager?.getCurrentUser()?.let { cachedUser ->
            currentUserCache = cachedUser
            Log.d("UserRepository", "Loaded cached user from SessionManager: ${cachedUser.fullName}")
        } ?: run {
            Log.d("UserRepository", "No cached user found in SessionManager")
        }
    }
    
    /**
     * Ensures the Supabase base URL always has an https scheme and no trailing slash.
     */
    private fun normalizeBaseUrl(raw: String): String {
        var v = raw.trim()
        if (v.endsWith("/")) v = v.dropLast(1)
        if (v.isNotEmpty() && !v.startsWith("http")) {
            v = "https://$v"
        }
        return v
    }

    private fun extractSupabaseError(body: String): String {
        return try {
            val el = Json.parseToJsonElement(body).jsonObject
            val msgKeys = listOf("message", "msg", "error_description", "error")
            var found: String? = null
            for (k in msgKeys) {
                val v = el[k]?.jsonPrimitive?.content
                if (!v.isNullOrBlank()) { found = v; break }
            }
            found ?: body.take(200)
        } catch (e: Exception) {
            body.take(200)
        }
    }
    
    suspend fun signUp(
        email: String, 
        password: String, 
        fullName: String = "",
        teacherName: String? = null,
        section: String? = null
    ): Result<UserInfo> = withContext(Dispatchers.IO) {
        val displayName = fullName.ifEmpty { email.substringBefore("@").replaceFirstChar { it.uppercase() } }
        try {
            val rawBase = SupabaseConfig.client.supabaseUrl
            val base = normalizeBaseUrl(rawBase)
            if (base.isBlank() || base.contains("localhost", ignoreCase = true)) {
                return@withContext Result.failure(IllegalStateException("Invalid Supabase URL (localhost)"))
            }
            val url = "$base/auth/v1/signup"
            val safeName = displayName.replace("\"", "\\\"")
            val response = http.post(url) {
                header("apikey", SupabaseConfig.client.supabaseKey)
                contentType(ContentType.Application.Json)
                header("Accept", "application/json")
                setBody("""{"email":"$email","password":"$password","data":{"full_name":"$safeName"}}""")
            }
            val body = response.bodyAsText()
            if (!response.status.isSuccess()) {
                val msg = extractSupabaseError(body)
                Log.e("UserRepository", "signUp failed status=${response.status} body=$body")
                return@withContext Result.failure(IllegalStateException("Sign up failed: $msg"))
            }
            // Try immediate sign in to obtain session
            val signInResult = signIn(email, password)
            signInResult.getOrNull()?.let { userInfo ->
                // Create extended profile with teacher name and section (replaces basic profile)
                ensureExtendedProfileAndSettings(userInfo.id, displayName, teacherName, section, email)
            }
            signInResult
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    suspend fun signIn(email: String, password: String): Result<UserInfo> = withContext(Dispatchers.IO) {
        try {
            val rawBase = SupabaseConfig.client.supabaseUrl
            val base = normalizeBaseUrl(rawBase)
            if (base.isBlank() || base.contains("localhost", ignoreCase = true)) {
                return@withContext Result.failure(IllegalStateException("Invalid Supabase URL (localhost)"))
            }
            val url = "$base/auth/v1/token?grant_type=password"
            val response = http.post(url) {
                header("apikey", SupabaseConfig.client.supabaseKey)
                contentType(ContentType.Application.Json)
                header("Accept", "application/json")
                setBody("""{"email":"$email","password":"$password"}""")
            }
            val body = response.bodyAsText()
            if (!response.status.isSuccess()) {
                val msg = extractSupabaseError(body)
                Log.e("UserRepository", "signIn failed status=${response.status} body=$body")
                return@withContext Result.failure(IllegalStateException("Sign in failed: $msg"))
            }
            val json = Json.parseToJsonElement(body).jsonObject
            val userId = json["user"]?.jsonObject?.get("id")?.jsonPrimitive?.content ?: ""
            val fullName = json["user"]?.jsonObject?.get("user_metadata")?.jsonObject
                ?.get("full_name")?.jsonPrimitive?.content ?: email.substringBefore('@')
            val accessToken = json["access_token"]?.jsonPrimitive?.content
            val refreshToken = json["refresh_token"]?.jsonPrimitive?.content
            // cache for later REST requests & persist
            this@UserRepository.accessToken = accessToken
            // Cannot call importSession (unavailable); rely on manual tokens for future calls by adding Authorization header manually later if needed.
            if (userId.isNotBlank()) ensureProfile(userId, fullName, email)
            val info = UserInfo(id = userId, email = email, fullName = fullName, role = "student")
            currentUserCache = info
            // Save session including tokens
            sessionManager?.saveAuthSession(info, accessToken, refreshToken)
            
            Result.success(info)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    suspend fun signOut() {
        try {
            supabase.auth.signOut()
            sessionManager?.clearSession()
            currentUserCache = null
            accessToken = null
            Log.d("UserRepository", "Sign out successful")
        } catch (e: Exception) {
            Log.e("UserRepository", "Error signing out", e)
        }
    }
    
    /**
     * Update extended user profile (display name, teacher name, section)
     */
    suspend fun updateExtendedProfile(
        userId: String,
        displayName: String,
        teacherName: String?,
        section: String?
    ): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            val token = accessToken ?: return@withContext Result.failure(
                IllegalStateException("Not authenticated")
            )
            
            val base = normalizeBaseUrl(supabase.supabaseUrl)
            val url = "$base/rest/v1/profiles?id=eq.$userId"
            
            val body = buildJsonObject {
                put("display_name", displayName)
                if (teacherName != null) {
                    put("teacher_name", teacherName)
                } else {
                    put("teacher_name", kotlinx.serialization.json.JsonNull)
                }
                if (section != null) {
                    put("section", section)
                } else {
                    put("section", kotlinx.serialization.json.JsonNull)
                }
            }.toString()
            
            val response = http.patch(url) {
                header("apikey", supabase.supabaseKey)
                header("Authorization", "Bearer $token")
                header("Prefer", "return=minimal")
                contentType(ContentType.Application.Json)
                setBody(body)
            }
            
            if (response.status.isSuccess()) {
                Log.d("UserRepository", "Profile updated successfully")
                
                // Update cached user
                currentUserCache = currentUserCache?.copy(fullName = displayName)
                sessionManager?.getCurrentUser()?.let { user ->
                    sessionManager.saveAuthSession(
                        user.copy(fullName = displayName),
                        accessToken,
                        null
                    )
                }
                
                Result.success(Unit)
            } else {
                val errorBody = response.bodyAsText()
                Log.e("UserRepository", "Profile update failed: ${response.status} - $errorBody")
                Result.failure(IllegalStateException("Failed to update profile: ${response.status}"))
            }
        } catch (e: Exception) {
            Log.e("UserRepository", "Exception updating profile", e)
            Result.failure(e)
        }
    }
    
    /**
     * Get extended user profile (includes teacher name and section)
     */
    suspend fun getExtendedUserProfile(userId: String): Result<ExtendedUserProfile> = withContext(Dispatchers.IO) {
        try {
            val token = accessToken ?: return@withContext Result.failure(
                IllegalStateException("Not authenticated")
            )
            
            val base = normalizeBaseUrl(supabase.supabaseUrl)
            val url = "$base/rest/v1/profiles?id=eq.$userId&select=*"
            
            val response = http.get(url) {
                header("apikey", supabase.supabaseKey)
                header("Authorization", "Bearer $token")
            }
            
            if (response.status.isSuccess()) {
                val responseBody = response.bodyAsText()
                Log.d("UserRepository", "Extended profile response: $responseBody")
                
                val jsonArray = Json.parseToJsonElement(responseBody).jsonArray
                if (jsonArray.isEmpty()) {
                    return@withContext Result.failure(
                        IllegalStateException("No profile found for user $userId")
                    )
                }
                
                val profile = Json.decodeFromString<ExtendedUserProfile>(jsonArray[0].toString())
                Log.d("UserRepository", "Loaded extended profile: ${profile.displayName}, teacher=${profile.teacherName}, section=${profile.section}")
                
                Result.success(profile)
            } else {
                val errorBody = response.bodyAsText()
                Log.e("UserRepository", "Failed to fetch profile: ${response.status} - $errorBody")
                Result.failure(IllegalStateException("Failed to fetch profile: ${response.status}"))
            }
        } catch (e: Exception) {
            Log.e("UserRepository", "Exception fetching extended profile", e)
            Result.failure(e)
        }
    }
    
    /**
     * Send password reset email
     */
    suspend fun resetPassword(email: String): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            val base = normalizeBaseUrl(supabase.supabaseUrl)
            val url = "$base/auth/v1/recover"
            
            val response = http.post(url) {
                header("apikey", supabase.supabaseKey)
                contentType(ContentType.Application.Json)
                setBody("""{"email":"$email"}""")
            }
            
            if (response.status.isSuccess()) {
                Log.d("UserRepository", "Password reset email sent to $email")
                Result.success(Unit)
            } else {
                val errorBody = response.bodyAsText()
                Log.e("UserRepository", "Password reset failed: ${response.status} - $errorBody")
                Result.failure(IllegalStateException("Failed to send reset email"))
            }
        } catch (e: Exception) {
            Log.e("UserRepository", "Exception resetting password", e)
            Result.failure(e)
        }
    }
    
    suspend fun getCurrentUser(): UserInfo? = withContext(Dispatchers.IO) {
        Log.d("UserRepository", "getCurrentUser() called")
        
        // First check SessionManager for cached user
        sessionManager?.getCurrentUser()?.let { cachedUser ->
            Log.d("UserRepository", "Using cached user: ${cachedUser.fullName}")
            currentUserCache = cachedUser
            return@withContext cachedUser
        }
        
        Log.d("UserRepository", "No cached user, attempting Supabase auth check...")
        
        try {
            val user = supabase.auth.currentUserOrNull()
            if (user != null) {
                Log.d("UserRepository", "Found Supabase user: ${user.email}")
                
                // First try to get name from user metadata
                val metaName = user.userMetadata?.get("full_name")?.toString()?.removeSurrounding("\"")
                Log.d("UserRepository", "User metadata full_name: $metaName")
                
                // If no metadata name, try fetching from profiles table
                val profileName = if (metaName.isNullOrBlank()) {
                    Log.d("UserRepository", "No metadata name, fetching from profiles table...")
                    fetchProfileFullName(user.id)
                } else metaName
                
                // Fallback to email prefix if no name found
                val fullName = profileName ?: user.email?.substringBefore('@')?.replaceFirstChar { 
                    if (it.isLowerCase()) it.titlecase() else it.toString() 
                }.orEmpty()
                
                Log.d("UserRepository", "Final fullName resolved: $fullName")
                
                val info = UserInfo(
                    id = user.id, 
                    email = user.email ?: "", 
                    fullName = fullName, 
                    role = "student"
                )
                currentUserCache = info
                
                // Save to SessionManager (without tokens if not yet available)
                if (!accessToken.isNullOrBlank()) {
                    sessionManager?.saveAuthSession(info, accessToken, sessionManager.getRefreshToken())
                } else {
                    sessionManager?.saveSession(info)
                }
                
                // Ensure profile exists in database with current info
                ensureProfile(user.id, fullName, user.email ?: "")
                
                Log.d("UserRepository", "Current user loaded: ${info.fullName} (${info.email})")
                info
            } else {
                Log.d("UserRepository", "No current Supabase user session")
                // Return cached user if available
                currentUserCache
            }
        } catch (e: Exception) {
            Log.e("UserRepository", "Error getting current user", e)
            // Fallback to SessionManager cache
            sessionManager?.getCurrentUser() ?: currentUserCache
        }
    }
    
    // Quiz and module methods using real Supabase database
    suspend fun getUserModulesWithProgress(userId: String): List<Module> = withContext(Dispatchers.IO) {
        if (!SupabaseConfig.isRealSupabase) {
            Log.d("UserRepository", "Using mock data - Supabase not configured")
            return@withContext KylonModules.modules.map { k ->
                Module(id = k.id, name = k.title, description = k.description, totalQuestions = k.questionCount)
            }
        }
        
        val token = accessToken
        if (token.isNullOrBlank()) {
            Log.w("UserRepository", "No access token available; returning base modules without progress")
            return@withContext KylonModules.modules.map { k -> 
                Module(k.id, k.title, k.description, k.questionCount, isCompleted = false, score = null) 
            }
        }
        
        try {
            val base = normalizeBaseUrl(supabase.supabaseUrl)
            val url = "$base/rest/v1/progress?user_id=eq.$userId&select=user_id,module,completed,best_score"
            
            Log.d("UserRepository", "Fetching progress for user: $userId")
            
            val resp = http.get(url) {
                header("apikey", supabase.supabaseKey)
                header("Authorization", "Bearer $token")
                header("Accept", "application/json")
            }
            
            val text = resp.bodyAsText()
            if (!resp.status.isSuccess()) {
                Log.e("UserRepository", "Progress fetch failed: ${resp.status} $text")
                throw IllegalStateException("Progress fetch failed: ${resp.status} $text")
            }
            
            val jsonArray = Json.parseToJsonElement(text).jsonArray
            val rows: List<UserProgress> = jsonArray.mapNotNull { element ->
                try {
                    val obj = element.jsonObject
                    val userIdVal = obj["user_id"]?.jsonPrimitive?.content ?: return@mapNotNull null
                    val moduleVal = obj["module"]?.jsonPrimitive?.content ?: return@mapNotNull null
                    val completedVal = obj["completed"]?.jsonPrimitive?.content?.equals("true", ignoreCase = true) ?: false
                    val bestScoreVal = obj["best_score"]?.jsonPrimitive?.content?.toFloatOrNull() ?: 0f
                    UserProgress(userId = userIdVal, module = moduleVal, completed = completedVal, bestScore = bestScoreVal)
                } catch (e: Exception) { 
                    Log.w("UserRepository", "Error parsing progress row: ${e.message}")
                    null 
                }
            }
            
            Log.d("UserRepository", "Loaded ${rows.size} progress records")
            
            val progressMap: Map<String, UserProgress> = rows.associateBy { it.module }
            
            KylonModules.modules.map { km ->
                val row = progressMap[km.id]
                Module(
                    id = km.id,
                    name = km.title,
                    description = km.description,
                    totalQuestions = km.questionCount,
                    isCompleted = row?.completed ?: false,
                    score = row?.bestScore
                ).also {
                    Log.d("UserRepository", "Module ${km.title}: completed=${it.isCompleted}, score=${it.score}")
                }
            }
        } catch (e: Exception) {
            Log.e("UserRepository", "Error fetching progress via REST", e)
            // Return base modules without progress rather than empty list
            KylonModules.modules.map { k -> 
                Module(k.id, k.title, k.description, k.questionCount, isCompleted = false, score = null) 
            }
        }
    }
    
    suspend fun getUserQuizProgress(userId: String): List<QuizProgress> = withContext(Dispatchers.IO) {
        if (!SupabaseConfig.isRealSupabase) return@withContext emptyList()
        val token = accessToken ?: return@withContext emptyList()
        try {
            val base = normalizeBaseUrl(supabase.supabaseUrl)
            val url = "$base/rest/v1/progress?user_id=eq.$userId&select=user_id,module,best_score"
            val resp = http.get(url) {
                header("apikey", supabase.supabaseKey)
                header("Authorization", "Bearer $token")
                header("Accept", "application/json")
            }
            val text = resp.bodyAsText()
            if (!resp.status.isSuccess()) return@withContext emptyList()
            val jsonArray = Json.parseToJsonElement(text).jsonArray
            jsonArray.mapNotNull { element ->
                try {
                    val obj = element.jsonObject
                    val moduleVal = obj["module"]?.jsonPrimitive?.content ?: return@mapNotNull null
                    val scoreVal = obj["best_score"]?.jsonPrimitive?.content?.toFloatOrNull() ?: 0f
                    QuizProgress(
                        id = "${userId}_${moduleVal}",
                        userId = userId,
                        module = moduleVal,
                        score = scoreVal,
                        completedAt = null,
                        questionsAnswered = scoreVal.toInt(),
                        totalQuestions = KylonModules.getModule(moduleVal)?.questionCount ?: 10
                    )
                } catch (e: Exception) { null }
            }
        } catch (e: Exception) { emptyList() }
    }
    
    suspend fun saveQuizProgress(quizProgress: QuizProgress) = withContext(Dispatchers.IO) {
        Log.d("UserRepository", "saveQuizProgress called: module=${quizProgress.module}, score=${quizProgress.score}, userId=${quizProgress.userId}")
        
        if (!SupabaseConfig.isRealSupabase) {
            Log.d("UserRepository", "(Mock) Quiz progress accepted for ${quizProgress.module} score ${quizProgress.score}")
            return@withContext
        }
        
        val token = accessToken
        if (token == null) {
            Log.e("UserRepository", "No access token available for saving quiz progress")
            return@withContext
        }
        
        Log.d("UserRepository", "Attempting to save quiz progress with token present")
        
        try {
            // First, get existing record to preserve best score
            val currentBestScore = getCurrentBestScore(quizProgress.userId, quizProgress.module)
            val newScore = quizProgress.score.toInt()
            val finalBestScore = maxOf(currentBestScore, newScore)
            val isCompleted = finalBestScore >= 70
            
            Log.d("UserRepository", "Score comparison: current=$currentBestScore, new=$newScore, final=$finalBestScore")
            
            val base = normalizeBaseUrl(supabase.supabaseUrl)
            
            if (currentBestScore > 0) {
                // Update existing record
                Log.d("UserRepository", "Updating existing record...")
                val updateUrl = "$base/rest/v1/progress?user_id=eq.${quizProgress.userId}&module=eq.${quizProgress.module}"
                val updatePayload = buildJsonObject {
                    put("best_score", finalBestScore)
                    put("completed", isCompleted)
                }.toString()
                
                Log.d("UserRepository", "PATCH $updateUrl with payload: $updatePayload")
                
                val updateResp = http.patch(updateUrl) {
                    header("apikey", supabase.supabaseKey)
                    header("Authorization", "Bearer $token")
                    header("Content-Type", "application/json")
                    setBody(updatePayload)
                }
                val updateText = updateResp.bodyAsText()
                if (!updateResp.status.isSuccess()) {
                    Log.e("UserRepository", "Progress update failed ${updateResp.status} $updateText")
                } else {
                    Log.d("UserRepository", "Progress updated successfully for ${quizProgress.module} with score $finalBestScore")
                }
            } else {
                // Insert new record
                Log.d("UserRepository", "Inserting new record...")
                val insertUrl = "$base/rest/v1/progress"
                val insertPayload = buildJsonObject {
                    put("user_id", quizProgress.userId)
                    put("module", quizProgress.module)
                    put("best_score", finalBestScore)
                    put("completed", isCompleted)
                }.toString()
                
                Log.d("UserRepository", "POST $insertUrl with payload: $insertPayload")
                
                val insertResp = http.post(insertUrl) {
                    header("apikey", supabase.supabaseKey)
                    header("Authorization", "Bearer $token")
                    header("Content-Type", "application/json")
                    setBody(insertPayload)
                }
                val insertText = insertResp.bodyAsText()
                if (!insertResp.status.isSuccess()) {
                    Log.e("UserRepository", "Progress insert failed ${insertResp.status} $insertText")
                } else {
                    Log.d("UserRepository", "Progress inserted successfully for ${quizProgress.module} with score $finalBestScore")
                }
            }
        } catch (e: Exception) {
            Log.e("UserRepository", "Error saving quiz progress", e)
        }
    }
    
    private suspend fun getCurrentBestScore(userId: String, module: String): Int {
        val token = accessToken ?: return 0
        return try {
            val base = normalizeBaseUrl(supabase.supabaseUrl)
            val url = "$base/rest/v1/progress?user_id=eq.$userId&module=eq.$module&select=best_score"
            val resp = http.get(url) {
                header("apikey", supabase.supabaseKey)
                header("Authorization", "Bearer $token")
            }
            val text = resp.bodyAsText()
            if (resp.status.isSuccess()) {
                val json = Json.parseToJsonElement(text).jsonArray
                if (json.isNotEmpty()) {
                    val score = json[0].jsonObject["best_score"]?.jsonPrimitive?.content?.toIntOrNull() ?: 0
                    Log.d("UserRepository", "Found existing best score for $module: $score")
                    score
                } else {
                    Log.d("UserRepository", "No existing record found for $module")
                    0
                }
            } else {
                Log.e("UserRepository", "Failed to get current best score: ${resp.status}")
                0
            }
        } catch (e: Exception) {
            Log.e("UserRepository", "Error getting current best score", e)
            0
        }
    }

    suspend fun updateProfile(userId: String, fullName: String): Result<Unit> = withContext(Dispatchers.IO) {
        if (!SupabaseConfig.isRealSupabase) {
            Log.d("UserRepository", "(Mock) Profile update accepted")
            return@withContext Result.success(Unit)
        }
        
        val token = accessToken ?: return@withContext Result.failure(IllegalStateException("No access token"))
        
        try {
            val base = normalizeBaseUrl(supabase.supabaseUrl)
            val url = "$base/rest/v1/profiles?id=eq.$userId"
            val body = buildJsonObject {
                put("full_name", fullName)
            }.toString()
            
            val resp = http.patch(url) {
                header("apikey", supabase.supabaseKey)
                header("Authorization", "Bearer $token")
                header("Prefer", "return=minimal")
                contentType(ContentType.Application.Json)
                setBody(body)
            }
            
            if (!resp.status.isSuccess()) {
                val errorText = resp.bodyAsText()
                Log.e("UserRepository", "Profile update failed ${resp.status} $errorText")
                return@withContext Result.failure(IllegalStateException("Profile update failed: $errorText"))
            }
            
            // Update cached user
            currentUserCache?.let {
                currentUserCache = it.copy(fullName = fullName)
            }
            
            Log.d("UserRepository", "Profile updated successfully")
            Result.success(Unit)
        } catch (e: Exception) {
            Log.e("UserRepository", "Error updating profile", e)
            Result.failure(e)
        }
    }

    // --- Internal helpers ---
    private suspend fun ensureProfile(userId: String, fullName: String, email: String) {
        if (userId.isBlank()) return
        val token = accessToken ?: return
        try {
            val base = normalizeBaseUrl(supabase.supabaseUrl)
            val url = "$base/rest/v1/profiles"
            val body = buildJsonObject {
                put("id", userId)
                put("full_name", fullName)
                put("email", email)
            }.toString()
            val resp = http.post(url) {
                header("apikey", supabase.supabaseKey)
                header("Authorization", "Bearer $token")
                header("Prefer", "resolution=merge-duplicates")
                contentType(ContentType.Application.Json)
                setBody(body)
            }
            if (!resp.status.isSuccess()) {
                Log.w("UserRepository", "Profile upsert failed ${resp.status}")
            } else {
                Log.d("UserRepository", "Profile ensured for $userId")
            }
        } catch (e: Exception) {
            Log.w("UserRepository", "Failed to ensure profile: ${e.message}")
        }
    }
    
    /**
     * Create extended profile with teacher name and section, plus default settings
     */
    private suspend fun ensureExtendedProfileAndSettings(
        userId: String,
        displayName: String,
        teacherName: String?,
        section: String?,
        email: String
    ) {
        if (userId.isBlank()) return
        val token = accessToken ?: return
        
        try {
            val base = normalizeBaseUrl(supabase.supabaseUrl)
            
            // Create extended profile in profiles table (if it doesn't already exist)
            val profileUrl = "$base/rest/v1/profiles"
            val profileBody = buildJsonObject {
                put("id", userId)
                put("email", email)
                put("full_name", displayName)
                put("display_name", displayName)
                if (!teacherName.isNullOrBlank()) put("teacher_name", teacherName)
                if (!section.isNullOrBlank()) put("section", section)
            }.toString()
            
            Log.d("UserRepository", "Creating extended profile - userId: $userId, email: $email, displayName: $displayName, teacherName: '$teacherName', section: '$section'")
            Log.d("UserRepository", "Profile JSON body: $profileBody")
            
            val profileResp = http.post(profileUrl) {
                header("apikey", supabase.supabaseKey)
                header("Authorization", "Bearer $token")
                header("Prefer", "resolution=merge-duplicates")
                contentType(ContentType.Application.Json)
                setBody(profileBody)
            }
            
            if (profileResp.status.isSuccess()) {
                val responseBody = profileResp.bodyAsText()
                Log.d("UserRepository", "Extended profile created for $userId - Response: $responseBody")
            } else {
                val errorBody = profileResp.bodyAsText()
                Log.w("UserRepository", "Extended profile creation failed: ${profileResp.status} - $errorBody")
            }
            
            // Create default settings
            val settingsUrl = "$base/rest/v1/user_settings"
            val settingsBody = buildJsonObject {
                put("user_id", userId)
                put("music_volume", 0.7)
                put("sfx_volume", 0.7)
                put("captions_enabled", true)
            }.toString()
            
            val settingsResp = http.post(settingsUrl) {
                header("apikey", supabase.supabaseKey)
                header("Authorization", "Bearer $token")
                header("Prefer", "resolution=merge-duplicates")
                contentType(ContentType.Application.Json)
                setBody(settingsBody)
            }
            
            if (settingsResp.status.isSuccess()) {
                Log.d("UserRepository", "Default settings created for $userId")
            } else {
                Log.w("UserRepository", "Settings creation failed: ${settingsResp.status}")
            }
            
        } catch (e: Exception) {
            Log.w("UserRepository", "Failed to create extended profile/settings: ${e.message}")
        }
    }

    private suspend fun fetchProfileFullName(userId: String): String? {
        val token = accessToken ?: return null
        return try {
            val base = normalizeBaseUrl(supabase.supabaseUrl)
            val url = "$base/rest/v1/profiles?id=eq.$userId&select=full_name"
            val resp = http.get(url) {
                header("apikey", supabase.supabaseKey)
                header("Authorization", "Bearer $token")
                header("Accept", "application/json")
            }
            val text = resp.bodyAsText()
            if (!resp.status.isSuccess()) return null
            val jsonArray = Json.parseToJsonElement(text).jsonArray
            jsonArray.firstOrNull()?.jsonObject?.get("full_name")?.jsonPrimitive?.content
        } catch (e: Exception) { null }
    }
}
