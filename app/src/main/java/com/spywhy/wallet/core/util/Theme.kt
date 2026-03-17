package com.spywhy.wallet.core.util

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.Typography
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp

// SpyWhy Dark Theme Colors - Pure black/gray, NO blue
object SpyWhyColors {
    val Black = Color(0xFF000000)
    val DarkGray = Color(0xFF121212)
    val MediumGray = Color(0xFF1E1E1E)
    val LightGray = Color(0xFF2C2C2C)
    val BorderGray = Color(0xFF3A3A3A)
    val TextPrimary = Color(0xFFE0E0E0)
    val TextSecondary = Color(0xFF9E9E9E)
    val TextDisabled = Color(0xFF616161)
    val AccentGreen = Color(0xFF4CAF50)
    val AccentRed = Color(0xFFF44336)
    val AccentOrange = Color(0xFFFF9800)
    val AccentYellow = Color(0xFFFFEB3B)
    val White = Color(0xFFFFFFFF)
    val CardBackground = Color(0xFF1A1A1A)
    val SurfaceVariant = Color(0xFF252525)
}

private val SpyWhyDarkColorScheme = darkColorScheme(
    primary = SpyWhyColors.TextPrimary,
    onPrimary = SpyWhyColors.Black,
    primaryContainer = SpyWhyColors.LightGray,
    onPrimaryContainer = SpyWhyColors.TextPrimary,
    secondary = SpyWhyColors.AccentGreen,
    onSecondary = SpyWhyColors.Black,
    secondaryContainer = SpyWhyColors.LightGray,
    onSecondaryContainer = SpyWhyColors.AccentGreen,
    tertiary = SpyWhyColors.AccentOrange,
    onTertiary = SpyWhyColors.Black,
    error = SpyWhyColors.AccentRed,
    onError = SpyWhyColors.White,
    errorContainer = Color(0xFF3D0000),
    onErrorContainer = SpyWhyColors.AccentRed,
    background = SpyWhyColors.Black,
    onBackground = SpyWhyColors.TextPrimary,
    surface = SpyWhyColors.DarkGray,
    onSurface = SpyWhyColors.TextPrimary,
    surfaceVariant = SpyWhyColors.SurfaceVariant,
    onSurfaceVariant = SpyWhyColors.TextSecondary,
    outline = SpyWhyColors.BorderGray,
    outlineVariant = SpyWhyColors.LightGray,
    inverseSurface = SpyWhyColors.TextPrimary,
    inverseOnSurface = SpyWhyColors.Black,
)

private val SpyWhyTypography = Typography(
    headlineLarge = TextStyle(
        fontWeight = FontWeight.Bold,
        fontSize = 28.sp,
        lineHeight = 36.sp,
        color = SpyWhyColors.TextPrimary
    ),
    headlineMedium = TextStyle(
        fontWeight = FontWeight.Bold,
        fontSize = 24.sp,
        lineHeight = 32.sp,
        color = SpyWhyColors.TextPrimary
    ),
    headlineSmall = TextStyle(
        fontWeight = FontWeight.SemiBold,
        fontSize = 20.sp,
        lineHeight = 28.sp,
        color = SpyWhyColors.TextPrimary
    ),
    titleLarge = TextStyle(
        fontWeight = FontWeight.SemiBold,
        fontSize = 18.sp,
        lineHeight = 26.sp,
        color = SpyWhyColors.TextPrimary
    ),
    titleMedium = TextStyle(
        fontWeight = FontWeight.Medium,
        fontSize = 16.sp,
        lineHeight = 24.sp,
        color = SpyWhyColors.TextPrimary
    ),
    bodyLarge = TextStyle(
        fontWeight = FontWeight.Normal,
        fontSize = 16.sp,
        lineHeight = 24.sp,
        color = SpyWhyColors.TextPrimary
    ),
    bodyMedium = TextStyle(
        fontWeight = FontWeight.Normal,
        fontSize = 14.sp,
        lineHeight = 20.sp,
        color = SpyWhyColors.TextSecondary
    ),
    bodySmall = TextStyle(
        fontWeight = FontWeight.Normal,
        fontSize = 12.sp,
        lineHeight = 16.sp,
        color = SpyWhyColors.TextSecondary
    ),
    labelLarge = TextStyle(
        fontWeight = FontWeight.Medium,
        fontSize = 14.sp,
        lineHeight = 20.sp,
        color = SpyWhyColors.TextPrimary
    ),
    labelMedium = TextStyle(
        fontWeight = FontWeight.Medium,
        fontSize = 12.sp,
        lineHeight = 16.sp,
        color = SpyWhyColors.TextSecondary
    ),
)

@Composable
fun SpyWhyTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = SpyWhyDarkColorScheme,
        typography = SpyWhyTypography,
        content = content
    )
}
