package com.example.ui.keyboard

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Casino
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Send
import androidx.compose.material.icons.outlined.Science
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.data.KeyboardThemeOption
import kotlin.random.Random

data class EmojiKitchenCombo(
    val emoji1: String,
    val emoji2: String,
    val title: String,
    val imageUrl: String
)

object EmojiKitchenCatalog {

    val baseMixEmojis = listOf(
        "😂", "🔥", "🐱", "🚀", "💀", "💐", "🥺", "💖",
        "🤡", "🎈", "🍕", "❤️", "😴", "☕", "👻", "🎃",
        "🤖", "🥑", "🍞", "🦄", "✨", "🐵", "🍌", "👽",
        "🛸", "🐶", "👑", "🥳", "🎂", "😭", "🌊", "🤯",
        "💥", "🥶", "🍦", "🤠", "🐎", "🧠", "💡", "🌈",
        "🌮", "🌶️", "🧸", "🎮", "🦁", "🍓", "🍫", "🏖️"
    )

    fun getKitchenUrl(e1: String, e2: String): String {
        fun toHex(e: String): String {
            return e.codePoints().toArray().joinToString("-") { String.format("%x", it) }
        }
        val h1 = toHex(e1)
        val h2 = toHex(e2)
        // Standard GStatic Emoji Kitchen CDN url pattern
        return "https://www.gstatic.com/android/keyboard/emojikitchen/20201001/u$h1/u${h1}_u${h2}.png"
    }

    val popularCombos = listOf(
        EmojiKitchenCombo("😂", "🔥", "Laughing Flame", getKitchenUrl("😂", "🔥")),
        EmojiKitchenCombo("🐱", "🚀", "Astro Space Cat", getKitchenUrl("🐱", "🚀")),
        EmojiKitchenCombo("💀", "💐", "Flower Day of Dead", getKitchenUrl("💀", "💐")),
        EmojiKitchenCombo("🥺", "💖", "Puppy Love Eyes", getKitchenUrl("🥺", "💖")),
        EmojiKitchenCombo("🤡", "🎈", "Pennywise Balloon", getKitchenUrl("🤡", "🎈")),
        EmojiKitchenCombo("🍕", "❤️", "Pizza My Heart", getKitchenUrl("🍕", "❤️")),
        EmojiKitchenCombo("😴", "☕", "Sleepy Zombie Coffee", getKitchenUrl("😴", "☕")),
        EmojiKitchenCombo("👻", "🎃", "Spooky Boo Pumpkin", getKitchenUrl("👻", "🎃")),
        EmojiKitchenCombo("🤖", "❤️", "Love Robot", getKitchenUrl("🤖", "❤️")),
        EmojiKitchenCombo("🥑", "🍞", "Avocado Toast", getKitchenUrl("🥑", "🍞")),
        EmojiKitchenCombo("🦄", "✨", "Cosmic Stardust Unicorn", getKitchenUrl("🦄", "✨")),
        EmojiKitchenCombo("🐵", "🍌", "Going Bananas", getKitchenUrl("🐵", "🍌")),
        EmojiKitchenCombo("👽", "🛸", "Alien Abduction Beam", getKitchenUrl("👽", "🛸")),
        EmojiKitchenCombo("🐶", "👑", "Royal King Puppy", getKitchenUrl("🐶", "👑")),
        EmojiKitchenCombo("🥳", "🎂", "Birthday Blast", getKitchenUrl("🥳", "🎂")),
        EmojiKitchenCombo("😭", "🌊", "Crying A River", getKitchenUrl("😭", "🌊")),
        EmojiKitchenCombo("🤯", "💥", "Mind Explosion", getKitchenUrl("🤯", "💥")),
        EmojiKitchenCombo("🥶", "🍦", "Brain Freeze Ice Cream", getKitchenUrl("🥶", "🍦")),
        EmojiKitchenCombo("🤠", "🐎", "Wild West Rodeo", getKitchenUrl("🤠", "🐎")),
        EmojiKitchenCombo("🧠", "💡", "Eureka Moment", getKitchenUrl("🧠", "💡")),
        EmojiKitchenCombo("🌈", "🦄", "Rainbow Horn", getKitchenUrl("🌈", "🦄")),
        EmojiKitchenCombo("🌮", "🌶️", "Ultra Spicy Taco", getKitchenUrl("🌮", "🌶️")),
        EmojiKitchenCombo("🧸", "💤", "Cozy Teddy Sleep", getKitchenUrl("🧸", "💤")),
        EmojiKitchenCombo("🎮", "🕹️", "Retro Gamer Boy", getKitchenUrl("🎮", "🕹️")),
        EmojiKitchenCombo("🦁", "🕶️", "Cool Boss Lion", getKitchenUrl("🦁", "🕶️")),
        EmojiKitchenCombo("🍓", "🍫", "Choco Strawberry", getKitchenUrl("🍓", "🍫")),
        EmojiKitchenCombo("🏖️", "🍹", "Tropical Vacation", getKitchenUrl("🏖️", "🍹"))
    )
}

@Composable
fun EmojiKitchenView(
    theme: KeyboardThemeOption,
    onEmojiSelected: (String) -> Unit,
    onMediaSelected: (String, String, String) -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    var slot1 by remember { mutableStateOf("😂") }
    var slot2 by remember { mutableStateOf("🔥") }
    var activeSlot by remember { mutableIntStateOf(2) } // 1 or 2
    var showPopularList by remember { mutableStateOf(false) }

    val accentColor = Color(theme.accentHex)
    val textColor = Color(theme.textHex)
    val surfaceColor = Color(theme.surfaceHex)

    val currentKitchenUrl = remember(slot1, slot2) {
        EmojiKitchenCatalog.getKitchenUrl(slot1, slot2)
    }

    val currentTitle = remember(slot1, slot2) {
        val match = EmojiKitchenCatalog.popularCombos.firstOrNull {
            (it.emoji1 == slot1 && it.emoji2 == slot2) || (it.emoji1 == slot2 && it.emoji2 == slot1)
        }
        match?.title ?: "$slot1 + $slot2 Mashup"
    }

    val onPickRandom = {
        val emojis = EmojiKitchenCatalog.baseMixEmojis
        val idx1 = Random.nextInt(emojis.size)
        var idx2 = Random.nextInt(emojis.size)
        if (idx2 == idx1) idx2 = (idx2 + 1) % emojis.size
        slot1 = emojis[idx1]
        slot2 = emojis[idx2]
    }

    val copyComboText = {
        val text = "$slot1$slot2"
        val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
        val clip = ClipData.newPlainText("Emoji Combo", text)
        clipboard.setPrimaryClip(clip)
        Toast.makeText(context, "Copied $text", Toast.LENGTH_SHORT).show()
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 6.dp, vertical = 2.dp)
    ) {
        // TOP INTERACTIVE MIXER CARD
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 2.dp),
            shape = RoundedCornerShape(14.dp),
            color = surfaceColor,
            border = BorderStroke(1.dp, accentColor.copy(alpha = 0.4f))
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 8.dp, vertical = 6.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                // Left: Slot 1 + Slot 2 interactive pills
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    // Slot 1
                    Box(
                        modifier = Modifier
                            .size(42.dp)
                            .clip(RoundedCornerShape(10.dp))
                            .background(if (activeSlot == 1) accentColor.copy(alpha = 0.25f) else textColor.copy(alpha = 0.06f))
                            .border(
                                1.5.dp,
                                if (activeSlot == 1) accentColor else textColor.copy(alpha = 0.15f),
                                RoundedCornerShape(10.dp)
                            )
                            .clickable { activeSlot = 1 },
                        contentAlignment = Alignment.Center
                    ) {
                        Text(text = slot1, fontSize = 22.sp)
                    }

                    Text(
                        text = "+",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = accentColor
                    )

                    // Slot 2
                    Box(
                        modifier = Modifier
                            .size(42.dp)
                            .clip(RoundedCornerShape(10.dp))
                            .background(if (activeSlot == 2) accentColor.copy(alpha = 0.25f) else textColor.copy(alpha = 0.06f))
                            .border(
                                1.5.dp,
                                if (activeSlot == 2) accentColor else textColor.copy(alpha = 0.15f),
                                RoundedCornerShape(10.dp)
                            )
                            .clickable { activeSlot = 2 },
                        contentAlignment = Alignment.Center
                    ) {
                        Text(text = slot2, fontSize = 22.sp)
                    }

                    Text(
                        text = "=",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = accentColor
                    )
                }

                // Center: Big Mashup Sticker Preview
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = textColor.copy(alpha = 0.04f),
                    border = BorderStroke(1.dp, accentColor.copy(alpha = 0.3f)),
                    modifier = Modifier
                        .size(52.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .clickable {
                            onMediaSelected(currentKitchenUrl, "image/png", currentTitle)
                        }
                ) {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        AsyncImage(
                            model = currentKitchenUrl,
                            contentDescription = currentTitle,
                            contentScale = ContentScale.Fit,
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(4.dp)
                        )
                    }
                }

                // Right: Action Buttons (Send Sticker, Copy, Random)
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    // Random Dice Button
                    IconButton(
                        onClick = onPickRandom,
                        modifier = Modifier
                            .size(36.dp)
                            .clip(CircleShape)
                            .background(textColor.copy(alpha = 0.08f))
                    ) {
                        Icon(
                            imageVector = Icons.Default.Casino,
                            contentDescription = "Random Mashup",
                            tint = accentColor,
                            modifier = Modifier.size(18.dp)
                        )
                    }

                    // 1-Tap Send Big Sticker Button
                    Surface(
                        onClick = {
                            onMediaSelected(currentKitchenUrl, "image/png", currentTitle)
                        },
                        shape = RoundedCornerShape(10.dp),
                        color = accentColor,
                        modifier = Modifier.height(36.dp)
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 8.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Send,
                                contentDescription = null,
                                tint = Color.White,
                                modifier = Modifier.size(13.dp)
                            )
                            Text(
                                text = "Send",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                        }
                    }
                }
            }
        }

        // Quick Sub-Filter / Mode Switch: "Mix Emojis" vs "🔥 Popular Combos"
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 2.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                FilterChip(
                    selected = !showPopularList,
                    onClick = { showPopularList = false },
                    label = { Text("🧪 Custom Mixer", fontSize = 11.5.sp) },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = accentColor,
                        selectedLabelColor = Color.White,
                        containerColor = surfaceColor,
                        labelColor = textColor
                    )
                )

                FilterChip(
                    selected = showPopularList,
                    onClick = { showPopularList = true },
                    label = { Text("🔥 Top Combos (${EmojiKitchenCatalog.popularCombos.size})", fontSize = 11.5.sp) },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = accentColor,
                        selectedLabelColor = Color.White,
                        containerColor = surfaceColor,
                        labelColor = textColor
                    )
                )
            }

            // Quick Insert Text button
            TextButton(
                onClick = { onEmojiSelected("$slot1 $slot2") },
                contentPadding = PaddingValues(horizontal = 6.dp, vertical = 2.dp)
            ) {
                Text(
                    text = "Insert \"$slot1$slot2\"",
                    fontSize = 11.sp,
                    color = accentColor,
                    fontWeight = FontWeight.SemiBold
                )
            }
        }

        // CONTENT: Either Base Emoji Grid or Popular Combos Grid
        if (showPopularList) {
            LazyVerticalGrid(
                columns = GridCells.Fixed(3),
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                verticalArrangement = Arrangement.spacedBy(6.dp),
                contentPadding = PaddingValues(bottom = 6.dp)
            ) {
                items(EmojiKitchenCatalog.popularCombos, key = { it.title }) { combo ->
                    Surface(
                        shape = RoundedCornerShape(10.dp),
                        color = surfaceColor,
                        border = BorderStroke(0.7.dp, textColor.copy(alpha = 0.12f)),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(86.dp)
                            .clip(RoundedCornerShape(10.dp))
                            .clickable {
                                slot1 = combo.emoji1
                                slot2 = combo.emoji2
                                onMediaSelected(combo.imageUrl, "image/png", combo.title)
                            }
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(4.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center
                        ) {
                            AsyncImage(
                                model = combo.imageUrl,
                                contentDescription = combo.title,
                                contentScale = ContentScale.Fit,
                                modifier = Modifier
                                    .size(46.dp)
                                    .padding(2.dp)
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = "${combo.emoji1}+${combo.emoji2} ${combo.title}",
                                fontSize = 9.5.sp,
                                fontWeight = FontWeight.Medium,
                                color = textColor.copy(alpha = 0.85f),
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                                textAlign = TextAlign.Center
                            )
                        }
                    }
                }
            }
        } else {
            // Base Emoji Palette for mixing
            LazyVerticalGrid(
                columns = GridCells.Fixed(8),
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth(),
                contentPadding = PaddingValues(bottom = 6.dp)
            ) {
                items(EmojiKitchenCatalog.baseMixEmojis, key = { it }) { emoji ->
                    val isSlot1 = emoji == slot1
                    val isSlot2 = emoji == slot2
                    Box(
                        modifier = Modifier
                            .aspectRatio(1f)
                            .clip(CircleShape)
                            .background(
                                when {
                                    isSlot1 && activeSlot == 1 -> accentColor.copy(alpha = 0.25f)
                                    isSlot2 && activeSlot == 2 -> accentColor.copy(alpha = 0.25f)
                                    else -> Color.Transparent
                                }
                            )
                            .clickable {
                                if (activeSlot == 1) {
                                    slot1 = emoji
                                    activeSlot = 2 // Auto-advance to slot 2 for snappy workflow!
                                } else {
                                    slot2 = emoji
                                }
                            },
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = emoji,
                            fontSize = 22.sp,
                            textAlign = TextAlign.Center
                        )
                    }
                }
            }
        }
    }
}
