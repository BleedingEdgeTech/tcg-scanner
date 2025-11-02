package com.example.mtgocr.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val DarkColorScheme = darkColorScheme(
    primary = PrimaryColor,
    onPrimary = OnPrimary,
    secondary = SecondaryColor,
    onSecondary = OnSecondary,
    background = Color(0xFF121212),
    onBackground = OnBackground,
    surface = Color(0xFF1E1E1E),
    onSurface = OnSurface,
    error = ErrorColor,
    onError = OnError
)

private val LightColorScheme = lightColorScheme(
    primary = PrimaryColor,
    onPrimary = OnPrimary,
    secondary = SecondaryColor,
    onSecondary = OnSecondary,
    background = BackgroundColor,
    onBackground = OnBackground,
    surface = SurfaceColor,
    onSurface = OnSurface,
    error = ErrorColor,
    onError = OnError
)

@Composable
fun MtgOcrTheme(
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