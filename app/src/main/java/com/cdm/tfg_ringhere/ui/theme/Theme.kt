package com.cdm.tfg_ringhere.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

// Paleta Clara (La que tenías)
private val LightColorScheme = lightColorScheme(
    primary = Color(0xFF2B3A8B),
    background = Color(0xFFF7F8FC),
    surface = Color(0xFFFFFFFF),
    surfaceVariant = Color(0xFFEAEBEE), // Para tarjetas grises
    onSurface = Color.Black,
    onBackground = Color.Black
)

// Paleta Oscura
private val DarkColorScheme = darkColorScheme(
    primary = Color(0xFF5C73E6), // Un azul más claro para que resalte sobre fondo negro
    background = Color(0xFF121212),
    surface = Color(0xFF1E1E1E),
    surfaceVariant = Color(0xFF2C2C2C),
    onSurface = Color.White,
    onBackground = Color.White
)

// Paleta Alto Contraste (Blanco y Negro puro)
private val HighContrastColorScheme = lightColorScheme(
    primary = Color.Black,
    background = Color.White,
    surface = Color.White,
    surfaceVariant = Color.LightGray,
    onSurface = Color.Black,
    onBackground = Color.Black
)

@Composable
fun TFG_RingHereTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    highContrast: Boolean = false,
    content: @Composable () -> Unit
) {
    val colorScheme = when {
        highContrast -> HighContrastColorScheme
        darkTheme -> DarkColorScheme
        else -> LightColorScheme
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography, // Asegúrate de mantener tu tipografía si la tienes
        content = content
    )
}