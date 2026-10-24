package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import com.example.data.ClipboardRepository
import com.example.data.KeyboardPreferences
import com.example.data.SnippetsRepository
import com.example.ime.KeyboardLayoutModel
import com.example.ui.screens.*
import com.example.ui.theme.MyApplicationTheme

enum class AppNavigationScreen {
    SPLASH,
    ONBOARDING,
    PLAYGROUND,
    SETTINGS_MAIN,
    SETTINGS_LANGUAGES,
    SETTINGS_THEMES,
    SETTINGS_TOOLBAR,
    SETTINGS_LAYOUT,
    SETTINGS_CLIPBOARD,
    SETTINGS_GAMING_CODING,
    SETTINGS_PRIVACY,
    SETTINGS_ABOUT
}

class MainActivity : ComponentActivity() {

    private lateinit var preferences: KeyboardPreferences
    private lateinit var clipboardRepo: ClipboardRepository
    private lateinit var snippetsRepo: SnippetsRepository

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        preferences = KeyboardPreferences(this)
        clipboardRepo = ClipboardRepository(this)
        snippetsRepo = SnippetsRepository(this)
        KeyboardLayoutModel.init(this)

        setContent {
            MyApplicationTheme {
                KeyNovaApp(
                    preferences = preferences,
                    clipboardRepo = clipboardRepo,
                    snippetsRepo = snippetsRepo
                )
            }
        }
    }
}

@Composable
fun KeyNovaApp(
    preferences: KeyboardPreferences,
    clipboardRepo: ClipboardRepository,
    snippetsRepo: SnippetsRepository
) {
    var currentScreen by remember { mutableStateOf(AppNavigationScreen.SPLASH) }

    AnimatedContent(
        targetState = currentScreen,
        transitionSpec = { fadeIn() togetherWith fadeOut() },
        label = "screen_transition",
        modifier = Modifier.fillMaxSize()
    ) { screen ->
        when (screen) {
            AppNavigationScreen.SPLASH -> {
                SplashScreen(
                    onTimeout = {
                        currentScreen = if (preferences.hasCompletedOnboarding) {
                            AppNavigationScreen.PLAYGROUND
                        } else {
                            AppNavigationScreen.ONBOARDING
                        }
                    }
                )
            }

            AppNavigationScreen.ONBOARDING -> {
                OnboardingScreen(
                    keyboardPreferences = preferences,
                    onFinish = {
                        currentScreen = AppNavigationScreen.PLAYGROUND
                    }
                )
            }

            AppNavigationScreen.PLAYGROUND -> {
                PlaygroundScreen(
                    keyboardPreferences = preferences,
                    clipboardRepository = clipboardRepo,
                    snippetsRepository = snippetsRepo,
                    onNavigateToSettings = {
                        currentScreen = AppNavigationScreen.SETTINGS_MAIN
                    },
                    onNavigateToToolbarSettings = {
                        currentScreen = AppNavigationScreen.SETTINGS_TOOLBAR
                    }
                )
            }

            AppNavigationScreen.SETTINGS_MAIN -> {
                BackHandler {
                    currentScreen = AppNavigationScreen.PLAYGROUND
                }
                SettingsScreen(
                    keyboardPreferences = preferences,
                    onNavigateBack = {
                        currentScreen = AppNavigationScreen.PLAYGROUND
                    },
                    onNavigateTo = { dest ->
                        currentScreen = when (dest) {
                            SettingsDestination.MAIN -> AppNavigationScreen.SETTINGS_MAIN
                            SettingsDestination.LANGUAGES -> AppNavigationScreen.SETTINGS_LANGUAGES
                            SettingsDestination.THEMES -> AppNavigationScreen.SETTINGS_THEMES
                            SettingsDestination.TOOLBAR -> AppNavigationScreen.SETTINGS_TOOLBAR
                            SettingsDestination.LAYOUT -> AppNavigationScreen.SETTINGS_LAYOUT
                            SettingsDestination.CLIPBOARD -> AppNavigationScreen.SETTINGS_CLIPBOARD
                            SettingsDestination.GAMING_CODING -> AppNavigationScreen.SETTINGS_GAMING_CODING
                            SettingsDestination.PRIVACY -> AppNavigationScreen.SETTINGS_PRIVACY
                            SettingsDestination.ABOUT -> AppNavigationScreen.SETTINGS_ABOUT
                        }
                    }
                )
            }

            AppNavigationScreen.SETTINGS_LANGUAGES -> {
                BackHandler {
                    currentScreen = AppNavigationScreen.SETTINGS_MAIN
                }
                LanguagesScreen(
                    keyboardPreferences = preferences,
                    onNavigateBack = {
                        currentScreen = AppNavigationScreen.SETTINGS_MAIN
                    }
                )
            }

            AppNavigationScreen.SETTINGS_THEMES -> {
                BackHandler {
                    currentScreen = AppNavigationScreen.SETTINGS_MAIN
                }
                ThemeSelectorScreen(
                    keyboardPreferences = preferences,
                    onNavigateBack = {
                        currentScreen = AppNavigationScreen.SETTINGS_MAIN
                    }
                )
            }

            AppNavigationScreen.SETTINGS_TOOLBAR -> {
                BackHandler {
                    currentScreen = AppNavigationScreen.SETTINGS_MAIN
                }
                ToolbarSettingsScreen(
                    keyboardPreferences = preferences,
                    onNavigateBack = {
                        currentScreen = AppNavigationScreen.SETTINGS_MAIN
                    }
                )
            }

            AppNavigationScreen.SETTINGS_LAYOUT -> {
                BackHandler {
                    currentScreen = AppNavigationScreen.SETTINGS_MAIN
                }
                LayoutSettingsScreen(
                    keyboardPreferences = preferences,
                    onNavigateBack = {
                        currentScreen = AppNavigationScreen.SETTINGS_MAIN
                    }
                )
            }

            AppNavigationScreen.SETTINGS_CLIPBOARD -> {
                BackHandler {
                    currentScreen = AppNavigationScreen.SETTINGS_MAIN
                }
                ClipboardScreen(
                    clipboardRepository = clipboardRepo,
                    onNavigateBack = {
                        currentScreen = AppNavigationScreen.SETTINGS_MAIN
                    }
                )
            }

            AppNavigationScreen.SETTINGS_GAMING_CODING -> {
                BackHandler {
                    currentScreen = AppNavigationScreen.SETTINGS_MAIN
                }
                GamingCodingScreen(
                    keyboardPreferences = preferences,
                    snippetsRepository = snippetsRepo,
                    onNavigateBack = {
                        currentScreen = AppNavigationScreen.SETTINGS_MAIN
                    }
                )
            }

            AppNavigationScreen.SETTINGS_PRIVACY -> {
                BackHandler {
                    currentScreen = AppNavigationScreen.SETTINGS_MAIN
                }
                PrivacyScreen(
                    keyboardPreferences = preferences,
                    clipboardRepository = clipboardRepo,
                    onNavigateBack = {
                        currentScreen = AppNavigationScreen.SETTINGS_MAIN
                    }
                )
            }

            AppNavigationScreen.SETTINGS_ABOUT -> {
                BackHandler {
                    currentScreen = AppNavigationScreen.SETTINGS_MAIN
                }
                AboutScreen(
                    onNavigateBack = {
                        currentScreen = AppNavigationScreen.SETTINGS_MAIN
                    }
                )
            }
        }
    }
}
