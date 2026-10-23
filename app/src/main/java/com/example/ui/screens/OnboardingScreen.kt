package com.example.ui.screens

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.KeyboardLanguage
import com.example.data.KeyboardPreferences
import com.example.data.KeyboardThemeOption

@Composable
fun OnboardingScreen(
    keyboardPreferences: KeyboardPreferences,
    onFinish: () -> Unit
) {
    var step by remember { mutableIntStateOf(1) } // 1, 2, 3

    var selectedLangs by remember {
        mutableStateOf(keyboardPreferences.enabledLanguages)
    }

    var selectedTheme by remember {
        mutableStateOf(keyboardPreferences.theme)
    }

    Scaffold(
        containerColor = Color(0xFFF8FAFC)
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(24.dp)
        ) {
            AnimatedContent(
                targetState = step,
                transitionSpec = { fadeIn() togetherWith fadeOut() },
                label = "onboarding_step"
            ) { targetStep ->
                when (targetStep) {
                    1 -> OnboardingStep1(onNext = { step = 2 })
                    2 -> OnboardingStep2(
                        selectedLangs = selectedLangs,
                        onToggleLang = { lang ->
                            val updated = if (selectedLangs.contains(lang)) {
                                if (selectedLangs.size > 1) selectedLangs - lang else selectedLangs
                            } else {
                                selectedLangs + lang
                            }
                            selectedLangs = updated
                            keyboardPreferences.enabledLanguages = updated
                        },
                        onNext = { step = 3 },
                        onSkip = {
                            keyboardPreferences.hasCompletedOnboarding = true
                            onFinish()
                        }
                    )
                    3 -> OnboardingStep3(
                        selectedTheme = selectedTheme,
                        onSelectTheme = { theme ->
                            selectedTheme = theme
                            keyboardPreferences.theme = theme
                        },
                        onFinish = {
                            keyboardPreferences.hasCompletedOnboarding = true
                            onFinish()
                        }
                    )
                }
            }
        }
    }
}

@Composable
private fun OnboardingStep1(onNext: () -> Unit) {
    Column(
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.padding(top = 28.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(80.dp)
                    .clip(RoundedCornerShape(20.dp))
                    .background(Color(0xFF2563EB)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Outlined.Keyboard,
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.size(42.dp)
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = "KeyNova",
                fontSize = 28.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF0F172A)
            )

            Text(
                text = "Clean • Simple • Smooth",
                fontSize = 14.sp,
                color = Color(0xFF64748B),
                modifier = Modifier.padding(top = 4.dp)
            )

            Spacer(modifier = Modifier.height(32.dp))

            // Feature Rows
            FeatureItem(
                icon = Icons.Outlined.Language,
                title = "Multiple Languages",
                desc = "English, Bangla, Hindi"
            )
            FeatureItem(
                icon = Icons.Outlined.SportsEsports,
                title = "Gaming & Coding Tools",
                desc = "Quick symbols & snippets"
            )
            FeatureItem(
                icon = Icons.Outlined.ContentPaste,
                title = "Clipboard",
                desc = "Copy • Paste • Manage"
            )
            FeatureItem(
                icon = Icons.Outlined.Security,
                title = "Privacy First",
                desc = "No data collection, 100% offline"
            )
        }

        Button(
            onClick = onNext,
            modifier = Modifier
                .fillMaxWidth()
                .height(52.dp),
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2563EB)),
            shape = RoundedCornerShape(12.dp)
        ) {
            Text("Get Started", fontSize = 16.sp, fontWeight = FontWeight.SemiBold)
        }
    }
}

@Composable
private fun OnboardingStep2(
    selectedLangs: Set<KeyboardLanguage>,
    onToggleLang: (KeyboardLanguage) -> Unit,
    onNext: () -> Unit,
    onSkip: () -> Unit
) {
    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        Column(modifier = Modifier.padding(top = 20.dp)) {
            Text(
                text = "Choose your languages",
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF0F172A)
            )
            Text(
                text = "You can add more later from settings.",
                fontSize = 14.sp,
                color = Color(0xFF64748B),
                modifier = Modifier.padding(top = 4.dp, bottom = 24.dp)
            )

            LanguageChoiceCard(
                language = KeyboardLanguage.ENGLISH,
                title = "English",
                subtitle = "QWERTY layout",
                isSelected = selectedLangs.contains(KeyboardLanguage.ENGLISH),
                onToggle = { onToggleLang(KeyboardLanguage.ENGLISH) }
            )

            Spacer(modifier = Modifier.height(12.dp))

            LanguageChoiceCard(
                language = KeyboardLanguage.BANGLA,
                title = "বাংলা (Bangla)",
                subtitle = "Bengali script with vowels & matras",
                isSelected = selectedLangs.contains(KeyboardLanguage.BANGLA),
                onToggle = { onToggleLang(KeyboardLanguage.BANGLA) }
            )

            Spacer(modifier = Modifier.height(12.dp))

            LanguageChoiceCard(
                language = KeyboardLanguage.HINDI,
                title = "हिन्दी (Hindi)",
                subtitle = "Devanagari script with swar & vyanjan",
                isSelected = selectedLangs.contains(KeyboardLanguage.HINDI),
                onToggle = { onToggleLang(KeyboardLanguage.HINDI) }
            )
        }

        Column(modifier = Modifier.fillMaxWidth()) {
            Button(
                onClick = onNext,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2563EB)),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text("Next", fontSize = 16.sp, fontWeight = FontWeight.SemiBold)
            }

            Spacer(modifier = Modifier.height(8.dp))

            TextButton(
                onClick = onSkip,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Skip", color = Color(0xFF64748B), fontSize = 14.sp)
            }
        }
    }
}

@Composable
private fun OnboardingStep3(
    selectedTheme: KeyboardThemeOption,
    onSelectTheme: (KeyboardThemeOption) -> Unit,
    onFinish: () -> Unit
) {
    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        Column(modifier = Modifier.padding(top = 20.dp)) {
            Text(
                text = "Pick a theme",
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF0F172A)
            )
            Text(
                text = "Keep it simple or make it yours.",
                fontSize = 14.sp,
                color = Color(0xFF64748B),
                modifier = Modifier.padding(top = 4.dp, bottom = 24.dp)
            )

            // Two main preview cards: Light and Dark
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                ThemeMiniCard(
                    title = "Light",
                    theme = KeyboardThemeOption.LIGHT,
                    isSelected = selectedTheme == KeyboardThemeOption.LIGHT,
                    modifier = Modifier.weight(1f),
                    onSelect = { onSelectTheme(KeyboardThemeOption.LIGHT) }
                )

                ThemeMiniCard(
                    title = "Dark",
                    theme = KeyboardThemeOption.DARK,
                    isSelected = selectedTheme == KeyboardThemeOption.DARK,
                    modifier = Modifier.weight(1f),
                    onSelect = { onSelectTheme(KeyboardThemeOption.DARK) }
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            Text(
                text = "Accent Colors",
                fontSize = 14.sp,
                fontWeight = FontWeight.SemiBold,
                color = Color(0xFF334155),
                modifier = Modifier.padding(bottom = 12.dp)
            )

            // Color Swatches
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                listOf(
                    KeyboardThemeOption.LIGHT,
                    KeyboardThemeOption.DARK,
                    KeyboardThemeOption.BLUE,
                    KeyboardThemeOption.GREEN,
                    KeyboardThemeOption.PURPLE,
                    KeyboardThemeOption.RED
                ).forEach { opt ->
                    val isSelected = selectedTheme == opt
                    Box(
                        modifier = Modifier
                            .size(44.dp)
                            .clip(CircleShape)
                            .background(Color(opt.accentHex))
                            .clickable { onSelectTheme(opt) }
                            .then(
                                if (isSelected) Modifier.border(3.dp, Color(0xFF0F172A), CircleShape)
                                else Modifier
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        if (isSelected) {
                            Icon(
                                imageVector = Icons.Default.Check,
                                contentDescription = null,
                                tint = Color.White,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }
                }
            }
        }

        Button(
            onClick = onFinish,
            modifier = Modifier
                .fillMaxWidth()
                .height(52.dp),
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2563EB)),
            shape = RoundedCornerShape(12.dp)
        ) {
            Text("Get Started", fontSize = 16.sp, fontWeight = FontWeight.SemiBold)
        }
    }
}

@Composable
private fun FeatureItem(
    icon: ImageVector,
    title: String,
    desc: String
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(44.dp)
                .clip(CircleShape)
                .background(Color(0xFFEFF6FF)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = Color(0xFF2563EB),
                modifier = Modifier.size(22.dp)
            )
        }
        Spacer(modifier = Modifier.width(16.dp))
        Column {
            Text(text = title, fontSize = 15.sp, fontWeight = FontWeight.SemiBold, color = Color(0xFF0F172A))
            Text(text = desc, fontSize = 13.sp, color = Color(0xFF64748B))
        }
    }
}

@Composable
private fun LanguageChoiceCard(
    language: KeyboardLanguage,
    title: String,
    subtitle: String,
    isSelected: Boolean,
    onToggle: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .clickable(onClick = onToggle)
            .then(
                if (isSelected) Modifier.border(1.5.dp, Color(0xFF2563EB), RoundedCornerShape(14.dp))
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
                Text(text = title, fontSize = 16.sp, fontWeight = FontWeight.SemiBold, color = Color(0xFF0F172A))
                Text(text = subtitle, fontSize = 12.sp, color = Color(0xFF64748B))
            }
            Checkbox(
                checked = isSelected,
                onCheckedChange = { onToggle() },
                colors = CheckboxDefaults.colors(checkedColor = Color(0xFF2563EB))
            )
        }
    }
}

@Composable
private fun ThemeMiniCard(
    title: String,
    theme: KeyboardThemeOption,
    isSelected: Boolean,
    modifier: Modifier = Modifier,
    onSelect: () -> Unit
) {
    Card(
        modifier = modifier
            .clip(RoundedCornerShape(14.dp))
            .clickable(onClick = onSelect)
            .then(
                if (isSelected) Modifier.border(2.dp, Color(0xFF2563EB), RoundedCornerShape(14.dp))
                else Modifier.border(1.dp, Color(0xFFE2E8F0), RoundedCornerShape(14.dp))
            ),
        colors = CardDefaults.cardColors(containerColor = Color(theme.backgroundHex))
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(60.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(Color(theme.surfaceHex))
                    .padding(6.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "Q W E R T Y",
                    color = Color(theme.textHex),
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold
                )
            }
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = title,
                fontSize = 14.sp,
                fontWeight = FontWeight.Medium,
                color = Color(theme.textHex)
            )
        }
    }
}
