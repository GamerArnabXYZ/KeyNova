package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.KeyboardPreferences

enum class SettingsDestination {
    MAIN,
    LANGUAGES,
    THEMES,
    TOOLBAR,
    LAYOUT,
    CLIPBOARD,
    GAMING_CODING,
    PRIVACY,
    ABOUT
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    keyboardPreferences: KeyboardPreferences,
    onNavigateBack: () -> Unit,
    onNavigateTo: (SettingsDestination) -> Unit
) {
    val enabledLangsCount = keyboardPreferences.enabledLanguages.size
    val currentThemeName = keyboardPreferences.theme.displayName

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Settings",
                        fontWeight = FontWeight.Bold,
                        fontSize = 20.sp,
                        color = Color(0xFF0F172A)
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                            tint = Color(0xFF0F172A)
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color(0xFFF8FAFC))
            )
        },
        containerColor = Color(0xFFF8FAFC)
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 16.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            item {
                SettingsCardItem(
                    icon = Icons.Outlined.Language,
                    title = "Languages",
                    subtitle = "English, বাংলা, हिन्दी ($enabledLangsCount active)",
                    onClick = { onNavigateTo(SettingsDestination.LANGUAGES) }
                )
            }

            item {
                SettingsCardItem(
                    icon = Icons.Outlined.Palette,
                    title = "Theme",
                    subtitle = "$currentThemeName • Accent colors",
                    onClick = { onNavigateTo(SettingsDestination.THEMES) }
                )
            }

            item {
                val activeToolsCount = keyboardPreferences.enabledToolbarTools.size
                SettingsCardItem(
                    icon = Icons.Outlined.Widgets,
                    title = "Toolbar Customization",
                    subtitle = "$activeToolsCount / 7 active tools • Reorder, Toggle on/off",
                    onClick = { onNavigateTo(SettingsDestination.TOOLBAR) }
                )
            }

            item {
                SettingsCardItem(
                    icon = Icons.Outlined.Keyboard,
                    title = "Keyboard Layout",
                    subtitle = "Hold-to-delete words, Speed, Key height, Spacing",
                    onClick = { onNavigateTo(SettingsDestination.LAYOUT) }
                )
            }

            item {
                SettingsCardItem(
                    icon = Icons.Outlined.ContentPaste,
                    title = "Clipboard",
                    subtitle = "History, Pinned clips, Auto clear",
                    onClick = { onNavigateTo(SettingsDestination.CLIPBOARD) }
                )
            }

            item {
                SettingsCardItem(
                    icon = Icons.Outlined.SportsEsports,
                    title = "Gaming & Coding",
                    subtitle = "Developer symbols, Custom snippets & macros",
                    onClick = { onNavigateTo(SettingsDestination.GAMING_CODING) }
                )
            }

            item {
                SettingsCardItem(
                    icon = Icons.Outlined.Security,
                    title = "Privacy",
                    subtitle = "No data collection, 100% offline",
                    onClick = { onNavigateTo(SettingsDestination.PRIVACY) }
                )
            }

            item {
                SettingsCardItem(
                    icon = Icons.Outlined.Info,
                    title = "About",
                    subtitle = "KeyNova v1.0.0 • Open Source",
                    onClick = { onNavigateTo(SettingsDestination.ABOUT) }
                )
            }
        }
    }
}

@Composable
private fun SettingsCardItem(
    icon: ImageVector,
    title: String,
    subtitle: String,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .clickable(onClick = onClick),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        border = CardDefaults.outlinedCardBorder()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 14.dp, vertical = 14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.weight(1f)
            ) {
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(CircleShape)
                        .background(Color(0xFFEFF6FF)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        tint = Color(0xFF2563EB),
                        modifier = Modifier.size(20.dp)
                    )
                }

                Spacer(modifier = Modifier.width(14.dp))

                Column {
                    Text(
                        text = title,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = Color(0xFF0F172A)
                    )
                    Text(
                        text = subtitle,
                        fontSize = 12.sp,
                        color = Color(0xFF64748B),
                        modifier = Modifier.padding(top = 2.dp)
                    )
                }
            }

            Icon(
                imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                contentDescription = null,
                tint = Color(0xFF94A3B8),
                modifier = Modifier.size(20.dp)
            )
        }
    }
}
