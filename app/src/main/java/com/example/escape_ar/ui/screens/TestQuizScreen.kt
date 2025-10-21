package com.example.escape_ar.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun TestQuizScreen(
    moduleId: String = "",
    onNavigateBack: () -> Unit
) {
    android.util.Log.wtf("TestQuizScreen", "═══════════════════════════════════")
    android.util.Log.wtf("TestQuizScreen", "TEST QUIZ SCREEN CALLED!")
    android.util.Log.wtf("TestQuizScreen", "moduleId: '$moduleId'")
    android.util.Log.wtf("TestQuizScreen", "═══════════════════════════════════")
    
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text("TEST QUIZ SCREEN", style = MaterialTheme.typography.headlineLarge)
            Text("ModuleId: $moduleId")
            Button(onClick = onNavigateBack) {
                Text("Go Back")
            }
        }
    }
}
