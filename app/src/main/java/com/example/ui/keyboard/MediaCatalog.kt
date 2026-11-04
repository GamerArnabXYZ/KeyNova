package com.example.ui.keyboard

data class StickerItem(
    val id: String,
    val title: String,
    val category: String,
    val url: String,
    val previewEmoji: String,
    val tags: List<String>,
    val previewUrl: String = url
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

    // ==========================================
    // STICKERS (Categorized, High-Quality Assets)
    // ==========================================

    val trendingStickers = listOf(
        StickerItem("s1", "Cool Doge", "Trending", "https://images.unsplash.com/photo-1583511655857-d19b40a7a54e?w=260&auto=format&fit=crop&q=80", "🐶", listOf("doge", "cool", "shiba", "sunglasses", "meme", "trending", "dog")),
        StickerItem("s2", "Popcorn Time", "Trending", "https://images.unsplash.com/photo-1578849278619-e73505e9610f?w=260&auto=format&fit=crop&q=80", "🍿", listOf("popcorn", "movie", "drama", "eating", "watching", "trending")),
        StickerItem("s3", "Mind Blown", "Trending", "https://images.unsplash.com/photo-1509198397868-475647b2a1e5?w=260&auto=format&fit=crop&q=80", "🤯", listOf("mindblown", "shock", "brain", "omg", "crazy", "wow", "trending")),
        StickerItem("s4", "Thumbs Up Bro", "Trending", "https://images.unsplash.com/photo-1544717305-2782549b5136?w=260&auto=format&fit=crop&q=80", "👍", listOf("thumbsup", "good", "agree", "yes", "nice", "ok", "trending")),
        StickerItem("s5", "Dancing Cat", "Trending", "https://images.unsplash.com/photo-1514888286974-6c03e2ca1dba?w=260&auto=format&fit=crop&q=80", "🐱", listOf("cat", "dance", "happy", "party", "cute", "pet", "trending")),
        StickerItem("s6", "Fire Lit", "Trending", "https://images.unsplash.com/photo-1542601906990-b4d3fb778b09?w=260&auto=format&fit=crop&q=80", "🔥", listOf("fire", "lit", "hot", "trending", "cool", "flame")),
        StickerItem("s7", "Facepalm", "Trending", "https://images.unsplash.com/photo-1534528741775-53994a69daeb?w=260&auto=format&fit=crop&q=80", "🤦", listOf("facepalm", "smh", "duh", "oops", "error", "trending")),
        StickerItem("s8", "Heart Spark", "Trending", "https://images.unsplash.com/photo-1518199266791-5375a83190b7?w=260&auto=format&fit=crop&q=80", "💖", listOf("heart", "love", "pink", "sparkle", "cute", "trending")),
        StickerItem("s9", "Party Confetti", "Trending", "https://images.unsplash.com/photo-1513151233558-d860c5398176?w=260&auto=format&fit=crop&q=80", "🎉", listOf("party", "celebrate", "confetti", "congrats", "fun", "trending")),
        StickerItem("s10", "Gamer Win", "Trending", "https://images.unsplash.com/photo-1550745165-9bc0b252726f?w=260&auto=format&fit=crop&q=80", "🎮", listOf("gaming", "gg", "winner", "play", "victory", "controller", "trending")),
        StickerItem("s11", "Coffee Boost", "Trending", "https://images.unsplash.com/photo-1509042239860-f550ce710b93?w=260&auto=format&fit=crop&q=80", "☕", listOf("coffee", "morning", "caffeine", "latte", "energy", "work", "trending")),
        StickerItem("s12", "Rocket Blast", "Trending", "https://images.unsplash.com/photo-1517976487507-5b3b4b371f73?w=260&auto=format&fit=crop&q=80", "🚀", listOf("rocket", "moon", "launch", "fast", "space", "crypto", "trending"))
    )

    val memeStickers = listOf(
        StickerItem("m1", "Deal With It", "Memes", "https://images.unsplash.com/photo-1508214751196-bcfd4ca60f91?w=260&auto=format&fit=crop&q=80", "😎", listOf("dealwithit", "glasses", "boss", "cool", "swag", "meme")),
        StickerItem("m2", "Think About It", "Memes", "https://images.unsplash.com/photo-1492562080023-ab3db95bfbce?w=260&auto=format&fit=crop&q=80", "🧠", listOf("think", "smart", "brain", "idea", "rollsafe", "meme")),
        StickerItem("m3", "This Is Fine", "Memes", "https://images.unsplash.com/photo-1525268771113-32d9e9021a97?w=260&auto=format&fit=crop&q=80", "☕", listOf("thisisfine", "fire", "dog", "okay", "chaos", "meme")),
        StickerItem("m4", "Gigachad Energy", "Memes", "https://images.unsplash.com/photo-1534528741775-53994a69daeb?w=260&auto=format&fit=crop&q=80", "🗿", listOf("gigachad", "chad", "sigma", "strong", "alpha", "meme")),
        StickerItem("m5", "Suspicious Fry", "Memes", "https://images.unsplash.com/photo-1507003211169-0a1dd7228f2d?w=260&auto=format&fit=crop&q=80", "🤨", listOf("suspicious", "doubt", "fry", "not_sure", "hmmm", "meme")),
        StickerItem("m6", "Shocked Cat", "Memes", "https://images.unsplash.com/photo-1518791841217-8f162f1e1131?w=260&auto=format&fit=crop&q=80", "😮", listOf("shocked", "surprised", "wow", "omg", "cat", "meme")),
        StickerItem("m7", "Confused Math", "Memes", "https://images.unsplash.com/photo-1509228468518-180dd4864904?w=260&auto=format&fit=crop&q=80", "📐", listOf("confused", "math", "calculating", "what", "thinking", "meme")),
        StickerItem("m8", "Stonks Up", "Memes", "https://images.unsplash.com/photo-1611974789855-9c2a0a7236a3?w=260&auto=format&fit=crop&q=80", "📈", listOf("stonks", "profit", "money", "up", "success", "meme")),
        StickerItem("m9", "Doge Wow", "Memes", "https://images.unsplash.com/photo-1583511655857-d19b40a7a54e?w=260&auto=format&fit=crop&q=80", "🐕", listOf("doge", "wow", "shiba", "much", "very", "meme")),
        StickerItem("m10", "Salt Bae", "Memes", "https://images.unsplash.com/photo-1555396273-367ea4eb4db5?w=260&auto=format&fit=crop&q=80", "🧂", listOf("salt", "season", "chef", "style", "swag", "meme"))
    )

    val cutePetsStickers = listOf(
        StickerItem("p1", "Happy Shiba", "Cute Pets", "https://images.unsplash.com/photo-1583511655857-d19b40a7a54e?w=260&auto=format&fit=crop&q=80", "🐕", listOf("shiba", "dog", "happy", "puppy", "smile", "pet")),
        StickerItem("p2", "Sleepy Kitten", "Cute Pets", "https://images.unsplash.com/photo-1514888286974-6c03e2ca1dba?w=260&auto=format&fit=crop&q=80", "🐱", listOf("cat", "sleepy", "nap", "cute", "kitten", "pet")),
        StickerItem("p3", "Fluffy Bunny", "Cute Pets", "https://images.unsplash.com/photo-1585110396000-c9ffd4e4b308?w=260&auto=format&fit=crop&q=80", "🐰", listOf("rabbit", "bunny", "hop", "easter", "cute", "pet")),
        StickerItem("p4", "Hungry Hamster", "Cute Pets", "https://images.unsplash.com/photo-1425082661705-1834bfd09dca?w=260&auto=format&fit=crop&q=80", "🐹", listOf("hamster", "food", "snack", "eat", "chubby", "pet")),
        StickerItem("p5", "Playful Pug", "Cute Pets", "https://images.unsplash.com/photo-1517849845537-4d257902454a?w=260&auto=format&fit=crop&q=80", "🐶", listOf("pug", "dog", "play", "silly", "funny", "pet")),
        StickerItem("p6", "Dancing Panda", "Cute Pets", "https://images.unsplash.com/photo-1564349683136-77e08dba1ef7?w=260&auto=format&fit=crop&q=80", "🐼", listOf("panda", "dance", "bamboo", "happy", "bear", "pet")),
        StickerItem("p7", "Golden Retriever Smile", "Cute Pets", "https://images.unsplash.com/photo-1552053831-71594a27632d?w=260&auto=format&fit=crop&q=80", "🦮", listOf("golden", "retriever", "dog", "happy", "cute", "pet")),
        StickerItem("p8", "Baby Hedgehog", "Cute Pets", "https://images.unsplash.com/photo-1563281577-a7be47e20db9?w=260&auto=format&fit=crop&q=80", "🦔", listOf("hedgehog", "tiny", "cute", "baby", "spiky", "pet")),
        StickerItem("p9", "Curious Fox", "Cute Pets", "https://images.unsplash.com/photo-1516934024742-b461fba47600?w=260&auto=format&fit=crop&q=80", "🦊", listOf("fox", "wild", "cute", "orange", "smart", "pet")),
        StickerItem("p10", "Playful Otter", "Cute Pets", "https://images.unsplash.com/photo-1549488344-cbb6c34cf08b?w=260&auto=format&fit=crop&q=80", "🦦", listOf("otter", "water", "play", "cute", "swim", "pet"))
    )

    val loveStickers = listOf(
        StickerItem("l1", "Heart Hug", "Love", "https://images.unsplash.com/photo-1518199266791-5375a83190b7?w=260&auto=format&fit=crop&q=80", "🤗", listOf("hug", "love", "heart", "cuddle", "sweet")),
        StickerItem("l2", "Flying Kiss", "Love", "https://images.unsplash.com/photo-1516589178581-6cd7833ae3b2?w=260&auto=format&fit=crop&q=80", "😘", listOf("kiss", "blowkiss", "romantic", "affection", "love")),
        StickerItem("l3", "Couple Teddy", "Love", "https://images.unsplash.com/photo-1559454403-b8fb88521f11?w=260&auto=format&fit=crop&q=80", "🧸", listOf("teddy", "couple", "together", "cute", "warm", "love")),
        StickerItem("l4", "Love Letter", "Love", "https://images.unsplash.com/photo-1579208575657-c595a05383b7?w=260&auto=format&fit=crop&q=80", "💌", listOf("letter", "message", "secret", "valentine", "heart", "love")),
        StickerItem("l5", "You Rock", "Love", "https://images.unsplash.com/photo-1529156069898-49953e39b3ac?w=260&auto=format&fit=crop&q=80", "🤘", listOf("yourock", "best", "appreciation", "stars", "awesome", "love")),
        StickerItem("l6", "Rose Bouquet", "Love", "https://images.unsplash.com/photo-1518709268805-4e9042af9f23?w=260&auto=format&fit=crop&q=80", "🌹", listOf("rose", "flowers", "romantic", "valentine", "red", "love")),
        StickerItem("l7", "Forever Together", "Love", "https://images.unsplash.com/photo-1522673607200-164d1b6ce486?w=260&auto=format&fit=crop&q=80", "💑", listOf("together", "holdinghands", "couple", "forever", "love")),
        StickerItem("l8", "Cute Blush", "Love", "https://images.unsplash.com/photo-1534528741775-53994a69daeb?w=260&auto=format&fit=crop&q=80", "😳", listOf("blush", "shy", "cute", "sweet", "crush", "love"))
    )

    val gamingStickers = listOf(
        StickerItem("g1", "Pixel Sword", "Gaming", "https://images.unsplash.com/photo-1550745165-9bc0b252726f?w=260&auto=format&fit=crop&q=80", "⚔️", listOf("sword", "pixel", "rpg", "battle", "gaming")),
        StickerItem("g2", "Retro Arcade", "Gaming", "https://images.unsplash.com/photo-1511512578047-dfb367046420?w=260&auto=format&fit=crop&q=80", "🕹️", listOf("arcade", "retro", "8bit", "joystick", "gaming")),
        StickerItem("g3", "Victory Royale", "Gaming", "https://images.unsplash.com/photo-1542751371-adc38448a05e?w=260&auto=format&fit=crop&q=80", "🏆", listOf("victory", "win", "champion", "fortnite", "gaming")),
        StickerItem("g4", "Headset Pro", "Gaming", "https://images.unsplash.com/photo-1546435770-a3e426bf472b?w=260&auto=format&fit=crop&q=80", "🎧", listOf("headset", "audio", "gamer", "discord", "gaming")),
        StickerItem("g5", "Level Up", "Gaming", "https://images.unsplash.com/photo-1518709268805-4e9042af9f23?w=260&auto=format&fit=crop&q=80", "🆙", listOf("levelup", "xp", "boost", "rankup", "gaming")),
        StickerItem("g6", "Game Over", "Gaming", "https://images.unsplash.com/photo-1550745165-9bc0b252726f?w=260&auto=format&fit=crop&q=80", "💀", listOf("gameover", "defeat", "respawn", "retry", "gaming"))
    )

    val foodStickers = listOf(
        StickerItem("f1", "Pizza Slice", "Food", "https://images.unsplash.com/photo-1513104890138-7c749659a591?w=260&auto=format&fit=crop&q=80", "🍕", listOf("pizza", "cheese", "slice", "delicious", "food")),
        StickerItem("f2", "Juicy Burger", "Food", "https://images.unsplash.com/photo-1568901346375-23c9450c58cd?w=260&auto=format&fit=crop&q=80", "🍔", listOf("burger", "fastfood", "fries", "tasty", "food")),
        StickerItem("f3", "Sweet Donut", "Food", "https://images.unsplash.com/photo-1527515637462-cff94eecc1ac?w=260&auto=format&fit=crop&q=80", "🍩", listOf("donut", "sweet", "chocolate", "dessert", "food")),
        StickerItem("f4", "Ice Cream Cone", "Food", "https://images.unsplash.com/photo-1497034825429-c343d7c6a68f?w=260&auto=format&fit=crop&q=80", "🍦", listOf("icecream", "cone", "summer", "sweet", "food")),
        StickerItem("f5", "Ramen Bowl", "Food", "https://images.unsplash.com/photo-1569718212165-3a8278d5f624?w=260&auto=format&fit=crop&q=80", "🍜", listOf("ramen", "noodles", "soup", "japanese", "food")),
        StickerItem("f6", "Boba Milk Tea", "Food", "https://images.unsplash.com/photo-1558857563-b37cf5a75db1?w=260&auto=format&fit=crop&q=80", "🧋", listOf("boba", "milktea", "tapioca", "drink", "food"))
    )

    val stickerCategories = listOf(
        StickerCategory("Trending", "🔥", trendingStickers),
        StickerCategory("Memes", "🐸", memeStickers),
        StickerCategory("Cute Pets", "🐾", cutePetsStickers),
        StickerCategory("Love & Sweet", "❤️", loveStickers),
        StickerCategory("Gaming", "🎮", gamingStickers),
        StickerCategory("Food & Treats", "🍔", foodStickers)
    )

    val allStickers = (trendingStickers + memeStickers + cutePetsStickers + loveStickers + gamingStickers + foodStickers).distinctBy { it.id }


    // ==========================================
    // GIFS (High-Speed Curated Animated Media)
    // ==========================================

    val trendingGifs = listOf(
        GifItem("g1", "Happy Dance", "Trending", "https://media.giphy.com/media/blSTtZehjAZ8I/giphy.gif", "https://media.giphy.com/media/blSTtZehjAZ8I/200w.gif", listOf("dance", "happy", "party", "excited", "fun", "trending")),
        GifItem("g2", "Laughing Out Loud", "Trending", "https://media.giphy.com/media/10JhviFuU2gWD6/giphy.gif", "https://media.giphy.com/media/10JhviFuU2gWD6/200w.gif", listOf("laugh", "lol", "funny", "haha", "rofl", "trending")),
        GifItem("g3", "Cat Vibing", "Trending", "https://media.giphy.com/media/jpbnoe3UIa8TU8LM13/giphy.gif", "https://media.giphy.com/media/jpbnoe3UIa8TU8LM13/200w.gif", listOf("cat", "vibe", "music", "nodding", "meme", "pet", "trending")),
        GifItem("g4", "Clapping Applause", "Trending", "https://media.giphy.com/media/BPJmthQ3YRwD6QqcVD/giphy.gif", "https://media.giphy.com/media/BPJmthQ3YRwD6QqcVD/200w.gif", listOf("clap", "cheers", "leonardo", "gatsby", "bravo", "trending")),
        GifItem("g5", "Yes Nodding", "Trending", "https://media.giphy.com/media/3o7abKhOpu0NwenH3O/giphy.gif", "https://media.giphy.com/media/3o7abKhOpu0NwenH3O/200w.gif", listOf("yes", "agree", "nod", "correct", "sure", "trending")),
        GifItem("g6", "Mind Blown Galaxy", "Trending", "https://media.giphy.com/media/26ufdipQqU2lhNA4g/giphy.gif", "https://media.giphy.com/media/26ufdipQqU2lhNA4g/200w.gif", listOf("mindblown", "space", "galaxy", "woah", "shock", "trending")),
        GifItem("g7", "Popcorn Michael", "Trending", "https://media.giphy.com/media/pUeXcg80cO8I8/giphy.gif", "https://media.giphy.com/media/pUeXcg80cO8I8/200w.gif", listOf("popcorn", "michaeljackson", "thriller", "drama", "watching", "trending")),
        GifItem("g8", "Thumbs Up Chuck", "Trending", "https://media.giphy.com/media/3o7TKqFZJpwL8Git28/giphy.gif", "https://media.giphy.com/media/3o7TKqFZJpwL8Git28/200w.gif", listOf("thumbsup", "approved", "ok", "great", "cool", "trending")),
        GifItem("g9", "Baby Dancing", "Trending", "https://media.giphy.com/media/14udF3WUwwGMaA/giphy.gif", "https://media.giphy.com/media/14udF3WUwwGMaA/200w.gif", listOf("dance", "baby", "cute", "happy", "party", "trending")),
        GifItem("g10", "Fire Lit", "Trending", "https://media.giphy.com/media/l0MYt5jPR6QX5pnqM/giphy.gif", "https://media.giphy.com/media/l0MYt5jPR6QX5pnqM/200w.gif", listOf("fire", "lit", "hot", "flame", "awesome", "trending"))
    )

    val reactionGifs = listOf(
        GifItem("r1", "Excited Screaming", "Reactions", "https://media.giphy.com/media/5GoVLqeAOo6PK/giphy.gif", "https://media.giphy.com/media/5GoVLqeAOo6PK/200w.gif", listOf("excited", "screaming", "happy", "yes", "omg", "reactions")),
        GifItem("r2", "Facepalm Picard", "Reactions", "https://media.giphy.com/media/XsUtdIeJ0MWMo/giphy.gif", "https://media.giphy.com/media/XsUtdIeJ0MWMo/200w.gif", listOf("facepalm", "picard", "sigh", "smh", "disappointed", "reactions")),
        GifItem("r3", "Shocked Steve Carell", "Reactions", "https://media.giphy.com/media/vyTnNTrs3wqQ0UIvwE/giphy.gif", "https://media.giphy.com/media/vyTnNTrs3wqQ0UIvwE/200w.gif", listOf("no", "godno", "michaelscott", "office", "screaming", "reactions")),
        GifItem("r4", "Suspicious Fry", "Reactions", "https://media.giphy.com/media/ANbD1CCdA3iI8/giphy.gif", "https://media.giphy.com/media/ANbD1CCdA3iI8/200w.gif", listOf("suspicious", "fry", "futurama", "hmmm", "doubt", "reactions")),
        GifItem("r5", "Confused Travolta", "Reactions", "https://media.giphy.com/media/g01ZnwAUvutuK8GIQn/giphy.gif", "https://media.giphy.com/media/g01ZnwAUvutuK8GIQn/200w.gif", listOf("confused", "travolta", "lost", "where", "what", "reactions")),
        GifItem("r6", "Roll Safe Think", "Reactions", "https://media.giphy.com/media/d3mlE7uhX8KFgEmY/giphy.gif", "https://media.giphy.com/media/d3mlE7uhX8KFgEmY/200w.gif", listOf("think", "smart", "brain", "bigbrain", "rollsafe", "reactions")),
        GifItem("r7", "Mind Blown Tim", "Reactions", "https://media.giphy.com/media/xT0xeJpnrWC4XWblEk/giphy.gif", "https://media.giphy.com/media/xT0xeJpnrWC4XWblEk/200w.gif", listOf("mindblown", "shock", "brain", "crazy", "reactions")),
        GifItem("r8", "Awkward Monkey Look", "Reactions", "https://media.giphy.com/media/cJMlRUCbMMZP3v96af/giphy.gif", "https://media.giphy.com/media/cJMlRUCbMMZP3v96af/200w.gif", listOf("awkward", "monkey", "lookaway", "oops", "guilty", "reactions"))
    )

    val laughGifs = listOf(
        GifItem("lf1", "Minion Laughing", "Laugh", "https://media.giphy.com/media/ZqlvCTNHpqrio/giphy.gif", "https://media.giphy.com/media/ZqlvCTNHpqrio/200w.gif", listOf("laugh", "lol", "minion", "haha", "funny", "rofl")),
        GifItem("lf2", "Spiderman Laughing", "Laugh", "https://media.giphy.com/media/enCWEo0vG25Ow/giphy.gif", "https://media.giphy.com/media/enCWEo0vG25Ow/200w.gif", listOf("laugh", "spiderman", "lol", "cackle", "funny", "haha")),
        GifItem("lf3", "Kangaroo Giggling", "Laugh", "https://media.giphy.com/media/lszAB3TzFtDxm/giphy.gif", "https://media.giphy.com/media/lszAB3TzFtDxm/200w.gif", listOf("laugh", "funny", "giggle", "chuckle", "rofl", "lol")),
        GifItem("lf4", "Dog Laughing", "Laugh", "https://media.giphy.com/media/10JhviFuU2gWD6/giphy.gif", "https://media.giphy.com/media/10JhviFuU2gWD6/200w.gif", listOf("dog", "laugh", "happy", "lol", "hilarious")),
        GifItem("lf5", "SpongeBob Chuckle", "Laugh", "https://media.giphy.com/media/3o7absbD7PbTFQa0c8/giphy.gif", "https://media.giphy.com/media/3o7absbD7PbTFQa0c8/200w.gif", listOf("spongebob", "laugh", "funny", "giggle", "lol", "haha")),
        GifItem("lf6", "Baby Giggle", "Laugh", "https://media.giphy.com/media/10t502xtCw026k/giphy.gif", "https://media.giphy.com/media/10t502xtCw026k/200w.gif", listOf("baby", "cute", "giggle", "laugh", "sweet", "lol"))
    )

    val danceGifs = listOf(
        GifItem("d1", "Snoopy Dance", "Dance", "https://media.giphy.com/media/o75ajIFH0QnQC3nCeD/giphy.gif", "https://media.giphy.com/media/o75ajIFH0QnQC3nCeD/200w.gif", listOf("dance", "snoopy", "party", "happy", "groove")),
        GifItem("d2", "Carlton Dance", "Dance", "https://media.giphy.com/media/pa37AAG5KXoek/giphy.gif", "https://media.giphy.com/media/pa37AAG5KXoek/200w.gif", listOf("carlton", "dance", "freshprince", "joy", "classic")),
        GifItem("d3", "Duck Dancing", "Dance", "https://media.giphy.com/media/v6aOjy0Qo1fIA/giphy.gif", "https://media.giphy.com/media/v6aOjy0Qo1fIA/200w.gif", listOf("duck", "dance", "cute", "waddle", "vibe", "music")),
        GifItem("d4", "Disco Fever", "Dance", "https://media.giphy.com/media/blSTtZehjAZ8I/giphy.gif", "https://media.giphy.com/media/blSTtZehjAZ8I/200w.gif", listOf("disco", "party", "dance", "groove", "saturday")),
        GifItem("d5", "Robot Dance", "Dance", "https://media.giphy.com/media/mIZ9rPe2PxVDO/giphy.gif", "https://media.giphy.com/media/mIZ9rPe2PxVDO/200w.gif", listOf("robot", "dance", "electronic", "fun", "moves")),
        GifItem("d6", "Celebration Shake", "Dance", "https://media.giphy.com/media/artj92V8o75VPL7AeQ/giphy.gif", "https://media.giphy.com/media/artj92V8o75VPL7AeQ/200w.gif", listOf("party", "shake", "dance", "celebrate", "cheers"))
    )

    val loveGifs = listOf(
        GifItem("e1", "Love Sending Kiss", "Love", "https://media.giphy.com/media/l4pTdcifPZLpDjL1e/giphy.gif", "https://media.giphy.com/media/l4pTdcifPZLpDjL1e/200w.gif", listOf("love", "kiss", "heart", "romantic", "missyou")),
        GifItem("e2", "Big Warm Hug", "Love", "https://media.giphy.com/media/l8ooT552bifIVU25dI/giphy.gif", "https://media.giphy.com/media/l8ooT552bifIVU25dI/200w.gif", listOf("hug", "warm", "cuddle", "together", "friend", "love")),
        GifItem("e3", "Panda Hearts", "Love", "https://media.giphy.com/media/MDJ9IbxxvDUQM/giphy.gif", "https://media.giphy.com/media/MDJ9IbxxvDUQM/200w.gif", listOf("cat", "panda", "heart", "cute", "love", "adorable")),
        GifItem("e4", "Sparkle Eyes", "Love", "https://media.giphy.com/media/26BRv0ThflsHCqDrG/giphy.gif", "https://media.giphy.com/media/26BRv0ThflsHCqDrG/200w.gif", listOf("sparkle", "eyes", "crush", "inlove", "adore", "love")),
        GifItem("e5", "Heart Explosion", "Love", "https://media.giphy.com/media/26ufdipQqU2lhNA4g/giphy.gif", "https://media.giphy.com/media/26ufdipQqU2lhNA4g/200w.gif", listOf("heart", "explosion", "sparkle", "romantic", "love")),
        GifItem("e6", "Puppy Love", "Love", "https://media.giphy.com/media/3ndAvMC5lfPNMCzywc/giphy.gif", "https://media.giphy.com/media/3ndAvMC5lfPNMCzywc/200w.gif", listOf("puppy", "dog", "sweet", "hug", "love"))
    )

    val sadGifs = listOf(
        GifItem("sd1", "Crying Rain", "Sad", "https://media.giphy.com/media/OPU6wzx8JrHna/giphy.gif", "https://media.giphy.com/media/OPU6wzx8JrHna/200w.gif", listOf("cry", "sad", "tears", "rain", "heartbroken")),
        GifItem("sd2", "SpongeBob Sad", "Sad", "https://media.giphy.com/media/ISOckXUybVfQ4/giphy.gif", "https://media.giphy.com/media/ISOckXUybVfQ4/200w.gif", listOf("spongebob", "sad", "alone", "crying", "depressed")),
        GifItem("sd3", "Pikachu Crying", "Sad", "https://media.giphy.com/media/L95W4wvxaEQwpUjRH6/giphy.gif", "https://media.giphy.com/media/L95W4wvxaEQwpUjRH6/200w.gif", listOf("pikachu", "cry", "tears", "pokemon", "sad")),
        GifItem("sd4", "Disappointed Sigh", "Sad", "https://media.giphy.com/media/XsUtdIeJ0MWMo/giphy.gif", "https://media.giphy.com/media/XsUtdIeJ0MWMo/200w.gif", listOf("sigh", "disappointed", "sad", "upset", "unhappy")),
        GifItem("sd5", "Cat Crying", "Sad", "https://media.giphy.com/media/9Y5BbDSkSTiY8/giphy.gif", "https://media.giphy.com/media/9Y5BbDSkSTiY8/200w.gif", listOf("cat", "cry", "tears", "sad", "hurt"))
    )

    val applauseGifs = listOf(
        GifItem("ap1", "Standing Ovation", "Applause", "https://media.giphy.com/media/BPJmthQ3YRwD6QqcVD/giphy.gif", "https://media.giphy.com/media/BPJmthQ3YRwD6QqcVD/200w.gif", listOf("clap", "applause", "cheers", "gatsby", "well done", "win")),
        GifItem("ap2", "Minion Clapping", "Applause", "https://media.giphy.com/media/11sBLVxNs7v6WA/giphy.gif", "https://media.giphy.com/media/11sBLVxNs7v6WA/200w.gif", listOf("minion", "clap", "applause", "excited", "bravo")),
        GifItem("ap3", "Joker Clapping", "Applause", "https://media.giphy.com/media/A7ZbCuv0fJBlC/giphy.gif", "https://media.giphy.com/media/A7ZbCuv0fJBlC/200w.gif", listOf("joker", "clap", "sarcastical", "ironic", "slowclap")),
        GifItem("ap4", "Seinfeld Cheers", "Applause", "https://media.giphy.com/media/26u4cqiYI30juCOGY/giphy.gif", "https://media.giphy.com/media/26u4cqiYI30juCOGY/200w.gif", listOf("cheers", "celebrate", "happy", "yes", "congrats")),
        GifItem("ap5", "Baby High Five", "Applause", "https://media.giphy.com/media/3o7TKqFZJpwL8Git28/giphy.gif", "https://media.giphy.com/media/3o7TKqFZJpwL8Git28/200w.gif", listOf("highfive", "thumbsup", "win", "great", "applause"))
    )

    val petsGifs = listOf(
        GifItem("pt1", "Cat Vibing Nod", "Cute Pets", "https://media.giphy.com/media/jpbnoe3UIa8TU8LM13/giphy.gif", "https://media.giphy.com/media/jpbnoe3UIa8TU8LM13/200w.gif", listOf("cat", "vibe", "music", "nod", "cute", "pet")),
        GifItem("pt2", "Dog Tilt Head", "Cute Pets", "https://media.giphy.com/media/4Zo41lhzKt6iZ8xff9/giphy.gif", "https://media.giphy.com/media/4Zo41lhzKt6iZ8xff9/200w.gif", listOf("dog", "tilt", "curious", "cute", "puppy", "pet")),
        GifItem("pt3", "Happy Corgi Run", "Cute Pets", "https://media.giphy.com/media/eYilisUwipOEM/giphy.gif", "https://media.giphy.com/media/eYilisUwipOEM/200w.gif", listOf("corgi", "dog", "run", "happy", "cute", "pet")),
        GifItem("pt4", "Hamster Eating", "Cute Pets", "https://media.giphy.com/media/13CoXDiaCcCoyk/giphy.gif", "https://media.giphy.com/media/13CoXDiaCcCoyk/200w.gif", listOf("hamster", "munch", "snack", "eat", "cute", "pet")),
        GifItem("pt5", "Sleepy Kitten Nap", "Cute Pets", "https://media.giphy.com/media/BzyTuYCmvSORqs1ABM/giphy.gif", "https://media.giphy.com/media/BzyTuYCmvSORqs1ABM/200w.gif", listOf("kitten", "sleep", "nap", "cute", "cat", "pet"))
    )

    val gamingGifs = listOf(
        GifItem("gm1", "Gamer Rage Smash", "Gaming", "https://media.giphy.com/media/3o7btPCcdNniyf0ArS/giphy.gif", "https://media.giphy.com/media/3o7btPCcdNniyf0ArS/200w.gif", listOf("gamer", "rage", "keyboard", "smash", "funny", "gaming")),
        GifItem("gm2", "Victory Dance Fortnite", "Gaming", "https://media.giphy.com/media/14udF3WUwwGMaA/giphy.gif", "https://media.giphy.com/media/14udF3WUwwGMaA/200w.gif", listOf("victory", "dance", "fortnite", "win", "gamer", "gaming")),
        GifItem("gm3", "Pacman Chomping", "Gaming", "https://media.giphy.com/media/d10dMmzqCYqQ0/giphy.gif", "https://media.giphy.com/media/d10dMmzqCYqQ0/200w.gif", listOf("pacman", "retro", "arcade", "ghost", "gaming")),
        GifItem("gm4", "Minecraft Dance", "Gaming", "https://media.giphy.com/media/129NVCr1UfsGTS/giphy.gif", "https://media.giphy.com/media/129NVCr1UfsGTS/200w.gif", listOf("minecraft", "steve", "dance", "craft", "gaming")),
        GifItem("gm5", "Level Up Glow", "Gaming", "https://media.giphy.com/media/26ufdipQqU2lhNA4g/giphy.gif", "https://media.giphy.com/media/26ufdipQqU2lhNA4g/200w.gif", listOf("levelup", "rpg", "power", "glow", "win", "gaming"))
    )

    val gifCategories = listOf(
        GifCategory("Trending", "🔥", trendingGifs),
        GifCategory("Reactions", "😲", reactionGifs),
        GifCategory("Laugh & LOL", "😂", laughGifs),
        GifCategory("Dance & Vibes", "💃", danceGifs),
        GifCategory("Love & Warm", "💖", loveGifs),
        GifCategory("Sad & Cry", "😭", sadGifs),
        GifCategory("Applause & Win", "👏", applauseGifs),
        GifCategory("Cute Pets", "🐾", petsGifs),
        GifCategory("Gaming", "🎮", gamingGifs)
    )

    val allGifs = (trendingGifs + reactionGifs + laughGifs + danceGifs + loveGifs + sadGifs + applauseGifs + petsGifs + gamingGifs).distinctBy { it.id }
}
