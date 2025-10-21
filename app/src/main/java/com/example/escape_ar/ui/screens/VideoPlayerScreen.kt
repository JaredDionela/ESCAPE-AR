package com.example.escape_ar.ui.screens

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.OpenInNew
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.PlayerConstants
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.YouTubePlayer
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.listeners.AbstractYouTubePlayerListener
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.views.YouTubePlayerView

/**
 * Hybrid YouTube Player Screen
 * 
 * Features:
 * - Tries to play video in-app (embedded player)
 * - If Error 15 occurs (video not embeddable), shows button to open in YouTube
 * - Tracks video progress for in-app playback
 * - Fallback to YouTube app for restricted videos
 */

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun VideoPlayerScreen(
    lessonId: String,
    lessonTitle: String,
    youtubeVideoId: String,
    onBackClick: () -> Unit,
    onVideoCompleted: () -> Unit = {}
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    var isVideoCompleted by remember { mutableStateOf(false) }
    var currentTime by remember { mutableStateOf(0f) }
    var duration by remember { mutableStateOf(0f) }
    var youtubePlayer by remember { mutableStateOf<YouTubePlayer?>(null) }
    var hasPlaybackError by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf("") }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Watch Lesson") },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.Default.ArrowBack, "Back")
                    }
                },
                actions = {
                    // Always show "Open in YouTube" button
                    IconButton(
                        onClick = {
                            openInYouTube(context, youtubeVideoId)
                        }
                    ) {
                        Icon(Icons.Default.OpenInNew, "Open in YouTube")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    titleContentColor = MaterialTheme.colorScheme.onPrimary,
                    navigationIconContentColor = MaterialTheme.colorScheme.onPrimary,
                    actionIconContentColor = MaterialTheme.colorScheme.onPrimary
                )
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
        ) {
            // YouTube Player or Error Message
            if (!hasPlaybackError) {
                AndroidView(
                    factory = { ctx ->
                        YouTubePlayerView(ctx).apply {
                            lifecycleOwner.lifecycle.addObserver(this)
                            
                            addYouTubePlayerListener(object : AbstractYouTubePlayerListener() {
                                override fun onReady(player: YouTubePlayer) {
                                    youtubePlayer = player
                                    player.loadVideo(youtubeVideoId, 0f)
                                }
                                
                                override fun onCurrentSecond(player: YouTubePlayer, second: Float) {
                                    currentTime = second
                                    
                                    // Check if video is near completion (95%)
                                    if (duration > 0 && second >= duration * 0.95f && !isVideoCompleted) {
                                        isVideoCompleted = true
                                        onVideoCompleted()
                                    }
                                }
                                
                                override fun onVideoDuration(player: YouTubePlayer, videoDuration: Float) {
                                    duration = videoDuration
                                }
                                
                                override fun onError(player: YouTubePlayer, error: PlayerConstants.PlayerError) {
                                    when (error) {
                                        PlayerConstants.PlayerError.VIDEO_NOT_PLAYABLE_IN_EMBEDDED_PLAYER -> {
                                            hasPlaybackError = true
                                            errorMessage = "This video cannot be played in embedded players (Error 15).\n\nThe video owner has disabled playback outside of YouTube."
                                        }
                                        PlayerConstants.PlayerError.VIDEO_NOT_FOUND -> {
                                            hasPlaybackError = true
                                            errorMessage = "Video not found. Please check the video ID."
                                        }
                                        else -> {
                                            hasPlaybackError = true
                                            errorMessage = "Unable to play video: ${error.name}"
                                        }
                                    }
                                }
                            })
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .aspectRatio(16f / 9f)
                )
            } else {
                // Error State - Show message and "Open in YouTube" button
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .aspectRatio(16f / 9f),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.errorContainer
                    )
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(24.dp),
                        verticalArrangement = Arrangement.Center,
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "⚠️",
                            style = MaterialTheme.typography.displayLarge
                        )
                        
                        Spacer(modifier = Modifier.height(16.dp))
                        
                        Text(
                            text = errorMessage,
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.onErrorContainer,
                            textAlign = TextAlign.Center
                        )
                        
                        Spacer(modifier = Modifier.height(24.dp))
                        
                        Button(
                            onClick = { openInYouTube(context, youtubeVideoId) },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = MaterialTheme.colorScheme.error
                            )
                        ) {
                            Icon(Icons.Default.OpenInNew, contentDescription = null)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Watch on YouTube")
                        }
                    }
                }
            }
            
            // Lesson Information
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            ) {
                Text(
                    text = lessonTitle,
                    style = MaterialTheme.typography.headlineSmall,
                    color = MaterialTheme.colorScheme.onSurface
                )
                
                Spacer(modifier = Modifier.height(16.dp))
                
                // Progress Indicator
                if (duration > 0) {
                    LinearProgressIndicator(
                        progress = (currentTime / duration).coerceIn(0f, 1f),
                        modifier = Modifier.fillMaxWidth()
                    )
                    
                    Spacer(modifier = Modifier.height(4.dp))
                    
                    Text(
                        text = "${formatTime(currentTime)} / ${formatTime(duration)}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                
                // Completion Badge
                if (isVideoCompleted) {
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    Card(
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.primaryContainer
                        )
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            horizontalArrangement = Arrangement.Center,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "✅ Video Completed!",
                                style = MaterialTheme.typography.titleMedium,
                                color = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                        }
                    }
                }
            }
        }
    }
    
    // Cleanup
    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_DESTROY) {
                youtubePlayer?.pause()
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
        }
    }
}

private fun formatTime(seconds: Float): String {
    val totalSeconds = seconds.toInt()
    val minutes = totalSeconds / 60
    val secs = totalSeconds % 60
    return String.format("%d:%02d", minutes, secs)
}

private fun openInYouTube(context: android.content.Context, videoId: String) {
    try {
        // Try to open in YouTube app first
        val appIntent = Intent(Intent.ACTION_VIEW, Uri.parse("vnd.youtube:$videoId"))
        appIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        context.startActivity(appIntent)
    } catch (e: Exception) {
        // If YouTube app not installed, open in browser
        val webIntent = Intent(Intent.ACTION_VIEW, 
            Uri.parse("https://www.youtube.com/watch?v=$videoId"))
        webIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        context.startActivity(webIntent)
    }
}
