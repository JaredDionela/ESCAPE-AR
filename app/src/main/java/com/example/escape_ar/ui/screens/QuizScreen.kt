package com.example.escape_ar.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.shape.CircleShape
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
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.example.escape_ar.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun QuizScreen(
    moduleId: String = "",
    onNavigateBack: () -> Unit
) {
    val context = androidx.compose.ui.platform.LocalContext.current
    val quizViewModel: com.example.escape_ar.viewmodel.QuizViewModel = androidx.lifecycle.viewmodel.compose.viewModel(
        factory = androidx.lifecycle.ViewModelProvider.AndroidViewModelFactory.getInstance(
            context.applicationContext as android.app.Application
        )
    )
    
    var selectedModule by remember { mutableStateOf<QuizModuleData?>(null) }
    var currentQuestionIndex by remember { mutableIntStateOf(0) }
    var selectedAnswers by remember { mutableStateOf(listOf<Int>()) }
    var showResults by remember { mutableStateOf(false) }
    var score by remember { mutableIntStateOf(0) }
    
    val modules = remember {
        listOf(
            QuizModuleData(
                id = "decantation",
                name = "Decantation",
                description = "Learn how to separate mixtures by carefully pouring off liquids",
                icon = Icons.Default.Science,
                color = NeonCyan,
                totalQuestions = 5,
                questions = getDecantationQuestions()
            ),
            QuizModuleData(
                id = "organ_system",
                name = "Organ Systems", 
                description = "Discover how different organ systems work together in the human body",
                icon = Icons.Default.Favorite,
                color = CrimsonRed,
                totalQuestions = 5,
                questions = getOrganSystemQuestions()
            ),
            QuizModuleData(
                id = "simple_machines",
                name = "Simple Machines",
                description = "Explore the six types of simple machines and how they make work easier",
                icon = Icons.Default.Settings,
                color = AmberAlert,
                totalQuestions = 5,
                questions = getSimpleMachineQuestions()
            ),
            QuizModuleData(
                id = "solar_system",
                name = "Solar System",
                description = "Journey through our solar system and learn about planets and space",
                icon = Icons.Default.Brightness7,
                color = PurpleHaze,
                totalQuestions = 5,
                questions = getSolarSystemQuestions()
            )
        )
    }
    
    if (selectedModule == null) {
        // Module Selection Screen
        ModuleSelectionScreen(
            modules = modules,
            onModuleSelected = { 
                selectedModule = it
                currentQuestionIndex = 0
                selectedAnswers = emptyList()
                showResults = false
                score = 0
            },
            onNavigateBack = onNavigateBack
        )
    } else if (!showResults) {
        // Quiz Screen
        QuizQuestionScreen(
            module = selectedModule!!,
            currentQuestionIndex = currentQuestionIndex,
            selectedAnswers = selectedAnswers,
            onAnswerSelected = { questionIndex, answerIndex ->
                selectedAnswers = selectedAnswers.toMutableList().apply {
                    while (size <= questionIndex) add(-1)
                    set(questionIndex, answerIndex)
                }
            },
            onNext = {
                if (currentQuestionIndex < selectedModule!!.questions.size - 1) {
                    currentQuestionIndex++
                } else {
                    // Calculate score
                    score = selectedAnswers.mapIndexed { index, answer ->
                        if (index < selectedModule!!.questions.size && 
                            answer == selectedModule!!.questions[index].correctAnswer) 1 else 0
                    }.sum()
                    
                    // Save quiz progress when quiz is completed
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
            onPrevious = {
                if (currentQuestionIndex > 0) {
                    currentQuestionIndex--
                }
            },
            onBack = { selectedModule = null }
        )
    } else {
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
            onBackToModules = { selectedModule = null }
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ModuleSelectionScreen(
    modules: List<QuizModuleData>,
    onModuleSelected: (QuizModuleData) -> Unit,
    onNavigateBack: () -> Unit
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
                                tint = NeonCyan,
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
                        onClick = { onModuleSelected(module) }
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
                        text = "${module.totalQuestions} Questions",
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

// Data classes and question functions would continue...
// Due to length constraints, I'll create the remaining functions in separate files

data class QuizModuleData(
    val id: String,
    val name: String,
    val description: String,
    val icon: ImageVector,
    val color: androidx.compose.ui.graphics.Color,
    val totalQuestions: Int,
    val questions: List<QuizQuestion>
)

data class QuizQuestion(
    val question: String,
    val options: List<String>,
    val correctAnswer: Int,
    val explanation: String
)
