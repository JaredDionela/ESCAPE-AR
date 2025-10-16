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

// Dark Sci-Fi Theme - Mysterious Labyrinth
private val DarkSciFiColorScheme = darkColorScheme(
    primary = NeonCyan,
    onPrimary = DeepSpace,
    secondary = ElectricBlue,
    onSecondary = WhiteSmoke,
    tertiary = PurpleHaze,
    onTertiary = WhiteSmoke,
    background = DeepSpace,
    onBackground = WhiteSmoke,
    surface = DarkGrey,
    onSurface = WhiteSmoke,
    surfaceVariant = CharcoalGrey,
    onSurfaceVariant = MetallicSilver,
    error = CrimsonRed,
    onError = WhiteSmoke,
    outline = MetallicSilver,
    outlineVariant = CharcoalGrey
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