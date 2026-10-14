package com.example.ui.screens

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
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.outlined.StarOutline
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
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
import com.example.data.model.AppScreen
import com.example.data.model.TemplateCategory
import com.example.data.model.VideoTemplate
import com.example.ui.components.FeatureBadge
import com.example.ui.components.GlassCard
import com.example.ui.components.NeonButton
import com.example.ui.components.TimelineMiniPreview
import com.example.ui.theme.AmberWarning
import com.example.ui.theme.CardBorderSubtle
import com.example.ui.theme.DeepBlack
import com.example.ui.theme.ElectricCyan
import com.example.ui.theme.ElectricMagenta
import com.example.ui.theme.NeonViolet
import com.example.ui.theme.SurfaceNavy
import com.example.ui.theme.SurfaceNavyElevated
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary

enum class TemplateSortOption(val displayName: String) {
    RECOMMENDED("Recommended"),
    DURATION_ASC("Shortest Duration"),
    CUTS_DESC("Most Cuts"),
    FAVORITES_FIRST("Favorites First")
}

@Composable
fun TemplateLibraryScreen(
    templates: List<VideoTemplate>,
    onSelectTemplate: (String) -> Unit,
    onToggleFavorite: (String, Boolean) -> Unit,
    onCreateNewTemplate: () -> Unit,
    modifier: Modifier = Modifier
) {
    var searchQuery by remember { mutableStateOf("") }
    var selectedCategory by remember { mutableStateOf(TemplateCategory.ALL) }
    var currentSort by remember { mutableStateOf(TemplateSortOption.RECOMMENDED) }
    var showOnlyFavorites by remember { mutableStateOf(false) }

    val filteredTemplates = remember(templates, searchQuery, selectedCategory, currentSort, showOnlyFavorites) {
        var list = templates.filter { tpl ->
            val matchesCategory = when (selectedCategory) {
                TemplateCategory.ALL -> true
                TemplateCategory.CUSTOM -> tpl.isUserCreated
                else -> tpl.category == selectedCategory
            }
            val matchesFavorite = if (showOnlyFavorites) tpl.isFavorite else true
            val matchesSearch = if (searchQuery.isBlank()) true else {
                tpl.title.contains(searchQuery, ignoreCase = true) ||
                        tpl.description.contains(searchQuery, ignoreCase = true) ||
                        tpl.tags.any { it.contains(searchQuery, ignoreCase = true) }
            }
            matchesCategory && matchesFavorite && matchesSearch
        }

        when (currentSort) {
            TemplateSortOption.RECOMMENDED -> list
            TemplateSortOption.DURATION_ASC -> list.sortedBy { it.durationSeconds }
            TemplateSortOption.CUTS_DESC -> list.sortedByDescending { it.cutsCount }
            TemplateSortOption.FAVORITES_FIRST -> list.sortedByDescending { it.isFavorite }
        }
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(DeepBlack)
            .statusBarsPadding()
            .testTag("template_library_screen")
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            // Screen Header
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 14.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "Template Library",
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary,
                        letterSpacing = (-0.5).sp
                    )
                    Text(
                        text = "Reusable cuts, speed ramps, and keyframe blueprints",
                        fontSize = 12.sp,
                        color = TextSecondary
                    )
                }

                IconButton(
                    onClick = {
                        // Toggle sorting cycle
                        currentSort = when (currentSort) {
                            TemplateSortOption.RECOMMENDED -> TemplateSortOption.CUTS_DESC
                            TemplateSortOption.CUTS_DESC -> TemplateSortOption.DURATION_ASC
                            TemplateSortOption.DURATION_ASC -> TemplateSortOption.FAVORITES_FIRST
                            TemplateSortOption.FAVORITES_FIRST -> TemplateSortOption.RECOMMENDED
                        }
                    },
                    modifier = Modifier.testTag("sort_templates_button")
                ) {
                    Icon(
                        imageVector = Icons.Filled.FilterList,
                        contentDescription = "Sort",
                        tint = ElectricCyan
                    )
                }
            }

            // Search Bar
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp)
                    .testTag("search_templates_input"),
                placeholder = { Text("Search templates, cuts, BPM...", color = TextMuted) },
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Filled.Search,
                        contentDescription = null,
                        tint = ElectricCyan
                    )
                },
                trailingIcon = {
                    if (searchQuery.isNotEmpty()) {
                        IconButton(onClick = { searchQuery = "" }) {
                            Icon(imageVector = Icons.Filled.Clear, contentDescription = "Clear", tint = TextMuted)
                        }
                    }
                },
                singleLine = true,
                shape = RoundedCornerShape(14.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedTextColor = TextPrimary,
                    unfocusedTextColor = TextPrimary,
                    focusedContainerColor = SurfaceNavy,
                    unfocusedContainerColor = SurfaceNavy,
                    focusedBorderColor = ElectricCyan,
                    unfocusedBorderColor = CardBorderSubtle,
                    cursorColor = ElectricCyan
                )
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Filter Chips Carousel
            LazyRow(
                contentPadding = PaddingValues(horizontal = 20.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                items(TemplateCategory.values()) { category ->
                    val isSelected = selectedCategory == category
                    FilterChip(
                        selected = isSelected,
                        onClick = { selectedCategory = category },
                        label = {
                            Text(
                                text = category.displayName,
                                fontSize = 12.sp,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                            )
                        },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = ElectricCyan.copy(alpha = 0.2f),
                            selectedLabelColor = ElectricCyan,
                            containerColor = SurfaceNavyElevated,
                            labelColor = TextSecondary
                        ),
                        border = FilterChipDefaults.filterChipBorder(
                            borderColor = CardBorderSubtle,
                            selectedBorderColor = ElectricCyan,
                            borderWidth = 1.dp,
                            selectedBorderWidth = 1.2.dp,
                            enabled = true,
                            selected = isSelected
                        )
                    )
                }

                // Favorites Quick Toggle Chip
                item {
                    FilterChip(
                        selected = showOnlyFavorites,
                        onClick = { showOnlyFavorites = !showOnlyFavorites },
                        leadingIcon = {
                            Icon(
                                imageVector = Icons.Filled.Star,
                                contentDescription = null,
                                tint = if (showOnlyFavorites) AmberWarning else TextMuted,
                                modifier = Modifier.size(16.dp)
                            )
                        },
                        label = {
                            Text(
                                text = "Favorites",
                                fontSize = 12.sp,
                                fontWeight = if (showOnlyFavorites) FontWeight.Bold else FontWeight.Normal
                            )
                        },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = AmberWarning.copy(alpha = 0.2f),
                            selectedLabelColor = AmberWarning,
                            containerColor = SurfaceNavyElevated,
                            labelColor = TextSecondary
                        ),
                        border = FilterChipDefaults.filterChipBorder(
                            borderColor = CardBorderSubtle,
                            selectedBorderColor = AmberWarning,
                            borderWidth = 1.dp,
                            selectedBorderWidth = 1.2.dp,
                            enabled = true,
                            selected = showOnlyFavorites
                        )
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Subtitle with current filter count and sorting badge
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 4.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "${filteredTemplates.size} Templates Available",
                    color = TextMuted,
                    fontSize = 12.sp
                )
                Text(
                    text = "Sort: ${currentSort.displayName}",
                    color = ElectricCyan,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Medium
                )
            }

            // Template Cards List
            if (filteredTemplates.isEmpty()) {
                EmptyTemplatesState(
                    onReset = {
                        searchQuery = ""
                        selectedCategory = TemplateCategory.ALL
                        showOnlyFavorites = false
                    },
                    onCreateNew = onCreateNewTemplate
                )
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(start = 20.dp, end = 20.dp, top = 8.dp, bottom = 96.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    items(filteredTemplates, key = { it.id }) { template ->
                        TemplateCard(
                            template = template,
                            onClick = { onSelectTemplate(template.id) },
                            onToggleFavorite = { onToggleFavorite(template.id, template.isFavorite) }
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun TemplateCard(
    template: VideoTemplate,
    onClick: () -> Unit,
    onToggleFavorite: () -> Unit
) {
    val accentColor = Color(template.primaryAccentColorHex)

    GlassCard(
        modifier = Modifier.fillMaxWidth(),
        shapeRadius = 18.dp,
        borderGlowColor = accentColor.copy(alpha = 0.35f),
        backgroundColor = SurfaceNavy,
        onClick = onClick
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            // Top Preview Area
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(140.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(
                        Brush.linearGradient(
                            listOf(
                                Color(template.primaryAccentColorHex).copy(alpha = 0.35f),
                                Color(template.secondaryAccentColorHex).copy(alpha = 0.2f),
                                DeepBlack
                            )
                        )
                    )
            ) {
                // Top tag: Category & Aspect Ratio
                Row(
                    modifier = Modifier
                        .align(Alignment.TopStart)
                        .padding(10.dp),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Surface(
                        shape = RoundedCornerShape(6.dp),
                        color = DeepBlack.copy(alpha = 0.8f)
                    ) {
                        Text(
                            text = template.category.displayName,
                            color = ElectricCyan,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                        )
                    }

                    Surface(
                        shape = RoundedCornerShape(6.dp),
                        color = DeepBlack.copy(alpha = 0.8f)
                    ) {
                        Text(
                            text = "${template.aspectRatio} • ${String.format("%.1f", template.durationSeconds)}s",
                            color = Color.White,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Medium,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                        )
                    }
                }

                // Favorite Button
                Box(
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(8.dp)
                        .size(34.dp)
                        .clip(CircleShape)
                        .background(DeepBlack.copy(alpha = 0.7f))
                        .clickable(onClick = onToggleFavorite),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = if (template.isFavorite) Icons.Filled.Star else Icons.Outlined.StarOutline,
                        contentDescription = "Favorite",
                        tint = if (template.isFavorite) AmberWarning else TextSecondary,
                        modifier = Modifier.size(18.dp)
                    )
                }

                // Center Play Button Overlay
                Box(
                    modifier = Modifier
                        .align(Alignment.Center)
                        .size(44.dp)
                        .clip(CircleShape)
                        .background(ElectricCyan.copy(alpha = 0.25f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Filled.PlayArrow,
                        contentDescription = "Play",
                        tint = ElectricCyan,
                        modifier = Modifier.size(26.dp)
                    )
                }

                // Interactive Mini Timeline visualization embedded inside
                TimelineMiniPreview(
                    cutsCount = template.cutsCount,
                    keyframesCount = template.keyframesCount,
                    durationSeconds = template.durationSeconds,
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .padding(8.dp)
                        .height(24.dp)
                )
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Title & Creator
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = template.title,
                    fontSize = 17.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextPrimary,
                    modifier = Modifier.weight(1f)
                )

                Text(
                    text = "by ${template.creator}",
                    fontSize = 11.sp,
                    color = TextMuted
                )
            }

            Spacer(modifier = Modifier.height(6.dp))

            Text(
                text = template.description,
                fontSize = 12.sp,
                color = TextSecondary,
                lineHeight = 17.sp
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Supported Features Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                FeatureBadge(label = "${template.cutsCount} Cuts", accentColor = ElectricCyan)
                FeatureBadge(label = "${template.keyframesCount} Keyframes", accentColor = NeonViolet)
                FeatureBadge(label = "${template.zoomCount} Zooms", accentColor = ElectricMagenta)
                if (template.hasBeatSync) {
                    FeatureBadge(label = "Sync ${template.bpm} BPM", accentColor = Color(0xFF00E676))
                }
            }
        }
    }
}

@Composable
private fun EmptyTemplatesState(
    onReset: () -> Unit,
    onCreateNew: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Surface(
            modifier = Modifier.size(80.dp),
            shape = CircleShape,
            color = SurfaceNavyElevated
        ) {
            Box(contentAlignment = Alignment.Center) {
                Icon(
                    imageVector = Icons.Filled.Search,
                    contentDescription = null,
                    tint = TextMuted,
                    modifier = Modifier.size(36.dp)
                )
            }
        }

        Spacer(modifier = Modifier.height(18.dp))

        Text(
            text = "No matching templates",
            color = TextPrimary,
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold
        )

        Spacer(modifier = Modifier.height(6.dp))

        Text(
            text = "Try adjusting your search query, clearing filters, or create a new custom template.",
            color = TextSecondary,
            fontSize = 13.sp,
            lineHeight = 18.sp
        )

        Spacer(modifier = Modifier.height(20.dp))

        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            TextButton(onClick = onReset) {
                Text("Reset Filters", color = ElectricCyan)
            }

            NeonButton(
                text = "Create Template",
                onClick = onCreateNew,
                icon = Icons.Filled.Add
            )
        }
    }
}
