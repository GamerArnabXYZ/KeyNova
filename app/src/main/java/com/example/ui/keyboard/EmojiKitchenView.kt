package com.example.ui.keyboard

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
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
import androidx.compose.foundation.lazy.grid.itemsIndexed
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Casino
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.ContentPaste
import androidx.compose.material.icons.filled.Send
import androidx.compose.material.icons.filled.SwapHoriz
import androidx.compose.material.icons.outlined.Science
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import coil.compose.SubcomposeAsyncImage
import com.example.data.KeyboardThemeOption
import kotlin.random.Random

data class EmojiKitchenCombo(
    val emoji1: String,
    val emoji2: String,
    val title: String,
    val imageUrl: String
)

object EmojiKitchenCatalog {

    val smileyEmojis = listOf(
        "😀", "😃", "😄", "😁", "😆", "😅", "😂", "🤣", "🥲", "🥹", "😊", "😇",
        "🙂", "🙃", "😉", "😌", "😍", "🥰", "😘", "😗", "😋", "😛", "😜", "🤪",
        "😝", "🤑", "🤗", "🤭", "🫢", "🫣", "🤫", "🤔", "🫡", "🤐", "🤨", "😐",
        "😑", "😶", "🫥", "😏", "😒", "🙄", "😬", "🤥", "😔", "😪", "🤤",
        "😴", "😷", "🤒", "🤕", "🤢", "🤮", "🤧", "🥵", "🥶", "🥴", "😵", "🤯",
        "🤠", "🥳", "🥸", "😎", "🤓", "🧐", "😕", "😟", "🙁", "☹️", "😮", "😯",
        "😲", "😳", "🥺", "😦", "😧", "😨", "😰", "😥", "😢", "😭", "😱", "😖",
        "😣", "😞", "😓", "😩", "😫", "🥱", "😤", "😡", "😠", "🤬", "😈", "👿",
        "💀", "☠️", "💩", "🤡", "👹", "👺", "👻", "👽", "👾", "🤖"
    )

    val animalEmojis = listOf(
        "🐱", "🐶", "🐭", "🐹", "🐰", "🦊", "🐻", "🐼", "🐨", "🐯", "🦁", "🐮",
        "🐷", "🐸", "🐵", "🐔", "🐧", "🐦", "🐤", "🦆", "🦅", "🦉", "🦇", "🐺",
        "🐗", "🐴", "🦄", "🐝", "🐛", "🦋", "🐌", "🐞", "🐜", "🪲", "🕷️", "🦂",
        "🐢", "🐍", "🦎", "🦖", "🐙", "🦑", "🦐", "🦞", "🦀", "🐡", "🐠", "🐟",
        "🐬", "🐳", "🦈", "🐊", "🐅", "🐆", "🐘", "🦣", "🦏", "🦛", "🐪", "🐫",
        "🦒", "🦘", "🦬", "🐃", "🐂", "🐄", "🐎", "🐖", "🐏", "🐑", "🦙", "🐐",
        "🦌", "🐕", "🐩", "🦮", "🐈", "🐓", "🦃", "🦚", "🦜", "🦢", "🦩", "🕊️"
    )

    val foodEmojis = listOf(
        "🍕", "🍔", "🍟", "🌭", "🍿", "🧂", "🥓", "🥚", "🍳", "🧇", "🥞", "🧈",
        "🍞", "🥐", "🥨", "🥯", "🥖", "🧀", "🥗", "🥪", "🌮", "🌯", "🫔", "🥙",
        "🧆", "🍜", "🍝", "🍣", "🍱", "🍛", "🍙", "🍚", "🍘", "🥟", "🍢", "🍡",
        "🍧", "🍨", "🍦", "🥧", "🧁", "🍰", "🎂", "🍮", "🍭", "🍬", "🍫", "🍩",
        "🍪", "🍯", "🍼", "🥛", "☕", "🫖", "🍵", "🧃", "🥤", "🧋", "🍶", "🍺",
        "🍻", "🥂", "🍷", "🥃", "🍸", "🍹", "🧉", "🍾", "🧊", "🥑", "🍓", "🍉"
    )

    val travelEmojis = listOf(
        "🚀", "🛸", "✈️", "🚁", "⛵", "🚤", "🚗", "🏎️", "🏍️", "🚲", "🚂", "🚆",
        "🚇", "🚌", "🚎", "🚑", "🚒", "🚓", "🚕", "🚜", "🚚", "🚢", "🛳️", "🛰️",
        "🪐", "🌍", "🌎", "🌏", "🌕", "🌖", "🌗", "🌘", "🌑", "🌒", "🌓", "🌔",
        "🌙", "☀️", "⭐", "🌟", "🌠", "☁️", "⛅", "⛈️", "🌦️", "🌧️", "🌩️", "🌈",
        "⚡", "❄️", "☃️", "🔥", "💧", "🌊", "🏖️", "🏔️", "🏕️", "🌋", "🏰", "🗼"
    )

    val heartAndMagicEmojis = listOf(
        "❤️", "💖", "💘", "💝", "💕", "💞", "💓", "💗", "❣️", "💔", "🧡", "💛",
        "💚", "💙", "💜", "🖤", "🤍", "🤎", "✨", "⭐", "🌟", "💫", "💥", "💯",
        "🔥", "👑", "💎", "💍", "💐", "🌹", "🥀", "🌺", "🌸", "🌼", "🌻", "🍀",
        "🎈", "🎉", "🎊", "🎀", "🎁", "🪄", "🔮", "🧿", "💌", "💤", "👁️", "🧠"
    )

    val funAndGameEmojis = listOf(
        "🎮", "🕹️", "🎲", "🧩", "♟️", "🎯", "🎳", "🎪", "🎭", "🎨", "🎬", "🎤",
        "🎧", "🎼", "🎹", "🥁", "🎷", "🎺", "🎸", "🪕", "🎻", "🏆", "🥇", "🥈",
        "🥉", "🏅", "🎖️", "⚽", "🏀", "🏈", "⚾", "🥎", "🎾", "🏐", "🥏",
        "🎱", "🏓", "🏸", "🏒", "🏑", "🥍", "🏏", "🪃", "🥊", "🥋", "⛸️", "🛹"
    )

    fun getKitchenUrl(e1: String, e2: String): String {
        fun toHex(e: String): String {
            return e.codePoints().toArray().joinToString("-") { String.format("%x", it) }
        }
        val h1 = toHex(e1)
        val h2 = toHex(e2)
        return "https://www.gstatic.com/android/keyboard/emojikitchen/20201001/u$h1/u${h1}_u${h2}.png"
    }

    // Curated high-res working combinations
    val popularCombos = listOf(
        EmojiKitchenCombo("😂", "🔥", "Laughing Flame", getKitchenUrl("😂", "🔥")),
        EmojiKitchenCombo("😂", "😂", "Mega Laugh Out Loud", getKitchenUrl("😂", "😂")),
        EmojiKitchenCombo("❤️", "🔥", "Heart on Fire", getKitchenUrl("❤️", "🔥")),
        EmojiKitchenCombo("🥺", "💖", "Puppy Love Eyes", getKitchenUrl("🥺", "💖")),
        EmojiKitchenCombo("💀", "💐", "Day of Dead Skull", getKitchenUrl("💀", "💐")),
        EmojiKitchenCombo("🤡", "🎈", "Pennywise Balloon", getKitchenUrl("🤡", "🎈")),
        EmojiKitchenCombo("🍕", "❤️", "Pizza My Heart", getKitchenUrl("🍕", "❤️")),
        EmojiKitchenCombo("😴", "☕", "Zombie Sleepy Coffee", getKitchenUrl("😴", "☕")),
        EmojiKitchenCombo("👻", "🎃", "Spooky Boo Pumpkin", getKitchenUrl("👻", "🎃")),
        EmojiKitchenCombo("🤖", "❤️", "Love Robot", getKitchenUrl("🤖", "❤️")),
        EmojiKitchenCombo("🥑", "🍞", "Avocado Toast", getKitchenUrl("🥑", "🍞")),
        EmojiKitchenCombo("🦄", "✨", "Stardust Unicorn", getKitchenUrl("🦄", "✨")),
        EmojiKitchenCombo("🐵", "🍌", "Going Bananas", getKitchenUrl("🐵", "🍌")),
        EmojiKitchenCombo("👽", "🛸", "Alien Abduction Beam", getKitchenUrl("👽", "🛸")),
        EmojiKitchenCombo("🐶", "👑", "King Puppy", getKitchenUrl("🐶", "👑")),
        EmojiKitchenCombo("🥳", "🎂", "Birthday Blast", getKitchenUrl("🥳", "🎂")),
        EmojiKitchenCombo("😭", "🌊", "Crying A River", getKitchenUrl("😭", "🌊")),
        EmojiKitchenCombo("🤯", "💥", "Mind Explosion", getKitchenUrl("🤯", "💥")),
        EmojiKitchenCombo("🥶", "🍦", "Brain Freeze Ice Cream", getKitchenUrl("🥶", "🍦")),
        EmojiKitchenCombo("🤠", "🐎", "Wild West Rodeo", getKitchenUrl("🤠", "🐎")),
        EmojiKitchenCombo("🧠", "💡", "Eureka Moment", getKitchenUrl("🧠", "💡")),
        EmojiKitchenCombo("🌈", "🦄", "Rainbow Horn", getKitchenUrl("🌈", "🦄")),
        EmojiKitchenCombo("🌮", "🌶️", "Spicy Taco", getKitchenUrl("🌮", "🌶️")),
        EmojiKitchenCombo("🧸", "💤", "Cozy Sleeping Bear", getKitchenUrl("🧸", "💤")),
        EmojiKitchenCombo("🎮", "🕹️", "Retro Arcade Gamer", getKitchenUrl("🎮", "🕹️")),
        EmojiKitchenCombo("🦁", "🕶️", "Cool Boss Lion", getKitchenUrl("🦁", "🕶️")),
        EmojiKitchenCombo("🍓", "🍫", "Choco Strawberry", getKitchenUrl("🍓", "🍫")),
        EmojiKitchenCombo("🏖️", "🍹", "Tropical Beach Vacation", getKitchenUrl("🏖️", "🍹"))
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
    var activeSlot by remember { mutableIntStateOf(1) } // 1 or 2
    var selectedCategoryIndex by remember { mutableIntStateOf(0) }
    var showPopularList by remember { mutableStateOf(false) }

    val accentColor = Color(theme.accentHex)
    val textColor = Color(theme.textHex)
    val surfaceColor = Color(theme.surfaceHex)
    val backgroundColor = Color(theme.backgroundHex)

    val currentKitchenUrl = remember(slot1, slot2) {
        EmojiKitchenCatalog.getKitchenUrl(slot1, slot2)
    }

    val currentTitle = remember(slot1, slot2) {
        val match = EmojiKitchenCatalog.popularCombos.firstOrNull {
            (it.emoji1 == slot1 && it.emoji2 == slot2) || (it.emoji1 == slot2 && it.emoji2 == slot1)
        }
        match?.title ?: "$slot1 + $slot2 Mashup"
    }

    val categoryTabs = listOf(
        "😀 Smileys" to EmojiCatalog.smileys,
        "🧑 People" to EmojiCatalog.people,
        "🐶 Animals" to EmojiCatalog.animals,
        "🍕 Food" to EmojiCatalog.food,
        "🚀 Travel" to EmojiCatalog.travel,
        "🎉 Fun" to EmojiCatalog.activities,
        "💡 Objects" to EmojiCatalog.objects,
        "💖 Hearts" to EmojiKitchenCatalog.heartAndMagicEmojis,
        "🔣 Symbols" to EmojiCatalog.symbols,
        "🚩 Flags" to EmojiCatalog.flags
    )

    val onPickRandom = {
        val all = EmojiCatalog.smileys + EmojiCatalog.animals + EmojiCatalog.food
        val idx1 = Random.nextInt(all.size)
        var idx2 = Random.nextInt(all.size)
        if (idx2 == idx1) idx2 = (idx2 + 1) % all.size
        slot1 = all[idx1]
        slot2 = all[idx2]
    }

    val onSwapSlots = {
        val tmp = slot1
        slot1 = slot2
        slot2 = tmp
    }

    val pasteEmojiToSlot: (Int) -> Unit = { targetSlot ->
        val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
        val clip = clipboard.primaryClip
        if (clip != null && clip.itemCount > 0) {
            val text = clip.getItemAt(0).coerceToText(context).toString().trim()
            if (text.isNotBlank()) {
                // Grab the first grapheme/codePoint or emoji
                val emojiChar = text.take(4).trim()
                if (targetSlot == 1) {
                    slot1 = emojiChar
                    activeSlot = 2
                } else {
                    slot2 = emojiChar
                }
                Toast.makeText(context, "Set Slot $targetSlot: $emojiChar", Toast.LENGTH_SHORT).show()
            }
        }
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
            .background(backgroundColor)
            .padding(horizontal = 6.dp, vertical = 2.dp)
    ) {
        // TOP DUAL-SLOT MIXER HERO CARD
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 2.dp),
            shape = RoundedCornerShape(14.dp),
            color = surfaceColor,
            border = BorderStroke(1.2.dp, accentColor.copy(alpha = 0.5f)),
            shadowElevation = 1.dp
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 8.dp, vertical = 6.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                // Left: Slot 1 + Swap + Slot 2 interactive slots
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    // Slot 1
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Box(
                            modifier = Modifier
                                .size(44.dp)
                                .clip(RoundedCornerShape(10.dp))
                                .background(if (activeSlot == 1) accentColor.copy(alpha = 0.25f) else textColor.copy(alpha = 0.06f))
                                .border(
                                    if (activeSlot == 1) 2.dp else 1.dp,
                                    if (activeSlot == 1) accentColor else textColor.copy(alpha = 0.18f),
                                    RoundedCornerShape(10.dp)
                                )
                                .clickable { activeSlot = 1 },
                            contentAlignment = Alignment.Center
                        ) {
                            Text(text = slot1, fontSize = 23.sp)
                        }
                        Text(
                            text = if (activeSlot == 1) "● Slot 1" else "Slot 1",
                            fontSize = 9.sp,
                            fontWeight = if (activeSlot == 1) FontWeight.Bold else FontWeight.Normal,
                            color = if (activeSlot == 1) accentColor else textColor.copy(alpha = 0.5f)
                        )
                    }

                    // Swap / Plus button
                    IconButton(
                        onClick = onSwapSlots,
                        modifier = Modifier.size(26.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.SwapHoriz,
                            contentDescription = "Swap slots",
                            tint = accentColor,
                            modifier = Modifier.size(16.dp)
                        )
                    }

                    // Slot 2
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Box(
                            modifier = Modifier
                                .size(44.dp)
                                .clip(RoundedCornerShape(10.dp))
                                .background(if (activeSlot == 2) accentColor.copy(alpha = 0.25f) else textColor.copy(alpha = 0.06f))
                                .border(
                                    if (activeSlot == 2) 2.dp else 1.dp,
                                    if (activeSlot == 2) accentColor else textColor.copy(alpha = 0.18f),
                                    RoundedCornerShape(10.dp)
                                )
                                .clickable { activeSlot = 2 },
                            contentAlignment = Alignment.Center
                        ) {
                            Text(text = slot2, fontSize = 23.sp)
                        }
                        Text(
                            text = if (activeSlot == 2) "● Slot 2" else "Slot 2",
                            fontSize = 9.sp,
                            fontWeight = if (activeSlot == 2) FontWeight.Bold else FontWeight.Normal,
                            color = if (activeSlot == 2) accentColor else textColor.copy(alpha = 0.5f)
                        )
                    }

                    Text(
                        text = "=",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = accentColor.copy(alpha = 0.8f)
                    )
                }

                // Center: Big Mashup Sticker / Graphic Preview Box
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = textColor.copy(alpha = 0.04f),
                    border = BorderStroke(1.2.dp, accentColor.copy(alpha = 0.6f)),
                    modifier = Modifier
                        .size(56.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .clickable {
                            onMediaSelected(currentKitchenUrl, "image/png", "$slot1+$slot2 $currentTitle")
                        }
                ) {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        // SubcomposeAsyncImage with reliable dynamic emoji composite fallback
                        SubcomposeAsyncImage(
                            model = currentKitchenUrl,
                            contentDescription = currentTitle,
                            contentScale = ContentScale.Fit,
                            loading = {
                                // Dynamic Composite Emoji Badge while loading or fallback
                                Box(
                                    modifier = Modifier.fillMaxSize(),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.Center
                                    ) {
                                        Text(text = slot1, fontSize = 20.sp)
                                        Text(text = slot2, fontSize = 20.sp)
                                    }
                                }
                            },
                            error = {
                                // GORGEOUS DYNAMIC COMPOSITE STICKER (Guaranteed to NEVER fail for ANY custom emoji!)
                                Box(
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .background(
                                            Brush.radialGradient(
                                                colors = listOf(
                                                    accentColor.copy(alpha = 0.25f),
                                                    Color.Transparent
                                                )
                                            )
                                        ),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Box(contentAlignment = Alignment.Center) {
                                        Text(
                                            text = slot1,
                                            fontSize = 26.sp,
                                            modifier = Modifier.offset(x = (-6).dp, y = (-2).dp)
                                        )
                                        Text(
                                            text = slot2,
                                            fontSize = 24.sp,
                                            modifier = Modifier.offset(x = 6.dp, y = 4.dp)
                                        )
                                    }
                                }
                            },
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(3.dp)
                        )
                    }
                }

                // Right: Action Buttons (Send Sticker / Text Emojis / Random)
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    // Random Dice
                    IconButton(
                        onClick = onPickRandom,
                        modifier = Modifier
                            .size(34.dp)
                            .clip(CircleShape)
                            .background(textColor.copy(alpha = 0.07f))
                    ) {
                        Icon(
                            imageVector = Icons.Default.Casino,
                            contentDescription = "Random Mix",
                            tint = accentColor,
                            modifier = Modifier.size(17.dp)
                        )
                    }

                    // Send Sticker Button (Dispatches actual sticker image!)
                    Surface(
                        onClick = {
                            onMediaSelected(currentKitchenUrl, "image/png", "$slot1+$slot2 $currentTitle")
                        },
                        shape = RoundedCornerShape(10.dp),
                        color = accentColor,
                        modifier = Modifier.height(34.dp)
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 8.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(3.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Send,
                                contentDescription = null,
                                tint = Color.White,
                                modifier = Modifier.size(12.dp)
                            )
                            Text(
                                text = "Send",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                        }
                    }

                    // Optional Plain Text Emojis Button
                    Surface(
                        onClick = {
                            onEmojiSelected("$slot1$slot2")
                        },
                        shape = RoundedCornerShape(8.dp),
                        color = textColor.copy(alpha = 0.08f),
                        border = BorderStroke(0.8.dp, textColor.copy(alpha = 0.15f)),
                        modifier = Modifier.height(34.dp)
                    ) {
                        Box(
                            modifier = Modifier.padding(horizontal = 6.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "Text",
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Medium,
                                color = textColor.copy(alpha = 0.8f)
                            )
                        }
                    }
                }
            }
        }

        // CUSTOM EMOJI QUICK BAR: Paste / Mode selector
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 2.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Mode Chips: "Custom Mixer" vs "🔥 Top 28 Combos"
            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                FilterChip(
                    selected = !showPopularList,
                    onClick = { showPopularList = false },
                    label = {
                        Text(
                            text = "🎨 Pick Custom Emoji (${if (activeSlot == 1) "Slot 1" else "Slot 2"})",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold
                        )
                    },
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
                    label = {
                        Text(
                            text = "🔥 Top Combos",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                    },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = accentColor,
                        selectedLabelColor = Color.White,
                        containerColor = surfaceColor,
                        labelColor = textColor
                    )
                )
            }

            // Quick Paste Button to paste clipboard emoji into active slot
            TextButton(
                onClick = { pasteEmojiToSlot(activeSlot) },
                contentPadding = PaddingValues(horizontal = 6.dp, vertical = 2.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.ContentPaste,
                    contentDescription = null,
                    tint = accentColor,
                    modifier = Modifier.size(13.dp)
                )
                Spacer(modifier = Modifier.width(3.dp))
                Text(
                    text = "Paste Emoji",
                    fontSize = 11.sp,
                    color = accentColor,
                    fontWeight = FontWeight.SemiBold
                )
            }
        }

        // FULL CATEGORY EMOJI PALETTE OR POPULAR LIST
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
                            SubcomposeAsyncImage(
                                model = combo.imageUrl,
                                contentDescription = combo.title,
                                contentScale = ContentScale.Fit,
                                error = {
                                    Text("${combo.emoji1}${combo.emoji2}", fontSize = 24.sp)
                                },
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
            // Category Tabs Row for Custom Emojis
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(30.dp)
                    .horizontalScroll(rememberScrollState())
                    .padding(bottom = 2.dp),
                horizontalArrangement = Arrangement.spacedBy(4.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                categoryTabs.forEachIndexed { idx, pair ->
                    val isSelected = idx == selectedCategoryIndex
                    Surface(
                        onClick = { selectedCategoryIndex = idx },
                        shape = RoundedCornerShape(8.dp),
                        color = if (isSelected) accentColor.copy(alpha = 0.2f) else Color.Transparent,
                        border = if (isSelected) BorderStroke(1.dp, accentColor) else null,
                        modifier = Modifier.height(26.dp)
                    ) {
                        Box(
                            modifier = Modifier.padding(horizontal = 8.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = pair.first,
                                fontSize = 11.sp,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                color = if (isSelected) accentColor else textColor.copy(alpha = 0.7f)
                            )
                        }
                    }
                }
            }

            // Current Category Emojis Grid (Supports ANY custom selection!)
            val currentCategoryEmojis = categoryTabs[selectedCategoryIndex].second
            LazyVerticalGrid(
                columns = GridCells.Fixed(8),
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth(),
                contentPadding = PaddingValues(bottom = 6.dp)
            ) {
                itemsIndexed(currentCategoryEmojis, key = { index, emoji -> "${emoji}_${index}" }) { _, emoji ->
                    val isSlot1 = emoji == slot1
                    val isSlot2 = emoji == slot2
                    Box(
                        modifier = Modifier
                            .aspectRatio(1f)
                            .clip(CircleShape)
                            .background(
                                when {
                                    isSlot1 && activeSlot == 1 -> accentColor.copy(alpha = 0.35f)
                                    isSlot2 && activeSlot == 2 -> accentColor.copy(alpha = 0.35f)
                                    isSlot1 || isSlot2 -> textColor.copy(alpha = 0.08f)
                                    else -> Color.Transparent
                                }
                            )
                            .clickable {
                                if (activeSlot == 1) {
                                    slot1 = emoji
                                    activeSlot = 2 // Seamless auto-advance to Slot 2!
                                } else {
                                    slot2 = emoji
                                    activeSlot = 1 // Cycle back to Slot 1 for the next mix!
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
