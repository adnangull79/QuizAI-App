package com.example.quizai.ui.theme

import android.app.Activity
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat
import com.example.notepad.ui.theme.Typography


// ---------------------- LIGHT MODE ----------------------
private val LightColorScheme = lightColorScheme(
    primary = PrimaryColor,
    onPrimary = Color.White,
    primaryContainer = PrimaryLight,

    background = BackgroundColor,     // Your white background
    surface = SurfaceColor,           // Light card surface
    onBackground = TextPrimary,
    onSurface = TextPrimary,

    secondary = PrimaryLight,
    tertiary = PrimaryDark
)


// ---------------------- DARK MODE ----------------------
private val DarkColorScheme = darkColorScheme(
    primary = PrimaryColor,
    onPrimary = Color.Black,
    primaryContainer = PrimaryDark,

    background = Color(0xFF0F0F0F),      // <- PURE DARK BACKGROUND
    surface = Color(0xFF1A1A1A),         // <- DARK SURFACES FOR CARDS
    onBackground = Color.White,          // <- WHITE TEXT
    onSurface = Color.White,

    secondary = PrimaryLight,
    tertiary = PrimaryDark
)


// ---------------------- THEME WRAPPER ----------------------
@Composable
fun QuizAITheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit
) {

    val colorScheme =
        if (darkTheme) DarkColorScheme
        else LightColorScheme

    // Status bar color and icons
    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            window.statusBarColor = colorScheme.primary.toArgb()
            WindowCompat.getInsetsController(window, view)
                .isAppearanceLightStatusBars = !darkTheme
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        shapes = Shapes,
        content = content
    )
}
