package com.example.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable

val EditCopyColorScheme = darkColorScheme(
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
    background = DeepBlack,
    onBackground = TextPrimary,
    surface = MidnightNavy,
    onSurface = TextPrimary,
    surfaceVariant = SurfaceNavy,
    onSurfaceVariant = TextSecondary,
    outline = CardBorderSubtle,
    outlineVariant = GlassBorder,
    error = RedError,
    onError = DeepBlack
)

@Composable
fun EditCopyTheme(
    content: @Composable () -> Unit
) {
    // Pro video editing software is intentionally dark-themed by design
    MaterialTheme(
        colorScheme = EditCopyColorScheme,
        typography = Typography,
        content = content
    )
}
