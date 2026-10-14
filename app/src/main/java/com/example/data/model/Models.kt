package com.example.data.model

import java.util.UUID

enum class TemplateCategory(val displayName: String) {
    ALL("All"),
    TRENDING("Trending"),
    CINEMATIC("Cinematic"),
    BEAT_SYNC("Beat Sync"),
    ZOOM("Zoom"),
    REELS("Reels"),
    SHORTS("Shorts"),
    CUSTOM("Custom")
}

data class ClipSlot(
    val slotIndex: Int,
    val label: String,
    val durationSeconds: Float,
    val transitionIn: String? = null,
    val speedFactor: Float = 1.0f,
    val keyframeEffect: String? = null
)

data class KeyframePoint(
    val timeOffsetMs: Long,
    val property: String, // "Zoom", "Position X", "Rotation", "Speed"
    val targetValue: String
)

data class VideoTemplate(
    val id: String = UUID.randomUUID().toString(),
    val title: String,
    val description: String,
    val category: TemplateCategory,
    val durationSeconds: Float,
    val aspectRatio: String = "9:16", // "9:16", "16:9", "1:1"
    val cutsCount: Int,
    val keyframesCount: Int,
    val zoomCount: Int,
    val hasBeatSync: Boolean = true,
    val hasAudioTrack: Boolean = true,
    val creator: String = "EditCopy Studio",
    val tags: List<String> = emptyList(),
    val isFavorite: Boolean = false,
    val isUserCreated: Boolean = false,
    val clipSlots: List<ClipSlot> = emptyList(),
    val primaryAccentColorHex: Long = 0xFF00F0FF,
    val secondaryAccentColorHex: Long = 0xFF7C4DFF,
    val bpm: Int = 128
)

data class SourceVideoMeta(
    val id: String = UUID.randomUUID().toString(),
    val name: String,
    val uriString: String? = null,
    val durationSeconds: Float,
    val resolution: String = "1080 x 1920",
    val aspectRatio: String = "9:16",
    val fileSizeBytes: Long = 24_500_000,
    val fps: Int = 60,
    val isSample: Boolean = false
)

enum class ImportFormatSupportStatus {
    SUPPORTED,
    STRUCTURAL_ONLY,
    TESTING_LAB_ONLY,
    UNSUPPORTED
}

data class ProjectFormatInfo(
    val formatName: String,
    val fileExtension: String,
    val status: ImportFormatSupportStatus,
    val description: String,
    val note: String
)

data class ImportAnalysisResult(
    val fileName: String,
    val detectedFormat: String,
    val isCompatible: Boolean,
    val statusBadge: String,
    val summaryNotice: String,
    val extractedCuts: Int = 0,
    val extractedKeyframes: Int = 0,
    val extractedAudioBeats: Int = 0,
    val rawDetails: String = "",
    val suggestedTemplate: VideoTemplate? = null,
    val fileSizeBytes: Long = 0L
)

sealed class AppScreen {
    data object Splash : AppScreen()
    data object Onboarding : AppScreen()
    data object Home : AppScreen()
    data object TemplateLibrary : AppScreen()
    data class TemplateDetails(val templateId: String) : AppScreen()
    data object ImportProject : AppScreen()
    data class VideoPicker(val targetTemplateId: String? = null) : AppScreen()
    data class ApplyWorkflow(
        val templateId: String,
        val initialStep: Int = 1,
        val sourceVideo: SourceVideoMeta? = null
    ) : AppScreen()
    data class TimelineEditor(val templateId: String? = null) : AppScreen()
    data object Library : AppScreen()
    data object ProfileSettings : AppScreen()
}
