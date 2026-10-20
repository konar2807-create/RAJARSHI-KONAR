package com.example.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext

private val DarkColorScheme = darkColorScheme(
    primary = Color(0xFF82B1FF),
    onPrimary = Color(0xFF002B75),
    primaryContainer = Color(0xFF0F3E85),
    onPrimaryContainer = Color(0xFFD6E4FF),
    secondary = Color(0xFFB0C6FF),
    onSecondary = Color(0xFF1B305F),
    background = Color(0xFF121418),
    surface = Color(0xFF1E2128),
    surfaceVariant = Color(0xFF282C36),
    onSurface = Color(0xFFE2E4EB),
    onSurfaceVariant = Color(0xFFC4C7D0)
)

private val LightColorScheme = lightColorScheme(
    primary = WordPrimary,
    onPrimary = Color.White,
    primaryContainer = WordContainer,
    onPrimaryContainer = WordOnContainer,
    secondary = WordSecondary,
    onSecondary = Color.White,
    secondaryContainer = WordSecondaryLight,
    onSecondaryContainer = Color(0xFF0D2854),
    tertiary = WordTertiary,
    background = WordCanvasBg,
    surface = Color.White,
    surfaceVariant = WordRibbonBg,
    onBackground = WordTextDark,
    onSurface = WordTextDark,
    onSurfaceVariant = WordTextMuted
)

@Composable
fun WordCraftTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = false, // Keep signature Word blue brand identity
    content: @Composable () -> Unit,
) {
    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }
        darkTheme -> DarkColorScheme
        else -> LightColorScheme
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
