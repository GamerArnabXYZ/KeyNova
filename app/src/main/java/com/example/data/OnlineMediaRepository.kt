package com.example.data

import android.util.Log
import com.example.ui.keyboard.GifItem
import com.example.ui.keyboard.MediaCatalog
import com.example.ui.keyboard.StickerItem
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.io.BufferedReader
import java.io.InputStreamReader
import java.net.HttpURLConnection
import java.net.URL
import java.net.URLEncoder

object OnlineMediaRepository {

    private const val TAG = "OnlineMediaRepository"
    private const val GIPHY_API_KEY = "sXpGFDGZs0Dv1mmNFvYaGUvYwKX0PWIh"
    private const val BASE_URL = "https://api.giphy.com/v1"

    // In-memory cache for fast, instant responses
    private val gifCache = mutableMapOf<String, List<GifItem>>()
    private val stickerCache = mutableMapOf<String, List<StickerItem>>()

    suspend fun fetchGifs(query: String? = null, limit: Int = 28): List<GifItem> = withContext(Dispatchers.IO) {
        val cacheKey = query?.trim()?.lowercase() ?: "__trending__"
        gifCache[cacheKey]?.let { return@withContext it }

        try {
            val endpoint = if (query.isNullOrBlank()) {
                "$BASE_URL/gifs/trending?api_key=$GIPHY_API_KEY&limit=$limit&rating=g"
            } else {
                val encodedQ = URLEncoder.encode(query.trim(), "UTF-8")
                "$BASE_URL/gifs/search?api_key=$GIPHY_API_KEY&q=$encodedQ&limit=$limit&rating=g"
            }

            val jsonStr = httpGet(endpoint)
            if (jsonStr.isNullOrEmpty()) {
                return@withContext getFallbackGifs(query)
            }

            val json = JSONObject(jsonStr)
            val dataArray = json.optJSONArray("data") ?: return@withContext getFallbackGifs(query)
            val results = mutableListOf<GifItem>()

            for (i in 0 until dataArray.length()) {
                val obj = dataArray.optJSONObject(i) ?: continue
                val id = obj.optString("id", "")
                val title = obj.optString("title", "GIF")
                    .replace("GIF", "", ignoreCase = true)
                    .replace("by", "", ignoreCase = true)
                    .trim()
                    .ifEmpty { "GIF" }
                val images = obj.optJSONObject("images") ?: continue
                val fixedHeight = images.optJSONObject("fixed_height") ?: images.optJSONObject("original") ?: continue
                val url = fixedHeight.optString("url", "")
                val fixedHeightSmall = images.optJSONObject("fixed_height_small")
                val previewUrl = fixedHeightSmall?.optString("url", url) ?: url

                if (url.isNotEmpty() && id.isNotEmpty()) {
                    results.add(
                        GifItem(
                            id = id,
                            title = title,
                            category = query ?: "Trending",
                            url = url,
                            previewUrl = previewUrl,
                            tags = listOf(title.lowercase(), query?.lowercase() ?: "trending")
                        )
                    )
                }
            }

            if (results.isNotEmpty()) {
                gifCache[cacheKey] = results
                results
            } else {
                getFallbackGifs(query)
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error fetching GIFs from internet", e)
            getFallbackGifs(query)
        }
    }

    suspend fun fetchStickers(query: String? = null, limit: Int = 28): List<StickerItem> = withContext(Dispatchers.IO) {
        val cacheKey = query?.trim()?.lowercase() ?: "__trending_stickers__"
        stickerCache[cacheKey]?.let { return@withContext it }

        try {
            val endpoint = if (query.isNullOrBlank()) {
                "$BASE_URL/stickers/trending?api_key=$GIPHY_API_KEY&limit=$limit&rating=g"
            } else {
                val encodedQ = URLEncoder.encode(query.trim(), "UTF-8")
                "$BASE_URL/stickers/search?api_key=$GIPHY_API_KEY&q=$encodedQ&limit=$limit&rating=g"
            }

            val jsonStr = httpGet(endpoint)
            if (jsonStr.isNullOrEmpty()) {
                return@withContext getFallbackStickers(query)
            }

            val json = JSONObject(jsonStr)
            val dataArray = json.optJSONArray("data") ?: return@withContext getFallbackStickers(query)
            val results = mutableListOf<StickerItem>()

            for (i in 0 until dataArray.length()) {
                val obj = dataArray.optJSONObject(i) ?: continue
                val id = obj.optString("id", "")
                val title = obj.optString("title", "Sticker")
                    .replace("Sticker", "", ignoreCase = true)
                    .replace("GIF", "", ignoreCase = true)
                    .trim()
                    .ifEmpty { "Sticker" }
                val images = obj.optJSONObject("images") ?: continue
                val fixedHeight = images.optJSONObject("fixed_height") ?: images.optJSONObject("original") ?: continue
                val url = fixedHeight.optString("url", "")

                if (url.isNotEmpty() && id.isNotEmpty()) {
                    results.add(
                        StickerItem(
                            id = id,
                            title = title,
                            category = query ?: "Trending",
                            url = url,
                            previewEmoji = "✨",
                            tags = listOf(title.lowercase(), query?.lowercase() ?: "sticker")
                        )
                    )
                }
            }

            if (results.isNotEmpty()) {
                stickerCache[cacheKey] = results
                results
            } else {
                getFallbackStickers(query)
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error fetching Stickers from internet", e)
            getFallbackStickers(query)
        }
    }

    private fun httpGet(urlStr: String): String? {
        var conn: HttpURLConnection? = null
        return try {
            val url = URL(urlStr)
            conn = url.openConnection() as HttpURLConnection
            conn.requestMethod = "GET"
            conn.connectTimeout = 6000
            conn.readTimeout = 6000
            conn.setRequestProperty("User-Agent", "KeyNovaKeyboard/1.0 (Android)")
            conn.setRequestProperty("Accept", "application/json")

            if (conn.responseCode in 200..299) {
                BufferedReader(InputStreamReader(conn.inputStream)).use { reader ->
                    reader.readText()
                }
            } else {
                null
            }
        } catch (_: Exception) {
            null
        } finally {
            conn?.disconnect()
        }
    }

    private fun getFallbackGifs(query: String?): List<GifItem> {
        if (query.isNullOrBlank()) return MediaCatalog.trendingGifs
        val q = query.trim().lowercase()
        val tokens = q.split("\\s+".toRegex()).filter { it.isNotEmpty() }
        val matches = MediaCatalog.allGifs.filter { gif ->
            val title = gif.title.lowercase()
            val cat = gif.category.lowercase()
            val tags = gif.tags.map { it.lowercase() }
            tokens.all { t -> title.contains(t) || cat.contains(t) || tags.any { it.contains(t) } }
        }
        return if (matches.isNotEmpty()) matches else MediaCatalog.trendingGifs
    }

    private fun getFallbackStickers(query: String?): List<StickerItem> {
        if (query.isNullOrBlank()) return MediaCatalog.trendingStickers
        val q = query.trim().lowercase()
        val tokens = q.split("\\s+".toRegex()).filter { it.isNotEmpty() }
        val matches = MediaCatalog.allStickers.filter { sticker ->
            val title = sticker.title.lowercase()
            val cat = sticker.category.lowercase()
            val tags = sticker.tags.map { it.lowercase() }
            tokens.all { t -> title.contains(t) || cat.contains(t) || tags.any { it.contains(t) } }
        }
        return if (matches.isNotEmpty()) matches else MediaCatalog.trendingStickers
    }
}
