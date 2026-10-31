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
import androidx.compose.material.icons.outlined.Fastfood
import androidx.compose.material.icons.outlined.FavoriteBorder
import androidx.compose.material.icons.outlined.Flight
import androidx.compose.material.icons.outlined.Laptop
import androidx.compose.material.icons.outlined.Lightbulb
import androidx.compose.material.icons.outlined.Mood
import androidx.compose.material.icons.outlined.PanTool
import androidx.compose.material.icons.outlined.Pets
import androidx.compose.material.icons.outlined.Schedule
import androidx.compose.material.icons.outlined.SportsEsports
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.KeyboardThemeOption

data class EmojiCategory(
    val title: String,
    val iconVector: ImageVector,
    val emojis: List<String>
)

object EmojiCatalog {
    val categories = listOf(
        EmojiCategory(
            title = "Smileys",
            iconVector = Icons.Outlined.Mood,
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
            iconVector = Icons.Outlined.PanTool,
            emojis = listOf(
                "👋", "🤚", "🖐️", "✋", "🖖", "🫱", "🫲", "👌", "🤌", "🤏",
                "✌️", "🤞", "🫰", "🤟", "🤘", "🤙", "👈", "👉", "👆", "🖕",
                "👇", "☝️", "👍", "👎", "✊", "👊", "🤛", "🤜", "👏", "🙌",
                "🫶", "👐", "🤲", "🤝", "🙏", "✍️", "💅", "🤳", "💪", "🦾"
            )
        ),
        EmojiCategory(
            title = "Hearts",
            iconVector = Icons.Outlined.FavoriteBorder,
            emojis = listOf(
                "❤️", "🧡", "💛", "💚", "💙", "💜", "🖤", "🤍", "🤎", "💔",
                "❤️‍🔥", "❤️‍🩹", "❣️", "💕", "💞", "💓", "💗", "💖", "💘", "💝",
                "💟", "💌", "💋", "💯", "🔥", "✨", "💫", "⭐", "🌟", "⚡"
            )
        ),
        EmojiCategory(
            title = "Animals",
            iconVector = Icons.Outlined.Pets,
            emojis = listOf(
                "🐶", "🐱", "🐭", "🐹", "🐰", "🦊", "🐻", "🐼", "🐨", "🐯",
                "🦁", "🐮", "🐷", "🐸", "🐵", "🐔", "🐧", "🐦", "🦆", "🦅",
                "🦉", "🦇", "🐺", "🐗", "🐴", "🦄", "🐝", "🦋", "🐌", "🐞",
                "🌴", "🌵", "🌾", "🌿", "🍀", "🍁", "🍂", "🍃", "🌸", "🌺"
            )
        ),
        EmojiCategory(
            title = "Food",
            iconVector = Icons.Outlined.Fastfood,
            emojis = listOf(
                "🍏", "🍎", "🍐", "🍊", "🍋", "🍌", "🍉", "🍇", "🍓", "🫐",
                "🍔", "🍟", "🍕", "🌭", "🥪", "🌮", "🌯", "🍜", "🍣", "🍦",
                "🍩", "🍪", "🎂", "🍫", "🍿", "☕", "🧋", "🍵", "🥤", "🍺"
            )
        ),
        EmojiCategory(
            title = "Gaming",
            iconVector = Icons.Outlined.SportsEsports,
            emojis = listOf(
                "🎮", "🕹️", "👾", "🎯", "🎲", "🧩", "🎳", "🏆", "🥇", "🥈",
                "🥉", "👑", "⚔️", "🛡️", "💣", "🪄", "🔮", "🛸", "🤖", "🚀"
            )
        ),
        EmojiCategory(
            title = "Dev",
            iconVector = Icons.Outlined.Laptop,
            emojis = listOf(
                "💻", "🖥️", "⌨️", "🖱️", "📱", "💾", "💿", "🔌", "🔋", "💡",
                "🛠️", "⚙️", "🔧", "🪛", "📡", "🛰️", "🔬", "🔭", "📊", "📈"
            )
        ),
        EmojiCategory(
            title = "Travel",
            iconVector = Icons.Outlined.Flight,
            emojis = listOf(
                "🚗", "🚕", "🚙", "🚌", "🏎️", "🚓", "🚑", "🚒", "🚲", "🏍️",
                "✈️", "🛫", "🛬", "🚀", "🛸", "🚁", "⛵", "🚢", "🏠", "🏙️"
            )
        ),
        EmojiCategory(
            title = "Objects",
            iconVector = Icons.Outlined.Lightbulb,
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

data class EmoticonCategory(
    val title: String,
    val icon: String,
    val emoticons: List<String>
)

object EmoticonCatalog {
    val categories = listOf(
        EmoticonCategory(
            title = "Shrug & Cool",
            icon = "¯\\_(ツ)_/¯",
            emoticons = listOf("¯\\_(ツ)_/¯", "( ͡° ͜ʖ ͡°)", "(•_•)", "(▀̿Ĺ̯▀̿ ̿)", "(⌐■_■)", "¯\\(°_o)/¯", "(☞ﾟヮﾟ)☞", "☜(ﾟヮﾟ☜)")
        ),
        EmoticonCategory(
            title = "Happy",
            icon = "(^o^)",
            emoticons = listOf("(^o^)", "(*^▽^*)", "(✿◠‿◠)", "(◕‿◕)", "(◠‿◠✿)", "(＾◡＾)", "(ﾉ◕ヮ◕)ﾉ*:･ﾟ✧", "٩(◕‿◕｡)۶", "(o^▽^o)", "(≧◡≦)")
        ),
        EmoticonCategory(
            title = "Love",
            icon = "(｡♥‿♥｡)",
            emoticons = listOf("(♡-_-♡)", "(｡♥‿♥｡)", "(⁄ ⁄>⁄ ▽ ⁄<⁄ ⁄)", "(♥ω♥*)", "(✿ ♥‿♥)", "(♡‿♡)", "(灬♥ω♥灬)", "(ര‿ര♥)")
        ),
        EmoticonCategory(
            title = "Cute / Animals",
            icon = "ʕ•ᴥ•ʔ",
            emoticons = listOf("ʕ•ᴥ•ʔ", "(=^･^=)", "(づ｡◕‿‿◕｡)づ", "⊂(・ヮ・⊂)", "(=^ェ^=)", "ʕᵔᴥᵔʔ", "(ᵔᴥᵔ)", "(=^-ω-^=)")
        ),
        EmoticonCategory(
            title = "Angry / Rage",
            icon = "(╯°□°)╯",
            emoticons = listOf("(╯°□°)╯︵ ┻━┻", "(ง'̀-'́)ง", "(ಠ_ಠ)", "(｀Д´)", "(ノಠ益ಠ)ノ彡┻━┻", "凸(｀0´)凸", "٩(╬ʘ益ʘ╬)۶")
        ),
        EmoticonCategory(
            title = "Sad / Cry",
            icon = "(╥﹏╥)",
            emoticons = listOf("(╥﹏╥)", "(T_T)", "(｡•́︿•̀｡)", "(;´༎ຶٹ༎ຶ)", "(ಥ﹏ಥ)", "(╯︵╰,)", "(ノ_<。)", "(っ˘̩╭╮˘̩)っ")
        ),
        EmoticonCategory(
            title = "Confused",
            icon = "(・_・;)",
            emoticons = listOf("(°o°)", "(・_・;)", "(¬_¬)", "(⊙_⊙)", "(ﾟДﾟ?))", "(・o・)", "(o_O)", "(°ロ°)", "(>_<)")
        ),
        EmoticonCategory(
            title = "Classic",
            icon = ":-)",
            emoticons = listOf(":-)", ":-D", ":-P", ";-)", "B-)", "XD", ":-O", ":-*", ":-(", ":'-(", ":-/", ":-|", "O:)", "o_O")
        )
    )
}

enum class EmojiSubMode {
    EMOJI,
    EMOTICON
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

    var subMode by remember { mutableStateOf(EmojiSubMode.EMOJI) }

    var recentEmojis by remember {
        val raw = prefs.getString("recent_list", null)
        val list = if (raw != null) raw.split(",").filter { it.isNotBlank() } else listOf("😀", "😂", "❤️", "👍", "🔥", "🎉", "😎", "🙏")
        mutableStateOf(list)
    }

    var recentEmoticons by remember {
        val raw = prefs.getString("recent_emoticons_list", null)
        val list = if (raw != null) raw.split("||").filter { it.isNotBlank() } else listOf("¯\\_(ツ)_/¯", "( ͡° ͜ʖ ͡°)", "(^o^)", "(｡♥‿♥｡)", "ʕ•ᴥ•ʔ")
        mutableStateOf(list)
    }

    fun saveRecentEmoji(emoji: String) {
        val updated = (listOf(emoji) + recentEmojis.filter { it != emoji }).take(30)
        recentEmojis = updated
        prefs.edit().putString("recent_list", updated.joinToString(",")).apply()
    }

    fun saveRecentEmoticon(emoticon: String) {
        val updated = (listOf(emoticon) + recentEmoticons.filter { it != emoticon }).take(20)
        recentEmoticons = updated
        prefs.edit().putString("recent_emoticons_list", updated.joinToString("||")).apply()
    }

    val bgColor = Color(theme.backgroundHex)
    val textColor = Color(theme.textHex)
    val accentColor = Color(theme.accentHex)
    val surfaceColor = Color(theme.surfaceHex)

    var searchQuery by remember { mutableStateOf("") }
    var selectedCategoryIndex by remember { mutableIntStateOf(0) }
    var selectedEmoticonCategoryIndex by remember { mutableIntStateOf(0) }

    // Categories list with Recent at the beginning
    val categories = remember(recentEmojis) {
        listOf(
            EmojiCategory(title = "Recent", iconVector = Icons.Outlined.Schedule, emojis = recentEmojis)
        ) + EmojiCatalog.categories
    }

    val emoticonCategories = remember(recentEmoticons) {
        listOf(
            EmoticonCategory(title = "Recent", icon = "🕒", emoticons = recentEmoticons)
        ) + EmoticonCatalog.categories
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
        // Mode Switcher: [ 😊 Emoji ] [ ¯\_(ツ)_/¯ Emoticon ]
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 4.dp),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Surface(
                shape = RoundedCornerShape(16.dp),
                color = surfaceColor,
                border = androidx.compose.foundation.BorderStroke(1.dp, if (theme == KeyboardThemeOption.LIGHT) Color(0xFFCBD5E1) else Color(0xFF404040)),
                modifier = Modifier.padding(vertical = 2.dp)
            ) {
                Row(modifier = Modifier.padding(2.dp)) {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(14.dp))
                            .background(if (subMode == EmojiSubMode.EMOJI) accentColor else Color.Transparent)
                            .clickable { subMode = EmojiSubMode.EMOJI }
                            .padding(horizontal = 14.dp, vertical = 4.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "😀 Emojis",
                            color = if (subMode == EmojiSubMode.EMOJI) Color.White else textColor,
                            fontSize = 12.sp,
                            fontWeight = if (subMode == EmojiSubMode.EMOJI) FontWeight.Bold else FontWeight.Medium
                        )
                    }

                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(14.dp))
                            .background(if (subMode == EmojiSubMode.EMOTICON) accentColor else Color.Transparent)
                            .clickable { subMode = EmojiSubMode.EMOTICON }
                            .padding(horizontal = 14.dp, vertical = 4.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "¯\\_(ツ)_/¯ Emoticons",
                            color = if (subMode == EmojiSubMode.EMOTICON) Color.White else textColor,
                            fontSize = 12.sp,
                            fontWeight = if (subMode == EmojiSubMode.EMOTICON) FontWeight.Bold else FontWeight.Medium
                        )
                    }
                }
            }
        }

        if (subMode == EmojiSubMode.EMOJI) {
            // Search Bar
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 2.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(32.dp)
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

            // Category Tab Bar (visible when search is empty) - Gboard style icon-only tabs
            if (searchQuery.isEmpty()) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState())
                        .padding(vertical = 3.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(2.dp)
                ) {
                    categories.forEachIndexed { index, cat ->
                        val isSelected = index == selectedCategoryIndex
                        Box(
                            modifier = Modifier
                                .height(36.dp)
                                .widthIn(min = 40.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .background(if (isSelected) accentColor.copy(alpha = 0.18f) else Color.Transparent)
                                .clickable { selectedCategoryIndex = index }
                                .padding(horizontal = 8.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.Center
                            ) {
                                Icon(
                                    imageVector = cat.iconVector,
                                    contentDescription = cat.title,
                                    tint = if (isSelected) accentColor else textColor.copy(alpha = 0.55f),
                                    modifier = Modifier.size(20.dp)
                                )
                                if (isSelected) {
                                    Spacer(modifier = Modifier.height(2.dp))
                                    Box(
                                        modifier = Modifier
                                            .width(14.dp)
                                            .height(2.dp)
                                            .clip(RoundedCornerShape(1.dp))
                                            .background(accentColor)
                                    )
                                }
                            }
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
        } else {
            // Emoticon Mode: Category Bar & Emoticon Cards
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState())
                    .padding(vertical = 2.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                emoticonCategories.forEachIndexed { index, cat ->
                    val isSelected = index == selectedEmoticonCategoryIndex
                    Box(
                        modifier = Modifier
                            .padding(horizontal = 3.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .background(if (isSelected) accentColor.copy(alpha = 0.2f) else Color.Transparent)
                            .clickable { selectedEmoticonCategoryIndex = index }
                            .padding(horizontal = 8.dp, vertical = 4.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = cat.title,
                            color = if (isSelected) accentColor else textColor.copy(alpha = 0.8f),
                            fontSize = 12.sp,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                        )
                    }
                }
            }

            // Emoticons Grid
            val activeEmoticons = emoticonCategories.getOrNull(selectedEmoticonCategoryIndex)?.emoticons ?: emptyList()
            LazyVerticalGrid(
                columns = GridCells.Fixed(3),
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .background(surfaceColor.copy(alpha = 0.5f), RoundedCornerShape(8.dp))
                    .padding(4.dp),
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                items(activeEmoticons) { emoticon ->
                    Surface(
                        shape = RoundedCornerShape(10.dp),
                        color = surfaceColor,
                        border = androidx.compose.foundation.BorderStroke(0.8.dp, if (theme == KeyboardThemeOption.LIGHT) Color(0xFFCBD5E1) else Color(0xFF404040)),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(42.dp)
                            .clickable {
                                saveRecentEmoticon(emoticon)
                                onEmojiSelected(emoticon)
                            }
                    ) {
                        Box(
                            contentAlignment = Alignment.Center,
                            modifier = Modifier.padding(horizontal = 4.dp)
                        ) {
                            Text(
                                text = emoticon,
                                color = textColor,
                                fontSize = 13.5.sp,
                                fontWeight = FontWeight.Medium,
                                maxLines = 1
                            )
                        }
                    }
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
