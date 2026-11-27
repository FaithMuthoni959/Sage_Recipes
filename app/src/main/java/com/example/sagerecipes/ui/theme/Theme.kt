package com.example.sagerecipes.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val LightColorScheme = lightColorScheme(
    primary = ButtonGreen,
    onPrimary = Color.White,
    secondary = ChipSelectedBg,
    background = Color(0xFFF7F5F0),
    surface = Color.White,
    onBackground = TextPrimary,
    onSurface = TextPrimary,
    surfaceVariant = Color(0xFFFDFBF7),
    onSurfaceVariant = TextSecondary,
    outline = Color(0xFFE2F0D9)
)

private val DarkColorScheme = darkColorScheme(
    primary = ButtonGreen,
    onPrimary = Color.White,
    secondary = Color(0xFF2E3B2B),
    background = Color(0xFF121212),
    surface = Color(0xFF1E1E1E),
    onBackground = Color(0xFFE6E6E6),
    onSurface = Color(0xFFE6E6E6),
    surfaceVariant = Color(0xFF262626),
    onSurfaceVariant = Color(0xFFB3B3B3),
    outline = Color(0xFF2E3B2B)
)

@Composable
fun SageRecipesTheme(
    darkTheme: Boolean = ThemeManager.isDarkMode || isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
