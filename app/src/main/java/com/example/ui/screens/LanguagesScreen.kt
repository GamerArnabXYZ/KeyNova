package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.data.KeyboardPreferences
import com.example.data.LanguageSwitchMethod
import com.example.ime.DynamicKeyboardLayout
import com.example.ime.KeyboardLayoutRegistry

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LanguagesScreen(
    keyboardPreferences: KeyboardPreferences,
    onNavigateBack: () -> Unit
) {
    val context = LocalContext.current
    val registeredLayouts = remember { KeyboardLayoutRegistry.getLayouts(context) }

    var enabledLayoutIds by remember { mutableStateOf(keyboardPreferences.enabledLayoutIds) }
    var activeLayoutId by remember { mutableStateOf(keyboardPreferences.activeLayoutId) }
    var switchMethod by remember { mutableStateOf(keyboardPreferences.languageSwitchMethod) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = stringResource(R.string.languages_screen_title),
                        fontWeight = FontWeight.Bold,
                        fontSize = 20.sp,
                        color = Color(0xFF0F172A)
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = stringResource(R.string.action_back),
                            tint = Color(0xFF0F172A)
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color(0xFFF8FAFC))
            )
        },
        containerColor = Color(0xFFF8FAFC)
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // 1. Language Switcher Method
            item {
                Text(
                    text = stringResource(R.string.languages_screen_switch_method_section),
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 0.8.sp,
                    color = Color(0xFF64748B),
                    modifier = Modifier.padding(top = 4.dp, bottom = 2.dp)
                )
            }

            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    border = CardDefaults.outlinedCardBorder()
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        LanguageSwitchMethod.entries.forEach { method ->
                            val isSelected = switchMethod == method
                            val borderColor = if (isSelected) Color(0xFF2563EB) else Color(0xFFE2E8F0)
                            val bgColor = if (isSelected) Color(0xFFEFF6FF) else Color(0xFFF8FAFC)

                            Surface(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(10.dp))
                                    .clickable {
                                        switchMethod = method
                                        keyboardPreferences.languageSwitchMethod = method
                                    }
                                    .border(1.dp, borderColor, RoundedCornerShape(10.dp)),
                                color = bgColor
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(12.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    RadioButton(
                                        selected = isSelected,
                                        onClick = {
                                            switchMethod = method
                                            keyboardPreferences.languageSwitchMethod = method
                                        },
                                        colors = RadioButtonDefaults.colors(selectedColor = Color(0xFF2563EB))
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                            text = method.displayName,
                                            fontSize = 14.sp,
                                            fontWeight = FontWeight.SemiBold,
                                            color = Color(0xFF0F172A)
                                        )
                                        Text(
                                            text = method.description,
                                            fontSize = 12.sp,
                                            color = Color(0xFF64748B),
                                            modifier = Modifier.padding(top = 2.dp)
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }

            // 2. Dynamic XML-Registered Keyboards Section
            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 8.dp, bottom = 2.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "REGISTERED KEYBOARDS (${registeredLayouts.size})",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 0.8.sp,
                        color = Color(0xFF64748B)
                    )
                    Text(
                        text = "XML-Configured",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = Color(0xFF3B82F6)
                    )
                }
            }

            items(registeredLayouts) { layout ->
                val isEnabled = enabledLayoutIds.contains(layout.id)
                val isActive = activeLayoutId == layout.id

                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(14.dp))
                        .clickable {
                            // Tapping card sets this layout as active default
                            activeLayoutId = layout.id
                            keyboardPreferences.activeLayoutId = layout.id
                            if (!isEnabled) {
                                val updated = enabledLayoutIds + layout.id
                                enabledLayoutIds = updated
                                keyboardPreferences.enabledLayoutIds = updated
                            }
                        }
                        .then(
                            if (isActive) Modifier.border(1.5.dp, Color(0xFF2563EB), RoundedCornerShape(14.dp))
                            else Modifier.border(1.dp, Color(0xFFE2E8F0), RoundedCornerShape(14.dp))
                        ),
                    colors = CardDefaults.cardColors(containerColor = Color.White)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(14.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = layout.languageName,
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFF0F172A)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Surface(
                                    shape = RoundedCornerShape(6.dp),
                                    color = if (isActive) Color(0xFFDBEAFE) else Color(0xFFF1F5F9)
                                ) {
                                    Text(
                                        text = layout.layoutName,
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.SemiBold,
                                        color = if (isActive) Color(0xFF1D4ED8) else Color(0xFF475569),
                                        modifier = Modifier.padding(horizontal = 7.dp, vertical = 2.dp)
                                    )
                                }
                                if (isActive) {
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Surface(
                                        shape = RoundedCornerShape(4.dp),
                                        color = Color(0xFFDCFCE7)
                                    ) {
                                        Text(
                                            text = "ACTIVE",
                                            fontSize = 10.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = Color(0xFF15803D),
                                            modifier = Modifier.padding(horizontal = 5.dp, vertical = 2.dp)
                                        )
                                    }
                                }
                            }
                            Text(
                                text = "Code: ${layout.languageCode.uppercase()} • Script: ${layout.scriptName} • ID: ${layout.id}",
                                fontSize = 11.5.sp,
                                color = Color(0xFF64748B),
                                modifier = Modifier.padding(top = 3.dp)
                            )
                        }

                        // Toggle checkbox for enabled rotation
                        Checkbox(
                            checked = isEnabled,
                            onCheckedChange = { checked ->
                                val updated = if (checked) {
                                    enabledLayoutIds + layout.id
                                } else {
                                    if (enabledLayoutIds.size > 1) enabledLayoutIds - layout.id else enabledLayoutIds
                                }
                                enabledLayoutIds = updated
                                keyboardPreferences.enabledLayoutIds = updated
                            },
                            colors = CheckboxDefaults.colors(checkedColor = Color(0xFF2563EB))
                        )
                    }
                }
            }
        }
    }
}
