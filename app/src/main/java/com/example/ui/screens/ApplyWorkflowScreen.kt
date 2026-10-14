package com.example.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedContent
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
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.Movie
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Sync
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
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
import androidx.compose.runtime.mutableStateMapOf
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
import com.example.data.model.ClipSlot
import com.example.data.model.SourceVideoMeta
import com.example.data.model.VideoTemplate
import com.example.data.repository.TemplateRepository
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
fun ApplyWorkflowScreen(
    initialTemplate: VideoTemplate,
    availableTemplates: List<VideoTemplate>,
    repository: TemplateRepository,
    initialVideo: SourceVideoMeta? = null,
    onBack: () -> Unit,
    onComplete: (VideoTemplate) -> Unit,
    modifier: Modifier = Modifier
) {
    var currentStep by remember { mutableIntStateOf(if (initialVideo != null) 3 else 1) }
    var selectedTemplate by remember { mutableStateOf(initialTemplate) }
    var selectedVideo by remember { mutableStateOf(initialVideo ?: repository.getSampleSourceVideos().first()) }

    // Slot trim offsets for Step 3
    val slotTrimMap = remember {
        mutableStateMapOf<Int, Float>().apply {
            selectedTemplate.clipSlots.forEach { slot ->
                put(slot.slotIndex, 0f)
            }
        }
    }

    var isPreviewPlaying by remember { mutableStateOf(false) }
    var previewProgress by remember { mutableFloatStateOf(0f) }
    var exportStatusMessage by remember { mutableStateOf<String?>(null) }
    var isExporting by remember { mutableStateOf(false) }

    BackHandler {
        if (currentStep > 1) {
            currentStep--
        } else {
            onBack()
        }
    }

    // Preview playback loop
    LaunchedEffect(isPreviewPlaying) {
        if (isPreviewPlaying) {
            while (isPreviewPlaying) {
                if (previewProgress >= 1f) previewProgress = 0f
                delay(80)
                previewProgress = (previewProgress + 0.02f).coerceAtMost(1f)
                if (previewProgress >= 1f) isPreviewPlaying = false
            }
        }
    }

    val animatedProgress by animateFloatAsState(
        targetValue = previewProgress,
        animationSpec = tween(durationMillis = 60, easing = LinearEasing),
        label = "anim_progress"
    )

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(DeepBlack)
            .statusBarsPadding()
            .navigationBarsPadding()
            .testTag("apply_workflow_screen")
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            // Top Navigation & Step Indicator
            WorkflowHeader(
                currentStep = currentStep,
                onBack = {
                    if (currentStep > 1) currentStep-- else onBack()
                }
            )

            // Step Progress Bar
            WorkflowStepperBar(currentStep = currentStep)

            // Step Content
            Box(modifier = Modifier.weight(1f)) {
                when (currentStep) {
                    1 -> Step1ChooseTemplate(
                        templates = availableTemplates,
                        selectedTemplate = selectedTemplate,
                        onSelectTemplate = { selectedTemplate = it }
                    )
                    2 -> Step2SelectVideo(
                        repository = repository,
                        selectedVideo = selectedVideo,
                        onSelectVideo = { selectedVideo = it }
                    )
                    3 -> Step3MatchSlots(
                        template = selectedTemplate,
                        video = selectedVideo,
                        trimMap = slotTrimMap,
                        onUpdateTrim = { index, trim -> slotTrimMap[index] = trim }
                    )
                    4 -> Step4PreviewResult(
                        template = selectedTemplate,
                        video = selectedVideo,
                        isPlaying = isPreviewPlaying,
                        progress = animatedProgress,
                        onTogglePlay = { isPreviewPlaying = !isPreviewPlaying }
                    )
                    5 -> Step5ExportPackage(
                        template = selectedTemplate,
                        video = selectedVideo,
                        isExporting = isExporting,
                        statusMessage = exportStatusMessage,
                        onTriggerExport = {
                            isExporting = true
                            exportStatusMessage = "Compiling Non-Destructive EDL & Keyframe Map..."
                        },
                        onFinish = { onComplete(selectedTemplate) }
                    )
                }
            }

            // Bottom Navigation Footer
            WorkflowFooter(
                currentStep = currentStep,
                canProceed = when (currentStep) {
                    1 -> true
                    2 -> true
                    3 -> true
                    4 -> true
                    5 -> true
                    else -> false
                },
                onNext = {
                    if (currentStep < 5) currentStep++
                },
                onPrev = {
                    if (currentStep > 1) currentStep--
                }
            )
        }
    }
}

@Composable
private fun WorkflowHeader(
    currentStep: Int,
    onBack: () -> Unit
) {
    val stepTitle = when (currentStep) {
        1 -> "Step 1: Choose Template"
        2 -> "Step 2: Select Source Video"
        3 -> "Step 3: Match Clip Segments"
        4 -> "Step 4: Preview Result"
        5 -> "Step 5: Export / Save Package"
        else -> ""
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        IconButton(onClick = onBack, modifier = Modifier.testTag("workflow_back_button")) {
            Icon(imageVector = Icons.Filled.ArrowBack, contentDescription = "Back", tint = TextPrimary)
        }

        Spacer(modifier = Modifier.width(8.dp))

        Column {
            Text(
                text = "Apply Editing Template",
                fontSize = 13.sp,
                color = ElectricCyan,
                fontWeight = FontWeight.SemiBold
            )
            Text(
                text = stepTitle,
                fontSize = 17.sp,
                fontWeight = FontWeight.Bold,
                color = TextPrimary
            )
        }
    }
}

@Composable
private fun WorkflowStepperBar(currentStep: Int) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp, vertical = 8.dp),
        horizontalArrangement = Arrangement.spacedBy(6.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        for (i in 1..5) {
            val isCompleted = i < currentStep
            val isCurrent = i == currentStep

            Box(
                modifier = Modifier
                    .weight(1f)
                    .height(4.dp)
                    .clip(RoundedCornerShape(2.dp))
                    .background(
                        when {
                            isCompleted -> NeonGreen
                            isCurrent -> ElectricCyan
                            else -> CardBorderSubtle
                        }
                    )
            )
        }
    }
}

@Composable
private fun Step1ChooseTemplate(
    templates: List<VideoTemplate>,
    selectedTemplate: VideoTemplate,
    onSelectTemplate: (VideoTemplate) -> Unit
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(horizontal = 20.dp, vertical = 12.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            Text(
                text = "Choose the structure, speed ramps, and cut pacing to transfer:",
                color = TextSecondary,
                fontSize = 13.sp
            )
        }

        items(templates, key = { it.id }) { tpl ->
            val isSelected = tpl.id == selectedTemplate.id

            GlassCard(
                modifier = Modifier.fillMaxWidth(),
                shapeRadius = 16.dp,
                borderGlowColor = if (isSelected) ElectricCyan else CardBorderSubtle,
                backgroundColor = if (isSelected) SurfaceNavyElevated else SurfaceNavy.copy(alpha = 0.7f),
                onClick = { onSelectTemplate(tpl) }
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(14.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = tpl.title,
                                color = TextPrimary,
                                fontWeight = FontWeight.Bold,
                                fontSize = 15.sp
                            )
                            if (isSelected) {
                                Spacer(modifier = Modifier.width(8.dp))
                                Surface(
                                    shape = RoundedCornerShape(4.dp),
                                    color = ElectricCyan.copy(alpha = 0.2f)
                                ) {
                                    Text(
                                        text = "SELECTED",
                                        color = ElectricCyan,
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold,
                                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(4.dp))

                        Text(
                            text = "${tpl.aspectRatio} • ${String.format("%.1f", tpl.durationSeconds)}s • ${tpl.cutsCount} Cuts • ${tpl.bpm} BPM",
                            color = TextSecondary,
                            fontSize = 12.sp
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        TimelineMiniPreview(
                            cutsCount = tpl.cutsCount,
                            keyframesCount = tpl.keyframesCount,
                            durationSeconds = tpl.durationSeconds,
                            modifier = Modifier.height(20.dp)
                        )
                    }

                    if (isSelected) {
                        Spacer(modifier = Modifier.width(10.dp))
                        Box(
                            modifier = Modifier
                                .size(28.dp)
                                .clip(CircleShape)
                                .background(ElectricCyan),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(imageVector = Icons.Filled.Check, contentDescription = null, tint = DeepBlack, modifier = Modifier.size(18.dp))
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun Step2SelectVideo(
    repository: TemplateRepository,
    selectedVideo: SourceVideoMeta,
    onSelectVideo: (SourceVideoMeta) -> Unit
) {
    val sampleVideos = remember { repository.getSampleSourceVideos() }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(horizontal = 20.dp, vertical = 12.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            Text(
                text = "Select source footage to cut and keyframe automatically:",
                color = TextSecondary,
                fontSize = 13.sp
            )
        }

        items(sampleVideos, key = { it.id }) { video ->
            val isSelected = video.id == selectedVideo.id

            GlassCard(
                modifier = Modifier.fillMaxWidth(),
                shapeRadius = 14.dp,
                borderGlowColor = if (isSelected) ElectricCyan else CardBorderSubtle,
                backgroundColor = if (isSelected) SurfaceNavyElevated else SurfaceNavy.copy(alpha = 0.7f),
                onClick = { onSelectVideo(video) }
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(14.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                        Icon(
                            imageVector = Icons.Filled.Movie,
                            contentDescription = null,
                            tint = if (isSelected) ElectricCyan else TextSecondary,
                            modifier = Modifier.size(24.dp)
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text(
                                text = video.name,
                                color = TextPrimary,
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.sp
                            )
                            Text(
                                text = "${video.resolution} • ${String.format("%.1f", video.durationSeconds)}s • ${video.fps}fps",
                                color = TextSecondary,
                                fontSize = 12.sp
                            )
                        }
                    }

                    if (isSelected) {
                        Box(
                            modifier = Modifier
                                .size(28.dp)
                                .clip(CircleShape)
                                .background(ElectricCyan),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(imageVector = Icons.Filled.Check, contentDescription = null, tint = DeepBlack, modifier = Modifier.size(18.dp))
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun Step3MatchSlots(
    template: VideoTemplate,
    video: SourceVideoMeta,
    trimMap: Map<Int, Float>,
    onUpdateTrim: (Int, Float) -> Unit
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(horizontal = 20.dp, vertical = 12.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        item {
            Column {
                Text(
                    text = "Matching ${template.clipSlots.size} Timeline Segments",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextPrimary
                )
                Text(
                    text = "The template's rhythm will extract these exact durations from ${video.name}:",
                    fontSize = 12.sp,
                    color = TextSecondary
                )
            }
        }

        items(template.clipSlots) { slot ->
            val trimVal = trimMap[slot.slotIndex] ?: 0f

            GlassCard(
                modifier = Modifier.fillMaxWidth(),
                shapeRadius = 14.dp,
                backgroundColor = SurfaceNavy
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Surface(
                                shape = CircleShape,
                                color = ElectricCyan.copy(alpha = 0.2f),
                                modifier = Modifier.size(28.dp)
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Text(
                                        text = "${slot.slotIndex}",
                                        color = ElectricCyan,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 12.sp
                                    )
                                }
                            }
                            Spacer(modifier = Modifier.width(10.dp))
                            Text(
                                text = slot.label,
                                color = TextPrimary,
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.sp
                            )
                        }

                        Text(
                            text = "${String.format("%.1f", slot.durationSeconds)}s @ ${slot.speedFactor}x",
                            color = NeonViolet,
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 12.sp
                        )
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    Text(
                        text = "Source Offset: +${String.format("%.1f", trimVal)}s",
                        color = TextMuted,
                        fontSize = 11.sp
                    )

                    Slider(
                        value = trimVal,
                        onValueChange = { onUpdateTrim(slot.slotIndex, it) },
                        valueRange = 0f..10f,
                        colors = SliderDefaults.colors(
                            thumbColor = ElectricCyan,
                            activeTrackColor = ElectricCyan,
                            inactiveTrackColor = SurfaceNavyElevated
                        )
                    )
                }
            }
        }
    }
}

@Composable
private fun Step4PreviewResult(
    template: VideoTemplate,
    video: SourceVideoMeta,
    isPlaying: Boolean,
    progress: Float,
    onTogglePlay: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(20.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = "Synchronized Video Preview",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = TextPrimary
            )
            Text(
                text = "Simulating cuts, keyframe speed ramps, and audio pacing",
                fontSize = 12.sp,
                color = TextSecondary
            )
        }

        // Preview Canvas Monitor
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .height(280.dp)
                .clip(RoundedCornerShape(20.dp)),
            color = SurfaceNavy,
            border = BorderStroke(1.5.dp, GlassBorder)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        Brush.radialGradient(
                            listOf(
                                ElectricCyan.copy(alpha = 0.2f),
                                DeepBlack
                            )
                        )
                    )
            ) {
                // Monitor center graphic
                Column(
                    modifier = Modifier.align(Alignment.Center),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Box(
                        modifier = Modifier
                            .size(60.dp)
                            .clip(CircleShape)
                            .background(ElectricCyan.copy(alpha = 0.3f))
                            .clickable(onClick = onTogglePlay),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = if (isPlaying) Icons.Filled.Pause else Icons.Filled.PlayArrow,
                            contentDescription = "Toggle",
                            tint = ElectricCyan,
                            modifier = Modifier.size(32.dp)
                        )
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    Text(
                        text = if (isPlaying) "Playing: ${template.title}" else "Tap to Play Synchronized Cuts",
                        color = TextPrimary,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Medium
                    )
                }

                // Timecode
                val curTime = template.durationSeconds * progress
                Surface(
                    shape = RoundedCornerShape(6.dp),
                    color = DeepBlack.copy(alpha = 0.8f),
                    modifier = Modifier
                        .align(Alignment.BottomEnd)
                        .padding(12.dp)
                ) {
                    Text(
                        text = "${String.format("00:%04.1f", curTime)} / ${String.format("00:%04.1f", template.durationSeconds)}",
                        color = ElectricCyan,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }
            }
        }

        // Progress bar
        Column(modifier = Modifier.fillMaxWidth()) {
            LinearProgressIndicator(
                progress = { progress },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(6.dp)
                    .clip(RoundedCornerShape(3.dp)),
                color = ElectricCyan,
                trackColor = SurfaceNavyElevated
            )
            Spacer(modifier = Modifier.height(8.dp))
            TimelineMiniPreview(
                cutsCount = template.cutsCount,
                keyframesCount = template.keyframesCount,
                durationSeconds = template.durationSeconds,
                modifier = Modifier.height(24.dp)
            )
        }
    }
}

@Composable
private fun Step5ExportPackage(
    template: VideoTemplate,
    video: SourceVideoMeta,
    isExporting: Boolean,
    statusMessage: String?,
    onTriggerExport: () -> Unit,
    onFinish: () -> Unit
) {
    var hasExported by remember { mutableStateOf(false) }

    LaunchedEffect(isExporting) {
        if (isExporting) {
            delay(1500)
            hasExported = true
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(20.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Surface(
            modifier = Modifier.size(80.dp),
            shape = CircleShape,
            color = if (hasExported) NeonGreen.copy(alpha = 0.2f) else ElectricCyan.copy(alpha = 0.2f)
        ) {
            Box(contentAlignment = Alignment.Center) {
                Icon(
                    imageVector = if (hasExported) Icons.Filled.CheckCircle else Icons.Filled.Download,
                    contentDescription = null,
                    tint = if (hasExported) NeonGreen else ElectricCyan,
                    modifier = Modifier.size(40.dp)
                )
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        Text(
            text = if (hasExported) "Template Applied & Compiled!" else "Compile Editing Blueprint",
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold,
            color = TextPrimary
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = if (hasExported)
                "The EDL sequence has been compiled. All ${template.cutsCount} cuts and keyframe coordinates are mapped to ${video.name}."
            else
                "Export a non-destructive EditCopy project package (.ecproj) containing the complete transfer recipe.",
            fontSize = 13.sp,
            color = TextSecondary,
            lineHeight = 18.sp,
            textAlign = androidx.compose.ui.text.style.TextAlign.Center
        )

        Spacer(modifier = Modifier.height(24.dp))

        // Honest Engine Notice
        GlassCard(
            modifier = Modifier.fillMaxWidth(),
            shapeRadius = 14.dp,
            borderGlowColor = AmberWarning.copy(alpha = 0.4f),
            backgroundColor = SurfaceNavy
        ) {
            Column(modifier = Modifier.padding(14.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(imageVector = Icons.Filled.Warning, contentDescription = null, tint = AmberWarning, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Engine Notice", color = AmberWarning, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                }
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "EditCopy outputs non-destructive metadata bundles (.ecproj / .json). Video re-encoding requires hardware encoder integration, which will be coupled in the upcoming native rendering pipeline.",
                    color = TextSecondary,
                    fontSize = 11.sp,
                    lineHeight = 15.sp
                )
            }
        }

        Spacer(modifier = Modifier.height(28.dp))

        if (!hasExported) {
            NeonButton(
                text = if (isExporting) "Compiling Blueprint..." else "Compile & Save Package",
                onClick = onTriggerExport,
                enabled = !isExporting,
                modifier = Modifier.fillMaxWidth(),
                testTag = "trigger_export_button"
            )
        } else {
            NeonButton(
                text = "Finish & Return to Library",
                onClick = onFinish,
                modifier = Modifier.fillMaxWidth(),
                testTag = "finish_workflow_button"
            )
        }
    }
}

@Composable
private fun WorkflowFooter(
    currentStep: Int,
    canProceed: Boolean,
    onNext: () -> Unit,
    onPrev: () -> Unit
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = DeepBlack,
        border = BorderStroke(1.dp, CardBorderSubtle)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            if (currentStep > 1) {
                NeonOutlinedButton(
                    text = "Back",
                    onClick = onPrev,
                    accentColor = TextSecondary,
                    modifier = Modifier.weight(0.8f)
                )
            } else {
                Spacer(modifier = Modifier.weight(0.8f))
            }

            Spacer(modifier = Modifier.width(16.dp))

            if (currentStep < 5) {
                NeonButton(
                    text = "Continue",
                    onClick = onNext,
                    enabled = canProceed,
                    modifier = Modifier.weight(1.2f),
                    testTag = "workflow_continue_button"
                )
            }
        }
    }
}
