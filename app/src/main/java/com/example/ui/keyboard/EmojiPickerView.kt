package com.example.ui.keyboard

import android.content.Context
import androidx.activity.compose.BackHandler
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
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Backspace
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.GridOn
import androidx.compose.material.icons.filled.Keyboard
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.outlined.Mood
import androidx.compose.material.icons.outlined.Schedule
import androidx.compose.material.icons.outlined.StickyNote2
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
    var subMode by remember { mutableStateOf(EmojiSubMode.EMOJI) }
    var searchQuery by remember { mutableStateOf("") }
    var isSearchActive by remember { mutableStateOf(false) }
    var showSearchKeyboard by remember { mutableStateOf(true) }
    var isNumericSearchKeyboard by remember { mutableStateOf(false) }

    // Intercept back key to close search or exit emoji mode
    BackHandler(enabled = true) {
        if (isSearchActive) {
            isSearchActive = false
            searchQuery = ""
        } else {
            onClose()
        }
    }

    val prefs = remember {
        context.getSharedPreferences("keynova_recent_emojis", Context.MODE_PRIVATE)
    }

    var recentEmojis by remember {
        val saved = prefs.getString("recents", "") ?: ""
        val list = if (saved.isEmpty()) {
            listOf("😊", "😂", "❤️", "👍", "🔥", "🙏", "🎉", "😍", "✨", "👏", "🤣", "🥺")
        } else {
            saved.split(",").filter { it.isNotBlank() }
        }
        mutableStateOf(list)
    }

    var recentEmoticons by remember {
        val saved = prefs.getString("recent_emoticons", "") ?: ""
        val list = if (saved.isEmpty()) {
            listOf("¯\\_(ツ)_/¯", "(^.^)", "(>_<)", "(^_^)/", "(o_O)", "(*_*)")
        } else {
            saved.split(",,,,").filter { it.isNotBlank() }
        }
        mutableStateOf(list)
    }

    val onSelectRecentEmoji: (String) -> Unit = { emoji ->
        onEmojiSelected(emoji)
        val updated = (listOf(emoji) + recentEmojis.filter { it != emoji }).take(30)
        recentEmojis = updated
        prefs.edit().putString("recents", updated.joinToString(",")).apply()
    }

    val onSelectRecentEmoticon: (String) -> Unit = { emo ->
        onEmojiSelected(emo)
        val updated = (listOf(emo) + recentEmoticons.filter { it != emo }).take(30)
        recentEmoticons = updated
        prefs.edit().putString("recent_emoticons", updated.joinToString(",,,,")).apply()
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

    // Categories list with Recent at the beginning
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

    // Quick filter chips for fast search
    val quickSearchChips = remember {
        listOf("Trending", "LOL", "Dance", "Love", "Sad", "Applause", "Cat", "Dog", "Coffee", "Gaming", "Party", "Yes", "No", "Shocked", "Sleepy", "Food", "Fire", "Meme")
    }

    // Filtered items based on query
    val filteredEmojis = remember(searchQuery, selectedEmojiCategoryIndex, recentEmojis, categories) {
        if (searchQuery.isNotBlank()) {
            val q = searchQuery.trim().lowercase()
            val tokens = q.split("\\s+".toRegex()).filter { it.isNotEmpty() }
            val matches = mutableListOf<String>()
            EmojiCatalog.emojiTags.forEach { (tag, list) ->
                if (tokens.any { tag.contains(it) || it.contains(tag) }) {
                    matches.addAll(list)
                }
            }
            categories.flatMap { it.emojis }.filter { emoji ->
                tokens.any { emoji.contains(it) }
            }.forEach { matches.add(it) }
            matches.filter { EmojiValidator.isRenderable(it) }.distinct()
        } else {
            categories.getOrNull(selectedEmojiCategoryIndex)?.emojis ?: emptyList()
        }
    }

    val filteredGifs = remember(searchQuery, selectedGifCategoryIndex) {
        if (searchQuery.isNotBlank()) {
            val q = searchQuery.trim().lowercase()
            val tokens = q.split("\\s+".toRegex()).filter { it.isNotEmpty() }
            MediaCatalog.allGifs.filter { gif ->
                val title = gif.title.lowercase()
                val cat = gif.category.lowercase()
                val tags = gif.tags.map { it.lowercase() }
                tokens.all { t ->
                    title.contains(t) || cat.contains(t) || tags.any { it.contains(t) || t.contains(it) }
                }
            }
        } else {
            MediaCatalog.gifCategories.getOrNull(selectedGifCategoryIndex)?.gifs ?: MediaCatalog.trendingGifs
        }
    }

    val filteredStickers = remember(searchQuery, selectedStickerCategoryIndex) {
        if (searchQuery.isNotBlank()) {
            val q = searchQuery.trim().lowercase()
            val tokens = q.split("\\s+".toRegex()).filter { it.isNotEmpty() }
            MediaCatalog.allStickers.filter { sticker ->
                val title = sticker.title.lowercase()
                val cat = sticker.category.lowercase()
                val tags = sticker.tags.map { it.lowercase() }
                tokens.all { t ->
                    title.contains(t) || cat.contains(t) || tags.any { it.contains(t) || t.contains(it) }
                }
            }
        } else {
            MediaCatalog.stickerCategories.getOrNull(selectedStickerCategoryIndex)?.stickers ?: MediaCatalog.trendingStickers
        }
    }

    val filteredEmoticons = remember(searchQuery, selectedEmoticonCategoryIndex, emoticonCategories) {
        if (searchQuery.isNotBlank()) {
            val q = searchQuery.trim().lowercase()
            val tokens = q.split("\\s+".toRegex()).filter { it.isNotEmpty() }
            emoticonCategories.flatMap { it.emoticons }.filter { emo ->
                tokens.any { emo.contains(it) || q.contains(it) }
            }.distinct()
        } else {
            emoticonCategories.getOrNull(selectedEmoticonCategoryIndex)?.emoticons ?: emptyList()
        }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(bgColor)
            .padding(horizontal = 4.dp, vertical = 2.dp)
    ) {
        // TOP HEADER STRIP (Search Header or Category Strip)
        if (isSearchActive) {
            // Search Input Header Bar
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

                // Search Query Display Field
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .height(34.dp)
                        .clip(RoundedCornerShape(17.dp))
                        .background(surfaceColor)
                        .clickable { showSearchKeyboard = true }
                        .padding(horizontal = 12.dp),
                    contentAlignment = Alignment.CenterStart
                ) {
                    if (searchQuery.isEmpty()) {
                        Text(
                            text = when (subMode) {
                                EmojiSubMode.EMOJI -> "Search emojis (e.g. fire, smile)..."
                                EmojiSubMode.GIF -> "Search GIFs (e.g. dance, lol, cat)..."
                                EmojiSubMode.STICKER -> "Search stickers (e.g. meme, pet)..."
                                EmojiSubMode.EMOTICON -> "Search emoticons..."
                            },
                            color = textColor.copy(alpha = 0.5f),
                            fontSize = 12.sp,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    } else {
                        Text(
                            text = searchQuery,
                            color = textColor,
                            fontSize = 13.5.sp,
                            fontWeight = FontWeight.Medium,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
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

                // Toggle Keyboard / Full Grid view
                IconButton(
                    onClick = { showSearchKeyboard = !showSearchKeyboard },
                    modifier = Modifier.size(36.dp)
                ) {
                    Icon(
                        imageVector = if (showSearchKeyboard) Icons.Default.GridOn else Icons.Default.Keyboard,
                        contentDescription = if (showSearchKeyboard) "Full Grid" else "Show Keyboard",
                        tint = if (showSearchKeyboard) indicatorColor else textColor.copy(alpha = 0.6f),
                        modifier = Modifier.size(20.dp)
                    )
                }
            }

            // Quick Search Chips Row
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(32.dp)
                    .horizontalScroll(rememberScrollState())
                    .padding(horizontal = 4.dp, vertical = 2.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                quickSearchChips.forEach { chip ->
                    val isSelected = searchQuery.equals(chip, ignoreCase = true)
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(12.dp))
                            .background(if (isSelected) accentColor.copy(alpha = 0.35f) else surfaceColor)
                            .border(
                                width = 0.8.dp,
                                color = if (isSelected) accentColor else textColor.copy(alpha = 0.12f),
                                shape = RoundedCornerShape(12.dp)
                            )
                            .clickable {
                                searchQuery = chip.lowercase()
                            }
                            .padding(horizontal = 10.dp, vertical = 4.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = chip,
                            color = if (isSelected) indicatorColor else textColor.copy(alpha = 0.8f),
                            fontSize = 11.5.sp,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                        )
                    }
                }
            }
        } else {
            // Normal Category Strip per Mode
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(38.dp)
                    .padding(horizontal = 2.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(
                    onClick = {
                        isSearchActive = true
                        showSearchKeyboard = true
                    },
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
                                        text = "${cat.icon} ${cat.title}",
                                        color = if (isSelected) indicatorColor else textColor.copy(alpha = 0.7f),
                                        fontSize = 11.5.sp,
                                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }

        Divider(
            color = textColor.copy(alpha = 0.08f),
            thickness = 0.8.dp,
            modifier = Modifier.padding(vertical = 2.dp)
        )

        // MAIN CONTENT AREA (Grid of items or Empty State)
        Box(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
        ) {
            when (subMode) {
                EmojiSubMode.EMOJI -> {
                    if (filteredEmojis.isEmpty()) {
                        SearchEmptyState(
                            query = searchQuery,
                            textColor = textColor,
                            surfaceColor = surfaceColor,
                            accentColor = accentColor,
                            onSuggestionClick = { searchQuery = it }
                        )
                    } else {
                        LazyVerticalGrid(
                            columns = GridCells.Fixed(8),
                            modifier = Modifier.fillMaxSize(),
                            contentPadding = PaddingValues(bottom = 8.dp)
                        ) {
                            items(filteredEmojis, key = { it }) { emoji ->
                                Box(
                                    modifier = Modifier
                                        .aspectRatio(1f)
                                        .clip(CircleShape)
                                        .clickable { onSelectRecentEmoji(emoji) },
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
                }

                EmojiSubMode.GIF -> {
                    if (filteredGifs.isEmpty()) {
                        SearchEmptyState(
                            query = searchQuery,
                            textColor = textColor,
                            surfaceColor = surfaceColor,
                            accentColor = accentColor,
                            onSuggestionClick = { searchQuery = it }
                        )
                    } else {
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
                                        .height(105.dp)
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
                                        // Title overlay badge
                                        Box(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .align(Alignment.BottomCenter)
                                                .background(Color.Black.copy(alpha = 0.65f))
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
                }

                EmojiSubMode.STICKER -> {
                    if (filteredStickers.isEmpty()) {
                        SearchEmptyState(
                            query = searchQuery,
                            textColor = textColor,
                            surfaceColor = surfaceColor,
                            accentColor = accentColor,
                            onSuggestionClick = { searchQuery = it }
                        )
                    } else {
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
                                        AsyncImage(
                                            model = sticker.url,
                                            contentDescription = sticker.title,
                                            contentScale = ContentScale.Crop,
                                            modifier = Modifier.fillMaxSize()
                                        )
                                        // Title label at bottom
                                        Box(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .align(Alignment.BottomCenter)
                                                .background(Color.Black.copy(alpha = 0.60f))
                                                .padding(horizontal = 4.dp, vertical = 2.dp)
                                        ) {
                                            Text(
                                                text = "${sticker.previewEmoji} ${sticker.title}",
                                                color = Color.White,
                                                fontSize = 10.sp,
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
                }

                EmojiSubMode.EMOTICON -> {
                    if (filteredEmoticons.isEmpty()) {
                        SearchEmptyState(
                            query = searchQuery,
                            textColor = textColor,
                            surfaceColor = surfaceColor,
                            accentColor = accentColor,
                            onSuggestionClick = { searchQuery = it }
                        )
                    } else {
                        LazyVerticalGrid(
                            columns = GridCells.Fixed(3),
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(horizontal = 4.dp, vertical = 2.dp),
                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                            verticalArrangement = Arrangement.spacedBy(6.dp),
                            contentPadding = PaddingValues(vertical = 4.dp)
                        ) {
                            items(filteredEmoticons, key = { it }) { emoticon ->
                                Box(
                                    modifier = Modifier
                                        .height(44.dp)
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(surfaceColor.copy(alpha = 0.65f))
                                        .clickable { onSelectRecentEmoticon(emoticon) }
                                        .padding(horizontal = 6.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = emoticon,
                                        color = textColor,
                                        fontSize = 13.sp,
                                        fontWeight = FontWeight.Medium,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }

        // BUILT-IN SEARCH KEYBOARD (Rendered when in search mode to enable typing)
        if (isSearchActive && showSearchKeyboard) {
            Divider(
                color = textColor.copy(alpha = 0.1f),
                thickness = 0.8.dp,
                modifier = Modifier.padding(vertical = 2.dp)
            )

            SearchSoftKeyboard(
                isNumeric = isNumericSearchKeyboard,
                textColor = textColor,
                surfaceColor = surfaceColor,
                accentColor = accentColor,
                onChar = { char -> searchQuery += char },
                onBackspace = {
                    if (searchQuery.isNotEmpty()) {
                        searchQuery = searchQuery.dropLast(1)
                    }
                },
                onSpace = { searchQuery += " " },
                onToggleNumeric = { isNumericSearchKeyboard = !isNumericSearchKeyboard },
                onDone = { showSearchKeyboard = false }
            )
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

@Composable
private fun SearchEmptyState(
    query: String,
    textColor: Color,
    surfaceColor: Color,
    accentColor: Color,
    onSuggestionClick: (String) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            imageVector = Icons.Default.Search,
            contentDescription = null,
            tint = textColor.copy(alpha = 0.35f),
            modifier = Modifier.size(36.dp)
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = if (query.isNotBlank()) "No results found for \"$query\"" else "No items found",
            color = textColor.copy(alpha = 0.7f),
            fontSize = 13.sp,
            fontWeight = FontWeight.Medium
        )
        Spacer(modifier = Modifier.height(10.dp))
        Text(
            text = "Try popular searches:",
            color = textColor.copy(alpha = 0.5f),
            fontSize = 11.5.sp
        )
        Spacer(modifier = Modifier.height(6.dp))
        Row(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            listOf("Dance", "Cat", "Love", "LOL").forEach { suggestion ->
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(10.dp))
                        .background(surfaceColor)
                        .clickable { onSuggestionClick(suggestion.lowercase()) }
                        .padding(horizontal = 10.dp, vertical = 4.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = suggestion,
                        color = accentColor,
                        fontSize = 11.5.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }
        }
    }
}

@Composable
private fun SearchSoftKeyboard(
    isNumeric: Boolean,
    textColor: Color,
    surfaceColor: Color,
    accentColor: Color,
    onChar: (String) -> Unit,
    onBackspace: () -> Unit,
    onSpace: () -> Unit,
    onToggleNumeric: () -> Unit,
    onDone: () -> Unit
) {
    val row1 = if (isNumeric) listOf("1", "2", "3", "4", "5", "6", "7", "8", "9", "0")
               else listOf("q", "w", "e", "r", "t", "y", "u", "i", "o", "p")
    val row2 = if (isNumeric) listOf("@", "#", "$", "%", "&", "-", "+", "(", ")", "/")
               else listOf("a", "s", "d", "f", "g", "h", "j", "k", "l")
    val row3 = if (isNumeric) listOf("*", "\"", "'", ":", ";", "!", "?")
               else listOf("z", "x", "c", "v", "b", "n", "m")

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 2.dp, vertical = 2.dp),
        verticalArrangement = Arrangement.spacedBy(3.dp)
    ) {
        // Row 1
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(3.dp)
        ) {
            row1.forEach { char ->
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .height(34.dp)
                        .clip(RoundedCornerShape(6.dp))
                        .background(surfaceColor)
                        .clickable { onChar(char) },
                    contentAlignment = Alignment.Center
                ) {
                    Text(text = char, color = textColor, fontSize = 14.sp, fontWeight = FontWeight.Medium)
                }
            }
        }

        // Row 2
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp),
            horizontalArrangement = Arrangement.spacedBy(3.dp)
        ) {
            row2.forEach { char ->
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .height(34.dp)
                        .clip(RoundedCornerShape(6.dp))
                        .background(surfaceColor)
                        .clickable { onChar(char) },
                    contentAlignment = Alignment.Center
                ) {
                    Text(text = char, color = textColor, fontSize = 14.sp, fontWeight = FontWeight.Medium)
                }
            }
        }

        // Row 3
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(3.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Numeric Toggle
            Box(
                modifier = Modifier
                    .weight(1.3f)
                    .height(34.dp)
                    .clip(RoundedCornerShape(6.dp))
                    .background(surfaceColor.copy(alpha = 0.8f))
                    .clickable { onToggleNumeric() },
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = if (isNumeric) "ABC" else "?123",
                    color = textColor,
                    fontSize = 11.5.sp,
                    fontWeight = FontWeight.Bold
                )
            }

            row3.forEach { char ->
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .height(34.dp)
                        .clip(RoundedCornerShape(6.dp))
                        .background(surfaceColor)
                        .clickable { onChar(char) },
                    contentAlignment = Alignment.Center
                ) {
                    Text(text = char, color = textColor, fontSize = 14.sp, fontWeight = FontWeight.Medium)
                }
            }

            // Backspace Key
            Box(
                modifier = Modifier
                    .weight(1.3f)
                    .height(34.dp)
                    .clip(RoundedCornerShape(6.dp))
                    .background(surfaceColor.copy(alpha = 0.8f))
                    .clickable { onBackspace() },
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.Backspace,
                    contentDescription = "Backspace",
                    tint = textColor,
                    modifier = Modifier.size(17.dp)
                )
            }
        }

        // Row 4: Space and Done
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(6.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .weight(1f)
                    .height(34.dp)
                    .clip(RoundedCornerShape(6.dp))
                    .background(surfaceColor)
                    .clickable { onSpace() },
                contentAlignment = Alignment.Center
            ) {
                Text(text = "Space", color = textColor.copy(alpha = 0.7f), fontSize = 12.sp)
            }

            Box(
                modifier = Modifier
                    .width(72.dp)
                    .height(34.dp)
                    .clip(RoundedCornerShape(6.dp))
                    .background(accentColor)
                    .clickable { onDone() },
                contentAlignment = Alignment.Center
            ) {
                Text(text = "Done", color = Color.White, fontSize = 12.5.sp, fontWeight = FontWeight.Bold)
            }
        }
    }
}
