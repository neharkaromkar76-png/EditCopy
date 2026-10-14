package com.example.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "templates")
data class TemplateEntity(
    @PrimaryKey val id: String,
    val title: String,
    val description: String,
    val categoryName: String,
    val durationSeconds: Float,
    val aspectRatio: String,
    val cutsCount: Int,
    val keyframesCount: Int,
    val zoomCount: Int,
    val hasBeatSync: Boolean,
    val hasAudioTrack: Boolean,
    val creator: String,
    val isFavorite: Boolean,
    val isUserCreated: Boolean,
    val tagsCsv: String,
    val primaryAccentColorHex: Long,
    val secondaryAccentColorHex: Long,
    val bpm: Int,
    val slotsJson: String,
    val updatedAtTimestamp: Long = System.currentTimeMillis()
)
