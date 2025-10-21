package com.example.escape_ar.ui.screens

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.automirrored.filled.ExitToApp
import androidx.compose.material3.*
import androidx.compose.runtime.*
import kotlinx.coroutines.launch
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.escape_ar.ui.theme.*
// import com.example.escape_ar.viewmodel.AuthViewModel // Temporarily disabled

data class QuizModule(
    val id: String,
    val title: String,
    val description: String,
    val storylineContext: String,
    val icon: ImageVector,
    val color: androidx.compose.ui.graphics.Color,
    val isCompleted: Boolean = false,
    val progress: Float = 0f,
    val estimatedTime: String = "15-20 min",
    val difficulty: String = "Intermediate",
    val unlocked: Boolean = true
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StudentMainScreen(
    onQuizClick: (String) -> Unit,
    onLessonsClick: (String, String) -> Unit, // moduleId, moduleName
    onProfileClick: () -> Unit,
    onUnityLaunch: () -> Unit,
    onLogout: () -> Unit
) {
    // Check authentication first
    val context = androidx.compose.ui.platform.LocalContext.current
    val sessionManager = remember { com.example.escape_ar.data.SessionManager.getInstance(context) }
    
    // If not authenticated, trigger logout (which navigates to auth)
    LaunchedEffect(Unit) {
        if (!sessionManager.isLoggedIn()) {
            android.util.Log.d("StudentMainScreen", "User not authenticated, logging out")
            onLogout()
            return@LaunchedEffect
        }
    }
    
    val userNameState = remember { mutableStateOf("Student") }
    val repo = remember { com.example.escape_ar.data.repository.UserRepository() }
    var learningProgress by remember { mutableStateOf(0f) }
    val lifecycleOwner = androidx.lifecycle.compose.LocalLifecycleOwner.current
    val scope = rememberCoroutineScope()
    
    fun loadUserData() {
        scope.launch {
            try {
                val user = repo.getCurrentUser()
                if (user != null) {
                    userNameState.value = user.fullName.ifBlank { "Student" }
                    val modules = repo.getUserModulesWithProgress(user.id)
                    if (modules.isNotEmpty()) {
                        learningProgress = modules.count { it.isCompleted }.toFloat() / modules.size
                    }
                }
            } catch (_: Exception) { }
        }
    }
    
    LaunchedEffect(Unit) { loadUserData() }
    DisposableEffect(lifecycleOwner) {
        val observer = androidx.lifecycle.LifecycleEventObserver { _, event ->
            if (event == androidx.lifecycle.Lifecycle.Event.ON_RESUME) loadUserData()
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose { lifecycleOwner.lifecycle.removeObserver(observer) }
    }
    
    // Science Learning Modules
    val quizModules = remember {
        listOf(
            QuizModule(
                id = "decantation",
                title = "Decantation",
                description = "Learn how to separate mixtures by carefully pouring off liquids.",
                storylineContext = "Understand decantation techniques used in chemistry to separate solid-liquid mixtures.",
                icon = Icons.Default.Science,
                color = NeonCyan,
                estimatedTime = "15-20 min",
                difficulty = "Beginner"
            ),
            QuizModule(
                id = "organ_system", 
                title = "Organ Systems",
                description = "Discover how different organ systems work together in the human body.",
                storylineContext = "Learn about the circulatory, respiratory, digestive, and nervous systems.",
                icon = Icons.Default.Favorite,
                color = CrimsonRed,
                estimatedTime = "20-25 min",
                difficulty = "Intermediate"
            ),
            QuizModule(
                id = "simple_machines",
                title = "Simple Machines",
                description = "Explore the six types of simple machines and how they make work easier.",
                storylineContext = "Study levers, pulleys, wedges, screws, inclined planes, and wheel-and-axle.",
                icon = Icons.Default.Build,
                color = AmberAlert,
                estimatedTime = "18-22 min", 
                difficulty = "Intermediate"
            ),
            QuizModule(
                id = "solar_system",
                title = "Solar System",
                description = "Journey through our solar system and learn about planets, moons, and more.",
                storylineContext = "Explore the eight planets, their characteristics, and their positions in space.",
                icon = Icons.Default.Public,
                color = PurpleHaze,
                estimatedTime = "25-30 min",
                difficulty = "Advanced"
            )
        )
    }
    
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
            // Top App Bar with Mission Status
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = CharcoalGrey.copy(alpha = 0.95f)
                ),
                shape = RoundedCornerShape(bottomStart = 24.dp, bottomEnd = 24.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(20.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "Welcome, ${userNameState.value}",
                            style = MaterialTheme.typography.headlineSmall,
                            color = NeonCyan,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "E.S.C.A.P.E. AR Learning Platform",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MetallicSilver
                        )
                    }
                    
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        IconButton(onClick = onProfileClick) {
                            Icon(
                                Icons.Default.Person,
                                contentDescription = "Profile",
                                tint = MetallicSilver
                            )
                        }
                        IconButton(onClick = onLogout) {
                            Icon(
                                Icons.AutoMirrored.Filled.ExitToApp,
                                contentDescription = "Logout",
                                tint = CrimsonRed
                            )
                        }
                    }
                }
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // Main Content
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {

                // AR Experience Card with Description
                item {
                    Card(
                        colors = CardDefaults.cardColors(
                            containerColor = PurpleHaze.copy(alpha = 0.2f)
                        ),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(20.dp)) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    Icons.Default.ViewInAr,
                                    contentDescription = null,
                                    tint = PurpleHaze,
                                    modifier = Modifier.size(32.dp)
                                )
                                Spacer(modifier = Modifier.width(12.dp))
                                Text(
                                    text = "AR Experience",
                                    style = MaterialTheme.typography.titleLarge,
                                    color = WhiteSmoke,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                            
                            Spacer(modifier = Modifier.height(12.dp))
                            
                            Text(
                                text = "Learn science through Augmented Reality with Kylon, your interactive science guide. Experience immersive 3D visualizations of scientific concepts!",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MetallicSilver,
                                lineHeight = 20.sp
                            )
                            
                            Spacer(modifier = Modifier.height(16.dp))
                            
                            Button(
                                onClick = onUnityLaunch,
                                modifier = Modifier.fillMaxWidth(),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = PurpleHaze,
                                    contentColor = WhiteSmoke
                                )
                            ) {
                                Icon(
                                    Icons.Default.PlayArrow,
                                    contentDescription = null,
                                    modifier = Modifier.size(20.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "Start AR Learning",
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                }
                
                // Video Lessons Card
                item {
                    Card(
                        colors = CardDefaults.cardColors(
                            containerColor = AmberAlert.copy(alpha = 0.2f)
                        ),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(20.dp)) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    Icons.Default.PlayCircle,
                                    contentDescription = null,
                                    tint = AmberAlert,
                                    modifier = Modifier.size(32.dp)
                                )
                                Spacer(modifier = Modifier.width(12.dp))
                                Text(
                                    text = "Video Lessons",
                                    style = MaterialTheme.typography.titleLarge,
                                    color = WhiteSmoke,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                            
                            Spacer(modifier = Modifier.height(12.dp))
                            
                            Text(
                                text = "Watch engaging video tutorials and download learning materials. Each module includes YouTube lessons and downloadable files to help you master the concepts.",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MetallicSilver,
                                lineHeight = 20.sp
                            )
                            
                            Spacer(modifier = Modifier.height(16.dp))
                            
                            // Module buttons in 2x2 grid
                            Column(
                                verticalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    OutlinedButton(
                                        onClick = { onLessonsClick("decantation", "Decantation") },
                                        modifier = Modifier.weight(1f),
                                        colors = ButtonDefaults.outlinedButtonColors(
                                            contentColor = NeonCyan
                                        )
                                    ) {
                                        Text("Decantation", fontSize = 12.sp)
                                    }
                                    OutlinedButton(
                                        onClick = { onLessonsClick("organ_system", "Organ Systems") },
                                        modifier = Modifier.weight(1f),
                                        colors = ButtonDefaults.outlinedButtonColors(
                                            contentColor = CrimsonRed
                                        )
                                    ) {
                                        Text("Organs", fontSize = 12.sp)
                                    }
                                }
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    OutlinedButton(
                                        onClick = { onLessonsClick("simple_machines", "Simple Machines") },
                                        modifier = Modifier.weight(1f),
                                        colors = ButtonDefaults.outlinedButtonColors(
                                            contentColor = AmberAlert
                                        )
                                    ) {
                                        Text("Machines", fontSize = 12.sp)
                                    }
                                    OutlinedButton(
                                        onClick = { onLessonsClick("solar_system", "Solar System") },
                                        modifier = Modifier.weight(1f),
                                        colors = ButtonDefaults.outlinedButtonColors(
                                            contentColor = PurpleHaze
                                        )
                                    ) {
                                        Text("Solar", fontSize = 12.sp)
                                    }
                                }
                            }
                        }
                    }
                }
                
                // Science Modules Card
                item {
                    Card(
                        colors = CardDefaults.cardColors(
                            containerColor = NeonCyan.copy(alpha = 0.2f)
                        ),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(20.dp)) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    Icons.Default.School,
                                    contentDescription = null,
                                    tint = NeonCyan,
                                    modifier = Modifier.size(32.dp)
                                )
                                Spacer(modifier = Modifier.width(12.dp))
                                Text(
                                    text = "Science Quizzes",
                                    style = MaterialTheme.typography.titleLarge,
                                    color = WhiteSmoke,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                            
                            Spacer(modifier = Modifier.height(12.dp))
                            
                            Text(
                                text = "Test your knowledge with interactive quizzes on 4 science topics: Decantation, Organ Systems, Simple Machines, and Solar System.",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MetallicSilver,
                                lineHeight = 20.sp
                            )
                            
                            Spacer(modifier = Modifier.height(16.dp))
                            
                            Button(
                                onClick = { onQuizClick("") },
                                modifier = Modifier.fillMaxWidth(),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = NeonCyan,
                                    contentColor = DeepSpace
                                )
                            ) {
                                Icon(
                                    Icons.Default.Quiz,
                                    contentDescription = null,
                                    modifier = Modifier.size(20.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "View Modules",
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun MissionBriefTab(
    isFullStoryMode: Boolean,
    onToggleStory: (Boolean) -> Unit,
    missionProgress: Float,
    onUnityLaunch: () -> Unit
) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            // Emergency Alert
            Card(
                colors = CardDefaults.cardColors(
                    containerColor = CrimsonRed.copy(alpha = 0.2f)
                ),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        Icons.Default.Warning,
                        contentDescription = null,
                        tint = CrimsonRed,
                        modifier = Modifier.size(32.dp)
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text(
                            text = "PRIORITY ALERT",
                            style = MaterialTheme.typography.titleMedium,
                            color = CrimsonRed,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "KYLON captured • Immediate rescue required",
                            style = MaterialTheme.typography.bodyMedium,
                            color = WhiteSmoke
                        )
                    }
                }
            }
        }
        
        item {
            // Mission Overview
            Card(
                colors = CardDefaults.cardColors(
                    containerColor = CharcoalGrey.copy(alpha = 0.8f)
                ),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(20.dp)) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            Icons.Default.Science,
                            contentDescription = null,
                            tint = NeonCyan,
                            modifier = Modifier.size(28.dp)
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Text(
                            text = "Mission Briefing",
                            style = MaterialTheme.typography.headlineSmall,
                            color = NeonCyan,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    Text(
                        text = if (isFullStoryMode) {
                            """
                            CLASSIFIED BRIEFING - AGENT EYES ONLY
                            
                            Target: KYLON (Knowledge Yielding Learning Operations Network)
                            Status: CAPTURED by hostile forces
                            Location: The Labyrinth - Underground research facility
                            
                            BACKGROUND:
                            KYLON is humanity's most advanced AI tutor, containing the accumulated knowledge of our greatest scientists. The entity has been captured by The Labyrinth - a rogue research organization conducting illegal experiments.
                            
                            YOUR MISSION:
                            1. Master the four core scientific disciplines
                            2. Navigate the Labyrinth's dangerous testing chambers
                            3. Locate and rescue KYLON before critical data is extracted
                            4. Preserve the future of human education
                            
                            Remember, Agent: Knowledge is your weapon. Science is your shield.
                            The fate of learning itself rests in your hands.
                            """.trimIndent()
                        } else {
                            """
                            KYLON, our advanced AI tutor, has been captured by The Labyrinth.
                            
                            Your mission: Master four scientific disciplines and navigate dangerous testing chambers to rescue KYLON and preserve the future of education.
                            
                            Complete the training modules to prepare for the rescue mission.
                            """.trimIndent()
                        },
                        style = MaterialTheme.typography.bodyMedium,
                        color = WhiteSmoke,
                        lineHeight = 20.sp
                    )
                    
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        TextButton(
                            onClick = { onToggleStory(!isFullStoryMode) }
                        ) {
                            Text(
                                text = if (isFullStoryMode) "Brief Summary" else "Full Briefing",
                                color = NeonCyan
                            )
                        }
                        
                        Button(
                            onClick = onUnityLaunch,
                            colors = ButtonDefaults.buttonColors(
                                containerColor = AmberAlert,
                                contentColor = DeepSpace
                            )
                        ) {
                            Icon(
                                Icons.Default.RocketLaunch,
                                contentDescription = null,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "ENTER LABYRINTH",
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }
        }
        
    }
}

@Composable
fun TrainingModulesTab(
    quizModules: List<QuizModule>,
    onQuizClick: (String) -> Unit,
    learningProgress: Float,
    onUnityLaunch: () -> Unit
) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Learning Progress Card
        item {
            Card(
                colors = CardDefaults.cardColors(
                    containerColor = CharcoalGrey.copy(alpha = 0.8f)
                ),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(20.dp)) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = "Learning Progress",
                            style = MaterialTheme.typography.titleMedium,
                            color = WhiteSmoke,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "${(learningProgress * 100).toInt()}%",
                            style = MaterialTheme.typography.titleLarge,
                            color = NeonCyan,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    Spacer(modifier = Modifier.height(12.dp))
                    LinearProgressIndicator(
                        progress = { learningProgress },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(8.dp)
                            .clip(RoundedCornerShape(4.dp)),
                        color = NeonCyan,
                        trackColor = DarkGrey
                    )
                }
            }
        }
        
        // AR Experience Button
        item {
            Button(
                onClick = onUnityLaunch,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(64.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = PurpleHaze,
                    contentColor = WhiteSmoke
                ),
                shape = RoundedCornerShape(12.dp)
            ) {
                Icon(
                    Icons.Default.ViewInAr,
                    contentDescription = null,
                    modifier = Modifier.size(28.dp)
                )
                Spacer(modifier = Modifier.width(12.dp))
                Text(
                    text = "Start AR Experience",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
            }
        }
        
        items(quizModules) { module ->
            Card(
                colors = CardDefaults.cardColors(
                    containerColor = CharcoalGrey.copy(alpha = 0.8f)
                ),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(20.dp)) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Box(
                            modifier = Modifier
                                .size(48.dp)
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
                                modifier = Modifier.size(24.dp)
                            )
                        }
                        
                        Spacer(modifier = Modifier.width(16.dp))
                        
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = module.title,
                                style = MaterialTheme.typography.titleMedium,
                                color = WhiteSmoke,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = module.description,
                                style = MaterialTheme.typography.bodyMedium,
                                color = MetallicSilver
                            )
                        }
                        
                        if (module.isCompleted) {
                            Icon(
                                Icons.Default.CheckCircle,
                                contentDescription = "Completed",
                                tint = NeonCyan,
                                modifier = Modifier.size(24.dp)
                            )
                        }
                    }
                    
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    // Module Stats
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                Icons.Default.Timer,
                                contentDescription = null,
                                tint = MetallicSilver,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = module.estimatedTime,
                                style = MaterialTheme.typography.bodySmall,
                                color = MetallicSilver
                            )
                        }
                        
                        Text(
                            text = module.difficulty,
                            style = MaterialTheme.typography.bodySmall,
                            color = when (module.difficulty) {
                                "Beginner" -> NeonCyan
                                "Intermediate" -> AmberAlert
                                "Advanced" -> CrimsonRed
                                else -> MetallicSilver
                            }
                        )
                    }
                    
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    Button(
                        onClick = { onQuizClick(module.id) },
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = module.color,
                            contentColor = DeepSpace
                        )
                    ) {
                        Text(
                            text = if (module.isCompleted) "Review" else "Start Quiz",
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }
    }
}
