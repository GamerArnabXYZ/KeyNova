package com.example.ui.screens

import android.content.Context
import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
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
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.outlined.PhotoLibrary
import androidx.compose.material3.*
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.data.CustomThemeConfig
import com.example.data.KeyboardPreferences
import com.example.data.KeyboardThemeOption
import java.io.File
import java.io.FileOutputStream
import kotlin.math.roundToInt

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CustomThemeBuilderScreen(
    keyboardPreferences: KeyboardPreferences,
    onNavigateBack: () -> Unit,
    onThemeSaved: () -> Unit = onNavigateBack
) {
    val context = LocalContext.current
    var themeConfig by remember {
        mutableStateOf(keyboardPreferences.customThemeConfig)
    }

    // Tabs: Background, Keys, Suggestion, Func. Keys
    var activeTab by remember { mutableIntStateOf(0) }
    val tabs = listOf("Background", "Keys", "Suggestion", "Func. Keys")

    // Custom Hex Color Picker Dialog State
    var showColorDialog by remember { mutableStateOf(false) }
    var colorDialogTarget by remember { mutableStateOf("") }
    var inputHexText by remember { mutableStateOf("FFFFFF") }

    // Photo picker launcher for custom background image
    val photoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri: Uri? ->
        uri?.let { selectedUri ->
            try {
                val destFile = File(context.filesDir, "custom_theme_bg_${System.currentTimeMillis()}.jpg")
                context.contentResolver.openInputStream(selectedUri)?.use { input ->
                    FileOutputStream(destFile).use { output ->
                        input.copyTo(output)
                    }
                }
                themeConfig = themeConfig.copy(backgroundImagePath = destFile.absolutePath)
                Toast.makeText(context, "Background image selected", Toast.LENGTH_SHORT).show()
            } catch (e: Exception) {
                Toast.makeText(context, "Failed to load image: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }

    val standardPalette = listOf(
        0xFFFFFFFF, // White
        0xFF121212, // Dark/Black
        0xFFDC2626, // Red
        0xFFE11D48, // Hot Pink
        0xFF6B21A8, // Deep Purple
        0xFF4338CA, // Indigo
        0xFF1E3A8A, // Navy
        0xFF2563EB, // Royal Blue
        0xFF0284C7, // Sky Blue
        0xFF0D9488, // Teal
        0xFF059669, // Emerald Green
        0xFF65A30D, // Lime Green
        0xFFD97706, // Amber
        0xFFEA580C, // Sunset Orange
        0xFF334155, // Slate
        0xFF1E293B  // Charcoal
    )

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "New Theme",
                        fontWeight = FontWeight.Bold,
                        fontSize = 19.sp,
                        color = Color(0xFF0F172A)
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                            tint = Color(0xFF0F172A)
                        )
                    }
                },
                actions = {
                    TextButton(
                        onClick = {
                            keyboardPreferences.customThemeConfig = themeConfig
                            keyboardPreferences.isCustomThemeActive = true
                            keyboardPreferences.theme = KeyboardThemeOption.CUSTOM
                            Toast.makeText(context, "Custom theme saved & applied!", Toast.LENGTH_SHORT).show()
                            onThemeSaved()
                        }
                    ) {
                        Text(
                            text = "Save",
                            color = Color(0xFF16A34A),
                            fontWeight = FontWeight.Bold,
                            fontSize = 17.sp
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.White)
            )
        },
        containerColor = Color(0xFFF8FAFC)
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
        ) {
            // 1. LIVE KEYBOARD PREVIEW AT TOP
            CustomThemeKeyboardPreview(
                config = themeConfig,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 8.dp, vertical = 6.dp)
            )

            // 2. COLOR PRESETS ROW (As shown in screenshot 3)
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color.White)
                    .padding(vertical = 8.dp)
            ) {
                Text(
                    text = "Color Presets",
                    fontSize = 15.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = Color(0xFF334155),
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp)
                )

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState())
                        .padding(horizontal = 12.dp, vertical = 4.dp),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    CustomThemeConfig.PRESETS.forEach { preset ->
                        Surface(
                            modifier = Modifier
                                .size(52.dp)
                                .clip(RoundedCornerShape(10.dp))
                                .clickable {
                                    // Retain existing background image if user only changed colors
                                    themeConfig = preset.config.copy(
                                        backgroundImagePath = themeConfig.backgroundImagePath
                                    )
                                },
                            shape = RoundedCornerShape(10.dp),
                            color = Color(preset.previewColors[0]),
                            border = BorderStroke(1.5.dp, Color(0xFFCBD5E1))
                        ) {
                            Column(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .padding(6.dp),
                                verticalArrangement = Arrangement.SpaceBetween
                            ) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(16.dp)
                                            .clip(RoundedCornerShape(3.dp))
                                            .background(Color(preset.previewColors[1]))
                                    )
                                    Box(
                                        modifier = Modifier
                                            .size(16.dp)
                                            .clip(RoundedCornerShape(3.dp))
                                            .background(Color(preset.previewColors[2]))
                                    )
                                }
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(16.dp)
                                            .clip(RoundedCornerShape(3.dp))
                                            .background(Color(preset.previewColors[3]))
                                    )
                                    Box(
                                        modifier = Modifier
                                            .size(16.dp)
                                            .clip(RoundedCornerShape(3.dp))
                                            .background(Color(preset.previewColors[1]))
                                    )
                                }
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(2.dp))

            // 3. TABS ROW: Background | Keys | Suggestion | Func. Keys
            TabRow(
                selectedTabIndex = activeTab,
                containerColor = Color.White,
                contentColor = Color(0xFF16A34A),
                indicator = { tabPositions ->
                    if (activeTab < tabPositions.size) {
                        TabRowDefaults.SecondaryIndicator(
                            modifier = Modifier.tabIndicatorOffset(tabPositions[activeTab]),
                            color = Color(0xFF16A34A),
                            height = 3.dp
                        )
                    }
                }
            ) {
                tabs.forEachIndexed { index, tabTitle ->
                    val isSelected = activeTab == index
                    Tab(
                        selected = isSelected,
                        onClick = { activeTab = index },
                        text = {
                            Text(
                                text = tabTitle,
                                fontSize = 14.5.sp,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                color = if (isSelected) Color(0xFF16A34A) else Color(0xFF64748B)
                            )
                        }
                    )
                }
            }

            // 4. TAB CONTENT
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(12.dp),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                ) {
                    when (activeTab) {
                        0 -> {
                            // TAB 1: BACKGROUND
                            Text(
                                text = "Background Color",
                                fontSize = 15.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = Color(0xFF1E293B)
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            ColorPaletteRow(
                                selectedColor = themeConfig.backgroundColor,
                                palette = standardPalette,
                                onSelectColor = { color ->
                                    themeConfig = themeConfig.copy(backgroundColor = color)
                                },
                                onOpenCustom = {
                                    colorDialogTarget = "backgroundColor"
                                    inputHexText = String.format("%06X", (themeConfig.backgroundColor and 0xFFFFFF))
                                    showColorDialog = true
                                }
                            )

                            Spacer(modifier = Modifier.height(20.dp))
                            HorizontalDivider(color = Color(0xFFF1F5F9))
                            Spacer(modifier = Modifier.height(16.dp))

                            Text(
                                text = "Set from Gallery",
                                fontSize = 15.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = Color(0xFF1E293B)
                            )
                            Spacer(modifier = Modifier.height(10.dp))

                            Button(
                                onClick = {
                                    photoPickerLauncher.launch(
                                        PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                                    )
                                },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(48.dp),
                                shape = RoundedCornerShape(10.dp),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = Color(0xFFF1F5F9),
                                    contentColor = Color(0xFF1E293B)
                                ),
                                border = BorderStroke(1.dp, Color(0xFFE2E8F0))
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Outlined.PhotoLibrary,
                                        contentDescription = null,
                                        tint = Color(0xFF475569),
                                        modifier = Modifier.size(20.dp)
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = "Choose image from gallery",
                                        fontSize = 14.5.sp,
                                        fontWeight = FontWeight.Medium
                                    )
                                }
                            }

                            // Show Remove Image button if user has an image set
                            if (themeConfig.backgroundImagePath != null) {
                                Spacer(modifier = Modifier.height(8.dp))
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.End
                                ) {
                                    OutlinedButton(
                                        onClick = {
                                            themeConfig = themeConfig.copy(backgroundImagePath = null)
                                            Toast.makeText(context, "Background image removed", Toast.LENGTH_SHORT).show()
                                        },
                                        shape = RoundedCornerShape(8.dp),
                                        border = BorderStroke(1.dp, Color(0xFFEF4444)),
                                        colors = ButtonDefaults.outlinedButtonColors(contentColor = Color(0xFFEF4444))
                                    ) {
                                        Icon(Icons.Default.Close, contentDescription = null, modifier = Modifier.size(14.dp))
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text("Remove Image", fontSize = 12.sp)
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.height(20.dp))
                            HorizontalDivider(color = Color(0xFFF1F5F9))
                            Spacer(modifier = Modifier.height(16.dp))

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "Background Brightness",
                                    fontSize = 15.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = Color(0xFF1E293B)
                                )
                                Text(
                                    text = "${(themeConfig.backgroundBrightness * 100).roundToInt()}%",
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFF16A34A)
                                )
                            }
                            Spacer(modifier = Modifier.height(6.dp))
                            Slider(
                                value = themeConfig.backgroundBrightness,
                                onValueChange = { newVal ->
                                    themeConfig = themeConfig.copy(backgroundBrightness = newVal)
                                },
                                valueRange = 0.05f..1.0f,
                                colors = SliderDefaults.colors(
                                    thumbColor = Color(0xFF16A34A),
                                    activeTrackColor = Color(0xFF16A34A),
                                    inactiveTrackColor = Color(0xFFBAE6FD)
                                )
                            )
                        }

                        1 -> {
                            // TAB 2: KEYS
                            Text(
                                text = "Key Background",
                                fontSize = 15.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = Color(0xFF1E293B)
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            ColorPaletteRow(
                                selectedColor = themeConfig.keyBackgroundColor,
                                palette = standardPalette,
                                onSelectColor = { color ->
                                    themeConfig = themeConfig.copy(keyBackgroundColor = color)
                                },
                                onOpenCustom = {
                                    colorDialogTarget = "keyBackgroundColor"
                                    inputHexText = String.format("%06X", (themeConfig.keyBackgroundColor and 0xFFFFFF))
                                    showColorDialog = true
                                }
                            )

                            Spacer(modifier = Modifier.height(20.dp))
                            HorizontalDivider(color = Color(0xFFF1F5F9))
                            Spacer(modifier = Modifier.height(16.dp))

                            Text(
                                text = "Key Text Color",
                                fontSize = 15.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = Color(0xFF1E293B)
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            ColorPaletteRow(
                                selectedColor = themeConfig.keyTextColor,
                                palette = standardPalette,
                                onSelectColor = { color ->
                                    themeConfig = themeConfig.copy(keyTextColor = color)
                                },
                                onOpenCustom = {
                                    colorDialogTarget = "keyTextColor"
                                    inputHexText = String.format("%06X", (themeConfig.keyTextColor and 0xFFFFFF))
                                    showColorDialog = true
                                }
                            )

                            Spacer(modifier = Modifier.height(20.dp))
                            HorizontalDivider(color = Color(0xFFF1F5F9))
                            Spacer(modifier = Modifier.height(16.dp))

                            Text(
                                text = "Key Hint Color",
                                fontSize = 15.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = Color(0xFF1E293B)
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            ColorPaletteRow(
                                selectedColor = themeConfig.keyHintColor,
                                palette = standardPalette,
                                onSelectColor = { color ->
                                    themeConfig = themeConfig.copy(keyHintColor = color)
                                },
                                onOpenCustom = {
                                    colorDialogTarget = "keyHintColor"
                                    inputHexText = String.format("%06X", (themeConfig.keyHintColor and 0xFFFFFF))
                                    showColorDialog = true
                                }
                            )

                            Spacer(modifier = Modifier.height(20.dp))
                            HorizontalDivider(color = Color(0xFFF1F5F9))
                            Spacer(modifier = Modifier.height(16.dp))

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "Key Brightness",
                                    fontSize = 15.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = Color(0xFF1E293B)
                                )
                                Text(
                                    text = "${(themeConfig.keyBrightness * 100).roundToInt()}%",
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFF16A34A)
                                )
                            }
                            Spacer(modifier = Modifier.height(6.dp))
                            Slider(
                                value = themeConfig.keyBrightness,
                                onValueChange = { newVal ->
                                    themeConfig = themeConfig.copy(keyBrightness = newVal)
                                },
                                valueRange = 0.1f..1.0f,
                                colors = SliderDefaults.colors(
                                    thumbColor = Color(0xFF16A34A),
                                    activeTrackColor = Color(0xFF16A34A),
                                    inactiveTrackColor = Color(0xFFE2E8F0)
                                )
                            )
                        }

                        2 -> {
                            // TAB 3: SUGGESTION
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = "Expand image behind suggestion bar",
                                        fontSize = 15.sp,
                                        fontWeight = FontWeight.SemiBold,
                                        color = Color(0xFF1E293B)
                                    )
                                    Text(
                                        text = "Translucent toolbar to reveal background art",
                                        fontSize = 12.sp,
                                        color = Color(0xFF64748B)
                                    )
                                }
                                Switch(
                                    checked = themeConfig.expandImageBehindSuggestion,
                                    onCheckedChange = { isChecked ->
                                        themeConfig = themeConfig.copy(expandImageBehindSuggestion = isChecked)
                                    },
                                    colors = SwitchDefaults.colors(
                                        checkedThumbColor = Color.White,
                                        checkedTrackColor = Color(0xFF16A34A)
                                    )
                                )
                            }

                            Spacer(modifier = Modifier.height(20.dp))
                            HorizontalDivider(color = Color(0xFFF1F5F9))
                            Spacer(modifier = Modifier.height(16.dp))

                            Text(
                                text = "Suggestion Bar Background",
                                fontSize = 15.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = Color(0xFF1E293B)
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            ColorPaletteRow(
                                selectedColor = themeConfig.suggestionBackgroundColor,
                                palette = standardPalette,
                                onSelectColor = { color ->
                                    themeConfig = themeConfig.copy(suggestionBackgroundColor = color)
                                },
                                onOpenCustom = {
                                    colorDialogTarget = "suggestionBackgroundColor"
                                    inputHexText = String.format("%06X", (themeConfig.suggestionBackgroundColor and 0xFFFFFF))
                                    showColorDialog = true
                                }
                            )

                            Spacer(modifier = Modifier.height(20.dp))
                            HorizontalDivider(color = Color(0xFFF1F5F9))
                            Spacer(modifier = Modifier.height(16.dp))

                            Text(
                                text = "Suggestion Bar Text Color",
                                fontSize = 15.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = Color(0xFF1E293B)
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            ColorPaletteRow(
                                selectedColor = themeConfig.suggestionTextColor,
                                palette = standardPalette,
                                onSelectColor = { color ->
                                    themeConfig = themeConfig.copy(suggestionTextColor = color)
                                },
                                onOpenCustom = {
                                    colorDialogTarget = "suggestionTextColor"
                                    inputHexText = String.format("%06X", (themeConfig.suggestionTextColor and 0xFFFFFF))
                                    showColorDialog = true
                                }
                            )

                            Spacer(modifier = Modifier.height(20.dp))
                            HorizontalDivider(color = Color(0xFFF1F5F9))
                            Spacer(modifier = Modifier.height(16.dp))

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "Suggestion Bar Brightness",
                                    fontSize = 15.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = Color(0xFF1E293B)
                                )
                                Text(
                                    text = "${(themeConfig.suggestionBrightness * 100).roundToInt()}%",
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFF16A34A)
                                )
                            }
                            Spacer(modifier = Modifier.height(6.dp))
                            Slider(
                                value = themeConfig.suggestionBrightness,
                                onValueChange = { newVal ->
                                    themeConfig = themeConfig.copy(suggestionBrightness = newVal)
                                },
                                valueRange = 0.05f..1.0f,
                                colors = SliderDefaults.colors(
                                    thumbColor = Color(0xFF16A34A),
                                    activeTrackColor = Color(0xFF16A34A),
                                    inactiveTrackColor = Color(0xFFE2E8F0)
                                )
                            )
                        }

                        3 -> {
                            // TAB 4: FUNCTIONAL KEYS
                            Text(
                                text = "Functional Key Background",
                                fontSize = 15.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = Color(0xFF1E293B)
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            ColorPaletteRow(
                                selectedColor = themeConfig.funcKeyBackgroundColor,
                                palette = standardPalette,
                                onSelectColor = { color ->
                                    themeConfig = themeConfig.copy(funcKeyBackgroundColor = color)
                                },
                                onOpenCustom = {
                                    colorDialogTarget = "funcKeyBackgroundColor"
                                    inputHexText = String.format("%06X", (themeConfig.funcKeyBackgroundColor and 0xFFFFFF))
                                    showColorDialog = true
                                }
                            )

                            Spacer(modifier = Modifier.height(20.dp))
                            HorizontalDivider(color = Color(0xFFF1F5F9))
                            Spacer(modifier = Modifier.height(16.dp))

                            Text(
                                text = "Functional Key Text Color",
                                fontSize = 15.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = Color(0xFF1E293B)
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            ColorPaletteRow(
                                selectedColor = themeConfig.funcKeyTextColor,
                                palette = standardPalette,
                                onSelectColor = { color ->
                                    themeConfig = themeConfig.copy(funcKeyTextColor = color)
                                },
                                onOpenCustom = {
                                    colorDialogTarget = "funcKeyTextColor"
                                    inputHexText = String.format("%06X", (themeConfig.funcKeyTextColor and 0xFFFFFF))
                                    showColorDialog = true
                                }
                            )

                            Spacer(modifier = Modifier.height(20.dp))
                            HorizontalDivider(color = Color(0xFFF1F5F9))
                            Spacer(modifier = Modifier.height(16.dp))

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "Functional Key Brightness",
                                    fontSize = 15.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = Color(0xFF1E293B)
                                )
                                Text(
                                    text = "${(themeConfig.funcKeyBrightness * 100).roundToInt()}%",
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFF16A34A)
                                )
                            }
                            Spacer(modifier = Modifier.height(6.dp))
                            Slider(
                                value = themeConfig.funcKeyBrightness,
                                onValueChange = { newVal ->
                                    themeConfig = themeConfig.copy(funcKeyBrightness = newVal)
                                },
                                valueRange = 0.1f..1.0f,
                                colors = SliderDefaults.colors(
                                    thumbColor = Color(0xFF16A34A),
                                    activeTrackColor = Color(0xFF16A34A),
                                    inactiveTrackColor = Color(0xFFE2E8F0)
                                )
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(30.dp))
        }
    }

    // Custom Color Hex Dialog
    if (showColorDialog) {
        AlertDialog(
            onDismissRequest = { showColorDialog = false },
            title = { Text("Choose Custom Color", fontWeight = FontWeight.Bold) },
            text = {
                Column {
                    Text("Enter 6-character Hex code (e.g. 2563EB):", fontSize = 13.sp, color = Color(0xFF64748B))
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = inputHexText,
                        onValueChange = { inputHexText = it.take(6).uppercase() },
                        prefix = { Text("#", fontWeight = FontWeight.Bold) },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        try {
                            val parsed = java.lang.Long.parseLong(inputHexText, 16) or 0xFF000000L
                            when (colorDialogTarget) {
                                "backgroundColor" -> themeConfig = themeConfig.copy(backgroundColor = parsed)
                                "keyBackgroundColor" -> themeConfig = themeConfig.copy(keyBackgroundColor = parsed)
                                "keyTextColor" -> themeConfig = themeConfig.copy(keyTextColor = parsed)
                                "keyHintColor" -> themeConfig = themeConfig.copy(keyHintColor = parsed)
                                "suggestionBackgroundColor" -> themeConfig = themeConfig.copy(suggestionBackgroundColor = parsed)
                                "suggestionTextColor" -> themeConfig = themeConfig.copy(suggestionTextColor = parsed)
                                "funcKeyBackgroundColor" -> themeConfig = themeConfig.copy(funcKeyBackgroundColor = parsed)
                                "funcKeyTextColor" -> themeConfig = themeConfig.copy(funcKeyTextColor = parsed)
                            }
                            showColorDialog = false
                        } catch (e: Exception) {
                            Toast.makeText(context, "Invalid hex code", Toast.LENGTH_SHORT).show()
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF16A34A))
                ) {
                    Text("Apply")
                }
            },
            dismissButton = {
                TextButton(onClick = { showColorDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }
}

/**
 * Color Circle Swatches Row with selection indicator and "+" custom color button
 */
@Composable
private fun ColorPaletteRow(
    selectedColor: Long,
    palette: List<Long>,
    onSelectColor: (Long) -> Unit,
    onOpenCustom: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .horizontalScroll(rememberScrollState())
            .padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.spacedBy(10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        palette.forEach { colorVal ->
            val isSelected = selectedColor == colorVal
            Box(
                modifier = Modifier
                    .size(38.dp)
                    .clip(CircleShape)
                    .background(Color(colorVal))
                    .border(
                        width = if (isSelected) 3.dp else 1.dp,
                        color = if (isSelected) Color(0xFF16A34A) else Color(0xFFCBD5E1),
                        shape = CircleShape
                    )
                    .clickable { onSelectColor(colorVal) },
                contentAlignment = Alignment.Center
            ) {
                if (isSelected) {
                    Icon(
                        imageVector = Icons.Default.Check,
                        contentDescription = null,
                        tint = if (colorVal == 0xFFFFFFFFL) Color(0xFF16A34A) else Color.White,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }
        }

        // Custom "+" color picker circle
        Box(
            modifier = Modifier
                .size(38.dp)
                .clip(CircleShape)
                .background(Color(0xFFF1F5F9))
                .border(1.dp, Color(0xFFCBD5E1), CircleShape)
                .clickable { onOpenCustom() },
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Default.Add,
                contentDescription = "Custom Color",
                tint = Color(0xFF475569),
                modifier = Modifier.size(20.dp)
            )
        }
    }
}

/**
 * High-fidelity Live Keyboard Preview mirroring the attached screenshots
 */
@Composable
private fun CustomThemeKeyboardPreview(
    config: CustomThemeConfig,
    modifier: Modifier = Modifier
) {
    val bgCol = Color(config.backgroundColor).copy(alpha = config.backgroundBrightness)
    val keyBgCol = Color(config.keyBackgroundColor).copy(alpha = config.keyBrightness)
    val keyTextCol = Color(config.keyTextColor)
    val keyHintCol = Color(config.keyHintColor)

    val suggBgCol = if (config.expandImageBehindSuggestion) {
        Color.Transparent
    } else {
        Color(config.suggestionBackgroundColor).copy(alpha = config.suggestionBrightness)
    }
    val suggTextCol = Color(config.suggestionTextColor)

    val funcBgCol = Color(config.funcKeyBackgroundColor).copy(alpha = config.funcKeyBrightness)
    val funcTextCol = Color(config.funcKeyTextColor)

    Box(
        modifier = modifier
            .clip(RoundedCornerShape(12.dp))
            .background(Color(0xFF0F172A))
            .border(1.dp, Color(0xFFCBD5E1), RoundedCornerShape(12.dp))
    ) {
        // If background image is present, render it with background brightness overlay
        if (config.backgroundImagePath != null) {
            AsyncImage(
                model = File(config.backgroundImagePath),
                contentDescription = null,
                contentScale = ContentScale.Crop,
                modifier = Modifier.matchParentSize()
            )
            // Brightness overlay
            Box(
                modifier = Modifier
                    .matchParentSize()
                    .background(Color.Black.copy(alpha = 1f - config.backgroundBrightness))
            )
        } else {
            Box(
                modifier = Modifier
                    .matchParentSize()
                    .background(bgCol)
            )
        }

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 4.dp, horizontal = 4.dp)
        ) {
            // 1. Toolbar Preview Row (Suggestion Bar)
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(34.dp),
                shape = RoundedCornerShape(6.dp),
                color = suggBgCol
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 6.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    val icons = listOf("⟲", "☺", "☰", "📋", "I", "123", "✨", "⚙", "•••", "🎤")
                    icons.forEach { iconText ->
                        Text(
                            text = iconText,
                            fontSize = 13.sp,
                            color = suggTextCol,
                            fontWeight = FontWeight.Bold,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.padding(horizontal = 2.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(4.dp))

            // 2. Number Row: 1 2 3 4 5 6 7 8 9 0
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(3.dp)
            ) {
                val numbers = listOf("1", "2", "3", "4", "5", "6", "7", "8", "9", "0")
                numbers.forEach { num ->
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .height(32.dp)
                            .clip(RoundedCornerShape(5.dp))
                            .background(keyBgCol),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(text = num, color = keyTextCol, fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                    }
                }
            }

            Spacer(modifier = Modifier.height(3.dp))

            // 3. Row 1: q w e r t y u i o p
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(3.dp)
            ) {
                val row1 = listOf(
                    "q" to "1", "w" to "2", "e" to "3", "r" to "4", "t" to "5",
                    "y" to "6", "u" to "7", "i" to "8", "o" to "9", "p" to "0"
                )
                row1.forEach { (char, hint) ->
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .height(36.dp)
                            .clip(RoundedCornerShape(5.dp))
                            .background(keyBgCol)
                    ) {
                        Text(
                            text = hint,
                            color = keyHintCol,
                            fontSize = 8.sp,
                            modifier = Modifier
                                .align(Alignment.TopEnd)
                                .padding(top = 1.dp, end = 2.dp)
                        )
                        Text(
                            text = char,
                            color = keyTextCol,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Medium,
                            modifier = Modifier.align(Alignment.Center)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(3.dp))

            // 4. Row 2: a s d f g h j k l
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 10.dp),
                horizontalArrangement = Arrangement.spacedBy(3.dp)
            ) {
                val row2 = listOf(
                    "a" to "@", "s" to "#", "d" to "&", "f" to "*", "g" to "-",
                    "h" to "!", "j" to "?", "k" to "(", "l" to ")"
                )
                row2.forEach { (char, hint) ->
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .height(36.dp)
                            .clip(RoundedCornerShape(5.dp))
                            .background(keyBgCol)
                    ) {
                        Text(
                            text = hint,
                            color = keyHintCol,
                            fontSize = 8.sp,
                            modifier = Modifier
                                .align(Alignment.TopEnd)
                                .padding(top = 1.dp, end = 2.dp)
                        )
                        Text(
                            text = char,
                            color = keyTextCol,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Medium,
                            modifier = Modifier.align(Alignment.Center)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(3.dp))

            // 5. Row 3: Shift(Func) z x c v b n m Backspace(Func)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(3.dp)
            ) {
                // Shift Key (Functional)
                Box(
                    modifier = Modifier
                        .weight(1.3f)
                        .height(36.dp)
                        .clip(RoundedCornerShape(5.dp))
                        .background(funcBgCol),
                    contentAlignment = Alignment.Center
                ) {
                    Text(text = "⇧", color = funcTextCol, fontSize = 14.sp, fontWeight = FontWeight.Bold)
                }

                val row3 = listOf(
                    "z" to "\"", "x" to "'", "c" to ":", "v" to ";",
                    "b" to "/", "n" to ",", "m" to "?"
                )
                row3.forEach { (char, hint) ->
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .height(36.dp)
                            .clip(RoundedCornerShape(5.dp))
                            .background(keyBgCol)
                    ) {
                        Text(
                            text = hint,
                            color = keyHintCol,
                            fontSize = 8.sp,
                            modifier = Modifier
                                .align(Alignment.TopEnd)
                                .padding(top = 1.dp, end = 2.dp)
                        )
                        Text(
                            text = char,
                            color = keyTextCol,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Medium,
                            modifier = Modifier.align(Alignment.Center)
                        )
                    }
                }

                // Backspace Key (Functional)
                Box(
                    modifier = Modifier
                        .weight(1.3f)
                        .height(36.dp)
                        .clip(RoundedCornerShape(5.dp))
                        .background(funcBgCol),
                    contentAlignment = Alignment.Center
                ) {
                    Text(text = "⌫", color = funcTextCol, fontSize = 14.sp, fontWeight = FontWeight.Bold)
                }
            }

            Spacer(modifier = Modifier.height(3.dp))

            // 6. Row 4: ?123(Func) , Spacebar . Enter(Func)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(3.dp)
            ) {
                // ?123 Key (Functional)
                Box(
                    modifier = Modifier
                        .weight(1.3f)
                        .height(36.dp)
                        .clip(RoundedCornerShape(5.dp))
                        .background(funcBgCol),
                    contentAlignment = Alignment.Center
                ) {
                    Text(text = "?123", color = funcTextCol, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                }

                // Comma
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .height(36.dp)
                        .clip(RoundedCornerShape(5.dp))
                        .background(keyBgCol),
                    contentAlignment = Alignment.Center
                ) {
                    Text(text = ",", color = keyTextCol, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                }

                // Spacebar
                Box(
                    modifier = Modifier
                        .weight(4.5f)
                        .height(36.dp)
                        .clip(RoundedCornerShape(5.dp))
                        .background(keyBgCol),
                    contentAlignment = Alignment.Center
                ) {
                    Text(text = "KeyNova", color = keyTextCol.copy(alpha = 0.5f), fontSize = 11.sp)
                }

                // Period
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .height(36.dp)
                        .clip(RoundedCornerShape(5.dp))
                        .background(keyBgCol),
                    contentAlignment = Alignment.Center
                ) {
                    Text(text = ".", color = keyTextCol, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                }

                // Enter Key (Functional)
                Box(
                    modifier = Modifier
                        .weight(1.3f)
                        .height(36.dp)
                        .clip(RoundedCornerShape(5.dp))
                        .background(funcBgCol),
                    contentAlignment = Alignment.Center
                ) {
                    Text(text = "↵", color = funcTextCol, fontSize = 14.sp, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}
