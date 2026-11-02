package com.example.ui.screens

import android.content.Context
import android.content.Intent
import android.provider.Settings
import android.view.inputmethod.InputMethodManager
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.SwapHoriz
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.data.ClipboardRepository
import com.example.data.KeyboardPreferences
import com.example.data.SnippetsRepository
import com.example.ui.keyboard.KeyNovaKeyboardView
import com.example.ui.keyboard.KeyboardActionTarget

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PlaygroundScreen(
    keyboardPreferences: KeyboardPreferences,
    clipboardRepository: ClipboardRepository,
    snippetsRepository: SnippetsRepository,
    onNavigateToSettings: () -> Unit,
    onNavigateToToolbarSettings: () -> Unit = onNavigateToSettings,
    onNavigateToDestination: (SettingsDestination) -> Unit = {}
) {
    val context = LocalContext.current
    val clipboardManager = LocalClipboardManager.current
    val imm = remember { context.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager }

    val prefRev = keyboardPreferences.preferenceRevision.intValue
    val isFloatingMode = remember(keyboardPreferences.isFloatingMode, prefRev) {
        keyboardPreferences.isFloatingMode
    }

    var testText by remember { mutableStateOf("Hello, KeyNova!") }
    var cursorPosition by remember { mutableIntStateOf(testText.length) }

    val lifecycleOwner = androidx.lifecycle.compose.LocalLifecycleOwner.current

    // Check if KeyNova is enabled and default in system
    var isEnabledInSystem by remember { mutableStateOf(false) }
    var isDefaultSelected by remember { mutableStateOf(false) }

    fun checkImeStatus() {
        val enabledMethods = imm.enabledInputMethodList
        val packageName = context.packageName
        isEnabledInSystem = enabledMethods.any { it.packageName == packageName }

        val defaultIme = Settings.Secure.getString(context.contentResolver, Settings.Secure.DEFAULT_INPUT_METHOD) ?: ""
        isDefaultSelected = defaultIme.contains(packageName)
    }

    DisposableEffect(lifecycleOwner) {
        val observer = androidx.lifecycle.LifecycleEventObserver { _, event ->
            if (event == androidx.lifecycle.Lifecycle.Event.ON_RESUME) {
                checkImeStatus()
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
        }
    }

    LaunchedEffect(Unit) {
        checkImeStatus()
    }

    val actionTarget = remember {
        object : KeyboardActionTarget {
            override fun onCommitText(text: String) {
                val safePos = cursorPosition.coerceIn(0, testText.length)
                val before = testText.substring(0, safePos)
                val after = testText.substring(safePos)
                testText = before + text + after
                cursorPosition = safePos + text.length
            }

            override fun onDeleteBackward() {
                val safePos = cursorPosition.coerceIn(0, testText.length)
                if (safePos > 0) {
                    val before = testText.substring(0, safePos - 1)
                    val after = testText.substring(safePos)
                    testText = before + after
                    cursorPosition = safePos - 1
                }
            }

            override fun onDeleteWordBackward() {
                val safePos = cursorPosition.coerceIn(0, testText.length)
                if (safePos > 0) {
                    var pos = safePos
                    while (pos > 0 && testText[pos - 1].isWhitespace()) pos--
                    while (pos > 0 && !testText[pos - 1].isWhitespace()) pos--
                    val before = testText.substring(0, pos)
                    val after = testText.substring(safePos)
                    testText = before + after
                    cursorPosition = pos
                }
            }

            override fun onEnterAction() {
                onCommitText("\n")
            }

            override fun onDoubleSpacePeriod() {
                val safePos = cursorPosition.coerceIn(0, testText.length)
                if (safePos > 0 && testText.getOrNull(safePos - 1) == ' ') {
                    onDeleteBackward()
                    onCommitText(". ")
                } else {
                    onCommitText(". ")
                }
            }

            override fun onVoiceTyping() {
                try {
                    val intent = android.content.Intent(android.speech.RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
                        putExtra(android.speech.RecognizerIntent.EXTRA_LANGUAGE_MODEL, android.speech.RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
                        putExtra(android.speech.RecognizerIntent.EXTRA_PROMPT, "Speak now to test voice typing...")
                    }
                    context.startActivity(intent)
                } catch (_: Exception) {}
            }

            override fun onMoveCursor(offset: Int) {
                cursorPosition = (cursorPosition + offset).coerceIn(0, testText.length)
            }

            override fun onMoveCursorUp() {
                val lines = testText.split("\n")
                // Jump one line up if multiline
                val safePos = cursorPosition.coerceIn(0, testText.length)
                val prevNewline = testText.lastIndexOf('\n', (safePos - 1).coerceAtLeast(0))
                if (prevNewline >= 0) {
                    val prevPrevNewline = testText.lastIndexOf('\n', (prevNewline - 1).coerceAtLeast(0))
                    val col = safePos - prevNewline
                    cursorPosition = ((prevPrevNewline + 1) + col).coerceIn(prevPrevNewline + 1, prevNewline)
                } else {
                    cursorPosition = 0
                }
            }

            override fun onMoveCursorDown() {
                val safePos = cursorPosition.coerceIn(0, testText.length)
                val nextNewline = testText.indexOf('\n', safePos)
                if (nextNewline >= 0) {
                    val curLineStart = testText.lastIndexOf('\n', (safePos - 1).coerceAtLeast(0)).let { if (it < 0) 0 else it + 1 }
                    val col = safePos - curLineStart
                    val afterNext = testText.indexOf('\n', nextNewline + 1).let { if (it < 0) testText.length else it }
                    cursorPosition = (nextNewline + 1 + col).coerceIn(nextNewline + 1, afterNext)
                } else {
                    cursorPosition = testText.length
                }
            }

            override fun onMoveCursorToStart() {
                cursorPosition = 0
            }

            override fun onMoveCursorToEnd() {
                cursorPosition = testText.length
            }

            override fun onSelectAll() {
                clipboardManager.setText(AnnotatedString(testText))
            }

            override fun onCopy() {
                if (testText.isNotEmpty()) {
                    clipboardManager.setText(AnnotatedString(testText))
                    clipboardRepository.addItem(testText)
                }
            }

            override fun onPaste() {
                val clip = clipboardManager.getText()?.text
                if (!clip.isNullOrEmpty()) {
                    onCommitText(clip)
                }
            }

            override fun onClearAll() {
                testText = ""
                cursorPosition = 0
            }

            override fun onCycleCase() {
                if (testText.isNotEmpty()) {
                    testText = when {
                        testText.all { it.isUpperCase() || !it.isLetter() } -> testText.lowercase()
                        testText.all { it.isLowerCase() || !it.isLetter() } -> testText.replaceFirstChar { it.uppercase() }
                        else -> testText.uppercase()
                    }
                    cursorPosition = cursorPosition.coerceIn(0, testText.length)
                }
            }
        }
    }

    var showCopiedFeedback by remember { mutableStateOf(false) }

    LaunchedEffect(showCopiedFeedback) {
        if (showCopiedFeedback) {
            kotlinx.coroutines.delay(1800)
            showCopiedFeedback = false
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .clip(RoundedCornerShape(10.dp))
                                .background(
                                    Brush.linearGradient(
                                        listOf(Color(0xFF2563EB), Color(0xFF0284C7))
                                    )
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Outlined.Keyboard,
                                contentDescription = null,
                                tint = Color.White,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text(
                                text = "KeyNova",
                                fontWeight = FontWeight.ExtraBold,
                                fontSize = 18.sp,
                                color = Color(0xFF0F172A),
                                maxLines = 1,
                                softWrap = false
                            )
                            Text(
                                text = if (isDefaultSelected) "● Active in all apps" else if (isEnabledInSystem) "● Enabled (tap to select)" else "○ Setup needed",
                                fontSize = 11.sp,
                                color = if (isDefaultSelected) Color(0xFF10B981) else if (isEnabledInSystem) Color(0xFF2563EB) else Color(0xFFF59E0B),
                                fontWeight = FontWeight.Medium,
                                maxLines = 1
                            )
                        }
                    }
                },
                actions = {
                    if (!isDefaultSelected) {
                        Surface(
                            onClick = {
                                if (!isEnabledInSystem) {
                                    val intent = Intent(Settings.ACTION_INPUT_METHOD_SETTINGS)
                                    context.startActivity(intent)
                                } else {
                                    imm.showInputMethodPicker()
                                }
                            },
                            shape = RoundedCornerShape(14.dp),
                            color = Color(0xFFEFF6FF),
                            border = BorderStroke(0.8.dp, Color(0xFF2563EB).copy(alpha = 0.4f)),
                            modifier = Modifier.padding(end = 4.dp)
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 9.dp, vertical = 5.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.Outlined.SwapHoriz,
                                    contentDescription = null,
                                    tint = Color(0xFF2563EB),
                                    modifier = Modifier.size(15.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = if (!isEnabledInSystem) "Enable" else "Select IME",
                                    color = Color(0xFF2563EB),
                                    fontSize = 11.5.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    } else {
                        IconButton(onClick = { imm.showInputMethodPicker() }) {
                            Icon(
                                imageVector = Icons.Outlined.SwapHoriz,
                                contentDescription = "Switch Input Method",
                                tint = Color(0xFF64748B),
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }

                    IconButton(onClick = onNavigateToSettings) {
                        Icon(
                            imageVector = Icons.Default.Settings,
                            contentDescription = stringResource(R.string.action_settings),
                            tint = Color(0xFF0F172A)
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color(0xFFF8FAFC))
            )
        },
        containerColor = Color(0xFFF8FAFC)
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 16.dp, vertical = 6.dp)
                    .padding(bottom = if (isFloatingMode) 80.dp else 290.dp)
            ) {
                // QUICK FEATURE SHORTCUTS HUB
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState())
                        .padding(bottom = 12.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Themes
                    Surface(
                        onClick = { onNavigateToDestination(SettingsDestination.THEMES) },
                        shape = RoundedCornerShape(12.dp),
                        color = Color.White,
                        border = BorderStroke(0.8.dp, Color(0xFFE2E8F0)),
                        shadowElevation = 0.5.dp
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 7.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Outlined.ColorLens,
                                contentDescription = null,
                                tint = Color(0xFF7C3AED),
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Themes", fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = Color(0xFF1E293B))
                        }
                    }

                    // Languages
                    Surface(
                        onClick = { onNavigateToDestination(SettingsDestination.LANGUAGES) },
                        shape = RoundedCornerShape(12.dp),
                        color = Color.White,
                        border = BorderStroke(0.8.dp, Color(0xFFE2E8F0)),
                        shadowElevation = 0.5.dp
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 7.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Outlined.Language,
                                contentDescription = null,
                                tint = Color(0xFF2563EB),
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Languages", fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = Color(0xFF1E293B))
                        }
                    }

                    // Clipboard
                    Surface(
                        onClick = { onNavigateToDestination(SettingsDestination.CLIPBOARD) },
                        shape = RoundedCornerShape(12.dp),
                        color = Color.White,
                        border = BorderStroke(0.8.dp, Color(0xFFE2E8F0)),
                        shadowElevation = 0.5.dp
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 7.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Outlined.Assignment,
                                contentDescription = null,
                                tint = Color(0xFF059669),
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Clipboard", fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = Color(0xFF1E293B))
                        }
                    }

                    // Toolbar
                    Surface(
                        onClick = { onNavigateToToolbarSettings() },
                        shape = RoundedCornerShape(12.dp),
                        color = Color.White,
                        border = BorderStroke(0.8.dp, Color(0xFFE2E8F0)),
                        shadowElevation = 0.5.dp
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 7.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Outlined.Widgets,
                                contentDescription = null,
                                tint = Color(0xFFEA580C),
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Toolbar", fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = Color(0xFF1E293B))
                        }
                    }

                    // Gaming & Code
                    Surface(
                        onClick = { onNavigateToDestination(SettingsDestination.GAMING_CODING) },
                        shape = RoundedCornerShape(12.dp),
                        color = Color.White,
                        border = BorderStroke(0.8.dp, Color(0xFFE2E8F0)),
                        shadowElevation = 0.5.dp
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 7.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Outlined.SportsEsports,
                                contentDescription = null,
                                tint = Color(0xFFD97706),
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Gaming/Code", fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = Color(0xFF1E293B))
                        }
                    }
                }

                // SYSTEM STATUS OR GUIDED SETUP BANNER
                if (isDefaultSelected) {
                    // Compact Status Pill
                    Surface(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 12.dp),
                        shape = RoundedCornerShape(12.dp),
                        color = Color(0xFFECFDF5),
                        border = BorderStroke(0.8.dp, Color(0xFF10B981).copy(alpha = 0.35f))
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 9.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Filled.CheckCircle,
                                contentDescription = null,
                                tint = Color(0xFF10B981),
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "KeyNova is active & ready in WhatsApp, Chrome & all apps",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = Color(0xFF065F46),
                                modifier = Modifier.weight(1f)
                            )
                            Text(
                                text = "Switch",
                                color = Color(0xFF2563EB),
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier
                                    .clickable { imm.showInputMethodPicker() }
                                    .padding(horizontal = 6.dp, vertical = 2.dp)
                            )
                        }
                    }
                } else {
                    // Guided Setup Card
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 12.dp),
                        colors = CardDefaults.cardColors(containerColor = Color.White),
                        shape = RoundedCornerShape(14.dp),
                        border = BorderStroke(0.8.dp, Color(0xFFE2E8F0))
                    ) {
                        Column(modifier = Modifier.padding(14.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "System Keyboard Setup",
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFF0F172A)
                                )
                                Text(
                                    text = if (isEnabledInSystem) "Step 2 of 2" else "Step 1 of 2",
                                    fontSize = 11.5.sp,
                                    color = Color(0xFF2563EB),
                                    fontWeight = FontWeight.Bold
                                )
                            }

                            Spacer(modifier = Modifier.height(10.dp))

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Button(
                                    onClick = {
                                        val intent = Intent(Settings.ACTION_INPUT_METHOD_SETTINGS)
                                        context.startActivity(intent)
                                    },
                                    modifier = Modifier
                                        .weight(1f)
                                        .height(38.dp),
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = if (isEnabledInSystem) Color(0xFF10B981) else Color(0xFF2563EB)
                                    ),
                                    shape = RoundedCornerShape(8.dp),
                                    contentPadding = PaddingValues(horizontal = 8.dp)
                                ) {
                                    Text(
                                        text = if (isEnabledInSystem) "✓ 1. Enabled" else "1. Enable in Settings",
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.SemiBold
                                    )
                                }

                                Button(
                                    onClick = { imm.showInputMethodPicker() },
                                    modifier = Modifier
                                        .weight(1f)
                                        .height(38.dp),
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = if (isDefaultSelected) Color(0xFF10B981) else Color(0xFF2563EB)
                                    ),
                                    shape = RoundedCornerShape(8.dp),
                                    contentPadding = PaddingValues(horizontal = 8.dp)
                                ) {
                                    Text(
                                        text = if (isDefaultSelected) "✓ 2. Active" else "2. Select KeyNova",
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.SemiBold
                                    )
                                }
                            }
                        }
                    }
                }

                // TYPING SANDBOX CARD
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    border = BorderStroke(0.8.dp, Color(0xFFE2E8F0))
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Outlined.TextFormat,
                                    contentDescription = null,
                                    tint = Color(0xFF2563EB),
                                    modifier = Modifier.size(17.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "Typing Sandbox",
                                    fontSize = 13.5.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFF0F172A)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "${testText.length} chars",
                                    fontSize = 11.sp,
                                    color = Color(0xFF64748B),
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(6.dp))
                                        .background(Color(0xFFF1F5F9))
                                        .padding(horizontal = 6.dp, vertical = 2.dp)
                                )
                            }

                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                if (showCopiedFeedback) {
                                    Text(
                                        text = "✓ Copied!",
                                        fontSize = 11.5.sp,
                                        color = Color(0xFF10B981),
                                        fontWeight = FontWeight.Bold,
                                        modifier = Modifier.padding(end = 4.dp)
                                    )
                                } else {
                                    IconButton(
                                        onClick = {
                                            clipboardManager.setText(AnnotatedString(testText))
                                            clipboardRepository.addItem(testText)
                                            showCopiedFeedback = true
                                        },
                                        modifier = Modifier.size(28.dp)
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.ContentCopy,
                                            contentDescription = stringResource(R.string.action_copy),
                                            tint = Color(0xFF64748B),
                                            modifier = Modifier.size(16.dp)
                                        )
                                    }
                                }

                                IconButton(
                                    onClick = {
                                        testText = ""
                                        cursorPosition = 0
                                    },
                                    modifier = Modifier.size(28.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Clear,
                                        contentDescription = stringResource(R.string.action_clear),
                                        tint = Color(0xFF64748B),
                                        modifier = Modifier.size(16.dp)
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        // Interactive Text Area
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .heightIn(min = 68.dp, max = 110.dp)
                                .clip(RoundedCornerShape(10.dp))
                                .background(Color(0xFFF8FAFC))
                                .border(0.8.dp, Color(0xFFE2E8F0), RoundedCornerShape(10.dp))
                                .padding(12.dp)
                        ) {
                            if (testText.isEmpty()) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text(
                                        text = "|",
                                        color = Color(0xFF2563EB),
                                        fontSize = 17.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(
                                        text = "Tap keyboard below to test typing...",
                                        color = Color(0xFF94A3B8),
                                        fontSize = 14.5.sp
                                    )
                                }
                            } else {
                                val safeCursor = cursorPosition.coerceIn(0, testText.length)
                                val annotated = androidx.compose.ui.text.buildAnnotatedString {
                                    append(testText.substring(0, safeCursor))
                                    pushStyle(
                                        androidx.compose.ui.text.SpanStyle(
                                            color = Color(0xFF2563EB),
                                            fontWeight = FontWeight.Black
                                        )
                                    )
                                    append("|")
                                    pop()
                                    append(testText.substring(safeCursor))
                                }
                                Text(
                                    text = annotated,
                                    color = Color(0xFF0F172A),
                                    fontSize = 15.5.sp,
                                    lineHeight = 22.sp
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        // Quick Starter Prompt Chips
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .horizontalScroll(rememberScrollState()),
                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            listOf(
                                "✨ Hello KeyNova!",
                                "🇧🇩 আমি বাংলায় গান গাই",
                                "😂 Emojis & GIFs",
                                "⚡ Fast Typing"
                            ).forEach { prompt ->
                                Surface(
                                    onClick = {
                                        testText = prompt
                                        cursorPosition = prompt.length
                                    },
                                    shape = RoundedCornerShape(10.dp),
                                    color = Color(0xFFF1F5F9)
                                ) {
                                    Text(
                                        text = prompt,
                                        color = Color(0xFF475569),
                                        fontSize = 11.5.sp,
                                        fontWeight = FontWeight.Medium,
                                        modifier = Modifier.padding(horizontal = 9.dp, vertical = 4.dp)
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // Interactive In-App Keyboard
            KeyNovaKeyboardView(
                keyboardPreferences = keyboardPreferences,
                clipboardRepository = clipboardRepository,
                snippetsRepository = snippetsRepository,
                actionTarget = actionTarget,
                onOpenSettings = onNavigateToSettings,
                onOpenToolbarSettings = onNavigateToToolbarSettings,
                modifier = if (isFloatingMode) {
                    Modifier.fillMaxSize()
                } else {
                    Modifier
                        .fillMaxWidth()
                        .align(Alignment.BottomCenter)
                }
            )
        }
    }
}
