# KeyNova — Native Android Keyboard Architecture & Implementation Plan

KeyNova is a clean, simple, lightweight, offline-first native Android keyboard application built with Kotlin, Android InputMethodService, and Jetpack Compose. It delivers smooth typing across English, Bangla, and Hindi, along with dedicated power tools for developers and gamers, all operating 100% offline with zero telemetry or data collection.

---

## User Review & Critical Decisions

> [!IMPORTANT]
> The following architectural and UX choices have been aligned based on user preferences and reference design:

- **Landing Experience**: Interactive test playground with inline keyboard testing, setup wizard (Enable & Select IME guides), and quick navigation to all settings.
- **Default Theme**: Clean Light theme with quick tools toolbar (accented with KeyNova modern blue, matching the reference design).
- **Pre-enabled Modules**: Multilingual typing (English, Bangla, Hindi), Coding toolbar with snippet manager, and Gaming layout with quick number row.
- **Privacy & Security**: Zero internet permissions, strictly local preferences and clipboard storage, zero background analytics or keylogging.

---

## 1. Overview & Core Concept

KeyNova serves two primary Android roles in a single unified APK:
1. **System-wide Native Input Method (`InputMethodService`)**: An Android IME that can be enabled in System Settings and used in any app on the phone for typing, emojis, clipboard paste, coding symbols, and gaming communication.
2. **KeyNova Hub & Settings Application (`MainActivity`)**: A Jetpack Compose application featuring onboarding, live interactive keyboard playground, setup assistant, theme selector, language configuration, snippet editor, clipboard manager, and privacy controls matching the 17-screen reference design.

```
┌────────────────────────────────────────────────────────────────────────┐
│                          KEYNOVA ARCHITECTURE                          │
├───────────────────────────────────┬────────────────────────────────────┤
│     KeyNova Settings & Hub App    │   KeyNova InputMethodService (IME) │
│          (Jetpack Compose)        │       (System-wide Keyboard)       │
├───────────────────────────────────┼────────────────────────────────────┤
│ • Splash & 3-Step Onboarding      │ • Full InputConnection Text Engine │
│ • Interactive Typing Playground   │ • English (QWERTY), Bangla, Hindi  │
│ • Theme Chooser (6 Themes)        │ • Quick Tools Toolbar              │
│ • Language Manager (EN, BN, HI)   │ • Coding Keyboard & Local Snippets │
│ • Layout Settings (Height/Number) │ • Gaming Keyboard (Pinned Numbers) │
│ • Clipboard Manager & Retention   │ • Emoji Panel (Offline Categories) │
│ • Coding Snippets Local Editor    │ • Local Clipboard Popup            │
│ • Privacy Control & Data Reset    │ • One-Handed & Compact Layouts     │
└───────────────────────────────────┴────────────────────────────────────┘
                                     │
                  ┌──────────────────┴──────────────────┐
                  ▼                                     ▼
        Local Storage / Prefs               Android OS Input Pipe
   (SharedPreferences / Zero Cloud)        (InputConnection / IME)
```

---

## 2. User Experience & Visual Design (Matching Reference Mockups)

### Screen Flows & Features
1. **Splash Screen**: Deep midnight blue background, minimalist KeyNova logo icon, tagline "Simple • Fast • Offline", bottom wave aesthetic.
2. **Onboarding (3 Steps)**:
   - *Step 1*: Intro to KeyNova with 4 feature pills (Multiple Languages, Gaming & Coding Tools, Clipboard, Privacy First).
   - *Step 2*: Language selection checklist (English, বাংলা Bangla, हिन्दी Hindi).
   - *Step 3*: Theme picker with interactive Light/Dark toggle and circular accent swatches (Light, Dark, Blue, Green, Purple, Red).
3. **Interactive Keyboard Playground & Setup**:
   - Interactive testing input field ("Hello, KeyNova!").
   - Live IME activation status card: Step 1: Enable KeyNova in System Settings (`ACTION_INPUT_METHOD_SETTINGS`), Step 2: Switch default keyboard (`showInputMethodPicker()`).
   - Live keyboard simulation preview directly inside Compose as well as full system IME support.
4. **Settings Hub**:
   - Clean list cards with Material symbols: Languages, Theme, Keyboard Layout, Clipboard, Gaming & Coding, Privacy, About.
5. **Keyboard Engine Layouts**:
   - **Toolbar**: Quick action buttons: `[Keyboard]`, `[Emoji]`, `[Clipboard]`, `[Coding </>]`, `[Gaming 🎮]`, `[Settings ⚙️]`.
   - **English (QWERTY)**: Standard 10-9-7 layout with dynamic Shift/Caps, number popup, action key (Enter/Search/Send/Done).
   - **Bangla Layout (বাংলা)**: Complete Bengali script characters, vowels, matras (কারচিহ্ন), hasant (্), conjunct handling, and dedicated spacebar label.
   - **Hindi Layout (हिन्दी)**: Devanagari script characters, swar, vyanjan, matras, nukta, halant (्), anusvara (ं), visarga (ः).
   - **Coding Keyboard**: Dedicated top symbol row (`{ } [ ] ( ) < > / \`), secondary symbol row (`@ # $ % ^ & * - +`), numbers row (`1-0`), and instant code snippets (`console.log()`, `fun main()`, `if () {}`, etc.).
   - **Gaming Keyboard**: Pinned top number row (`1 2 3 4 5 6 7 8 9 0`), WASD/arrow emphasis, quick tactical chat presets ("GG", "Need backup", "glhf", "Push B"), compact toolbar.
   - **Offline Emoji Panel**: Fast offline categorized emojis (Smileys, Gestures, Hearts, Animals, Food, Objects, Symbols) with search and recently used list.
   - **Clipboard History**: Clean card popup with recent copied items, tap to paste into text field, pin favorite snippets, delete, and clear all.
   - **One-Handed & Compact Mode**: Left/Right align toggle for easy thumb reach on large screens.

---

## 3. Key Product Decisions & Trade-Offs

- **No Network / Zero Telemetry**: Manifest will NOT request `android.permission.INTERNET`. Absolute privacy by design; no user keystrokes can ever leave the device.
- **Unified Keyboard View Model**: The keyboard rendering engine is designed to be reusable both within the `InputMethodService` (for system-wide typing) and directly inside the Compose Activity (for instant testing without needing system permissions).
- **Lightweight State Persistence**: Uses standard Android `SharedPreferences` for instant synchronous read during IME startup, avoiding heavy disk I/O or Room overhead on cold keyboard launch.
- **Custom Adaptive Launcher Icon**: Handcrafted vector/PNG adaptive icon with KeyNova keyboard glyph on a deep indigo canvas.

---

## 4. Technical Architecture & File Organization

```
app/src/main/
├── AndroidManifest.xml                  (IME Service declaration + Settings Activity)
├── res/
│   ├── xml/method.xml                   (InputMethod subtype definitions: EN, BN, HI)
│   ├── values/strings.xml               (App name "KeyNova", labels, strings)
│   ├── values/colors.xml                (KeyNova theme palettes)
│   └── drawable/                        (KeyNova vector assets and custom icon)
└── java/com/example/
    ├── MainActivity.kt                  (Compose host activity with edge-to-edge)
    ├── data/
    │   ├── KeyboardPreferences.kt       (Settings manager: theme, language, height, sound)
    │   ├── ClipboardRepository.kt       (Local clipboard history & pinned items)
    │   └── SnippetsRepository.kt        (Developer code snippets storage)
    ├── ime/
    │   ├── KeyNovaInputMethodService.kt (Native Android InputMethodService implementation)
    │   ├── KeyboardLayoutModel.kt       (Key mappings for EN, BN, HI, Coding, Gaming)
    │   └── KeyboardActionHandler.kt     (Text insertion, deletion, cursor motion)
    └── ui/
        ├── theme/Theme.kt & Color.kt    (KeyNova design system: Light, Dark, Blue, Green, etc.)
        ├── keyboard/                    (Compose Keyboard UI & Views)
        │   ├── KeyNovaKeyboardView.kt   (Core keyboard layout renderer)
        │   ├── KeyboardToolbar.kt       (Emoji, Clipboard, Coding, Gaming shortcuts)
        │   ├── EmojiPickerView.kt       (Offline emoji picker)
        │   ├── ClipboardPanelView.kt    (Clipboard sheet)
        │   └── CodingSnippetsView.kt    (Snippets sheet)
        └── screens/
            ├── SplashScreen.kt          (Splash screen)
            ├── OnboardingScreen.kt      (3-step onboarding flow)
            ├── PlaygroundScreen.kt      (Interactive testing bench & setup wizard)
            ├── SettingsScreen.kt        (Main settings directory)
            ├── LanguagesScreen.kt       (Language management)
            ├── ThemeSelectorScreen.kt   (Theme picker & preview)
            ├── LayoutSettingsScreen.kt  (Key height, spacing, number row toggle)
            ├── GamingCodingScreen.kt    (Gaming & coding preferences & custom snippets)
            ├── PrivacyScreen.kt         (Privacy statement, clear data)
            └── AboutScreen.kt           (Version, info, open source notice)
```

---

## 5. Verification & Testing Plan

1. **Compilation & Packaging**: Verify clean Gradle build with `compile_applet`.
2. **IME Manifest & Subtype Verification**: Confirm `method.xml` exposes EN, BN, and HI subtypes correctly.
3. **Robolectric Unit Tests**:
   - `KeyboardLayoutModelTest`: Verify Bangla Unicode points, Hindi Devanagari sequences, QWERTY layout definitions.
   - `KeyboardPreferencesTest`: Verify theme switching, language toggle persistence.
   - `ClipboardRepositoryTest`: Verify item clipping, pinning, and retention policy.
4. **Interactive Playground Verification**: Test keypress feedback, text editing, backspace, and toolbar switches directly in the app.
