package com.example.ui.keyboard

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.ActiveKeyboardPage
import com.example.data.KeyboardThemeOption

@Composable
fun KeyboardToolbar(
    theme: KeyboardThemeOption,
    activePage: ActiveKeyboardPage,
    onPageSelected: (ActiveKeyboardPage) -> Unit,
    onCycleTheme: () -> Unit,
    onOpenSettings: () -> Unit,
    onToggleOneHanded: () -> Unit,
    modifier: Modifier = Modifier
) {
    val bgColor = Color(theme.backgroundHex)
    val textColor = Color(theme.textHex)
    val accentColor = Color(theme.accentHex)

    Row(
        modifier = modifier
            .fillMaxWidth()
            .background(bgColor)
            .padding(horizontal = 4.dp, vertical = 3.dp)
            .horizontalScroll(rememberScrollState()),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            // 1. History / Recent
            ToolbarIconItem(
                icon = Icons.Outlined.History,
                contentDescription = "History",
                isSelected = false,
                textColor = textColor,
                accentColor = accentColor,
                onClick = { onPageSelected(ActiveKeyboardPage.CLIPBOARD) }
            )

            // 2. Emoji
            ToolbarIconItem(
                icon = Icons.Outlined.SentimentSatisfied,
                contentDescription = "Emoji",
                isSelected = activePage == ActiveKeyboardPage.EMOJI,
                textColor = textColor,
                accentColor = accentColor,
                onClick = {
                    if (activePage == ActiveKeyboardPage.EMOJI) onPageSelected(ActiveKeyboardPage.LETTERS)
                    else onPageSelected(ActiveKeyboardPage.EMOJI)
                }
            )

            // 3. Theme / Store
            ToolbarIconItem(
                icon = Icons.Outlined.Storefront,
                contentDescription = "Theme Store",
                isSelected = false,
                textColor = textColor,
                accentColor = accentColor,
                onClick = onCycleTheme
            )

            // 4. Clipboard
            ToolbarIconItem(
                icon = Icons.Outlined.ContentPaste,
                contentDescription = "Clipboard",
                isSelected = activePage == ActiveKeyboardPage.CLIPBOARD,
                textColor = textColor,
                accentColor = accentColor,
                onClick = {
                    if (activePage == ActiveKeyboardPage.CLIPBOARD) onPageSelected(ActiveKeyboardPage.LETTERS)
                    else onPageSelected(ActiveKeyboardPage.CLIPBOARD)
                }
            )

            // 5. Text Editing (<|I|>)
            ToolbarTextEditorBadge(
                isSelected = activePage == ActiveKeyboardPage.TEXT_EDITING,
                textColor = textColor,
                accentColor = accentColor,
                onClick = {
                    if (activePage == ActiveKeyboardPage.TEXT_EDITING) onPageSelected(ActiveKeyboardPage.LETTERS)
                    else onPageSelected(ActiveKeyboardPage.TEXT_EDITING)
                }
            )

            // 6. Number Pad ([123])
            ToolbarNumPadBadge(
                isSelected = activePage == ActiveKeyboardPage.NUMPAD,
                textColor = textColor,
                accentColor = accentColor,
                onClick = {
                    if (activePage == ActiveKeyboardPage.NUMPAD) onPageSelected(ActiveKeyboardPage.LETTERS)
                    else onPageSelected(ActiveKeyboardPage.NUMPAD)
                }
            )

            // 7. Sparkles / Coding / Gaming Tools
            ToolbarIconItem(
                icon = Icons.Outlined.AutoAwesome,
                contentDescription = "Tools",
                isSelected = activePage == ActiveKeyboardPage.CODING || activePage == ActiveKeyboardPage.GAMING,
                textColor = Color(0xFF10B981),
                accentColor = Color(0xFF10B981),
                onClick = {
                    when (activePage) {
                        ActiveKeyboardPage.CODING -> onPageSelected(ActiveKeyboardPage.GAMING)
                        ActiveKeyboardPage.GAMING -> onPageSelected(ActiveKeyboardPage.LETTERS)
                        else -> onPageSelected(ActiveKeyboardPage.CODING)
                    }
                }
            )

            // 8. Settings
            ToolbarIconItem(
                icon = Icons.Outlined.Settings,
                contentDescription = "Settings",
                isSelected = false,
                textColor = textColor,
                accentColor = accentColor,
                onClick = onOpenSettings
            )

            // 9. More (...)
            ToolbarIconItem(
                icon = Icons.Outlined.MoreHoriz,
                contentDescription = "More",
                isSelected = false,
                textColor = textColor,
                accentColor = accentColor,
                onClick = onToggleOneHanded
            )
        }
    }
}

@Composable
private fun ToolbarIconItem(
    icon: ImageVector,
    contentDescription: String,
    isSelected: Boolean,
    textColor: Color,
    accentColor: Color,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .size(38.dp)
            .clip(CircleShape)
            .background(if (isSelected) accentColor.copy(alpha = 0.22f) else Color.Transparent)
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = icon,
            contentDescription = contentDescription,
            tint = if (isSelected) accentColor else textColor.copy(alpha = 0.85f),
            modifier = Modifier.size(22.dp)
        )
    }
}

@Composable
private fun ToolbarTextEditorBadge(
    isSelected: Boolean,
    textColor: Color,
    accentColor: Color,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .height(34.dp)
            .padding(horizontal = 2.dp)
            .clip(RoundedCornerShape(8.dp))
            .background(if (isSelected) accentColor.copy(alpha = 0.22f) else Color.Transparent)
            .border(
                1.dp,
                if (isSelected) accentColor else textColor.copy(alpha = 0.25f),
                RoundedCornerShape(8.dp)
            )
            .clickable(onClick = onClick)
            .padding(horizontal = 6.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = "⟨ I ⟩",
            color = if (isSelected) accentColor else textColor,
            fontSize = 13.sp,
            fontWeight = FontWeight.Bold
        )
    }
}

@Composable
private fun ToolbarNumPadBadge(
    isSelected: Boolean,
    textColor: Color,
    accentColor: Color,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .height(34.dp)
            .padding(horizontal = 2.dp)
            .clip(RoundedCornerShape(8.dp))
            .background(if (isSelected) accentColor.copy(alpha = 0.22f) else Color.Transparent)
            .border(
                1.dp,
                if (isSelected) accentColor else textColor.copy(alpha = 0.25f),
                RoundedCornerShape(8.dp)
            )
            .clickable(onClick = onClick)
            .padding(horizontal = 6.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = "123",
            color = if (isSelected) accentColor else textColor,
            fontSize = 13.sp,
            fontWeight = FontWeight.Bold
        )
    }
}
