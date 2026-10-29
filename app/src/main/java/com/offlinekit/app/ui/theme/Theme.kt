package com.offlinekit.app.ui.theme

import android.app.Activity
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

private val Dark = darkColorScheme(
    primary = Color(0xFFE5393B), onPrimary = Color.White,
    background = Color(0xFF0B0B0C), onBackground = Color(0xFFF5F5F5),
    surface = Color(0xFF131315), onSurface = Color(0xFFF5F5F5),
    surfaceVariant = Color(0xFF1C1C1F), onSurfaceVariant = Color(0xFFB0B0B5),
    outline = Color(0xFF3A3A3F), error = Color(0xFFFF7A7A),
)

private val Light = lightColorScheme(
    primary = Color(0xFFC62828), onPrimary = Color.White,
    background = Color(0xFFFAFAFA), onBackground = Color(0xFF111113),
    surface = Color.White, onSurface = Color(0xFF111113),
    surfaceVariant = Color(0xFFEFEFF1), onSurfaceVariant = Color(0xFF55555B),
    outline = Color(0xFFC8C8CE), error = Color(0xFFB3261E),
)

@Composable
fun OfflineKitTheme(mode: String, content: @Composable () -> Unit) {
    val dark = when (mode) {
        "dark" -> true
        "light" -> false
        else -> isSystemInDarkTheme()
    }
    val view = LocalView.current
    SideEffect {
        val window = (view.context as? Activity)?.window
        if (window != null) {
            WindowCompat.getInsetsController(window, view).apply {
                isAppearanceLightStatusBars = !dark
                isAppearanceLightNavigationBars = !dark
            }
        }
    }
    MaterialTheme(colorScheme = if (dark) Dark else Light, content = content)
}
