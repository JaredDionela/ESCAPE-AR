package com.example.escape_ar.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.example.escape_ar.ui.theme.*
import com.example.escape_ar.data.model.QuizQuestion // Import the database model

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun QuizQuestionScreen(
    module: QuizModuleData,
    questions: List<QuizQuestion>,
    currentQuestionIndex: Int,
    selectedAnswers: List<Int>,
    onAnswerSelected: (Int, Int) -> Unit,
    onPreviousQuestion: () -> Unit,
    onNextQuestion: () -> Unit,
    onSubmitQuiz: () -> Unit,
    onNavigateBack: () -> Unit,
    audioManager: com.example.escape_ar.utils.AudioManager
) {
    // Log for debugging
    android.util.Log.d("QuizComponents", "========= QuizDisplay called =========")
    android.util.Log.d("QuizComponents", "Module: ${module.name}")
    android.util.Log.d("QuizComponents", "Questions count: ${questions.size}")
    android.util.Log.d("QuizComponents", "Current index: $currentQuestionIndex")
    
    // Safety check - prevent crash if questions list is empty
    if (questions.isEmpty() || currentQuestionIndex >= questions.size) {
        android.util.Log.e("QuizComponents", "NO QUESTIONS AVAILABLE!")
        android.util.Log.e("QuizComponents", "Questions empty: ${questions.isEmpty()}")
        android.util.Log.e("QuizComponents", "Index >= size: ${currentQuestionIndex >= questions.size}")
        
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
                Icon(
                    Icons.Default.Error,
                    contentDescription = null,
                    modifier = Modifier.size(64.dp),
                    tint = CrimsonRed
                )
                Text(
                    "No questions available",
                    style = MaterialTheme.typography.titleLarge,
                    color = WhiteSmoke
                )
                Button(onClick = { 
                    audioManager.playButtonClick()
                    onNavigateBack() 
                }) {
                    Text("Go Back")
                }
            }
        }
        return
    }
    
    val currentQuestion: com.example.escape_ar.data.model.QuizQuestion = questions[currentQuestionIndex]
    val selectedAnswer = selectedAnswers.getOrElse(currentQuestionIndex) { -1 }
    
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
                    Column {
                        Text(
                            text = module.name,
                            color = WhiteSmoke,
                            fontWeight = FontWeight.Bold,
                            style = MaterialTheme.typography.titleMedium
                        )
                        Text(
                            text = "Question ${currentQuestionIndex + 1} of ${questions.size}",
                            color = MetallicSilver,
                            style = MaterialTheme.typography.bodySmall
                        )
                    }
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
            
            // Progress Bar
            LinearProgressIndicator(
                progress = { (currentQuestionIndex + 1).toFloat() / questions.size },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(4.dp),
                color = module.color,
                trackColor = DarkGrey
            )
            
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp),
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                // Question Card
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = CharcoalGrey.copy(alpha = 0.8f)
                    ),
                    elevation = CardDefaults.cardElevation(12.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(24.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                module.icon,
                                contentDescription = null,
                                tint = module.color,
                                modifier = Modifier.size(24.dp)
                            )
                            Spacer(modifier = Modifier.width(12.dp))
                            Text(
                                text = "Question ${currentQuestionIndex + 1}",
                                style = MaterialTheme.typography.titleMedium,
                                color = module.color,
                                fontWeight = FontWeight.Bold
                            )
                        }
                        
                        Spacer(modifier = Modifier.height(20.dp))
                        
                        Text(
                            text = currentQuestion.questionText, // Fixed: use questionText from database model
                            style = MaterialTheme.typography.titleLarge,
                            color = WhiteSmoke,
                            lineHeight = MaterialTheme.typography.titleLarge.lineHeight * 1.3
                        )
                    }
                }
                
                Spacer(modifier = Modifier.height(24.dp))
                
                // Answer Options
                Column(
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    currentQuestion.getOptions().forEachIndexed { index, option -> // Fixed: use getOptions() helper
                        AnswerOption(
                            text = option,
                            isSelected = selectedAnswer == index,
                            optionIndex = index,
                            moduleColor = module.color,
                            onClick = { 
                                audioManager.playButtonClick()
                                onAnswerSelected(currentQuestionIndex, index) 
                            }
                        )
                    }
                }
                
                Spacer(modifier = Modifier.height(24.dp))
                
                // Navigation Buttons
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    // Previous Button
                    if (currentQuestionIndex > 0) {
                        OutlinedButton(
                            onClick = { 
                                audioManager.playButtonClick()
                                onPreviousQuestion() 
                            },
                            colors = ButtonDefaults.outlinedButtonColors(
                                contentColor = MetallicSilver
                            ),
                            border = ButtonDefaults.outlinedButtonBorder.copy(
                                brush = Brush.linearGradient(listOf(MetallicSilver, MetallicSilver))
                            )
                        ) {
                            Icon(
                                Icons.AutoMirrored.Filled.ArrowBack,
                                contentDescription = null,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Previous")
                        }
                    } else {
                        Spacer(modifier = Modifier.width(1.dp))
                    }
                    
                    // Next/Finish Button
                    Button(
                        onClick = { 
                            audioManager.playButtonClick()
                            onNextQuestion() 
                        },
                        enabled = selectedAnswer != -1,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = module.color,
                            contentColor = DeepSpace,
                            disabledContainerColor = DarkGrey,
                            disabledContentColor = MetallicSilver
                        ),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text(
                            text = if (currentQuestionIndex == questions.size - 1) "Finish" else "Next",
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Icon(
                            if (currentQuestionIndex == questions.size - 1) Icons.Default.Check 
                            else Icons.AutoMirrored.Filled.ArrowForward,
                            contentDescription = null,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun AnswerOption(
    text: String,
    isSelected: Boolean,
    optionIndex: Int,
    moduleColor: androidx.compose.ui.graphics.Color,
    onClick: () -> Unit
) {
    val optionLabels = listOf("A", "B", "C", "D")
    
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .selectable(
                selected = isSelected,
                onClick = onClick
            ),
        colors = CardDefaults.cardColors(
            containerColor = if (isSelected) 
                moduleColor.copy(alpha = 0.2f) 
            else 
                CharcoalGrey.copy(alpha = 0.6f)
        ),
        elevation = CardDefaults.cardElevation(
            if (isSelected) 8.dp else 4.dp
        ),
        shape = RoundedCornerShape(12.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Option Circle
            Box(
                modifier = Modifier
                    .size(32.dp)
                    .background(
                        if (isSelected) moduleColor else DarkGrey,
                        CircleShape
                    ),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = optionLabels[optionIndex],
                    color = if (isSelected) DeepSpace else MetallicSilver,
                    fontWeight = FontWeight.Bold,
                    style = MaterialTheme.typography.bodyMedium
                )
            }
            
            Spacer(modifier = Modifier.width(16.dp))
            
            Text(
                text = text,
                style = MaterialTheme.typography.bodyLarge,
                color = WhiteSmoke,
                modifier = Modifier.weight(1f)
            )
            
            // Selection Indicator
            if (isSelected) {
                Icon(
                    Icons.Default.RadioButtonChecked,
                    contentDescription = null,
                    tint = moduleColor,
                    modifier = Modifier.size(24.dp)
                )
            } else {
                Icon(
                    Icons.Default.RadioButtonUnchecked,
                    contentDescription = null,
                    tint = MetallicSilver,
                    modifier = Modifier.size(24.dp)
                )
            }
        }
    }
}

@Composable
fun QuizResultsScreen(
    module: QuizModuleData,
    score: Int,
    totalQuestions: Int,
    quizViewModel: com.example.escape_ar.viewmodel.QuizViewModel,
    onRetry: () -> Unit,
    onNavigateBackToModules: () -> Unit,
    audioManager: com.example.escape_ar.utils.AudioManager
) {
    // Progress is already saved by the QuizScreen when the quiz completes
    // No need to save it again here
    
    val percentage = (score * 100) / totalQuestions
    val isPassed = percentage >= 70
    
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
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            // Results Card
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = CharcoalGrey.copy(alpha = 0.9f)
                ),
                elevation = CardDefaults.cardElevation(16.dp)
            ) {
                Column(
                    modifier = Modifier.padding(32.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    // Success/Failure Icon
                    Box(
                        modifier = Modifier
                            .size(80.dp)
                            .background(
                                if (isPassed) GlowGreen.copy(alpha = 0.2f) 
                                else CrimsonRed.copy(alpha = 0.2f),
                                CircleShape
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            if (isPassed) Icons.Default.CheckCircle else Icons.Default.Cancel,
                            contentDescription = null,
                            tint = if (isPassed) GlowGreen else CrimsonRed,
                            modifier = Modifier.size(48.dp)
                        )
                    }
                    
                    Spacer(modifier = Modifier.height(24.dp))
                    
                    Text(
                        text = if (isPassed) "Module Complete!" else "Try Again",
                        style = MaterialTheme.typography.headlineMedium,
                        color = WhiteSmoke,
                        fontWeight = FontWeight.Bold,
                        textAlign = TextAlign.Center
                    )
                    
                    Spacer(modifier = Modifier.height(8.dp))
                    
                    Text(
                        text = module.name,
                        style = MaterialTheme.typography.titleLarge,
                        color = module.color,
                        fontWeight = FontWeight.Bold
                    )
                    
                    Spacer(modifier = Modifier.height(24.dp))
                    
                    // Score Display
                    Row(
                        horizontalArrangement = Arrangement.SpaceEvenly,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        ScoreItem("Score", "$score/$totalQuestions")
                        ScoreItem("Percentage", "$percentage%")
                        ScoreItem("Status", if (isPassed) "PASSED" else "FAILED")
                    }
                    
                    Spacer(modifier = Modifier.height(32.dp))
                    
                    if (isPassed) {
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(
                                containerColor = GlowGreen.copy(alpha = 0.1f)
                            )
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(16.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    Icons.Default.EmojiEvents,
                                    contentDescription = null,
                                    tint = GlowGreen,
                                    modifier = Modifier.size(20.dp)
                                )
                                Spacer(modifier = Modifier.width(12.dp))
                                Text(
                                    text = "Great job! You've mastered this topic!",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = WhiteSmoke
                                )
                            }
                        }
                    }
                    
                    Spacer(modifier = Modifier.height(24.dp))
                    
                    // Action Buttons
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        OutlinedButton(
                            onClick = { 
                                audioManager.playButtonClick()
                                onRetry() 
                            },
                            modifier = Modifier.weight(1f),
                            colors = ButtonDefaults.outlinedButtonColors(
                                contentColor = MetallicSilver
                            )
                        ) {
                            Icon(
                                Icons.Default.Refresh,
                                contentDescription = null,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Retry")
                        }
                        
                        Button(
                            onClick = { 
                                audioManager.playButtonClick()
                                onNavigateBackToModules() 
                            },
                            modifier = Modifier.weight(1f),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = MistyBlue,
                                contentColor = DeepSpace
                            )
                        ) {
                            Text("Continue", fontWeight = FontWeight.Bold)
                            Spacer(modifier = Modifier.width(8.dp))
                            Icon(
                                Icons.AutoMirrored.Filled.ArrowForward,
                                contentDescription = null,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun ScoreItem(label: String, value: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            text = value,
            style = MaterialTheme.typography.headlineSmall,
            color = MistyBlue,
            fontWeight = FontWeight.Bold
        )
        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall,
            color = MetallicSilver
        )
    }
}
