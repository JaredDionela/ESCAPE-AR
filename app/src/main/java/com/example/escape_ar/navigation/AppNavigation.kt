package com.example.escape_ar.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.escape_ar.ui.screens.*

@Composable
fun AppNavigation(
    navController: NavHostController = rememberNavController(),
    startDestination: String = Screen.Loading.route
) {
    NavHost(
        navController = navController,
        startDestination = startDestination
    ) {
        composable(Screen.Loading.route) {
            LoadingScreen {
                navController.navigate(Screen.Auth.route) {
                    popUpTo(Screen.Loading.route) { inclusive = true }
                }
            }
        }
        
        composable(Screen.Auth.route) {
            AuthScreen(
                onLoginSuccess = {
                    navController.navigate(Screen.Student.route) {
                        popUpTo(Screen.Auth.route) { inclusive = true }
                    }
                }
            )
        }
        
        composable(Screen.Student.route) {
            StudentMainScreen(
                onQuizClick = { moduleId ->
                    navController.navigate("${Screen.Quiz.route}/$moduleId")
                },
                onLessonsClick = { moduleId, moduleName ->
                    navController.navigate("lessons/$moduleId/$moduleName")
                },
                onProfileClick = {
                    navController.navigate(Screen.Profile.route)
                },
                onUnityLaunch = {
                    navController.navigate("unity")
                },
                onLogout = {
                    navController.navigate(Screen.Auth.route) {
                        popUpTo(Screen.Student.route) { inclusive = true }
                    }
                }
            )
        }
        
        composable(Screen.Profile.route) {
            ProfileScreen(
                onNavigateBack = {
                    navController.popBackStack()
                }
            )
        }
        
        composable(
            route = "${Screen.Quiz.route}/{moduleId}",
            arguments = listOf(
                androidx.navigation.navArgument("moduleId") {
                    type = androidx.navigation.NavType.StringType
                    defaultValue = ""
                }
            )
        ) { backStackEntry ->
            val moduleId = backStackEntry.arguments?.getString("moduleId") ?: ""
            QuizScreen(
                moduleId = moduleId,
                onNavigateBack = {
                    navController.popBackStack()
                }
            )
        }
        
    // Settings screen removed
    // Admin screen removed
    }
}

sealed class Screen(val route: String) {
    object Loading : Screen("loading")
    object Auth : Screen("auth")
    object Student : Screen("student")
    object Profile : Screen("profile")
    object Quiz : Screen("quiz")
    // object Settings : Screen("settings") // removed
    // object Admin : Screen("admin") // removed
}
