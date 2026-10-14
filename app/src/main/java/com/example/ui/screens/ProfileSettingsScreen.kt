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
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Feedback
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material.icons.filled.PrivacyTip
import androidx.compose.material.icons.filled.Science
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
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
import com.example.ui.components.EditCopyLogo
import com.example.ui.components.GlassCard
import com.example.ui.components.NeonButton
import com.example.ui.theme.AmberWarning
import com.example.ui.theme.CardBorderSubtle
import com.example.ui.theme.DeepBlack
import com.example.ui.theme.ElectricCyan
import com.example.ui.theme.ElectricMagenta
import com.example.ui.theme.NeonGreen
import com.example.ui.theme.NeonViolet
import com.example.ui.theme.SurfaceNavy
import com.example.ui.theme.SurfaceNavyElevated
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary

@Composable
fun ProfileSettingsScreen(
    templatesCount: Int,
    onNavigate: (AppScreen) -> Unit,
    modifier: Modifier = Modifier
) {
    var defaultResolution by remember { mutableStateOf("1080 x 1920 (9:16)") }
    var defaultFramerate by remember { mutableStateOf("60 fps") }
    var hapticFeedbackEnabled by remember { mutableStateOf(true) }
    var showLabDialog by remember { mutableStateOf(false) }
    var showFeedbackDialog by remember { mutableStateOf(false) }
    var showPrivacyDialog by remember { mutableStateOf(false) }
    var feedbackText by remember { mutableStateOf("") }
    var feedbackSubmitted by remember { mutableStateOf(false) }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(DeepBlack)
            .statusBarsPadding()
            .testTag("profile_settings_screen")
    ) {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(start = 20.dp, end = 20.dp, top = 16.dp, bottom = 96.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // User Profile Card
            item {
                GlassCard(
                    modifier = Modifier.fillMaxWidth(),
                    shapeRadius = 20.dp,
                    borderGlowColor = ElectricCyan.copy(alpha = 0.4f),
                    backgroundColor = SurfaceNavy
                ) {
                    Column(modifier = Modifier.padding(20.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(60.dp)
                                    .clip(CircleShape)
                                    .background(
                                        Brush.linearGradient(
                                            listOf(ElectricCyan, NeonViolet, ElectricMagenta)
                                        )
                                    ),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = "EC",
                                    color = DeepBlack,
                                    fontWeight = FontWeight.ExtraBold,
                                    fontSize = 20.sp
                                )
                            }

                            Spacer(modifier = Modifier.width(16.dp))

                            Column {
                                Text(
                                    text = "Creator Studio",
                                    color = TextPrimary,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 18.sp
                                )
                                Text(
                                    text = "Pro Template Architect",
                                    color = ElectricCyan,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Medium
                                )
                                Text(
                                    text = "Edit Once · Copy Forever",
                                    color = TextSecondary,
                                    fontSize = 11.sp
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(18.dp))

                        // Stats Row
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(12.dp))
                                .background(SurfaceNavyElevated)
                                .padding(vertical = 12.dp),
                            horizontalArrangement = Arrangement.SpaceAround
                        ) {
                            StatItem(count = "$templatesCount", label = "Templates")
                            StatItem(count = "12", label = "Edits Applied")
                            StatItem(count = "100%", label = "Lossless")
                        }
                    }
                }
            }

            // Export Defaults Settings Section
            item {
                SectionTitle(title = "Export & Blueprint Defaults")
            }

            item {
                GlassCard(modifier = Modifier.fillMaxWidth(), backgroundColor = SurfaceNavy) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        SettingRowText(
                            title = "Default Output Resolution",
                            subtitle = defaultResolution,
                            onClick = {
                                defaultResolution = if (defaultResolution.startsWith("1080")) "2160 x 3840 (4K)" else "1080 x 1920 (9:16)"
                            }
                        )

                        Spacer(modifier = Modifier.height(14.dp))

                        SettingRowText(
                            title = "Target Framerate",
                            subtitle = defaultFramerate,
                            onClick = {
                                defaultFramerate = if (defaultFramerate.startsWith("60")) "30 fps" else "60 fps"
                            }
                        )

                        Spacer(modifier = Modifier.height(14.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text("Split Cut Haptic Feedback", color = TextPrimary, fontSize = 14.sp, fontWeight = FontWeight.SemiBold)
                                Text("Vibrate on split cuts & keyframe snapping", color = TextSecondary, fontSize = 11.sp)
                            }

                            Switch(
                                checked = hapticFeedbackEnabled,
                                onCheckedChange = { hapticFeedbackEnabled = it },
                                colors = SwitchDefaults.colors(
                                    checkedThumbColor = DeepBlack,
                                    checkedTrackColor = ElectricCyan
                                )
                            )
                        }
                    }
                }
            }

            // Format Compatibility Laboratory
            item {
                SectionTitle(title = "Engine & Laboratory Status")
            }

            item {
                GlassCard(
                    modifier = Modifier.fillMaxWidth(),
                    shapeRadius = 14.dp,
                    borderGlowColor = AmberWarning.copy(alpha = 0.35f),
                    backgroundColor = SurfaceNavy,
                    onClick = { showLabDialog = true }
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(imageVector = Icons.Filled.Science, contentDescription = null, tint = AmberWarning, modifier = Modifier.size(24.dp))
                            Spacer(modifier = Modifier.width(12.dp))
                            Column {
                                Text("VN Video Editor Compatibility Lab", color = TextPrimary, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                                Text("Laboratory status report & interchange roadmap", color = TextSecondary, fontSize = 11.sp)
                            }
                        }

                        Icon(imageVector = Icons.Filled.ArrowForward, contentDescription = null, tint = TextMuted, modifier = Modifier.size(16.dp))
                    }
                }
            }

            // Support & Feedback
            item {
                SectionTitle(title = "Support & About")
            }

            item {
                GlassCard(modifier = Modifier.fillMaxWidth(), backgroundColor = SurfaceNavy) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        SettingActionRow(
                            title = "Privacy & Local Data Architecture",
                            subtitle = "Zero tracking • Media files never leave your device",
                            icon = Icons.Filled.PrivacyTip,
                            onClick = { showPrivacyDialog = true }
                        )

                        Spacer(modifier = Modifier.height(14.dp))

                        SettingActionRow(
                            title = "Send Feedback to Developers",
                            subtitle = "Feature requests, custom curves, and format bugs",
                            icon = Icons.Filled.Feedback,
                            onClick = { showFeedbackDialog = true }
                        )

                        Spacer(modifier = Modifier.height(14.dp))

                        SettingActionRow(
                            title = "About EditCopy",
                            subtitle = "Version 1.0.0 • Architecture v1.2",
                            icon = Icons.Filled.Info,
                            onClick = {}
                        )
                    }
                }
            }
        }

        // Lab Status Dialog
        if (showLabDialog) {
            AlertDialog(
                onDismissRequest = { showLabDialog = false },
                confirmButton = {
                    TextButton(onClick = { showLabDialog = false }) {
                        Text("Understood", color = ElectricCyan)
                    }
                },
                containerColor = SurfaceNavyElevated,
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Filled.Science, null, tint = AmberWarning)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Laboratory Status", color = TextPrimary, fontWeight = FontWeight.Bold)
                    }
                },
                text = {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text(
                            text = "Commitment to Technical Honesty:",
                            color = ElectricCyan,
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp
                        )
                        Text(
                            text = "EditCopy will NEVER pretend to support proprietary closed-source project formats without certified round-trip stability. Direct VN Video Editor (.vnproject) binary structure modifications are currently isolated in laboratory testing to protect users against lost projects.",
                            color = TextSecondary,
                            fontSize = 12.sp,
                            lineHeight = 17.sp
                        )
                        Text(
                            text = "Recommended Workflow: Export pacing into EditCopy Native (.ecproj) or OpenTimelineIO (.otio) interchange standards.",
                            color = TextPrimary,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
            )
        }

        // Feedback Dialog
        if (showFeedbackDialog) {
            AlertDialog(
                onDismissRequest = { showFeedbackDialog = false },
                confirmButton = {
                    NeonButton(
                        text = if (feedbackSubmitted) "Submitted!" else "Send Feedback",
                        onClick = {
                            feedbackSubmitted = true
                            feedbackText = ""
                        }
                    )
                },
                dismissButton = {
                    TextButton(onClick = { showFeedbackDialog = false }) {
                        Text("Close", color = TextSecondary)
                    }
                },
                containerColor = SurfaceNavyElevated,
                title = { Text("Developer Feedback", color = TextPrimary, fontWeight = FontWeight.Bold) },
                text = {
                    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        Text("Help us refine editing template transfers:", color = TextSecondary, fontSize = 12.sp)
                        OutlinedTextField(
                            value = feedbackText,
                            onValueChange = { feedbackText = it },
                            placeholder = { Text("What formats or curves would you like next?", color = TextMuted) },
                            modifier = Modifier.fillMaxWidth(),
                            minLines = 3,
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedTextColor = TextPrimary,
                                unfocusedTextColor = TextPrimary,
                                focusedBorderColor = ElectricCyan,
                                unfocusedBorderColor = CardBorderSubtle,
                                cursorColor = ElectricCyan
                            )
                        )
                    }
                }
            )
        }

        // Privacy Dialog
        if (showPrivacyDialog) {
            AlertDialog(
                onDismissRequest = { showPrivacyDialog = false },
                confirmButton = {
                    TextButton(onClick = { showPrivacyDialog = false }) {
                        Text("Close", color = ElectricCyan)
                    }
                },
                containerColor = SurfaceNavyElevated,
                title = { Text("Privacy Architecture", color = TextPrimary, fontWeight = FontWeight.Bold) },
                text = {
                    Text(
                        text = "EditCopy is built with a zero-compromise local-first policy. All video timelines, keyframes, clip segment metadata, and source footage remain strictly on your Android device. Original media files are never overwritten or altered.",
                        color = TextSecondary,
                        fontSize = 13.sp,
                        lineHeight = 18.sp
                    )
                }
            )
        }
    }
}

@Composable
private fun StatItem(count: String, label: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(text = count, color = ElectricCyan, fontWeight = FontWeight.ExtraBold, fontSize = 18.sp)
        Text(text = label, color = TextSecondary, fontSize = 11.sp)
    }
}

@Composable
private fun SectionTitle(title: String) {
    Text(
        text = title,
        color = TextPrimary,
        fontWeight = FontWeight.Bold,
        fontSize = 15.sp,
        modifier = Modifier.padding(vertical = 2.dp)
    )
}

@Composable
private fun SettingRowText(
    title: String,
    subtitle: String,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column {
            Text(text = title, color = TextPrimary, fontSize = 14.sp, fontWeight = FontWeight.SemiBold)
            Text(text = subtitle, color = ElectricCyan, fontSize = 12.sp, fontWeight = FontWeight.Medium)
        }
        Icon(imageVector = Icons.Filled.Tune, contentDescription = null, tint = TextMuted, modifier = Modifier.size(18.dp))
    }
}

@Composable
private fun SettingActionRow(
    title: String,
    subtitle: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(imageVector = icon, contentDescription = null, tint = ElectricCyan, modifier = Modifier.size(20.dp))
            Spacer(modifier = Modifier.width(12.dp))
            Column {
                Text(text = title, color = TextPrimary, fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
                Text(text = subtitle, color = TextSecondary, fontSize = 11.sp)
            }
        }
        Icon(imageVector = Icons.Filled.ArrowForward, contentDescription = null, tint = TextMuted, modifier = Modifier.size(16.dp))
    }
}
