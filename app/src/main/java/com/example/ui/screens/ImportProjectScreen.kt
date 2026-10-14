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
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Science
import androidx.compose.material.icons.filled.UploadFile
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
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
import com.example.data.model.ImportAnalysisResult
import com.example.data.model.ImportFormatSupportStatus
import com.example.data.model.ProjectFormatInfo
import com.example.data.model.VideoTemplate
import com.example.data.repository.TemplateRepository
import com.example.data.util.FileMetadata
import com.example.data.util.ProjectImportManager
import com.example.data.util.SevenZipProjectExtractor
import com.example.ui.components.FeatureBadge
import com.example.ui.components.GlassCard
import com.example.ui.components.NeonButton
import com.example.ui.components.NeonOutlinedButton
import com.example.ui.theme.AmberWarning
import com.example.ui.theme.CardBorderSubtle
import com.example.ui.theme.DeepBlack
import com.example.ui.theme.ElectricCyan
import com.example.ui.theme.GlassBorder
import com.example.ui.theme.NeonGreen
import com.example.ui.theme.NeonViolet
import com.example.ui.theme.RedError
import com.example.ui.theme.SurfaceNavy
import com.example.ui.theme.SurfaceNavyElevated
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

@Composable
fun ImportProjectScreen(
    repository: TemplateRepository,
    onBack: () -> Unit,
    onTemplateImported: (VideoTemplate) -> Unit,
    modifier: Modifier = Modifier
) {
    BackHandler(onBack = onBack)

    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()

    var analysisResult by remember { mutableStateOf<ImportAnalysisResult?>(null) }
    var selectedFileMeta by remember { mutableStateOf<FileMetadata?>(null) }
    var isAnalyzing by remember { mutableStateOf(false) }
    var isWorkflowGuideExpanded by remember { mutableStateOf(true) }

    // Standard Android Document Picker using ACTION_OPEN_DOCUMENT and CATEGORY_OPENABLE
    // With broad MIME type strategy to ensure Android Document Providers (internal storage, Downloads, Drive)
    // allow selecting .ecproj, .json, .otio, .7z, .zip, .xml, .vnproject
    val filePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument()
    ) { uri: Uri? ->
        if (uri != null) {
            val meta = ProjectImportManager.getFileMetadata(context, uri)
            selectedFileMeta = meta
            isAnalyzing = true
            analysisResult = null

            coroutineScope.launch {
                val result = withContext(Dispatchers.IO) {
                    ProjectImportManager.inspectAndImport(context, uri)
                }
                analysisResult = result
                isAnalyzing = false
            }
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

            // Primary Import Action Card with dual Browse / Archive buttons
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp, vertical = 6.dp)
                ) {
                    Surface(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(20.dp))
                            .testTag("import_file_dropzone"),
                        color = SurfaceNavy,
                        border = BorderStroke(1.5.dp, GlassBorder)
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(24.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(64.dp)
                                    .clip(CircleShape)
                                    .background(
                                        Brush.linearGradient(
                                            listOf(
                                                ElectricCyan.copy(alpha = 0.2f),
                                                NeonViolet.copy(alpha = 0.2f)
                                            )
                                        )
                                    ),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Filled.UploadFile,
                                    contentDescription = "Upload",
                                    tint = ElectricCyan,
                                    modifier = Modifier.size(32.dp)
                                )
                            }

                            Spacer(modifier = Modifier.height(14.dp))

                            Text(
                                text = "Select Project or Archive",
                                fontSize = 17.sp,
                                fontWeight = FontWeight.Bold,
                                color = TextPrimary
                            )

                            Spacer(modifier = Modifier.height(6.dp))

                            Text(
                                text = "Supports .ecproj, .json, .otio, .7z, .zip, .xml, and .vnproject",
                                fontSize = 12.sp,
                                color = TextSecondary
                            )

                            Spacer(modifier = Modifier.height(18.dp))

                            // Dual Action Buttons: Browse Device Files & Import Archive
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                NeonButton(
                                    text = "Browse Device Files",
                                    onClick = {
                                        filePickerLauncher.launch(ProjectImportManager.BROAD_MIME_TYPES)
                                    },
                                    icon = Icons.Filled.Description,
                                    modifier = Modifier.weight(1f),
                                    testTag = "browse_files_button"
                                )

                                NeonOutlinedButton(
                                    text = "Import Archive",
                                    onClick = {
                                        filePickerLauncher.launch(ProjectImportManager.BROAD_MIME_TYPES)
                                    },
                                    icon = Icons.Filled.FileDownload,
                                    modifier = Modifier.weight(1f),
                                    testTag = "import_archive_button"
                                )
                            }
                        }
                    }
                }
            }

            // Storage & ZArchiver Workflow Guide
            item {
                Spacer(modifier = Modifier.height(8.dp))
                StorageAndZArchiverGuideCard(
                    isExpanded = isWorkflowGuideExpanded,
                    onToggle = { isWorkflowGuideExpanded = !isWorkflowGuideExpanded }
                )
            }

            // Active Inspection State / Selected File Card
            if (isAnalyzing || selectedFileMeta != null) {
                item {
                    Spacer(modifier = Modifier.height(14.dp))
                    ActiveFileStatusCard(
                        meta = selectedFileMeta,
                        isAnalyzing = isAnalyzing
                    )
                }
            }

            // Analysis Result Inspector Card
            if (analysisResult != null && !isAnalyzing) {
                item {
                    Spacer(modifier = Modifier.height(16.dp))
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

            // Sample Projects Quick Loader (for instant emulator testing)
            item {
                Spacer(modifier = Modifier.height(18.dp))
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
                        fileName = "Cyber_BeatSync_Sample.7z",
                        formatType = "7-Zip Archive (Apache Commons Compress)",
                        isVerified = true,
                        onClick = {
                            isAnalyzing = true
                            selectedFileMeta = FileMetadata("Cyber_BeatSync_Sample.7z", 1024L, "application/x-7z-compressed")
                            coroutineScope.launch {
                                val sample7z = SevenZipProjectExtractor.createSample7zArchive(context)
                                val res = withContext(Dispatchers.IO) {
                                    SevenZipProjectExtractor.extractFromFile(context, sample7z, "Cyber_BeatSync_Sample.7z", sample7z.length())
                                }
                                analysisResult = res
                                isAnalyzing = false
                            }
                        }
                    )

                    SampleFileButton(
                        fileName = "Cyber_Project_Package.zip",
                        formatType = "ZIP Archive (Native Safe Stream)",
                        isVerified = true,
                        onClick = {
                            isAnalyzing = true
                            selectedFileMeta = FileMetadata("Cyber_Project_Package.zip", 1450L, "application/zip")
                            coroutineScope.launch {
                                val sampleZip = SevenZipProjectExtractor.createSampleZipArchive(context)
                                val uri = Uri.fromFile(sampleZip)
                                val res = withContext(Dispatchers.IO) {
                                    ProjectImportManager.inspectAndImport(context, uri)
                                }
                                analysisResult = res
                                isAnalyzing = false
                            }
                        }
                    )

                    SampleFileButton(
                        fileName = "Cyber_Velocity_Preset.ecproj",
                        formatType = "Native EditCopy JSON Blueprint",
                        isVerified = true,
                        onClick = {
                            isAnalyzing = true
                            selectedFileMeta = FileMetadata("Cyber_Velocity_Preset.ecproj", 4200L, "application/json")
                            analysisResult = repository.analyzeProjectImport("Cyber_Velocity_Preset.ecproj")
                            isAnalyzing = false
                        }
                    )

                    SampleFileButton(
                        fileName = "Dance_BeatDrop_Edit.vnproject",
                        formatType = "VN Video Editor Archive (Lab Validation)",
                        isVerified = false,
                        onClick = {
                            isAnalyzing = true
                            selectedFileMeta = FileMetadata("Dance_BeatDrop_Edit.vnproject", 1048576L, "application/octet-stream")
                            coroutineScope.launch {
                                val sampleVn = SevenZipProjectExtractor.createSampleVnProject(context)
                                val uri = Uri.fromFile(sampleVn)
                                val res = withContext(Dispatchers.IO) {
                                    ProjectImportManager.inspectAndImport(context, uri)
                                }
                                analysisResult = res
                                isAnalyzing = false
                            }
                        }
                    )

                    SampleFileButton(
                        fileName = "Documentary_Sequence.otio",
                        formatType = "OpenTimelineIO Interchange",
                        isVerified = true,
                        onClick = {
                            isAnalyzing = true
                            selectedFileMeta = FileMetadata("Documentary_Sequence.otio", 8400L, "application/json")
                            analysisResult = repository.analyzeProjectImport("Documentary_Sequence.otio")
                            isAnalyzing = false
                        }
                    )

                    SampleFileButton(
                        fileName = "Premiere_Cut_Sequence.xml",
                        formatType = "Premiere Pro / FCP XML Sequence",
                        isVerified = true,
                        onClick = {
                            isAnalyzing = true
                            selectedFileMeta = FileMetadata("Premiere_Cut_Sequence.xml", 2800L, "text/xml")
                            coroutineScope.launch {
                                val sampleXml = SevenZipProjectExtractor.createSampleXmlProject(context)
                                val uri = Uri.fromFile(sampleXml)
                                val res = withContext(Dispatchers.IO) {
                                    ProjectImportManager.inspectAndImport(context, uri)
                                }
                                analysisResult = res
                                isAnalyzing = false
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

/**
 * Explains how Android's Document Picker interacts with Internal Storage, Downloads, and ZArchiver.
 */
@Composable
private fun StorageAndZArchiverGuideCard(
    isExpanded: Boolean,
    onToggle: () -> Unit
) {
    GlassCard(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp),
        shapeRadius = 14.dp,
        borderGlowColor = ElectricCyan.copy(alpha = 0.25f),
        backgroundColor = SurfaceNavy.copy(alpha = 0.75f),
        onClick = onToggle
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Filled.Info,
                        contentDescription = null,
                        tint = ElectricCyan,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Accessing Files from ZArchiver & File Managers",
                        color = TextPrimary,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                }
                Text(
                    text = if (isExpanded) "Hide" else "Show",
                    color = ElectricCyan,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold
                )
            }

            AnimatedVisibility(visible = isExpanded) {
                Column(modifier = Modifier.padding(top = 10.dp)) {
                    Text(
                        text = "Android protects files with Scoped Storage. When the picker opens:",
                        color = TextSecondary,
                        fontSize = 12.sp,
                        lineHeight = 16.sp
                    )

                    Spacer(modifier = Modifier.height(6.dp))

                    Text(
                        text = "• In the system file picker, tap the top-left menu (≡) to open the sidebar.",
                        color = TextSecondary,
                        fontSize = 12.sp,
                        lineHeight = 16.sp
                    )

                    Text(
                        text = "• Select 'Internal Storage' (e.g., your device model) or 'Downloads'.",
                        color = TextSecondary,
                        fontSize = 12.sp,
                        lineHeight = 16.sp
                    )

                    Text(
                        text = "• If ZArchiver is not listed as a provider, simply extract the archive in ZArchiver first, then use 'Browse Device Files' to select the extracted .ecproj, .json, or .7z from Downloads.",
                        color = TextSecondary,
                        fontSize = 12.sp,
                        lineHeight = 16.sp
                    )

                    Spacer(modifier = Modifier.height(4.dp))

                    Text(
                        text = "Tip: If Internal Storage does not appear in the picker drawer, tap the 3 dots in the top-right corner of the file picker and select 'Show internal storage'.",
                        color = ElectricCyan,
                        fontSize = 11.sp,
                        lineHeight = 15.sp
                    )
                }
            }
        }
    }
}

/**
 * Displays active file selection metadata and analysis progress.
 */
@Composable
private fun ActiveFileStatusCard(
    meta: FileMetadata?,
    isAnalyzing: Boolean
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp),
        shape = RoundedCornerShape(14.dp),
        color = SurfaceNavyElevated,
        border = BorderStroke(1.dp, CardBorderSubtle)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    modifier = Modifier.weight(1f),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Filled.Description,
                        contentDescription = null,
                        tint = ElectricCyan,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text(
                            text = meta?.displayName ?: "Selected File",
                            color = TextPrimary,
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp,
                            maxLines = 1
                        )
                        val sizeStr = if (meta != null && meta.sizeBytes > 0) {
                            ProjectImportManager.formatFileSize(meta.sizeBytes)
                        } else {
                            "Content Stream"
                        }
                        Text(
                            text = "$sizeStr • ${meta?.displayName?.substringAfterLast('.', "file")?.uppercase() ?: "DATA"}",
                            color = TextSecondary,
                            fontSize = 11.sp
                        )
                    }
                }

                if (isAnalyzing) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(20.dp),
                        color = ElectricCyan,
                        strokeWidth = 2.dp
                    )
                } else {
                    Surface(
                        shape = RoundedCornerShape(6.dp),
                        color = NeonGreen.copy(alpha = 0.15f)
                    ) {
                        Text(
                            text = "Inspected",
                            color = NeonGreen,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }
                }
            }

            if (isAnalyzing) {
                Spacer(modifier = Modifier.height(12.dp))
                LinearProgressIndicator(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(3.dp),
                    color = ElectricCyan,
                    trackColor = DeepBlack
                )
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = "Inspecting archive entries, verifying checksums and timeline schema...",
                    color = TextSecondary,
                    fontSize = 11.sp
                )
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
            Row(
                modifier = Modifier.weight(1f),
                verticalAlignment = Alignment.CenterVertically
            ) {
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
                    Row(
                        modifier = Modifier.weight(1f),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
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

                if (result.fileSizeBytes > 0) {
                    Spacer(modifier = Modifier.height(10.dp))
                    Text(
                        text = "Verified File Size: ${ProjectImportManager.formatFileSize(result.fileSizeBytes)}",
                        color = TextMuted,
                        fontSize = 11.sp
                    )
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
