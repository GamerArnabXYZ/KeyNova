package com.example.ui.screens

import android.content.Context
import android.content.Intent
import android.provider.Settings
import android.view.inputmethod.InputMethodManager
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.data.KeyboardPreferences

enum class SettingsDestination {
    MAIN,
    LANGUAGES,
    THEMES,
    CUSTOM_THEME,
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
    val context = LocalContext.current
    val enabledLangsCount = keyboardPreferences.enabledLanguages.size
    val currentThemeName = keyboardPreferences.theme.displayName
    val activeToolsCount = keyboardPreferences.enabledToolbarTools.size

    // IME Status Detection
    val imm = remember { context.getSystemService(Context.INPUT_METHOD_SERVICE) as? InputMethodManager }
    val isEnabled = remember(imm) {
        val enabledList = imm?.enabledInputMethodList ?: emptyList()
        enabledList.any { it.packageName == context.packageName }
    }
    val isDefault = remember(context) {
        val defaultIme = Settings.Secure.getString(context.contentResolver, Settings.Secure.DEFAULT_INPUT_METHOD) ?: ""
        defaultIme.contains(context.packageName)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = stringResource(R.string.settings_title),
                        fontWeight = FontWeight.Bold,
                        fontSize = 20.sp,
                        color = Color(0xFF0F172A)
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = stringResource(R.string.action_back),
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
                .padding(innerPadding),
            contentPadding = PaddingValues(start = 16.dp, top = 8.dp, end = 16.dp, bottom = 48.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            // ==========================================
            // SYSTEM STATUS BANNER
            // ==========================================
            item {
                SettingsSectionHeader(title = stringResource(R.string.settings_section_status))
            }

            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = if (isDefault) Color(0xFFF0FDF4) else if (isEnabled) Color(0xFFEFF6FF) else Color(0xFFFFFBEB)
                    ),
                    border = CardDefaults.outlinedCardBorder()
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = if (isDefault) Icons.Filled.CheckCircle else Icons.Filled.Warning,
                                contentDescription = null,
                                tint = if (isDefault) Color(0xFF16A34A) else if (isEnabled) Color(0xFF2563EB) else Color(0xFFD97706),
                                modifier = Modifier.size(24.dp)
                            )
                            Spacer(modifier = Modifier.width(10.dp))
                            Column {
                                Text(
                                    text = if (isDefault) {
                                        stringResource(R.string.settings_ime_active)
                                    } else if (isEnabled) {
                                        stringResource(R.string.settings_ime_not_default)
                                    } else {
                                        stringResource(R.string.settings_ime_disabled)
                                    },
                                    fontSize = 15.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFF0F172A)
                                )
                                Text(
                                    text = if (isDefault) {
                                        stringResource(R.string.settings_ime_ready_desc)
                                    } else if (isEnabled) {
                                        stringResource(R.string.settings_ime_not_default_desc)
                                    } else {
                                        stringResource(R.string.settings_ime_disabled_desc)
                                    },
                                    fontSize = 12.sp,
                                    color = Color(0xFF64748B),
                                    modifier = Modifier.padding(top = 2.dp)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            if (!isEnabled) {
                                Button(
                                    onClick = {
                                        val intent = Intent(Settings.ACTION_INPUT_METHOD_SETTINGS).apply {
                                            flags = Intent.FLAG_ACTIVITY_NEW_TASK
                                        }
                                        context.startActivity(intent)
                                    },
                                    shape = RoundedCornerShape(8.dp),
                                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2563EB)),
                                    modifier = Modifier.weight(1f)
                                ) {
                                    Icon(Icons.Filled.Settings, contentDescription = null, modifier = Modifier.size(16.dp))
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(stringResource(R.string.settings_btn_open_settings), fontSize = 12.sp)
                                }
                            } else if (!isDefault) {
                                Button(
                                    onClick = {
                                        imm?.showInputMethodPicker()
                                    },
                                    shape = RoundedCornerShape(8.dp),
                                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2563EB)),
                                    modifier = Modifier.weight(1f)
                                ) {
                                    Text(stringResource(R.string.settings_btn_switch_ime), fontSize = 12.sp)
                                }
                            }

                            OutlinedButton(
                                onClick = onNavigateBack,
                                shape = RoundedCornerShape(8.dp),
                                modifier = Modifier.weight(1f)
                            ) {
                                Text(stringResource(R.string.settings_btn_playground), fontSize = 12.sp)
                            }
                        }
                    }
                }
            }

            // ==========================================
            // SECTION 1: INPUT & APPEARANCE
            // ==========================================
            item {
                Spacer(modifier = Modifier.height(6.dp))
                SettingsSectionHeader(title = stringResource(R.string.settings_section_input_appearance))
            }

            item {
                SettingsCardItem(
                    icon = Icons.Outlined.Language,
                    iconTint = Color(0xFF2563EB),
                    iconBg = Color(0xFFEFF6FF),
                    title = stringResource(R.string.settings_nav_languages_title),
                    subtitle = stringResource(R.string.settings_nav_languages_subtitle, enabledLangsCount),
                    badge = "$enabledLangsCount Active",
                    onClick = { onNavigateTo(SettingsDestination.LANGUAGES) }
                )
            }

            item {
                SettingsCardItem(
                    icon = Icons.Outlined.Palette,
                    iconTint = Color(0xFF8B5CF6),
                    iconBg = Color(0xFFF5F3FF),
                    title = stringResource(R.string.settings_nav_themes_title),
                    subtitle = stringResource(R.string.settings_nav_themes_subtitle, currentThemeName),
                    badge = currentThemeName,
                    onClick = { onNavigateTo(SettingsDestination.THEMES) }
                )
            }

            item {
                SettingsCardItem(
                    icon = Icons.Outlined.ColorLens,
                    iconTint = Color(0xFF16A34A),
                    iconBg = Color(0xFFDCFCE7),
                    title = "Custom Theme Maker",
                    subtitle = "Design theme with gallery photo, custom colors & keys",
                    badge = if (keyboardPreferences.isCustomThemeActive) "Active" else "Design",
                    onClick = { onNavigateTo(SettingsDestination.CUSTOM_THEME) }
                )
            }

            item {
                SettingsCardItem(
                    icon = Icons.Outlined.Widgets,
                    iconTint = Color(0xFF0D9488),
                    iconBg = Color(0xFFF0FDFA),
                    title = stringResource(R.string.settings_nav_toolbar_title),
                    subtitle = stringResource(R.string.settings_nav_toolbar_subtitle, activeToolsCount),
                    badge = "$activeToolsCount / 7",
                    onClick = { onNavigateTo(SettingsDestination.TOOLBAR) }
                )
            }

            item {
                SettingsCardItem(
                    icon = Icons.Outlined.Keyboard,
                    iconTint = Color(0xFF3B82F6),
                    iconBg = Color(0xFFEFF6FF),
                    title = stringResource(R.string.settings_nav_layout_title),
                    subtitle = stringResource(R.string.settings_nav_layout_subtitle),
                    badge = null,
                    onClick = { onNavigateTo(SettingsDestination.LAYOUT) }
                )
            }

            // ==========================================
            // SECTION 2: PRODUCTIVITY & TOOLS
            // ==========================================
            item {
                Spacer(modifier = Modifier.height(6.dp))
                SettingsSectionHeader(title = stringResource(R.string.settings_section_productivity))
            }

            item {
                SettingsCardItem(
                    icon = Icons.Outlined.ContentPaste,
                    iconTint = Color(0xFFEA580C),
                    iconBg = Color(0xFFFFF7ED),
                    title = stringResource(R.string.settings_nav_clipboard_title),
                    subtitle = stringResource(R.string.settings_nav_clipboard_subtitle),
                    badge = "Offline",
                    onClick = { onNavigateTo(SettingsDestination.CLIPBOARD) }
                )
            }

            item {
                SettingsCardItem(
                    icon = Icons.Outlined.SportsEsports,
                    iconTint = Color(0xFF10B981),
                    iconBg = Color(0xFFECFDF5),
                    title = stringResource(R.string.settings_nav_gaming_coding_title),
                    subtitle = stringResource(R.string.settings_nav_gaming_coding_subtitle),
                    badge = "WASD • Macros",
                    onClick = { onNavigateTo(SettingsDestination.GAMING_CODING) }
                )
            }

            // ==========================================
            // SECTION 3: PRIVACY & SYSTEM
            // ==========================================
            item {
                Spacer(modifier = Modifier.height(6.dp))
                SettingsSectionHeader(title = stringResource(R.string.settings_section_privacy_system))
            }

            item {
                SettingsCardItem(
                    icon = Icons.Outlined.Security,
                    iconTint = Color(0xFF059669),
                    iconBg = Color(0xFFECFDF5),
                    title = stringResource(R.string.settings_nav_privacy_title),
                    subtitle = stringResource(R.string.settings_nav_privacy_subtitle),
                    badge = "100% Private",
                    onClick = { onNavigateTo(SettingsDestination.PRIVACY) }
                )
            }

            item {
                SettingsCardItem(
                    icon = Icons.Outlined.Info,
                    iconTint = Color(0xFF64748B),
                    iconBg = Color(0xFFF1F5F9),
                    title = stringResource(R.string.settings_nav_about_title),
                    subtitle = stringResource(R.string.settings_nav_about_subtitle),
                    badge = "v1.0.0",
                    onClick = { onNavigateTo(SettingsDestination.ABOUT) }
                )
            }
        }
    }
}

@Composable
fun SettingsSectionHeader(title: String) {
    Text(
        text = title,
        fontSize = 11.5.sp,
        fontWeight = FontWeight.Bold,
        letterSpacing = 1.sp,
        color = Color(0xFF64748B),
        modifier = Modifier.padding(start = 4.dp, top = 6.dp, bottom = 2.dp)
    )
}

@Composable
private fun SettingsCardItem(
    icon: ImageVector,
    iconTint: Color,
    iconBg: Color,
    title: String,
    subtitle: String,
    badge: String? = null,
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
                .padding(horizontal = 14.dp, vertical = 13.dp),
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
                        .background(iconBg),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        tint = iconTint,
                        modifier = Modifier.size(20.dp)
                    )
                }

                Spacer(modifier = Modifier.width(14.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = title,
                        fontSize = 14.5.sp,
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

            if (badge != null) {
                Surface(
                    shape = RoundedCornerShape(6.dp),
                    color = Color(0xFFF1F5F9),
                    modifier = Modifier.padding(horizontal = 8.dp)
                ) {
                    Text(
                        text = badge,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Medium,
                        color = Color(0xFF475569),
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp)
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
