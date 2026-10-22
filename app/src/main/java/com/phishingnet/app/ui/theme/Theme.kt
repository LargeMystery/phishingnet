package com.phishingnet.app.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.Typography
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

/*
 * Palette — "open water":
 *   Abyss    deep water, the brand colour
 *   Shallows cool light background
 *   Line     mid-water blue used for the fishing line and quiet details
 *   Buoy     orange-red, reserved for danger
 *   Signal   amber, for caution
 *   Kelp     green, for safe / passed
 */
val Abyss = Color(0xFF0E2A47)
val AbyssLight = Color(0xFF1D4470)
val Shallows = Color(0xFFE9F1F7)
val Foam = Color(0xFFFFFFFF)
val Line = Color(0xFF7FA7C9)
val Buoy = Color(0xFFF2542D)
val Signal = Color(0xFFE89B12)
val Kelp = Color(0xFF2F8F6B)
val InkBlue = Color(0xFF0A1C30)

private val LightColors = lightColorScheme(
    primary = Abyss,
    onPrimary = Foam,
    primaryContainer = Color(0xFFD3E3F0),
    onPrimaryContainer = Abyss,
    secondary = AbyssLight,
    onSecondary = Foam,
    tertiary = Buoy,
    onTertiary = Foam,
    background = Shallows,
    onBackground = InkBlue,
    surface = Foam,
    onSurface = InkBlue,
    surfaceVariant = Color(0xFFDCE7F0),
    onSurfaceVariant = Color(0xFF4B6077),
    outline = Line,
    outlineVariant = Color(0xFFC3D5E4),
    error = Buoy,
)

private val DarkColors = darkColorScheme(
    primary = Color(0xFF9CC3E6),
    onPrimary = Color(0xFF06182A),
    primaryContainer = Color(0xFF173A5E),
    onPrimaryContainer = Color(0xFFD3E3F0),
    secondary = Line,
    onSecondary = Color(0xFF06182A),
    tertiary = Color(0xFFFF7A55),
    onTertiary = Color(0xFF2A0A00),
    background = Color(0xFF06182A),
    onBackground = Color(0xFFE3EDF5),
    surface = Color(0xFF0D2540),
    onSurface = Color(0xFFE3EDF5),
    surfaceVariant = Color(0xFF15304F),
    onSurfaceVariant = Color(0xFFA8BDD1),
    outline = Color(0xFF4F7295),
    outlineVariant = Color(0xFF223F60),
    error = Color(0xFFFF7A55),
)

private val Sans = FontFamily.SansSerif

private val AppTypography = Typography(
    displaySmall = TextStyle(fontFamily = Sans, fontWeight = FontWeight.Black, fontSize = 40.sp, lineHeight = 42.sp, letterSpacing = (-1.2).sp),
    headlineLarge = TextStyle(fontFamily = Sans, fontWeight = FontWeight.Black, fontSize = 32.sp, lineHeight = 36.sp, letterSpacing = (-0.8).sp),
    headlineMedium = TextStyle(fontFamily = Sans, fontWeight = FontWeight.ExtraBold, fontSize = 26.sp, lineHeight = 31.sp, letterSpacing = (-0.5).sp),
    headlineSmall = TextStyle(fontFamily = Sans, fontWeight = FontWeight.ExtraBold, fontSize = 22.sp, lineHeight = 28.sp, letterSpacing = (-0.3).sp),
    titleLarge = TextStyle(fontFamily = Sans, fontWeight = FontWeight.Bold, fontSize = 19.sp, lineHeight = 25.sp, letterSpacing = (-0.2).sp),
    titleMedium = TextStyle(fontFamily = Sans, fontWeight = FontWeight.Bold, fontSize = 16.sp, lineHeight = 22.sp),
    titleSmall = TextStyle(fontFamily = Sans, fontWeight = FontWeight.Bold, fontSize = 15.sp, lineHeight = 20.sp),
    bodyLarge = TextStyle(fontFamily = Sans, fontWeight = FontWeight.Normal, fontSize = 17.sp, lineHeight = 27.sp),
    bodyMedium = TextStyle(fontFamily = Sans, fontWeight = FontWeight.Normal, fontSize = 15.sp, lineHeight = 22.sp),
    bodySmall = TextStyle(fontFamily = Sans, fontWeight = FontWeight.Normal, fontSize = 13.sp, lineHeight = 18.sp),
    labelLarge = TextStyle(fontFamily = Sans, fontWeight = FontWeight.SemiBold, fontSize = 15.sp, lineHeight = 20.sp),
    labelMedium = TextStyle(fontFamily = Sans, fontWeight = FontWeight.SemiBold, fontSize = 13.sp, lineHeight = 16.sp),
    labelSmall = TextStyle(fontFamily = Sans, fontWeight = FontWeight.Medium, fontSize = 12.sp, lineHeight = 16.sp),
)

/** Monospace is used only where the exact characters matter: addresses, links, raw email text. */
val MonoStyle = TextStyle(fontFamily = FontFamily.Monospace, fontSize = 13.sp, lineHeight = 20.sp)

private val AppShapes = Shapes(
    small = RoundedCornerShape(10.dp),
    medium = RoundedCornerShape(14.dp),
    large = RoundedCornerShape(24.dp),
)

@Composable
fun PhishingNetTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = if (isSystemInDarkTheme()) DarkColors else LightColors,
        typography = AppTypography,
        shapes = AppShapes,
        content = content,
    )
}
