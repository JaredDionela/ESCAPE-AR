package com.example.escape_ar

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.DisposableEffect
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.escape_ar.data.SupabaseConfig
import com.example.escape_ar.ui.screens.*
import com.example.escape_ar.ui.theme.ESCAPEARTheme
import com.example.escape_ar.unity.UnityBridge
import com.example.escape_ar.unity.UnityHolderActivity
import java.io.File

class MainActivity : ComponentActivity() {
    
    // Audio manager for app-wide audio control
    private lateinit var audioManager: com.example.escape_ar.utils.AudioManager
    
    // Unity activity launcher with result handling
    private val unityLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        // Handle Unity exit
        if (result.resultCode == UnityHolderActivity.UNITY_EXIT_CODE) {
            android.util.Log.d("MainActivity", "Returned from Unity experience")
            Toast.makeText(this, "Welcome back, Agent! 🎮", Toast.LENGTH_SHORT).show()
        }
    }
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        
        // Initialize AudioManager
        audioManager = com.example.escape_ar.utils.AudioManager.getInstance(this)
        
        // Load saved audio settings and start background music if enabled
        val sharedPrefs = getSharedPreferences("escape_ar_settings", Context.MODE_PRIVATE)
        val backgroundMusicEnabled = sharedPrefs.getBoolean("background_music", true)
        val musicVolume = sharedPrefs.getFloat("music_volume", 0.5f)
        val soundEffectsEnabled = sharedPrefs.getBoolean("sound_effects", true)
        val effectsVolume = sharedPrefs.getFloat("effects_volume", 0.7f)
        
        audioManager.setMusicEnabled(backgroundMusicEnabled)
        audioManager.setMusicVolume(musicVolume)
        audioManager.setEffectsEnabled(soundEffectsEnabled)
        audioManager.setEffectsVolume(effectsVolume)
        
        // Don't start background music here - it will start after successful login
        
        setContent {
            ESCAPEARTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    AppNavigation(
                        modifier = Modifier.padding(innerPadding),
                        onStartUnity = { startUnityActivity() }
                    )
                }
            }
        }
    }
    
    override fun onResume() {
        super.onResume()
        // Only resume background music if user is logged in
        val sessionManager = com.example.escape_ar.data.SessionManager.getInstance(this)
        val sharedPrefs = getSharedPreferences("escape_ar_settings", Context.MODE_PRIVATE)
        
        if (sessionManager.isLoggedIn() && sharedPrefs.getBoolean("background_music", true)) {
            audioManager.resumeBackgroundMusic()
        }
    }
    
    override fun onPause() {
        super.onPause()
        // Pause background music when app goes to background
        audioManager.pauseBackgroundMusic()
    }
    
    override fun onDestroy() {
        super.onDestroy()
        // Release audio resources
        audioManager.release()
    }
    
    private fun startUnityActivity() {
        android.util.Log.d("MainActivity", "startUnityActivity called")
        
        try {
            // Try to launch Unity directly
            val unityIntent = Intent(this, UnityHolderActivity::class.java)
            unityLauncher.launch(unityIntent)
            android.util.Log.d("MainActivity", "Unity activity launch attempted")
        } catch (e: Exception) {
            android.util.Log.e("MainActivity", "Failed to launch Unity AR experience", e)
            Toast.makeText(this, "Error: ${e.message}", Toast.LENGTH_LONG).show()
            showUnityNotAvailableDialog()
        }
    }
    
    private fun showUnityNotAvailableDialog() {
        // Create a simple alert dialog
        runOnUiThread {
            android.app.AlertDialog.Builder(this)
                .setTitle("🎮 AR Experience Unavailable")
                .setMessage("The Labyrinth AR experience requires Unity integration. Complete the training modules first to prepare for the mission!\n\nNote: AR functionality will be available in future releases.")
                .setPositiveButton("Continue Training") { dialog, _ ->
                    dialog.dismiss()
                }
                .setNeutralButton("Learn More") { dialog, _ ->
                    dialog.dismiss()
                    // Could open a webpage about AR features
                }
                .setIcon(android.R.drawable.ic_dialog_info)
                .show()
        }
    }
}

@Composable
fun AppNavigation(
    modifier: Modifier = Modifier,
    onStartUnity: () -> Unit,
    navController: NavHostController = rememberNavController()
) {
    NavHost(
        modifier = modifier,
        navController = navController,
        startDestination = "loading"
    ) {
        composable("loading") {
            val context = androidx.compose.ui.platform.LocalContext.current
            val sessionManager = com.example.escape_ar.data.SessionManager.getInstance(context)
            val audioManager = remember { com.example.escape_ar.utils.AudioManager.getInstance(context) }
            
            LoadingScreen {
                // Check if terms have been accepted
                val sharedPrefs = context.getSharedPreferences("escape_ar_settings", Context.MODE_PRIVATE)
                val termsAccepted = sharedPrefs.getBoolean("terms_accepted", false)
                
                if (!termsAccepted) {
                    // Show terms first
                    android.util.Log.d("MainActivity", "Terms not accepted, showing T&C")
                    navController.navigate("terms_and_conditions") {
                        popUpTo("loading") { inclusive = true }
                    }
                } else if (sessionManager.isLoggedIn()) {
                    // User already logged in - start background music
                    android.util.Log.d("MainActivity", "User already logged in, navigating to student")
                    if (sharedPrefs.getBoolean("background_music", true)) {
                        audioManager.startBackgroundMusic()
                    }
                    navController.navigate("student") {
                        popUpTo("loading") { inclusive = true }
                    }
                } else {
                    // No session found, go to auth (no music on auth screen)
                    android.util.Log.d("MainActivity", "No session found, navigating to auth")
                    navController.navigate("auth") {
                        popUpTo("loading") { inclusive = true }
                    }
                }
            }
        }
        
        // Terms and Conditions Screen
        composable("terms_and_conditions") {
            TermsAndConditionsScreen(navController = navController)
        }
        
        composable("auth") {
            val context = androidx.compose.ui.platform.LocalContext.current
            val audioManager = remember { com.example.escape_ar.utils.AudioManager.getInstance(context) }
            
            AuthScreen(
                onLoginSuccess = {
                    // Start background music after successful login
                    val sharedPrefs = context.getSharedPreferences("escape_ar_settings", Context.MODE_PRIVATE)
                    if (sharedPrefs.getBoolean("background_music", true)) {
                        audioManager.startBackgroundMusic()
                    }
                    
                    navController.navigate("student") {
                        popUpTo("auth") { inclusive = true }
                    }
                }
            )
        }
        
        composable("student") {
            val context = androidx.compose.ui.platform.LocalContext.current
            
            StudentMainScreen(
                onQuizClick = { moduleId ->
                    android.util.Log.d("MainActivity", "Quiz clicked with moduleId: $moduleId")
                    if (moduleId.isEmpty()) {
                        navController.navigate("quiz")
                    } else {
                        navController.navigate("quiz/$moduleId")
                    }
                },
                onLessonsClick = { moduleId, moduleName ->
                    navController.navigate("lessons/$moduleId/$moduleName")
                },
                onProfileClick = {
                    navController.navigate("profile")
                },
                onUnityLaunch = {
                    android.util.Log.d("MainActivity", "🎮 AR Button clicked - calling onStartUnity")
                    onStartUnity()
                },
                onLogout = {
                    // Stop background music when logging out
                    val audioManager = com.example.escape_ar.utils.AudioManager.getInstance(context)
                    audioManager.stopBackgroundMusic()
                    
                    // Clear the session before navigating
                    val sessionManager = com.example.escape_ar.data.SessionManager.getInstance(context)
                    sessionManager.clearSession()
                    android.util.Log.d("MainActivity", "Session cleared, navigating to auth")
                    
                    navController.navigate("auth") {
                        popUpTo("student") { inclusive = true }
                    }
                }
            )
        }
        
        composable("profile") {
            ProfileScreen(
                onNavigateBack = {
                    navController.popBackStack()
                },
                onNavigateToSettings = {
                    navController.navigate("settings")
                }
            )
        }
        
        // Settings Screen
        composable("settings") {
            val context = androidx.compose.ui.platform.LocalContext.current
            val audioManager = remember { com.example.escape_ar.utils.AudioManager.getInstance(context) }
            
            SettingsScreen(
                navController = navController,
                onBackgroundMusicToggle = { enabled ->
                    audioManager.setMusicEnabled(enabled)
                    if (enabled) {
                        audioManager.startBackgroundMusic()
                    } else {
                        audioManager.stopBackgroundMusic()
                    }
                    android.util.Log.d("MainActivity", "Background Music: $enabled")
                },
                onMusicVolumeChange = { volume ->
                    audioManager.setMusicVolume(volume)
                    android.util.Log.d("MainActivity", "Music Volume: ${(volume * 100).toInt()}%")
                },
                onSoundEffectsToggle = { enabled ->
                    audioManager.setEffectsEnabled(enabled)
                    android.util.Log.d("MainActivity", "Sound Effects: $enabled")
                },
                onEffectsVolumeChange = { volume ->
                    audioManager.setEffectsVolume(volume)
                    android.util.Log.d("MainActivity", "Effects Volume: ${(volume * 100).toInt()}%")
                },
                onWifiOnlyToggle = { enabled ->
                    android.util.Log.d("MainActivity", "Wi-Fi Only: $enabled")
                },
                onClearCache = {
                    // Clear Unity cache
                    try {
                        val unityDataDir = File(context.filesDir, "UnityCache")
                        if (unityDataDir.exists()) {
                            val deleted = unityDataDir.deleteRecursively()
                            Toast.makeText(
                                context, 
                                if (deleted) "Cache cleared successfully" else "Failed to clear cache",
                                Toast.LENGTH_SHORT
                            ).show()
                        } else {
                            Toast.makeText(context, "No cache to clear", Toast.LENGTH_SHORT).show()
                        }
                    } catch (e: Exception) {
                        Toast.makeText(context, "Error: ${e.message}", Toast.LENGTH_SHORT).show()
                    }
                }
            )
        }
        
        // Edit Profile Screen
        composable("edit_profile") {
            EditProfileScreen(
                navController = navController,
                supabaseClient = SupabaseConfig.client
            )
        }
        
        // Change Password Screen
        composable("change_password") {
            ChangePasswordScreen(
                navController = navController,
                supabaseClient = SupabaseConfig.client
            )
        }
        
        // About Screen
        composable("about") {
            AboutScreen(navController = navController)
        }
        
        // Settings Terms & Privacy Screen
        composable("settings_terms") {
            SettingsTermsScreen(navController = navController)
        }
        
        composable("quiz") {
            android.util.Log.e("MainActivity", "🔥🔥🔥 QUIZ NAVIGATION - no moduleId")
            android.util.Log.wtf("MainActivity", "ABOUT TO CALL QuizScreen() with empty moduleId")
            
            // Force fresh composition with unique key
            androidx.compose.runtime.key("quiz_no_module") {
                QuizScreen(
                    moduleId = "",
                    onNavigateBack = {
                        navController.popBackStack()
                    }
                )
            }
            
            android.util.Log.wtf("MainActivity", "AFTER calling QuizScreen()")
        }
        
        composable(
            route = "quiz/{moduleId}",
            arguments = listOf(
                androidx.navigation.navArgument("moduleId") {
                    type = androidx.navigation.NavType.StringType
                    defaultValue = ""
                }
            )
        ) { backStackEntry ->
            val moduleId = backStackEntry.arguments?.getString("moduleId") ?: ""
            android.util.Log.e("MainActivity", "🔥🔥🔥 QUIZ NAVIGATION - moduleId: $moduleId")
            
            // Force fresh composition with unique key based on moduleId
            androidx.compose.runtime.key("quiz_$moduleId") {
                QuizScreen(
                    moduleId = moduleId,
                    onNavigateBack = {
                        navController.popBackStack()
                    }
                )
            }
        }
        
        composable("lessons/{moduleId}/{moduleName}") { backStackEntry ->
            val moduleId = backStackEntry.arguments?.getString("moduleId") ?: ""
            val moduleName = backStackEntry.arguments?.getString("moduleName") ?: ""
            LessonsScreen(
                moduleId = moduleId,
                moduleName = moduleName,
                onBackClick = {
                    navController.popBackStack()
                },
                onLessonClick = { lessonId, lessonTitle, youtubeVideoId ->
                    navController.navigate("videoplayer/$lessonId/$lessonTitle/$youtubeVideoId")
                }
            )
        }
        
        composable("videoplayer/{lessonId}/{lessonTitle}/{youtubeVideoId}") { backStackEntry ->
            val lessonId = backStackEntry.arguments?.getString("lessonId") ?: ""
            val lessonTitle = backStackEntry.arguments?.getString("lessonTitle") ?: ""
            val youtubeVideoId = backStackEntry.arguments?.getString("youtubeVideoId") ?: ""
            VideoPlayerScreen(
                lessonId = lessonId,
                lessonTitle = lessonTitle,
                youtubeVideoId = youtubeVideoId,
                onBackClick = {
                    navController.popBackStack()
                }
            )
        }
        
        composable("unity") {
            // Launch Unity only once, then wait for user to return. On return (Activity resumes)
            // we navigate back to student screen automatically.
            val launched = remember { mutableStateOf(false) }
            val lifecycleOwner = androidx.lifecycle.compose.LocalLifecycleOwner.current
            LaunchedEffect(launched.value) {
                if (!launched.value) {
                    launched.value = true
                    onStartUnity()
                }
            }
            DisposableEffect(lifecycleOwner, launched.value) {
                val observer = androidx.lifecycle.LifecycleEventObserver { _, event ->
                    if (event == androidx.lifecycle.Lifecycle.Event.ON_RESUME && launched.value) {
                        // Unity activity likely finished and focus returned
                        if (navController.currentDestination?.route == "unity") {
                            navController.popBackStack()
                        }
                    }
                }
                lifecycleOwner.lifecycle.addObserver(observer)
                onDispose { lifecycleOwner.lifecycle.removeObserver(observer) }
            }
            LoadingScreen(message = if (launched.value) "Return detected..." else "Launching AR Experience...") {}
        }
        
    // Admin screen removed (not implemented)
    }
}

