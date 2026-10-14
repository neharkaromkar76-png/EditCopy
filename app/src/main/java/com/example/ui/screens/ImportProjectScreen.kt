package com.example.ui.screens

import android.net.Uri
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
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
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.ErrorOutline
import androidx.compose.material.icons.filled.FileDownload
import androidx.compose.material.icons.filled.Science
import androidx.compose.material.icons.filled.UploadFile
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
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
import com.example.data.model.ImportAnalysisResult
import com.example.data.model.ImportFormatSupportStatus
import com.example.data.model.ProjectFormatInfo
import com.example.data.model.VideoTemplate
import com.example.data.repository.TemplateRepository
import com.example.ui.components.FeatureBadge
import com.example.ui.components.GlassCard
import com.example.ui.components.NeonButton
import com.example.ui.components.NeonOutlinedButton
import com.example.ui.theme.AmberWarning
import com.example.ui.theme.CardBorderSubtle
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

@Composable
fun ImportProjectScreen(
    repository: TemplateRepository,
    onBack: () -> Unit,
    onTemplateImported: (VideoTemplate) -> Unit,
    modifier: Modifier = Modifier
) {
    BackHandler(onBack = onBack)

    var analysisResult by remember { mutableStateOf<ImportAnalysisResult?>(null) }
    var isAnalyzing by remember { mutableStateOf(false) }

    val filePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        if (uri != null) {
            val fileName = uri.lastPathSegment ?: "imported_project.ecproj"
            isAnalyzing = true
            // Run analysis
            val result = repository.analyzeProjectImport(fileName)
            analysisResult = result
            isAnalyzing = false
        }
    }

    val supportedFormats = remember { repository.getSupportedFormats() }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(DeepBlack)
            .statusBarsPadding()
            .navigationBarsPadding()
            .testTag("import_project_screen")
    ) {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(bottom = 96.dp)
        ) {
            // Header
            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(onClick = onBack, modifier = Modifier.testTag("import_back_button")) {
                        Icon(imageVector = Icons.Filled.ArrowBack, contentDescription = "Back", tint = TextPrimary)
                    }

                    Spacer(modifier = Modifier.width(8.dp))

                    Column {
                        Text(
                            text = "Import Project File",
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold,
                            color = TextPrimary
                        )
                        Text(
                            text = "Extract cuts, keyframe curves, and audio timing",
                            fontSize = 12.sp,
                            color = TextSecondary
                        )
                    }
                }
            }

            // File Upload Dropzone
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp, vertical = 8.dp)
                ) {
                    Surface(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(20.dp))
                            .clickable {
                                filePickerLauncher.launch("*/*")
                            }
                            .testTag("import_file_dropzone"),
                        color = SurfaceNavy,
                        border = BorderStroke(1.5.dp, GlassBorder)
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(28.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(68.dp)
                                    .clip(CircleShape)
                                    .background(
                                        Brush.linearGradient(listOf(ElectricCyan.copy(alpha = 0.2f), NeonViolet.copy(alpha = 0.2f)))
                                    ),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Filled.UploadFile,
                                    contentDescription = "Upload",
                                    tint = ElectricCyan,
                                    modifier = Modifier.size(34.dp)
                                )
                            }

                            Spacer(modifier = Modifier.height(16.dp))

                            Text(
                                text = "Select Project to Inspect",
                                fontSize = 17.sp,
                                fontWeight = FontWeight.Bold,
                                color = TextPrimary
                            )

                            Spacer(modifier = Modifier.height(6.dp))

                            Text(
                                text = "Tap to open system file picker (.ecproj, .otio, .xml, .vnproject)",
                                fontSize = 12.sp,
                                color = TextSecondary
                            )

                            Spacer(modifier = Modifier.height(18.dp))

                            NeonButton(
                                text = "Browse Files",
                                onClick = { filePickerLauncher.launch("*/*") },
                                icon = Icons.Filled.FileDownload,
                                testTag = "browse_files_button"
                            )
                        }
                    }
                }
            }

            // Sample Projects Quick Loader (for testing right now in the browser emulator!)
            item {
                Spacer(modifier = Modifier.height(10.dp))
                Text(
                    text = "Test With Sample Project Archives",
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextPrimary,
                    modifier = Modifier.padding(horizontal = 20.dp, vertical = 6.dp)
                )
            }

            item {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    SampleFileButton(
                        fileName = "Cyber_Velocity_Preset.ecproj",
                        formatType = "Native EditCopy JSON",
                        isVerified = true,
                        onClick = {
                            analysisResult = repository.analyzeProjectImport("Cyber_Velocity_Preset.ecproj")
                        }
                    )

                    SampleFileButton(
                        fileName = "Dance_BeatDrop_Edit.vnproject",
                        formatType = "VN Video Editor Archive (Lab Test)",
                        isVerified = false,
                        onClick = {
                            analysisResult = repository.analyzeProjectImport("Dance_BeatDrop_Edit.vnproject")
                        }
                    )

                    SampleFileButton(
                        fileName = "Documentary_Sequence.otio",
                        formatType = "OpenTimelineIO Interchange",
                        isVerified = true,
                        onClick = {
                            analysisResult = repository.analyzeProjectImport("Documentary_Sequence.otio")
                        }
                    )
                }
            }

            // Analysis Result Inspector Card
            if (analysisResult != null) {
                item {
                    Spacer(modifier = Modifier.height(18.dp))
                    AnalysisResultCard(
                        result = analysisResult!!,
                        onUseTemplate = {
                            analysisResult?.suggestedTemplate?.let { tpl ->
                                onTemplateImported(tpl)
                            }
                        }
                    )
                }
            }

            // Supported Format Guide & Transparent Compatibility Matrix
            item {
                Spacer(modifier = Modifier.height(24.dp))
                Text(
                    text = "Format Compatibility & Lab Status",
                    fontSize = 17.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextPrimary,
                    modifier = Modifier.padding(horizontal = 20.dp, vertical = 6.dp)
                )
            }

            items(supportedFormats) { format ->
                FormatInfoCard(format = format)
            }
        }
    }
}

@Composable
private fun SampleFileButton(
    fileName: String,
    formatType: String,
    isVerified: Boolean,
    onClick: () -> Unit
) {
    GlassCard(
        modifier = Modifier.fillMaxWidth(),
        shapeRadius = 12.dp,
        onClick = onClick,
        backgroundColor = SurfaceNavy.copy(alpha = 0.8f)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 14.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Filled.Description,
                    contentDescription = null,
                    tint = if (isVerified) ElectricCyan else AmberWarning,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(10.dp))
                Column {
                    Text(
                        text = fileName,
                        color = TextPrimary,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                    Text(
                        text = formatType,
                        color = TextSecondary,
                        fontSize = 11.sp
                    )
                }
            }

            Surface(
                shape = RoundedCornerShape(6.dp),
                color = if (isVerified) NeonGreen.copy(alpha = 0.15f) else AmberWarning.copy(alpha = 0.15f)
            ) {
                Text(
                    text = if (isVerified) "Test Parse" else "Lab Test",
                    color = if (isVerified) NeonGreen else AmberWarning,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                )
            }
        }
    }
}

@Composable
private fun AnalysisResultCard(
    result: ImportAnalysisResult,
    onUseTemplate: () -> Unit
) {
    val borderColor = if (result.isCompatible) NeonGreen else AmberWarning

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp)
    ) {
        Surface(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            color = SurfaceNavyElevated,
            border = BorderStroke(1.5.dp, borderColor.copy(alpha = 0.7f))
        ) {
            Column(modifier = Modifier.padding(18.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = if (result.isCompatible) Icons.Filled.CheckCircle else Icons.Filled.Science,
                            contentDescription = null,
                            tint = borderColor,
                            modifier = Modifier.size(22.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = result.statusBadge,
                            color = borderColor,
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp
                        )
                    }

                    Surface(
                        shape = RoundedCornerShape(6.dp),
                        color = DeepBlack
                    ) {
                        Text(
                            text = result.detectedFormat,
                            color = TextSecondary,
                            fontSize = 10.sp,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                Text(
                    text = result.summaryNotice,
                    color = TextPrimary,
                    fontSize = 13.sp,
                    lineHeight = 18.sp
                )

                if (result.extractedCuts > 0 || result.extractedKeyframes > 0) {
                    Spacer(modifier = Modifier.height(14.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        FeatureBadge(label = "${result.extractedCuts} Cuts Extracted", accentColor = ElectricCyan)
                        FeatureBadge(label = "${result.extractedKeyframes} Keyframes Extracted", accentColor = NeonViolet)
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                if (result.suggestedTemplate != null) {
                    NeonButton(
                        text = if (result.isCompatible) "Use Extracted Template" else "Adopt Safe Reconstructed Pacing",
                        onClick = onUseTemplate,
                        modifier = Modifier.fillMaxWidth(),
                        testTag = "adopt_imported_template_button"
                    )
                }
            }
        }
    }
}

@Composable
private fun FormatInfoCard(format: ProjectFormatInfo) {
    val statusColor = when (format.status) {
        ImportFormatSupportStatus.SUPPORTED -> NeonGreen
        ImportFormatSupportStatus.STRUCTURAL_ONLY -> ElectricCyan
        ImportFormatSupportStatus.TESTING_LAB_ONLY -> AmberWarning
        ImportFormatSupportStatus.UNSUPPORTED -> RedError
    }

    GlassCard(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp, vertical = 6.dp),
        shapeRadius = 14.dp,
        borderGlowColor = statusColor.copy(alpha = 0.25f),
        backgroundColor = SurfaceNavy.copy(alpha = 0.6f)
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = format.formatName,
                    color = TextPrimary,
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp
                )

                Surface(
                    shape = RoundedCornerShape(6.dp),
                    color = statusColor.copy(alpha = 0.15f)
                ) {
                    Text(
                        text = when (format.status) {
                            ImportFormatSupportStatus.SUPPORTED -> "Fully Verified"
                            ImportFormatSupportStatus.STRUCTURAL_ONLY -> "EDL Structural"
                            ImportFormatSupportStatus.TESTING_LAB_ONLY -> "Lab Testing Active"
                            ImportFormatSupportStatus.UNSUPPORTED -> "Unsupported"
                        },
                        color = statusColor,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(6.dp))

            Text(
                text = format.description,
                color = TextSecondary,
                fontSize = 12.sp,
                lineHeight = 16.sp
            )

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = "Note: ${format.note}",
                color = TextMuted,
                fontSize = 11.sp,
                lineHeight = 15.sp
            )
        }
    }
}
