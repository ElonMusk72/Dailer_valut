package com.example.ui.theme

import android.app.Activity
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

private val DarkColorScheme =
  darkColorScheme(
    primary = VaultAccent,
    onPrimary = Color.White,
    primaryContainer = VaultAccent.copy(alpha = 0.25f),
    onPrimaryContainer = VaultAccentLight,
    secondary = CallGreen,
    onSecondary = Color.White,
    secondaryContainer = CallGreen.copy(alpha = 0.2f),
    onSecondaryContainer = CallGreen,
    tertiary = VaultGold,
    background = VaultDarkBackground,
    onBackground = TextPrimary,
    surface = VaultDarkSurface,
    onSurface = TextPrimary,
    surfaceVariant = VaultDarkSurfaceVariant,
    onSurfaceVariant = TextSecondary,
    outline = TextMuted,
    error = VaultRed,
    onError = Color.White,
  )

@Composable
fun MyApplicationTheme(
  darkTheme: Boolean = true, // Default to dark theme as requested
  dynamicColor: Boolean = false,
  content: @Composable () -> Unit,
) {
  val colorScheme = DarkColorScheme

  val view = LocalView.current
  if (!view.isInEditMode) {
    SideEffect {
      val window = (view.context as? Activity)?.window
      if (window != null) {
        window.statusBarColor = VaultDarkBackground.toArgb()
        window.navigationBarColor = VaultDarkBackground.toArgb()
        val insetsController = WindowCompat.getInsetsController(window, view)
        insetsController.isAppearanceLightStatusBars = false
        insetsController.isAppearanceLightNavigationBars = false
      }
    }
  }

  MaterialTheme(
    colorScheme = colorScheme,
    typography = Typography,
    content = content
  )
}

