package com.example.ui.keyboard

data class StickerItem(
    val id: String,
    val title: String,
    val category: String,
    val url: String,
    val previewEmoji: String,
    val tags: List<String>
)

data class StickerCategory(
    val title: String,
    val icon: String,
    val stickers: List<StickerItem>
)

data class GifItem(
    val id: String,
    val title: String,
    val category: String,
    val url: String,
    val previewUrl: String = url,
    val tags: List<String>
)

data class GifCategory(
    val title: String,
    val icon: String,
    val gifs: List<GifItem>
)

object MediaCatalog {

    val trendingStickers = listOf(
        StickerItem("s1", "Cool Doge", "Trending", "https://images.unsplash.com/photo-1583511655857-d19b40a7a54e?w=200&auto=format&fit=crop&q=80", "🐶", listOf("doge", "cool", "shiba", "sunglasses", "meme")),
        StickerItem("s2", "Popcorn Time", "Trending", "https://images.unsplash.com/photo-1578849278619-e73505e9610f?w=200&auto=format&fit=crop&q=80", "🍿", listOf("popcorn", "movie", "drama", "eating", "watching")),
        StickerItem("s3", "Mind Blown", "Trending", "https://images.unsplash.com/photo-1509198397868-475647b2a1e5?w=200&auto=format&fit=crop&q=80", "🤯", listOf("mindblown", "shock", "brain", "omg", "crazy")),
        StickerItem("s4", "Thumbs Up Bro", "Trending", "https://images.unsplash.com/photo-1544717305-2782549b5136?w=200&auto=format&fit=crop&q=80", "👍", listOf("thumbsup", "good", "agree", "yes", "nice")),
        StickerItem("s5", "Dancing Cat", "Trending", "https://images.unsplash.com/photo-1514888286974-6c03e2ca1dba?w=200&auto=format&fit=crop&q=80", "🐱", listOf("cat", "dance", "happy", "party", "cute")),
        StickerItem("s6", "Fire Lit", "Trending", "https://images.unsplash.com/photo-1542601906990-b4d3fb778b09?w=200&auto=format&fit=crop&q=80", "🔥", listOf("fire", "lit", "hot", "trending", "cool")),
        StickerItem("s7", "Facepalm", "Trending", "https://images.unsplash.com/photo-1534528741775-53994a69daeb?w=200&auto=format&fit=crop&q=80", "🤦", listOf("facepalm", "smh", "duh", "oops", "error")),
        StickerItem("s8", "Heart Spark", "Trending", "https://images.unsplash.com/photo-1518199266791-5375a83190b7?w=200&auto=format&fit=crop&q=80", "💖", listOf("heart", "love", "pink", "sparkle", "cute")),
        StickerItem("s9", "Party Time", "Trending", "https://images.unsplash.com/photo-1513151233558-d860c5398176?w=200&auto=format&fit=crop&q=80", "🎉", listOf("party", "celebrate", "confetti", "congrats", "fun")),
        StickerItem("s10", "Gamer Win", "Trending", "https://images.unsplash.com/photo-1550745165-9bc0b252726f?w=200&auto=format&fit=crop&q=80", "🎮", listOf("gaming", "gg", "winner", "play", "victory"))
    )

    val memeStickers = listOf(
        StickerItem("m1", "Deal With It", "Memes", "https://images.unsplash.com/photo-1508214751196-bcfd4ca60f91?w=200&auto=format&fit=crop&q=80", "😎", listOf("dealwithit", "glasses", "boss", "cool", "swag")),
        StickerItem("m2", "Think About It", "Memes", "https://images.unsplash.com/photo-1492562080023-ab3db95bfbce?w=200&auto=format&fit=crop&q=80", "🧠", listOf("think", "smart", "brain", "idea", "rollsafe")),
        StickerItem("m3", "This Is Fine", "Memes", "https://images.unsplash.com/photo-1525268771113-32d9e9021a97?w=200&auto=format&fit=crop&q=80", "☕", listOf("thisisfine", "fire", "dog", "okay", "chaos")),
        StickerItem("m4", "Gigachad Energy", "Memes", "https://images.unsplash.com/photo-1534528741775-53994a69daeb?w=200&auto=format&fit=crop&q=80", "🗿", listOf("gigachad", "chad", "sigma", "strong", "alpha")),
        StickerItem("m5", "Suspicious Fry", "Memes", "https://images.unsplash.com/photo-1507003211169-0a1dd7228f2d?w=200&auto=format&fit=crop&q=80", "🤨", listOf("suspicious", "doubt", "fry", "not_sure", "hmmm")),
        StickerItem("m6", "Shocked Pikachu", "Memes", "https://images.unsplash.com/photo-1518791841217-8f162f1e1131?w=200&auto=format&fit=crop&q=80", "😮", listOf("pikachu", "shocked", "surprised", "wow", "anime")),
        StickerItem("m7", "Confused Math", "Memes", "https://images.unsplash.com/photo-1509228468518-180dd4864904?w=200&auto=format&fit=crop&q=80", "📐", listOf("confused", "math", "calculating", "what", "thinking")),
        StickerItem("m8", "Stonks Up", "Memes", "https://images.unsplash.com/photo-1611974789855-9c2a0a7236a3?w=200&auto=format&fit=crop&q=80", "📈", listOf("stonks", "profit", "money", "up", "success"))
    )

    val cutePetsStickers = listOf(
        StickerItem("p1", "Happy Shiba", "Cute Pets", "https://images.unsplash.com/photo-1583511655857-d19b40a7a54e?w=200&auto=format&fit=crop&q=80", "🐕", listOf("shiba", "dog", "happy", "puppy", "smile")),
        StickerItem("p2", "Sleepy Kitten", "Cute Pets", "https://images.unsplash.com/photo-1514888286974-6c03e2ca1dba?w=200&auto=format&fit=crop&q=80", "🐱", listOf("cat", "sleepy", "nap", "cute", "kitten")),
        StickerItem("p3", "Fluffy Bunny", "Cute Pets", "https://images.unsplash.com/photo-1585110396000-c9ffd4e4b308?w=200&auto=format&fit=crop&q=80", "🐰", listOf("rabbit", "bunny", "hop", "easter", "cute")),
        StickerItem("p4", "Hungry Hamster", "Cute Pets", "https://images.unsplash.com/photo-1425082661705-1834bfd09dca?w=200&auto=format&fit=crop&q=80", "🐹", listOf("hamster", "food", "snack", "eat", "chubby")),
        StickerItem("p5", "Playful Pug", "Cute Pets", "https://images.unsplash.com/photo-1517849845537-4d257902454a?w=200&auto=format&fit=crop&q=80", "🐶", listOf("pug", "dog", "play", "silly", "funny")),
        StickerItem("p6", "Dancing Panda", "Cute Pets", "https://images.unsplash.com/photo-1564349683136-77e08dba1ef7?w=200&auto=format&fit=crop&q=80", "🐼", listOf("panda", "dance", "bamboo", "happy", "bear"))
    )

    val loveStickers = listOf(
        StickerItem("l1", "Heart Hug", "Love", "https://images.unsplash.com/photo-1518199266791-5375a83190b7?w=200&auto=format&fit=crop&q=80", "🤗", listOf("hug", "love", "heart", "cuddle", "sweet")),
        StickerItem("l2", "Flying Kiss", "Love", "https://images.unsplash.com/photo-1516589178581-6cd7833ae3b2?w=200&auto=format&fit=crop&q=80", "😘", listOf("kiss", "blowkiss", "romantic", "affection", "love")),
        StickerItem("l3", "Couple Teddy", "Love", "https://images.unsplash.com/photo-1559454403-b8fb88521f11?w=200&auto=format&fit=crop&q=80", "🧸", listOf("teddy", "couple", "together", "cute", "warm")),
        StickerItem("l4", "Love Letter", "Love", "https://images.unsplash.com/photo-1579208575657-c595a05383b7?w=200&auto=format&fit=crop&q=80", "💌", listOf("letter", "message", "secret", "valentine", "heart")),
        StickerItem("l5", "You Rock", "Love", "https://images.unsplash.com/photo-1529156069898-49953e39b3ac?w=200&auto=format&fit=crop&q=80", "🤘", listOf("yourock", "best", "appreciation", "stars", "awesome"))
    )

    val stickerCategories = listOf(
        StickerCategory("Trending", "🔥", trendingStickers),
        StickerCategory("Memes", "🐸", memeStickers),
        StickerCategory("Cute Pets", "🐾", cutePetsStickers),
        StickerCategory("Love", "❤️", loveStickers)
    )

    val allStickers = (trendingStickers + memeStickers + cutePetsStickers + loveStickers).distinctBy { it.id }

    // Curated Trending GIFs (Reliable high-speed animated WebP/GIF assets)
    val trendingGifs = listOf(
        GifItem("g1", "Happy Dance", "Trending", "https://media.giphy.com/media/blSTtZehjAZ8I/giphy.gif", "https://media.giphy.com/media/blSTtZehjAZ8I/200w.gif", listOf("dance", "happy", "party", "excited", "fun")),
        GifItem("g2", "Laughing Out Loud", "Trending", "https://media.giphy.com/media/10JhviFuU2gWD6/giphy.gif", "https://media.giphy.com/media/10JhviFuU2gWD6/200w.gif", listOf("laugh", "lol", "funny", "haha", "rofl")),
        GifItem("g3", "Cat Vibing", "Trending", "https://media.giphy.com/media/jpbnoe3UIa8TU8LM13/giphy.gif", "https://media.giphy.com/media/jpbnoe3UIa8TU8LM13/200w.gif", listOf("cat", "vibe", "music", "nodding", "meme")),
        GifItem("g4", "Clapping Applause", "Trending", "https://media.giphy.com/media/BPJmthQ3YRwD6QqcVD/giphy.gif", "https://media.giphy.com/media/BPJmthQ3YRwD6QqcVD/200w.gif", listOf("clap", "cheers", "leonardo", "gatsby", "bravo")),
        GifItem("g5", "Yes Nodding", "Trending", "https://media.giphy.com/media/3o7abKhOpu0NwenH3O/giphy.gif", "https://media.giphy.com/media/3o7abKhOpu0NwenH3O/200w.gif", listOf("yes", "agree", "nod", "correct", "sure")),
        GifItem("g6", "Mind Blown Galaxy", "Trending", "https://media.giphy.com/media/26ufdipQqU2lhNA4g/giphy.gif", "https://media.giphy.com/media/26ufdipQqU2lhNA4g/200w.gif", listOf("mindblown", "space", "galaxy", "woah", "shock")),
        GifItem("g7", "Popcorn Michael", "Trending", "https://media.giphy.com/media/pUeXcg80cO8I8/giphy.gif", "https://media.giphy.com/media/pUeXcg80cO8I8/200w.gif", listOf("popcorn", "michaeljackson", "thriller", "drama", "watching")),
        GifItem("g8", "Thumbs Up Chuck", "Trending", "https://media.giphy.com/media/3o7TKqFZJpwL8Git28/giphy.gif", "https://media.giphy.com/media/3o7TKqFZJpwL8Git28/200w.gif", listOf("thumbsup", "approved", "ok", "great", "cool"))
    )

    val reactionGifs = listOf(
        GifItem("r1", "Excited Screaming", "Reactions", "https://media.giphy.com/media/5GoVLqeAOo6PK/giphy.gif", "https://media.giphy.com/media/5GoVLqeAOo6PK/200w.gif", listOf("excited", "screaming", "happy", "yes", "omg")),
        GifItem("r2", "Facepalm Picard", "Reactions", "https://media.giphy.com/media/XsUtdIeJ0MWMo/giphy.gif", "https://media.giphy.com/media/XsUtdIeJ0MWMo/200w.gif", listOf("facepalm", "picard", "sigh", "smh", "disappointed")),
        GifItem("r3", "Shocked Steve Carell", "Reactions", "https://media.giphy.com/media/vyTnNTrs3wqQ0UIvwE/giphy.gif", "https://media.giphy.com/media/vyTnNTrs3wqQ0UIvwE/200w.gif", listOf("no", "godno", "michaelscott", "office", "screaming")),
        GifItem("r4", "Suspicious Fry", "Reactions", "https://media.giphy.com/media/ANbD1CCdA3iI8/giphy.gif", "https://media.giphy.com/media/ANbD1CCdA3iI8/200w.gif", listOf("suspicious", "fry", "futurama", "hmmm", "doubt")),
        GifItem("r5", "Confused Travolta", "Reactions", "https://media.giphy.com/media/g01ZnwAUvutuK8GIQn/giphy.gif", "https://media.giphy.com/media/g01ZnwAUvutuK8GIQn/200w.gif", listOf("confused", "travolta", "lost", "where", "what")),
        GifItem("r6", "Roll Safe Think", "Reactions", "https://media.giphy.com/media/d3mlE7uhX8KFgEmY/giphy.gif", "https://media.giphy.com/media/d3mlE7uhX8KFgEmY/200w.gif", listOf("think", "smart", "brain", "bigbrain", "rollsafe"))
    )

    val emotionGifs = listOf(
        GifItem("e1", "Love Sending Kiss", "Love", "https://media.giphy.com/media/l4pTdcifPZLpDjL1e/giphy.gif", "https://media.giphy.com/media/l4pTdcifPZLpDjL1e/200w.gif", listOf("love", "kiss", "heart", "romantic", "missyou")),
        GifItem("e2", "Big Warm Hug", "Love", "https://media.giphy.com/media/l8ooT552bifIVU25dI/giphy.gif", "https://media.giphy.com/media/l8ooT552bifIVU25dI/200w.gif", listOf("hug", "warm", "cuddle", "together", "friend")),
        GifItem("e3", "Crying Rain", "Sad", "https://media.giphy.com/media/OPU6wzx8JrHna/giphy.gif", "https://media.giphy.com/media/OPU6wzx8JrHna/200w.gif", listOf("cry", "sad", "tears", "rain", "heartbroken")),
        GifItem("e4", "Happy Celebration", "Party", "https://media.giphy.com/media/artj92V8o75VPL7AeQ/giphy.gif", "https://media.giphy.com/media/artj92V8o75VPL7AeQ/200w.gif", listOf("party", "celebrate", "beer", "cheers", "friends"))
    )

    val gifCategories = listOf(
        GifCategory("Trending", "🔥", trendingGifs),
        GifCategory("Reactions", "😲", reactionGifs),
        GifCategory("Love & Warm", "💖", emotionGifs)
    )

    val allGifs = (trendingGifs + reactionGifs + emotionGifs).distinctBy { it.id }
}
