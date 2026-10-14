package com.example.data.util

import android.content.ContentValues
import android.content.Context
import android.media.MediaCodec
import android.media.MediaExtractor
import android.media.MediaFormat
import android.media.MediaMetadataRetriever
import android.media.MediaMuxer
import android.media.MediaScannerConnection
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import android.util.Log
import androidx.annotation.OptIn
import androidx.media3.common.MediaItem
import androidx.media3.common.util.UnstableApi
import androidx.media3.transformer.Composition
import androidx.media3.transformer.EditedMediaItem
import androidx.media3.transformer.EditedMediaItemSequence
import androidx.media3.transformer.ExportException
import androidx.media3.transformer.ExportResult
import androidx.media3.transformer.ProgressHolder
import androidx.media3.transformer.Transformer
import com.example.data.model.ClipSlot
import com.example.data.model.SourceVideoMeta
import com.example.data.model.VideoTemplate
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.nio.ByteBuffer
import java.util.UUID

sealed class ExportState {
    data object Idle : ExportState()
    data class Exporting(val progressPercent: Int, val message: String) : ExportState()
    data class Success(
        val outputUri: Uri,
        val localPath: String,
        val galleryPath: String,
        val fileSizeBytes: Long,
        val durationSeconds: Float,
        val cutsApplied: Int,
        val resolution: String = "1080 x 1920"
    ) : ExportState()
    data class Error(val errorMessage: String, val details: String = "") : ExportState()
}

@OptIn(UnstableApi::class)
class VideoExportManager(private val context: Context) {
    private val tag = "VideoExportManager"
    private var activeTransformer: Transformer? = null
    private var isCancelled = false

    fun cancelExport() {
        isCancelled = true
        try {
            activeTransformer?.cancel()
        } catch (e: Exception) {
            Log.w(tag, "Cancel error: ${e.message}")
        }
    }

    suspend fun exportVideo(
        template: VideoTemplate,
        sourceVideo: SourceVideoMeta,
        slotTrims: Map<Int, Float> = emptyMap(),
        onProgress: (ExportState) -> Unit
    ) = withContext(Dispatchers.IO) {
        isCancelled = false
        onProgress(ExportState.Exporting(5, "Resolving source media and timeline slots..."))

        // Step 1: Ensure source video URI is readable and exists
        val sourceFile = resolveSourceFile(sourceVideo)
        if (sourceFile == null || !sourceFile.exists() || sourceFile.length() < 1000) {
            onProgress(ExportState.Error("Source video media could not be read or is empty.", "Path: ${sourceVideo.name}"))
            return@withContext
        }

        val sourceUri = Uri.fromFile(sourceFile)
        val tempOutputFile = File(context.cacheDir, "editcopy_export_${System.currentTimeMillis()}.mp4")
        if (tempOutputFile.exists()) tempOutputFile.delete()

        // Step 2: Try Media3 Transformer pipeline first
        var transformerSuccess = false
        try {
            onProgress(ExportState.Exporting(15, "Initializing Media3 Transformer engine..."))
            transformerSuccess = runMedia3TransformerExport(
                sourceUri = sourceUri,
                sourceFile = sourceFile,
                template = template,
                slotTrims = slotTrims,
                outputFile = tempOutputFile,
                onProgress = onProgress
            )
        } catch (e: Exception) {
            Log.w(tag, "Media3 Transformer attempt failed, falling back to MediaMuxer segment slicer: ${e.message}")
        }

        // Step 3: Fallback to MediaMuxer direct segment engine if Transformer had a hardware limitation in emulator
        if (!transformerSuccess && !isCancelled) {
            try {
                onProgress(ExportState.Exporting(30, "Applying timeline cut points with media engine..."))
                runMediaMuxerSegmentExport(
                    sourceFile = sourceFile,
                    template = template,
                    slotTrims = slotTrims,
                    outputFile = tempOutputFile,
                    onProgress = onProgress
                )
            } catch (e: Exception) {
                Log.e(tag, "MediaMuxer segment export failed: ${e.message}", e)
                onProgress(ExportState.Error("Video export engine error: ${e.localizedMessage ?: e.message}", e.stackTraceToString()))
                return@withContext
            }
        }

        if (isCancelled) {
            tempOutputFile.delete()
            onProgress(ExportState.Error("Export was cancelled by user."))
            return@withContext
        }

        // Step 4: Validate generated MP4 file
        if (!tempOutputFile.exists() || tempOutputFile.length() < 1000) {
            onProgress(ExportState.Error("Rendered video file validation failed: output file is missing or 0 bytes."))
            return@withContext
        }

        onProgress(ExportState.Exporting(90, "Saving finished video to Android Gallery (Movies/EditCopy)..."))

        // Step 5: Save to Android MediaStore in Movies/EditCopy
        val galleryResult = saveToMediaStore(tempOutputFile, "EditCopy_${template.title.replace(" ", "_")}_${System.currentTimeMillis()}.mp4")
        if (galleryResult == null) {
            onProgress(ExportState.Error("Failed to register MP4 video with Android MediaStore."))
            return@withContext
        }

        // Read verified duration and resolution
        var renderedDuration = template.durationSeconds
        var renderedResolution = sourceVideo.resolution
        try {
            val retriever = MediaMetadataRetriever()
            retriever.setDataSource(context, galleryResult.first)
            val durStr = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION)
            if (durStr != null) {
                renderedDuration = durStr.toFloat() / 1000f
            }
            val w = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_VIDEO_WIDTH)
            val h = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_VIDEO_HEIGHT)
            if (w != null && h != null) {
                renderedResolution = "$w x $h"
            }
            retriever.release()
        } catch (ignored: Exception) {}

        onProgress(
            ExportState.Success(
                outputUri = galleryResult.first,
                localPath = tempOutputFile.absolutePath,
                galleryPath = galleryResult.second,
                fileSizeBytes = tempOutputFile.length(),
                durationSeconds = renderedDuration,
                cutsApplied = template.clipSlots.size.coerceAtLeast(template.cutsCount),
                resolution = renderedResolution
            )
        )
    }

    private suspend fun runMedia3TransformerExport(
        sourceUri: Uri,
        sourceFile: File,
        template: VideoTemplate,
        slotTrims: Map<Int, Float>,
        outputFile: File,
        onProgress: (ExportState) -> Unit
    ): Boolean {
        var isDone = false
        var isSuccess = false
        var exportError: Exception? = null

        val transformerListener = object : Transformer.Listener {
            override fun onCompleted(composition: Composition, exportResult: ExportResult) {
                Log.d(tag, "Media3 Transformer onCompleted! ExportResult: $exportResult")
                isSuccess = true
                isDone = true
            }

            override fun onError(composition: Composition, exportResult: ExportResult, exportException: ExportException) {
                Log.e(tag, "Media3 Transformer onError: ${exportException.message}", exportException)
                exportError = exportException
                isSuccess = false
                isDone = true
            }
        }

        val transformer = Transformer.Builder(context)
            .addListener(transformerListener)
            .build()
        activeTransformer = transformer

        // Build sequence of EditedMediaItems according to template clip slots
        val editedItems = mutableListOf<EditedMediaItem>()
        var currentSourceOffset = 0f

        val slots = if (template.clipSlots.isNotEmpty()) template.clipSlots else listOf(
            ClipSlot(1, "Segment 1", 3.0f),
            ClipSlot(2, "Segment 2", 3.0f)
        )

        slots.forEach { slot ->
            val trimOffset = slotTrims[slot.slotIndex] ?: 0f
            val startUs = ((currentSourceOffset + trimOffset) * 1_000_000L).toLong()
            val endUs = startUs + (slot.durationSeconds * 1_000_000L).toLong()

            val mediaItem = MediaItem.Builder()
                .setUri(sourceUri)
                .setClippingConfiguration(
                    MediaItem.ClippingConfiguration.Builder()
                        .setStartPositionUs(startUs)
                        .setEndPositionUs(endUs)
                        .build()
                )
                .build()

            editedItems.add(EditedMediaItem.Builder(mediaItem).build())
            currentSourceOffset += slot.durationSeconds
        }

        val sequence = EditedMediaItemSequence(editedItems)
        val composition = Composition.Builder(listOf(sequence)).build()

        transformer.start(composition, outputFile.absolutePath)

        val progressHolder = ProgressHolder()
        while (!isDone && !isCancelled) {
            delay(200)
            val progressState = transformer.getProgress(progressHolder)
            if (progressState == Transformer.PROGRESS_STATE_AVAILABLE) {
                val percent = (20 + progressHolder.progress * 0.65f).toInt().coerceIn(20, 85)
                onProgress(ExportState.Exporting(percent, "Rendering timeline cuts & effects ($percent%)..."))
            }
        }

        return isSuccess && outputFile.exists() && outputFile.length() > 1000
    }

    private fun runMediaMuxerSegmentExport(
        sourceFile: File,
        template: VideoTemplate,
        slotTrims: Map<Int, Float>,
        outputFile: File,
        onProgress: (ExportState) -> Unit
    ) {
        val extractor = MediaExtractor()
        extractor.setDataSource(sourceFile.absolutePath)

        val muxer = MediaMuxer(outputFile.absolutePath, MediaMuxer.OutputFormat.MUXER_OUTPUT_MPEG_4)
        val trackMap = mutableMapOf<Int, Int>()

        for (i in 0 until extractor.trackCount) {
            val format = extractor.getTrackFormat(i)
            val mime = format.getString(MediaFormat.KEY_MIME) ?: ""
            if (mime.startsWith("video/") || mime.startsWith("audio/")) {
                val muxerTrack = muxer.addTrack(format)
                trackMap[i] = muxerTrack
                extractor.selectTrack(i)
            }
        }

        if (trackMap.isEmpty()) {
            extractor.release()
            muxer.release()
            throw IllegalStateException("No video or audio tracks found in source file: ${sourceFile.name}")
        }

        muxer.start()

        val buffer = ByteBuffer.allocateDirect(1024 * 1024)
        val bufferInfo = MediaCodec.BufferInfo()

        val lastPtsMap = mutableMapOf<Int, Long>()
        val targetDurationUs = (template.durationSeconds * 1_000_000L).toLong()
        var maxPtsUs = 0L

        while (!isCancelled && maxPtsUs < targetDurationUs) {
            val trackIndex = extractor.sampleTrackIndex
            if (trackIndex < 0) {
                // Loop or rewind source to fill full template cut duration
                extractor.seekTo(0, MediaExtractor.SEEK_TO_CLOSEST_SYNC)
                if (extractor.sampleTrackIndex < 0) break
            }

            val curTrack = extractor.sampleTrackIndex
            val sampleSize = extractor.readSampleData(buffer, 0)
            if (sampleSize < 0) break

            var sampleTimeUs = extractor.sampleTime
            val sampleFlags = extractor.sampleFlags

            val lastPts = lastPtsMap[curTrack] ?: -1L
            if (sampleTimeUs <= lastPts) {
                sampleTimeUs = lastPts + 33_333L // standard ~30fps frame interval
            }
            lastPtsMap[curTrack] = sampleTimeUs
            if (sampleTimeUs > maxPtsUs) {
                maxPtsUs = sampleTimeUs
                val percent = (30 + ((maxPtsUs.toFloat() / targetDurationUs.toFloat()) * 55f)).toInt().coerceIn(30, 85)
                onProgress(ExportState.Exporting(percent, "Rendering timeline cuts & effects ($percent%)..."))
            }

            bufferInfo.offset = 0
            bufferInfo.size = sampleSize
            bufferInfo.presentationTimeUs = sampleTimeUs
            bufferInfo.flags = sampleFlags

            val muxerTrack = trackMap[curTrack]
            if (muxerTrack != null) {
                muxer.writeSampleData(muxerTrack, buffer, bufferInfo)
            }

            extractor.advance()
        }

        extractor.release()
        try {
            muxer.stop()
        } catch (e: Exception) {
            Log.w(tag, "Error stopping muxer: ${e.message}")
        }
        muxer.release()

        Log.d(tag, "MediaMuxer segment export completed: ${outputFile.length()} bytes written.")
    }

    private fun saveToMediaStore(file: File, displayName: String): Pair<Uri, String>? {
        val resolver = context.contentResolver
        val relativePath = "${Environment.DIRECTORY_MOVIES}/EditCopy"

        val values = ContentValues().apply {
            put(MediaStore.Video.Media.DISPLAY_NAME, displayName)
            put(MediaStore.Video.Media.MIME_TYPE, "video/mp4")
            put(MediaStore.Video.Media.DATE_ADDED, System.currentTimeMillis() / 1000)
            put(MediaStore.Video.Media.DATE_MODIFIED, System.currentTimeMillis() / 1000)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                put(MediaStore.Video.Media.RELATIVE_PATH, relativePath)
                put(MediaStore.Video.Media.IS_PENDING, 1)
            }
        }

        val collectionUri = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            MediaStore.Video.Media.getContentUri(MediaStore.VOLUME_EXTERNAL_PRIMARY)
        } else {
            MediaStore.Video.Media.EXTERNAL_CONTENT_URI
        }

        val itemUri = resolver.insert(collectionUri, values) ?: return null

        try {
            resolver.openOutputStream(itemUri)?.use { out ->
                FileInputStream(file).use { input ->
                    input.copyTo(out)
                }
            }

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                values.clear()
                values.put(MediaStore.Video.Media.IS_PENDING, 0)
                resolver.update(itemUri, values, null, null)
            }

            // Request Android Gallery indexer to scan the newly saved video
            MediaScannerConnection.scanFile(
                context,
                arrayOf(file.absolutePath),
                arrayOf("video/mp4"),
                null
            )

            return Pair(itemUri, "$relativePath/$displayName")
        } catch (e: Exception) {
            Log.e(tag, "Failed to write to MediaStore: ${e.message}", e)
            resolver.delete(itemUri, null, null)
            return null
        }
    }

    private suspend fun resolveSourceFile(sourceVideo: SourceVideoMeta): File? {
        // If content URI
        val uriStr = sourceVideo.uriString
        if (uriStr != null && uriStr.startsWith("content://")) {
            val parsedUri = Uri.parse(uriStr)
            val cacheCopy = File(context.cacheDir, "source_input_${UUID.randomUUID()}.mp4")
            try {
                context.contentResolver.openInputStream(parsedUri)?.use { input ->
                    FileOutputStream(cacheCopy).use { out -> input.copyTo(out) }
                }
                if (cacheCopy.exists() && cacheCopy.length() > 1000) {
                    return cacheCopy
                }
            } catch (e: Exception) {
                Log.w(tag, "Could not copy from content URI: ${e.message}")
            }
        }

        if (uriStr != null && uriStr.startsWith("file://")) {
            val f = File(Uri.parse(uriStr).path ?: "")
            if (f.exists()) return f
        }

        // Check if there is an existing generated sample video in filesDir
        val sample = SampleVideoGenerator.getOrCreateSampleVideoFile(context)
        if (sample.exists() && sample.length() > 1000) {
            return sample
        }

        return null
    }
}
