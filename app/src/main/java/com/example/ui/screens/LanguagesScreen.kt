package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.KeyboardLanguage
import com.example.data.KeyboardPreferences
import com.example.data.LanguageSwitchMethod

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LanguagesScreen(
    keyboardPreferences: KeyboardPreferences,
    onNavigateBack: () -> Unit
) {
    var enabledLangs by remember { mutableStateOf(keyboardPreferences.enabledLanguages) }
    var activeLang by remember { mutableStateOf(keyboardPreferences.activeLanguage) }
    var switchMethod by remember { mutableStateOf(keyboardPreferences.languageSwitchMethod) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Languages",
                        fontWeight = FontWeight.Bold,
                        fontSize = 20.sp,
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
            item {
                Text(
                    text = "Language Switcher Method",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF475569),
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
                                    .border(if (isSelected) 1.5.dp else 1.dp, borderColor, RoundedCornerShape(10.dp)),
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
                                            fontSize = 15.sp,
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

            item {
                Text(
                    text = "Enabled Keyboards",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF475569),
                    modifier = Modifier.padding(top = 8.dp, bottom = 4.dp)
                )
            }

            items(KeyboardLanguage.entries.size) { index ->
                val lang = KeyboardLanguage.entries[index]
                val isEnabled = enabledLangs.contains(lang)
                val isDefault = activeLang == lang

                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(14.dp))
                        .clickable {
                            val updated = if (isEnabled) {
                                if (enabledLangs.size > 1) enabledLangs - lang else enabledLangs
                            } else {
                                enabledLangs + lang
                            }
                            enabledLangs = updated
                            keyboardPreferences.enabledLanguages = updated
                        }
                        .then(
                            if (isDefault) Modifier.border(1.5.dp, Color(0xFF2563EB), RoundedCornerShape(14.dp))
                            else Modifier.border(1.dp, Color(0xFFE2E8F0), RoundedCornerShape(14.dp))
                        ),
                    colors = CardDefaults.cardColors(containerColor = Color.White)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = lang.nativeName,
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = Color(0xFF0F172A)
                                )
                                if (isDefault) {
                                    Spacer(modifier = Modifier.width(8.dp))
                                    AssistChip(
                                        onClick = {},
                                        label = { Text("Default", fontSize = 10.sp) },
                                        colors = AssistChipDefaults.assistChipColors(containerColor = Color(0xFFEFF6FF))
                                    )
                                }
                            }
                            Text(
                                text = "${lang.name} • ${lang.scriptName} script",
                                fontSize = 12.sp,
                                color = Color(0xFF64748B),
                                modifier = Modifier.padding(top = 2.dp)
                            )
                        }

                        Checkbox(
                            checked = isEnabled,
                            onCheckedChange = { checked ->
                                val updated = if (checked) {
                                    enabledLangs + lang
                                } else {
                                    if (enabledLangs.size > 1) enabledLangs - lang else enabledLangs
                                }
                                enabledLangs = updated
                                keyboardPreferences.enabledLanguages = updated
                            },
                            colors = CheckboxDefaults.colors(checkedColor = Color(0xFF2563EB))
                        )
                    }
                }
            }
        }
    }
}
