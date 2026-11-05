package com.example.ui.keyboard

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.SubcomposeAsyncImage
import com.example.data.KeyboardThemeOption

fun getCombinationsForEmojis(emojis: List<String>): List<EmojiKitchenCombo> {
    if (emojis.isEmpty()) {
        return EmojiKitchenCatalog.popularCombos
    }

    val result = mutableListOf<EmojiKitchenCombo>()
    val e1 = emojis.first()
    val e2 = if (emojis.size >= 2) emojis[1] else null

    if (e2 != null && e2.isNotBlank() && e1 != e2) {
        // Two distinct emojis:
        // 1. Primary combination (e1 + e2)
        result.add(
            EmojiKitchenCombo(
                emoji1 = e1,
                emoji2 = e2,
                title = "$e1 + $e2 Mashup",
                imageUrl = EmojiKitchenCatalog.getKitchenUrl(e1, e2)
            )
        )
        // 2. Flipped combination (e2 + e1)
        result.add(
            EmojiKitchenCombo(
                emoji1 = e2,
                emoji2 = e1,
                title = "$e2 + $e1 Mix",
                imageUrl = EmojiKitchenCatalog.getKitchenUrl(e2, e1)
            )
        )
        // 3. Double e1
        result.add(
            EmojiKitchenCombo(
                emoji1 = e1,
                emoji2 = e1,
                title = "Mega $e1",
                imageUrl = EmojiKitchenCatalog.getKitchenUrl(e1, e1)
            )
        )
        // 4. Double e2
        result.add(
            EmojiKitchenCombo(
                emoji1 = e2,
                emoji2 = e2,
                title = "Mega $e2",
                imageUrl = EmojiKitchenCatalog.getKitchenUrl(e2, e2)
            )
        )
        // 5. Popular companion mixes for e1 & e2
        val companions = listOf("🔥", "❤️", "😂", "🥺", "✨", "💀", "😭", "🎉", "🥰")
        for (comp in companions) {
            if (comp != e1 && comp != e2) {
                result.add(
                    EmojiKitchenCombo(
                        emoji1 = e1,
                        emoji2 = comp,
                        title = "$e1 + $comp",
                        imageUrl = EmojiKitchenCatalog.getKitchenUrl(e1, comp)
                    )
                )
                result.add(
                    EmojiKitchenCombo(
                        emoji1 = e2,
                        emoji2 = comp,
                        title = "$e2 + $comp",
                        imageUrl = EmojiKitchenCatalog.getKitchenUrl(e2, comp)
                    )
                )
            }
        }
    } else {
        // Single emoji (or two identical emojis):
        // 1. Double
        result.add(
            EmojiKitchenCombo(
                emoji1 = e1,
                emoji2 = e1,
                title = "Mega $e1",
                imageUrl = EmojiKitchenCatalog.getKitchenUrl(e1, e1)
            )
        )
        // 2. High-affinity companions
        val companions = listOf(
            "🔥", "❤️", "😂", "😭", "✨", "🥺", "💀", "🥰", "🎉",
            "😎", "💩", "🍕", "🐶", "🐱", "🌈", "☕", "🧠", "👻"
        )
        for (comp in companions) {
            if (comp != e1) {
                result.add(
                    EmojiKitchenCombo(
                        emoji1 = e1,
                        emoji2 = comp,
                        title = "$e1 + $comp",
                        imageUrl = EmojiKitchenCatalog.getKitchenUrl(e1, comp)
                    )
                )
            }
        }
    }

    return result
}

@Composable
fun EmojiKitchenSuggestionStrip(
    emojis: List<String>,
    theme: KeyboardThemeOption,
    onSelectSticker: (url: String, mimeType: String, description: String) -> Unit,
    modifier: Modifier = Modifier,
    height: Dp = 40.dp,
    showPrefixBadge: Boolean = true,
    onToggleTools: (() -> Unit)? = null
) {
    val accentColor = Color(theme.accentHex)
    val textColor = Color(theme.textHex)
    val surfaceColor = Color(theme.surfaceHex)

    val combinations = remember(emojis) {
        getCombinationsForEmojis(emojis)
    }

    Row(
        modifier = modifier
            .fillMaxWidth()
            .height(height)
            .background(Color(theme.backgroundHex))
            .padding(horizontal = 4.dp, vertical = 2.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Left badge / Toggle tools button if provided
        if (onToggleTools != null) {
            IconButton(
                onClick = onToggleTools,
                modifier = Modifier
                    .size(32.dp)
                    .clip(CircleShape)
                    .background(surfaceColor)
            ) {
                Icon(
                    imageVector = Icons.Default.AutoAwesome,
                    contentDescription = "Switch to Toolbar",
                    tint = accentColor,
                    modifier = Modifier.size(16.dp)
                )
            }
            Spacer(modifier = Modifier.width(4.dp))
        } else if (showPrefixBadge && emojis.isNotEmpty()) {
            Box(
                modifier = Modifier
                    .height(30.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(accentColor.copy(alpha = 0.18f))
                    .border(0.8.dp, accentColor.copy(alpha = 0.4f), RoundedCornerShape(8.dp))
                    .padding(horizontal = 6.dp),
                contentAlignment = Alignment.Center
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(2.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.AutoAwesome,
                        contentDescription = null,
                        tint = accentColor,
                        modifier = Modifier.size(12.dp)
                    )
                    Text(
                        text = emojis.take(2).joinToString("+"),
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = textColor
                    )
                }
            }
            Spacer(modifier = Modifier.width(4.dp))
        }

        // Horizontal scrollable sticker suggestions
        Row(
            modifier = Modifier
                .weight(1f)
                .horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(5.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            combinations.forEach { combo ->
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = surfaceColor,
                    border = BorderStroke(0.8.dp, accentColor.copy(alpha = 0.35f)),
                    modifier = Modifier
                        .size(34.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .clickable {
                            onSelectSticker(combo.imageUrl, "image/png", "${combo.emoji1}+${combo.emoji2} ${combo.title}")
                        }
                        .testTag("kitchen_suggestion_${combo.emoji1}_${combo.emoji2}")
                ) {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        SubcomposeAsyncImage(
                            model = combo.imageUrl,
                            contentDescription = combo.title,
                            contentScale = ContentScale.Fit,
                            loading = {
                                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                                    Text(
                                        text = "${combo.emoji1}${combo.emoji2}",
                                        fontSize = 10.sp
                                    )
                                }
                            },
                            error = {
                                Box(
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .background(accentColor.copy(alpha = 0.12f)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = "${combo.emoji1}${combo.emoji2}",
                                        fontSize = 11.sp
                                    )
                                }
                            },
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(2.dp)
                        )
                    }
                }
            }
        }
    }
}
