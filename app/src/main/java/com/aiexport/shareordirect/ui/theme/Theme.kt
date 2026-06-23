package com.aiexport.shareordirect.ui.theme

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

private val LightColors = lightColorScheme(
    primary          = Color(0xFF6650A4),
    onPrimary        = Color.White,
    primaryContainer = Color(0xFFEADDFF),
    secondary        = Color(0xFF00897B),
    tertiary         = Color(0xFFFF8F00),
    background       = Color(0xFFFFFBFE),
    surface          = Color(0xFFFFFBFE),
)
private val DarkColors = darkColorScheme(
    primary          = Color(0xFFD0BCFF),
    onPrimary        = Color(0xFF381E72),
    primaryContainer = Color(0xFF4F378B),
    secondary        = Color(0xFF4DB6AC),
    tertiary         = Color(0xFFFFCC02),
    background       = Color(0xFF1C1B1F),
    surface          = Color(0xFF1C1B1F),
)

@Composable
fun ShareToFileTheme(darkTheme: Boolean = isSystemInDarkTheme(), content: @Composable () -> Unit) {
    val scheme = when {
        Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val ctx = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(ctx) else dynamicLightColorScheme(ctx)
        }
        darkTheme -> DarkColors
        else      -> LightColors
    }
    MaterialTheme(colorScheme = scheme, content = content)
}
