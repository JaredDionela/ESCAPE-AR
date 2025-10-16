package com.example.escape_ar.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.escape_ar.ui.theme.*
import kotlinx.coroutines.launch
import com.example.escape_ar.data.repository.UserRepository
// import com.example.escape_ar.viewmodel.AuthViewModel // Temporarily disabled

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AuthScreen(
    onLoginSuccess: () -> Unit
    // authViewModel: AuthViewModel = viewModel() // Temporarily disabled
) {
    var isLoginMode by remember { mutableStateOf(true) }
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var fullName by remember { mutableStateOf("") }
    var teacherName by remember { mutableStateOf("") }
    var section by remember { mutableStateOf("") }
    var isPasswordVisible by remember { mutableStateOf(false) }
    var isLoading by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    val scope = rememberCoroutineScope()
    
    // Get context and create repository with proper context
    val context = androidx.compose.ui.platform.LocalContext.current
    val repo = remember { UserRepository(context) }
    
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(
                        DeepSpace,
                        DarkGrey.copy(alpha = 0.5f),
                        DeepSpace
                    )
                )
            )
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            // App Header
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = CharcoalGrey.copy(alpha = 0.9f)
                ),
                elevation = CardDefaults.cardElevation(12.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(32.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Icon(
                        Icons.Default.Science,
                        contentDescription = null,
                        tint = NeonCyan,
                        modifier = Modifier.size(64.dp)
                    )
                    
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    Text(
                        text = "E.S.C.A.P.E. AR",
                        style = MaterialTheme.typography.headlineSmall,
                        color = NeonCyan,
                        fontWeight = FontWeight.Bold,
                        textAlign = TextAlign.Center
                    )
                    
                    Spacer(modifier = Modifier.height(8.dp))
                    
                    Text(
                        text = "Enhanced Science Comprehension\nthrough Augmented and Playful Education",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MetallicSilver,
                        textAlign = TextAlign.Center
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(24.dp))
            
            // Auth Form
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = CharcoalGrey.copy(alpha = 0.95f)
                ),
                elevation = CardDefaults.cardElevation(8.dp)
            ) {
                Column(
                    modifier = Modifier.padding(24.dp)
                ) {
                    // Mode Toggle
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.Center
                    ) {
                        FilterChip(
                            onClick = { isLoginMode = true },
                            label = { Text("Sign In") },
                            selected = isLoginMode,
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = NeonCyan,
                                selectedLabelColor = DeepSpace
                            )
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        FilterChip(
                            onClick = { isLoginMode = false },
                            label = { Text("Sign Up") },
                            selected = !isLoginMode,
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = PurpleHaze,
                                selectedLabelColor = WhiteSmoke
                            )
                        )
                    }
                    
                    Spacer(modifier = Modifier.height(24.dp))
                    
                    // Full Name field (only for registration)
                    if (!isLoginMode) {
                        OutlinedTextField(
                            value = fullName,
                            onValueChange = { fullName = it },
                            label = { Text("Student Name") },
                            leadingIcon = {
                                Icon(Icons.Default.Person, contentDescription = null)
                            },
                            modifier = Modifier.fillMaxWidth(),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = NeonCyan,
                                focusedLabelColor = NeonCyan,
                                focusedLeadingIconColor = NeonCyan
                            )
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        
                        // Teacher Name field
                        OutlinedTextField(
                            value = teacherName,
                            onValueChange = { teacherName = it },
                            label = { Text("Teacher Name (Required)") },
                            leadingIcon = {
                                Icon(Icons.Default.School, contentDescription = null)
                            },
                            modifier = Modifier.fillMaxWidth(),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = NeonCyan,
                                focusedLabelColor = NeonCyan,
                                focusedLeadingIconColor = NeonCyan
                            )
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        
                        // Section field
                        OutlinedTextField(
                            value = section,
                            onValueChange = { section = it },
                            label = { Text("Section (Required)") },
                            leadingIcon = {
                                Icon(Icons.Default.Class, contentDescription = null)
                            },
                            modifier = Modifier.fillMaxWidth(),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = NeonCyan,
                                focusedLabelColor = NeonCyan,
                                focusedLeadingIconColor = NeonCyan
                            )
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                    }
                    
                    // Email field
                    OutlinedTextField(
                        value = email,
                        onValueChange = { email = it },
                        label = { Text("Email") },
                        leadingIcon = {
                            Icon(Icons.Default.Email, contentDescription = null)
                        },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                        modifier = Modifier.fillMaxWidth(),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = NeonCyan,
                            focusedLabelColor = NeonCyan,
                            focusedLeadingIconColor = NeonCyan
                        )
                    )
                    
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    // Password field
                    OutlinedTextField(
                        value = password,
                        onValueChange = { password = it },
                        label = { Text("Password") },
                        leadingIcon = {
                            Icon(Icons.Default.Lock, contentDescription = null)
                        },
                        trailingIcon = {
                            IconButton(onClick = { isPasswordVisible = !isPasswordVisible }) {
                                Icon(
                                    if (isPasswordVisible) Icons.Default.Visibility 
                                    else Icons.Default.VisibilityOff,
                                    contentDescription = null
                                )
                            }
                        },
                        visualTransformation = if (isPasswordVisible) VisualTransformation.None 
                                             else PasswordVisualTransformation(),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                        modifier = Modifier.fillMaxWidth(),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = NeonCyan,
                            focusedLabelColor = NeonCyan,
                            focusedLeadingIconColor = NeonCyan
                        )
                    )
                    
                    Spacer(modifier = Modifier.height(24.dp))
                    
                    // Error message
                    if (errorMessage?.isNotEmpty() == true) {
                        Card(
                            colors = CardDefaults.cardColors(
                                containerColor = CrimsonRed.copy(alpha = 0.2f)
                            ),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(
                                text = errorMessage ?: "",
                                color = CrimsonRed,
                                style = MaterialTheme.typography.bodyMedium,
                                modifier = Modifier.padding(12.dp)
                            )
                        }
                        Spacer(modifier = Modifier.height(16.dp))
                    }
                    
                    // Submit button
                    Button(
                        onClick = {
                            android.util.Log.d("AuthScreen", "Button clicked, mode: ${if (isLoginMode) "login" else "signup"}")
                            
                            // Validate signup fields
                            if (!isLoginMode) {
                                if (teacherName.trim().isEmpty()) {
                                    errorMessage = "Teacher name is required"
                                    return@Button
                                }
                                if (section.trim().isEmpty()) {
                                    errorMessage = "Section is required"
                                    return@Button
                                }
                            }
                            
                            errorMessage = null
                            isLoading = true
                            scope.launch {
                                try {
                                    val result = if (isLoginMode) {
                                        android.util.Log.d("AuthScreen", "Attempting sign in for: $email")
                                        repo.signIn(email.trim(), password)
                                    } else {
                                        android.util.Log.d("AuthScreen", "Attempting sign up for: $email with teacher: '$teacherName', section: '$section'")
                                        repo.signUp(
                                            email = email.trim(), 
                                            password = password, 
                                            fullName = fullName.trim(),
                                            teacherName = teacherName.trim(),
                                            section = section.trim()
                                        )
                                    }
                                    isLoading = false
                                    result.onSuccess { 
                                        android.util.Log.d("AuthScreen", "Authentication successful!")
                                        onLoginSuccess() 
                                    }.onFailure { 
                                        android.util.Log.e("AuthScreen", "Authentication failed: ${it.message}")
                                        errorMessage = it.message ?: "Authentication failed" 
                                    }
                                } catch (e: Exception) {
                                    android.util.Log.e("AuthScreen", "Exception during authentication", e)
                                    isLoading = false
                                    errorMessage = "Authentication error: ${e.message}"
                                }
                            }
                        },
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (isLoginMode) NeonCyan else PurpleHaze,
                            contentColor = if (isLoginMode) DeepSpace else WhiteSmoke
                        ),
                        enabled = email.isNotEmpty() && password.isNotEmpty() &&
                                  (isLoginMode || (fullName.isNotEmpty() && teacherName.isNotEmpty() && section.isNotEmpty()))
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            if (isLoading) {
                                CircularProgressIndicator(
                                    color = if (isLoginMode) DeepSpace else WhiteSmoke,
                                    modifier = Modifier.size(20.dp)
                                )
                            } else {
                                Icon(
                                    if (isLoginMode) Icons.Default.VpnKey else Icons.Default.PersonAdd,
                                    contentDescription = null,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = if (isLoginMode) "Sign In" else "Sign Up",
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }
        }
    }
}
