package com.example.escape_ar.ui.screens

import androidx.compose.animation.core.*
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.scale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.example.escape_ar.R
import com.example.escape_ar.ui.theme.*
import kotlinx.coroutines.delay

@Composable
fun LoadingScreen(
    message: String? = null,
    onLoadingComplete: () -> Unit
) {
    // Animation values (retain subtle breathing effect on logo)
    val fadeInAnimation by rememberInfiniteTransition(label = "fadeIn").animateFloat(
        initialValue = 0.4f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(2200, easing = EaseInOut),
            repeatMode = RepeatMode.Reverse
        ),
        label = "fade"
    )
    val scaleAnimation by rememberInfiniteTransition(label = "scale").animateFloat(
        initialValue = 0.93f,
        targetValue = 1.07f,
        animationSpec = infiniteRepeatable(
            animation = tween(3600, easing = EaseInOut),
            repeatMode = RepeatMode.Reverse
        ),
        label = "scale"
    )
    // Single-phase loading: show logo immediately and complete after delay
    LaunchedEffect(Unit) {
        delay(3000) // splash duration
        onLoadingComplete()
    }
    
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.radialGradient(
                    colors = listOf(
                        DeepSpace,
                        DarkGrey.copy(alpha = 0.8f),
                        DeepSpace
                    ),
                    radius = 1000f
                )
            ),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            // Branded Logo (requires drawable resource: logo.png in res/drawable)
            Card(
                modifier = Modifier
                    .size(180.dp)
                    .scale(scaleAnimation)
                    .alpha(fadeInAnimation),
                shape = RoundedCornerShape(36.dp),
                colors = CardDefaults.cardColors(containerColor = CharcoalGrey),
                elevation = CardDefaults.cardElevation(24.dp)
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(
                            Brush.linearGradient(
                                colors = listOf(
                                    NeonCyan.copy(alpha = 0.25f),
                                    ElectricBlue.copy(alpha = 0.25f),
                                    PurpleHaze.copy(alpha = 0.25f)
                                )
                            )
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    // Fallback if logo resource not yet placed
                    val logoPainter = runCatching { painterResource(id = R.drawable.logo) }.getOrNull()
                    if (logoPainter != null) {
                        Image(
                            painter = logoPainter,
                            contentDescription = "App Logo",
                            modifier = Modifier.scale(scaleAnimation * 0.95f)
                        )
                    } else {
                        Text(
                            text = "ESC",
                            style = MaterialTheme.typography.displayMedium,
                            color = NeonCyan,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
            Spacer(modifier = Modifier.height(48.dp))
            LinearProgressIndicator(
                modifier = Modifier
                    .width(220.dp)
                    .height(5.dp),
                color = NeonCyan,
                trackColor = DarkGrey
            )
            Spacer(modifier = Modifier.height(20.dp))
            Text(
                text = message ?: "Initializing Systems...",
                style = MaterialTheme.typography.bodyMedium,
                color = TextMuted.copy(alpha = 0.7f),
                textAlign = TextAlign.Center
            )
        }
    }
}
