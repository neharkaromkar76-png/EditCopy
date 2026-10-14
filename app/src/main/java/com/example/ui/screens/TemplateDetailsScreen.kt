package com.example.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.outlined.StarOutline
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.VideoTemplate
import com.example.ui.components.FeatureBadge
import com.example.ui.components.GlassCard
import com.example.ui.components.NeonButton
import com.example.ui.components.NeonOutlinedButton
import com.example.ui.components.TimelineMiniPreview
import com.example.ui.theme.AmberWarning
import com.example.ui.theme.CardBorderSubtle
import com.example.ui.theme.ClipTrackCyan
import com.example.ui.theme.ClipTrackPurple
import com.example.ui.theme.DeepBlack
import com.example.ui.theme.ElectricCyan
import com.example.ui.theme.ElectricMagenta
import com.example.ui.theme.GlassBorder
import com.example.ui.theme.NeonGreen
import com.example.ui.theme.NeonViolet
import com.example.ui.theme.SurfaceNavy
import com.example.ui.theme.SurfaceNavyElevated
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import kotlinx.coroutines.delay

@Composable
fun TemplateDetailsScreen(
    template: VideoTemplate,
    onBack: () -> Unit,
    onApplyToVideo: (String) -> Unit,
    onDuplicate: (VideoTemplate) -> Unit,
    onToggleFavorite: (String, Boolean) -> Unit,
    modifier: Modifier = Modifier
) {
    BackHandler(onBack = onBack)

    var isPlaying by remember { mutableStateOf(false) }
    var playbackProgress by remember { mutableFloatStateOf(0f) }

    // Simulated playback loop for previewing the editing structure
    LaunchedEffect(isPlaying) {
        if (isPlaying) {
            val totalSteps = 100
            val delayPerStep = (template.durationSeconds * 1000f / totalSteps).toLong()
            while (isPlaying) {
                if (playbackProgress >= 1f) {
                    playbackProgress = 0f
                }
                delay(delayPerStep)
                playbackProgress = (playbackProgress + 0.01f).coerceAtMost(1f)
                if (playbackProgress >= 1f) {
                    isPlaying = false
                }
            }
        }
    }

    val animatedProgress by animateFloatAsState(
        targetValue = playbackProgress,
        animationSpec = tween(durationMillis = 80, easing = LinearEasing),
        label = "progress"
    )

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(DeepBlack)
            .statusBarsPadding()
            .navigationBarsPadding()
            .testTag("template_details_screen")
    ) {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(bottom = 110.dp)
        ) {
            // Top Navigation Bar
            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 10.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(onClick = onBack, modifier = Modifier.testTag("details_back_button")) {
                        Icon(imageVector = Icons.Filled.ArrowBack, contentDescription = "Back", tint = TextPrimary)
                    }

                    Text(
                        text = "Template Details",
                        fontWeight = FontWeight.Bold,
                        fontSize = 17.sp,
                        color = TextPrimary
                    )

                    IconButton(
                        onClick = { onToggleFavorite(template.id, template.isFavorite) },
                        modifier = Modifier.testTag("details_favorite_button")
                    ) {
                        Icon(
                            imageVector = if (template.isFavorite) Icons.Filled.Star else Icons.Outlined.StarOutline,
                            contentDescription = "Favorite",
                            tint = if (template.isFavorite) AmberWarning else TextSecondary
                        )
                    }
                }
            }

            // Large Video / Canvas Preview Area
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp, vertical = 8.dp)
                ) {
                    Surface(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(260.dp)
                            .clip(RoundedCornerShape(20.dp)),
                        color = SurfaceNavy,
                        border = BorderStroke(1.dp, GlassBorder)
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .background(
                                    Brush.verticalGradient(
                                        listOf(
                                            Color(template.primaryAccentColorHex).copy(alpha = 0.25f),
                                            Color(template.secondaryAccentColorHex).copy(alpha = 0.15f),
                                            DeepBlack
                                        )
                                    )
                                )
                        ) {
                            // Video Preview Simulation Canvas
                            Column(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .padding(16.dp),
                                verticalArrangement = Arrangement.SpaceBetween,
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                // Top status bar in preview
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Surface(
                                        shape = RoundedCornerShape(6.dp),
                                        color = DeepBlack.copy(alpha = 0.7f)
                                    ) {
                                        Text(
                                            text = "${template.aspectRatio} • ${template.bpm} BPM",
                                            color = ElectricCyan,
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.Bold,
                                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                        )
                                    }

                                    Surface(
                                        shape = RoundedCornerShape(6.dp),
                                        color = if (isPlaying) NeonGreen.copy(alpha = 0.2f) else DeepBlack.copy(alpha = 0.7f)
                                    ) {
                                        Text(
                                            text = if (isPlaying) "SIMULATING CUTS" else "PREVIEW READY",
                                            color = if (isPlaying) NeonGreen else TextSecondary,
                                            fontSize = 10.sp,
                                            fontWeight = FontWeight.Bold,
                                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                        )
                                    }
                                }

                                // Center Play / Pause Big Button
                                Box(
                                    modifier = Modifier
                                        .size(64.dp)
                                        .clip(CircleShape)
                                        .background(ElectricCyan.copy(alpha = 0.3f))
                                        .clickable { isPlaying = !isPlaying }
                                        .testTag("preview_play_toggle"),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = if (isPlaying) Icons.Filled.Pause else Icons.Filled.PlayArrow,
                                        contentDescription = if (isPlaying) "Pause" else "Play",
                                        tint = ElectricCyan,
                                        modifier = Modifier.size(36.dp)
                                    )
                                }

                                // Bottom scrub & mini timeline preview
                                Column(modifier = Modifier.fillMaxWidth()) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        val currentTime = template.durationSeconds * animatedProgress
                                        Text(
                                            text = String.format("00:%04.1f", currentTime),
                                            color = TextPrimary,
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.Medium
                                        )
                                        Text(
                                            text = String.format("00:%04.1f", template.durationSeconds),
                                            color = TextMuted,
                                            fontSize = 11.sp
                                        )
                                    }

                                    Spacer(modifier = Modifier.height(4.dp))

                                    LinearProgressIndicator(
                                        progress = { animatedProgress },
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .height(4.dp)
                                            .clip(RoundedCornerShape(2.dp)),
                                        color = ElectricCyan,
                                        trackColor = SurfaceNavyElevated
                                    )

                                    Spacer(modifier = Modifier.height(6.dp))

                                    TimelineMiniPreview(
                                        cutsCount = template.cutsCount,
                                        keyframesCount = template.keyframesCount,
                                        durationSeconds = template.durationSeconds,
                                        modifier = Modifier.height(20.dp)
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // Title, Description, and Creator
            item {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp, vertical = 8.dp)
                ) {
                    Text(
                        text = template.title,
                        fontSize = 24.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = TextPrimary,
                        letterSpacing = (-0.4).sp
                    )

                    Spacer(modifier = Modifier.height(4.dp))

                    Text(
                        text = "Created by ${template.creator} • ${template.category.displayName}",
                        fontSize = 13.sp,
                        color = TextSecondary
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    Text(
                        text = template.description,
                        fontSize = 14.sp,
                        color = TextSecondary,
                        lineHeight = 20.sp
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    // Feature Tags Row
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        FeatureBadge(label = "${template.cutsCount} Cuts", accentColor = ElectricCyan)
                        FeatureBadge(label = "${template.keyframesCount} Keyframes", accentColor = NeonViolet)
                        FeatureBadge(label = "${template.zoomCount} Zoom Ramps", accentColor = ElectricMagenta)
                        if (template.hasBeatSync) {
                            FeatureBadge(label = "${template.bpm} BPM Beats", accentColor = NeonGreen)
                        }
                    }
                }
            }

            // Timeline Structure Breakdown
            item {
                Spacer(modifier = Modifier.height(12.dp))
                Text(
                    text = "Timeline Segments & Clip Slots",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextPrimary,
                    modifier = Modifier.padding(horizontal = 20.dp, vertical = 8.dp)
                )
            }

            // List of Clip Slots
            items(template.clipSlots.size) { index ->
                val slot = template.clipSlots[index]
                GlassCard(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp, vertical = 4.dp),
                    shapeRadius = 12.dp,
                    backgroundColor = SurfaceNavy.copy(alpha = 0.8f)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(14.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Surface(
                                shape = CircleShape,
                                color = if (index % 2 == 0) ClipTrackCyan.copy(alpha = 0.2f) else ClipTrackPurple.copy(alpha = 0.2f),
                                modifier = Modifier.size(32.dp)
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Text(
                                        text = "${slot.slotIndex}",
                                        color = if (index % 2 == 0) ClipTrackCyan else ClipTrackPurple,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 12.sp
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.width(12.dp))

                            Column {
                                Text(
                                    text = slot.label,
                                    color = TextPrimary,
                                    fontWeight = FontWeight.SemiBold,
                                    fontSize = 14.sp
                                )
                                Text(
                                    text = "Duration: ${String.format("%.1f", slot.durationSeconds)}s • Speed: ${slot.speedFactor}x",
                                    color = TextSecondary,
                                    fontSize = 11.sp
                                )
                            }
                        }

                        if (slot.keyframeEffect != null) {
                            Surface(
                                shape = RoundedCornerShape(6.dp),
                                color = NeonViolet.copy(alpha = 0.15f)
                            ) {
                                Text(
                                    text = slot.keyframeEffect,
                                    color = NeonViolet,
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Medium,
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp)
                                )
                            }
                        }
                    }
                }
            }

            // Transparent Feature Compatibility Report
            item {
                Spacer(modifier = Modifier.height(16.dp))
                GlassCard(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp),
                    shapeRadius = 14.dp,
                    borderGlowColor = AmberWarning.copy(alpha = 0.3f),
                    backgroundColor = SurfaceNavy.copy(alpha = 0.6f)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Filled.Info,
                                contentDescription = null,
                                tint = AmberWarning,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Compatibility & Engine Status",
                                color = TextPrimary,
                                fontWeight = FontWeight.Bold,
                                fontSize = 13.sp
                            )
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        Text(
                            text = "• Structural Cut & Slot Timing: Fully functional in EditCopy engine.\n" +
                                    "• Keyframe & Zoom Mapping: Supported in non-destructive EDL export.\n" +
                                    "• Direct VN Project Binary Modification: Laboratory testing active; unsupported binary overrides are never simulated.",
                            color = TextSecondary,
                            fontSize = 12.sp,
                            lineHeight = 17.sp
                        )
                    }
                }
            }
        }

        // Bottom Fixed Action Bar
        Surface(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth(),
            color = DeepBlack.copy(alpha = 0.95f),
            border = BorderStroke(1.dp, CardBorderSubtle)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 12.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                NeonOutlinedButton(
                    text = "Duplicate",
                    onClick = { onDuplicate(template) },
                    icon = Icons.Filled.ContentCopy,
                    accentColor = NeonViolet,
                    modifier = Modifier.weight(0.8f),
                    testTag = "details_duplicate_button"
                )

                NeonButton(
                    text = "Apply to My Video",
                    onClick = { onApplyToVideo(template.id) },
                    modifier = Modifier.weight(1.2f),
                    testTag = "details_apply_button"
                )
            }
        }
    }
}
