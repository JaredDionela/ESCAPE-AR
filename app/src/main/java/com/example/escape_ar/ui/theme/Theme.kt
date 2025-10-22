package com.example.escape_ar.ui.theme

import android.app.Activity
import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext

// 🎨 **UNIFIED THEME** - Student-Friendly Dark Theme
private val DarkSciFiColorScheme = darkColorScheme(
    primary = SoftTeal,              // Soft Teal (easier on eyes than harsh cyan)
    onPrimary = DarkNavy,
    secondary = SoftPurple,          // Soft Purple (main brand)
    onSecondary = TextWhite,
    tertiary = BrandIndigo,          // Indigo accent
    onTertiary = TextWhite,
    background = DarkNavy,           // Dark Navy background
    onBackground = TextWhite,
    surface = DarkSlate,             // Slate surfaces
    onSurface = TextWhite,
    surfaceVariant = DarkGray,
    onSurfaceVariant = TextMuted,
    error = RoseSoft,                // Soft Rose (easier on eyes)
    onError = TextWhite,
    outline = TextMuted,
    outlineVariant = DarkGray
)

private val LightColorScheme = lightColorScheme(
    primary = Purple40,
    secondary = PurpleGrey40,
    tertiary = Pink40
    /* Other default colors to override
    background = Color(0xFFFFFBFE),
    surface = Color(0xFFFFFBFE),
    onPrimary = Color.White,
    onSecondary = Color.White,
    onTertiary = Color.White,
    onBackground = Color(0xFF1C1B1F),
    onSurface = Color(0xFF1C1B1F),
    */
)

@Composable
fun ESCAPEARTheme(
    darkTheme: Boolean = true, // Always use dark theme for sci-fi vibes
    // Dynamic color is disabled for consistent theming
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit
) {
    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S && !darkTheme -> {
            val context = LocalContext.current
            dynamicLightColorScheme(context)
        }
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S && darkTheme -> {
            val context = LocalContext.current
            dynamicDarkColorScheme(context)
        }
        // Always use dark sci-fi theme regardless of system setting
        else -> DarkSciFiColorScheme
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}