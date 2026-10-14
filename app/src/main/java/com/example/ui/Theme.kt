package com.example.ui

import androidx.compose.material3.ColorScheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import com.example.ui.theme.Typography

// Core Brand Palette Colors requested for EditCopy aesthetic
val DeepBlack = Color(0xFF07090E)
val MidnightNavy = Color(0xFF0D111D)
val ElectricCyan = Color(0xFF00F0FF)
val Magenta = Color(0xFFFF007F)

// Complementary Studio Accents & Neutrals
val SurfaceNavy = Color(0xFF141A29)
val SurfaceNavyElevated = Color(0xFF1C2438)
val NeonViolet = Color(0xFF7C4DFF)
val TextPrimary = Color(0xFFFFFFFF)
val TextSecondary = Color(0xFF94A3B8)
val GlassBorder = Color(0x3300F0FF)
val CardBorderSubtle = Color(0x1FFFFFFF)

/**
 * Premium EditCopy ColorScheme built with Deep Black, Midnight Navy,
 * Electric Cyan, and Magenta to establish a pro creative studio aesthetic.
 */
val EditCopyColorScheme: ColorScheme = darkColorScheme(
    primary = ElectricCyan,
    onPrimary = DeepBlack,
    primaryContainer = SurfaceNavyElevated,
    onPrimaryContainer = ElectricCyan,
    secondary = NeonViolet,
    onSecondary = DeepBlack,
    secondaryContainer = SurfaceNavy,
    onSecondaryContainer = TextPrimary,
    tertiary = Magenta,
    onTertiary = DeepBlack,
    tertiaryContainer = SurfaceNavyElevated,
    onTertiaryContainer = Magenta,
    background = DeepBlack,
    onBackground = TextPrimary,
    surface = MidnightNavy,
    onSurface = TextPrimary,
    surfaceVariant = SurfaceNavy,
    onSurfaceVariant = TextSecondary,
    outline = CardBorderSubtle,
    outlineVariant = GlassBorder
)

@Composable
fun EditCopyStudioTheme(
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = EditCopyColorScheme,
        typography = Typography,
        content = content
    )
}
