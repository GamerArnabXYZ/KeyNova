package com.example.ui.keyboard

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.ActiveKeyboardPage
import com.example.data.KeyboardPreferences
import com.example.data.KeyboardThemeOption
import com.example.data.OneHandedMode
import com.example.data.ToolbarTool
import com.example.ui.screens.getToolbarIconForTool

@Composable
fun KeyboardToolbar(
    theme: KeyboardThemeOption,
    activePage: ActiveKeyboardPage,
    keyboardPreferences: KeyboardPreferences,
    onPageSelected: (ActiveKeyboardPage) -> Unit,
    onCycleTheme: () -> Unit,
    onOpenSettings: () -> Unit,
    onOpenToolbarSettings: () -> Unit,
    onToggleOneHanded: () -> Unit,
    onClearAll: () -> Unit,
    onCycleCase: () -> Unit,
    onVoiceTyping: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    val bgColor = Color(theme.backgroundHex)
    val textColor = Color(theme.textHex)
    val accentColor = Color(theme.accentHex)

    val enabledTools = keyboardPreferences.enabledToolbarTools.take(ToolbarTool.MAX_ENABLED_TOOLS)

    Row(
        modifier = modifier
            .fillMaxWidth()
            .height(44.dp)
            .background(bgColor)
            .padding(horizontal = 6.dp, vertical = 3.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        // Scrollable Row of Active Tools (up to 7 tools)
        Row(
            modifier = Modifier
                .weight(1f, fill = false)
                .horizontalScroll(rememberScrollState()),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            enabledTools.forEach { tool ->
                when (tool) {
                    ToolbarTool.EMOJI -> {
                        val isSelected = activePage == ActiveKeyboardPage.EMOJI
                        ModernToolbarPill(
                            icon = getToolbarIconForTool(tool),
                            contentDescription = "Emoji",
                            isSelected = isSelected,
                            textColor = textColor,
                            accentColor = accentColor,
                            testTag = "toolbar_tool_emoji",
                            onClick = {
                                if (isSelected) onPageSelected(ActiveKeyboardPage.LETTERS)
                                else onPageSelected(ActiveKeyboardPage.EMOJI)
                            }
                        )
                    }

                    ToolbarTool.CLIPBOARD -> {
                        val isSelected = activePage == ActiveKeyboardPage.CLIPBOARD
                        ModernToolbarPill(
                            icon = getToolbarIconForTool(tool),
                            contentDescription = "Clipboard",
                            isSelected = isSelected,
                            textColor = textColor,
                            accentColor = accentColor,
                            testTag = "toolbar_tool_clipboard",
                            onClick = {
                                if (isSelected) onPageSelected(ActiveKeyboardPage.LETTERS)
                                else onPageSelected(ActiveKeyboardPage.CLIPBOARD)
                            }
                        )
                    }

                    ToolbarTool.TEXT_EDITING -> {
                        val isSelected = activePage == ActiveKeyboardPage.TEXT_EDITING
                        ModernToolbarPill(
                            icon = getToolbarIconForTool(tool),
                            contentDescription = "Text Editing D-Pad",
                            isSelected = isSelected,
                            textColor = textColor,
                            accentColor = accentColor,
                            testTag = "toolbar_tool_text_editing",
                            onClick = {
                                if (isSelected) onPageSelected(ActiveKeyboardPage.LETTERS)
                                else onPageSelected(ActiveKeyboardPage.TEXT_EDITING)
                            }
                        )
                    }

                    ToolbarTool.NUMPAD -> {
                        val isSelected = activePage == ActiveKeyboardPage.NUMPAD
                        ModernToolbarPill(
                            icon = getToolbarIconForTool(tool),
                            contentDescription = "Numeric Keypad",
                            isSelected = isSelected,
                            textColor = textColor,
                            accentColor = accentColor,
                            testTag = "toolbar_tool_numpad",
                            onClick = {
                                if (isSelected) onPageSelected(ActiveKeyboardPage.LETTERS)
                                else onPageSelected(ActiveKeyboardPage.NUMPAD)
                            }
                        )
                    }

                    ToolbarTool.SNIPPETS -> {
                        val isSelected = activePage == ActiveKeyboardPage.SNIPPETS
                        ModernToolbarPill(
                            icon = getToolbarIconForTool(tool),
                            contentDescription = "Quick Snippets",
                            isSelected = isSelected,
                            textColor = textColor,
                            accentColor = accentColor,
                            testTag = "toolbar_tool_snippets",
                            onClick = {
                                if (isSelected) onPageSelected(ActiveKeyboardPage.LETTERS)
                                else onPageSelected(ActiveKeyboardPage.SNIPPETS)
                            }
                        )
                    }

                    ToolbarTool.CODING -> {
                        val isSelected = activePage == ActiveKeyboardPage.CODING
                        ModernToolbarPill(
                            icon = getToolbarIconForTool(tool),
                            contentDescription = "Coding Symbols",
                            isSelected = isSelected,
                            textColor = textColor,
                            accentColor = accentColor,
                            testTag = "toolbar_tool_coding",
                            onClick = {
                                if (isSelected) onPageSelected(ActiveKeyboardPage.LETTERS)
                                else onPageSelected(ActiveKeyboardPage.CODING)
                            }
                        )
                    }

                    ToolbarTool.GAMING -> {
                        val isSelected = activePage == ActiveKeyboardPage.GAMING
                        ModernToolbarPill(
                            icon = getToolbarIconForTool(tool),
                            contentDescription = "Gaming Keypad",
                            isSelected = isSelected,
                            textColor = textColor,
                            accentColor = accentColor,
                            testTag = "toolbar_tool_gaming",
                            onClick = {
                                if (isSelected) onPageSelected(ActiveKeyboardPage.LETTERS)
                                else onPageSelected(ActiveKeyboardPage.GAMING)
                            }
                        )
                    }

                    ToolbarTool.THEMES -> {
                        ModernToolbarPill(
                            icon = getToolbarIconForTool(tool),
                            contentDescription = "Theme Switcher",
                            isSelected = false,
                            textColor = textColor,
                            accentColor = accentColor,
                            testTag = "toolbar_tool_themes",
                            onClick = onCycleTheme
                        )
                    }

                    ToolbarTool.ONE_HANDED -> {
                        val isSelected = keyboardPreferences.oneHandedMode != OneHandedMode.OFF
                        ModernToolbarPill(
                            icon = getToolbarIconForTool(tool),
                            contentDescription = "One-Handed Mode",
                            isSelected = isSelected,
                            textColor = textColor,
                            accentColor = accentColor,
                            testTag = "toolbar_tool_one_handed",
                            onClick = onToggleOneHanded
                        )
                    }

                    ToolbarTool.CASE_CONVERTER -> {
                        ModernToolbarPill(
                            icon = getToolbarIconForTool(tool),
                            contentDescription = "Case Converter",
                            isSelected = false,
                            textColor = textColor,
                            accentColor = accentColor,
                            testTag = "toolbar_tool_case_converter",
                            onClick = onCycleCase
                        )
                    }

                    ToolbarTool.CLEAR_TEXT -> {
                        ModernToolbarPill(
                            icon = getToolbarIconForTool(tool),
                            contentDescription = "Clear All Text",
                            isSelected = false,
                            textColor = textColor,
                            accentColor = accentColor,
                            testTag = "toolbar_tool_clear_text",
                            onClick = onClearAll
                        )
                    }

                    ToolbarTool.VOICE_TYPING -> {
                        ModernToolbarPill(
                            icon = getToolbarIconForTool(tool),
                            contentDescription = "Voice Typing",
                            isSelected = false,
                            textColor = textColor,
                            accentColor = accentColor,
                            testTag = "toolbar_tool_voice_typing",
                            onClick = onVoiceTyping
                        )
                    }

                    ToolbarTool.SETTINGS -> {
                        ModernToolbarPill(
                            icon = getToolbarIconForTool(tool),
                            contentDescription = "KeyNova Settings",
                            isSelected = false,
                            textColor = textColor,
                            accentColor = accentColor,
                            testTag = "toolbar_tool_settings",
                            onClick = onOpenSettings
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.width(4.dp))

        // Right-end: Quick Toolbar Customization shortcut button
        Box(
            modifier = Modifier
                .size(36.dp)
                .clip(CircleShape)
                .background(accentColor.copy(alpha = 0.12f))
                .border(1.dp, accentColor.copy(alpha = 0.25f), CircleShape)
                .clickable(onClick = onOpenToolbarSettings)
                .testTag("toolbar_customize_button"),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Outlined.Widgets,
                contentDescription = "Customize Toolbar",
                tint = accentColor,
                modifier = Modifier.size(19.dp)
            )
        }
    }
}

@Composable
private fun ModernToolbarPill(
    icon: ImageVector,
    contentDescription: String,
    isSelected: Boolean,
    textColor: Color,
    accentColor: Color,
    testTag: String,
    onClick: () -> Unit
) {
    val animatedBg by animateColorAsState(
        targetValue = if (isSelected) accentColor.copy(alpha = 0.22f) else textColor.copy(alpha = 0.05f),
        animationSpec = tween(durationMillis = 180),
        label = "ToolbarPillBg"
    )

    val animatedBorder by animateColorAsState(
        targetValue = if (isSelected) accentColor.copy(alpha = 0.55f) else textColor.copy(alpha = 0.10f),
        animationSpec = tween(durationMillis = 180),
        label = "ToolbarPillBorder"
    )

    val animatedIconColor by animateColorAsState(
        targetValue = if (isSelected) accentColor else textColor.copy(alpha = 0.85f),
        animationSpec = tween(durationMillis = 180),
        label = "ToolbarPillIcon"
    )

    Box(
        modifier = Modifier
            .size(width = 38.dp, height = 34.dp)
            .clip(RoundedCornerShape(10.dp))
            .background(animatedBg)
            .border(1.dp, animatedBorder, RoundedCornerShape(10.dp))
            .clickable(onClick = onClick)
            .testTag(testTag),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = icon,
            contentDescription = contentDescription,
            tint = animatedIconColor,
            modifier = Modifier.size(20.dp)
        )
    }
}
