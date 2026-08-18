package com.example.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable

private val DarkColorScheme = darkColorScheme(
    primary = FacebookBlue,
    secondary = FacebookBlueDark,
    background = FacebookBgDark,
    surface = FacebookCardDark,
    onPrimary = FacebookCardLight,
    onSecondary = FacebookTextPrimaryDark,
    onBackground = FacebookTextPrimaryDark,
    onSurface = FacebookTextPrimaryDark,
    surfaceVariant = FacebookDividerDark,
    onSurfaceVariant = FacebookTextSecondaryDark
)

private val LightColorScheme = lightColorScheme(
    primary = FacebookBlue,
    secondary = FacebookBlueDark,
    background = FacebookBgLight,
    surface = FacebookCardLight,
    onPrimary = FacebookCardLight,
    onSecondary = FacebookTextPrimaryLight,
    onBackground = FacebookTextPrimaryLight,
    onSurface = FacebookTextPrimaryLight,
    surfaceVariant = FacebookDividerLight,
    onSurfaceVariant = FacebookTextSecondaryLight
)

@Composable
fun FacebookTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
