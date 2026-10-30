package com.example.ui.keyboard

import android.view.HapticFeedbackConstants
import android.widget.Toast
import androidx.compose.foundation.BorderStroke
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
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.ContentPaste
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.PushPin
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.outlined.PushPin
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.ClipboardItem
import com.example.data.ClipboardRepository
import com.example.data.KeyboardThemeOption
import kotlinx.coroutines.delay

@Composable
fun ClipboardPanelView(
    theme: KeyboardThemeOption,
    clipboardRepository: ClipboardRepository,
    onPasteText: (String) -> Unit,
    onClose: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val view = LocalView.current
    val clipsList by clipboardRepository.itemsFlow.collectAsState()
    var isManageMode by remember { mutableStateOf(false) }
    var systemClipText by remember { mutableStateOf<String?>(null) }
    var syncTrigger by remember { mutableIntStateOf(0) }

    fun refreshClipboard() {
        clipboardRepository.syncFromSystemClipboard(context)
        systemClipText = clipboardRepository.getSystemClipboardText(context)
    }

    LaunchedEffect(syncTrigger) {
        refreshClipboard()
        // Retry shortly after IME window is fully attached
        delay(200)
        refreshClipboard()
    }

    val recentClips = remember(clipsList) {
        clipsList.filter { !it.isPinned }.sortedByDescending { it.timestamp }
    }
    val pinnedClips = remember(clipsList) {
        clipsList.filter { it.isPinned }.sortedByDescending { it.timestamp }
    }

    val bgColor = Color(theme.backgroundHex)
    val cardBg = if (theme == KeyboardThemeOption.LIGHT) Color(0xFFF1F5F9) else Color(0xFF262626)
    val textColor = Color(theme.textHex)
    val accentColor = Color(theme.accentHex)
    val borderColor = if (theme == KeyboardThemeOption.LIGHT) Color(0xFFCBD5E1) else Color(0xFF404040)

    Column(
        modifier = modifier
            .fillMaxWidth()
            .background(bgColor)
            .padding(horizontal = 12.dp, vertical = 4.dp)
    ) {
        // Top Toolbar: "← Clipboard" on left, "Sync", "Clear Unpinned", "Manage / Done" on right
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 2.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                IconButton(
                    onClick = {
                        view.performHapticFeedback(HapticFeedbackConstants.KEYBOARD_TAP)
                        onClose()
                    },
                    modifier = Modifier.size(32.dp)
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Back to Keyboard",
                        tint = textColor,
                        modifier = Modifier.size(18.dp)
                    )
                }

                Text(
                    text = "Clipboard",
                    color = textColor,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold
                )
            }

            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                // Sync button
                IconButton(
                    onClick = {
                        view.performHapticFeedback(HapticFeedbackConstants.KEYBOARD_TAP)
                        refreshClipboard()
                        syncTrigger++
                        Toast.makeText(context, "Clipboard synced", Toast.LENGTH_SHORT).show()
                    },
                    modifier = Modifier.size(28.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Refresh,
                        contentDescription = "Sync System Clipboard",
                        tint = accentColor,
                        modifier = Modifier.size(18.dp)
                    )
                }

                if (isManageMode && clipsList.isNotEmpty()) {
                    Text(
                        text = "Clear Unpinned",
                        color = Color(0xFFEF4444),
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold,
                        modifier = Modifier
                            .clickable {
                                view.performHapticFeedback(HapticFeedbackConstants.KEYBOARD_TAP)
                                clipboardRepository.clearAll()
                            }
                            .padding(4.dp)
                    )
                }

                Text(
                    text = if (isManageMode) "Done" else "Manage",
                    color = accentColor,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.SemiBold,
                    modifier = Modifier
                        .clickable {
                            view.performHapticFeedback(HapticFeedbackConstants.KEYBOARD_TAP)
                            isManageMode = !isManageMode
                        }
                        .padding(4.dp)
                )
            }
        }

        // Quick Paste Banner (if system clipboard has text ready to paste)
        if (!systemClipText.isNullOrBlank()) {
            val clipSnippet = systemClipText!!.replace("\n", " ").trim()
            val displayText = if (clipSnippet.length > 36) clipSnippet.take(34) + "…" else clipSnippet

            Surface(
                onClick = {
                    view.performHapticFeedback(HapticFeedbackConstants.KEYBOARD_TAP)
                    onPasteText(systemClipText!!)
                },
                shape = RoundedCornerShape(8.dp),
                color = accentColor.copy(alpha = 0.15f),
                border = BorderStroke(1.dp, accentColor.copy(alpha = 0.5f)),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 3.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 10.dp, vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.weight(1f)
                    ) {
                        Icon(
                            imageVector = Icons.Default.ContentPaste,
                            contentDescription = "Paste",
                            tint = accentColor,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "Paste: \"$displayText\"",
                            color = textColor,
                            fontSize = 12.5.sp,
                            fontWeight = FontWeight.Medium,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }

                    Text(
                        text = "Tap to paste",
                        color = accentColor,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(start = 6.dp)
                    )
                }
            }
        }

        // Recent items container (horizontal cards)
        if (recentClips.isNotEmpty()) {
            Text(
                text = "Recent",
                color = textColor.copy(alpha = 0.75f),
                fontSize = 12.sp,
                fontWeight = FontWeight.SemiBold,
                modifier = Modifier.padding(top = 2.dp, bottom = 2.dp)
            )

            LazyRow(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 2.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(recentClips, key = { it.id }) { item ->
                    Box(
                        modifier = Modifier
                            .widthIn(min = 120.dp, max = 200.dp)
                            .height(72.dp)
                            .clip(RoundedCornerShape(10.dp))
                            .background(cardBg)
                            .border(1.dp, borderColor, RoundedCornerShape(10.dp))
                            .clickable {
                                view.performHapticFeedback(HapticFeedbackConstants.KEYBOARD_TAP)
                                onPasteText(item.text)
                            }
                            .padding(8.dp)
                    ) {
                        // Pin Icon at top-left
                        Box(
                            modifier = Modifier
                                .size(22.dp)
                                .clip(CircleShape)
                                .background(textColor.copy(alpha = 0.08f))
                                .clickable {
                                    view.performHapticFeedback(HapticFeedbackConstants.KEYBOARD_TAP)
                                    clipboardRepository.togglePin(item.id)
                                },
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Outlined.PushPin,
                                contentDescription = "Pin to keep",
                                tint = textColor.copy(alpha = 0.7f),
                                modifier = Modifier.size(13.dp)
                            )
                        }

                        if (isManageMode) {
                            Icon(
                                imageVector = Icons.Default.Delete,
                                contentDescription = "Delete",
                                tint = Color(0xFFEF4444),
                                modifier = Modifier
                                    .align(Alignment.TopEnd)
                                    .size(16.dp)
                                    .clickable {
                                        view.performHapticFeedback(HapticFeedbackConstants.KEYBOARD_TAP)
                                        clipboardRepository.deleteItem(item.id)
                                    }
                            )
                        }

                        // Text content
                        Text(
                            text = item.text,
                            color = textColor,
                            fontSize = 13.sp,
                            maxLines = 2,
                            overflow = TextOverflow.Ellipsis,
                            modifier = Modifier
                                .align(Alignment.BottomStart)
                                .padding(top = 22.dp)
                        )
                    }
                }
            }
        } else if (systemClipText.isNullOrBlank()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(cardBg.copy(alpha = 0.6f))
                    .clickable {
                        view.performHapticFeedback(HapticFeedbackConstants.KEYBOARD_TAP)
                        refreshClipboard()
                        syncTrigger++
                    },
                contentAlignment = Alignment.Center
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.ContentPaste,
                        contentDescription = null,
                        tint = textColor.copy(alpha = 0.6f),
                        modifier = Modifier.size(16.dp)
                    )
                    Text(
                        text = "Copy text in any app, or tap here to sync phone clipboard",
                        color = textColor.copy(alpha = 0.7f),
                        fontSize = 11.5.sp
                    )
                }
            }
        }

        // Pinned Header & Items
        if (pinnedClips.isNotEmpty()) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 4.dp, bottom = 2.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "Pinned",
                    color = textColor.copy(alpha = 0.75f),
                    fontSize = 12.sp,
                    fontWeight = FontWeight.SemiBold
                )

                Text(
                    text = "${pinnedClips.size} saved",
                    color = accentColor,
                    fontSize = 11.sp
                )
            }

            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                verticalArrangement = Arrangement.spacedBy(5.dp)
            ) {
                val chunked = pinnedClips.chunked(2)
                items(chunked) { pair ->
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        pair.forEach { pinnedItem ->
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .height(54.dp)
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(cardBg)
                                    .border(1.dp, borderColor, RoundedCornerShape(8.dp))
                                    .clickable {
                                        view.performHapticFeedback(HapticFeedbackConstants.KEYBOARD_TAP)
                                        onPasteText(pinnedItem.text)
                                    }
                                    .padding(horizontal = 8.dp, vertical = 6.dp)
                            ) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.Top
                                ) {
                                    Text(
                                        text = pinnedItem.text,
                                        color = textColor,
                                        fontSize = 12.5.sp,
                                        maxLines = 2,
                                        overflow = TextOverflow.Ellipsis,
                                        modifier = Modifier.weight(1f)
                                    )

                                    if (isManageMode) {
                                        Icon(
                                            imageVector = Icons.Default.Delete,
                                            contentDescription = "Delete",
                                            tint = Color(0xFFEF4444),
                                            modifier = Modifier
                                                .size(15.dp)
                                                .clickable {
                                                    view.performHapticFeedback(HapticFeedbackConstants.KEYBOARD_TAP)
                                                    clipboardRepository.deleteItem(pinnedItem.id)
                                                }
                                        )
                                    } else {
                                        Icon(
                                            imageVector = Icons.Filled.PushPin,
                                            contentDescription = "Pinned",
                                            tint = accentColor,
                                            modifier = Modifier
                                                .size(15.dp)
                                                .clickable {
                                                    view.performHapticFeedback(HapticFeedbackConstants.KEYBOARD_TAP)
                                                    clipboardRepository.togglePin(pinnedItem.id)
                                                }
                                        )
                                    }
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
}
