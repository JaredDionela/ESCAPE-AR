package com.example.escape_ar

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
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
import com.example.escape_ar.ui.screens.*
import com.example.escape_ar.ui.theme.ESCAPEARTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
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
    
    private fun startUnityActivity() {
        try {
            // Check if Unity library is available first
            try {
                Class.forName("com.unity3d.player.UnityPlayerGameActivity")
            } catch (e: ClassNotFoundException) {
                android.util.Log.w("MainActivity", "Unity PlayerGameActivity class not found - AR experience not available")
                showUnityNotAvailableDialog()
                return
            }
            
                val unityIntent = Intent().setClassName(this, "com.unity3d.player.UnityPlayerGameActivity")
                startActivity(unityIntent)
                android.util.Log.d("MainActivity", "Unity activity launched")
        } catch (e: Exception) {
            android.util.Log.e("MainActivity", "Failed to launch Unity AR experience", e)
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
            
            LoadingScreen {
                // Check if user is already authenticated
                if (sessionManager.isLoggedIn()) {
                    android.util.Log.d("MainActivity", "User already logged in, navigating to student")
                    navController.navigate("student") {
                        popUpTo("loading") { inclusive = true }
                    }
                } else {
                    android.util.Log.d("MainActivity", "No session found, navigating to auth")
                    navController.navigate("auth") {
                        popUpTo("loading") { inclusive = true }
                    }
                }
            }
        }
        
        composable("auth") {
            AuthScreen(
                onLoginSuccess = {
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
                    navController.navigate("unity")
                },
                onLogout = {
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
                }
            )
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

