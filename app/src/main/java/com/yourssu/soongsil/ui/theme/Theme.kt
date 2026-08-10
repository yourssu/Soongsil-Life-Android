package com.yourssu.soongsil.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext

private val DarkColorScheme = darkColorScheme(
    primary = SoongsilPalette.Blue600,
    onPrimary = SoongsilPalette.White,
    primaryContainer = SoongsilPalette.Blue500,
    onPrimaryContainer = SoongsilPalette.White,
    secondary = SoongsilPalette.Slate350,
    onSecondary = SoongsilPalette.Navy900,
    tertiary = SoongsilPalette.Green500,
    background = SoongsilPalette.Gray950,
    onBackground = SoongsilPalette.Gray25,
    surface = SoongsilPalette.Gray900,
    surfaceContainerLow = SoongsilPalette.Gray900,
    surfaceContainer = SoongsilPalette.Gray875,
    onSurface = SoongsilPalette.Gray25,
    surfaceVariant = SoongsilPalette.Navy800,
    onSurfaceVariant = SoongsilPalette.Slate300,
    outline = SoongsilPalette.Gray800,
    outlineVariant = SoongsilPalette.Navy800,
    inverseSurface = SoongsilPalette.White,
    inverseOnSurface = SoongsilPalette.Gray950
)

private val LightColorScheme = lightColorScheme(
    primary = SoongsilPalette.Blue600,
    onPrimary = SoongsilPalette.White,
    primaryContainer = SoongsilPalette.Blue100,
    onPrimaryContainer = SoongsilPalette.Gray950,
    secondary = SoongsilPalette.Slate600,
    onSecondary = SoongsilPalette.White,
    tertiary = SoongsilPalette.Green500,
    background = SoongsilPalette.White,
    onBackground = SoongsilPalette.Gray850,
    surface = SoongsilPalette.White,
    surfaceContainerLow = SoongsilPalette.Gray100,
    surfaceContainer = SoongsilPalette.White,
    onSurface = SoongsilPalette.Gray850,
    surfaceVariant = SoongsilPalette.Gray100,
    onSurfaceVariant = SoongsilPalette.Slate500,
    outline = SoongsilPalette.Slate100,
    outlineVariant = SoongsilPalette.Gray200,
    inverseSurface = SoongsilPalette.Gray950,
    inverseOnSurface = SoongsilPalette.White
)

@Composable
fun SoongsilLifeAndroidTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = false,
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
