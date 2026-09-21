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
    primary = RetailTealLight,
    onPrimary = RetailSlate900,
    primaryContainer = RetailTealDark,
    onPrimaryContainer = RetailTealContainer,
    secondary = RetailStatusBlue,
    onSecondary = Color.White,
    background = RetailSlate900,
    surface = RetailSlate800,
    onSurface = Color.White,
    surfaceVariant = RetailSlate700,
    onSurfaceVariant = RetailSlate300
)

private val LightColorScheme = lightColorScheme(
    primary = RetailTealPrimary,
    onPrimary = Color.White,
    primaryContainer = RetailTealContainer,
    onPrimaryContainer = RetailOnTealContainer,
    secondary = RetailStatusBlue,
    onSecondary = Color.White,
    background = RetailSlate50,
    surface = Color.White,
    onSurface = RetailSlate900,
    surfaceVariant = RetailSlate100,
    onSurfaceVariant = RetailSlate700
)

@Composable
fun MyApplicationTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = false, // Keep consistent custom retail brand colors
    content: @Composable () -> Unit
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
