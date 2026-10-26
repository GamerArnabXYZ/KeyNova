package com.example.ui.screens

import android.content.Context
import android.content.Intent
import android.provider.Settings
import android.view.inputmethod.InputMethodManager
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.outlined.Check
import androidx.compose.material.icons.outlined.Keyboard
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
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
    onNavigateToToolbarSettings: () -> Unit = onNavigateToSettings
) {
    val context = LocalContext.current
    val clipboardManager = LocalClipboardManager.current
    val imm = remember { context.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager }

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

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(32.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .background(Color(0xFF2563EB)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Outlined.Keyboard,
                                contentDescription = null,
                                tint = Color.White,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Text(
                            text = stringResource(R.string.app_name),
                            fontWeight = FontWeight.Bold,
                            fontSize = 19.sp,
                            color = Color(0xFF0F172A)
                        )
                    }
                },
                actions = {
                    // Setup Status Badge
                    AssistChip(
                        onClick = { checkImeStatus() },
                        label = {
                            Text(
                                text = when {
                                    isDefaultSelected -> stringResource(R.string.playground_status_default)
                                    isEnabledInSystem -> stringResource(R.string.playground_status_enabled)
                                    else -> stringResource(R.string.playground_status_not_default)
                                },
                                fontSize = 11.sp,
                                fontWeight = FontWeight.SemiBold
                            )
                        },
                        leadingIcon = {
                            Icon(
                                imageVector = if (isDefaultSelected) Icons.Filled.CheckCircle else Icons.Outlined.Keyboard,
                                contentDescription = null,
                                tint = when {
                                    isDefaultSelected -> Color(0xFF10B981)
                                    isEnabledInSystem -> Color(0xFF2563EB)
                                    else -> Color(0xFFF59E0B)
                                },
                                modifier = Modifier.size(14.dp)
                            )
                        },
                        colors = AssistChipDefaults.assistChipColors(
                            containerColor = when {
                                isDefaultSelected -> Color(0xFFECFDF5)
                                isEnabledInSystem -> Color(0xFFEFF6FF)
                                else -> Color(0xFFFFFBEB)
                            }
                        ),
                        modifier = Modifier.padding(end = 4.dp)
                    )

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
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Column(
                modifier = Modifier
                    .weight(1f)
                    .padding(horizontal = 16.dp, vertical = 8.dp)
            ) {
                // System Setup Assistance Card if not enabled or user wants to switch
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 12.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    shape = RoundedCornerShape(14.dp),
                    border = CardDefaults.outlinedCardBorder()
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
                                text = when {
                                    isDefaultSelected -> "Ready in all apps"
                                    isEnabledInSystem -> "Step 2 pending"
                                    else -> "Step 1 of 2"
                                },
                                fontSize = 11.sp,
                                color = if (isDefaultSelected) Color(0xFF10B981) else Color(0xFF2563EB),
                                fontWeight = FontWeight.SemiBold
                            )
                        }

                        Spacer(modifier = Modifier.height(8.dp))

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
                                    .height(40.dp),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = if (isEnabledInSystem) Color(0xFF10B981) else Color(0xFF2563EB)
                                ),
                                shape = RoundedCornerShape(8.dp),
                                contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp)
                            ) {
                                Text(
                                    text = if (isEnabledInSystem) "✓ 1. Enabled" else "1. Enable in Settings",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.SemiBold
                                )
                            }

                            Button(
                                onClick = {
                                    imm.showInputMethodPicker()
                                },
                                modifier = Modifier
                                    .weight(1f)
                                    .height(40.dp),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = if (isDefaultSelected) Color(0xFF10B981) else Color(0xFF2563EB)
                                ),
                                shape = RoundedCornerShape(8.dp),
                                contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp)
                            ) {
                                Text(
                                    text = if (isDefaultSelected) "✓ 2. Active Default" else "2. Select KeyNova",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.SemiBold
                                )
                            }
                        }
                    }
                }

                // Interactive Typing Box
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    border = CardDefaults.outlinedCardBorder()
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Live Test Field",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = Color(0xFF64748B)
                            )

                            Row {
                                IconButton(
                                    onClick = {
                                        clipboardManager.setText(AnnotatedString(testText))
                                        clipboardRepository.addItem(testText)
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

                                IconButton(
                                    onClick = { testText = "" },
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

                        Spacer(modifier = Modifier.height(4.dp))

                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .heightIn(min = 60.dp, max = 110.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .background(Color(0xFFF1F5F9))
                                .padding(10.dp)
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
                                        text = stringResource(R.string.playground_input_hint),
                                        color = Color(0xFF94A3B8),
                                        fontSize = 15.sp
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
                                    fontSize = 16.sp
                                )
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
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}
