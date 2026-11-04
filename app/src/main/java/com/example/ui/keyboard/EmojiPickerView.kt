package com.example.ui.keyboard

import android.content.Context
import android.view.HapticFeedbackConstants
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
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
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.GridOn
import androidx.compose.material.icons.filled.Keyboard
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.outlined.Mood
import androidx.compose.material.icons.outlined.Schedule
import androidx.compose.material.icons.outlined.StickyNote2
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.data.KeyboardThemeOption
import com.example.data.OnlineMediaRepository
import kotlinx.coroutines.delay

enum class EmojiSubMode {
    EMOJI,
    GIF,
    STICKER,
    KITCHEN,
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

    // NOTE: NEVER call BackHandler here directly! In an InputMethodService window,
    // LocalOnBackPressedDispatcherOwner is NULL which causes an instant crash in other apps.
    // Back navigation is handled by the in-view arrow and ABC buttons.

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
        listOf(
            "🔥 Trending", "😂 LOL", "💃 Dance", "❤️ Love", "😢 Sad", "👏 Claps",
            "🐱 Cat", "🐶 Dog", "☕ Coffee", "🎮 Gaming", "🎉 Party", "👍 Yes",
            "😱 Wow", "😴 Sleepy", "🍕 Food", "✨ Meme"
        )
    }

    // ONLINE LIVE MEDIA STATE (Fetched from internet via OnlineMediaRepository)
    var onlineGifs by remember { mutableStateOf<List<GifItem>>(MediaCatalog.allGifs) }
    var isFetchingGifs by remember { mutableStateOf(false) }

    var onlineStickers by remember { mutableStateOf<List<StickerItem>>(MediaCatalog.allStickers) }
    var isFetchingStickers by remember { mutableStateOf(false) }

    // Live internet fetch for GIFs
    LaunchedEffect(subMode, selectedGifCategoryIndex, searchQuery) {
        if (subMode == EmojiSubMode.GIF) {
            isFetchingGifs = true
            // Debounce slightly if user is typing
            if (searchQuery.isNotBlank()) delay(250)

            val categoryTitle = MediaCatalog.gifCategories.getOrNull(selectedGifCategoryIndex)?.title
            val targetQuery = if (searchQuery.isNotBlank()) {
                searchQuery
            } else if (categoryTitle != null && !categoryTitle.equals("Trending", ignoreCase = true)) {
                categoryTitle
            } else {
                null
            }

            val fetched = OnlineMediaRepository.fetchGifs(query = targetQuery)
            if (fetched.isNotEmpty()) {
                onlineGifs = fetched
            }
            isFetchingGifs = false
        }
    }

    // Live internet fetch for Stickers
    LaunchedEffect(subMode, selectedStickerCategoryIndex, searchQuery) {
        if (subMode == EmojiSubMode.STICKER) {
            isFetchingStickers = true
            // Debounce slightly if user is typing
            if (searchQuery.isNotBlank()) delay(250)

            val categoryTitle = MediaCatalog.stickerCategories.getOrNull(selectedStickerCategoryIndex)?.title
            val targetQuery = if (searchQuery.isNotBlank()) {
                searchQuery
            } else if (categoryTitle != null && !categoryTitle.equals("Trending", ignoreCase = true)) {
                categoryTitle
            } else {
                null
            }

            val fetched = OnlineMediaRepository.fetchStickers(query = targetQuery)
            if (fetched.isNotEmpty()) {
                onlineStickers = fetched
            }
            isFetchingStickers = false
        }
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
                    .height(42.dp)
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

                // Sleek Search Query Display Capsule
                Row(
                    modifier = Modifier
                        .weight(1f)
                        .height(36.dp)
                        .clip(RoundedCornerShape(18.dp))
                        .background(surfaceColor)
                        .border(
                            width = 0.8.dp,
                            color = if (searchQuery.isNotEmpty()) accentColor.copy(alpha = 0.6f) else textColor.copy(alpha = 0.14f),
                            shape = RoundedCornerShape(18.dp)
                        )
                        .clickable { showSearchKeyboard = true }
                        .padding(horizontal = 10.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.Search,
                        contentDescription = null,
                        tint = if (searchQuery.isNotEmpty()) accentColor else textColor.copy(alpha = 0.45f),
                        modifier = Modifier.size(17.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Box(
                        modifier = Modifier.weight(1f),
                        contentAlignment = Alignment.CenterStart
                    ) {
                        if (searchQuery.isEmpty()) {
                            Text(
                                text = when (subMode) {
                                    EmojiSubMode.EMOJI -> "Search emojis (e.g. fire, smile)..."
                                    EmojiSubMode.GIF -> "Search live GIFs online..."
                                    EmojiSubMode.STICKER -> "Search live stickers online..."
                                    EmojiSubMode.KITCHEN -> "Mix emojis in kitchen..."
                                    EmojiSubMode.EMOTICON -> "Search emoticons..."
                                },
                                color = textColor.copy(alpha = 0.45f),
                                fontSize = 12.sp,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        } else {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = searchQuery,
                                    color = textColor,
                                    fontSize = 13.5.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                                Text(
                                    text = "|",
                                    color = accentColor,
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier.padding(start = 1.dp)
                                )
                            }
                        }
                    }

                    if (searchQuery.isNotEmpty()) {
                        Box(
                            modifier = Modifier
                                .size(20.dp)
                                .clip(CircleShape)
                                .background(textColor.copy(alpha = 0.14f))
                                .clickable { searchQuery = "" },
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Close,
                                contentDescription = "Clear",
                                tint = textColor,
                                modifier = Modifier.size(13.dp)
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.width(4.dp))

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
                    .height(34.dp)
                    .horizontalScroll(rememberScrollState())
                    .padding(horizontal = 4.dp, vertical = 2.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                quickSearchChips.forEach { chip ->
                    val cleanKeyword = chip.substringAfter(" ").lowercase()
                    val isSelected = searchQuery.equals(cleanKeyword, ignoreCase = true)
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
                                searchQuery = cleanKeyword
                            }
                            .padding(horizontal = 10.dp, vertical = 4.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = chip,
                            color = if (isSelected) indicatorColor else textColor.copy(alpha = 0.85f),
                            fontSize = 11.5.sp,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
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

                    EmojiSubMode.KITCHEN -> {
                        // Kitchen handles its own mixing controls internally
                    }
                }
            }
        }

        // Live Fetching indicator bar
        AnimatedVisibility(
            visible = (subMode == EmojiSubMode.GIF && isFetchingGifs) || (subMode == EmojiSubMode.STICKER && isFetchingStickers),
            enter = fadeIn(),
            exit = fadeOut()
        ) {
            LinearProgressIndicator(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(2.dp),
                color = accentColor,
                trackColor = surfaceColor
            )
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
                    if (onlineGifs.isEmpty() && !isFetchingGifs) {
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
                            items(onlineGifs, key = { it.id }) { gif ->
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
                                            model = gif.previewUrl.ifEmpty { gif.url },
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
                    if (onlineStickers.isEmpty() && !isFetchingStickers) {
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
                            items(onlineStickers, key = { it.id }) { sticker ->
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
                                            onMediaSelected(sticker.url, "image/gif", sticker.title)
                                        }
                                ) {
                                    Box(
                                        modifier = Modifier.fillMaxSize(),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        AsyncImage(
                                            model = sticker.previewUrl.ifEmpty { sticker.url },
                                            contentDescription = sticker.title,
                                            contentScale = ContentScale.Fit,
                                            modifier = Modifier
                                                .fillMaxSize()
                                                .padding(6.dp)
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
                                                text = sticker.title,
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

                EmojiSubMode.KITCHEN -> {
                    EmojiKitchenView(
                        theme = theme,
                        onEmojiSelected = onEmojiSelected,
                        onMediaSelected = onMediaSelected,
                        modifier = Modifier.fillMaxSize()
                    )
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
                theme = theme,
                isNumeric = isNumericSearchKeyboard,
                textColor = textColor,
                surfaceColor = surfaceColor,
                accentColor = accentColor,
                keyColor = Color(theme.keyHex),
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

                // Emoji Kitchen / Mix Tab
                Column(
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .clickable {
                            subMode = EmojiSubMode.KITCHEN
                            isSearchActive = false
                        }
                        .padding(horizontal = 6.dp, vertical = 2.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Icon(
                        imageVector = Icons.Default.AutoAwesome,
                        contentDescription = "Emoji Kitchen Mixer",
                        tint = if (subMode == EmojiSubMode.KITCHEN) indicatorColor else textColor.copy(alpha = 0.45f),
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    if (subMode == EmojiSubMode.KITCHEN) {
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
internal fun SearchSoftKeyboard(
    theme: KeyboardThemeOption,
    isNumeric: Boolean,
    textColor: Color,
    surfaceColor: Color,
    accentColor: Color,
    keyColor: Color,
    onChar: (String) -> Unit,
    onBackspace: () -> Unit,
    onSpace: () -> Unit,
    onToggleNumeric: () -> Unit,
    onDone: () -> Unit
) {
    var isShifted by remember { mutableStateOf(false) }

    val row1 = if (isNumeric) listOf("1", "2", "3", "4", "5", "6", "7", "8", "9", "0")
               else listOf("q", "w", "e", "r", "t", "y", "u", "i", "o", "p")
    val row2 = if (isNumeric) listOf("@", "#", "$", "%", "&", "-", "+", "(", ")", "/")
               else listOf("a", "s", "d", "f", "g", "h", "j", "k", "l")
    val row3 = if (isNumeric) listOf("*", "\"", "'", ":", ";", "!", "?")
               else listOf("z", "x", "c", "v", "b", "n", "m")

    val keyBorder = if (theme == KeyboardThemeOption.LIGHT) {
        androidx.compose.foundation.BorderStroke(0.6.dp, Color(0xFFCBD5E1))
    } else {
        androidx.compose.foundation.BorderStroke(0.5.dp, Color.White.copy(alpha = 0.08f))
    }
    val funcKeyBorder = if (theme == KeyboardThemeOption.LIGHT) {
        androidx.compose.foundation.BorderStroke(0.6.dp, Color(0xFF94A3B8).copy(alpha = 0.35f))
    } else {
        androidx.compose.foundation.BorderStroke(0.5.dp, Color.White.copy(alpha = 0.06f))
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 3.dp, vertical = 3.dp),
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        // ROW 1
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(3.dp)
        ) {
            row1.forEach { char ->
                val displayChar = if (!isNumeric && isShifted) char.uppercase() else char
                SearchKeyCap(
                    modifier = Modifier.weight(1f),
                    backgroundColor = keyColor,
                    contentColor = textColor,
                    border = keyBorder,
                    elevation = 1.2.dp,
                    onClick = {
                        onChar(displayChar)
                        if (isShifted) isShifted = false
                    }
                ) {
                    Text(
                        text = displayChar,
                        color = textColor,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Medium
                    )
                }
            }
        }

        // ROW 2 (indented slightly on letters for realistic keyboard geometry)
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = if (isNumeric) 0.dp else 14.dp),
            horizontalArrangement = Arrangement.spacedBy(3.dp)
        ) {
            row2.forEach { char ->
                val displayChar = if (!isNumeric && isShifted) char.uppercase() else char
                SearchKeyCap(
                    modifier = Modifier.weight(1f),
                    backgroundColor = keyColor,
                    contentColor = textColor,
                    border = keyBorder,
                    elevation = 1.2.dp,
                    onClick = {
                        onChar(displayChar)
                        if (isShifted) isShifted = false
                    }
                ) {
                    Text(
                        text = displayChar,
                        color = textColor,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Medium
                    )
                }
            }
        }

        // ROW 3 (Shift / Symbols on Left, Letters in Center, Backspace on Right)
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(3.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            if (isNumeric) {
                // Secondary symbol helper key
                SearchKeyCap(
                    modifier = Modifier.weight(1.2f),
                    backgroundColor = surfaceColor,
                    contentColor = textColor,
                    border = funcKeyBorder,
                    elevation = 1.dp,
                    onClick = { onChar("=") }
                ) {
                    Text(
                        text = "=",
                        color = textColor,
                        fontSize = 14.5.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            } else {
                // Shift key with active highlight indicator
                SearchKeyCap(
                    modifier = Modifier.weight(1.35f),
                    backgroundColor = if (isShifted) accentColor else surfaceColor,
                    contentColor = if (isShifted) Color.White else textColor,
                    border = if (isShifted) null else funcKeyBorder,
                    elevation = 1.dp,
                    onClick = { isShifted = !isShifted }
                ) {
                    Icon(
                        imageVector = Icons.Default.ArrowUpward,
                        contentDescription = "Shift",
                        tint = if (isShifted) Color.White else textColor,
                        modifier = Modifier.size(19.dp)
                    )
                }
            }

            row3.forEach { char ->
                val displayChar = if (!isNumeric && isShifted) char.uppercase() else char
                SearchKeyCap(
                    modifier = Modifier.weight(1f),
                    backgroundColor = keyColor,
                    contentColor = textColor,
                    border = keyBorder,
                    elevation = 1.2.dp,
                    onClick = {
                        onChar(displayChar)
                        if (isShifted) isShifted = false
                    }
                ) {
                    Text(
                        text = displayChar,
                        color = textColor,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Medium
                    )
                }
            }

            // Backspace Key
            SearchKeyCap(
                modifier = Modifier.weight(1.35f),
                backgroundColor = surfaceColor,
                contentColor = textColor,
                border = funcKeyBorder,
                elevation = 1.dp,
                onClick = onBackspace
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.Backspace,
                    contentDescription = "Backspace",
                    tint = textColor,
                    modifier = Modifier.size(19.dp)
                )
            }
        }

        // ROW 4 (?123 / ABC, comma, Space, dot, Search action)
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Numeric Toggle
            SearchKeyCap(
                modifier = Modifier.weight(1.35f),
                backgroundColor = surfaceColor,
                contentColor = textColor,
                border = funcKeyBorder,
                elevation = 1.dp,
                onClick = onToggleNumeric
            ) {
                Text(
                    text = if (isNumeric) "ABC" else "?123",
                    color = textColor,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold
                )
            }

            // Comma Punctuation
            SearchKeyCap(
                modifier = Modifier.weight(1f),
                backgroundColor = keyColor,
                contentColor = textColor,
                border = keyBorder,
                elevation = 1.2.dp,
                onClick = { onChar(",") }
            ) {
                Text(
                    text = ",",
                    color = textColor,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.SemiBold
                )
            }

            // Spacebar
            SearchKeyCap(
                modifier = Modifier.weight(4.0f),
                backgroundColor = keyColor,
                contentColor = textColor.copy(alpha = 0.5f),
                border = keyBorder,
                elevation = 1.2.dp,
                onClick = onSpace
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth(0.55f)
                        .height(3.dp)
                        .clip(RoundedCornerShape(1.5.dp))
                        .background(textColor.copy(alpha = 0.22f))
                )
            }

            // Period Punctuation
            SearchKeyCap(
                modifier = Modifier.weight(1f),
                backgroundColor = keyColor,
                contentColor = textColor,
                border = keyBorder,
                elevation = 1.2.dp,
                onClick = { onChar(".") }
            ) {
                Text(
                    text = ".",
                    color = textColor,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold
                )
            }

            // Search Action Button
            SearchKeyCap(
                modifier = Modifier.weight(1.6f),
                backgroundColor = accentColor,
                contentColor = Color.White,
                border = null,
                elevation = 2.dp,
                onClick = onDone
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Search,
                        contentDescription = "Search",
                        tint = Color.White,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(3.dp))
                    Text(
                        text = "Go",
                        color = Color.White,
                        fontSize = 12.5.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}

@Composable
private fun SearchKeyCap(
    modifier: Modifier = Modifier,
    backgroundColor: Color,
    contentColor: Color,
    border: androidx.compose.foundation.BorderStroke? = null,
    elevation: androidx.compose.ui.unit.Dp = 1.2.dp,
    onClick: () -> Unit,
    content: @Composable () -> Unit
) {
    val view = LocalView.current
    Surface(
        modifier = modifier
            .height(39.dp)
            .clip(RoundedCornerShape(7.dp))
            .clickable {
                try {
                    view.performHapticFeedback(HapticFeedbackConstants.KEYBOARD_TAP)
                } catch (_: Exception) {}
                onClick()
            },
        shape = RoundedCornerShape(7.dp),
        color = backgroundColor,
        contentColor = contentColor,
        shadowElevation = elevation,
        border = border
    ) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            content()
        }
    }
}
