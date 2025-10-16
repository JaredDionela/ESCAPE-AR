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
                onProfileClick = {
                    navController.navigate(Screen.Profile.route)
                },
                onSettingsClick = {
                    navController.navigate(Screen.Settings.route)
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
        
        composable(Screen.Quiz.route) {
            QuizScreen(
                onNavigateBack = {
                    navController.popBackStack()
                }
            )
        }
        
        composable(Screen.Settings.route) {
            SettingsScreen(
                onNavigateBack = {
                    navController.popBackStack()
                }
            )
        }
        
    // Admin screen removed
    }
}

sealed class Screen(val route: String) {
    object Loading : Screen("loading")
    object Auth : Screen("auth")
    object Student : Screen("student")
    object Profile : Screen("profile")
    object Quiz : Screen("quiz")
    object Settings : Screen("settings")
    // object Admin : Screen("admin") // removed
}
