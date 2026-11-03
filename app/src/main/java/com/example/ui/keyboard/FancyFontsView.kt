package com.example.ui.keyboard

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Keyboard
import androidx.compose.material.icons.filled.Send
import androidx.compose.material.icons.outlined.AutoAwesome
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.KeyboardThemeOption

/**
 * Data class representing a Fancy Unicode Font Style
 */
data class FancyStyle(
    val id: String,
    val name: String,
    val category: String, // "Popular", "Script", "Gothic", "Bubbles", "Blocks", "Frames"
    val badge: String? = null,
    val transform: (String) -> String
)

/**
 * Unicode Character Transformation Engine
 */
object UnicodeFontEngine {

    fun transform(text: String, styleId: String): String {
        val style = styles.firstOrNull { it.id == styleId } ?: return text
        return style.transform(text)
    }

    private fun mapChars(text: String, upperMap: String, lowerMap: String, digitMap: String? = null): String {
        val sb = StringBuilder()
        var i = 0
        while (i < text.length) {
            val cp = text.codePointAt(i)
            val charCount = Character.charCount(cp)
            i += charCount

            if (cp in 'A'.code..'Z'.code) {
                val index = cp - 'A'.code
                val targetCp = upperMap.codePointAt(upperMap.offsetByCodePoints(0, index))
                sb.appendCodePoint(targetCp)
            } else if (cp in 'a'.code..'z'.code) {
                val index = cp - 'a'.code
                val targetCp = lowerMap.codePointAt(lowerMap.offsetByCodePoints(0, index))
                sb.appendCodePoint(targetCp)
            } else if (digitMap != null && cp in '0'.code..'9'.code) {
                val index = cp - '0'.code
                val targetCp = digitMap.codePointAt(digitMap.offsetByCodePoints(0, index))
                sb.appendCodePoint(targetCp)
            } else {
                sb.appendCodePoint(cp)
            }
        }
        return sb.toString()
    }

    private fun addCombiningChar(text: String, combiningChar: Char): String {
        val sb = StringBuilder()
        for (ch in text) {
            sb.append(ch)
            if (ch != ' ') sb.append(combiningChar)
        }
        return sb.toString()
    }

    // Unicode lookup strings
    private const val FRAKTUR_BOLD_UPPER = "𝕬𝕭𝕮𝕯𝕰𝕱𝕲𝕳𝕴𝕵𝕶𝕷𝕸𝕹𝕺𝕻𝕼𝕽𝕾𝕿𝖀𝖁𝖂𝖃𝖄𝖅"
    private const val FRAKTUR_BOLD_LOWER = "𝖆𝖇𝖈𝖉𝖊𝖋𝖌𝖍𝖎𝖏𝖐𝖑𝖒𝖓𝖔𝖕𝖖𝖗𝖘𝖙𝖚𝖛𝖜𝖝𝖞𝖟"

    private const val FRAKTUR_REG_UPPER = "𝔄𝔅ℭ𝔇𝔈𝔉𝔊ℌℑ𝔍𝔎𝔏𝔐𝔑𝔒𝔓𝔔ℜ𝔖𝔗𝔘𝔙𝔚𝔛𝔜ℨ"
    private const val FRAKTUR_REG_LOWER = "𝔞𝔟𝔠𝔡𝔢𝔣𝔤𝔥𝔦𝔧𝔨𝔩𝔪𝔫𝔬𝔭𝔮𝔯𝔰𝔱𝔲𝔳𝔴𝔵𝔶𝔷"

    private const val SCRIPT_BOLD_UPPER = "𝓐𝓑𝓒𝓓𝓔𝓕𝓖𝓗𝓘𝓙𝓚𝓛𝓜𝓝𝓞𝓟𝓠𝓡𝓢𝓣𝓤𝓥𝓦𝓧𝓨𝓩"
    private const val SCRIPT_BOLD_LOWER = "𝓪𝓫𝓬𝓭𝓮𝓯𝓰𝓱𝓲𝓳𝓴𝓵𝓶𝓷𝓸𝓹𝓺𝓻𝓼𝓽𝓾𝓿𝔀𝔁𝔂𝔃"

    private const val SCRIPT_REG_UPPER = "𝒜ℬ𝒞𝒟ℰℱ𝒢ℋℐ𝒥𝒦ℒℳ𝒩𝒪𝒫𝒬ℛ𝒮𝒯𝒰𝒱𝒲𝒳𝒴𝒵"
    private const val SCRIPT_REG_LOWER = "𝒶𝒷𝒸𝒹ℯ𝒻ℊ𝒽𝒾𝒿𝓀𝓁𝓂𝓃ℴ𝓅𝓆𝓇𝓈𝓉𝓊𝓋𝓌𝓍𝓎𝓏"

    private const val DOUBLE_STRUCK_UPPER = "𝔸𝔹ℂ𝔻𝔼𝔽𝔾ℍ𝕀𝕁𝕂𝕃𝕄ℕ𝕆ℙℚℝ𝕊𝕋𝕌𝕍𝕎𝕏𝕐ℤ"
    private const val DOUBLE_STRUCK_LOWER = "𝕒𝕓𝕔𝕕𝕖𝕗𝕘𝕙𝕚𝕛𝕜𝕝𝕞𝕟𝕠𝕡𝕢𝕣𝕤𝕥𝕦𝕧𝕨𝕩𝕪𝕫"
    private const val DOUBLE_STRUCK_DIGITS = "𝟘𝟙𝟚𝟛𝟜𝟝𝟞𝟟𝟠𝟡"

    private const val CIRCLED_UPPER = "ⒶⒷⒸⒹⒺⒻⒼⒽⒾⒿⓀⓁⓂⓃⓄⓅⓆⓇⓈⓉⓊⓋⓌⓍⓎⓏ"
    private const val CIRCLED_LOWER = "ⓐⓑⓒⓓⓔⓕⓖⓗⓘⓙⓚⓛⓜⓝⓞⓟⓠⓡⓢⓣⓤⓥⓦⓧⓨⓩ"
    private const val CIRCLED_DIGITS = "⓪①②③④⑤⑥⑦⑧⑨"

    private const val DARK_CIRCLED_UPPER = "🅐🅑🅒🅓🅔🅕🅖🅗🅘🅙🅚🅛🅜🅝🅞🅟🅠🅡🅢🅣🅤🅥🅦🅧🅨🅩"
    private const val DARK_CIRCLED_LOWER = "🅐🅑🅒🅓🅔🅕🅖🅗🅘🅙🅚🅛🅜🅝🅞🅟🅠🅡🅢🅣🅤🅥🅦🅧🅨🅩"

    private const val SQUARED_UPPER = "🄰🄱🄲🄳🄴🄵🄶🄷🄸🄹🄺🄻🄼🄽🄾🄿🅀🅁🅂🅃🅄🅅🅆🅇🅈🅉"
    private const val SQUARED_LOWER = "🄰🄱🄲🄳🄴🄵🄶🄷🄸🄹🄺🄻🄼🄽🄾🄿🅀🅁🅂🅃🅄🅅🅆🅇🅈🅉"

    private const val DARK_SQUARED_UPPER = "🅰🅱🅲🅳🅴🅵🅶🅷🅸🅹🅺🅻🅼🅽🅾🅿🆀🆁🆂🆃🆄🆅🆆🆇🆈🆉"
    private const val DARK_SQUARED_LOWER = "🅰🅱🅲🅳🅴🅵🅶🅷🅸🅹🅺🅻🅼🅽🅾🅿🆀🆁🆂🆃🆄🆅🆆🆇🆈🆉"

    private const val FULLWIDTH_UPPER = "ＡＢＣＤＥＦＧＨＩＪＫＬＭＮＯＰＱＲＳＴＵＶＷＸＹＺ"
    private const val FULLWIDTH_LOWER = "ａｂｃｄｅｆｇｈｉｊｋｌｍｎｏｐｑｒｓｔｕｖｗｘｙｚ"
    private const val FULLWIDTH_DIGITS = "０１２３４５６７８９"

    private const val MONOSPACE_UPPER = "𝙰𝙱𝙲𝙳𝙴𝙵𝙶𝙷𝙸𝙹𝙺𝙻𝙼𝙽𝙾𝙿𝚀𝚁𝚂𝚃𝚄𝚅𝚆𝚇𝚈𝚉"
    private const val MONOSPACE_LOWER = "𝚊𝚋𝚌𝚍𝚎𝚏𝚐𝚑𝚒𝚓𝚔𝚕𝚖𝚗𝚘𝚙𝚚𝚛𝚜𝚝𝚞𝚟𝚠𝚡𝚢𝚣"
    private const val MONOSPACE_DIGITS = "𝟶𝟷𝟸𝟹𝟺𝟻𝟼𝟽𝟾𝟿"

    private const val BOLD_SANS_UPPER = "𝗔𝗕𝗖𝗗𝗘𝗙𝗚𝗛𝗜𝗝𝗞𝗟𝗠𝗡𝗢𝗣𝗤𝗥𝗦𝗧𝗨𝗩𝗪𝗫𝗬𝗭"
    private const val BOLD_SANS_LOWER = "𝗮𝗯𝗰𝗱𝗲𝗳𝗴𝗵𝗶𝗷𝗸𝗹𝗺𝗻𝗼𝗽𝗾𝗿𝘀𝘁𝘂𝘃𝘄𝘅𝘆𝘇"
    private const val BOLD_SANS_DIGITS = "𝟬𝟭𝟮𝟯𝟰𝟱𝟲𝟳𝟴𝟵"

    private const val ITALIC_SANS_UPPER = "𝘈𝘉𝘊𝘋𝘌𝘍𝘎𝘏𝘐𝘑𝘒𝘓𝘔𝘕𝘖𝘗𝘘𝘙𝘚𝘛𝘜𝘝𝘞𝘟𝘠𝘡"
    private const val ITALIC_SANS_LOWER = "𝘢𝘣𝘤𝘥𝘦𝘧𝘨𝘩𝘪𝘫𝘬𝘭𝘮𝘯𝘰𝘱𝘲𝘳𝘴𝘵𝘶𝘷𝘸𝘹𝘺𝘻"

    private const val BOLD_ITALIC_SANS_UPPER = "𝘼𝘽𝘾𝘿𝙀𝙁𝙂𝙃𝙄𝙅𝙆𝙇𝙈𝙉𝙊𝙋𝙌𝙍𝙎𝙏𝙐𝙑𝙒𝙓𝙔𝙕"
    private const val BOLD_ITALIC_SANS_LOWER = "𝙖𝙗𝙘𝙙𝙚𝙛𝙜𝙝𝙞𝙟𝙠𝙡𝙢𝙣𝙤𝙥𝙦𝙧𝙨𝙩𝙪𝙫𝙬𝙭𝙮𝙯"

    // Small Caps characters: a-z map
    private val SMALL_CAPS_MAP = mapOf(
        'a' to "ᴀ", 'b' to "ʙ", 'c' to "ᴄ", 'd' to "ᴅ", 'e' to "ᴇ", 'f' to "ꜰ",
        'g' to "ɢ", 'h' to "ʜ", 'i' to "ɪ", 'j' to "ᴊ", 'k' to "ᴋ", 'l' to "ʟ",
        'm' to "ᴍ", 'n' to "ɴ", 'o' to "ᴏ", 'p' to "ᴘ", 'q' to "ǫ", 'r' to "ʀ",
        's' to "ꜱ", 't' to "ᴛ", 'u' to "ᴜ", 'v' to "ᴠ", 'w' to "ᴡ", 'x' to "x",
        'y' to "ʏ", 'z' to "ᴢ"
    )

    fun toSmallCaps(text: String): String {
        val sb = StringBuilder()
        for (ch in text) {
            val lower = ch.lowercaseChar()
            if (SMALL_CAPS_MAP.containsKey(lower)) {
                sb.append(SMALL_CAPS_MAP[lower])
            } else {
                sb.append(ch)
            }
        }
        return sb.toString()
    }

    val styles = listOf(
        FancyStyle(
            id = "cursive_bold",
            name = "Cursive Bold",
            category = "Script",
            badge = "🔥 Popular",
            transform = { mapChars(it, SCRIPT_BOLD_UPPER, SCRIPT_BOLD_LOWER) }
        ),
        FancyStyle(
            id = "fraktur_bold",
            name = "Gothic / Fraktur",
            category = "Gothic",
            badge = "👑 Trending",
            transform = { mapChars(it, FRAKTUR_BOLD_UPPER, FRAKTUR_BOLD_LOWER) }
        ),
        FancyStyle(
            id = "double_struck",
            name = "Blackboard Bold",
            category = "Popular",
            badge = "✨ Classic",
            transform = { mapChars(it, DOUBLE_STRUCK_UPPER, DOUBLE_STRUCK_LOWER, DOUBLE_STRUCK_DIGITS) }
        ),
        FancyStyle(
            id = "bubble_light",
            name = "Bubble Circles",
            category = "Bubbles",
            badge = "⚪ Aesthetic",
            transform = { mapChars(it, CIRCLED_UPPER, CIRCLED_LOWER, CIRCLED_DIGITS) }
        ),
        FancyStyle(
            id = "bubble_dark",
            name = "Dark Bubble",
            category = "Bubbles",
            badge = "⚫ Dark",
            transform = { mapChars(it, DARK_CIRCLED_UPPER, DARK_CIRCLED_LOWER) }
        ),
        FancyStyle(
            id = "small_caps",
            name = "Small Capitals",
            category = "Popular",
            badge = "⭐ Clean",
            transform = { toSmallCaps(it) }
        ),
        FancyStyle(
            id = "vaporwave",
            name = "Aesthetic Wide",
            category = "Popular",
            badge = "🌴 Vapour",
            transform = { mapChars(it, FULLWIDTH_UPPER, FULLWIDTH_LOWER, FULLWIDTH_DIGITS) }
        ),
        FancyStyle(
            id = "squared_box",
            name = "Squared Blocks",
            category = "Blocks",
            badge = "🔲 Clean",
            transform = { mapChars(it, SQUARED_UPPER, SQUARED_LOWER) }
        ),
        FancyStyle(
            id = "dark_squared",
            name = "Dark Red / Badges",
            category = "Blocks",
            badge = "🟥 Bold",
            transform = { mapChars(it, DARK_SQUARED_UPPER, DARK_SQUARED_LOWER) }
        ),
        FancyStyle(
            id = "cursive_reg",
            name = "Cursive Elegant",
            category = "Script",
            badge = "✒️ Script",
            transform = { mapChars(it, SCRIPT_REG_UPPER, SCRIPT_REG_LOWER) }
        ),
        FancyStyle(
            id = "fraktur_reg",
            name = "Medieval Old English",
            category = "Gothic",
            badge = "🏰 Classic",
            transform = { mapChars(it, FRAKTUR_REG_UPPER, FRAKTUR_REG_LOWER) }
        ),
        FancyStyle(
            id = "monospace_code",
            name = "Retro Typewriter",
            category = "Popular",
            badge = "📟 Monospace",
            transform = { mapChars(it, MONOSPACE_UPPER, MONOSPACE_LOWER, MONOSPACE_DIGITS) }
        ),
        FancyStyle(
            id = "bold_sans",
            name = "Modern Heavy Sans",
            category = "Popular",
            badge = "📱 Modern",
            transform = { mapChars(it, BOLD_SANS_UPPER, BOLD_SANS_LOWER, BOLD_SANS_DIGITS) }
        ),
        FancyStyle(
            id = "italic_sans",
            name = "Smooth Slanted",
            category = "Popular",
            badge = "⚡ Fast",
            transform = { mapChars(it, ITALIC_SANS_UPPER, ITALIC_SANS_LOWER) }
        ),
        FancyStyle(
            id = "strike_through",
            name = "Strikethrough",
            category = "Decorations",
            badge = "❌ Crossed",
            transform = { addCombiningChar(it, '\u0336') }
        ),
        FancyStyle(
            id = "underline_double",
            name = "Double Underline",
            category = "Decorations",
            badge = "═ Underline",
            transform = { addCombiningChar(it, '\u0333') }
        ),
        FancyStyle(
            id = "sparkle_glitch",
            name = "Glitch Sparkles",
            category = "Decorations",
            badge = "҉ Glitch",
            transform = { addCombiningChar(it, '\u0489') }
        ),
        FancyStyle(
            id = "star_frame",
            name = "Royal Star Frame",
            category = "Frames",
            badge = "★ Magic",
            transform = { "★彡 $it 彡★" }
        ),
        FancyStyle(
            id = "heart_frame",
            name = "Love Hearts Frame",
            category = "Frames",
            badge = "♥ Sweet",
            transform = { "♥╣ $it ╠♥" }
        ),
        FancyStyle(
            id = "japanese_brackets",
            name = "Anime Brackets",
            category = "Frames",
            badge = "『』 Asian",
            transform = { "『 $it 』" }
        ),
        FancyStyle(
            id = "sparkle_badge",
            name = "Stardust Glint",
            category = "Frames",
            badge = "✧ Aesthetic",
            transform = { "✧･ﾟ:* $it *:･ﾟ✧" }
        ),
        FancyStyle(
            id = "crown_royal",
            name = "Royal Dynasty",
            category = "Frames",
            badge = "👑 VIP",
            transform = { "꧁ঔৣ☬✞ $it ✞☬ঔৣ꧂" }
        )
    )
}

/**
 * Modern M3 Fancy Fonts Keyboard View
 */
@Composable
fun FancyFontsView(
    theme: KeyboardThemeOption,
    onInsertText: (String) -> Unit,
    onClose: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    var inputText by remember { mutableStateOf("") }
    var selectedCategory by remember { mutableStateOf("All") }
    var showQuickKeyboard by remember { mutableStateOf(false) }

    val accentColor = Color(theme.accentHex)
    val textColor = Color(theme.textHex)
    val surfaceColor = Color(theme.surfaceHex)
    val backgroundColor = Color(theme.backgroundHex)

    val samplePresets = listOf(
        "KeyNova",
        "Hello World",
        "Good Morning ☀️",
        "Cool Vibes ✨",
        "Happy Birthday 🎂",
        "I Love You ❤️",
        "Game Over 🎮",
        "Thank You 🙏"
    )

    val categories = listOf("All", "Popular", "Script", "Gothic", "Bubbles", "Blocks", "Decorations", "Frames")

    val effectiveText = if (inputText.isNotBlank()) inputText else "KeyNova"

    val filteredStyles = remember(selectedCategory) {
        if (selectedCategory == "All") {
            UnicodeFontEngine.styles
        } else {
            UnicodeFontEngine.styles.filter { it.category.equals(selectedCategory, ignoreCase = true) }
        }
    }

    val copyToClipboard: (String) -> Unit = { textToCopy ->
        val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
        val clip = ClipData.newPlainText("KeyNova Fancy Font", textToCopy)
        clipboard.setPrimaryClip(clip)
        Toast.makeText(context, "Copied to clipboard!", Toast.LENGTH_SHORT).show()
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(backgroundColor)
            .padding(top = 2.dp)
    ) {
        // Top Navigation Bar
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(42.dp)
                .background(surfaceColor.copy(alpha = 0.5f))
                .padding(horizontal = 6.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(
                onClick = onClose,
                modifier = Modifier.size(34.dp)
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "Back to Keyboard",
                    tint = textColor.copy(alpha = 0.85f),
                    modifier = Modifier.size(20.dp)
                )
            }

            Spacer(modifier = Modifier.width(4.dp))

            // Brand title pill
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .clip(RoundedCornerShape(8.dp))
                    .background(accentColor.copy(alpha = 0.15f))
                    .padding(horizontal = 8.dp, vertical = 4.dp)
            ) {
                Icon(
                    imageVector = Icons.Outlined.AutoAwesome,
                    contentDescription = null,
                    tint = accentColor,
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = "Text Magic",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    color = accentColor
                )
            }

            Spacer(modifier = Modifier.weight(1f))

            // Quick Type Toggle
            IconButton(
                onClick = { showQuickKeyboard = !showQuickKeyboard },
                modifier = Modifier.size(34.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Keyboard,
                    contentDescription = "Toggle Keyboard Input",
                    tint = if (showQuickKeyboard) accentColor else textColor.copy(alpha = 0.7f),
                    modifier = Modifier.size(20.dp)
                )
            }

            // Quick Clear if text is present
            if (inputText.isNotEmpty()) {
                IconButton(
                    onClick = { inputText = "" },
                    modifier = Modifier.size(34.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Clear,
                        contentDescription = "Clear input",
                        tint = textColor.copy(alpha = 0.7f),
                        modifier = Modifier.size(18.dp)
                    )
                }
            }
        }

        // Live Input Bar
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp, vertical = 4.dp),
            shape = RoundedCornerShape(12.dp),
            color = surfaceColor,
            border = BorderStroke(1.dp, if (inputText.isNotBlank()) accentColor.copy(alpha = 0.6f) else textColor.copy(alpha = 0.12f))
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 10.dp, vertical = 6.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                BasicTextField(
                    value = inputText,
                    onValueChange = { inputText = it },
                    textStyle = TextStyle(
                        color = textColor,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.SemiBold
                    ),
                    cursorBrush = SolidColor(accentColor),
                    singleLine = true,
                    modifier = Modifier
                        .weight(1f)
                        .testTag("fancy_fonts_input"),
                    decorationBox = { innerTextField ->
                        if (inputText.isEmpty()) {
                            Text(
                                text = "Type text to transform (e.g. KeyNova)...",
                                color = textColor.copy(alpha = 0.45f),
                                fontSize = 13.sp
                            )
                        }
                        innerTextField()
                    }
                )

                if (inputText.isNotBlank()) {
                    Spacer(modifier = Modifier.width(6.dp))
                    // 1-tap insert of first style
                    FilledTonalIconButton(
                        onClick = {
                            val styled = UnicodeFontEngine.transform(inputText, "fraktur_bold")
                            onInsertText(styled)
                        },
                        modifier = Modifier.size(28.dp),
                        colors = IconButtonDefaults.filledTonalIconButtonColors(
                            containerColor = accentColor,
                            contentColor = Color.White
                        )
                    ) {
                        Icon(
                            imageVector = Icons.Default.Send,
                            contentDescription = "Insert",
                            tint = Color.White,
                            modifier = Modifier.size(14.dp)
                        )
                    }
                }
            }
        }

        // Preset Chips Row
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(30.dp)
                .horizontalScroll(rememberScrollState())
                .padding(horizontal = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            samplePresets.forEach { preset ->
                val isSelected = inputText.equals(preset, ignoreCase = true)
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .background(if (isSelected) accentColor.copy(alpha = 0.25f) else surfaceColor.copy(alpha = 0.6f))
                        .border(
                            0.6.dp,
                            if (isSelected) accentColor else textColor.copy(alpha = 0.10f),
                            RoundedCornerShape(8.dp)
                        )
                        .clickable { inputText = preset }
                        .padding(horizontal = 8.dp, vertical = 3.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = preset,
                        fontSize = 11.sp,
                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                        color = if (isSelected) accentColor else textColor.copy(alpha = 0.8f)
                    )
                }
            }
        }

        // Category Filter Chips
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(34.dp)
                .horizontalScroll(rememberScrollState())
                .padding(horizontal = 8.dp, vertical = 2.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            categories.forEach { cat ->
                val isSelected = selectedCategory == cat
                Surface(
                    onClick = { selectedCategory = cat },
                    shape = RoundedCornerShape(12.dp),
                    color = if (isSelected) accentColor else surfaceColor.copy(alpha = 0.7f),
                    border = BorderStroke(0.8.dp, if (isSelected) accentColor else textColor.copy(alpha = 0.12f)),
                    modifier = Modifier.height(26.dp)
                ) {
                    Box(
                        modifier = Modifier.padding(horizontal = 10.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = cat,
                            fontSize = 11.5.sp,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                            color = if (isSelected) Color.White else textColor.copy(alpha = 0.85f)
                        )
                    }
                }
            }
        }

        // Mini On-Screen Keyboard if user wants to type directly without leaving
        if (showQuickKeyboard) {
            SearchSoftKeyboard(
                theme = theme,
                isNumeric = false,
                textColor = textColor,
                surfaceColor = surfaceColor,
                accentColor = accentColor,
                keyColor = Color(theme.keyHex),
                onChar = { char -> inputText += char },
                onBackspace = {
                    if (inputText.isNotEmpty()) {
                        inputText = inputText.dropLast(1)
                    }
                },
                onSpace = { inputText += " " },
                onToggleNumeric = {},
                onDone = { showQuickKeyboard = false }
            )
        }

        // Scrollable List of Styled Text Cards
        LazyColumn(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .padding(horizontal = 8.dp, vertical = 4.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
            contentPadding = PaddingValues(bottom = 12.dp)
        ) {
            items(filteredStyles, key = { it.id }) { style ->
                val styledText = style.transform(effectiveText)

                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = surfaceColor,
                    border = BorderStroke(0.8.dp, textColor.copy(alpha = 0.12f)),
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 12.dp, vertical = 10.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Text(
                                    text = style.name,
                                    fontSize = 11.5.sp,
                                    fontWeight = FontWeight.Medium,
                                    color = textColor.copy(alpha = 0.6f)
                                )
                                if (style.badge != null) {
                                    Box(
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(6.dp))
                                            .background(accentColor.copy(alpha = 0.15f))
                                            .padding(horizontal = 5.dp, vertical = 1.dp)
                                    ) {
                                        Text(
                                            text = style.badge,
                                            fontSize = 9.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = accentColor
                                        )
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.height(4.dp))

                            Text(
                                text = styledText,
                                fontSize = 17.sp,
                                fontWeight = FontWeight.Bold,
                                color = textColor,
                                maxLines = 2,
                                overflow = TextOverflow.Ellipsis
                            )
                        }

                        Spacer(modifier = Modifier.width(8.dp))

                        // Action Buttons: Copy & Insert
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            // Copy button
                            IconButton(
                                onClick = { copyToClipboard(styledText) },
                                modifier = Modifier
                                    .size(34.dp)
                                    .clip(CircleShape)
                                    .background(textColor.copy(alpha = 0.06f))
                            ) {
                                Icon(
                                    imageVector = Icons.Default.ContentCopy,
                                    contentDescription = "Copy styled font",
                                    tint = textColor.copy(alpha = 0.75f),
                                    modifier = Modifier.size(16.dp)
                                )
                            }

                            // 1-tap direct insert button
                            Surface(
                                onClick = { onInsertText(styledText) },
                                shape = RoundedCornerShape(10.dp),
                                color = accentColor,
                                modifier = Modifier.height(34.dp)
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 10.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Send,
                                        contentDescription = null,
                                        tint = Color.White,
                                        modifier = Modifier.size(13.dp)
                                    )
                                    Text(
                                        text = "Insert",
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color.White
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
