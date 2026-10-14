package com.example.ui.components

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.RoundRect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.example.ui.theme.ElectricCyan
import com.example.ui.theme.ElectricMagenta
import com.example.ui.theme.NeonPurple
import com.example.ui.theme.NeonViolet

/**
 * EditCopy Signature Brand Icon:
 * Concept combining:
 * 1. Duplicated Layer stack (the "Copy Forever" aspect)
 * 2. Filmstrip perforations & frame border (the "Video Edit" aspect)
 * 3. Forward Play Triangle in core focus (the "Motion / Playback" aspect)
 * Finished with Electric Cyan -> Neon Purple -> Magenta glow gradient.
 */
@Composable
fun EditCopyLogo(
    modifier: Modifier = Modifier,
    size: Dp = 64.dp,
    animated: Boolean = true
) {
    val infiniteTransition = rememberInfiniteTransition(label = "logo_anim")
    val pulseScale by if (animated) {
        infiniteTransition.animateFloat(
            initialValue = 0.98f,
            targetValue = 1.03f,
            animationSpec = infiniteRepeatable(
                animation = tween(2200, easing = FastOutSlowInEasing),
                repeatMode = RepeatMode.Reverse
            ),
            label = "pulse_scale"
        )
    } else {
        rememberInfiniteTransition(label = "static").animateFloat(
            initialValue = 1f,
            targetValue = 1f,
            animationSpec = infiniteRepeatable(tween(1000)),
            label = "static_scale"
        )
    }

    Box(
        modifier = modifier.size(size),
        contentAlignment = Alignment.Center
    ) {
        Canvas(modifier = Modifier.size(size * pulseScale)) {
            val w = this.size.width
            val h = this.size.height

            // Gradient brush for the primary visual system
            val brandGradient = Brush.linearGradient(
                colors = listOf(ElectricCyan, NeonViolet, ElectricMagenta),
                start = Offset(0f, 0f),
                end = Offset(w, h)
            )

            val backLayerGradient = Brush.linearGradient(
                colors = listOf(NeonPurple.copy(alpha = 0.5f), ElectricCyan.copy(alpha = 0.3f)),
                start = Offset(0f, 0f),
                end = Offset(w, h)
            )

            // Layer 1 (Back duplicated layer offset)
            val layerOffset = w * 0.10f
            val cardW = w * 0.72f
            val cardH = h * 0.76f
            val cornerR = CornerRadius(w * 0.12f, w * 0.12f)

            // Back layer outline
            drawRoundRect(
                brush = backLayerGradient,
                topLeft = Offset(layerOffset * 1.6f, layerOffset * 0.4f),
                size = Size(cardW, cardH),
                cornerRadius = cornerR,
                style = Stroke(width = w * 0.04f)
            )

            // Layer 2 (Front filmstrip card)
            val frontX = layerOffset * 0.4f
            val frontY = layerOffset * 1.2f

            // Fill with dark glass
            drawRoundRect(
                color = Color(0xEE0B101E),
                topLeft = Offset(frontX, frontY),
                size = Size(cardW, cardH),
                cornerRadius = cornerR
            )

            // Front layer glowing border
            drawRoundRect(
                brush = brandGradient,
                topLeft = Offset(frontX, frontY),
                size = Size(cardW, cardH),
                cornerRadius = cornerR,
                style = Stroke(width = w * 0.05f)
            )

            // Filmstrip perforations on left and right borders of front card
            val perfSize = w * 0.045f
            val perfRadius = CornerRadius(perfSize * 0.25f, perfSize * 0.25f)
            val perfSpacing = cardH / 5.2f

            for (i in 1..4) {
                val py = frontY + (i * perfSpacing) - (perfSize / 2f)
                // Left perforation
                drawRoundRect(
                    color = ElectricCyan.copy(alpha = 0.8f),
                    topLeft = Offset(frontX + (w * 0.04f), py),
                    size = Size(perfSize, perfSize * 0.75f),
                    cornerRadius = perfRadius
                )
                // Right perforation
                drawRoundRect(
                    color = NeonViolet.copy(alpha = 0.8f),
                    topLeft = Offset(frontX + cardW - (w * 0.04f) - perfSize, py),
                    size = Size(perfSize, perfSize * 0.75f),
                    cornerRadius = perfRadius
                )
            }

            // Central Play Triangle with rounded vertex feel
            val playPath = Path().apply {
                val cx = frontX + (cardW / 2f)
                val cy = frontY + (cardH / 2f)
                val triSize = w * 0.22f

                moveTo(cx - (triSize * 0.35f), cy - (triSize * 0.5f))
                lineTo(cx + (triSize * 0.55f), cy)
                lineTo(cx - (triSize * 0.35f), cy + (triSize * 0.5f))
                close()
            }

            // Fill Play Button
            drawPath(
                path = playPath,
                brush = brandGradient
            )
        }
    }
}
