package com.example.escape_ar.ui.components

import androidx.compose.foundation.clickable
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.platform.LocalContext
import com.example.escape_ar.utils.AudioManager

/**
 * Extension function to add click sound effect to any clickable element
 * Usage: Modifier.clickableWithSound { /* your click action */ }
 */
fun Modifier.clickableWithSound(
    enabled: Boolean = true,
    onClickLabel: String? = null,
    onClick: () -> Unit
) = composed {
    val context = LocalContext.current
    val audioManager = remember { AudioManager.getInstance(context) }
    
    this.clickable(
        enabled = enabled,
        onClickLabel = onClickLabel,
        onClick = {
            audioManager.playButtonClick()
            onClick()
        }
    )
}

/**
 * Wrapper composable for Button clicks with automatic sound
 * Usage: Replace Button's onClick with onClickWithSound
 */
@Composable
fun rememberOnClickWithSound(onClick: () -> Unit): () -> Unit {
    val context = LocalContext.current
    val audioManager = remember { AudioManager.getInstance(context) }
    
    return remember(onClick) {
        {
            audioManager.playButtonClick()
            onClick()
        }
    }
}
