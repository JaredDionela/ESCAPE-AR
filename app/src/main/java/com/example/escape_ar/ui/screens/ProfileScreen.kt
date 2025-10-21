package com.example.escape_ar.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.automirrored.filled.TrendingUp
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.ViewModelProvider
import kotlinx.coroutines.launch
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.example.escape_ar.ui.theme.*
import com.example.escape_ar.viewmodel.ProfileViewModel
import com.example.escape_ar.viewmodel.ModuleProgress

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(
    onNavigateBack: () -> Unit
) {
    val context = LocalContext.current
    
    // Check authentication first
    val sessionManager = remember { com.example.escape_ar.data.SessionManager.getInstance(context) }
    
    // If not authenticated, navigate back (to main screen which will redirect to auth)
    LaunchedEffect(Unit) {
        if (!sessionManager.isLoggedIn()) {
            android.util.Log.d("ProfileScreen", "User not authenticated, navigating back")
            onNavigateBack()
            return@LaunchedEffect
        }
    }
    
    val viewModel: ProfileViewModel = viewModel(
        factory = ViewModelProvider.AndroidViewModelFactory.getInstance(
            context.applicationContext as android.app.Application
        )
    )
    val uiState by viewModel.uiState.collectAsState()
    val scope = rememberCoroutineScope()
    
    // Debug logging
    LaunchedEffect(uiState.teacherName, uiState.section) {
        android.util.Log.d("ProfileScreen", "UI State - teacherName: '${uiState.teacherName}', section: '${uiState.section}'")
    }
    
    // Edit profile dialog state
    var showEditDialog by remember { mutableStateOf(false) }
    var editStudentName by remember { mutableStateOf("") }
    var editTeacherName by remember { mutableStateOf("") }
    var editSection by remember { mutableStateOf("") }
    var isSaving by remember { mutableStateOf(false) }
    var saveError by remember { mutableStateOf<String?>(null) }
    
    // Auto-refresh when screen resumes OR when first loaded
    val lifecycleOwner = androidx.lifecycle.compose.LocalLifecycleOwner.current
    DisposableEffect(lifecycleOwner) {
        val observer = androidx.lifecycle.LifecycleEventObserver { _, event ->
            if (event == androidx.lifecycle.Lifecycle.Event.ON_RESUME) {
                android.util.Log.d("ProfileScreen", "ON_RESUME event - refreshing data")
                viewModel.refreshData()
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        
        // Also refresh immediately when screen is first composed
        android.util.Log.d("ProfileScreen", "ProfileScreen composed - initial refresh")
        viewModel.refreshData()
        
        onDispose { lifecycleOwner.lifecycle.removeObserver(observer) }
    }
    
    val totalModules = uiState.modules.size.takeIf { it > 0 } ?: 4
    val completedModules = uiState.modules.count { it.isCompleted }
    val averageScore = if (uiState.modules.isNotEmpty()) {
        uiState.modules.sumOf { it.score } / uiState.modules.size
    } else 0
    
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(
                        DeepSpace,
                        DarkGrey.copy(alpha = 0.3f),
                        DeepSpace
                    )
                )
            )
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
        ) {
            // Top Bar
            TopAppBar(
                title = {
                    Text(
                        text = "Profile & Progress",
                        color = WhiteSmoke,
                        fontWeight = FontWeight.Bold
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(
                            Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                            tint = NeonCyan
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = CharcoalGrey.copy(alpha = 0.9f)
                )
            )
            
            Column(
                modifier = Modifier.padding(16.dp)
            ) {
                // Profile Card
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = CharcoalGrey.copy(alpha = 0.8f)
                    ),
                    elevation = CardDefaults.cardElevation(12.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        // Avatar
                        Box(
                            modifier = Modifier
                                .size(80.dp)
                                .clip(CircleShape)
                                .background(
                                    Brush.radialGradient(
                                        colors = listOf(
                                            NeonCyan.copy(alpha = 0.3f),
                                            PurpleHaze.copy(alpha = 0.1f)
                                        )
                                    )
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = uiState.userName.firstOrNull()?.toString() ?: "?",
                                style = MaterialTheme.typography.headlineLarge,
                                color = NeonCyan,
                                fontWeight = FontWeight.Bold
                            )
                        }
                        
                        Spacer(modifier = Modifier.height(16.dp))
                        
                        Text(
                            text = uiState.userName,
                            style = MaterialTheme.typography.headlineSmall,
                            color = WhiteSmoke,
                            fontWeight = FontWeight.Bold
                        )
                        
                        Text(
                            text = uiState.userEmail,
                            style = MaterialTheme.typography.bodyMedium,
                            color = MetallicSilver
                        )
                        
                        // Teacher Name and Section - Enhanced Display
                        if (!uiState.teacherName.isNullOrBlank() || !uiState.section.isNullOrBlank()) {
                            Spacer(modifier = Modifier.height(12.dp))
                            
                            Card(
                                colors = CardDefaults.cardColors(
                                    containerColor = DarkGrey.copy(alpha = 0.4f)
                                ),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Column(modifier = Modifier.padding(12.dp)) {
                                    if (!uiState.teacherName.isNullOrBlank()) {
                                        Row(
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Icon(
                                                Icons.Default.School,
                                                contentDescription = null,
                                                tint = AmberAlert,
                                                modifier = Modifier.size(16.dp)
                                            )
                                            Spacer(modifier = Modifier.width(8.dp))
                                            Column {
                                                Text(
                                                    text = "Teacher",
                                                    style = MaterialTheme.typography.bodySmall,
                                                    color = MetallicSilver
                                                )
                                                Text(
                                                    text = uiState.teacherName!!,
                                                    style = MaterialTheme.typography.bodyMedium,
                                                    color = WhiteSmoke,
                                                    fontWeight = FontWeight.Medium
                                                )
                                            }
                                        }
                                    }
                                    
                                    if (!uiState.section.isNullOrBlank()) {
                                        if (!uiState.teacherName.isNullOrBlank()) {
                                            Spacer(modifier = Modifier.height(8.dp))
                                        }
                                        Row(
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Icon(
                                                Icons.Default.Class,
                                                contentDescription = null,
                                                tint = NeonCyan,
                                                modifier = Modifier.size(16.dp)
                                            )
                                            Spacer(modifier = Modifier.width(8.dp))
                                            Column {
                                                Text(
                                                    text = "Section",
                                                    style = MaterialTheme.typography.bodySmall,
                                                    color = MetallicSilver
                                                )
                                                Text(
                                                    text = uiState.section!!,
                                                    style = MaterialTheme.typography.bodyMedium,
                                                    color = WhiteSmoke,
                                                    fontWeight = FontWeight.Medium
                                                )
                                            }
                                        }
                                    }
                                }
                            }
                        }
                        
                        Spacer(modifier = Modifier.height(24.dp))
                        
                        // Overall Progress
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceEvenly
                        ) {
                            StatItem("Completed", "$completedModules/$totalModules")
                            StatItem("Final Grade", "$averageScore%")
                        }
                        
                        Spacer(modifier = Modifier.height(16.dp))
                        
                        // Edit Profile Button
                        OutlinedButton(
                            onClick = {
                                // Load current values from UI state
                                editStudentName = uiState.userName
                                editTeacherName = uiState.teacherName ?: ""
                                editSection = uiState.section ?: ""
                                saveError = null
                                showEditDialog = true
                            },
                            modifier = Modifier.fillMaxWidth(),
                            colors = ButtonDefaults.outlinedButtonColors(
                                contentColor = NeonCyan
                            )
                        ) {
                            Icon(Icons.Default.Edit, contentDescription = null)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Edit Profile")
                        }
                    }
                }
                
                Spacer(modifier = Modifier.height(24.dp))
                
                // Overall Progress Bar
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = CharcoalGrey.copy(alpha = 0.8f)
                    ),
                    elevation = CardDefaults.cardElevation(8.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(20.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                Icons.AutoMirrored.Filled.TrendingUp,
                                contentDescription = null,
                                tint = GlowGreen,
                                modifier = Modifier.size(24.dp)
                            )
                            Spacer(modifier = Modifier.width(12.dp))
                            Text(
                                text = "Overall Progress",
                                style = MaterialTheme.typography.titleMedium,
                                color = WhiteSmoke,
                                fontWeight = FontWeight.Bold
                            )
                        }
                        
                        Spacer(modifier = Modifier.height(16.dp))
                        
                        LinearProgressIndicator(
                            progress = { completedModules.toFloat() / totalModules },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(8.dp)
                                .clip(RoundedCornerShape(4.dp)),
                            color = GlowGreen,
                            trackColor = DarkGrey,
                        )
                        
                        Spacer(modifier = Modifier.height(8.dp))
                        
                        Text(
                            text = if (uiState.isLoading) "Loading..." else "${(if (totalModules>0) completedModules * 100 / totalModules else 0)}% Complete",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MetallicSilver
                        )
                    }
                }
                
                Spacer(modifier = Modifier.height(24.dp))
                
                // Module Progress
                Text(
                    text = "Knowledge Modules",
                    style = MaterialTheme.typography.headlineSmall,
                    color = WhiteSmoke,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(horizontal = 4.dp)
                )
                
                Spacer(modifier = Modifier.height(16.dp))
                
                if (uiState.isLoading) {
                    CircularProgressIndicator(color = NeonCyan)
                } else if (uiState.error != null) {
                    Column {
                        Text(text = uiState.error ?: "Unknown error", color = CrimsonRed)
                        Spacer(modifier = Modifier.height(8.dp))
                        Button(
                            onClick = { viewModel.refreshData() },
                            colors = ButtonDefaults.buttonColors(containerColor = NeonCyan)
                        ) {
                            Text("Retry", color = CharcoalGrey)
                        }
                    }
                } else if (uiState.modules.isEmpty()) {
                    Text(text = "No progress yet", color = MetallicSilver)
                } else {
                    uiState.modules.forEach { module ->
                        ModuleProgressCard(module = module)
                        Spacer(modifier = Modifier.height(12.dp))
                    }
                }
                
                Spacer(modifier = Modifier.height(32.dp))
                
                // Achievement Section
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = DarkGrey.copy(alpha = 0.6f)
                    ),
                    elevation = CardDefaults.cardElevation(4.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(20.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                Icons.Default.EmojiEvents,
                                contentDescription = null,
                                tint = AmberAlert,
                                modifier = Modifier.size(24.dp)
                            )
                            Spacer(modifier = Modifier.width(12.dp))
                            Text(
                                text = "Achievements",
                                style = MaterialTheme.typography.titleMedium,
                                color = WhiteSmoke,
                                fontWeight = FontWeight.Bold
                            )
                        }
                        
                        Spacer(modifier = Modifier.height(16.dp))
                        
                        // Calculate average score across completed modules
                        val averageScore = if (completedModules > 0) {
                            uiState.modules.filter { it.isCompleted }.map { it.score }.average().toInt()
                        } else {
                            0
                        }
                        
                        // Achievement: First Steps (Complete first module)
                        Row(
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                if (completedModules > 0) Icons.Default.CheckCircle else Icons.Default.Lock,
                                contentDescription = null,
                                tint = if (completedModules > 0) GlowGreen else MetallicSilver,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "🎯 First Steps - Complete your first module",
                                style = MaterialTheme.typography.bodyMedium,
                                color = if (completedModules > 0) WhiteSmoke else MetallicSilver
                            )
                        }
                        
                        // Achievement: Knowledge Seeker (Complete 2 modules)
                        Spacer(modifier = Modifier.height(8.dp))
                        Row(
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                if (completedModules >= 2) Icons.Default.CheckCircle else Icons.Default.Lock,
                                contentDescription = null,
                                tint = if (completedModules >= 2) GlowGreen else MetallicSilver,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "📚 Knowledge Seeker - Complete 2 modules",
                                style = MaterialTheme.typography.bodyMedium,
                                color = if (completedModules >= 2) WhiteSmoke else MetallicSilver
                            )
                        }
                        
                        // Achievement: Halfway There (Complete half of modules)
                        Spacer(modifier = Modifier.height(8.dp))
                        Row(
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                if (completedModules >= totalModules / 2 && totalModules > 0) Icons.Default.CheckCircle else Icons.Default.Lock,
                                contentDescription = null,
                                tint = if (completedModules >= totalModules / 2 && totalModules > 0) GlowGreen else MetallicSilver,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "⭐ Halfway There - Complete ${totalModules / 2} modules",
                                style = MaterialTheme.typography.bodyMedium,
                                color = if (completedModules >= totalModules / 2 && totalModules > 0) WhiteSmoke else MetallicSilver
                            )
                        }
                        
                        // Achievement: Perfect Score (Get 100% on any module)
                        Spacer(modifier = Modifier.height(8.dp))
                        Row(
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            val hasPerfectScore = uiState.modules.any { it.isCompleted && it.score >= 100 }
                            Icon(
                                if (hasPerfectScore) Icons.Default.CheckCircle else Icons.Default.Lock,
                                contentDescription = null,
                                tint = if (hasPerfectScore) GlowGreen else MetallicSilver,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "💯 Perfect Score - Get 100% on any module",
                                style = MaterialTheme.typography.bodyMedium,
                                color = if (hasPerfectScore) WhiteSmoke else MetallicSilver
                            )
                        }
                        
                        // Achievement: Outstanding Student (Average 90%+ across all completed)
                        Spacer(modifier = Modifier.height(8.dp))
                        Row(
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            val isOutstanding = completedModules > 0 && averageScore >= 90
                            Icon(
                                if (isOutstanding) Icons.Default.CheckCircle else Icons.Default.Lock,
                                contentDescription = null,
                                tint = if (isOutstanding) GlowGreen else MetallicSilver,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "🌟 Outstanding Student - Average 90%+ score",
                                style = MaterialTheme.typography.bodyMedium,
                                color = if (isOutstanding) WhiteSmoke else MetallicSilver
                            )
                        }
                        
                        // Achievement: Escape Master (Complete all modules)
                        Spacer(modifier = Modifier.height(8.dp))
                        Row(
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            val isEscapeMaster = completedModules == totalModules && totalModules > 0
                            Icon(
                                if (isEscapeMaster) Icons.Default.CheckCircle else Icons.Default.Lock,
                                contentDescription = null,
                                tint = if (isEscapeMaster) AmberAlert else MetallicSilver,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "🏆 Escape Master - Complete all $totalModules modules",
                                style = MaterialTheme.typography.bodyMedium,
                                color = if (isEscapeMaster) AmberAlert else MetallicSilver,
                                fontWeight = if (isEscapeMaster) FontWeight.Bold else FontWeight.Normal
                            )
                        }
                    }
                }
            }
        }
        
        // Edit Profile Dialog
        EditProfileDialog(
            show = showEditDialog,
            studentName = editStudentName,
            teacherName = editTeacherName,
            section = editSection,
            onStudentNameChange = { editStudentName = it },
            onTeacherNameChange = { editTeacherName = it },
            onSectionChange = { editSection = it },
            onDismiss = {
                showEditDialog = false
                saveError = null
            },
            onSave = {
                isSaving = true
                saveError = null
                scope.launch {
                    try {
                        val repo = com.example.escape_ar.data.repository.UserRepository(context)
                        val userId = repo.getCurrentUser()?.id
                        
                        if (userId != null) {
                            // Update profile in Supabase
                            val result = repo.updateExtendedProfile(
                                userId = userId,
                                displayName = editStudentName.trim(),
                                teacherName = editTeacherName.trim().ifBlank { null },
                                section = editSection.trim().ifBlank { null }
                            )
                            
                            result.onSuccess {
                                android.util.Log.d("ProfileScreen", "Profile updated successfully")
                                showEditDialog = false
                                viewModel.refreshData()
                            }.onFailure { error ->
                                android.util.Log.e("ProfileScreen", "Failed to update profile: ${error.message}")
                                saveError = error.message ?: "Failed to save profile"
                            }
                        } else {
                            saveError = "User not found"
                        }
                    } catch (e: Exception) {
                        android.util.Log.e("ProfileScreen", "Exception updating profile", e)
                        saveError = e.message ?: "Unknown error"
                    } finally {
                        isSaving = false
                    }
                }
            },
            onResetPassword = {
                scope.launch {
                    try {
                        val repo = com.example.escape_ar.data.repository.UserRepository(context)
                        val email = repo.getCurrentUser()?.email
                        
                        if (email != null) {
                            val result = repo.resetPassword(email)
                            result.onSuccess {
                                android.util.Log.d("ProfileScreen", "Password reset email sent")
                                saveError = "Password reset email sent! Check your inbox."
                            }.onFailure { error ->
                                android.util.Log.e("ProfileScreen", "Failed to send reset email: ${error.message}")
                                saveError = error.message ?: "Failed to send reset email"
                            }
                        }
                    } catch (e: Exception) {
                        android.util.Log.e("ProfileScreen", "Exception resetting password", e)
                        saveError = e.message ?: "Unknown error"
                    }
                }
            },
            isSaving = isSaving,
            error = saveError
        )
    }
}

@Composable
private fun StatItem(label: String, value: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            text = value,
            style = MaterialTheme.typography.titleLarge,
            color = NeonCyan,
            fontWeight = FontWeight.Bold
        )
        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall,
            color = MetallicSilver
        )
    }
}

@Composable
private fun ModuleProgressCard(module: ModuleProgress) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = CharcoalGrey.copy(alpha = 0.8f)
        ),
        elevation = CardDefaults.cardElevation(6.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        if (module.isCompleted) Icons.Default.CheckCircle else Icons.Default.RadioButtonUnchecked,
                        contentDescription = null,
                        tint = if (module.isCompleted) GlowGreen else MetallicSilver,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text(
                            text = module.name,
                            style = MaterialTheme.typography.titleMedium,
                            color = WhiteSmoke,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
                
                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        text = "${module.score}%",
                        style = MaterialTheme.typography.titleMedium,
                        color = if (module.isCompleted) GlowGreen else MetallicSilver,
                        fontWeight = FontWeight.Bold
                    )
                    if (module.isCompleted) {
                        Text(
                            text = "Passed",
                            style = MaterialTheme.typography.bodySmall,
                            color = GlowGreen
                        )
                    }
                }
            }
            
            Spacer(modifier = Modifier.height(12.dp))
            
            LinearProgressIndicator(
                progress = { module.score.toFloat() / module.maxScore },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(6.dp)
                    .clip(RoundedCornerShape(3.dp)),
                color = if (module.isCompleted) GlowGreen else NeonCyan,
                trackColor = DarkGrey,
            )
        }
    }
}

/**
 * Edit Profile Dialog
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun EditProfileDialog(
    show: Boolean,
    studentName: String,
    teacherName: String,
    section: String,
    onStudentNameChange: (String) -> Unit,
    onTeacherNameChange: (String) -> Unit,
    onSectionChange: (String) -> Unit,
    onDismiss: () -> Unit,
    onSave: () -> Unit,
    onResetPassword: () -> Unit,
    isSaving: Boolean,
    error: String?
) {
    if (show) {
        AlertDialog(
            onDismissRequest = { if (!isSaving) onDismiss() },
            title = {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(Icons.Default.Edit, contentDescription = null, tint = NeonCyan)
                    Text("Edit Profile")
                }
            },
            text = {
                Column(
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    // Error message
                    if (error != null) {
                        Card(
                            colors = CardDefaults.cardColors(
                                containerColor = CrimsonRed.copy(alpha = 0.2f)
                            )
                        ) {
                            Text(
                                text = error,
                                modifier = Modifier.padding(12.dp),
                                color = CrimsonRed,
                                style = MaterialTheme.typography.bodySmall
                            )
                        }
                    }
                    
                    // Student Name
                    OutlinedTextField(
                        value = studentName,
                        onValueChange = onStudentNameChange,
                        label = { Text("Student Name") },
                        leadingIcon = {
                            Icon(Icons.Default.Person, contentDescription = null)
                        },
                        modifier = Modifier.fillMaxWidth(),
                        enabled = !isSaving,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = NeonCyan,
                            focusedLabelColor = NeonCyan,
                            focusedLeadingIconColor = NeonCyan
                        )
                    )
                    
                    // Teacher Name
                    OutlinedTextField(
                        value = teacherName,
                        onValueChange = onTeacherNameChange,
                        label = { Text("Teacher Name (Required)") },
                        leadingIcon = {
                            Icon(Icons.Default.School, contentDescription = null)
                        },
                        modifier = Modifier.fillMaxWidth(),
                        enabled = !isSaving,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = NeonCyan,
                            focusedLabelColor = NeonCyan,
                            focusedLeadingIconColor = NeonCyan
                        )
                    )
                    
                    // Section
                    OutlinedTextField(
                        value = section,
                        onValueChange = onSectionChange,
                        label = { Text("Section (Required)") },
                        leadingIcon = {
                            Icon(Icons.Default.Class, contentDescription = null)
                        },
                        modifier = Modifier.fillMaxWidth(),
                        enabled = !isSaving,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = NeonCyan,
                            focusedLabelColor = NeonCyan,
                            focusedLeadingIconColor = NeonCyan
                        )
                    )
                    
                    // Reset Password Button
                    OutlinedButton(
                        onClick = onResetPassword,
                        modifier = Modifier.fillMaxWidth(),
                        enabled = !isSaving,
                        colors = ButtonDefaults.outlinedButtonColors(
                            contentColor = AmberAlert
                        )
                    ) {
                        Icon(Icons.Default.Lock, contentDescription = null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Reset Password")
                    }
                    
                    Text(
                        text = "A password reset link will be sent to your email",
                        style = MaterialTheme.typography.bodySmall,
                        color = MetallicSilver
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = onSave,
                    enabled = !isSaving && studentName.isNotBlank(),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = NeonCyan,
                        contentColor = DeepSpace
                    )
                ) {
                    if (isSaving) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(16.dp),
                            strokeWidth = 2.dp,
                            color = DeepSpace
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                    }
                    Text("Save")
                }
            },
            dismissButton = {
                TextButton(
                    onClick = onDismiss,
                    enabled = !isSaving
                ) {
                    Text("Cancel")
                }
            }
        )
    }
}
