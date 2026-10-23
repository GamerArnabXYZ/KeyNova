package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.KeyboardPreferences
import com.example.data.ToolbarTool
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ToolbarSettingsScreen(
    keyboardPreferences: KeyboardPreferences,
    onNavigateBack: () -> Unit
) {
    val coroutineScope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }

    var enabledTools by remember { mutableStateOf(keyboardPreferences.enabledToolbarTools) }
    var previewActiveToolId by remember { mutableStateOf<String?>(null) }
    val currentTheme = keyboardPreferences.theme

    val allTools = ToolbarTool.entries
    val maxTools = ToolbarTool.MAX_ENABLED_TOOLS
    val activeCount = enabledTools.size

    fun toggleTool(tool: ToolbarTool, isChecked: Boolean) {
        val success = keyboardPreferences.toggleToolbarTool(tool, isChecked)
        if (success) {
            enabledTools = keyboardPreferences.enabledToolbarTools
        } else {
            coroutineScope.launch {
                snackbarHostState.currentSnackbarData?.dismiss()
                snackbarHostState.showSnackbar(
                    message = "Maximum $maxTools tools can be active simultaneously. Turn off a tool first.",
                    duration = SnackbarDuration.Short
                )
            }
        }
    }

    fun moveTool(fromIndex: Int, toIndex: Int) {
        keyboardPreferences.moveToolbarTool(fromIndex, toIndex)
        enabledTools = keyboardPreferences.enabledToolbarTools
    }

    fun resetDefaults() {
        keyboardPreferences.resetToolbarTools()
        enabledTools = keyboardPreferences.enabledToolbarTools
        coroutineScope.launch {
            snackbarHostState.showSnackbar("Toolbar restored to default 7 tools")
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = "Toolbar Customization",
                            fontWeight = FontWeight.Bold,
                            fontSize = 19.sp,
                            color = Color(0xFF0F172A)
                        )
                        Text(
                            text = "$activeCount of $maxTools tools active",
                            fontSize = 12.sp,
                            color = if (activeCount == maxTools) Color(0xFFD97706) else Color(0xFF64748B)
                        )
                    }
                },
                navigationIcon = {
                    IconButton(
                        onClick = onNavigateBack,
                        modifier = Modifier.testTag("toolbar_settings_back_button")
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                            tint = Color(0xFF0F172A)
                        )
                    }
                },
                actions = {
                    IconButton(
                        onClick = { resetDefaults() },
                        modifier = Modifier.testTag("toolbar_settings_reset_button")
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.RestartAlt,
                            contentDescription = "Reset to Default",
                            tint = Color(0xFF3B82F6)
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color(0xFFF8FAFC))
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) },
        containerColor = Color(0xFFF8FAFC)
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 16.dp, vertical = 6.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // 1. Quota & Status Banner
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Active Toolbar Slots",
                                fontWeight = FontWeight.Bold,
                                fontSize = 15.sp,
                                color = Color(0xFF0F172A)
                            )
                            Surface(
                                shape = RoundedCornerShape(20.dp),
                                color = if (activeCount == maxTools) Color(0xFFFEF3C7) else Color(0xFFEFF6FF)
                            ) {
                                Text(
                                    text = "$activeCount / $maxTools ACTIVE",
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (activeCount == maxTools) Color(0xFFB45309) else Color(0xFF1D4ED8)
                                )
                            }
                        }

                        LinearProgressIndicator(
                            progress = { activeCount / maxTools.toFloat() },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(8.dp)
                                .clip(RoundedCornerShape(4.dp)),
                            color = if (activeCount == maxTools) Color(0xFFF59E0B) else Color(0xFF2563EB),
                            trackColor = Color(0xFFE2E8F0)
                        )

                        Text(
                            text = if (activeCount == maxTools) {
                                "Maximum limit reached (7 tools). Toggle off an active tool if you want to enable another one."
                            } else {
                                "You can enable up to $maxTools tools simultaneously. Inactive tools are kept off the keyboard bar for a clean, focused typing experience."
                            },
                            fontSize = 12.sp,
                            color = Color(0xFF64748B),
                            lineHeight = 16.sp
                        )
                    }
                }
            }

            // 2. Live Interactive Preview
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(14.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "LIVE KEYBOARD TOOLBAR PREVIEW",
                                fontWeight = FontWeight.Bold,
                                fontSize = 11.sp,
                                letterSpacing = 1.sp,
                                color = Color(0xFF64748B)
                            )
                            Text(
                                text = "Theme: ${currentTheme.displayName}",
                                fontSize = 11.sp,
                                color = Color(0xFF94A3B8)
                            )
                        }

                        // Toolbar Simulation Bar
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(12.dp))
                                .background(Color(currentTheme.backgroundHex))
                                .border(1.dp, Color(currentTheme.textHex).copy(alpha = 0.1f), RoundedCornerShape(12.dp))
                                .padding(horizontal = 8.dp, vertical = 6.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Row(
                                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    enabledTools.forEach { tool ->
                                        val isSelected = previewActiveToolId == tool.id
                                        val icon = getToolbarIconForTool(tool)
                                        Box(
                                            modifier = Modifier
                                                .size(36.dp)
                                                .clip(RoundedCornerShape(10.dp))
                                                .background(
                                                    if (isSelected) Color(currentTheme.accentHex).copy(alpha = 0.22f)
                                                    else Color(currentTheme.surfaceHex).copy(alpha = 0.5f)
                                                )
                                                .border(
                                                    1.dp,
                                                    if (isSelected) Color(currentTheme.accentHex)
                                                    else Color(currentTheme.textHex).copy(alpha = 0.12f),
                                                    RoundedCornerShape(10.dp)
                                                )
                                                .clickable {
                                                    previewActiveToolId = if (isSelected) null else tool.id
                                                },
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Icon(
                                                imageVector = icon,
                                                contentDescription = tool.title,
                                                tint = if (isSelected) Color(currentTheme.accentHex) else Color(currentTheme.textHex),
                                                modifier = Modifier.size(19.dp)
                                            )
                                        }
                                    }
                                }

                                // End customize gear icon in preview
                                Box(
                                    modifier = Modifier
                                        .size(34.dp)
                                        .clip(CircleShape)
                                        .background(Color(currentTheme.accentHex).copy(alpha = 0.12f)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Outlined.Widgets,
                                        contentDescription = "Customize",
                                        tint = Color(currentTheme.accentHex),
                                        modifier = Modifier.size(17.dp)
                                    )
                                }
                            }
                        }

                        previewActiveToolId?.let { activeId ->
                            val activeTool = ToolbarTool.fromId(activeId)
                            if (activeTool != null) {
                                Surface(
                                    shape = RoundedCornerShape(8.dp),
                                    color = Color(0xFFF1F5F9),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Text(
                                        text = "Selected preview: ${activeTool.title} • ${activeTool.description}",
                                        fontSize = 11.sp,
                                        color = Color(0xFF334155),
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 6.dp)
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // 3. Section Title
            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 4.dp, vertical = 2.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "AVAILABLE TOOLS (${allTools.size})",
                        fontWeight = FontWeight.Bold,
                        fontSize = 12.sp,
                        letterSpacing = 0.8.sp,
                        color = Color(0xFF64748B)
                    )
                    Text(
                        text = "Max 7 Active",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = Color(0xFF3B82F6)
                    )
                }
            }

            // 4. Tools List Items
            itemsIndexed(allTools) { _, tool ->
                val isEnabled = enabledTools.contains(tool)
                val orderIndex = enabledTools.indexOf(tool)
                val isAtMaxAndDisabled = !isEnabled && activeCount >= maxTools

                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("tool_card_${tool.id}"),
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = if (isEnabled) Color.White else Color(0xFFF8FAFC)
                    ),
                    border = if (isEnabled) {
                        androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFBFDBFE))
                    } else {
                        androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFE2E8F0))
                    },
                    elevation = CardDefaults.cardElevation(defaultElevation = if (isEnabled) 1.dp else 0.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 14.dp, vertical = 12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        // Icon Pill
                        val icon = getToolbarIconForTool(tool)
                        Box(
                            modifier = Modifier
                                .size(42.dp)
                                .clip(RoundedCornerShape(12.dp))
                                .background(
                                    if (isEnabled) Color(0xFFEFF6FF) else Color(0xFFF1F5F9)
                                )
                                .border(
                                    1.dp,
                                    if (isEnabled) Color(0xFF93C5FD) else Color(0xFFCBD5E1),
                                    RoundedCornerShape(12.dp)
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = icon,
                                contentDescription = tool.title,
                                tint = if (isEnabled) Color(0xFF2563EB) else Color(0xFF64748B),
                                modifier = Modifier.size(22.dp)
                            )
                        }

                        // Texts
                        Column(
                            modifier = Modifier.weight(1f),
                            verticalArrangement = Arrangement.spacedBy(2.dp)
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Text(
                                    text = tool.title,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 15.sp,
                                    color = if (isEnabled) Color(0xFF0F172A) else Color(0xFF64748B)
                                )
                                if (isEnabled) {
                                    Surface(
                                        shape = RoundedCornerShape(4.dp),
                                        color = Color(0xFFDBEAFE)
                                    ) {
                                        Text(
                                            text = "#${orderIndex + 1}",
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = Color(0xFF1E40AF),
                                            modifier = Modifier.padding(horizontal = 5.dp, vertical = 1.dp)
                                        )
                                    }
                                }
                            }
                            Text(
                                text = tool.description,
                                fontSize = 12.sp,
                                color = Color(0xFF64748B),
                                lineHeight = 16.sp
                            )
                        }

                        // Order Up/Down buttons if enabled
                        if (isEnabled) {
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.spacedBy(2.dp)
                            ) {
                                if (orderIndex > 0) {
                                    IconButton(
                                        onClick = { moveTool(orderIndex, orderIndex - 1) },
                                        modifier = Modifier.size(24.dp)
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.KeyboardArrowUp,
                                            contentDescription = "Move Left",
                                            tint = Color(0xFF64748B),
                                            modifier = Modifier.size(18.dp)
                                        )
                                    }
                                } else {
                                    Spacer(modifier = Modifier.size(24.dp))
                                }

                                if (orderIndex < enabledTools.size - 1) {
                                    IconButton(
                                        onClick = { moveTool(orderIndex, orderIndex + 1) },
                                        modifier = Modifier.size(24.dp)
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.KeyboardArrowDown,
                                            contentDescription = "Move Right",
                                            tint = Color(0xFF64748B),
                                            modifier = Modifier.size(18.dp)
                                        )
                                    }
                                } else {
                                    Spacer(modifier = Modifier.size(24.dp))
                                }
                            }
                        }

                        // Switch toggle
                        Switch(
                            checked = isEnabled,
                            onCheckedChange = { checked ->
                                toggleTool(tool, checked)
                            },
                            enabled = !isAtMaxAndDisabled || isEnabled,
                            colors = SwitchDefaults.colors(
                                checkedThumbColor = Color.White,
                                checkedTrackColor = Color(0xFF2563EB),
                                uncheckedThumbColor = Color.White,
                                uncheckedTrackColor = Color(0xFFCBD5E1)
                            ),
                            modifier = Modifier.testTag("tool_switch_${tool.id}")
                        )
                    }
                }
            }

            // Bottom Spacing
            item {
                Spacer(modifier = Modifier.height(24.dp))
            }
        }
    }
}

/**
 * Returns a sleek, modern Material icon for each toolbar tool.
 */
fun getToolbarIconForTool(tool: ToolbarTool): ImageVector {
    return when (tool) {
        ToolbarTool.EMOJI -> Icons.Outlined.Mood
        ToolbarTool.CLIPBOARD -> Icons.Outlined.ContentPaste
        ToolbarTool.TEXT_EDITING -> Icons.Outlined.EditNote
        ToolbarTool.NUMPAD -> Icons.Outlined.Dialpad
        ToolbarTool.SNIPPETS -> Icons.Outlined.Quickreply
        ToolbarTool.CODING -> Icons.Outlined.Code
        ToolbarTool.GAMING -> Icons.Outlined.SportsEsports
        ToolbarTool.THEMES -> Icons.Outlined.Palette
        ToolbarTool.ONE_HANDED -> Icons.Outlined.TouchApp
        ToolbarTool.CASE_CONVERTER -> Icons.Outlined.TextFields
        ToolbarTool.CLEAR_TEXT -> Icons.Outlined.DeleteSweep
        ToolbarTool.SETTINGS -> Icons.Outlined.Tune
    }
}
