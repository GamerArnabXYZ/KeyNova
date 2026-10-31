package com.example.ui.keyboard

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.view.HapticFeedbackConstants
import android.widget.Toast
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Send
import androidx.compose.material.icons.filled.SwapHoriz
import androidx.compose.material.icons.outlined.Translate
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.KeyboardThemeOption

data class SupportedLang(val code: String, val displayName: String, val nativeName: String)

object TranslationEngine {
    val languages = listOf(
        SupportedLang("en", "English", "English"),
        SupportedLang("hi", "Hindi", "हिन्दी"),
        SupportedLang("bn", "Bengali", "বাংলা"),
        SupportedLang("es", "Spanish", "Español"),
        SupportedLang("fr", "French", "Français"),
        SupportedLang("de", "German", "Deutsch"),
        SupportedLang("ja", "Japanese", "日本語"),
        SupportedLang("zh", "Chinese", "中文"),
        SupportedLang("ar", "Arabic", "العربية"),
        SupportedLang("ru", "Russian", "Русский"),
        SupportedLang("pt", "Portuguese", "Português"),
        SupportedLang("it", "Italian", "Italiano"),
        SupportedLang("ko", "Korean", "한국어"),
        SupportedLang("ur", "Urdu", "اردو")
    )

    // Offline dictionary phrasebook mapping for common phrases & words
    private val phraseMap = mapOf(
        "hello" to mapOf("hi" to "नमस्ते", "bn" to "হ্যালো", "es" to "¡Hola!", "fr" to "Bonjour", "de" to "Hallo", "ja" to "こんにちは", "zh" to "你好", "ar" to "مرحباً", "ru" to "Привет", "pt" to "Olá", "it" to "Ciao", "ko" to "안녕하세요", "ur" to "ہیلو"),
        "how are you" to mapOf("hi" to "आप कैसे हैं?", "bn" to "আপনি কেমন আছেন?", "es" to "¿Cómo estás?", "fr" to "Comment allez-vous?", "de" to "Wie geht es dir?", "ja" to "お元気ですか？", "zh" to "你好吗？", "ar" to "كيف حالك؟", "ru" to "Как дела?", "pt" to "Como você está?", "it" to "Come stai?", "ko" to "어떻게 지내세요?", "ur" to "آپ کیسے ہیں؟"),
        "thank you" to mapOf("hi" to "धन्यवाद", "bn" to "ধন্যবাদ", "es" to "Gracias", "fr" to "Merci", "de" to "Danke", "ja" to "ありがとう", "zh" to "谢谢", "ar" to "شكراً", "ru" to "Спасибо", "pt" to "Obrigado", "it" to "Grazie", "ko" to "감사합니다", "ur" to "شکریہ"),
        "thank you so much" to mapOf("hi" to "बहुत बहुत धन्यवाद", "bn" to "আপনাকে অনেক ধন্যবাদ", "es" to "Muchas gracias", "fr" to "Merci beaucoup", "de" to "Vielen Dank", "ja" to "どうもありがとうございます", "zh" to "非常感谢", "ar" to "شكراً جزيلاً", "ru" to "Большое спасибо", "pt" to "Muito obrigado", "it" to "Molte grazie", "ko" to "정말 감사합니다", "ur" to "بہت بہت شکریہ"),
        "good morning" to mapOf("hi" to "शुभ प्रभात", "bn" to "সুপ্রভাত", "es" to "Buenos días", "fr" to "Bonjour", "de" to "Guten Morgen", "ja" to "おはようございます", "zh" to "早上好", "ar" to "صباح الخير", "ru" to "Доброе утро", "pt" to "Bom dia", "it" to "Buongiorno", "ko" to "좋은 아침입니다", "ur" to "صبح بخیر"),
        "good night" to mapOf("hi" to "शुभ रात्रि", "bn" to "শুভ রাত্রি", "es" to "Buenas noches", "fr" to "Bonne nuit", "de" to "Gute Nacht", "ja" to "おやすみなさい", "zh" to "晚安", "ar" to "تصبح على خير", "ru" to "Спокойной ночи", "pt" to "Boa noite", "it" to "Buonanotte", "ko" to "안녕히 주무세요", "ur" to "شب بخیر"),
        "nice to meet you" to mapOf("hi" to "आपसे मिलकर खुशी हुई", "bn" to "আপনার সাথে দেখা করে ভালো লাগলো", "es" to "Mucho gusto", "fr" to "Enchanté", "de" to "Freut mich, Sie kennenzulernen", "ja" to "はじめまして", "zh" to "很高兴见到你", "ar" to "تشرفت بمعرفتك", "ru" to "Приятно познакомиться", "pt" to "Prazer em conhecê-lo", "it" to "Piacere di conoscerti", "ko" to "만나서 반갑습니다", "ur" to "آپ سے مل کر خوشی ہوئی"),
        "where are you" to mapOf("hi" to "आप कहाँ हैं?", "bn" to "আপনি কোথায় আছেন?", "es" to "¿Dónde estás?", "fr" to "Où êtes-vous?", "de" to "Wo bist du?", "ja" to "どこにいますか？", "zh" to "你在哪里？", "ar" to "أين أنت؟", "ru" to "Где ты?", "pt" to "Onde você está?", "it" to "Dove sei?", "ko" to "어디에 계신가요?", "ur" to "آپ کہاں ہیں؟"),
        "see you soon" to mapOf("hi" to "जल्द ही मिलेंगे", "bn" to "শীঘ্রই দেখা হবে", "es" to "Hasta pronto", "fr" to "À bientôt", "de" to "Bis bald", "ja" to "またすぐ会いましょう", "zh" to "回头见", "ar" to "أراك قريباً", "ru" to "До скорой встречи", "pt" to "Até logo", "it" to "A presto", "ko" to "곧 뵙겠습니다", "ur" to "جلد ملیں گے"),
        "yes" to mapOf("hi" to "हाँ", "bn" to "হ্যাঁ", "es" to "Sí", "fr" to "Oui", "de" to "Ja", "ja" to "はい", "zh" to "是", "ar" to "نعم", "ru" to "Да", "pt" to "Sim", "it" to "Sì", "ko" to "네", "ur" to "ہاں"),
        "no" to mapOf("hi" to "नहीं", "bn" to "না", "es" to "No", "fr" to "Non", "de" to "Nein", "ja" to "いいえ", "zh" to "不", "ar" to "لا", "ru" to "Нет", "pt" to "Não", "it" to "No", "ko" to "아니요", "ur" to "نہیں"),
        "please" to mapOf("hi" to "कृपया", "bn" to "দয়া করে", "es" to "Por favor", "fr" to "S\'il vous plaît", "de" to "Bitte", "ja" to "お願いします", "zh" to "请", "ar" to "من فضلك", "ru" to "Пожалуйста", "pt" to "Por favor", "it" to "Per favore", "ko" to "부탁합니다", "ur" to "براہ کرم"),
        "welcome" to mapOf("hi" to "स्वागत है", "bn" to "স্বাগতম", "es" to "Bienvenido", "fr" to "Bienvenue", "de" to "Willkommen", "ja" to "ようこそ", "zh" to "欢迎", "ar" to "أهلاً بك", "ru" to "Добро пожаловать", "pt" to "Bem-vindo", "it" to "Benvenuto", "ko" to "환영합니다", "ur" to "خوش آمدید"),
        "what is your name" to mapOf("hi" to "आपका नाम क्या है?", "bn" to "আপনার নাম কি?", "es" to "¿Cómo te llamas?", "fr" to "Comment vous appelez-vous?", "de" to "Wie heißt du?", "ja" to "お名前は何ですか？", "zh" to "你叫什么名字？", "ar" to "ما اسمك؟", "ru" to "Как тебя зовут?", "pt" to "Qual é o seu nome?", "it" to "Come ti chiami?", "ko" to "이름이 무엇입니까?", "ur" to "آپ کا نام کیا ہے؟"),
        "i love this" to mapOf("hi" to "मुझे यह पसंद है", "bn" to "আমার এটা খুব পছন্দ", "es" to "Me encanta esto", "fr" to "J\'adore ça", "de" to "Ich liebe das", "ja" to "これが大好きです", "zh" to "我喜欢这个", "ar" to "أحب هذا", "ru" to "Мне это нравится", "pt" to "Eu adoro isso", "it" to "Lo adoro", "ko" to "이것이 마음에 듭니다", "ur" to "مجھے یہ پسند ہے")
    )

    fun translate(query: String, fromCode: String, toCode: String): String {
        val clean = query.trim()
        if (clean.isEmpty()) return ""
        if (fromCode == toCode) return clean

        val lower = clean.lowercase().removeSuffix("?").removeSuffix("!").removeSuffix(".")

        // Direct phrase match
        phraseMap[lower]?.get(toCode)?.let { return it }

        // Reverse check: if translating from Hindi/Bangla/etc. to English or other target
        for ((enKey, translations) in phraseMap) {
            val srcMatch = translations[fromCode]?.lowercase()?.removeSuffix("?")?.removeSuffix("!")?.removeSuffix(".")
            if (srcMatch != null && srcMatch == lower) {
                if (toCode == "en") return enKey.replaceFirstChar { it.uppercase() }
                translations[toCode]?.let { return it }
            }
        }

        // Word by word mapping
        val words = clean.split(" ")
        val translatedWords = words.map { word ->
            val wLower = word.lowercase().trim(',', '.', '!', '?')
            val trans = phraseMap[wLower]?.get(toCode)
            trans ?: word
        }

        return translatedWords.joinToString(" ")
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TranslationPanelView(
    theme: KeyboardThemeOption,
    onInsertText: (String) -> Unit,
    onClose: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val view = LocalView.current

    var fromLang by remember { mutableStateOf(TranslationEngine.languages[0]) } // English
    var toLang by remember { mutableStateOf(TranslationEngine.languages[1]) }   // Hindi
    var inputText by remember { mutableStateOf("") }
    var translatedText by remember { mutableStateOf("") }
    var showFromMenu by remember { mutableStateOf(false) }
    var showToMenu by remember { mutableStateOf(false) }

    val bgColor = Color(theme.backgroundHex)
    val cardBg = if (theme == KeyboardThemeOption.LIGHT) Color(0xFFF8FAFC) else Color(0xFF262626)
    val textColor = Color(theme.textHex)
    val accentColor = Color(theme.accentHex)
    val borderColor = if (theme == KeyboardThemeOption.LIGHT) Color(0xFFCBD5E1) else Color(0xFF404040)

    fun doTranslate(text: String, src: SupportedLang, dst: SupportedLang) {
        translatedText = TranslationEngine.translate(text, src.code, dst.code)
    }

    LaunchedEffect(inputText, fromLang, toLang) {
        if (inputText.isNotBlank()) {
            doTranslate(inputText, fromLang, toLang)
        } else {
            translatedText = ""
        }
    }

    val rotationAngle by animateFloatAsState(
        targetValue = if (fromLang.code == "hi") 180f else 0f,
        label = "swap_rotate"
    )

    Column(
        modifier = modifier
            .fillMaxWidth()
            .background(bgColor)
            .padding(horizontal = 8.dp, vertical = 4.dp),
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        // Top Toolbar: [←] [From Lang ▾] [⇄ Swap] [To Lang ▾] [Done / ×]
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 2.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                IconButton(
                    onClick = {
                        view.performHapticFeedback(HapticFeedbackConstants.KEYBOARD_TAP)
                        onClose()
                    },
                    modifier = Modifier.size(32.dp)
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Back to Keyboard",
                        tint = textColor,
                        modifier = Modifier.size(20.dp)
                    )
                }

                // Source Language Button
                Box {
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = cardBg,
                        border = androidx.compose.foundation.BorderStroke(1.dp, borderColor),
                        modifier = Modifier.clickable { showFromMenu = true }
                    ) {
                        Text(
                            text = fromLang.displayName,
                            color = textColor,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.SemiBold,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 6.dp)
                        )
                    }
                    DropdownMenu(
                        expanded = showFromMenu,
                        onDismissRequest = { showFromMenu = false }
                    ) {
                        TranslationEngine.languages.forEach { lang ->
                            DropdownMenuItem(
                                text = { Text("${lang.displayName} (${lang.nativeName})") },
                                onClick = {
                                    fromLang = lang
                                    showFromMenu = false
                                }
                            )
                        }
                    }
                }

                // Swap Button
                IconButton(
                    onClick = {
                        view.performHapticFeedback(HapticFeedbackConstants.KEYBOARD_TAP)
                        val temp = fromLang
                        fromLang = toLang
                        toLang = temp
                    },
                    modifier = Modifier.size(32.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.SwapHoriz,
                        contentDescription = "Swap Languages",
                        tint = accentColor,
                        modifier = Modifier
                            .size(20.dp)
                            .rotate(rotationAngle)
                    )
                }

                // Target Language Button
                Box {
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = cardBg,
                        border = androidx.compose.foundation.BorderStroke(1.dp, borderColor),
                        modifier = Modifier.clickable { showToMenu = true }
                    ) {
                        Text(
                            text = toLang.displayName,
                            color = textColor,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.SemiBold,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 6.dp)
                        )
                    }
                    DropdownMenu(
                        expanded = showToMenu,
                        onDismissRequest = { showToMenu = false }
                    ) {
                        TranslationEngine.languages.forEach { lang ->
                            DropdownMenuItem(
                                text = { Text("${lang.displayName} (${lang.nativeName})") },
                                onClick = {
                                    toLang = lang
                                    showToMenu = false
                                }
                            )
                        }
                    }
                }
            }

            // Quick Insert Action Button
            Button(
                onClick = {
                    val toInsert = if (translatedText.isNotBlank()) translatedText else inputText
                    if (toInsert.isNotBlank()) {
                        view.performHapticFeedback(HapticFeedbackConstants.KEYBOARD_TAP)
                        onInsertText(toInsert)
                        inputText = ""
                    }
                },
                enabled = inputText.isNotBlank(),
                colors = ButtonDefaults.buttonColors(
                    containerColor = accentColor,
                    contentColor = Color.White
                ),
                contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                modifier = Modifier.height(32.dp),
                shape = RoundedCornerShape(8.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Send,
                    contentDescription = "Insert",
                    modifier = Modifier.size(14.dp)
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text("Insert", fontSize = 12.sp, fontWeight = FontWeight.Bold)
            }
        }

        // Input & Translation Box
        Card(
            shape = RoundedCornerShape(12.dp),
            colors = CardDefaults.cardColors(containerColor = cardBg),
            border = androidx.compose.foundation.BorderStroke(1.dp, borderColor),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(8.dp),
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                // Input Text Field
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(modifier = Modifier.weight(1f)) {
                        if (inputText.isEmpty()) {
                            Text(
                                text = "Type in ${fromLang.displayName} to translate...",
                                color = textColor.copy(alpha = 0.45f),
                                fontSize = 13.sp
                            )
                        }
                        BasicTextField(
                            value = inputText,
                            onValueChange = { inputText = it },
                            textStyle = TextStyle(
                                color = textColor,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Medium
                            ),
                            cursorBrush = SolidColor(accentColor),
                            singleLine = true,
                            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
                            keyboardActions = KeyboardActions(onDone = {
                                val toInsert = if (translatedText.isNotBlank()) translatedText else inputText
                                if (toInsert.isNotBlank()) {
                                    onInsertText(toInsert)
                                    inputText = ""
                                }
                            }),
                            modifier = Modifier.fillMaxWidth()
                        )
                    }

                    if (inputText.isNotEmpty()) {
                        IconButton(
                            onClick = { inputText = "" },
                            modifier = Modifier.size(24.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Close,
                                contentDescription = "Clear",
                                tint = textColor.copy(alpha = 0.6f),
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    }
                }

                Divider(color = borderColor.copy(alpha = 0.5f), thickness = 0.8.dp)

                // Translated Result
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = if (translatedText.isNotBlank()) translatedText else "Translation will appear here...",
                        color = if (translatedText.isNotBlank()) accentColor else textColor.copy(alpha = 0.4f),
                        fontSize = 14.sp,
                        fontWeight = if (translatedText.isNotBlank()) FontWeight.Bold else FontWeight.Normal,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.weight(1f)
                    )

                    if (translatedText.isNotBlank()) {
                        IconButton(
                            onClick = {
                                val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                                clipboard.setPrimaryClip(ClipData.newPlainText("Translated Text", translatedText))
                                Toast.makeText(context, "Translation copied", Toast.LENGTH_SHORT).show()
                            },
                            modifier = Modifier.size(26.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.ContentCopy,
                                contentDescription = "Copy Translation",
                                tint = textColor.copy(alpha = 0.8f),
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    }
                }
            }
        }

        // Quick Common Conversation Phrase Chips
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(6.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            val quickPhrases = listOf(
                "Hello",
                "How are you",
                "Thank you so much",
                "Good morning",
                "Where are you",
                "Nice to meet you",
                "See you soon",
                "Yes",
                "No",
                "Please",
                "I love this"
            )

            quickPhrases.forEach { phrase ->
                Surface(
                    shape = RoundedCornerShape(16.dp),
                    color = cardBg,
                    border = androidx.compose.foundation.BorderStroke(1.dp, borderColor),
                    modifier = Modifier.clickable {
                        view.performHapticFeedback(HapticFeedbackConstants.KEYBOARD_TAP)
                        inputText = phrase
                    }
                ) {
                    Text(
                        text = phrase,
                        color = textColor.copy(alpha = 0.9f),
                        fontSize = 11.5.sp,
                        fontWeight = FontWeight.Medium,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }
            }
        }
    }
}
