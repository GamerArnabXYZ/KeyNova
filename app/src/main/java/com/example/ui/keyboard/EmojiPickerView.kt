package com.example.ui.keyboard

import android.content.Context
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Backspace
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.KeyboardThemeOption

data class EmojiCategory(
    val title: String,
    val icon: String,
    val emojis: List<String>
)

object EmojiCatalog {
    val categories = listOf(
        EmojiCategory(
            title = "Smileys",
            icon = "😀",
            emojis = listOf(
                "😀", "😃", "😄", "😁", "😆", "😅", "😂", "🤣", "🥲", "🥹",
                "😊", "😇", "🙂", "🙃", "😉", "😌", "😍", "🥰", "😘", "😗",
                "😙", "😚", "😋", "😛", "😝", "😜", "🤪", "🤨", "🧐", "🤓",
                "😎", "🥸", "🤩", "🥳", "😏", "😒", "😞", "😔", "😟", "😕",
                "🙁", "☹️", "😣", "😖", "😫", "😩", "🥺", "😢", "😭", "😮‍💨",
                "😤", "😠", "😡", "🤬", "🤯", "😳", "🥵", "🥶", "😱", "😨"
            )
        ),
        EmojiCategory(
            title = "Gestures",
            icon = "👍",
            emojis = listOf(
                "👋", "🤚", "🖐️", "✋", "🖖", "🫱", "🫲", "👌", "🤌", "🤏",
                "✌️", "🤞", "🫰", "🤟", "🤘", "🤙", "👈", "👉", "👆", "🖕",
                "👇", "☝️", "👍", "👎", "✊", "👊", "🤛", "🤜", "👏", "🙌",
                "🫶", "👐", "🤲", "🤝", "🙏", "✍️", "💅", "🤳", "💪", "🦾"
            )
        ),
        EmojiCategory(
            title = "Hearts",
            icon = "❤️",
            emojis = listOf(
                "❤️", "🧡", "💛", "💚", "💙", "💜", "🖤", "🤍", "🤎", "💔",
                "❤️‍🔥", "❤️‍🩹", "❣️", "💕", "💞", "💓", "💗", "💖", "💘", "💝",
                "💟", "💌", "💋", "💯", "🔥", "✨", "💫", "⭐", "🌟", "⚡"
            )
        ),
        EmojiCategory(
            title = "Animals",
            icon = "🐶",
            emojis = listOf(
                "🐶", "🐱", "🐭", "🐹", "🐰", "🦊", "🐻", "🐼", "🐨", "🐯",
                "🦁", "🐮", "🐷", "🐸", "🐵", "🐔", "🐧", "🐦", "🦆", "🦅",
                "🦉", "🦇", "🐺", "🐗", "🐴", "🦄", "🐝", "🦋", "🐌", "🐞",
                "🌴", "🌵", "🌾", "🌿", "🍀", "🍁", "🍂", "🍃", "🌸", "🌺"
            )
        ),
        EmojiCategory(
            title = "Food",
            icon = "🍕",
            emojis = listOf(
                "🍏", "🍎", "🍐", "🍊", "🍋", "🍌", "🍉", "🍇", "🍓", "🫐",
                "🍔", "🍟", "🍕", "🌭", "🥪", "🌮", "🌯", "🍜", "🍣", "🍦",
                "🍩", "🍪", "🎂", "🍫", "🍿", "☕", "🧋", "🍵", "🥤", "🍺"
            )
        ),
        EmojiCategory(
            title = "Gaming",
            icon = "🎮",
            emojis = listOf(
                "🎮", "🕹️", "👾", "🎯", "🎲", "🧩", "🎳", "🏆", "🥇", "🥈",
                "🥉", "👑", "⚔️", "🛡️", "💣", "🪄", "🔮", "🛸", "🤖", "🚀"
            )
        ),
        EmojiCategory(
            title = "Dev",
            icon = "💻",
            emojis = listOf(
                "💻", "🖥️", "⌨️", "🖱️", "📱", "💾", "💿", "🔌", "🔋", "💡",
                "🛠️", "⚙️", "🔧", "🪛", "📡", "🛰️", "🔬", "🔭", "📊", "📈"
            )
        ),
        EmojiCategory(
            title = "Travel",
            icon = "✈️",
            emojis = listOf(
                "🚗", "🚕", "🚙", "🚌", "🏎️", "🚓", "🚑", "🚒", "🚲", "🏍️",
                "✈️", "🛫", "🛬", "🚀", "🛸", "🚁", "⛵", "🚢", "🏠", "🏙️"
            )
        ),
        EmojiCategory(
            title = "Objects",
            icon = "💡",
            emojis = listOf(
                "💡", "📱", "💻", "⏰", "⏱️", "💎", "💰", "💳", "🎁", "🎈",
                "📦", "📌", "✂️", "🔒", "🔑", "🔍", "🔔", "💬", "💭", "🎉"
            )
        )
    )

    // Tag based search index
    val emojiTags = mapOf(
        "laugh" to listOf("😂", "🤣", "😆", "😀", "😄", "😅"),
        "smile" to listOf("😀", "😃", "😄", "😁", "😊", "🙂", "😇"),
        "happy" to listOf("😀", "😊", "🥳", "🤩", "😁", "😇"),
        "love" to listOf("❤️", "🥰", "😍", "😘", "💖", "💕", "💓", "💗"),
        "heart" to listOf("❤️", "🧡", "💛", "💚", "💙", "💜", "🖤", "🤍", "🤎", "💔"),
        "cry" to listOf("😢", "😭", "🥺", "😿", "💧", "🥲"),
        "sad" to listOf("😞", "😔", "😟", "🙁", "☹️", "😢", "😭"),
        "fire" to listOf("🔥", "❤️‍🔥", "💥", "⚡", "✨"),
        "cool" to listOf("😎", "🤩", "🤙", "✌️", "🔥"),
        "thumbs" to listOf("👍", "👎"),
        "ok" to listOf("👌", "👍", "🙆"),
        "dog" to listOf("🐶", "🐕", "🦮"),
        "cat" to listOf("🐱", "🐈", "🦁"),
        "food" to listOf("🍕", "🍔", "🍟", "🌭", "🥪", "🍜", "🍣"),
        "coffee" to listOf("☕", "🧋", "🍵"),
        "game" to listOf("🎮", "🕹️", "👾", "🎲", "🎯"),
        "code" to listOf("💻", "🖥️", "⌨️", "📱", "💡"),
        "star" to listOf("⭐", "🌟", "✨", "💫"),
        "party" to listOf("🥳", "🎉", "🎈", "🍾", "🥂"),
        "money" to listOf("💰", "💳", "💎", "🤑"),
        "car" to listOf("🚗", "🏎️", "🚕", "🚙"),
        "plane" to listOf("✈️", "🛫", "🛬", "🚀")
    )
}

@Composable
fun EmojiPickerView(
    theme: KeyboardThemeOption,
    onEmojiSelected: (String) -> Unit,
    onBackspace: () -> Unit,
    onClose: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val prefs = remember { context.getSharedPreferences("keynova_recent_emojis", Context.MODE_PRIVATE) }

    var recentEmojis by remember {
        val raw = prefs.getString("recent_list", null)
        val list = if (raw != null) raw.split(",").filter { it.isNotBlank() } else listOf("😀", "😂", "❤️", "👍", "🔥", "🎉", "😎", "🙏")
        mutableStateOf(list)
    }

    fun saveRecentEmoji(emoji: String) {
        val updated = (listOf(emoji) + recentEmojis.filter { it != emoji }).take(30)
        recentEmojis = updated
        prefs.edit().putString("recent_list", updated.joinToString(",")).apply()
    }

    val bgColor = Color(theme.backgroundHex)
    val textColor = Color(theme.textHex)
    val accentColor = Color(theme.accentHex)
    val surfaceColor = Color(theme.surfaceHex)

    var searchQuery by remember { mutableStateOf("") }
    var selectedCategoryIndex by remember { mutableIntStateOf(0) }

    // Categories list with Recent at the beginning
    val categories = remember(recentEmojis) {
        listOf(
            EmojiCategory(title = "Recent", icon = "🕒", emojis = recentEmojis)
        ) + EmojiCatalog.categories
    }

    val filteredEmojis = remember(searchQuery, selectedCategoryIndex, recentEmojis) {
        if (searchQuery.isNotBlank()) {
            val q = searchQuery.trim().lowercase()
            val matches = mutableListOf<String>()
            EmojiCatalog.emojiTags.forEach { (tag, list) ->
                if (tag.contains(q) || q.contains(tag)) {
                    matches.addAll(list)
                }
            }
            EmojiCatalog.categories.flatMap { it.emojis }.filter { it.contains(q) }.forEach { matches.add(it) }
            matches.distinct().ifEmpty {
                EmojiCatalog.categories.flatMap { it.emojis }.take(40)
            }
        } else {
            categories.getOrNull(selectedCategoryIndex)?.emojis ?: emptyList()
        }
    }

    Column(
        modifier = modifier
            .fillMaxWidth()
            .background(bgColor)
            .padding(horizontal = 6.dp, vertical = 2.dp)
    ) {
        // Search & Category Header Row
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 3.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            // Search Bar
            Box(
                modifier = Modifier
                    .weight(1f)
                    .height(34.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(surfaceColor)
                    .padding(horizontal = 8.dp),
                contentAlignment = Alignment.CenterStart
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Search,
                        contentDescription = "Search",
                        tint = textColor.copy(alpha = 0.6f),
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    BasicTextField(
                        value = searchQuery,
                        onValueChange = { searchQuery = it },
                        textStyle = TextStyle(color = textColor, fontSize = 13.sp),
                        cursorBrush = SolidColor(accentColor),
                        singleLine = true,
                        modifier = Modifier.weight(1f),
                        decorationBox = { innerTextField ->
                            if (searchQuery.isEmpty()) {
                                Text(
                                    text = "Search emojis (e.g. fire, smile, love)...",
                                    color = textColor.copy(alpha = 0.45f),
                                    fontSize = 11.5.sp
                                )
                            }
                            innerTextField()
                        }
                    )
                    if (searchQuery.isNotEmpty()) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Clear",
                            tint = textColor.copy(alpha = 0.6f),
                            modifier = Modifier
                                .size(16.dp)
                                .clickable { searchQuery = "" }
                        )
                    }
                }
            }
        }

        // Category Tab Bar (visible when search is empty)
        if (searchQuery.isEmpty()) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState())
                    .padding(vertical = 2.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                categories.forEachIndexed { index, cat ->
                    val isSelected = index == selectedCategoryIndex
                    Box(
                        modifier = Modifier
                            .padding(horizontal = 3.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .background(if (isSelected) accentColor.copy(alpha = 0.2f) else Color.Transparent)
                            .clickable { selectedCategoryIndex = index }
                            .padding(horizontal = 8.dp, vertical = 4.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = cat.icon + " " + cat.title,
                            color = if (isSelected) accentColor else textColor.copy(alpha = 0.8f),
                            fontSize = 12.sp,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                        )
                    }
                }
            }
        }

        // Emoji Grid
        LazyVerticalGrid(
            columns = GridCells.Fixed(8),
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
                .background(surfaceColor.copy(alpha = 0.5f), RoundedCornerShape(8.dp))
                .padding(2.dp)
        ) {
            items(filteredEmojis) { emoji ->
                Box(
                    modifier = Modifier
                        .padding(2.dp)
                        .aspectRatio(1f)
                        .clip(CircleShape)
                        .clickable {
                            saveRecentEmoji(emoji)
                            onEmojiSelected(emoji)
                        },
                    contentAlignment = Alignment.Center
                ) {
                    Text(text = emoji, fontSize = 23.sp)
                }
            }
        }

        // Bottom Bar: ABC / Backspace / Close
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 3.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Button(
                onClick = onClose,
                colors = ButtonDefaults.buttonColors(containerColor = surfaceColor, contentColor = textColor),
                shape = RoundedCornerShape(8.dp),
                modifier = Modifier.height(36.dp)
            ) {
                Text("ABC", fontWeight = FontWeight.Bold, fontSize = 12.sp)
            }

            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                IconButton(
                    onClick = onBackspace,
                    modifier = Modifier
                        .size(36.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(surfaceColor)
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.Backspace,
                        contentDescription = "Backspace",
                        tint = textColor,
                        modifier = Modifier.size(18.dp)
                    )
                }

                IconButton(
                    onClick = onClose,
                    modifier = Modifier
                        .size(36.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(accentColor)
                ) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "Close",
                        tint = Color.White,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }
        }
    }
}
