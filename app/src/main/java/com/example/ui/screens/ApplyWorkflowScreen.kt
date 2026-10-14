package com.example.ui.screens

import android.content.Intent
import android.media.MediaMetadataRetriever
import android.net.Uri
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedContent
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
import androidx.compose.material.icons.filled.AddPhotoAlternate
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.ErrorOutline
import androidx.compose.material.icons.filled.Movie
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Sync
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.data.model.ClipSlot
import com.example.data.model.SourceVideoMeta
import com.example.data.model.VideoTemplate
import com.example.data.repository.TemplateRepository
import com.example.data.util.ExportState
import com.example.data.util.VideoExportManager
import com.example.ui.components.FeatureBadge
import com.example.ui.components.GlassCard
import com.example.ui.components.NeonButton
import com.example.ui.components.NeonOutlinedButton
import com.example.ui.components.RealVideoPlayerView
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
import com.example.ui.theme.RedError
import com.example.ui.theme.SurfaceNavy
import com.example.ui.theme.SurfaceNavyElevated
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import kotlinx.coroutines.launch

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
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val exportManager = remember { VideoExportManager(context) }

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

    var exportState by remember { mutableStateOf<ExportState>(ExportState.Idle) }

    BackHandler {
        if (exportState is ExportState.Exporting) {
            exportManager.cancelExport()
            exportState = ExportState.Idle
        } else if (currentStep > 1) {
            currentStep--
        } else {
            onBack()
        }
    }

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
                    if (exportState is ExportState.Exporting) {
                        exportManager.cancelExport()
                        exportState = ExportState.Idle
                    } else if (currentStep > 1) {
                        currentStep--
                    } else {
                        onBack()
                    }
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
                        slotTrims = slotTrimMap
                    )
                    5 -> Step5ExportPackage(
                        template = selectedTemplate,
                        video = selectedVideo,
                        exportState = exportState,
                        onTriggerExport = {
                            coroutineScope.launch {
                                exportManager.exportVideo(
                                    template = selectedTemplate,
                                    sourceVideo = selectedVideo,
                                    slotTrims = slotTrimMap
                                ) { state ->
                                    exportState = state
                                }
                            }
                        },
                        onCancelExport = {
                            exportManager.cancelExport()
                            exportState = ExportState.Idle
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
    val context = LocalContext.current
    val sampleVideos = remember { repository.getSampleSourceVideos() }

    val photoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri: Uri? ->
        if (uri != null) {
            try {
                context.contentResolver.takePersistableUriPermission(
                    uri,
                    Intent.FLAG_GRANT_READ_URI_PERMISSION
                )
            } catch (ignored: Exception) {}

            var durationSec = 15.0f
            var resolutionStr = "1080 x 1920 (9:16)"
            var width = 1080
            var height = 1920
            try {
                val retriever = MediaMetadataRetriever()
                retriever.setDataSource(context, uri)
                val d = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION)
                if (d != null) durationSec = d.toFloat() / 1000f
                val w = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_VIDEO_WIDTH)
                val h = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_VIDEO_HEIGHT)
                if (w != null && h != null) {
                    width = w.toInt()
                    height = h.toInt()
                    val ratio = if (width < height) "9:16" else "16:9"
                    resolutionStr = "$width x $height ($ratio)"
                }
                retriever.release()
            } catch (ignored: Exception) {}

            val pickedName = uri.lastPathSegment?.substringAfterLast('/') ?: "Picked_Footage.mp4"
            val pickedMeta = SourceVideoMeta(
                id = uri.toString(),
                name = pickedName,
                uriString = uri.toString(),
                durationSeconds = durationSec,
                resolution = resolutionStr,
                aspectRatio = if (width < height) "9:16" else "16:9",
                fileSizeBytes = 25_000_000,
                fps = 60,
                isSample = false
            )
            onSelectVideo(pickedMeta)
        }
    }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(horizontal = 20.dp, vertical = 12.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            // Pick from Device Gallery Card
            GlassCard(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable {
                        photoPickerLauncher.launch(
                            PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.VideoOnly)
                        )
                    }
                    .testTag("pick_device_video_card"),
                shapeRadius = 14.dp,
                borderGlowColor = ElectricCyan,
                backgroundColor = SurfaceNavyElevated
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(44.dp)
                            .clip(CircleShape)
                            .background(ElectricCyan.copy(alpha = 0.2f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Filled.AddPhotoAlternate,
                            contentDescription = "Pick custom video",
                            tint = ElectricCyan,
                            modifier = Modifier.size(24.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(14.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Select from Device Gallery",
                            color = TextPrimary,
                            fontWeight = FontWeight.Bold,
                            fontSize = 15.sp
                        )
                        Text(
                            text = "Choose your own real video via Android Photo Picker",
                            color = TextSecondary,
                            fontSize = 12.sp
                        )
                    }
                }
            }
        }

        if (!selectedVideo.isSample) {
            item {
                Text(
                    text = "Selected User Video:",
                    color = ElectricCyan,
                    fontWeight = FontWeight.Bold,
                    fontSize = 13.sp
                )
                Spacer(modifier = Modifier.height(4.dp))
                GlassCard(
                    modifier = Modifier.fillMaxWidth(),
                    shapeRadius = 14.dp,
                    borderGlowColor = ElectricCyan,
                    backgroundColor = SurfaceNavyElevated
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
                                tint = ElectricCyan,
                                modifier = Modifier.size(24.dp)
                            )
                            Spacer(modifier = Modifier.width(12.dp))
                            Column {
                                Text(
                                    text = selectedVideo.name,
                                    color = TextPrimary,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 14.sp
                                )
                                Text(
                                    text = "${selectedVideo.resolution} • ${String.format("%.1f", selectedVideo.durationSeconds)}s",
                                    color = TextSecondary,
                                    fontSize = 12.sp
                                )
                            }
                        }
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

        item {
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "Or Choose a Studio Sample Video:",
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
    slotTrims: Map<Int, Float> = emptyMap()
) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 20.dp, vertical = 12.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Column {
                Text(
                    text = "Live Synchronized Video Preview",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextPrimary
                )
                Text(
                    text = "AndroidX Media3 ExoPlayer playback mapped to '${template.title}'",
                    fontSize = 12.sp,
                    color = TextSecondary
                )
            }
        }

        item {
            RealVideoPlayerView(
                sourceVideo = video,
                height = 290.dp,
                clipTitle = "${video.name} • ${template.title}",
                autoPlay = true,
                modifier = Modifier.testTag("step4_real_video_player")
            )
        }

        item {
            // Template Pacing Breakdown Card
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
                        Text(
                            text = "Target Template Rhythm",
                            color = ElectricCyan,
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp
                        )
                        Text(
                            text = "${template.clipSlots.size.coerceAtLeast(template.cutsCount)} Cut Slots • ${String.format("%.1f", template.durationSeconds)}s",
                            color = TextSecondary,
                            fontSize = 12.sp
                        )
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    TimelineMiniPreview(
                        cutsCount = template.cutsCount,
                        keyframesCount = template.keyframesCount,
                        durationSeconds = template.durationSeconds,
                        modifier = Modifier.height(24.dp)
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    template.clipSlots.take(4).forEach { slot ->
                        val trim = slotTrims[slot.slotIndex] ?: 0f
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 3.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Slot ${slot.slotIndex}: ${slot.label}",
                                color = TextPrimary,
                                fontSize = 12.sp
                            )
                            Text(
                                text = "${String.format("%.1f", slot.durationSeconds)}s (${slot.transitionIn ?: "Cut"})${if (trim > 0f) " +${String.format("%.1f", trim)}s" else ""}",
                                color = NeonViolet,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun Step5ExportPackage(
    template: VideoTemplate,
    video: SourceVideoMeta,
    exportState: ExportState,
    onTriggerExport: () -> Unit,
    onCancelExport: () -> Unit,
    onFinish: () -> Unit
) {
    val context = LocalContext.current
    var isPlayingExported by remember { mutableStateOf(false) }

    if (isPlayingExported && exportState is ExportState.Success) {
        Dialog(onDismissRequest = { isPlayingExported = false }) {
            Surface(
                shape = RoundedCornerShape(20.dp),
                color = DeepBlack,
                border = BorderStroke(1.5.dp, GlassBorder),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(8.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Exported Video Player",
                            color = TextPrimary,
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp
                        )
                        IconButton(onClick = { isPlayingExported = false }) {
                            Icon(imageVector = Icons.Filled.Close, contentDescription = "Close", tint = TextSecondary)
                        }
                    }
                    Spacer(modifier = Modifier.height(12.dp))
                    RealVideoPlayerView(
                        sourceVideo = null,
                        directUri = exportState.outputUri,
                        height = 280.dp,
                        clipTitle = exportState.galleryPath.substringAfterLast('/'),
                        autoPlay = true
                    )
                }
            }
        }
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 20.dp, vertical = 12.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        when (exportState) {
            is ExportState.Idle -> {
                item {
                    Spacer(modifier = Modifier.height(20.dp))
                    Surface(
                        modifier = Modifier.size(80.dp),
                        shape = CircleShape,
                        color = ElectricCyan.copy(alpha = 0.2f)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                imageVector = Icons.Filled.Download,
                                contentDescription = null,
                                tint = ElectricCyan,
                                modifier = Modifier.size(40.dp)
                            )
                        }
                    }
                }

                item {
                    Text(
                        text = "Export Video to Gallery",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "Apply '${template.title}' cut timing to '${video.name}' and render an MP4 video directly into your device Gallery.",
                        fontSize = 13.sp,
                        color = TextSecondary,
                        textAlign = androidx.compose.ui.text.style.TextAlign.Center
                    )
                }

                item {
                    GlassCard(
                        modifier = Modifier.fillMaxWidth(),
                        shapeRadius = 14.dp,
                        backgroundColor = SurfaceNavy
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text("Export Specifications", color = ElectricCyan, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                            Spacer(modifier = Modifier.height(8.dp))
                            DetailRow("Target Duration", "${String.format("%.1f", template.durationSeconds)}s")
                            DetailRow("Cuts to Apply", "${template.clipSlots.size.coerceAtLeast(template.cutsCount)} cuts")
                            DetailRow("Resolution", video.resolution)
                            DetailRow("Gallery Location", "Movies/EditCopy")
                        }
                    }
                }

                item {
                    Spacer(modifier = Modifier.height(10.dp))
                    NeonButton(
                        text = "Export Video & Save to Gallery",
                        onClick = onTriggerExport,
                        modifier = Modifier.fillMaxWidth(),
                        testTag = "trigger_export_button"
                    )
                }
            }

            is ExportState.Exporting -> {
                item {
                    Spacer(modifier = Modifier.height(30.dp))
                    CircularProgressIndicator(
                        modifier = Modifier.size(60.dp),
                        color = ElectricCyan,
                        strokeWidth = 4.dp
                    )
                }

                item {
                    Text(
                        text = "${exportState.progressPercent}%",
                        fontSize = 28.sp,
                        fontWeight = FontWeight.Bold,
                        color = ElectricCyan
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = exportState.message,
                        fontSize = 14.sp,
                        color = TextPrimary,
                        textAlign = androidx.compose.ui.text.style.TextAlign.Center
                    )
                }

                item {
                    LinearProgressIndicator(
                        progress = { exportState.progressPercent / 100f },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(8.dp)
                            .clip(RoundedCornerShape(4.dp))
                            .testTag("export_progress_bar"),
                        color = ElectricCyan,
                        trackColor = SurfaceNavyElevated
                    )
                }

                item {
                    Spacer(modifier = Modifier.height(16.dp))
                    NeonOutlinedButton(
                        text = "Cancel Export",
                        onClick = onCancelExport,
                        accentColor = RedError,
                        modifier = Modifier.fillMaxWidth(),
                        testTag = "cancel_export_button"
                    )
                }
            }

            is ExportState.Error -> {
                item {
                    Spacer(modifier = Modifier.height(20.dp))
                    Surface(
                        modifier = Modifier.size(80.dp),
                        shape = CircleShape,
                        color = RedError.copy(alpha = 0.2f)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                imageVector = Icons.Filled.ErrorOutline,
                                contentDescription = null,
                                tint = RedError,
                                modifier = Modifier.size(40.dp)
                            )
                        }
                    }
                }

                item {
                    Text(
                        text = "Export Failed",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = RedError
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = exportState.errorMessage,
                        fontSize = 13.sp,
                        color = TextPrimary,
                        textAlign = androidx.compose.ui.text.style.TextAlign.Center
                    )
                    if (exportState.details.isNotEmpty()) {
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = exportState.details.take(200),
                            fontSize = 11.sp,
                            color = TextSecondary,
                            textAlign = androidx.compose.ui.text.style.TextAlign.Center
                        )
                    }
                }

                item {
                    Spacer(modifier = Modifier.height(16.dp))
                    NeonButton(
                        text = "Retry Export",
                        onClick = onTriggerExport,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }

            is ExportState.Success -> {
                item {
                    Spacer(modifier = Modifier.height(12.dp))
                    Surface(
                        modifier = Modifier.size(80.dp),
                        shape = CircleShape,
                        color = NeonGreen.copy(alpha = 0.2f)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                imageVector = Icons.Filled.CheckCircle,
                                contentDescription = null,
                                tint = NeonGreen,
                                modifier = Modifier.size(44.dp)
                            )
                        }
                    }
                }

                item {
                    Text(
                        text = "Video Exported & Saved to Gallery!",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary,
                        textAlign = androidx.compose.ui.text.style.TextAlign.Center
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "The MP4 video was generated and registered with Android MediaStore.",
                        fontSize = 13.sp,
                        color = TextSecondary,
                        textAlign = androidx.compose.ui.text.style.TextAlign.Center
                    )
                }

                item {
                    GlassCard(
                        modifier = Modifier.fillMaxWidth(),
                        shapeRadius = 14.dp,
                        backgroundColor = SurfaceNavy,
                        borderGlowColor = NeonGreen.copy(alpha = 0.3f)
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text("Verified Render Output", color = NeonGreen, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                            Spacer(modifier = Modifier.height(8.dp))
                            DetailRow("Saved Location", exportState.galleryPath)
                            DetailRow("Duration", "${String.format("%.1f", exportState.durationSeconds)}s")
                            DetailRow("Resolution", exportState.resolution)
                            DetailRow("File Size", "${String.format("%.2f", exportState.fileSizeBytes / (1024f * 1024f))} MB")
                            DetailRow("Cuts Applied", "${exportState.cutsApplied} cuts synchronized")
                        }
                    }
                }

                item {
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        NeonButton(
                            text = "Play Exported Video",
                            icon = Icons.Filled.PlayArrow,
                            onClick = { isPlayingExported = true },
                            modifier = Modifier.fillMaxWidth(),
                            testTag = "play_exported_video_button"
                        )

                        NeonOutlinedButton(
                            text = "Share Video",
                            icon = Icons.Filled.Share,
                            onClick = {
                                try {
                                    val shareIntent = Intent(Intent.ACTION_SEND).apply {
                                        type = "video/mp4"
                                        putExtra(Intent.EXTRA_STREAM, exportState.outputUri)
                                        addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                                    }
                                    context.startActivity(Intent.createChooser(shareIntent, "Share Exported Video"))
                                } catch (e: Exception) {
                                    android.util.Log.e("ApplyWorkflow", "Share error: ${e.message}")
                                }
                            },
                            accentColor = ElectricCyan,
                            modifier = Modifier.fillMaxWidth(),
                            testTag = "share_exported_video_button"
                        )

                        NeonButton(
                            text = "Finish & Return to Library",
                            onClick = onFinish,
                            modifier = Modifier.fillMaxWidth(),
                            testTag = "finish_workflow_button"
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun DetailRow(label: String, value: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 3.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(text = label, color = TextSecondary, fontSize = 12.sp)
        Text(text = value, color = TextPrimary, fontWeight = FontWeight.SemiBold, fontSize = 12.sp)
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
