package com.example.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.horizontalScroll
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.ContentCut
import androidx.compose.material.icons.filled.Diamond
import androidx.compose.material.icons.filled.FastForward
import androidx.compose.material.icons.filled.FastRewind
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Redo
import androidx.compose.material.icons.filled.Save
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material.icons.filled.Undo
import androidx.compose.material.icons.filled.ZoomIn
import androidx.compose.material.icons.filled.ZoomOut
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.ClipSlot
import com.example.data.model.TemplateCategory
import com.example.data.model.VideoTemplate
import com.example.ui.components.FeatureBadge
import com.example.ui.components.GlassCard
import com.example.ui.components.NeonButton
import com.example.ui.theme.AudioTrackGreen
import com.example.ui.theme.CardBorderSubtle
import com.example.ui.theme.ClipTrackBlue
import com.example.ui.theme.ClipTrackCyan
import com.example.ui.theme.ClipTrackPink
import com.example.ui.theme.ClipTrackPurple
import com.example.ui.theme.DeepBlack
import com.example.ui.theme.ElectricCyan
import com.example.ui.theme.ElectricMagenta
import com.example.ui.theme.GlassBorder
import com.example.ui.theme.KeyframeDiamondGold
import com.example.ui.theme.NeonGreen
import com.example.ui.theme.NeonViolet
import com.example.ui.theme.SurfaceNavy
import com.example.ui.theme.SurfaceNavyElevated
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import kotlinx.coroutines.delay
import java.util.UUID

data class EditableClip(
    val id: String = UUID.randomUUID().toString(),
    var name: String,
    var durationSec: Float,
    var color: Color,
    var speed: Float = 1.0f,
    var keyframes: MutableList<Float> = mutableListOf()
)

@Composable
fun TimelineEditorScreen(
    templateId: String?,
    existingTemplate: VideoTemplate?,
    onBack: () -> Unit,
    onSaveTemplate: (VideoTemplate) -> Unit,
    modifier: Modifier = Modifier
) {
    BackHandler(onBack = onBack)

    var templateTitle by remember {
        mutableStateOf(existingTemplate?.title ?: "My Custom Template")
    }
    var showSaveDialog by remember { mutableStateOf(false) }

    // Clips list (Interactive!)
    val clips = remember {
        mutableStateListOf(
            EditableClip(name = "Hook Shot", durationSec = 1.8f, color = ClipTrackCyan, keyframes = mutableListOf(0.5f, 1.2f)),
            EditableClip(name = "Build Up", durationSec = 2.4f, color = ClipTrackPurple, speed = 1.2f, keyframes = mutableListOf(1.0f)),
            EditableClip(name = "Drop Zoom", durationSec = 1.5f, color = ClipTrackPink, speed = 2.0f, keyframes = mutableListOf(0.2f, 0.8f)),
            EditableClip(name = "Outro", durationSec = 3.2f, color = ClipTrackBlue, keyframes = mutableListOf(1.5f))
        )
    }

    var selectedClipIndex by remember { mutableIntStateOf(0) }
    var playheadPositionSec by remember { mutableFloatStateOf(0f) }
    var isPlaying by remember { mutableStateOf(false) }
    var zoomScale by remember { mutableFloatStateOf(1.0f) } // Timeline zoom
    var undoStackCount by remember { mutableIntStateOf(3) }
    var redoStackCount by remember { mutableIntStateOf(0) }

    val totalDurationSec = clips.sumOf { it.durationSec.toDouble() }.toFloat()

    // Playback loop
    LaunchedEffect(isPlaying) {
        if (isPlaying) {
            while (isPlaying) {
                delay(50)
                playheadPositionSec += 0.05f
                if (playheadPositionSec >= totalDurationSec) {
                    playheadPositionSec = 0f
                }
            }
        }
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(DeepBlack)
            .statusBarsPadding()
            .navigationBarsPadding()
            .testTag("timeline_editor_screen")
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            // Top App Bar: Back, Title, Undo/Redo, Save
            TimelineTopBar(
                title = templateTitle,
                undoAvailable = undoStackCount > 0,
                redoAvailable = redoStackCount > 0,
                onBack = onBack,
                onUndo = {
                    if (undoStackCount > 0) {
                        undoStackCount--
                        redoStackCount++
                    }
                },
                onRedo = {
                    if (redoStackCount > 0) {
                        redoStackCount--
                        undoStackCount++
                    }
                },
                onSave = { showSaveDialog = true }
            )

            // Video Preview Monitor (Upper half)
            VideoMonitorView(
                currentTimeSec = playheadPositionSec,
                totalTimeSec = totalDurationSec,
                isPlaying = isPlaying,
                onTogglePlay = { isPlaying = !isPlaying },
                selectedClip = clips.getOrNull(selectedClipIndex)
            )

            // Pro Toolbar (Split, Add Keyframe, Speed, Zoom In/Out)
            TimelineToolbar(
                zoomScale = zoomScale,
                onSplit = {
                    val current = clips.getOrNull(selectedClipIndex)
                    if (current != null && current.durationSec > 0.8f) {
                        val half = current.durationSec / 2f
                        current.durationSec = half
                        clips.add(
                            selectedClipIndex + 1,
                            EditableClip(
                                name = "${current.name} (Split)",
                                durationSec = half,
                                color = ClipTrackCyan,
                                speed = current.speed
                            )
                        )
                        undoStackCount++
                    }
                },
                onAddKeyframe = {
                    val current = clips.getOrNull(selectedClipIndex)
                    current?.keyframes?.add(current.durationSec / 2f)
                    undoStackCount++
                },
                onZoomIn = { zoomScale = (zoomScale + 0.25f).coerceAtMost(2.5f) },
                onZoomOut = { zoomScale = (zoomScale - 0.25f).coerceAtLeast(0.5f) }
            )

            // Multi-Track Timeline Canvas (Lower half)
            TimelineMultiTrackView(
                clips = clips,
                selectedClipIndex = selectedClipIndex,
                onSelectClip = { selectedClipIndex = it },
                playheadSec = playheadPositionSec,
                totalDurationSec = totalDurationSec,
                zoomScale = zoomScale,
                onScrub = { playheadPositionSec = it }
            )

            // Selected Clip Inspector Panel
            val currentClip = clips.getOrNull(selectedClipIndex)
            if (currentClip != null) {
                ClipPropertiesBar(
                    clip = currentClip,
                    onUpdateSpeed = { currentClip.speed = it }
                )
            }
        }

        // Save Template Dialog
        if (showSaveDialog) {
            SaveTemplateModal(
                initialName = templateTitle,
                totalCuts = clips.size,
                totalDuration = totalDurationSec,
                onDismiss = { showSaveDialog = false },
                onConfirmSave = { name, category ->
                    templateTitle = name
                    showSaveDialog = false
                    val created = VideoTemplate(
                        title = name,
                        description = "Custom editing blueprint with ${clips.size} cuts and keyframe curves.",
                        category = category,
                        durationSeconds = totalDurationSec,
                        cutsCount = clips.size,
                        keyframesCount = clips.sumOf { it.keyframes.size },
                        zoomCount = clips.count { it.speed > 1.2f },
                        isUserCreated = true,
                        clipSlots = clips.mapIndexed { idx, clp ->
                            ClipSlot(
                                slotIndex = idx + 1,
                                label = clp.name,
                                durationSeconds = clp.durationSec,
                                speedFactor = clp.speed
                            )
                        }
                    )
                    onSaveTemplate(created)
                }
            )
        }
    }
}

@Composable
private fun TimelineTopBar(
    title: String,
    undoAvailable: Boolean,
    redoAvailable: Boolean,
    onBack: () -> Unit,
    onUndo: () -> Unit,
    onRedo: () -> Unit,
    onSave: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 14.dp, vertical = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            IconButton(onClick = onBack, modifier = Modifier.testTag("timeline_back_button")) {
                Icon(imageVector = Icons.Filled.ArrowBack, contentDescription = "Back", tint = TextPrimary)
            }
            Spacer(modifier = Modifier.width(6.dp))
            Column {
                Text(
                    text = title,
                    color = TextPrimary,
                    fontWeight = FontWeight.Bold,
                    fontSize = 15.sp,
                    maxLines = 1
                )
                Text(
                    text = "Timeline Prototype Engine",
                    color = ElectricCyan,
                    fontSize = 11.sp
                )
            }
        }

        Row(verticalAlignment = Alignment.CenterVertically) {
            IconButton(onClick = onUndo, enabled = undoAvailable) {
                Icon(
                    imageVector = Icons.Filled.Undo,
                    contentDescription = "Undo",
                    tint = if (undoAvailable) TextPrimary else TextMuted
                )
            }

            IconButton(onClick = onRedo, enabled = redoAvailable) {
                Icon(
                    imageVector = Icons.Filled.Redo,
                    contentDescription = "Redo",
                    tint = if (redoAvailable) TextPrimary else TextMuted
                )
            }

            IconButton(onClick = onSave, modifier = Modifier.testTag("timeline_save_button")) {
                Icon(
                    imageVector = Icons.Filled.Save,
                    contentDescription = "Save Template",
                    tint = ElectricCyan
                )
            }
        }
    }
}

@Composable
private fun VideoMonitorView(
    currentTimeSec: Float,
    totalTimeSec: Float,
    isPlaying: Boolean,
    onTogglePlay: () -> Unit,
    selectedClip: EditableClip?
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .height(200.dp)
            .padding(horizontal = 16.dp, vertical = 4.dp)
            .clip(RoundedCornerShape(16.dp)),
        color = SurfaceNavy,
        border = BorderStroke(1.dp, GlassBorder)
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.radialGradient(
                        listOf(SurfaceNavyElevated, DeepBlack)
                    )
                )
        ) {
            // Center Play Icon & Active clip tag
            Column(
                modifier = Modifier.align(Alignment.Center),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Box(
                    modifier = Modifier
                        .size(52.dp)
                        .clip(CircleShape)
                        .background(ElectricCyan.copy(alpha = 0.3f))
                        .clickable(onClick = onTogglePlay)
                        .testTag("timeline_play_pause"),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = if (isPlaying) Icons.Filled.Pause else Icons.Filled.PlayArrow,
                        contentDescription = "Play/Pause",
                        tint = ElectricCyan,
                        modifier = Modifier.size(28.dp)
                    )
                }

                if (selectedClip != null) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Active: ${selectedClip.name} (${selectedClip.speed}x)",
                        color = TextSecondary,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium
                    )
                }
            }

            // Timecode Overlay in Bottom Right
            Surface(
                shape = RoundedCornerShape(6.dp),
                color = DeepBlack.copy(alpha = 0.8f),
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(10.dp)
            ) {
                Text(
                    text = "${String.format("00:%04.1f", currentTimeSec)} / ${String.format("00:%04.1f", totalTimeSec)}",
                    color = ElectricCyan,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                )
            }

            // Top Status Chip
            Surface(
                shape = RoundedCornerShape(6.dp),
                color = DeepBlack.copy(alpha = 0.7f),
                modifier = Modifier
                    .align(Alignment.TopStart)
                    .padding(10.dp)
            ) {
                Text(
                    text = "CANVAS MONITOR • 9:16",
                    color = TextMuted,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp)
                )
            }
        }
    }
}

@Composable
private fun TimelineToolbar(
    zoomScale: Float,
    onSplit: () -> Unit,
    onAddKeyframe: () -> Unit,
    onZoomIn: () -> Unit,
    onZoomOut: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 6.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            ToolbarButton(
                icon = Icons.Filled.ContentCut,
                label = "Split",
                accentColor = ElectricCyan,
                onClick = onSplit,
                testTag = "toolbar_split_button"
            )

            ToolbarButton(
                icon = Icons.Filled.Diamond,
                label = "Keyframe",
                accentColor = KeyframeDiamondGold,
                onClick = onAddKeyframe,
                testTag = "toolbar_keyframe_button"
            )
        }

        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            IconButton(onClick = onZoomOut, modifier = Modifier.size(34.dp)) {
                Icon(imageVector = Icons.Filled.ZoomOut, contentDescription = "Zoom Out", tint = TextSecondary, modifier = Modifier.size(18.dp))
            }
            Text(
                text = "${String.format("%.1f", zoomScale)}x",
                color = TextMuted,
                fontSize = 11.sp
            )
            IconButton(onClick = onZoomIn, modifier = Modifier.size(34.dp)) {
                Icon(imageVector = Icons.Filled.ZoomIn, contentDescription = "Zoom In", tint = TextSecondary, modifier = Modifier.size(18.dp))
            }
        }
    }
}

@Composable
private fun ToolbarButton(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    accentColor: Color,
    onClick: () -> Unit,
    testTag: String
) {
    Surface(
        shape = RoundedCornerShape(8.dp),
        color = SurfaceNavy,
        border = BorderStroke(1.dp, accentColor.copy(alpha = 0.4f)),
        modifier = Modifier
            .clip(RoundedCornerShape(8.dp))
            .clickable(onClick = onClick)
            .testTag(testTag)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(imageVector = icon, contentDescription = null, tint = accentColor, modifier = Modifier.size(14.dp))
            Spacer(modifier = Modifier.width(6.dp))
            Text(text = label, color = TextPrimary, fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
        }
    }
}

@Composable
private fun TimelineMultiTrackView(
    clips: List<EditableClip>,
    selectedClipIndex: Int,
    onSelectClip: (Int) -> Unit,
    playheadSec: Float,
    totalDurationSec: Float,
    zoomScale: Float,
    onScrub: (Float) -> Unit
) {
    val scrollState = rememberScrollState()

    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .height(180.dp)
            .padding(horizontal = 16.dp, vertical = 6.dp)
            .clip(RoundedCornerShape(14.dp)),
        color = SurfaceNavyElevated,
        border = BorderStroke(1.dp, CardBorderSubtle)
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .horizontalScroll(scrollState)
        ) {
            val pxPerSecond = 70f * zoomScale
            val timelineWidthDp = (totalDurationSec * pxPerSecond).coerceAtLeast(340f).dp

            Column(
                modifier = Modifier
                    .width(timelineWidthDp)
                    .fillMaxSize()
                    .padding(8.dp)
            ) {
                // Time Ruler Bar
                Canvas(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(20.dp)
                ) {
                    val w = size.width
                    val tickCount = totalDurationSec.toInt() + 1
                    for (t in 0..tickCount) {
                        val tx = t * pxPerSecond
                        drawLine(
                            color = TextMuted,
                            start = Offset(tx, 0f),
                            end = Offset(tx, if (t % 2 == 0) 14f else 8f),
                            strokeWidth = 1.5f
                        )
                    }
                }

                Spacer(modifier = Modifier.height(4.dp))

                // Video Track (Clips blocks)
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(64.dp)
                ) {
                    clips.forEachIndexed { index, clip ->
                        val isSelected = index == selectedClipIndex
                        val clipWidth = (clip.durationSec * pxPerSecond).dp

                        Surface(
                            modifier = Modifier
                                .width(clipWidth)
                                .height(64.dp)
                                .padding(end = 2.dp)
                                .clip(RoundedCornerShape(6.dp))
                                .clickable { onSelectClip(index) },
                            color = if (isSelected) clip.color.copy(alpha = 0.9f) else clip.color.copy(alpha = 0.5f),
                            border = if (isSelected) BorderStroke(2.dp, Color.White) else null
                        ) {
                            Column(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .padding(6.dp),
                                verticalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(
                                    text = clip.name,
                                    color = Color.White,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 11.sp,
                                    maxLines = 1
                                )

                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text(
                                        text = "${String.format("%.1f", clip.durationSec)}s",
                                        color = Color.White.copy(alpha = 0.8f),
                                        fontSize = 10.sp
                                    )
                                    if (clip.speed != 1.0f) {
                                        Text(
                                            text = "${clip.speed}x",
                                            color = KeyframeDiamondGold,
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 10.sp
                                        )
                                    }
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(6.dp))

                // Keyframe Lane
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(18.dp)
                        .background(SurfaceNavy.copy(alpha = 0.6f))
                ) {
                    Canvas(modifier = Modifier.fillMaxSize()) {
                        var accumulatedSec = 0f
                        clips.forEach { clip ->
                            clip.keyframes.forEach { kfSec ->
                                val kx = (accumulatedSec + kfSec) * pxPerSecond
                                drawCircle(
                                    color = KeyframeDiamondGold,
                                    radius = 4f,
                                    center = Offset(kx, size.height / 2f)
                                )
                            }
                            accumulatedSec += clip.durationSec
                        }
                    }
                }

                Spacer(modifier = Modifier.height(4.dp))

                // Audio Track with Waveform
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(30.dp)
                        .clip(RoundedCornerShape(4.dp))
                        .background(SurfaceNavy)
                ) {
                    Canvas(modifier = Modifier.fillMaxSize()) {
                        val barCount = (totalDurationSec * 16).toInt()
                        val barStep = size.width / barCount.coerceAtLeast(1)
                        for (i in 0 until barCount) {
                            val bx = i * barStep
                            val amp = (kotlin.math.sin(i * 0.8) + 1.2) * (size.height * 0.35f)
                            drawLine(
                                color = AudioTrackGreen.copy(alpha = 0.7f),
                                start = Offset(bx, size.height),
                                end = Offset(bx, (size.height - amp.toFloat()).coerceAtLeast(2f)),
                                strokeWidth = 2f
                            )
                        }
                    }
                }
            }

            // Playhead Vertical Line Scrub Overlay
            val playheadX = (playheadSec * pxPerSecond).dp
            Box(
                modifier = Modifier
                    .width(playheadX + 2.dp)
                    .fillMaxSize()
            ) {
                Box(
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .width(2.dp)
                        .fillMaxSize()
                        .background(ElectricCyan)
                )
            }
        }
    }
}

@Composable
private fun ClipPropertiesBar(
    clip: EditableClip,
    onUpdateSpeed: (Float) -> Unit
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 4.dp),
        color = SurfaceNavy,
        shape = RoundedCornerShape(10.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 14.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column {
                Text(
                    text = "${clip.name} Properties",
                    color = TextPrimary,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "Duration: ${String.format("%.1f", clip.durationSec)}s • Keyframes: ${clip.keyframes.size}",
                    color = TextSecondary,
                    fontSize = 11.sp
                )
            }

            Row(verticalAlignment = Alignment.CenterVertically) {
                Text("Speed: ", color = TextSecondary, fontSize = 11.sp)
                val speeds = listOf(0.5f, 1.0f, 1.5f, 2.0f)
                speeds.forEach { spd ->
                    val isSelected = clip.speed == spd
                    Surface(
                        shape = RoundedCornerShape(4.dp),
                        color = if (isSelected) ElectricCyan else SurfaceNavyElevated,
                        modifier = Modifier
                            .padding(horizontal = 3.dp)
                            .clickable { onUpdateSpeed(spd) }
                    ) {
                        Text(
                            text = "${spd}x",
                            color = if (isSelected) DeepBlack else TextPrimary,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(horizontal = 5.dp, vertical = 3.dp)
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun SaveTemplateModal(
    initialName: String,
    totalCuts: Int,
    totalDuration: Float,
    onDismiss: () -> Unit,
    onConfirmSave: (String, TemplateCategory) -> Unit
) {
    var name by remember { mutableStateOf(initialName) }
    var selectedCategory by remember { mutableStateOf(TemplateCategory.CUSTOM) }

    AlertDialog(
        onDismissRequest = onDismiss,
        confirmButton = {
            NeonButton(
                text = "Save Blueprint",
                onClick = { onConfirmSave(name, selectedCategory) },
                testTag = "confirm_save_template_button"
            )
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel", color = TextSecondary)
            }
        },
        containerColor = SurfaceNavyElevated,
        title = {
            Text("Save as Reusable Template", color = TextPrimary, fontWeight = FontWeight.Bold)
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text(
                    text = "Capture $totalCuts cuts and keyframe curves (${String.format("%.1f", totalDuration)}s total) into a reusable template.",
                    color = TextSecondary,
                    fontSize = 12.sp
                )

                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Template Name") },
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = TextPrimary,
                        unfocusedTextColor = TextPrimary,
                        focusedBorderColor = ElectricCyan,
                        unfocusedBorderColor = CardBorderSubtle,
                        cursorColor = ElectricCyan
                    )
                )
            }
        }
    )
}
