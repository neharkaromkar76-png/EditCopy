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
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.outlined.StarOutline
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
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
import com.example.data.model.VideoTemplate
import com.example.ui.components.FeatureBadge
import com.example.ui.components.GlassCard
import com.example.ui.components.NeonButton
import com.example.ui.components.NeonOutlinedButton
import com.example.ui.components.TimelineMiniPreview
import com.example.ui.theme.AmberWarning
import com.example.ui.theme.CardBorderSubtle
import com.example.ui.theme.DeepBlack
import com.example.ui.theme.ElectricCyan
import com.example.ui.theme.ElectricMagenta
import com.example.ui.theme.NeonGreen
import com.example.ui.theme.NeonViolet
import com.example.ui.theme.RedError
import com.example.ui.theme.SurfaceNavy
import com.example.ui.theme.SurfaceNavyElevated
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary

enum class LibraryTab(val displayName: String) {
    ALL("All Work"),
    SAVED("Saved Templates"),
    DRAFTS("Drafts & Customs"),
    FAVORITES("Favorites")
}

@Composable
fun LibraryScreen(
    templates: List<VideoTemplate>,
    onSelectTemplate: (String) -> Unit,
    onToggleFavorite: (String, Boolean) -> Unit,
    onRenameTemplate: (String, String) -> Unit,
    onDuplicateTemplate: (VideoTemplate) -> Unit,
    onDeleteTemplate: (String) -> Unit,
    onCreateNewTemplate: () -> Unit,
    modifier: Modifier = Modifier
) {
    var selectedTab by remember { mutableStateOf(LibraryTab.ALL) }
    var searchQuery by remember { mutableStateOf("") }

    // Dialog state for rename
    var templateToRename by remember { mutableStateOf<VideoTemplate?>(null) }
    var renameInput by remember { mutableStateOf("") }

    // Dialog state for delete confirmation
    var templateToDelete by remember { mutableStateOf<VideoTemplate?>(null) }

    val filteredList = remember(templates, selectedTab, searchQuery) {
        templates.filter { tpl ->
            val matchesTab = when (selectedTab) {
                LibraryTab.ALL -> true
                LibraryTab.SAVED -> !tpl.isUserCreated
                LibraryTab.DRAFTS -> tpl.isUserCreated
                LibraryTab.FAVORITES -> tpl.isFavorite
            }
            val matchesSearch = if (searchQuery.isBlank()) true else {
                tpl.title.contains(searchQuery, ignoreCase = true) ||
                        tpl.description.contains(searchQuery, ignoreCase = true)
            }
            matchesTab && matchesSearch
        }
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(DeepBlack)
            .statusBarsPadding()
            .testTag("library_screen")
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            // Header
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 14.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "My Creative Vault",
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary,
                        letterSpacing = (-0.5).sp
                    )
                    Text(
                        text = "Your reusable video editing templates & recipes",
                        fontSize = 12.sp,
                        color = TextSecondary
                    )
                }

                IconButton(
                    onClick = onCreateNewTemplate,
                    modifier = Modifier.testTag("library_create_button")
                ) {
                    Icon(
                        imageVector = Icons.Filled.Add,
                        contentDescription = "New Template",
                        tint = ElectricCyan
                    )
                }
            }

            // Search input
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp),
                placeholder = { Text("Search your vault...", color = TextMuted) },
                leadingIcon = {
                    Icon(imageVector = Icons.Filled.Search, contentDescription = null, tint = ElectricCyan)
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

            // Tabs Row
            LazyRow(
                contentPadding = PaddingValues(horizontal = 20.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                items(LibraryTab.values()) { tab ->
                    val isSelected = selectedTab == tab
                    FilterChip(
                        selected = isSelected,
                        onClick = { selectedTab = tab },
                        label = {
                            Text(
                                text = tab.displayName,
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
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Items List
            if (filteredList.isEmpty()) {
                EmptyLibraryView(
                    tab = selectedTab,
                    onCreateNew = onCreateNewTemplate
                )
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(start = 20.dp, end = 20.dp, bottom = 96.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(filteredList, key = { it.id }) { template ->
                        LibraryItemCard(
                            template = template,
                            onClick = { onSelectTemplate(template.id) },
                            onToggleFavorite = { onToggleFavorite(template.id, template.isFavorite) },
                            onRename = {
                                templateToRename = template
                                renameInput = template.title
                            },
                            onDuplicate = { onDuplicateTemplate(template) },
                            onDelete = { templateToDelete = template }
                        )
                    }
                }
            }
        }

        // Rename Dialog
        if (templateToRename != null) {
            AlertDialog(
                onDismissRequest = { templateToRename = null },
                confirmButton = {
                    NeonButton(
                        text = "Rename",
                        onClick = {
                            if (renameInput.isNotBlank()) {
                                onRenameTemplate(templateToRename!!.id, renameInput.trim())
                            }
                            templateToRename = null
                        }
                    )
                },
                dismissButton = {
                    TextButton(onClick = { templateToRename = null }) {
                        Text("Cancel", color = TextSecondary)
                    }
                },
                containerColor = SurfaceNavyElevated,
                title = { Text("Rename Template", color = TextPrimary, fontWeight = FontWeight.Bold) },
                text = {
                    OutlinedTextField(
                        value = renameInput,
                        onValueChange = { renameInput = it },
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
            )
        }

        // Delete Confirmation Dialog
        if (templateToDelete != null) {
            AlertDialog(
                onDismissRequest = { templateToDelete = null },
                confirmButton = {
                    TextButton(
                        onClick = {
                            onDeleteTemplate(templateToDelete!!.id)
                            templateToDelete = null
                        }
                    ) {
                        Text("Delete", color = RedError, fontWeight = FontWeight.Bold)
                    }
                },
                dismissButton = {
                    TextButton(onClick = { templateToDelete = null }) {
                        Text("Cancel", color = TextSecondary)
                    }
                },
                containerColor = SurfaceNavyElevated,
                title = { Text("Delete Template?", color = TextPrimary, fontWeight = FontWeight.Bold) },
                text = {
                    Text(
                        text = "Are you sure you want to remove \"${templateToDelete!!.title}\" from your library? This action cannot be undone.",
                        color = TextSecondary,
                        fontSize = 13.sp
                    )
                }
            )
        }
    }
}

@Composable
private fun LibraryItemCard(
    template: VideoTemplate,
    onClick: () -> Unit,
    onToggleFavorite: () -> Unit,
    onRename: () -> Unit,
    onDuplicate: () -> Unit,
    onDelete: () -> Unit
) {
    var showMenu by remember { mutableStateOf(false) }

    GlassCard(
        modifier = Modifier.fillMaxWidth(),
        shapeRadius = 14.dp,
        backgroundColor = SurfaceNavy,
        onClick = onClick
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                    Box(
                        modifier = Modifier
                            .size(42.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .background(
                                Brush.linearGradient(
                                    listOf(
                                        Color(template.primaryAccentColorHex),
                                        Color(template.secondaryAccentColorHex)
                                    )
                                )
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(imageVector = Icons.Filled.PlayArrow, contentDescription = null, tint = DeepBlack, modifier = Modifier.size(22.dp))
                    }

                    Spacer(modifier = Modifier.width(12.dp))

                    Column {
                        Text(
                            text = template.title,
                            color = TextPrimary,
                            fontWeight = FontWeight.Bold,
                            fontSize = 15.sp,
                            maxLines = 1
                        )
                        Text(
                            text = "${template.aspectRatio} • ${String.format("%.1f", template.durationSeconds)}s • ${template.cutsCount} Cuts",
                            color = TextSecondary,
                            fontSize = 12.sp
                        )
                    }
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconButton(onClick = onToggleFavorite) {
                        Icon(
                            imageVector = if (template.isFavorite) Icons.Filled.Star else Icons.Outlined.StarOutline,
                            contentDescription = "Favorite",
                            tint = if (template.isFavorite) AmberWarning else TextMuted,
                            modifier = Modifier.size(20.dp)
                        )
                    }

                    Box {
                        IconButton(onClick = { showMenu = true }) {
                            Icon(imageVector = Icons.Filled.MoreVert, contentDescription = "Options", tint = TextMuted)
                        }

                        DropdownMenu(
                            expanded = showMenu,
                            onDismissRequest = { showMenu = false },
                            modifier = Modifier.background(SurfaceNavyElevated)
                        ) {
                            DropdownMenuItem(
                                text = { Text("Rename", color = TextPrimary) },
                                onClick = {
                                    showMenu = false
                                    onRename()
                                },
                                leadingIcon = { Icon(Icons.Filled.Edit, null, tint = ElectricCyan) }
                            )

                            DropdownMenuItem(
                                text = { Text("Duplicate", color = TextPrimary) },
                                onClick = {
                                    showMenu = false
                                    onDuplicate()
                                },
                                leadingIcon = { Icon(Icons.Filled.ContentCopy, null, tint = NeonViolet) }
                            )

                            DropdownMenuItem(
                                text = { Text("Delete", color = RedError) },
                                onClick = {
                                    showMenu = false
                                    onDelete()
                                },
                                leadingIcon = { Icon(Icons.Filled.Delete, null, tint = RedError) }
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            TimelineMiniPreview(
                cutsCount = template.cutsCount,
                keyframesCount = template.keyframesCount,
                durationSeconds = template.durationSeconds,
                modifier = Modifier.height(18.dp)
            )
        }
    }
}

@Composable
private fun EmptyLibraryView(
    tab: LibraryTab,
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
            modifier = Modifier.size(72.dp),
            shape = CircleShape,
            color = SurfaceNavyElevated
        ) {
            Box(contentAlignment = Alignment.Center) {
                Icon(
                    imageVector = Icons.Filled.Search,
                    contentDescription = null,
                    tint = TextMuted,
                    modifier = Modifier.size(32.dp)
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = "No items in ${tab.displayName}",
            color = TextPrimary,
            fontSize = 17.sp,
            fontWeight = FontWeight.Bold
        )

        Spacer(modifier = Modifier.height(6.dp))

        Text(
            text = "Create a custom template or save templates to your vault.",
            color = TextSecondary,
            fontSize = 13.sp
        )

        Spacer(modifier = Modifier.height(20.dp))

        NeonButton(
            text = "Create New Template",
            onClick = onCreateNew,
            icon = Icons.Filled.Add
        )
    }
}
