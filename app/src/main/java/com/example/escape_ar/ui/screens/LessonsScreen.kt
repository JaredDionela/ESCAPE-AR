package com.example.escape_ar.ui.screens

import android.content.Intent
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.FileProvider
import com.example.escape_ar.data.SessionManager
import com.example.escape_ar.data.model.Lesson
import com.example.escape_ar.data.model.LessonFile
import com.example.escape_ar.data.model.LessonProgress
import com.example.escape_ar.data.repository.LessonRepository
import com.example.escape_ar.ui.theme.*
import kotlinx.coroutines.launch
import java.io.File

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LessonsScreen(
    moduleId: String,
    moduleName: String,
    onBackClick: () -> Unit,
    onLessonClick: (lessonId: String, lessonTitle: String, youtubeVideoId: String) -> Unit = { _, _, _ -> }
) {
    val context = LocalContext.current
    val sessionManager = remember { SessionManager.getInstance(context) }
    val repository = remember { LessonRepository() }
    val scope = rememberCoroutineScope()

    var lessons by remember { mutableStateOf<List<Pair<Lesson, LessonProgress?>>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    // Load lessons on mount
    LaunchedEffect(moduleId) {
        isLoading = true
        errorMessage = null
        try {
            val currentUser = sessionManager.getCurrentUser()
            if (currentUser != null) {
                lessons = repository.getLessonsWithProgress(currentUser.id, moduleId)
            } else {
                errorMessage = "User not authenticated"
            }
        } catch (e: Exception) {
            errorMessage = "Failed to load lessons: ${e.message}"
        } finally {
            isLoading = false
        }
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
        Column(modifier = Modifier.fillMaxSize()) {
            // Top App Bar
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = moduleName,
                            style = MaterialTheme.typography.titleLarge,
                            color = WhiteSmoke,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "Video Lessons & Materials",
                            style = MaterialTheme.typography.bodySmall,
                            color = MetallicSilver
                        )
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(
                            Icons.Default.ArrowBack,
                            contentDescription = "Back",
                            tint = NeonCyan
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = CharcoalGrey.copy(alpha = 0.95f)
                )
            )

            // Content
            when {
                isLoading -> {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator(color = NeonCyan)
                    }
                }
                errorMessage != null -> {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            Icon(
                                Icons.Default.ErrorOutline,
                                contentDescription = null,
                                tint = CrimsonRed,
                                modifier = Modifier.size(64.dp)
                            )
                            Text(
                                text = errorMessage ?: "Unknown error",
                                color = WhiteSmoke,
                                style = MaterialTheme.typography.bodyLarge
                            )
                        }
                    }
                }
                lessons.isEmpty() -> {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(16.dp),
                            modifier = Modifier.padding(32.dp)
                        ) {
                            Icon(
                                Icons.Default.MenuBook,
                                contentDescription = null,
                                tint = MetallicSilver,
                                modifier = Modifier.size(64.dp)
                            )
                            Text(
                                text = "No lessons available yet",
                                color = WhiteSmoke,
                                style = MaterialTheme.typography.titleMedium
                            )
                            Text(
                                text = "Lessons will be added by your teacher soon!",
                                color = MetallicSilver,
                                style = MaterialTheme.typography.bodyMedium,
                                textAlign = androidx.compose.ui.text.style.TextAlign.Center
                            )
                        }
                    }
                }
                else -> {
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        items(lessons) { (lesson, progress) ->
                            LessonCard(
                                lesson = lesson,
                                progress = progress,
                                onLessonClick = {
                                    // Navigate to in-app video player
                                    onLessonClick(lesson.id, lesson.title, lesson.youtubeVideoId ?: "")
                                }
                            )
                        }

                        // Placeholder info card
                        item {
                            Card(
                                colors = CardDefaults.cardColors(
                                    containerColor = PurpleHaze.copy(alpha = 0.2f)
                                ),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Row(
                                    modifier = Modifier.padding(16.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(
                                        Icons.Default.Info,
                                        contentDescription = null,
                                        tint = PurpleHaze,
                                        modifier = Modifier.size(24.dp)
                                    )

                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LessonCard(
    lesson: Lesson,
    progress: LessonProgress?,
    onLessonClick: () -> Unit
) {
    val context = LocalContext.current
    val repository = remember { LessonRepository() }
    val sessionManager = remember { SessionManager.getInstance(context) }
    val scope = rememberCoroutineScope()
    
    var lessonFiles by remember { mutableStateOf<List<LessonFile>>(emptyList()) }
    var isExpanded by remember { mutableStateOf(false) }
    var isLoadingFiles by remember { mutableStateOf(false) }

    Card(
        colors = CardDefaults.cardColors(
            containerColor = CharcoalGrey.copy(alpha = 0.8f)
        ),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            // Lesson Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.Top
            ) {
                // Play Icon
                Box(
                    modifier = Modifier
                        .size(56.dp)
                        .clip(CircleShape)
                        .background(CrimsonRed.copy(alpha = 0.2f))
                        .clickable(onClick = onLessonClick),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        Icons.Default.PlayArrow,
                        contentDescription = "Play video",
                        tint = CrimsonRed,
                        modifier = Modifier.size(32.dp)
                    )
                }

                Spacer(modifier = Modifier.width(16.dp))

                // Lesson Info
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = lesson.title,
                        style = MaterialTheme.typography.titleMedium,
                        color = WhiteSmoke,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = lesson.description,
                        style = MaterialTheme.typography.bodySmall,
                        color = MetallicSilver,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        if (progress != null && progress.completed) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    Icons.Default.CheckCircle,
                                    contentDescription = null,
                                    tint = NeonCyan,
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = "Completed",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = NeonCyan
                                )
                            }
                        }
                    }
                }
            }

            // Progress Bar
            if (progress != null && progress.videoProgress > 0f) {
                Spacer(modifier = Modifier.height(12.dp))
                LinearProgressIndicator(
                    progress = { progress.videoProgress },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(4.dp)
                        .clip(RoundedCornerShape(2.dp)),
                    color = NeonCyan,
                    trackColor = DarkGrey
                )
            }

            // Downloadable Files Section
            Spacer(modifier = Modifier.height(12.dp))
            Divider(color = DarkGrey)
            Spacer(modifier = Modifier.height(12.dp))

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable {
                        isExpanded = !isExpanded
                        if (isExpanded && lessonFiles.isEmpty() && !isLoadingFiles) {
                            isLoadingFiles = true
                            scope.launch {
                                lessonFiles = repository.getLessonFiles(lesson.id)
                                isLoadingFiles = false
                            }
                        }
                    },
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        Icons.Default.AttachFile,
                        contentDescription = null,
                        tint = AmberAlert,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Downloadable Files",
                        style = MaterialTheme.typography.bodyMedium,
                        color = WhiteSmoke,
                        fontWeight = FontWeight.Medium
                    )
                }
                Icon(
                    if (isExpanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                    contentDescription = null,
                    tint = MetallicSilver
                )
            }

            // Expandable Files List
            AnimatedVisibility(visible = isExpanded) {
                Column(modifier = Modifier.padding(top = 12.dp)) {
                    when {
                        isLoadingFiles -> {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.Center
                            ) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(24.dp),
                                    color = NeonCyan
                                )
                            }
                        }
                        lessonFiles.isEmpty() -> {
                            Text(
                                text = "📁 No files available yet",
                                color = MetallicSilver,
                                style = MaterialTheme.typography.bodySmall,
                                modifier = Modifier.padding(vertical = 8.dp)
                            )
                        }
                        else -> {
                            lessonFiles.forEach { file ->
                                FileDownloadItem(file = file)
                                if (file != lessonFiles.last()) {
                                    Spacer(modifier = Modifier.height(8.dp))
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun FileDownloadItem(file: LessonFile) {
    val context = LocalContext.current
    val downloadLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) { /* Handle download result */ }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(8.dp))
            .background(DarkGrey.copy(alpha = 0.3f))
            .clickable {
                // Open file URL in browser or download
                val intent = Intent(Intent.ACTION_VIEW, Uri.parse(file.fileUrl))
                context.startActivity(intent)
            }
            .padding(12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            when (file.fileType.lowercase()) {
                "pdf" -> Icons.Default.PictureAsPdf
                "pptx", "ppt" -> Icons.Default.Slideshow
                "docx", "doc" -> Icons.Default.Description
                "image", "jpg", "png" -> Icons.Default.Image
                else -> Icons.Default.InsertDriveFile
            },
            contentDescription = null,
            tint = when (file.fileType.lowercase()) {
                "pdf" -> CrimsonRed
                "pptx", "ppt" -> AmberAlert
                else -> NeonCyan
            },
            modifier = Modifier.size(24.dp)
        )
        Spacer(modifier = Modifier.width(12.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = file.fileName,
                color = WhiteSmoke,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Medium,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Text(
                text = formatFileSize(file.fileSize),
                color = MetallicSilver,
                style = MaterialTheme.typography.bodySmall
            )
        }
        Icon(
            Icons.Default.Download,
            contentDescription = "Download",
            tint = NeonCyan,
            modifier = Modifier.size(20.dp)
        )
    }
}

fun formatFileSize(bytes: Long): String {
    return when {
        bytes < 1024 -> "$bytes B"
        bytes < 1024 * 1024 -> "${bytes / 1024} KB"
        else -> String.format("%.1f MB", bytes / (1024.0 * 1024.0))
    }
}
