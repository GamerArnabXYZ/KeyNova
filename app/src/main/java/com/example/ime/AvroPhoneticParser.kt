package com.example.ime

/**
 * Avro Phonetic Transliteration Engine.
 * Converts Roman English keystrokes into authentic Bangla text (OmicronLab Avro Phonetic standard).
 * e.g., "ami" -> "আমি", "tumi" -> "তুমি", "kemon" -> "কেমন", "bangla" -> "বাংলা".
 */
object AvroPhoneticParser {

    private val COMMON_DICTIONARY: Map<String, List<String>> = mapOf(
        "ami" to listOf("আমি", "অমি", "আমিই"),
        "tumi" to listOf("তুমি", "তুমিই"),
        "apni" to listOf("আপনি", "আপনার"),
        "amar" to listOf("আমার"),
        "tomar" to listOf("তোমার"),
        "apnar" to listOf("আপনার"),
        "kemon" to listOf("কেমন", "কেমনও"),
        "acho" to listOf("আছো"),
        "achen" to listOf("আছেন"),
        "bhalo" to listOf("ভালো", "ভাল"),
        "bangla" to listOf("বাংলা", "বাঙলা"),
        "bangladesh" to listOf("বাংলাদেশ"),
        "dhonnobad" to listOf("ধন্যবাদ"),
        "desh" to listOf("দেশ", "দেস"),
        "shonar" to listOf("সোনার"),
        "sonar" to listOf("সোনার"),
        "onek" to listOf("অনেক"),
        "ekhon" to listOf("এখন"),
        "kichu" to listOf("কিছু"),
        "ki" to listOf("কি", "কী"),
        "keno" to listOf("কেন"),
        "kothay" to listOf("কোথায়", "কোথা"),
        "shundor" to listOf("সুন্দর"),
        "khub" to listOf("খুব"),
        "hobe" to listOf("হবে"),
        "korbo" to listOf("করবো"),
        "kori" to listOf("করি"),
        "kore" to listOf("করে"),
        "korte" to listOf("করতে"),
        "shob" to listOf("সব", "শব"),
        "sob" to listOf("সব"),
        "janina" to listOf("জানিনা"),
        "shathe" to listOf("সাথে"),
        "sathe" to listOf("সাথে"),
        "bondhu" to listOf("বন্ধু"),
        "shomoy" to listOf("সময়"),
        "somoy" to listOf("সময়"),
        "din" to listOf("দিন"),
        "raat" to listOf("রাত"),
        "rat" to listOf("রাত"),
        "shokal" to listOf("সকাল"),
        "sokal" to listOf("সকাল"),
        "mon" to listOf("মন"),
        "notun" to listOf("নতুন"),
        "bhasha" to listOf("ভাষা"),
        "manush" to listOf("মানুষ"),
        "kaj" to listOf("কাজ"),
        "ghor" to listOf("ঘর"),
        "baba" to listOf("বাবা"),
        "ma" to listOf("মা"),
        "bhai" to listOf("ভাই"),
        "bon" to listOf("বোন"),
        "khabar" to listOf("খাবার"),
        "paani" to listOf("পানি"),
        "pani" to listOf("পানি"),
        "ei" to listOf("এই"),
        "oi" to listOf("ওই"),
        "she" to listOf("সে"),
        "se" to listOf("সে"),
        "tara" to listOf("তারা"),
        "amra" to listOf("আমরা"),
        "tomra" to listOf("তোমরা"),
        "apnara" to listOf("আপনারা"),
        "ora" to listOf("ওরা"),
        "era" to listOf("এরা"),
        "prothom" to listOf("প্রথম"),
        "shikkha" to listOf("শিক্ষা"),
        "porikkha" to listOf("পরীক্ষা"),
        "dorkar" to listOf("দরকার"),
        "shobar" to listOf("সবার"),
        "sobar" to listOf("সবার"),
        "shonibar" to listOf("শনিবার"),
        "robibar" to listOf("রবিবার")
    )

    // Sorted by descending length for greedy matching
    private val CONJUNCTS: List<Pair<String, String>> = listOf(
        "kkh" to "ক্ষ",
        "jng" to "জ্ঞ",
        "cch" to "চ্ছ",
        "shth" to "ষ্ঠ",
        "shph" to "ষ্ফ",
        "nggh" to "ঙ্ঘ",
        "shk" to "ষ্ক",
        "sht" to "ষ্ট",
        "shn" to "ষ্ণ",
        "shp" to "ষ্প",
        "shm" to "ষ্ম",
        "skh" to "স্খ",
        "sth" to "স্থ",
        "sph" to "স্ফ",
        "nth" to "ন্থ",
        "ndh" to "ন্ধ",
        "mph" to "ম্ফ",
        "mbh" to "ম্ভ",
        "gdh" to "গ্ধ",
        "ddh" to "দ্ধ",
        "bdh" to "ব্ধ",
        "nch" to "ঞ্ছ",
        "njh" to "ঞ্ঝ",
        "NTh" to "ণ্ঠ",
        "NDh" to "ণ্ঢ",
        "tth" to "ত্থ",
        "jjh" to "জ্ঝ",
        "sk" to "স্ক",
        "st" to "স্ত",
        "sn" to "স্ন",
        "sp" to "স্প",
        "sm" to "স্ম",
        "sl" to "স্ল",
        "nt" to "ন্ত",
        "nd" to "ন্দ",
        "nn" to "ন্ন",
        "mp" to "ম্প",
        "mb" to "ম্ব",
        "mm" to "ম্ম",
        "kt" to "ক্ত",
        "kk" to "ক্ক",
        "gd" to "গ্দ",
        "gn" to "গ্ন",
        "gm" to "গ্ম",
        "tt" to "ত্ত",
        "tn" to "ত্ন",
        "tm" to "ত্ম",
        "dd" to "দ্দ",
        "db" to "দ্ব",
        "bd" to "ব্দ",
        "bb" to "ব্ব",
        "nk" to "ঙ্ক",
        "nkh" to "ঙ্খ",
        "ng" to "ঙ্গ",
        "nc" to "ঞ্চ",
        "nj" to "ঞ্জ",
        "NT" to "ণ্ট",
        "ND" to "ণ্ড",
        "NN" to "ণ্ণ",
        "TT" to "ট্ট",
        "DD" to "ড্ড",
        "cc" to "চ্চ",
        "jj" to "জ্জ",
        "ll" to "ল্ল",
        "lk" to "ল্ক",
        "lg" to "ল্গ",
        "lp" to "ল্প",
        "lb" to "ল্ব",
        "lm" to "ল্ম",
        "pt" to "প্ত",
        "pn" to "প্ন",
        "pl" to "প্ল",
        "ps" to "প্স"
    )

    private val SINGLE_CONSONANTS: Map<String, String> = mapOf(
        "kkh" to "ক্ষ",
        "kh" to "খ",
        "gh" to "ঘ",
        "Ng" to "ঙ",
        "ch" to "ছ",
        "c" to "চ",
        "jh" to "ঝ",
        "j" to "জ",
        "Th" to "ঠ",
        "T" to "ট",
        "Dh" to "ঢ",
        "D" to "ড",
        "th" to "থ",
        "t" to "ত",
        "dh" to "ধ",
        "d" to "দ",
        "N" to "ণ",
        "n" to "ন",
        "ph" to "ফ",
        "p" to "প",
        "f" to "ফ",
        "bh" to "ভ",
        "b" to "ব",
        "v" to "ভ",
        "m" to "ম",
        "r" to "র",
        "l" to "ল",
        "Sh" to "ষ",
        "sh" to "শ",
        "S" to "শ",
        "s" to "স",
        "h" to "হ",
        "Rh" to "ঢ়",
        "R" to "ড়",
        "y" to "য়",
        "Y" to "য়",
        "z" to "য",
        "Z" to "য",
        "k" to "ক",
        "g" to "গ",
        "q" to "ক",
        "w" to "ও",
        "x" to "ক্স"
    )

    private val VOWELS_INDEPENDENT: List<Pair<String, String>> = listOf(
        "rF" to "ঋ",
        "aa" to "আ",
        "ee" to "ঈ",
        "oo" to "ঊ",
        "oi" to "ঐ",
        "ou" to "ঔ",
        "OI" to "ঐ",
        "OU" to "ঔ",
        "A" to "আ",
        "a" to "আ",
        "i" to "ই",
        "I" to "ঈ",
        "u" to "উ",
        "U" to "ঊ",
        "e" to "এ",
        "E" to "এ",
        "o" to "ও",
        "O" to "ও"
    )

    private val VOWELS_DEPENDENT: List<Pair<String, String>> = listOf(
        "rF" to "ৃ",
        "aa" to "া",
        "ee" to "ী",
        "oo" to "ূ",
        "oi" to "ৈ",
        "ou" to "ৌ",
        "OI" to "ৈ",
        "OU" to "ৌ",
        "A" to "া",
        "a" to "া",
        "i" to "ি",
        "I" to "ী",
        "u" to "ু",
        "U" to "ূ",
        "e" to "ে",
        "E" to "ে",
        "o" to "ো",
        "O" to "ো"
    )

    /**
     * Parses Romanized English text into Bangla using Avro phonetic grammar rules.
     */
    fun parse(input: String): String {
        if (input.isBlank()) return input

        // 1. Direct dictionary match
        val lower = input.lowercase()
        COMMON_DICTIONARY[lower]?.firstOrNull()?.let { return it }

        val sb = StringBuilder()
        var i = 0
        val len = input.length
        var prevWasConsonant = false

        while (i < len) {
            val remaining = input.substring(i)

            // Punctuation & spaces
            val firstChar = remaining[0]
            if (firstChar == ' ') {
                sb.append(' ')
                i++
                prevWasConsonant = false
                continue
            }
            if (firstChar == '.') {
                sb.append('।')
                i++
                prevWasConsonant = false
                continue
            }
            if (remaining.startsWith("::")) {
                sb.append('ঃ')
                i += 2
                prevWasConsonant = false
                continue
            }
            if (firstChar == '^') {
                sb.append('ঁ')
                i++
                prevWasConsonant = false
                continue
            }
            if (remaining.startsWith("t`")) {
                sb.append('ৎ')
                i += 2
                prevWasConsonant = false
                continue
            }

            // Anusvar rule: "ng" at word boundary or before consonant
            if (remaining.startsWith("ng", ignoreCase = true)) {
                val afterNg = i + 2
                if (afterNg >= len || !isVowel(input[afterNg])) {
                    sb.append('ং')
                    i += 2
                    prevWasConsonant = false
                    continue
                }
            }

            // Vowel handling
            if (prevWasConsonant) {
                // Dependent vowel sign (kar)
                var matchedVowel = false
                for ((vKey, vVal) in VOWELS_DEPENDENT) {
                    if (remaining.startsWith(vKey, ignoreCase = (vKey.length > 1 && vKey != "rF"))) {
                        // Note: "o" after consonant is often implicit inherent vowel in Bengali (e.g. kemon -> কে-ম-ন)
                        // If "o", only add ো if capitalized "O" or followed by another vowel/end in explicit mode
                        if (vKey == "o" && remaining.startsWith("o") && !remaining.startsWith("O")) {
                            // Inherent vowel in Bengali: only add ো if at the very end of the word (e.g. "bhalo" -> "ভালো", "koro" -> "করো")
                            if (i + 1 == len) {
                                sb.append(vVal)
                            }
                        } else {
                            sb.append(vVal)
                        }
                        i += vKey.length
                        prevWasConsonant = false
                        matchedVowel = true
                        break
                    }
                }
                if (matchedVowel) continue
            } else {
                // Independent vowel (স্বরবর্ণ)
                var matchedVowel = false
                for ((vKey, vVal) in VOWELS_INDEPENDENT) {
                    if (remaining.startsWith(vKey, ignoreCase = (vKey.length > 1 && vKey != "rF"))) {
                        // "o" at start can be অ or ও
                        val outVowel = if (vKey == "o" && remaining.startsWith("o") && !remaining.startsWith("O")) {
                            "অ"
                        } else {
                            vVal
                        }
                        sb.append(outVowel)
                        i += vKey.length
                        prevWasConsonant = false
                        matchedVowel = true
                        break
                    }
                }
                if (matchedVowel) continue
            }

            // Reph handling: "r" followed by consonant (e.g. "rk" -> "র্ক", "rm" -> "র্ম")
            if ((firstChar == 'r' || firstChar == 'R') && i + 1 < len && isConsonant(input[i + 1])) {
                val nextCons = matchConsonant(input.substring(i + 1))
                if (nextCons != null) {
                    sb.append(nextCons.second)
                    sb.append('্') // Hasanta for conjunct or reph representation
                    // Reph in Bengali Unicode is র + ্ + ব্যঞ্জন
                    // Replace the appended char with reph sequence:
                    val lastAppendedLen = nextCons.second.length + 1
                    sb.delete(sb.length - lastAppendedLen, sb.length)
                    sb.append("র্")
                    sb.append(nextCons.second)
                    i += 1 + nextCons.first.length
                    prevWasConsonant = true
                    continue
                }
            }

            // Conjuncts (যুক্তবর্ণ)
            var matchedConjunct = false
            for ((cKey, cVal) in CONJUNCTS) {
                if (remaining.startsWith(cKey, ignoreCase = true)) {
                    sb.append(cVal)
                    i += cKey.length
                    prevWasConsonant = true
                    matchedConjunct = true
                    break
                }
            }
            if (matchedConjunct) continue

            // Ro-phola: Consonant + 'r' (e.g. "pr" -> "প্র", "tr" -> "ত্র")
            val singleMatch = matchConsonant(remaining)
            if (singleMatch != null) {
                val afterCons = i + singleMatch.first.length
                if (afterCons < len && (input[afterCons] == 'r' || input[afterCons] == 'R') && (afterCons + 1 >= len || isVowel(input[afterCons + 1]))) {
                    sb.append(singleMatch.second)
                    sb.append("্র") // Ro-phola
                    i = afterCons + 1
                    prevWasConsonant = true
                    continue
                }
                // Ja-phola: Consonant + 'y' or 'z' (e.g. "ky" -> "ক্য", "ty" -> "ত্য")
                if (afterCons < len && (input[afterCons] == 'y' || input[afterCons] == 'z' || input[afterCons] == 'Y')) {
                    sb.append(singleMatch.second)
                    sb.append("্য") // Ja-phola
                    i = afterCons + 1
                    prevWasConsonant = true
                    continue
                }
                // Ba-phola: Consonant + 'w' (e.g. "dw" -> "দ্ব", "sw" -> "স্ব")
                if (afterCons < len && (input[afterCons] == 'w' || input[afterCons] == 'W')) {
                    sb.append(singleMatch.second)
                    sb.append("্ব") // Ba-phola
                    i = afterCons + 1
                    prevWasConsonant = true
                    continue
                }

                sb.append(singleMatch.second)
                i += singleMatch.first.length
                prevWasConsonant = true
                continue
            }

            // Default fallback
            sb.append(firstChar)
            i++
            prevWasConsonant = false
        }

        return sb.toString()
    }

    private fun matchConsonant(str: String): Pair<String, String>? {
        for ((k, v) in SINGLE_CONSONANTS) {
            if (str.startsWith(k, ignoreCase = (k.length > 1 || k.lowercase() == k))) {
                return k to v
            }
        }
        return null
    }

    private fun isVowel(c: Char): Boolean {
        return c in "aeiouAEIOUrR"
    }

    private fun isConsonant(c: Char): Boolean {
        return c.isLetter() && !isVowel(c)
    }

    /**
     * Generates a ranked list of candidate Bangla predictions for an active Avro composing buffer.
     * e.g., "ami" -> ["আমি", "অমি", "আমিই"]
     */
    fun getCandidates(rawInput: String): List<String> {
        if (rawInput.isBlank()) return emptyList()

        val lower = rawInput.lowercase().trim()
        val list = mutableListOf<String>()

        // 1. Direct dictionary candidates
        COMMON_DICTIONARY[lower]?.let {
            list.addAll(it)
        }

        // 2. Grammar-parsed primary candidate
        val parsed = parse(rawInput)
        if (!list.contains(parsed)) {
            list.add(0, parsed)
        }

        // 3. Prefix matching from dictionary for partial completions
        if (rawInput.length in 2..5) {
            COMMON_DICTIONARY.keys.filter { it.startsWith(lower) && it != lower }.take(3).forEach { key ->
                COMMON_DICTIONARY[key]?.firstOrNull()?.let { cand ->
                    if (!list.contains(cand)) {
                        list.add(cand)
                    }
                }
            }
        }

        return list.distinct().take(5)
    }
}
