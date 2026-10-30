package com.example.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
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
import com.example.data.KeyboardPreferences
import com.example.data.KeyboardThemeOption

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ThemeSelectorScreen(
    keyboardPreferences: KeyboardPreferences,
    onNavigateBack: () -> Unit,
    onNavigateToCustomThemeBuilder: () -> Unit = {}
) {
    var selectedTheme by remember { mutableStateOf(keyboardPreferences.theme) }
    var selectedTab by remember { mutableStateOf("Basic") }
    val tabs = listOf("Basic", "Nature", "Dark", "Accent")

    val themes = KeyboardThemeOption.entries

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Themes",
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
                actions = {
                    TextButton(
                        onClick = onNavigateToCustomThemeBuilder,
                        colors = ButtonDefaults.textButtonColors(contentColor = Color(0xFF16A34A))
                    ) {
                        Text(
                            text = "+ Custom",
                            fontWeight = FontWeight.Bold,
                            fontSize = 15.sp
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
                .padding(innerPadding)
                .padding(16.dp),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Column {
                // Category tabs
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 16.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    tabs.forEach { tab ->
                        val isSelected = tab == selectedTab
                        FilterChip(
                            selected = isSelected,
                            onClick = { selectedTab = tab },
                            label = { Text(tab, fontSize = 13.sp) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = Color(0xFF2563EB),
                                selectedLabelColor = Color.White
                            )
                        )
                    }
                }

                // Live Preview Card
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 16.dp),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(selectedTheme.backgroundHex))
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "${selectedTheme.displayName} Preview",
                            color = Color(selectedTheme.textHex),
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold
                        )

                        Spacer(modifier = Modifier.height(12.dp))

                        // Preview Mock Keys
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            listOf("q", "w", "e", "r", "t", "y", "u", "i", "o", "p").forEach { char ->
                                Box(
                                    modifier = Modifier
                                        .weight(1f)
                                        .height(38.dp)
                                        .clip(RoundedCornerShape(6.dp))
                                        .background(Color(selectedTheme.keyHex)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = char,
                                        color = Color(selectedTheme.textHex),
                                        fontSize = 13.sp,
                                        fontWeight = FontWeight.Medium
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(6.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .weight(1.5f)
                                    .height(38.dp)
                                    .clip(RoundedCornerShape(6.dp))
                                    .background(Color(selectedTheme.surfaceHex)),
                                contentAlignment = Alignment.Center
                            ) {
                                Text("?123", color = Color(selectedTheme.textHex), fontSize = 11.sp)
                            }

                            Box(
                                modifier = Modifier
                                    .weight(4f)
                                    .height(38.dp)
                                    .clip(RoundedCornerShape(6.dp))
                                    .background(Color(selectedTheme.keyHex)),
                                contentAlignment = Alignment.Center
                            ) {
                                Text("KeyNova", color = Color(selectedTheme.textHex), fontSize = 12.sp)
                            }

                            Box(
                                modifier = Modifier
                                    .weight(1.5f)
                                    .height(38.dp)
                                    .clip(RoundedCornerShape(6.dp))
                                    .background(Color(selectedTheme.accentHex)),
                                contentAlignment = Alignment.Center
                            ) {
                                Text("↵", color = Color.White, fontSize = 14.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }

                // Custom Theme Creator Banner
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 12.dp)
                        .clip(RoundedCornerShape(14.dp))
                        .clickable { onNavigateToCustomThemeBuilder() },
                    colors = CardDefaults.cardColors(containerColor = Color(0xFFF0FDF4)),
                    border = BorderStroke(1.5.dp, Color(0xFF86EFAC))
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(14.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.weight(1f)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(40.dp)
                                    .clip(RoundedCornerShape(10.dp))
                                    .background(Color(0xFFDCFCE7)),
                                contentAlignment = Alignment.Center
                            ) {
                                Text("🎨", fontSize = 20.sp)
                            }
                            Spacer(modifier = Modifier.width(12.dp))
                            Column {
                                Text(
                                    text = "Custom Theme Maker",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 15.sp,
                                    color = Color(0xFF14532D)
                                )
                                Text(
                                    text = "Gallery background, custom keys & colors",
                                    fontSize = 12.sp,
                                    color = Color(0xFF166534)
                                )
                            }
                        }
                        Button(
                            onClick = onNavigateToCustomThemeBuilder,
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF16A34A)),
                            shape = RoundedCornerShape(8.dp),
                            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp)
                        ) {
                            Text(if (selectedTheme == KeyboardThemeOption.CUSTOM) "Edit" else "Create", fontSize = 13.sp)
                        }
                    }
                }

                Text(
                    text = "Select Theme",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF475569),
                    modifier = Modifier.padding(bottom = 8.dp)
                )

                // Themes Grid
                LazyVerticalGrid(
                    columns = GridCells.Fixed(2),
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                    modifier = Modifier.height(200.dp)
                ) {
                    items(themes) { theme ->
                        val isSelected = selectedTheme == theme
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(12.dp))
                                .clickable {
                                    selectedTheme = theme
                                    keyboardPreferences.theme = theme
                                    if (theme == KeyboardThemeOption.CUSTOM) {
                                        keyboardPreferences.isCustomThemeActive = true
                                    } else {
                                        keyboardPreferences.isCustomThemeActive = false
                                    }
                                }
                                .then(
                                    if (isSelected) Modifier.border(2.dp, Color(0xFF2563EB), RoundedCornerShape(12.dp))
                                    else Modifier.border(1.dp, Color(0xFFE2E8F0), RoundedCornerShape(12.dp))
                                ),
                            colors = CardDefaults.cardColors(containerColor = Color(theme.backgroundHex))
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(12.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Box(
                                        modifier = Modifier
                                            .size(24.dp)
                                            .clip(RoundedCornerShape(6.dp))
                                            .background(Color(theme.accentHex))
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = theme.displayName,
                                        color = Color(theme.textHex),
                                        fontSize = 13.sp,
                                        fontWeight = FontWeight.SemiBold
                                    )
                                }

                                if (isSelected) {
                                    Icon(
                                        imageVector = Icons.Default.Check,
                                        contentDescription = null,
                                        tint = Color(theme.accentHex),
                                        modifier = Modifier.size(18.dp)
                                    )
                                }
                            }
                        }
                    }
                }
            }

            Button(
                onClick = onNavigateBack,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2563EB)),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text("Apply Theme", fontSize = 15.sp, fontWeight = FontWeight.SemiBold)
            }
        }
    }
}
