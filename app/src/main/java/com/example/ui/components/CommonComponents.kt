package com.example.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Layers
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.VideoLibrary
import androidx.compose.material.icons.outlined.BookmarkBorder
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.Layers
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material.icons.outlined.VideoLibrary
import androidx.compose.material3.ripple
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.AppScreen
import com.example.ui.theme.AudioTrackGreen
import com.example.ui.theme.CardBorderSubtle
import com.example.ui.theme.ClipTrackCyan
import com.example.ui.theme.ClipTrackPurple
import com.example.ui.theme.DeepBlack
import com.example.ui.theme.ElectricCyan
import com.example.ui.theme.ElectricMagenta
import com.example.ui.theme.GlassBorder
import com.example.ui.theme.GlassSurface
import com.example.ui.theme.KeyframeDiamondGold
import com.example.ui.theme.MidnightNavy
import com.example.ui.theme.NeonPurple
import com.example.ui.theme.NeonViolet
import com.example.ui.theme.SurfaceNavy
import com.example.ui.theme.SurfaceNavyElevated
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary

@Composable
fun GlassCard(
    modifier: Modifier = Modifier,
    shapeRadius: Dp = 16.dp,
    borderGlowColor: Color = GlassBorder,
    backgroundColor: Color = GlassSurface,
    onClick: (() -> Unit)? = null,
    content: @Composable () -> Unit
) {
    val shape = RoundedCornerShape(shapeRadius)
    val cardModifier = if (onClick != null) {
        modifier
            .clip(shape)
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = ripple(color = ElectricCyan),
                onClick = onClick
            )
    } else {
        modifier.clip(shape)
    }

    Surface(
        modifier = cardModifier,
        shape = shape,
        color = backgroundColor,
        border = BorderStroke(1.dp, borderGlowColor),
        tonalElevation = 4.dp
    ) {
        content()
    }
}

@Composable
fun NeonButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    icon: ImageVector? = null,
    enabled: Boolean = true,
    testTag: String = "neon_button"
) {
    val gradientBrush = Brush.horizontalGradient(
        colors = if (enabled) listOf(ElectricCyan, NeonViolet, ElectricMagenta)
        else listOf(SurfaceNavy, SurfaceNavyElevated)
    )

    Box(
        modifier = modifier
            .height(50.dp)
            .clip(RoundedCornerShape(25.dp))
            .background(brush = gradientBrush)
            .clickable(
                enabled = enabled,
                interactionSource = remember { MutableInteractionSource() },
                indication = ripple(color = Color.White),
                onClick = onClick
            )
            .padding(horizontal = 24.dp, vertical = 12.dp)
            .testTag(testTag),
        contentAlignment = Alignment.Center
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            if (icon != null) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = if (enabled) DeepBlack else TextMuted,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
            }
            Text(
                text = text,
                color = if (enabled) DeepBlack else TextMuted,
                fontWeight = FontWeight.Bold,
                fontSize = 15.sp,
                letterSpacing = 0.3.sp
            )
        }
    }
}

@Composable
fun NeonOutlinedButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    icon: ImageVector? = null,
    accentColor: Color = ElectricCyan,
    testTag: String = "outlined_button"
) {
    OutlinedButton(
        onClick = onClick,
        modifier = modifier
            .height(48.dp)
            .testTag(testTag),
        shape = RoundedCornerShape(24.dp),
        border = BorderStroke(1.2.dp, accentColor),
        colors = ButtonDefaults.outlinedButtonColors(
            contentColor = accentColor,
            containerColor = Color.Transparent
        ),
        contentPadding = PaddingValues(horizontal = 20.dp, vertical = 10.dp)
    ) {
        if (icon != null) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = accentColor,
                modifier = Modifier.size(18.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
        }
        Text(
            text = text,
            color = accentColor,
            fontWeight = FontWeight.SemiBold,
            fontSize = 14.sp
        )
    }
}

@Composable
fun FeatureBadge(
    label: String,
    modifier: Modifier = Modifier,
    accentColor: Color = ElectricCyan,
    icon: ImageVector? = null
) {
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(8.dp),
        color = accentColor.copy(alpha = 0.12f),
        border = BorderStroke(0.8.dp, accentColor.copy(alpha = 0.4f))
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            if (icon != null) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = accentColor,
                    modifier = Modifier.size(12.dp)
                )
                Spacer(modifier = Modifier.width(4.dp))
            }
            Text(
                text = label,
                color = accentColor,
                fontSize = 11.sp,
                fontWeight = FontWeight.SemiBold
            )
        }
    }
}

/**
 * Visual timeline representation showing cuts, keyframes, and waveform bars.
 */
@Composable
fun TimelineMiniPreview(
    cutsCount: Int,
    keyframesCount: Int,
    durationSeconds: Float,
    modifier: Modifier = Modifier
) {
    Canvas(
        modifier = modifier
            .fillMaxWidth()
            .height(36.dp)
            .clip(RoundedCornerShape(8.dp))
            .background(SurfaceNavyElevated)
    ) {
        val w = size.width
        val h = size.height

        // Background tracks (Video track & Audio track split)
        val videoTrackH = h * 0.6f
        val audioTrackH = h * 0.4f

        // Draw clip segments
        val segments = (cutsCount.coerceIn(3, 10))
        val segW = w / segments.toFloat()

        for (i in 0 until segments) {
            val color = if (i % 2 == 0) ClipTrackCyan.copy(alpha = 0.8f) else ClipTrackPurple.copy(alpha = 0.8f)
            drawRect(
                color = color,
                topLeft = Offset(i * segW + 1f, 2f),
                size = Size(segW - 2f, videoTrackH - 4f)
            )

            // Split line
            if (i > 0) {
                drawLine(
                    color = Color.White.copy(alpha = 0.7f),
                    start = Offset(i * segW, 0f),
                    end = Offset(i * segW, videoTrackH),
                    strokeWidth = 2f
                )
            }
        }

        // Draw keyframe diamonds on top track
        val kfCount = keyframesCount.coerceIn(2, 6)
        val kfSpacing = w / (kfCount + 1)
        for (k in 1..kfCount) {
            val kx = k * kfSpacing
            val ky = videoTrackH / 2f
            drawCircle(
                color = KeyframeDiamondGold,
                radius = 3.5f,
                center = Offset(kx, ky)
            )
        }

        // Draw audio waveform bars on bottom track
        val audioTop = videoTrackH
        val barCount = 36
        val barW = w / (barCount * 1.5f)
        for (b in 0 until barCount) {
            val bx = b * (w / barCount)
            val barAmp = ((kotlin.math.sin(b * 0.6) + 1.2) / 2.2).toFloat() * (audioTrackH - 4f)
            drawLine(
                color = AudioTrackGreen.copy(alpha = 0.7f),
                start = Offset(bx, audioTop + audioTrackH - 2f),
                end = Offset(bx, audioTop + audioTrackH - 2f - barAmp.coerceAtLeast(4f)),
                strokeWidth = barW.coerceAtLeast(2f)
            )
        }
    }
}

/**
 * Sleek bottom navigation bar adhering strictly to M3 & edge-to-edge guidelines.
 */
@Composable
fun EditCopyBottomNavBar(
    currentScreen: AppScreen,
    onNavigate: (AppScreen) -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier
            .fillMaxWidth()
            .navigationBarsPadding(),
        color = MidnightNavy,
        border = BorderStroke(1.dp, CardBorderSubtle),
        tonalElevation = 8.dp
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp, vertical = 6.dp),
            horizontalArrangement = Arrangement.SpaceAround,
            verticalAlignment = Alignment.CenterVertically
        ) {
            val navItems = listOf(
                NavItemData(
                    label = "Home",
                    iconSelected = Icons.Filled.Home,
                    iconUnselected = Icons.Outlined.Home,
                    screen = AppScreen.Home,
                    testTag = "nav_home"
                ),
                NavItemData(
                    label = "Templates",
                    iconSelected = Icons.Filled.Layers,
                    iconUnselected = Icons.Outlined.Layers,
                    screen = AppScreen.TemplateLibrary,
                    testTag = "nav_templates"
                ),
                NavItemData(
                    label = "Create",
                    iconSelected = Icons.Filled.Add,
                    iconUnselected = Icons.Filled.Add,
                    screen = AppScreen.TimelineEditor(null),
                    testTag = "nav_create",
                    isCenterAction = true
                ),
                NavItemData(
                    label = "Library",
                    iconSelected = Icons.Filled.VideoLibrary,
                    iconUnselected = Icons.Outlined.VideoLibrary,
                    screen = AppScreen.Library,
                    testTag = "nav_library"
                ),
                NavItemData(
                    label = "Profile",
                    iconSelected = Icons.Filled.Person,
                    iconUnselected = Icons.Outlined.Person,
                    screen = AppScreen.ProfileSettings,
                    testTag = "nav_profile"
                )
            )

            navItems.forEach { item ->
                val isSelected = when (currentScreen) {
                    is AppScreen.Home -> item.screen is AppScreen.Home
                    is AppScreen.TemplateLibrary, is AppScreen.TemplateDetails -> item.screen is AppScreen.TemplateLibrary
                    is AppScreen.TimelineEditor -> item.screen is AppScreen.TimelineEditor
                    is AppScreen.Library -> item.screen is AppScreen.Library
                    is AppScreen.ProfileSettings -> item.screen is AppScreen.ProfileSettings
                    else -> false
                }

                if (item.isCenterAction) {
                    // Elevated Center Action Button with gradient
                    Box(
                        modifier = Modifier
                            .size(50.dp)
                            .clip(CircleShape)
                            .background(
                                Brush.linearGradient(
                                    colors = listOf(ElectricCyan, NeonViolet, ElectricMagenta)
                                )
                            )
                            .clickable(
                                interactionSource = remember { MutableInteractionSource() },
                                indication = ripple(color = Color.White),
                                onClick = { onNavigate(item.screen) }
                            )
                            .testTag(item.testTag),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Add,
                            contentDescription = "Create Template",
                            tint = DeepBlack,
                            modifier = Modifier.size(28.dp)
                        )
                    }
                } else {
                    Column(
                        modifier = Modifier
                            .clip(RoundedCornerShape(12.dp))
                            .clickable(
                                interactionSource = remember { MutableInteractionSource() },
                                indication = ripple(color = ElectricCyan),
                                onClick = { onNavigate(item.screen) }
                            )
                            .padding(horizontal = 12.dp, vertical = 6.dp)
                            .testTag(item.testTag),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(
                            imageVector = if (isSelected) item.iconSelected else item.iconUnselected,
                            contentDescription = item.label,
                            tint = if (isSelected) ElectricCyan else TextMuted,
                            modifier = Modifier.size(22.dp)
                        )
                        Spacer(modifier = Modifier.height(3.dp))
                        Text(
                            text = item.label,
                            color = if (isSelected) ElectricCyan else TextMuted,
                            fontSize = 11.sp,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                        )
                    }
                }
            }
        }
    }
}

private data class NavItemData(
    val label: String,
    val iconSelected: ImageVector,
    val iconUnselected: ImageVector,
    val screen: AppScreen,
    val testTag: String,
    val isCenterAction: Boolean = false
)
