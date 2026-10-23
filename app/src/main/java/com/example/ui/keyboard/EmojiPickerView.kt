package com.example.ui.keyboard

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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Backspace
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
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
            title = "Food",
            icon = "🍕",
            emojis = listOf(
                "🍏", "🍎", "🍐", "🍊", "🍋", "🍌", "🍉", "🍇", "🍓", "🫐",
                "🍔", "🍟", "🍕", "🌭", "🥪", "🌮", "🌯", "🍜", "🍣", "🍦",
                "🍩", "🍪", "🎂", "🍫", "🍿", "☕", "🧋", "🍵", "🥤", "🍺"
            )
        )
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
    val bgColor = Color(theme.backgroundHex)
    val textColor = Color(theme.textHex)
    val accentColor = Color(theme.accentHex)
    val surfaceColor = Color(theme.surfaceHex)

    var selectedCategoryIndex by remember { mutableIntStateOf(0) }
    val categories = EmojiCatalog.categories
    val currentEmojis = categories.getOrNull(selectedCategoryIndex)?.emojis ?: emptyList()

    Column(
        modifier = modifier
            .fillMaxWidth()
            .background(bgColor)
            .padding(horizontal = 4.dp, vertical = 2.dp)
    ) {
        // Category Tab Bar
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState())
                .padding(vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            categories.forEachIndexed { index, cat ->
                val isSelected = index == selectedCategoryIndex
                Box(
                    modifier = Modifier
                        .padding(horizontal = 4.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(if (isSelected) accentColor.copy(alpha = 0.2f) else Color.Transparent)
                        .clickable { selectedCategoryIndex = index }
                        .padding(horizontal = 10.dp, vertical = 6.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = cat.icon + " " + cat.title,
                        color = if (isSelected) accentColor else textColor.copy(alpha = 0.8f),
                        fontSize = 13.sp,
                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                    )
                }
            }
        }

        // Emoji Grid
        LazyVerticalGrid(
            columns = GridCells.Fixed(8),
            modifier = Modifier
                .fillMaxWidth()
                .height(180.dp)
                .background(surfaceColor.copy(alpha = 0.5f), RoundedCornerShape(8.dp))
                .padding(4.dp)
        ) {
            items(currentEmojis) { emoji ->
                Box(
                    modifier = Modifier
                        .padding(2.dp)
                        .aspectRatio(1f)
                        .clip(CircleShape)
                        .clickable { onEmojiSelected(emoji) },
                    contentAlignment = Alignment.Center
                ) {
                    Text(text = emoji, fontSize = 22.sp)
                }
            }
        }

        // Bottom Bar: ABC / Backspace / Close
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 4.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Button(
                onClick = onClose,
                colors = ButtonDefaults.buttonColors(containerColor = surfaceColor, contentColor = textColor),
                shape = RoundedCornerShape(8.dp),
                modifier = Modifier.height(40.dp)
            ) {
                Text("ABC", fontWeight = FontWeight.Bold, fontSize = 13.sp)
            }

            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                IconButton(
                    onClick = onBackspace,
                    modifier = Modifier
                        .size(40.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(surfaceColor)
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.Backspace,
                        contentDescription = "Backspace",
                        tint = textColor
                    )
                }

                IconButton(
                    onClick = onClose,
                    modifier = Modifier
                        .size(40.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(accentColor)
                ) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "Close",
                        tint = Color.White
                    )
                }
            }
        }
    }
}
