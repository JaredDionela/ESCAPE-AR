package com.example.escape_ar.ui.screens

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.escape_ar.ui.theme.*
import com.example.escape_ar.viewmodel.QuizViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun QuizScreen(
    moduleId: String = "",
    onNavigateBack: () -> Unit
) {

    android.util.Log.d("QuizScreen", "═══════════════════════════════════")
    android.util.Log.d("QuizScreen", "QuizScreen started with moduleId: '$moduleId'")
    
    val context = LocalContext.current
    val audioManager = remember { com.example.escape_ar.utils.AudioManager.getInstance(context) }
    val quizViewModel: QuizViewModel = viewModel(
        factory = ViewModelProvider.AndroidViewModelFactory.getInstance(
            context.applicationContext as android.app.Application
        )
    )
    
    var selectedModule by remember { mutableStateOf<QuizModuleData?>(null) }
    var currentQuestionIndex by remember { mutableIntStateOf(0) }
    var selectedAnswers by remember { mutableStateOf(listOf<Int>()) }
    var showResults by remember { mutableStateOf(false) }
    var score by remember { mutableIntStateOf(0) }
    var isLoadingQuestions by remember { mutableStateOf(false) }
    var loadError by remember { mutableStateOf<String?>(null) }
    
    val modules = remember {
        listOf(
            QuizModuleData(
                id = "decantation",
                name = "Decantation",
                description = "Learn how to separate mixtures",
                icon = Icons.Default.Science,
                color = MistyBlue,
                totalQuestions = 0,
                questions = emptyList()
            ),
            QuizModuleData(
                id = "organ_system",
                name = "Organ Systems",
                description = "Learn about human organ systems",
                icon = Icons.Default.Favorite,
                color = CrimsonRed,
                totalQuestions = 0,
                questions = emptyList()
            ),
            QuizModuleData(
                id = "simple_machines",
                name = "Simple Machines",
                description = "Explore simple machines",
                icon = Icons.Default.Settings,
                color = AmberAlert,
                totalQuestions = 0,
                questions = emptyList()
            ),
            QuizModuleData(
                id = "solar_system",
                name = "Solar System",
                description = "Journey through space",
                icon = Icons.Default.Brightness7,
                color = PurpleHaze,
                totalQuestions = 0,
                questions = emptyList()
            )
        )
    }
    
    // Auto-select module when moduleId is provided
    LaunchedEffect(moduleId) {
        android.util.Log.d("QuizScreen", "LaunchedEffect triggered with moduleId: '$moduleId'")
        
        if (moduleId.isNotEmpty() && selectedModule == null) {
            android.util.Log.d("QuizScreen", "Auto-selecting module with ID: $moduleId")
            val module = modules.find { it.id == moduleId }
            if (module != null) {
                android.util.Log.d("QuizScreen", "Found module: ${module.name}")
                selectedModule = module
                currentQuestionIndex = 0
                selectedAnswers = emptyList()
                showResults = false
                score = 0
                loadError = null
            } else {
                android.util.Log.e("QuizScreen", "Module not found with id: '$moduleId'")
            }
        }
    }
    
    // Load questions when a module is selected
    LaunchedEffect(selectedModule?.id) {
        selectedModule?.let { module ->
            android.util.Log.d("QuizScreen", "Loading questions for module: ${module.id}")
            
            if (module.questions.isEmpty()) {
                android.util.Log.d("QuizScreen", "Questions empty, loading from database...")
                isLoadingQuestions = true
                loadError = null
                
                val result = quizViewModel.getQuizQuestionsByModule(module.id)
                android.util.Log.d("QuizScreen", "Result received: ${if (result.isSuccess) "SUCCESS" else "FAILURE"}")
                
                result.onSuccess { questions ->
                    android.util.Log.d("QuizScreen", "✅ Loaded ${questions.size} questions")
                    if (questions.isNotEmpty()) {
                        selectedModule = module.copy(
                            questions = questions,
                            totalQuestions = questions.size
                        )
                        android.util.Log.d("QuizScreen", "Module updated with ${questions.size} questions")
                    } else {
                        android.util.Log.e("QuizScreen", "No questions returned from database")
                        loadError = "No questions available for this module yet"
                    }
                    isLoadingQuestions = false
                }.onFailure { error ->
                    android.util.Log.e("QuizScreen", "❌ Failed to load questions: ${error.message}", error)
                    loadError = error.message ?: "Failed to load questions"
                    isLoadingQuestions = false
                }
            } else {
                android.util.Log.d("QuizScreen", "Module already has ${module.questions.size} questions")
            }
        }
    }
    
    // UI Logic
    when {
        selectedModule == null -> {
            // Module Selection Screen
            ModuleSelectionScreen(
                modules = modules,
                onModuleSelected = { 
                    android.util.Log.d("QuizScreen", "Module selected: ${it.id}")
                    selectedModule = it
                    currentQuestionIndex = 0
                    selectedAnswers = emptyList()
                    showResults = false
                    score = 0
                    loadError = null
                },
                onNavigateBack = onNavigateBack,
                audioManager = audioManager
            )
        }
        isLoadingQuestions -> {
            // Loading State
            LoadingScreen(moduleColor = selectedModule!!.color)
        }
        loadError != null || selectedModule!!.questions.isEmpty() -> {
            // Error State
            ErrorScreen(
                module = selectedModule!!,
                error = loadError,
                onBack = { 
                    selectedModule = null
                    loadError = null
                },
                audioManager = audioManager
            )
        }
        !showResults -> {
            // Quiz Screen
            QuizQuestionScreen(
                module = selectedModule!!,
                questions = selectedModule!!.questions,
                currentQuestionIndex = currentQuestionIndex,
                selectedAnswers = selectedAnswers,
                onAnswerSelected = { questionIndex, answerIndex ->
                    selectedAnswers = selectedAnswers.toMutableList().apply {
                        while (size <= questionIndex) add(-1)
                        set(questionIndex, answerIndex)
                    }
                },
                onNextQuestion = {
                    if (currentQuestionIndex < selectedModule!!.questions.size - 1) {
                        currentQuestionIndex++
                    } else {
                        // Calculate score
                        score = selectedAnswers.mapIndexed { index, answer ->
                            if (index < selectedModule!!.questions.size) {
                                val correctAnswerIndex = when(selectedModule!!.questions[index].correctAnswer) {
                                    "A" -> 0
                                    "B" -> 1
                                    "C" -> 2
                                    "D" -> 3
                                    else -> -1
                                }
                                if (answer == correctAnswerIndex) 1 else 0
                            } else 0
                        }.sum()
                        
                        // Save quiz progress
                        val percentage = (score.toFloat() / selectedModule!!.questions.size.toFloat()) * 100f
                        quizViewModel.completeModule(
                            moduleId = selectedModule!!.id,
                            score = percentage,
                            questionsAnswered = score,
                            totalQuestions = selectedModule!!.questions.size
                        )
                        
                        showResults = true
                    }
                },
                onPreviousQuestion = {
                    if (currentQuestionIndex > 0) {
                        currentQuestionIndex--
                    }
                },
                onSubmitQuiz = {},
                onNavigateBack = { selectedModule = null },
                audioManager = audioManager
            )
        }
        else -> {
            // Results Screen
            QuizResultsScreen(
                module = selectedModule!!,
                score = score,
                totalQuestions = selectedModule!!.questions.size,
                quizViewModel = quizViewModel,
                onRetry = {
                    currentQuestionIndex = 0
                    selectedAnswers = emptyList()
                    showResults = false
                    score = 0
                },
                onNavigateBackToModules = { selectedModule = null },
                audioManager = audioManager
            )
        }
    }
}

@Composable
private fun LoadingScreen(moduleColor: androidx.compose.ui.graphics.Color) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(DeepSpace),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            CircularProgressIndicator(color = moduleColor)
            Text(
                text = "Loading questions...",
                style = MaterialTheme.typography.bodyLarge,
                color = WhiteSmoke
            )
        }
    }
}

@Composable
private fun ErrorScreen(
    module: QuizModuleData,
    error: String?,
    onBack: () -> Unit,
    audioManager: com.example.escape_ar.utils.AudioManager
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(DeepSpace),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp),
            modifier = Modifier.padding(32.dp)
        ) {
            Icon(
                imageVector = Icons.Default.Error,
                contentDescription = "Error",
                tint = CrimsonRed,
                modifier = Modifier.size(64.dp)
            )
            Text(
                text = error ?: "No questions available",
                style = MaterialTheme.typography.bodyLarge,
                color = WhiteSmoke,
                textAlign = androidx.compose.ui.text.style.TextAlign.Center
            )
            Text(
                text = "Module: ${module.name}\nID: ${module.id}\nQuestions: ${module.questions.size}",
                style = MaterialTheme.typography.bodySmall,
                color = MetallicSilver,
                textAlign = androidx.compose.ui.text.style.TextAlign.Center
            )
            Button(
                onClick = { 
                    audioManager.playButtonClick()
                    onBack() 
                },
                colors = ButtonDefaults.buttonColors(
                    containerColor = module.color
                )
            ) {
                Text("Back to Modules")
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ModuleSelectionScreen(
    modules: List<QuizModuleData>,
    onModuleSelected: (QuizModuleData) -> Unit,
    onNavigateBack: () -> Unit,
    audioManager: com.example.escape_ar.utils.AudioManager
) {
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
            modifier = Modifier.fillMaxSize()
        ) {
            TopAppBar(
                title = {
                    Text(
                        text = "Science Modules",
                        color = WhiteSmoke,
                        fontWeight = FontWeight.Bold
                    )
                },
                navigationIcon = {
                    IconButton(onClick = { 
                        audioManager.playButtonClick()
                        onNavigateBack() 
                    }) {
                        Icon(
                            Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                            tint = MistyBlue
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = CharcoalGrey.copy(alpha = 0.9f)
                )
            )
            
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(
                            containerColor = DarkGrey.copy(alpha = 0.6f)
                        ),
                        elevation = CardDefaults.cardElevation(4.dp)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                Icons.Default.Info,
                                contentDescription = null,
                                tint = MistyBlue,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(12.dp))
                            Text(
                                text = "Complete each module to test your science knowledge",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MetallicSilver
                            )
                        }
                    }
                }
                
                items(modules) { module ->
                    ModuleCard(
                        module = module,
                        onClick = { 
                            audioManager.playButtonClick()
                            onModuleSelected(module) 
                        }
                    )
                }
            }
        }
    }
}

@Composable
private fun ModuleCard(
    module: QuizModuleData,
    onClick: () -> Unit
) {
    val scale by animateFloatAsState(
        targetValue = 1f,
        animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy),
        label = "scale"
    )
    
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .scale(scale),
        colors = CardDefaults.cardColors(
            containerColor = CharcoalGrey.copy(alpha = 0.8f)
        ),
        elevation = CardDefaults.cardElevation(12.dp)
    ) {
        Button(
            onClick = onClick,
            modifier = Modifier.fillMaxWidth(),
            colors = ButtonDefaults.buttonColors(
                containerColor = androidx.compose.ui.graphics.Color.Transparent
            ),
            shape = RoundedCornerShape(12.dp)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(60.dp)
                        .background(
                            module.color.copy(alpha = 0.2f),
                            CircleShape
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        module.icon,
                        contentDescription = null,
                        tint = module.color,
                        modifier = Modifier.size(32.dp)
                    )
                }
                
                Spacer(modifier = Modifier.width(20.dp))
                
                Column(
                    modifier = Modifier.weight(1f)
                ) {
                    Text(
                        text = module.name,
                        style = MaterialTheme.typography.titleLarge,
                        color = WhiteSmoke,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = module.description,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MetallicSilver
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = if (module.totalQuestions > 0) {
                            "${module.totalQuestions} Questions"
                        } else {
                            "Loading..."
                        },
                        style = MaterialTheme.typography.bodySmall,
                        color = module.color
                    )
                }
                
                Icon(
                    Icons.Default.ChevronRight,
                    contentDescription = null,
                    tint = MetallicSilver
                )
            }
        }
    }
}

data class QuizModuleData(
    val id: String,
    val name: String,
    val description: String,
    val icon: ImageVector,
    val color: androidx.compose.ui.graphics.Color,
    val totalQuestions: Int,
    val questions: List<com.example.escape_ar.data.model.QuizQuestion>
)
