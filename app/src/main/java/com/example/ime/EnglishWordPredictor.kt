package com.example.ime

object EnglishWordPredictor {
    private val dictionary = listOf(
        "the", "be", "to", "of", "and", "a", "in", "that", "have", "i", "it", "for", "not", "on", "with", "he",
        "as", "you", "do", "at", "this", "but", "his", "by", "from", "they", "we", "say", "her", "she", "or",
        "an", "will", "my", "one", "all", "would", "there", "their", "what", "so", "up", "out", "if", "about",
        "who", "get", "which", "go", "me", "when", "make", "can", "like", "time", "no", "just", "him", "know",
        "take", "people", "into", "year", "your", "good", "some", "could", "them", "see", "other", "than", "then",
        "now", "look", "only", "come", "its", "over", "think", "also", "back", "after", "use", "two", "how", "our",
        "work", "first", "well", "way", "even", "new", "want", "because", "any", "these", "give", "day", "most",
        "us", "great", "hello", "thanks", "thank", "welcome", "please", "sorry", "phone", "number", "today",
        "tomorrow", "tonight", "morning", "afternoon", "evening", "night", "happy", "birthday", "friend", "family",
        "house", "home", "school", "college", "office", "meeting", "problem", "question", "answer", "water", "food",
        "dinner", "lunch", "breakfast", "help", "need", "call", "message", "text", "send", "receive", "email",
        "address", "check", "confirm", "done", "fine", "cool", "super", "awesome", "perfect", "ready", "start",
        "finish", "again", "always", "never", "sometimes", "maybe", "really", "sure", "ok", "okay", "yes", "yeah",
        "yep", "wait", "soon", "later", "where", "why", "here", "there", "right", "left", "keep", "let", "feel",
        "try", "leave", "show", "hear", "play", "run", "move", "live", "believe", "bring", "happen", "write",
        "provide", "sit", "stand", "lose", "pay", "meet", "include", "continue", "set", "learn", "change", "lead",
        "understand", "watch", "follow", "stop", "create", "speak", "read", "allow", "add", "spend", "grow", "open",
        "walk", "win", "offer", "remember", "love", "consider", "appear", "buy", "wait", "serve", "die", "send",
        "expect", "build", "stay", "fall", "cut", "reach", "kill", "remain", "suggest", "raise", "pass", "sell",
        "require", "report", "decide", "pull", "hope", "develop", "explain", "carry", "break", "thank", "receive",
        "agree", "support", "hit", "produce", "eat", "cover", "catch", "draw", "choose", "beautiful", "important"
    )

    fun predict(prefix: String, limit: Int = 3): List<String> {
        val trimmed = prefix.trim()
        if (trimmed.length < 2) return emptyList()

        val lower = trimmed.lowercase()
        val matches = dictionary.filter { it.startsWith(lower) && it != lower }
            .take(limit)

        if (matches.isEmpty()) return emptyList()

        val isAllUpper = trimmed.all { it.isUpperCase() }
        val isCapitalized = trimmed[0].isUpperCase()

        return matches.map { word ->
            when {
                isAllUpper -> word.uppercase()
                isCapitalized -> word.replaceFirstChar { it.uppercase() }
                else -> word
            }
        }
    }
}
