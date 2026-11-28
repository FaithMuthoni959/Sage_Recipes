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
    onBackground = Color(0xFF2D2D2D),
    onSurface = Color(0xFF2D2D2D),
    surfaceVariant = Color(0xFFFDFBF7),
    onSurfaceVariant = Color(0xFF6B7280),
    outline = Color(0xFFE2F0D9)
)

private val DarkColorScheme = darkColorScheme(
    primary = ButtonGreen,
    onPrimary = Color.White,
    secondary = Color(0xFF48633B),
    background = Color(0xFF222222),      // Smooth dark background
    surface = Color(0xFF363636),         // User requested #363636 surface card color
    onBackground = Color.White,          // White text for dark background
    onSurface = Color.White,             // White text on dark cards
    surfaceVariant = Color(0xFF2D2D2D),
    onSurfaceVariant = Color(0xFFD0D0D0), // Off-white secondary text
    outline = Color(0xFF48633B)
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
