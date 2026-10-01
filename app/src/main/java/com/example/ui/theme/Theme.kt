package com.example.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext

private val DarkColorScheme =
  darkColorScheme(
    primary = SchoolPrimaryDark,
    onPrimary = SchoolOnPrimaryDark,
    primaryContainer = SchoolPrimaryContainerDark,
    onPrimaryContainer = SchoolOnPrimaryContainerDark,
    secondary = SchoolSecondaryDark,
    onSecondary = SchoolOnSecondaryDark,
    secondaryContainer = SchoolSecondaryContainerDark,
    onSecondaryContainer = SchoolOnSecondaryContainerDark,
    tertiary = SchoolTertiaryDark,
    onTertiary = SchoolOnTertiaryDark,
    background = SchoolBackgroundDark,
    onBackground = SchoolOnBackgroundDark,
    surface = SchoolSurfaceDark,
    onSurface = SchoolOnSurfaceDark,
    surfaceVariant = SchoolSurfaceVariantDark,
    onSurfaceVariant = SchoolOnSurfaceVariantDark,
  )

private val LightColorScheme =
  lightColorScheme(
    primary = SchoolPrimary,
    onPrimary = SchoolOnPrimary,
    primaryContainer = SchoolPrimaryContainerDark,
    onPrimaryContainer = SchoolOnPrimaryContainer,
    secondary = SchoolSecondary,
    onSecondary = SchoolOnSecondary,
    secondaryContainer = SchoolSecondaryContainer,
    onSecondaryContainer = SchoolOnSecondaryContainer,
    tertiary = SchoolTertiary,
    onTertiary = SchoolOnTertiary,
    background = SchoolBackground,
    onBackground = SchoolOnBackground,
    surface = SchoolSurface,
    onSurface = SchoolOnSurface,
    surfaceVariant = SchoolSurfaceVariant,
    onSurfaceVariant = SchoolOnSurfaceVariant,
  )

@Composable
fun MyApplicationTheme(
  darkTheme: Boolean = isSystemInDarkTheme(),
  dynamicColor: Boolean = false, // Keep consistent prestigious school branding
  content: @Composable () -> Unit,
) {
  val colorScheme =
    when {
      dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
        val context = LocalContext.current
        if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
      }
      darkTheme -> DarkColorScheme
      else -> LightColorScheme
    }

  MaterialTheme(colorScheme = colorScheme, typography = Typography, content = content)
}
