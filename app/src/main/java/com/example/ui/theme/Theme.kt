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

private val DarkColorScheme =
  darkColorScheme(
    primary = QuantCyan,
    onPrimary = Color(0xFF00363D),
    primaryContainer = Color(0xFF004F58),
    onPrimaryContainer = Color(0xFF8CEEFF),
    secondary = QuantGreen,
    onSecondary = Color(0xFF003917),
    secondaryContainer = Color(0xFF005324),
    onSecondaryContainer = Color(0xFF67FF8E),
    tertiary = QuantAmber,
    onTertiary = Color(0xFF452B00),
    background = QuantDarkBackground,
    onBackground = QuantTextPrimary,
    surface = QuantDarkSurface,
    onSurface = QuantTextPrimary,
    surfaceVariant = QuantDarkSurfaceVariant,
    onSurfaceVariant = QuantTextSecondary,
    outline = QuantBorder,
    error = QuantRed,
    onError = Color.White
  )

private val LightColorScheme =
  lightColorScheme(
    primary = Color(0xFF006877),
    onPrimary = Color.White,
    primaryContainer = Color(0xFF9EEFFF),
    secondary = Color(0xFF006D31),
    background = QuantLightBackground,
    onBackground = QuantLightTextPrimary,
    surface = QuantLightSurface,
    onSurface = QuantLightTextPrimary,
    surfaceVariant = QuantLightSurfaceVariant,
    onSurfaceVariant = QuantLightTextSecondary,
    outline = Color(0xFFCBD5E1),
    error = QuantRedDim,
    onError = Color.White
  )

@Composable
fun TradeSuiteTheme(
  darkTheme: Boolean = true, // Default to quant dark terminal for high fidelity
  dynamicColor: Boolean = false, // Preserve crisp custom quant tokens
  content: @Composable () -> Unit,
) {
  val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme
  MaterialTheme(colorScheme = colorScheme, typography = Typography, content = content)
}

// Keep backwards-compatibility alias
@Composable
fun MyApplicationTheme(
  darkTheme: Boolean = true,
  dynamicColor: Boolean = false,
  content: @Composable () -> Unit,
) {
  TradeSuiteTheme(darkTheme = darkTheme, dynamicColor = dynamicColor, content = content)
}
