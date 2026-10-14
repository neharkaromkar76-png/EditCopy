package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
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
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.FileDownload
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.outlined.StarOutline
import androidx.compose.material.ripple.rememberRipple
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
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
import com.example.ui.components.EditCopyLogo
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
import com.example.ui.theme.GlassBorder
import com.example.ui.theme.NeonGreen
import com.example.ui.theme.NeonViolet
import com.example.ui.theme.SurfaceNavy
import com.example.ui.theme.SurfaceNavyElevated
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary

@Composable
fun HomeScreen(
    templates: List<VideoTemplate>,
    onNavigate: (AppScreen) -> Unit,
    onToggleFavorite: (String, Boolean) -> Unit,
    modifier: Modifier = Modifier
) {
    var showSearchDialog by remember { mutableStateOf(false) }
    var showNotificationsDialog by remember { mutableStateOf(false) }
    var searchQuery by remember { mutableStateOf("") }

    val featuredTemplates = remember(templates) {
        templates.take(5)
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(DeepBlack)
            .statusBarsPadding()
            .testTag("home_screen")
    ) {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(bottom = 96.dp)
        ) {
            // Header: Greeting, Search, Notification, Profile
            item {
                HomeHeader(
                    onSearchClick = { showSearchDialog = true },
                    onNotificationClick = { showNotificationsDialog = true },
                    onProfileClick = { onNavigate(AppScreen.ProfileSettings) }
                )
            }

            // Hero Featured Card: "Create Your Next Edit"
            item {
                FeaturedHeroCard(
                    onCreateTemplate = { onNavigate(AppScreen.TimelineEditor(null)) },
                    onApplyTemplate = {
                        val firstTemplate = templates.firstOrNull()
                        if (firstTemplate != null) {
                            onNavigate(AppScreen.ApplyWorkflow(templateId = firstTemplate.id))
                        } else {
                            onNavigate(AppScreen.TemplateLibrary)
                        }
                    }
                )
            }

            // Quick Action Strip: Import Project shortcut with format badge
            item {
                QuickImportBanner(
                    onImportClick = { onNavigate(AppScreen.ImportProject) }
                )
            }

            // Featured Templates Horizontal Carousel
            item {
                SectionHeader(
                    title = "Featured Templates",
                    actionLabel = "View All",
                    onActionClick = { onNavigate(AppScreen.TemplateLibrary) }
                )
            }

            item {
                LazyRow(
                    contentPadding = PaddingValues(horizontal = 20.dp),
                    horizontalArrangement = Arrangement.spacedBy(16.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    items(featuredTemplates, key = { it.id }) { template ->
                        FeaturedTemplateCard(
                            template = template,
                            onClick = { onNavigate(AppScreen.TemplateDetails(template.id)) },
                            onToggleFavorite = { onToggleFavorite(template.id, template.isFavorite) }
                        )
                    }
                }
            }

            // Recent Projects / Workspaces
            item {
                Spacer(modifier = Modifier.height(24.dp))
                SectionHeader(
                    title = "Recent Projects & Drafts",
                    actionLabel = "Open Library",
                    onActionClick = { onNavigate(AppScreen.Library) }
                )
            }

            item {
                RecentProjectsList(
                    templates = templates.filter { it.isUserCreated || it.isFavorite }.take(3),
                    onOpenTemplate = { onNavigate(AppScreen.TemplateDetails(it.id)) },
                    onCreateNew = { onNavigate(AppScreen.TimelineEditor(null)) }
                )
            }
        }

        // Search Modal Dialog
        if (showSearchDialog) {
            SearchModal(
                query = searchQuery,
                onQueryChange = { searchQuery = it },
                onDismiss = { showSearchDialog = false },
                allTemplates = templates,
                onSelectTemplate = {
                    showSearchDialog = false
                    onNavigate(AppScreen.TemplateDetails(it.id))
                }
            )
        }

        // Notification Modal
        if (showNotificationsDialog) {
            NotificationsDialog(onDismiss = { showNotificationsDialog = false })
        }
    }
}

@Composable
private fun HomeHeader(
    onSearchClick: () -> Unit,
    onNotificationClick: () -> Unit,
    onProfileClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp, vertical = 16.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            EditCopyLogo(size = 40.dp, animated = true)
            Spacer(modifier = Modifier.width(12.dp))
            Column {
                Text(
                    text = "Welcome back, Creator",
                    fontSize = 13.sp,
                    color = TextSecondary,
                    fontWeight = FontWeight.Normal
                )
                Text(
                    text = "EditCopy Studio",
                    fontSize = 20.sp,
                    color = TextPrimary,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = (-0.3).sp
                )
            }
        }

        Row(verticalAlignment = Alignment.CenterVertically) {
            IconButton(
                onClick = onSearchClick,
                modifier = Modifier.testTag("home_search_button")
            ) {
                Icon(
                    imageVector = Icons.Filled.Search,
                    contentDescription = "Search Templates",
                    tint = TextPrimary
                )
            }

            IconButton(
                onClick = onNotificationClick,
                modifier = Modifier.testTag("home_notification_button")
            ) {
                BadgedBox(
                    badge = {
                        Badge(
                            containerColor = ElectricCyan,
                            modifier = Modifier.size(8.dp)
                        )
                    }
                ) {
                    Icon(
                        imageVector = Icons.Filled.Notifications,
                        contentDescription = "Notifications",
                        tint = TextPrimary
                    )
                }
            }

            Box(
                modifier = Modifier
                    .padding(start = 4.dp)
                    .size(38.dp)
                    .clip(CircleShape)
                    .background(
                        Brush.linearGradient(
                            listOf(ElectricCyan, NeonViolet)
                        )
                    )
                    .clickable(onClick = onProfileClick)
                    .testTag("home_profile_avatar"),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "EC",
                    color = DeepBlack,
                    fontWeight = FontWeight.ExtraBold,
                    fontSize = 14.sp
                )
            }
        }
    }
}

@Composable
private fun FeaturedHeroCard(
    onCreateTemplate: () -> Unit,
    onApplyTemplate: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp, vertical = 8.dp)
    ) {
        // Subtle ambient neon blur glow under the card
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(24.dp)),
            color = SurfaceNavy,
            border = BorderStroke(
                1.5.dp,
                Brush.horizontalGradient(
                    colors = listOf(ElectricCyan, NeonViolet, ElectricMagenta)
                )
            )
        ) {
            Box(
                modifier = Modifier
                    .background(
                        Brush.verticalGradient(
                            colors = listOf(
                                ElectricCyan.copy(alpha = 0.08f),
                                SurfaceNavyElevated
                            )
                        )
                    )
                    .padding(22.dp)
            ) {
                Column {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        FeatureBadge(
                            label = "PRO WORKFLOW",
                            accentColor = ElectricCyan,
                            icon = Icons.Filled.AutoAwesome
                        )
                        Text(
                            text = "Non-destructive",
                            color = TextMuted,
                            fontSize = 12.sp
                        )
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    Text(
                        text = "Create Your Next Edit",
                        fontSize = 24.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = TextPrimary,
                        letterSpacing = (-0.5).sp
                    )

                    Spacer(modifier = Modifier.height(6.dp))

                    Text(
                        text = "Transfer cuts, keyframe speed ramps, zoom curves, and 128+ BPM beat drops onto new video footage.",
                        fontSize = 13.sp,
                        color = TextSecondary,
                        lineHeight = 19.sp
                    )

                    Spacer(modifier = Modifier.height(20.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        NeonButton(
                            text = "Create Template",
                            onClick = onCreateTemplate,
                            icon = Icons.Filled.Add,
                            modifier = Modifier.weight(1f),
                            testTag = "hero_create_template_button"
                        )

                        NeonOutlinedButton(
                            text = "Apply Template",
                            onClick = onApplyTemplate,
                            accentColor = ElectricCyan,
                            modifier = Modifier.weight(1f),
                            testTag = "hero_apply_template_button"
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun QuickImportBanner(
    onImportClick: () -> Unit
) {
    GlassCard(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp, vertical = 8.dp),
        onClick = onImportClick,
        shapeRadius = 18.dp,
        borderGlowColor = NeonViolet.copy(alpha = 0.4f),
        backgroundColor = SurfaceNavy.copy(alpha = 0.7f)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 18.dp, vertical = 14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.weight(1f)
            ) {
                Box(
                    modifier = Modifier
                        .size(42.dp)
                        .clip(CircleShape)
                        .background(NeonViolet.copy(alpha = 0.2f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Filled.FileDownload,
                        contentDescription = "Import",
                        tint = NeonViolet,
                        modifier = Modifier.size(22.dp)
                    )
                }

                Spacer(modifier = Modifier.width(14.dp))

                Column {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = "Import Project File",
                            color = TextPrimary,
                            fontWeight = FontWeight.Bold,
                            fontSize = 15.sp
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Surface(
                            shape = RoundedCornerShape(4.dp),
                            color = NeonGreen.copy(alpha = 0.15f)
                        ) {
                            Text(
                                text = ".ecproj / .otio",
                                color = NeonGreen,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(horizontal = 5.dp, vertical = 2.dp)
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = "Extract cuts & keyframes • VN project lab testing active",
                        color = TextSecondary,
                        fontSize = 11.sp
                    )
                }
            }

            Icon(
                imageVector = Icons.Filled.ArrowForward,
                contentDescription = "Open Import",
                tint = NeonViolet,
                modifier = Modifier.size(18.dp)
            )
        }
    }
}

@Composable
private fun SectionHeader(
    title: String,
    actionLabel: String,
    onActionClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp, vertical = 12.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = title,
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold,
            color = TextPrimary
        )

        TextButton(onClick = onActionClick) {
            Text(
                text = actionLabel,
                color = ElectricCyan,
                fontSize = 13.sp,
                fontWeight = FontWeight.SemiBold
            )
        }
    }
}

@Composable
private fun FeaturedTemplateCard(
    template: VideoTemplate,
    onClick: () -> Unit,
    onToggleFavorite: () -> Unit
) {
    val accentColor = Color(template.primaryAccentColorHex)

    GlassCard(
        modifier = Modifier
            .width(260.dp),
        shapeRadius = 20.dp,
        borderGlowColor = accentColor.copy(alpha = 0.35f),
        backgroundColor = SurfaceNavy,
        onClick = onClick
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            // Visual simulated video frame preview
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(130.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(
                        Brush.linearGradient(
                            listOf(
                                Color(template.primaryAccentColorHex).copy(alpha = 0.3f),
                                Color(template.secondaryAccentColorHex).copy(alpha = 0.15f),
                                DeepBlack
                            )
                        )
                    )
            ) {
                // Aspect ratio & duration chip
                Surface(
                    shape = RoundedCornerShape(6.dp),
                    color = DeepBlack.copy(alpha = 0.75f),
                    modifier = Modifier
                        .align(Alignment.TopStart)
                        .padding(8.dp)
                ) {
                    Text(
                        text = "${template.aspectRatio} • ${String.format("%.1f", template.durationSeconds)}s",
                        color = Color.White,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Medium,
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp)
                    )
                }

                // Favorite star button
                Box(
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(6.dp)
                        .size(32.dp)
                        .clip(CircleShape)
                        .background(DeepBlack.copy(alpha = 0.6f))
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

                // Play icon in center
                Box(
                    modifier = Modifier
                        .align(Alignment.Center)
                        .size(40.dp)
                        .clip(CircleShape)
                        .background(ElectricCyan.copy(alpha = 0.25f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Filled.PlayArrow,
                        contentDescription = "Preview",
                        tint = ElectricCyan,
                        modifier = Modifier.size(24.dp)
                    )
                }

                // Mini timeline track on bottom of preview
                TimelineMiniPreview(
                    cutsCount = template.cutsCount,
                    keyframesCount = template.keyframesCount,
                    durationSeconds = template.durationSeconds,
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .padding(6.dp)
                        .height(22.dp)
                )
            }

            Spacer(modifier = Modifier.height(14.dp))

            Text(
                text = template.title,
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = TextPrimary,
                maxLines = 1
            )

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = template.description,
                fontSize = 12.sp,
                color = TextSecondary,
                maxLines = 2,
                lineHeight = 16.sp
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Badges row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                FeatureBadge(
                    label = "${template.cutsCount} Cuts",
                    accentColor = ElectricCyan
                )
                FeatureBadge(
                    label = "${template.keyframesCount} Keyframes",
                    accentColor = NeonViolet
                )
                if (template.hasBeatSync) {
                    FeatureBadge(
                        label = "${template.bpm} BPM",
                        accentColor = ElectricMagenta
                    )
                }
            }
        }
    }
}

@Composable
private fun RecentProjectsList(
    templates: List<VideoTemplate>,
    onOpenTemplate: (VideoTemplate) -> Unit,
    onCreateNew: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        if (templates.isEmpty()) {
            GlassCard(
                modifier = Modifier.fillMaxWidth(),
                backgroundColor = SurfaceNavy.copy(alpha = 0.5f)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column {
                        Text(
                            text = "No custom templates yet",
                            color = TextPrimary,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                        Text(
                            text = "Create or duplicate a template to begin",
                            color = TextSecondary,
                            fontSize = 12.sp
                        )
                    }
                    NeonOutlinedButton(
                        text = "New",
                        onClick = onCreateNew,
                        accentColor = ElectricCyan
                    )
                }
            }
        } else {
            templates.forEach { tpl ->
                GlassCard(
                    modifier = Modifier.fillMaxWidth(),
                    shapeRadius = 14.dp,
                    onClick = { onOpenTemplate(tpl) }
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(14.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(48.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .background(
                                    Brush.linearGradient(
                                        listOf(
                                            Color(tpl.primaryAccentColorHex),
                                            Color(tpl.secondaryAccentColorHex)
                                        )
                                    )
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "${tpl.cutsCount}c",
                                color = DeepBlack,
                                fontWeight = FontWeight.Bold,
                                fontSize = 13.sp
                            )
                        }

                        Spacer(modifier = Modifier.width(14.dp))

                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = tpl.title,
                                color = TextPrimary,
                                fontWeight = FontWeight.SemiBold,
                                fontSize = 14.sp
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = "${tpl.category.displayName} • ${tpl.aspectRatio} • ${String.format("%.1f", tpl.durationSeconds)}s",
                                color = TextSecondary,
                                fontSize = 12.sp
                            )
                        }

                        Icon(
                            imageVector = Icons.Filled.ArrowForward,
                            contentDescription = "Open",
                            tint = TextMuted,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun SearchModal(
    query: String,
    onQueryChange: (String) -> Unit,
    onDismiss: () -> Unit,
    allTemplates: List<VideoTemplate>,
    onSelectTemplate: (VideoTemplate) -> Unit
) {
    val filtered = remember(query, allTemplates) {
        if (query.isBlank()) allTemplates
        else allTemplates.filter {
            it.title.contains(query, ignoreCase = true) ||
                    it.description.contains(query, ignoreCase = true) ||
                    it.category.displayName.contains(query, ignoreCase = true) ||
                    it.tags.any { tag -> tag.contains(query, ignoreCase = true) }
        }
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("Close", color = ElectricCyan)
            }
        },
        containerColor = SurfaceNavyElevated,
        title = {
            Text(
                text = "Search Templates",
                color = TextPrimary,
                fontWeight = FontWeight.Bold
            )
        },
        text = {
            Column(modifier = Modifier.fillMaxWidth()) {
                OutlinedTextField(
                    value = query,
                    onValueChange = onQueryChange,
                    modifier = Modifier.fillMaxWidth(),
                    placeholder = { Text("Search by cut, beat, zoom, genre...", color = TextMuted) },
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = TextPrimary,
                        unfocusedTextColor = TextPrimary,
                        focusedBorderColor = ElectricCyan,
                        unfocusedBorderColor = CardBorderSubtle,
                        cursorColor = ElectricCyan
                    )
                )

                Spacer(modifier = Modifier.height(12.dp))

                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(220.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(filtered, key = { it.id }) { tpl ->
                        Surface(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(8.dp))
                                .clickable { onSelectTemplate(tpl) },
                            color = SurfaceNavy
                        ) {
                            Column(modifier = Modifier.padding(10.dp)) {
                                Text(
                                    text = tpl.title,
                                    color = TextPrimary,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 13.sp
                                )
                                Text(
                                    text = "${tpl.category.displayName} • ${tpl.cutsCount} Cuts",
                                    color = TextSecondary,
                                    fontSize = 11.sp
                                )
                            }
                        }
                    }
                }
            }
        }
    )
}

@Composable
private fun NotificationsDialog(onDismiss: () -> Unit) {
    AlertDialog(
        onDismissRequest = onDismiss,
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("Dismiss", color = ElectricCyan)
            }
        },
        containerColor = SurfaceNavyElevated,
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Filled.Notifications,
                    contentDescription = null,
                    tint = ElectricCyan
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text("Studio Updates", color = TextPrimary, fontWeight = FontWeight.Bold)
            }
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Surface(
                    shape = RoundedCornerShape(10.dp),
                    color = SurfaceNavy,
                    modifier = Modifier.padding(vertical = 4.dp)
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Text(
                            text = "Lab Status: VN Video Editor Parser",
                            color = AmberWarning,
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "VN binary project compatibility is currently in laboratory validation. Direct .vnproject binary modification remains experimental. Native EditCopy packages (.ecproj) are fully supported.",
                            color = TextSecondary,
                            fontSize = 12.sp,
                            lineHeight = 16.sp
                        )
                    }
                }

                Surface(
                    shape = RoundedCornerShape(10.dp),
                    color = SurfaceNavy,
                    modifier = Modifier.padding(vertical = 4.dp)
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Text(
                            text = "New Template Pack Added",
                            color = NeonGreen,
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Cyber Velocity Ramp and 35mm Cinematic Drift templates are loaded and ready to apply to your videos.",
                            color = TextSecondary,
                            fontSize = 12.sp,
                            lineHeight = 16.sp
                        )
                    }
                }
            }
        }
    )
}
