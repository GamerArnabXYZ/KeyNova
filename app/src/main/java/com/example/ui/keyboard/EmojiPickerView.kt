package com.example.ui.keyboard

import android.content.Context
import androidx.activity.compose.BackHandler
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
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Backspace
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.outlined.Mood
import androidx.compose.material.icons.outlined.Schedule
import androidx.compose.material.icons.outlined.StickyNote2
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.data.KeyboardThemeOption

enum class EmojiSubMode {
    EMOJI,
    GIF,
    STICKER,
    EMOTICON
}

@Composable
fun EmojiPickerView(
    theme: KeyboardThemeOption,
    onEmojiSelected: (String) -> Unit,
    onMediaSelected: (String, String, String) -> Unit = { url, _, _ -> onEmojiSelected(url) },
    onBackspace: () -> Unit,
    onClose: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val prefs = remember { context.getSharedPreferences("keynova_recent_emojis", Context.MODE_PRIVATE) }

    var subMode by remember { mutableStateOf(EmojiSubMode.EMOJI) }
    var isSearchActive by remember { mutableStateOf(false) }
    var searchQuery by remember { mutableStateOf("") }
    val focusRequester = remember { FocusRequester() }

    BackHandler {
        if (isSearchActive) {
            isSearchActive = false
            searchQuery = ""
        } else {
            onClose()
        }
    }

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
    val indicatorColor = if (theme == KeyboardThemeOption.LIGHT) accentColor else Color.White

    // Category selection indices per mode
    var selectedEmojiCategoryIndex by remember { mutableIntStateOf(0) }
    var selectedGifCategoryIndex by remember { mutableIntStateOf(0) }
    var selectedStickerCategoryIndex by remember { mutableIntStateOf(0) }
    var selectedEmoticonCategoryIndex by remember { mutableIntStateOf(0) }

    // Categories list with Recent at the beginning (validatedCategories is pre-computed lazily once)
    val categories = remember(recentEmojis) {
        listOf(
            EmojiCategory(
                title = "Recent",
                iconVector = Icons.Outlined.Schedule,
                emojis = recentEmojis.filter { EmojiValidator.isRenderable(it) }
            )
        ) + EmojiCatalog.validatedCategories
    }

    val emoticonCategories = remember(recentEmoticons) {
        listOf(
            EmoticonCategory(title = "Recent", icon = "🕒", emoticons = recentEmoticons)
        ) + EmoticonCatalog.categories
    }

    // Filtered items based on query
    val filteredEmojis = remember(searchQuery, selectedEmojiCategoryIndex, recentEmojis, categories) {
        if (searchQuery.isNotBlank()) {
            val q = searchQuery.trim().lowercase()
            val matches = mutableListOf<String>()
            EmojiCatalog.emojiTags.forEach { (tag, list) ->
                if (tag.contains(q) || q.contains(tag)) {
                    matches.addAll(list)
                }
            }
            categories.flatMap { it.emojis }.filter { it.contains(q) }.forEach { matches.add(it) }
            matches.filter { EmojiValidator.isRenderable(it) }.distinct().ifEmpty {
                categories.flatMap { it.emojis }.take(64)
            }
        } else {
            categories.getOrNull(selectedEmojiCategoryIndex)?.emojis ?: emptyList()
        }
    }

    val filteredGifs = remember(searchQuery, selectedGifCategoryIndex) {
        if (searchQuery.isNotBlank()) {
            val q = searchQuery.trim().lowercase()
            MediaCatalog.allGifs.filter { gif ->
                gif.title.lowercase().contains(q) || gif.category.lowercase().contains(q) || gif.tags.any { it.contains(q) }
            }.ifEmpty { MediaCatalog.allGifs }
        } else {
            MediaCatalog.gifCategories.getOrNull(selectedGifCategoryIndex)?.gifs ?: MediaCatalog.trendingGifs
        }
    }

    val filteredStickers = remember(searchQuery, selectedStickerCategoryIndex) {
        if (searchQuery.isNotBlank()) {
            val q = searchQuery.trim().lowercase()
            MediaCatalog.allStickers.filter { sticker ->
                sticker.title.lowercase().contains(q) || sticker.category.lowercase().contains(q) || sticker.tags.any { it.contains(q) }
            }.ifEmpty { MediaCatalog.allStickers }
        } else {
            MediaCatalog.stickerCategories.getOrNull(selectedStickerCategoryIndex)?.stickers ?: MediaCatalog.trendingStickers
        }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(bgColor)
            .padding(horizontal = 4.dp, vertical = 2.dp)
    ) {
        // TOP STRIP (Search icon + Category strip)
        if (isSearchActive) {
            // Search Input Header
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(38.dp)
                    .padding(horizontal = 2.dp, vertical = 2.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(
                    onClick = {
                        isSearchActive = false
                        searchQuery = ""
                    },
                    modifier = Modifier.size(36.dp)
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Back",
                        tint = textColor,
                        modifier = Modifier.size(20.dp)
                    )
                }

                Box(
                    modifier = Modifier
                        .weight(1f)
                        .height(34.dp)
                        .clip(RoundedCornerShape(17.dp))
                        .background(surfaceColor)
                        .padding(horizontal = 12.dp),
                    contentAlignment = Alignment.CenterStart
                ) {
                    BasicTextField(
                        value = searchQuery,
                        onValueChange = { searchQuery = it },
                        textStyle = TextStyle(color = textColor, fontSize = 13.5.sp),
                        cursorBrush = SolidColor(indicatorColor),
                        singleLine = true,
                        modifier = Modifier
                            .fillMaxWidth()
                            .focusRequester(focusRequester),
                        decorationBox = { innerTextField ->
                            if (searchQuery.isEmpty()) {
                                Text(
                                    text = when (subMode) {
                                        EmojiSubMode.EMOJI -> "Search emojis..."
                                        EmojiSubMode.GIF -> "Search GIFs (e.g. dance, lol)..."
                                        EmojiSubMode.STICKER -> "Search stickers (e.g. meme, cat)..."
                                        EmojiSubMode.EMOTICON -> "Search emoticons..."
                                    },
                                    color = textColor.copy(alpha = 0.5f),
                                    fontSize = 12.5.sp
                                )
                            }
                            innerTextField()
                        }
                    )
                }

                LaunchedEffect(Unit) {
                    focusRequester.requestFocus()
                }

                if (searchQuery.isNotEmpty()) {
                    IconButton(
                        onClick = { searchQuery = "" },
                        modifier = Modifier.size(36.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Clear",
                            tint = textColor.copy(alpha = 0.7f),
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
            }
        } else {
            // Top Category Strip per Mode
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(38.dp)
                    .padding(horizontal = 2.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(
                    onClick = { isSearchActive = true },
                    modifier = Modifier.size(36.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Search,
                        contentDescription = "Search",
                        tint = textColor.copy(alpha = 0.75f),
                        modifier = Modifier.size(20.dp)
                    )
                }

                Spacer(modifier = Modifier.width(2.dp))

                when (subMode) {
                    EmojiSubMode.EMOJI -> {
                        Row(
                            modifier = Modifier
                                .weight(1f)
                                .horizontalScroll(rememberScrollState()),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            categories.forEachIndexed { index, cat ->
                                val isSelected = index == selectedEmojiCategoryIndex
                                Column(
                                    modifier = Modifier
                                        .height(38.dp)
                                        .clip(RoundedCornerShape(6.dp))
                                        .clickable { selectedEmojiCategoryIndex = index }
                                        .padding(horizontal = 8.dp),
                                    horizontalAlignment = Alignment.CenterHorizontally,
                                    verticalArrangement = Arrangement.Center
                                ) {
                                    Icon(
                                        imageVector = cat.iconVector,
                                        contentDescription = cat.title,
                                        tint = if (isSelected) indicatorColor else textColor.copy(alpha = 0.45f),
                                        modifier = Modifier.size(19.dp)
                                    )
                                    Spacer(modifier = Modifier.height(3.dp))
                                    if (isSelected) {
                                        Box(
                                            modifier = Modifier
                                                .width(18.dp)
                                                .height(2.dp)
                                                .clip(RoundedCornerShape(1.dp))
                                                .background(indicatorColor)
                                        )
                                    } else {
                                        Spacer(modifier = Modifier.height(2.dp))
                                    }
                                }
                            }
                        }
                    }

                    EmojiSubMode.GIF -> {
                        Row(
                            modifier = Modifier
                                .weight(1f)
                                .horizontalScroll(rememberScrollState()),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            MediaCatalog.gifCategories.forEachIndexed { index, cat ->
                                val isSelected = index == selectedGifCategoryIndex
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(12.dp))
                                        .background(if (isSelected) accentColor.copy(alpha = 0.25f) else surfaceColor.copy(alpha = 0.4f))
                                        .clickable { selectedGifCategoryIndex = index }
                                        .padding(horizontal = 10.dp, vertical = 5.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = "${cat.icon} ${cat.title}",
                                        color = if (isSelected) indicatorColor else textColor.copy(alpha = 0.7f),
                                        fontSize = 11.5.sp,
                                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
                                    )
                                }
                            }
                        }
                    }

                    EmojiSubMode.STICKER -> {
                        Row(
                            modifier = Modifier
                                .weight(1f)
                                .horizontalScroll(rememberScrollState()),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            MediaCatalog.stickerCategories.forEachIndexed { index, cat ->
                                val isSelected = index == selectedStickerCategoryIndex
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(12.dp))
                                        .background(if (isSelected) accentColor.copy(alpha = 0.25f) else surfaceColor.copy(alpha = 0.4f))
                                        .clickable { selectedStickerCategoryIndex = index }
                                        .padding(horizontal = 10.dp, vertical = 5.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = "${cat.icon} ${cat.title}",
                                        color = if (isSelected) indicatorColor else textColor.copy(alpha = 0.7f),
                                        fontSize = 11.5.sp,
                                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
                                    )
                                }
                            }
                        }
                    }

                    EmojiSubMode.EMOTICON -> {
                        Row(
                            modifier = Modifier
                                .weight(1f)
                                .horizontalScroll(rememberScrollState()),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            emoticonCategories.forEachIndexed { index, cat ->
                                val isSelected = index == selectedEmoticonCategoryIndex
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(12.dp))
                                        .background(if (isSelected) accentColor.copy(alpha = 0.25f) else surfaceColor.copy(alpha = 0.4f))
                                        .clickable { selectedEmoticonCategoryIndex = index }
                                        .padding(horizontal = 10.dp, vertical = 5.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = cat.title,
                                        color = if (isSelected) indicatorColor else textColor.copy(alpha = 0.7f),
                                        fontSize = 12.sp,
                                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }

        // CENTER MEDIA CONTENT
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
        ) {
            when (subMode) {
                EmojiSubMode.EMOJI -> {
                    LazyVerticalGrid(
                        columns = GridCells.Fixed(8),
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(horizontal = 2.dp, vertical = 2.dp),
                        contentPadding = PaddingValues(vertical = 2.dp)
                    ) {
                        items(filteredEmojis, key = { it }) { emoji ->
                            Box(
                                modifier = Modifier
                                    .aspectRatio(1f)
                                    .clip(CircleShape)
                                    .clickable {
                                        saveRecentEmoji(emoji)
                                        onEmojiSelected(emoji)
                                    },
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = emoji,
                                    fontSize = 23.sp,
                                    textAlign = TextAlign.Center
                                )
                            }
                        }
                    }
                }

                EmojiSubMode.GIF -> {
                    LazyVerticalGrid(
                        columns = GridCells.Fixed(2),
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(horizontal = 4.dp, vertical = 2.dp),
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        verticalArrangement = Arrangement.spacedBy(6.dp),
                        contentPadding = PaddingValues(vertical = 4.dp)
                    ) {
                        items(filteredGifs, key = { it.id }) { gif ->
                            Surface(
                                shape = RoundedCornerShape(10.dp),
                                color = surfaceColor.copy(alpha = 0.7f),
                                border = androidx.compose.foundation.BorderStroke(
                                    0.6.dp,
                                    textColor.copy(alpha = 0.15f)
                                ),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(100.dp)
                                    .clip(RoundedCornerShape(10.dp))
                                    .clickable {
                                        onMediaSelected(gif.url, "image/gif", gif.title)
                                    }
                            ) {
                                Box(modifier = Modifier.fillMaxSize()) {
                                    AsyncImage(
                                        model = gif.url,
                                        contentDescription = gif.title,
                                        contentScale = ContentScale.Crop,
                                        modifier = Modifier.fillMaxSize()
                                    )
                                    // Title chip at bottom
                                    Box(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .align(Alignment.BottomCenter)
                                            .background(Color.Black.copy(alpha = 0.55f))
                                            .padding(horizontal = 6.dp, vertical = 3.dp)
                                    ) {
                                        Text(
                                            text = gif.title,
                                            color = Color.White,
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.SemiBold,
                                            maxLines = 1,
                                            overflow = TextOverflow.Ellipsis
                                        )
                                    }
                                }
                            }
                        }
                    }
                }

                EmojiSubMode.STICKER -> {
                    LazyVerticalGrid(
                        columns = GridCells.Fixed(3),
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(horizontal = 4.dp, vertical = 2.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                        contentPadding = PaddingValues(vertical = 4.dp)
                    ) {
                        items(filteredStickers, key = { it.id }) { sticker ->
                            Surface(
                                shape = RoundedCornerShape(12.dp),
                                color = surfaceColor.copy(alpha = 0.75f),
                                border = androidx.compose.foundation.BorderStroke(
                                    0.6.dp,
                                    textColor.copy(alpha = 0.15f)
                                ),
                                modifier = Modifier
                                    .aspectRatio(1f)
                                    .clip(RoundedCornerShape(12.dp))
                                    .clickable {
                                        onMediaSelected(sticker.url, "image/png", sticker.title)
                                    }
                            ) {
                                Box(
                                    modifier = Modifier.fillMaxSize(),
                                    contentAlignment = Alignment.Center
                                ) {
                                    // Fallback emoji preview
                                    Text(
                                        text = sticker.previewEmoji,
                                        fontSize = 32.sp,
                                        textAlign = TextAlign.Center
                                    )
                                    AsyncImage(
                                        model = sticker.url,
                                        contentDescription = sticker.title,
                                        contentScale = ContentScale.Fit,
                                        modifier = Modifier
                                            .fillMaxSize()
                                            .padding(6.dp)
                                    )
                                    // Title label
                                    Box(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .align(Alignment.BottomCenter)
                                            .background(surfaceColor.copy(alpha = 0.85f))
                                            .padding(horizontal = 4.dp, vertical = 2.dp)
                                    ) {
                                        Text(
                                            text = sticker.title,
                                            color = textColor,
                                            fontSize = 9.5.sp,
                                            fontWeight = FontWeight.Medium,
                                            maxLines = 1,
                                            textAlign = TextAlign.Center,
                                            overflow = TextOverflow.Ellipsis,
                                            modifier = Modifier.fillMaxWidth()
                                        )
                                    }
                                }
                            }
                        }
                    }
                }

                EmojiSubMode.EMOTICON -> {
                    val activeEmoticons = emoticonCategories.getOrNull(selectedEmoticonCategoryIndex)?.emoticons ?: emptyList()
                    LazyVerticalGrid(
                        columns = GridCells.Fixed(3),
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(horizontal = 4.dp, vertical = 2.dp),
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        verticalArrangement = Arrangement.spacedBy(6.dp),
                        contentPadding = PaddingValues(vertical = 4.dp)
                    ) {
                        items(activeEmoticons, key = { it }) { emoticon ->
                            Surface(
                                shape = RoundedCornerShape(10.dp),
                                color = surfaceColor,
                                border = androidx.compose.foundation.BorderStroke(
                                    0.8.dp,
                                    if (theme == KeyboardThemeOption.LIGHT) Color(0xFFCBD5E1) else Color(0xFF333333)
                                ),
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
                                    modifier = Modifier.padding(horizontal = 6.dp)
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
            }
        }

        // BOTTOM BAR (ABC on left, Mode tabs in center: [😊 Emoji] [GIF] [🏷️ Sticker] [¯\_(ツ)_/¯], Backspace on right)
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(44.dp)
                .padding(horizontal = 4.dp, vertical = 3.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Left: ABC button to return to letters
            Box(
                modifier = Modifier
                    .width(48.dp)
                    .height(36.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(surfaceColor)
                    .clickable { onClose() },
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "ABC",
                    color = textColor,
                    fontWeight = FontWeight.Bold,
                    fontSize = 12.5.sp
                )
            }

            // Center: Mode Switchers (Emoji / GIF / Sticker / Emoticon)
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Emojis Tab
                Column(
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .clickable {
                            subMode = EmojiSubMode.EMOJI
                            isSearchActive = false
                        }
                        .padding(horizontal = 8.dp, vertical = 2.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Icon(
                        imageVector = Icons.Outlined.Mood,
                        contentDescription = "Emojis",
                        tint = if (subMode == EmojiSubMode.EMOJI) indicatorColor else textColor.copy(alpha = 0.45f),
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    if (subMode == EmojiSubMode.EMOJI) {
                        Box(
                            modifier = Modifier
                                .width(16.dp)
                                .height(2.dp)
                                .clip(RoundedCornerShape(1.dp))
                                .background(indicatorColor)
                        )
                    } else {
                        Spacer(modifier = Modifier.height(2.dp))
                    }
                }

                // GIF Tab
                Column(
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .clickable {
                            subMode = EmojiSubMode.GIF
                            isSearchActive = false
                        }
                        .padding(horizontal = 8.dp, vertical = 2.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "GIF",
                        color = if (subMode == EmojiSubMode.GIF) indicatorColor else textColor.copy(alpha = 0.45f),
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Black
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    if (subMode == EmojiSubMode.GIF) {
                        Box(
                            modifier = Modifier
                                .width(16.dp)
                                .height(2.dp)
                                .clip(RoundedCornerShape(1.dp))
                                .background(indicatorColor)
                        )
                    } else {
                        Spacer(modifier = Modifier.height(2.dp))
                    }
                }

                // Stickers Tab
                Column(
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .clickable {
                            subMode = EmojiSubMode.STICKER
                            isSearchActive = false
                        }
                        .padding(horizontal = 8.dp, vertical = 2.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Icon(
                        imageVector = Icons.Outlined.StickyNote2,
                        contentDescription = "Stickers",
                        tint = if (subMode == EmojiSubMode.STICKER) indicatorColor else textColor.copy(alpha = 0.45f),
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    if (subMode == EmojiSubMode.STICKER) {
                        Box(
                            modifier = Modifier
                                .width(16.dp)
                                .height(2.dp)
                                .clip(RoundedCornerShape(1.dp))
                                .background(indicatorColor)
                        )
                    } else {
                        Spacer(modifier = Modifier.height(2.dp))
                    }
                }

                // Emoticon / Kaomoji Tab
                Column(
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .clickable {
                            subMode = EmojiSubMode.EMOTICON
                            isSearchActive = false
                        }
                        .padding(horizontal = 8.dp, vertical = 2.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "¯\\_(ツ)_/¯",
                        color = if (subMode == EmojiSubMode.EMOTICON) indicatorColor else textColor.copy(alpha = 0.45f),
                        fontSize = 11.5.sp,
                        fontWeight = if (subMode == EmojiSubMode.EMOTICON) FontWeight.Bold else FontWeight.Normal
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    if (subMode == EmojiSubMode.EMOTICON) {
                        Box(
                            modifier = Modifier
                                .width(20.dp)
                                .height(2.dp)
                                .clip(RoundedCornerShape(1.dp))
                                .background(indicatorColor)
                        )
                    } else {
                        Spacer(modifier = Modifier.height(2.dp))
                    }
                }
            }

            // Right: Backspace button
            Box(
                modifier = Modifier
                    .width(48.dp)
                    .height(36.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(surfaceColor)
                    .clickable { onBackspace() },
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.Backspace,
                    contentDescription = "Backspace",
                    tint = textColor,
                    modifier = Modifier.size(19.dp)
                )
            }
        }
    }
}
