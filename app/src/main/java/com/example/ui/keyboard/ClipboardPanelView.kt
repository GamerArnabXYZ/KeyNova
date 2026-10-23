package com.example.ui.keyboard

import android.view.HapticFeedbackConstants
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.PushPin
import androidx.compose.material.icons.outlined.PushPin
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.ClipboardItem
import com.example.data.ClipboardRepository
import com.example.data.KeyboardThemeOption

@Composable
fun ClipboardPanelView(
    theme: KeyboardThemeOption,
    clipboardRepository: ClipboardRepository,
    onPasteText: (String) -> Unit,
    onClose: () -> Unit,
    modifier: Modifier = Modifier
) {
    val view = LocalView.current
    var clips by remember { mutableStateOf(clipboardRepository.getItems()) }
    var isManageMode by remember { mutableStateOf(false) }

    fun refresh() {
        clips = clipboardRepository.getItems()
    }

    val recentClips = remember(clips) { clips.filter { !it.isPinned } }
    val pinnedClips = remember(clips) { clips.filter { it.isPinned } }

    val bgColor = Color(theme.backgroundHex)
    val cardBg = if (theme == KeyboardThemeOption.LIGHT) Color(0xFFF1F5F9) else Color(0xFF262626)
    val textColor = Color(theme.textHex)
    val accentColor = Color(theme.accentHex)
    val borderColor = if (theme == KeyboardThemeOption.LIGHT) Color(0xFFE2E8F0) else Color(0xFF383838)

    Column(
        modifier = modifier
            .fillMaxWidth()
            .background(bgColor)
            .padding(horizontal = 12.dp, vertical = 6.dp)
    ) {
        // Subheader: "Recent" on left, "Manage" on right
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 4.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Recent",
                color = textColor,
                fontSize = 17.sp,
                fontWeight = FontWeight.SemiBold
            )

            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                if (isManageMode && clips.isNotEmpty()) {
                    Text(
                        text = "Clear Unpinned",
                        color = Color(0xFFEF4444),
                        fontSize = 13.sp,
                        fontWeight = FontWeight.SemiBold,
                        modifier = Modifier
                            .clickable {
                                clipboardRepository.clearAll()
                                refresh()
                            }
                            .padding(4.dp)
                    )
                }

                Text(
                    text = if (isManageMode) "Done" else "Manage",
                    color = accentColor,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.SemiBold,
                    modifier = Modifier
                        .clickable { isManageMode = !isManageMode }
                        .padding(4.dp)
                )
            }
        }

        // Recent items container (horizontal or compact rows)
        if (recentClips.isNotEmpty()) {
            LazyRow(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(recentClips, key = { it.id }) { item ->
                    Box(
                        modifier = Modifier
                            .widthIn(min = 120.dp, max = 220.dp)
                            .height(82.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(cardBg)
                            .border(1.dp, borderColor, RoundedCornerShape(12.dp))
                            .clickable {
                                view.performHapticFeedback(HapticFeedbackConstants.KEYBOARD_TAP)
                                onPasteText(item.text)
                            }
                            .padding(8.dp)
                    ) {
                        // Pin Icon at top-left
                        Box(
                            modifier = Modifier
                                .size(24.dp)
                                .clip(CircleShape)
                                .background(Color.White.copy(alpha = 0.15f))
                                .clickable {
                                    clipboardRepository.togglePin(item.id)
                                    refresh()
                                },
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Outlined.PushPin,
                                contentDescription = "Pin to keep",
                                tint = textColor.copy(alpha = 0.7f),
                                modifier = Modifier.size(14.dp)
                            )
                        }

                        if (isManageMode) {
                            Icon(
                                imageVector = Icons.Default.Delete,
                                contentDescription = "Delete",
                                tint = Color(0xFFEF4444),
                                modifier = Modifier
                                    .align(Alignment.TopEnd)
                                    .size(18.dp)
                                    .clickable {
                                        clipboardRepository.deleteItem(item.id)
                                        refresh()
                                    }
                            )
                        }

                        // Text
                        Text(
                            text = item.text,
                            color = textColor,
                            fontSize = 14.sp,
                            maxLines = 2,
                            overflow = TextOverflow.Ellipsis,
                            modifier = Modifier
                                .align(Alignment.BottomStart)
                                .padding(top = 26.dp)
                        )
                    }
                }
            }
        } else {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(cardBg.copy(alpha = 0.5f)),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "No recent clips yet. Copied texts appear here.",
                    color = textColor.copy(alpha = 0.6f),
                    fontSize = 12.sp
                )
            }
        }

        // Subtitle Tip matching image:
        Text(
            text = "Recently copied texts are automatically deleted after 1 hour. Pin them to keep them.",
            color = textColor.copy(alpha = 0.6f),
            fontSize = 11.sp,
            fontStyle = FontStyle.Italic,
            modifier = Modifier.padding(vertical = 6.dp)
        )

        // "Pinned" Header
        Text(
            text = "Pinned",
            color = textColor,
            fontSize = 16.sp,
            fontWeight = FontWeight.SemiBold,
            modifier = Modifier.padding(top = 4.dp, bottom = 6.dp)
        )

        // Pinned Items 2-Column Grid
        LazyColumn(
            modifier = Modifier
                .fillMaxWidth()
                .height(86.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            val chunked = pinnedClips.chunked(2)
            items(chunked) { pair ->
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    pair.forEach { pinnedItem ->
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .height(64.dp)
                                .clip(RoundedCornerShape(10.dp))
                                .background(cardBg)
                                .border(1.dp, borderColor, RoundedCornerShape(10.dp))
                                .clickable {
                                    view.performHapticFeedback(HapticFeedbackConstants.KEYBOARD_TAP)
                                    onPasteText(pinnedItem.text)
                                }
                                .padding(horizontal = 10.dp, vertical = 6.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.Top
                            ) {
                                Text(
                                    text = pinnedItem.text,
                                    color = textColor,
                                    fontSize = 13.sp,
                                    maxLines = 2,
                                    overflow = TextOverflow.Ellipsis,
                                    modifier = Modifier.weight(1f)
                                )

                                Icon(
                                    imageVector = Icons.Filled.PushPin,
                                    contentDescription = "Pinned",
                                    tint = accentColor,
                                    modifier = Modifier
                                        .size(16.dp)
                                        .clickable {
                                            clipboardRepository.togglePin(pinnedItem.id)
                                            refresh()
                                        }
                                )
                            }
                        }
                    }
                    if (pair.size == 1) {
                        Spacer(modifier = Modifier.weight(1f))
                    }
                }
            }
        }
    }
}
