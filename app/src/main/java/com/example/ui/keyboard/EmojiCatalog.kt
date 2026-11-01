package com.example.ui.keyboard

import android.graphics.Paint
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Fastfood
import androidx.compose.material.icons.outlined.Flag
import androidx.compose.material.icons.outlined.Flight
import androidx.compose.material.icons.outlined.Lightbulb
import androidx.compose.material.icons.outlined.Mood
import androidx.compose.material.icons.outlined.PanTool
import androidx.compose.material.icons.outlined.Pets
import androidx.compose.material.icons.outlined.SportsSoccer
import androidx.compose.material.icons.outlined.Tag
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.core.graphics.PaintCompat
import androidx.emoji2.text.EmojiCompat

data class EmojiCategory(
    val title: String,
    val iconVector: ImageVector,
    val emojis: List<String>
)

object EmojiValidator {
    private val paint = Paint()

    fun isRenderable(emoji: String): Boolean {
        return try {
            if (EmojiCompat.isConfigured()) {
                val compat = EmojiCompat.get()
                if (compat.loadState == EmojiCompat.LOAD_STATE_SUCCEEDED) {
                    val match = compat.getEmojiMatch(emoji, Int.MAX_VALUE)
                    if (match == EmojiCompat.EMOJI_SUPPORTED) return true
                    if (match == EmojiCompat.EMOJI_UNSUPPORTED) return false
                }
            }
            PaintCompat.hasGlyph(paint, emoji)
        } catch (_: Exception) {
            true
        }
    }
}

object EmojiCatalog {

    val smileys = listOf(
        // Grinning & Joy
        "😀", "😃", "😄", "😁", "😆", "😅", "🤣", "😂", "🙂", "🙃", "🫠", "😉", "😊", "😇",
        // Affection & Love
        "🥰", "😍", "🤩", "😘", "😗", "😚", "😙", "🥲", "😋", "😛", "😜", "🤪", "😝", "🤑",
        // Hands on face & thoughtful
        "🤗", "🫣", "🤭", "🫢", "🫡", "🤫", "🤔", "🫡", "🤐", "🤨", "😐", "😑", "😶", "🫥",
        // Neutral & Skeptical
        "😏", "😒", "🙄", "😬", "🤥", "🫨", "😌", "😔", "😪", "🤤", "😴", "😷", "🤒", "🤕",
        // Unwell & Negative
        "🤢", "🤮", "🤧", "🥵", "🥶", "🥴", "😵", "😵‍💫", "🤯", "🤠", "🥳", "🥸", "😎", "🤓",
        // Surprised & Pleading
        "🧐", "😕", "🫤", "😟", "🙁", "☹️", "😮", "😯", "😲", "😳", "🥺", "🥹", "😦", "😧",
        // Fear & Sorrow
        "😨", "😰", "😥", "😢", "😭", "😱", "😖", "😣", "😞", "😓", "😩", "😫", "🥱", "😤",
        // Angry & Negative
        "😡", "😠", "🤬", "😈", "👿", "💀", "☠️", "💩", "🤡", "👹", "👺", "👻", "👽", "👾", "🤖",
        // Cat faces
        "😺", "😸", "😹", "😻", "😼", "😽", "🙀", "😿", "😾",
        // Monkeys
        "🙈", "🙉", "🙊",
        // Hearts & Heart emotions
        "❤️", "🩷", "🧡", "💛", "💚", "💙", "🩵", "💜", "🖤", "🩶", "🤍", "🤎", "💔",
        "❤️‍🔥", "❤️‍🩹", "❣️", "💕", "💞", "💓", "💗", "💖", "💘", "💝", "💟",
        // Emotional symbols
        "💋", "💌", "💯", "💢", "💥", "💫", "💦", "💨", "🕳️", "💭", "💬", "🗯️", "💤"
    )

    val people = listOf(
        // Hands & Gestures
        "👋", "🤚", "🖐️", "✋", "🖖", "🫱", "🫲", "🫸", "🫷", "🫳", "🫴",
        "👌", "🤌", "🤏", "✌️", "🤞", "🫰", "🤟", "🤘", "🤙", "👈", "👉", "👆", "🖕", "👇", "☝️", "🫵",
        "👍", "👎", "✊", "👊", "🤛", "🤜", "👏", "🙌", "🫶", "👐", "🤲", "🤝", "🙏",
        "✍️", "💅", "🤳", "💪", "🦾", "🦿", "🦵", "🦶", "👂", "🦻", "👃", "🧠", "🫀", "🫁", "🦷", "🦴",
        "👀", "👁️", "👅", "👄", "🫦",
        // People & Personas
        "👶", "🧒", "👦", "👧", "🧑", "👱", "👨", "🧔", "👩", "🧓", "👴", "👵",
        "🙍", "🙎", "🙅", "🙆", "💁", "🙋", "🧏", "🙇", "🤦", "🤷",
        "🧑‍⚕️", "🧑‍🎓", "🧑‍🏫", "🧑‍⚖️", "🧑‍🌾", "🧑‍🍳", "🧑‍🔧", "🧑‍🏭", "🧑‍💼", "🧑‍🔬", "🧑‍💻", "🧑‍🎤", "🧑‍🎨", "🧑‍✈️", "🧑‍🚀", "🧑‍🚒",
        "👮", "🕵️", "💂", "🥷", "👷", "🤴", "👸", "👳", "👲", "🧕", "🤵", "👰",
        "🤰", "🫄", "🤱", "👼", "🎅", "🤶", "🧑‍🎄", "🦸", "🦹", "🧙", "🧚", "🧛", "🧜", "🧝", "🧞", "🧟", "🧌",
        // Activities & Poses
        "💆", "💇", "🚶", "🧍", "🧎", "🏃", "💃", "🕺", "🕴️", "👯", "🧖", "🧗", "🧘", "🛀", "🛌",
        "👭", "👫", "👬", "💏", "💑", "👪"
    )

    val animals = listOf(
        // Mammals
        "🐶", "🐱", "🐭", "🐹", "🐰", "🦊", "🐻", "🐼", "🐻‍❄️", "🐨", "🐯", "🦁", "🐮", "🐷", "🐽", "🐸",
        "🐵", "🐒", "🐔", "🐧", "🐦", "🐤", "🐣", "🐥", "🦆", "🦅", "🦉", "🦇", "🐺", "🐗", "🐴", "🦄",
        // Bugs & Insects
        "🐝", "🪱", "🐛", "🦋", "🐌", "🐞", "🐜", "🪰", "🪲", "🪳", "🦟", "🦗", "🕷️", "🕸️", "🦂",
        // Reptiles & Sea
        "🐢", "🐍", "🦎", "🦖", "🦕", "🐙", "🦑", "🦐", "🦞", "🦀", "🐡", "🐠", "🐟", "🐬", "🐳", "🐋", "🦈", "🦭", "🐊",
        // Wild Animals
        "🐅", "🐆", "🦓", "🦍", "🦧", "🦣", "🐘", "🦛", "🦏", "🐪", "🐫", "🦒", "🦘", "🦬", "🐃", "🐂", "🐄", "🐎",
        "🐖", "🐏", "🐑", "🦙", "🐐", "🦌", "🐕", "🐩", "🦮", "🐕‍🦺", "🐈", "🐈‍⬛", "🪶", "🐓", "🦃", "🦤", "🦚", "🦜", "🦢", "🦩", "🕊️",
        "🐇", "🦝", "🦨", "🦡", "🦫", "🦦", "🦥", "🐁", "🐀", "🐿️", "🦔",
        // Plants & Nature
        "🌵", "🎄", "🌲", "🌳", "🌴", "🪵", "🌱", "🌿", "☘️", "🍀", "🎍", "🪴", "🎋", "🍃", "🍂", "🍁", "🍄", "🌾", "💐",
        "🌷", "🌹", "🥀", "🌺", "🌸", "🌼", "🌻", "🪷",
        // Sky & Weather
        "🌞", "🌝", "🌛", "🌜", "🌚", "🌕", "🌖", "🌗", "🌘", "🌑", "🌒", "🌓", "🌔", "🌙",
        "🌎", "🌍", "🌏", "🪐", "💫", "⭐", "🌟", "✨", "⚡", "☄️", "💥", "🔥", "🌪️", "🌈",
        "☀️", "🌤️", "⛅", "🌥️", "☁️", "🌦️", "🌧️", "⛈️", "🌩️", "🌨️", "❄️", "☃️", "⛄", "🌬️", "💨", "💧", "💦", "🫧", "☔", "☂️", "🌊", "🌫️"
    )

    val food = listOf(
        // Fruits
        "🍏", "🍎", "🍐", "🍊", "🍋", "🍌", "🍉", "🍇", "🍓", "🫐", "🍈", "🍒", "🍑", "🥭", "🍍", "🥥", "🥝", "🍅",
        // Vegetables
        "🍆", "🥑", "🥦", "🥬", "🥒", "🌶️", "🫑", "🌽", "🥕", "🫒", "🧄", "🧅", "🥔", "🍠", "🫚", "🫛",
        // Bakery & Breakfast
        "🥐", "🥯", "🍞", "🥖", "🥨", "🧀", "🥚", "🍳", "🧈", "🥞", "🧇", "🥓", "🥩", "🍗", "🍖", "🦴",
        // Meals & Fast Food
        "🌭", "🍔", "🍟", "🍕", "🫓", "🥪", "🥙", "🧆", "🌮", "🌯", "🫔", "🥗", "🥘", "🫕", "🥫",
        // Asian Cuisine
        "🍝", "🍜", "🍲", "🍛", "🍣", "🍱", "🥟", "🦪", "🍤", "🍙", "🍚", "🍘", "🍥", "🥠", "🥮", "🍢", "🍡",
        // Desserts & Sweets
        "🍧", "🍨", "🍦", "🥧", "🧁", "🍰", "🎂", "🍮", "🍭", "🍬", "🍫", "🍿", "🍩", "🍪", "🌰", "🥜", "🫘", "🍯",
        // Drinks
        "🥛", "🍼", "☕", "🫖", "🍵", "🍶", "🍾", "🍷", "🍸", "🍹", "🍺", "🍻", "🥂", "🥃", "🫗", "🥤", "🧋", "🧃", "🧉", "🧊"
    )

    val activities = listOf(
        // Ball sports
        "⚽", "⚾", "🥎", "🏀", "🏐", "🏈", "🏉", "🎾", "🥏", "🎳", "🏏", "🏑", "🏒", "🥍", "🏓", "🏸", "🥊", "🥋", "🥅", "⛳",
        // Other sports
        "⛸️", "🎣", "🤿", "🎽", "🎿", "🛷", "🥌", "🎯", "🪀", "🪁", "🔫", "🎱",
        // Games & Fun
        "🔮", "🪄", "🎮", "🕹️", "🎰", "🎲", "🧩", "🧸", "🪅", "🪩", "🪆", "🃏", "🀄", "🎭", "🖼️", "🎨",
        // Music & Instruments
        "🧵", "🪡", "🧶", "🪢", "🎤", "🎧", "🎼", "🎹", "🥁", "🪘", "🎷", "🎺", "🪗", "🎸", "🪕", "🎻",
        // Awards & Activities
        "🏆", "🥇", "🥈", "🥉", "🏅", "🎖️", "🎟️", "🎫", "🎪", "🤹", "🛹", "🛼", "🚴", "🚵", "🏇", "🧗", "🤺"
    )

    val travel = listOf(
        // Vehicles
        "🚗", "🚙", "🚕", "🚌", "🚎", "🏎️", "🚓", "🚑", "🚒", "🚐", "🛻", "🚚", "🚛", "🚜", "🏍️", "🛵", "🚲", "🛴",
        "🦯", "🦽", "🦼", "🛺", "🚨", "🚔", "🚍", "🚘", "🚖", "🚡", "🚠", "🚟", "🚃", "🚋", "🚞", "🚝", "🚄", "🚅", "🚈", "🚂", "🚆", "🚇", "🚊", "🚉",
        // Aerial & Maritime
        "✈️", "🛫", "🛬", "🛩️", "💺", "🛰️", "🚀", "🛸", "🚁", "🛶", "⛵", "🚤", "🛥️", "🛳️", "⛴️", "🚢", "⚓", "🛟",
        // Road items
        "⛽", "🚧", "🛑", "🚥", "🚦", "🧭", "🪂", "🚏",
        // Places & Buildings
        "🌋", "⛰️", "🏔️", "🗻", "🏕️", "🏖️", "🏜️", "🏝️", "🏞️", "🏟️", "🏛️", "🏗️", "🧱", "🛖", "🏘️", "🏚️", "🏠", "🏡",
        "🏢", "🏣", "🏤", "🏥", "🏦", "🏧", "🏨", "🏩", "🏪", "🏫", "🏬", "🏭", "🏯", "🏰", "💒", "🗼", "🗽", "⛪", "🕌", "🛕", "🕍", "⛩️", "🕋",
        // Scenery & City
        "⛲", "⛺", "🌁", "🌃", "🏙️", "🌄", "🌅", "🌆", "🌇", "🌉", "🎠", "🎡", "🎢"
    )

    val objects = listOf(
        // Electronics & Tech
        "📱", "📲", "☎️", "📞", "📟", "📠", "🔋", "🪫", "🔌", "💻", "🖥️", "🖨️", "⌨️", "🖱️", "💽", "💾", "💿", "📀",
        "🧮", "🎥", "🎞️", "📽️", "📺", "📷", "📸", "📹", "📼", "🔍", "🔎", "🕯️", "💡", "🔦", "🏮", "🪔",
        // Books & Paper
        "📔", "📕", "📖", "📗", "📘", "📙", "📚", "📓", "📒", "📃", "📜", "📄", "📰", "🗞️", "📑", "🔖", "🏷️",
        // Money
        "💰", "🪙", "💴", "💵", "💶", "💷", "💸", "💳", "🧾",
        // Mail & Office
        "✉️", "📧", "📨", "📩", "📤", "📥", "📦", "📫", "📪", "📬", "📭", "📮", "🗳️",
        "✏️", "✒️", "🖋️", "🖊️", "🖌️", "🖍️", "📝", "💼", "📁", "📂", "🗂️", "📅", "📆", "🗒️", "🗓️", "📇", "📈", "📉", "📊", "📋", "📌", "📍", "📎", "🖇️", "📏", "📐", "✂️", "🗃️", "🗄️", "🗑️",
        // Security & Tools
        "🔒", "🔓", "🔏", "🔐", "🔑", "🗝️", "🔨", "🪓", "⛏️", "⚒️", "🛠️", "🗡️", "⚔️", "💣", "🪃", "🏹", "🛡️", "🪚", "🔧", "🪛", "🔩", "⚙️", "🗜️", "⚖️", "🦯", "🔗", "⛓️", "🪝", "🧰", "🧲", "🪜",
        // Science & Medical
        "🧪", "🧫", "🧬", "🔬", "🔭", "📡", "💉", "🩸", "💊", "🩹", "🩺", "🩻",
        // Household items
        "🚪", "🛗", "🪞", "🪟", "🛏️", "🛋️", "🪑", "🚽", "🪠", "🚿", "🛁", "🪒", "🧴", "🧷", "🧹", "🧺", "🧻", "🪣", "🧼", "🫧", "🪥", "🧽", "🧯", "🛒", "🚬", "⚰️", "🪦", "⚱️"
    )

    val symbols = listOf(
        // Signs & Badges
        "🏧", "🚮", "🚰", "♿", "🚹", "🚺", "🚻", "🚼", "🚾", "🛂", "🛃", "🛄", "🛅",
        "⚠️", "🚸", "⛔", "🚫", "🚳", "🚭", "🚯", "🚱", "🚷", "📵", "🔞", "☢️", "☣️",
        // Arrows
        "⬆️", "↗️", "➡️", "↘️", "⬇️", "↙️", "⬅️", "↖️", "↕️", "↔️", "↩️", "↪️", "⤴️", "⤵️", "🔃", "🔄", "🔙", "🔚", "🔛", "🔜", "🔝",
        // Religion & Zodiac
        "🛐", "⚛️", "🕉️", "✡️", "☸️", "☯️", "✝️", "☦️", "☪️", "☮️", "🕎", "🔯", "🪯",
        "♈", "♉", "♊", "♋", "♌", "♍", "♎", "♏", "♐", "♑", "♒", "♓", "⛎",
        // Media control
        "🔀", "🔁", "🔂", "▶️", "⏩", "⏭️", "⏯️", "◀️", "⏪", "⏮️", "🔼", "⏫", "🔽", "⏬", "⏸️", "⏹️", "⏺️", "⏏️", "🎦", "🔅", "🔆", "📶", "📳", "📴",
        // Symbols & Math
        "♀️", "♂️", "⚧️", "✖️", "➕", "➖", "➗", "🟰", "♾️", "‼️", "⁉️", "❓", "❔", "❕", "❗", "〰️", "💱", "💲", "⚕️", "♻️", "⚜️", "🔱", "📛", "🔰",
        "⭕", "✅", "☑️", "✔️", "❌", "❎", "➰", "➿", "〽️", "✳️", "✴️", "❇️", "©️", "®️", "™️",
        // Keycaps & Alphanumeric
        "#️⃣", "*️⃣", "0️⃣", "1️⃣", "2️⃣", "3️⃣", "4️⃣", "5️⃣", "6️⃣", "7️⃣", "8️⃣", "9️⃣", "🔟",
        "🔠", "🔡", "🔢", "🔣", "🔤", "🅰️", "🆎", "🅱️", "🆑", "🆒", "🆓", "ℹ️", "🆔", "Ⓜ️", "🆕", "🆖", "🅾️", "🆗", "🅿️", "🆘", "🆙", "🆚",
        // Colored Shapes
        "🔴", "🟠", "🟡", "🟢", "🔵", "🟣", "🟤", "⚫", "⚪",
        "🟥", "🟧", "🟨", "🟩", "🟦", "🟪", "🟫", "⬛", "⬜",
        "◼️", "◻️", "◾", "◽", "▪️", "▫️", "🔶", "🔷", "🔸", "🔹", "🔺", "🔻", "💠", "🔘", "🔳", "🔲"
    )

    val flags = listOf(
        "🏁", "🚩", "🎌", "🏴", "🏳️", "🏳️‍🌈", "🏳️‍⚧️", "🏴‍☠️",
        "🇮🇳", "🇧🇩", "🇺🇸", "🇬🇧", "🇨🇦", "🇦🇺", "🇩🇪", "🇫🇷", "🇯🇵", "🇧🇷",
        "🇦🇷", "🇪🇸", "🇮🇹", "🇵🇰", "🇳🇵", "🇱🇰", "🇦🇪", "🇸🇦", "🇷🇺", "🇨🇳",
        "🇰🇷", "🇿🇦", "🇹🇷", "🇳🇬", "🇪🇬", "🇮🇩", "🇲🇾", "🇸🇬", "🇵🇭", "🇻🇳",
        "🇹🇭", "🇲🇽", "🇨🇴", "🇨🇱", "🇵🇪", "🇳🇿", "🇨🇭", "🇸🇪", "🇳🇴", "🇳🇱",
        "🇧🇪", "🇦🇹", "🇩🇰", "🇫🇮", "🇵🇹", "🇵🇱", "🇺🇦", "🇬🇷", "🇮🇪", "🇮🇱",
        "🇶🇦", "🇰🇼", "🇴🇲", "🇧🇭", "🇯🇴", "🇲🇦", "🇩🇿", "🇹🇳", "🇰🇪", "🇬🇭"
    )

    val categories = listOf(
        EmojiCategory(
            title = "Smileys & Emotion",
            iconVector = Icons.Outlined.Mood,
            emojis = smileys
        ),
        EmojiCategory(
            title = "People & Body",
            iconVector = Icons.Outlined.PanTool,
            emojis = people
        ),
        EmojiCategory(
            title = "Animals & Nature",
            iconVector = Icons.Outlined.Pets,
            emojis = animals
        ),
        EmojiCategory(
            title = "Food & Drink",
            iconVector = Icons.Outlined.Fastfood,
            emojis = food
        ),
        EmojiCategory(
            title = "Activities & Sports",
            iconVector = Icons.Outlined.SportsSoccer,
            emojis = activities
        ),
        EmojiCategory(
            title = "Travel & Places",
            iconVector = Icons.Outlined.Flight,
            emojis = travel
        ),
        EmojiCategory(
            title = "Objects",
            iconVector = Icons.Outlined.Lightbulb,
            emojis = objects
        ),
        EmojiCategory(
            title = "Symbols",
            iconVector = Icons.Outlined.Tag,
            emojis = symbols
        ),
        EmojiCategory(
            title = "Flags",
            iconVector = Icons.Outlined.Flag,
            emojis = flags
        )
    )

    // Tag based search index for fast instant search
    val emojiTags = mapOf(
        "laugh" to listOf("😂", "🤣", "😆", "😀", "😄", "😅"),
        "smile" to listOf("😀", "😃", "😄", "😁", "😊", "🙂", "😇"),
        "happy" to listOf("😀", "😊", "🥳", "🤩", "😁", "😇"),
        "love" to listOf("❤️", "🩷", "🥰", "😍", "😘", "💖", "💕", "💓", "💗", "💘", "💝"),
        "heart" to listOf("❤️", "🩷", "🧡", "💛", "💚", "💙", "🩵", "💜", "🤎", "🖤", "🩶", "🤍", "💔", "❤️‍🔥", "❤️‍🩹"),
        "cry" to listOf("😢", "😭", "🥺", "😿", "💧", "🥲"),
        "sad" to listOf("😞", "😔", "😟", "🙁", "☹️", "😢", "😭"),
        "fire" to listOf("🔥", "❤️‍🔥", "💥", "⚡", "✨"),
        "cool" to listOf("😎", "🤩", "🤙", "✌️", "🔥"),
        "thumbs" to listOf("👍", "👎"),
        "ok" to listOf("👌", "👍", "🙆"),
        "dog" to listOf("🐶", "🐕", "🦮", "🐕‍🦺", "🐩"),
        "cat" to listOf("🐱", "🐈", "🐈‍⬛", "🦁", "🐯"),
        "food" to listOf("🍕", "🍔", "🍟", "🌭", "🥪", "🍜", "🍣", "🌮", "🌯", "🍛"),
        "coffee" to listOf("☕", "🧋", "🍵", "🫖"),
        "game" to listOf("🎮", "🕹️", "👾", "🎲", "🎯", "🎰", "🧩"),
        "code" to listOf("💻", "🖥️", "⌨️", "📱", "💡", "📡"),
        "star" to listOf("⭐", "🌟", "✨", "💫"),
        "party" to listOf("🥳", "🎉", "🎈", "🍾", "🥂", "🎊"),
        "money" to listOf("💰", "💳", "💎", "🤑", "💵", "🪙"),
        "car" to listOf("🚗", "🏎️", "🚕", "🚙", "🚓", "🚑"),
        "plane" to listOf("✈️", "🛫", "🛬", "🚀", "🛸", "🚁"),
        "sport" to listOf("⚽", "🏀", "🏈", "⚾", "🎾", "🏐", "🏏", "🏸"),
        "music" to listOf("🎵", "🎶", "🎤", "🎧", "🎹", "🎸", "🥁"),
        "flag" to listOf("🇮🇳", "🇧🇩", "🇺🇸", "🇬🇧", "🏁", "🚩", "🏳️‍🌈"),
        "india" to listOf("🇮🇳"),
        "bangladesh" to listOf("🇧🇩"),
        "usa" to listOf("🇺🇸"),
        "uk" to listOf("🇬🇧")
    )
}

data class EmoticonCategory(
    val title: String,
    val icon: String,
    val emoticons: List<String>
)

object EmoticonCatalog {
    val categories = listOf(
        EmoticonCategory(
            title = "Shrug & Cool",
            icon = "¯\\_(ツ)_/¯",
            emoticons = listOf("¯\\_(ツ)_/¯", "( ͡° ͜ʖ ͡°)", "(•_•)", "(▀̿Ĺ̯▀̿ ̿)", "(⌐■_■)", "¯\\(°_o)/¯", "(☞ﾟヮﾟ)☞", "☜(ﾟヮﾟ☜)")
        ),
        EmoticonCategory(
            title = "Happy",
            icon = "(^o^)",
            emoticons = listOf("(^o^)", "(*^▽^*)", "(✿◠‿◠)", "(◕‿◕)", "(◠‿◠✿)", "(＾◡＾)", "(ﾉ◕ヮ◕)ﾉ*:･ﾟ✧", "٩(◕‿◕｡)۶", "(o^▽^o)", "(≧◡≦)")
        ),
        EmoticonCategory(
            title = "Love",
            icon = "(｡♥‿♥｡)",
            emoticons = listOf("(♡-_-♡)", "(｡♥‿♥｡)", "(⁄ ⁄>⁄ ▽ ⁄<⁄ ⁄)", "(♥ω♥*)", "(✿ ♥‿♥)", "(♡‿♡)", "(灬♥ω♥灬)", "(ര‿ര♥)")
        ),
        EmoticonCategory(
            title = "Cute / Animals",
            icon = "ʕ•ᴥ•ʔ",
            emoticons = listOf("ʕ•ᴥ•ʔ", "(=^･^=)", "(づ｡◕‿‿◕｡)づ", "⊂(・ヮ・⊂)", "(=^ェ^=)", "ʕᵔᴥᵔʔ", "(ᵔᴥᵔ)", "(=^-ω-^=)")
        ),
        EmoticonCategory(
            title = "Angry / Rage",
            icon = "(╯°□°)╯",
            emoticons = listOf("(╯°□°)╯︵ ┻━┻", "(ง'̀-'́)ง", "(ಠ_ಠ)", "(｀Д´)", "(ノಠ益ಠ)ノ彡┻━┻", "凸(｀0´)凸", "٩(╬ʘ益ʘ╬)۶")
        ),
        EmoticonCategory(
            title = "Sad / Cry",
            icon = "(╥﹏╥)",
            emoticons = listOf("(╥﹏╥)", "(T_T)", "(｡•́︿•̀｡)", "(;´༎ຶٹ༎ຶ)", "(ಥ﹏ಥ)", "(╯︵╰,)", "(ノ_<。)", "(っ˘̩╭╮˘̩)っ")
        )
    )
}
