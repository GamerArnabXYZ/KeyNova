package com.example.util

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.RadialGradient
import android.graphics.Shader
import android.net.Uri
import android.view.inputmethod.EditorInfo
import android.view.inputmethod.InputConnection
import android.widget.Toast
import androidx.core.content.FileProvider
import androidx.core.view.inputmethod.EditorInfoCompat
import androidx.core.view.inputmethod.InputConnectionCompat
import androidx.core.view.inputmethod.InputContentInfoCompat
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream
import java.net.HttpURLConnection
import java.net.URL

object MediaStickerDispatcher {

    /**
     * Synthesizes a high-resolution 512x512 PNG sticker from two emojis with glowing badge background.
     */
    fun generateMashupSticker(context: Context, emoji1: String, emoji2: String, title: String): File {
        val stickersDir = File(context.cacheDir, "stickers").apply { mkdirs() }
        val fileName = "mashup_${emoji1.hashCode()}_${emoji2.hashCode()}.png"
        val stickerFile = File(stickersDir, fileName)

        if (stickerFile.exists() && stickerFile.length() > 0) {
            return stickerFile
        }

        try {
            val size = 512
            val bitmap = Bitmap.createBitmap(size, size, Bitmap.Config.ARGB_8888)
            val canvas = Canvas(bitmap)

            // 1. Soft glowing circular badge backdrop
            val badgePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                shader = RadialGradient(
                    size / 2f, size / 2f, size * 0.48f,
                    intArrayOf(
                        Color.parseColor("#403B82F6"),
                        Color.parseColor("#208B5CF6"),
                        Color.TRANSPARENT
                    ),
                    floatArrayOf(0f, 0.7f, 1f),
                    Shader.TileMode.CLAMP
                )
            }
            canvas.drawCircle(size / 2f, size / 2f, size * 0.45f, badgePaint)

            // 2. Render Left Emoji
            val leftPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                textSize = 190f
                textAlign = Paint.Align.CENTER
            }
            canvas.drawText(emoji1, size * 0.38f, size * 0.58f, leftPaint)

            // 3. Render Right Emoji with artistic overlay
            val rightPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                textSize = 180f
                textAlign = Paint.Align.CENTER
            }
            canvas.drawText(emoji2, size * 0.65f, size * 0.68f, rightPaint)

            // 4. Save to PNG
            FileOutputStream(stickerFile).use { out ->
                bitmap.compress(Bitmap.CompressFormat.PNG, 100, out)
                out.flush()
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }

        return stickerFile
    }

    /**
     * Extracts unicode emojis from text.
     */
    fun extractEmojis(text: String): List<String> {
        val emojis = mutableListOf<String>()
        var i = 0
        while (i < text.length) {
            val cp = text.codePointAt(i)
            val charCount = Character.charCount(cp)
            val isEmoji = (cp in 0x1F300..0x1FAFF) ||
                    (cp in 0x2600..0x27BF) ||
                    (cp in 0x1F600..0x1F64F) ||
                    (cp in 0x1F680..0x1F6FF) ||
                    (cp in 0x1F1E6..0x1F1FF) ||
                    (cp in 0x2702..0x27B0)
            if (isEmoji) {
                emojis.add(String(Character.toChars(cp)))
            }
            i += charCount
        }
        return emojis
    }

    /**
     * Decodes source emojis from a Google Emoji Kitchen URL if present (e.g. u1f602_u1f525).
     */
    fun extractEmojisFromKitchenUrl(url: String): Pair<String, String>? {
        val regex = Regex("u([0-9a-fA-F]+)_u([0-9a-fA-F]+)")
        val match = regex.find(url) ?: return null
        return try {
            val cp1 = match.groupValues[1].toInt(16)
            val cp2 = match.groupValues[2].toInt(16)
            Pair(String(Character.toChars(cp1)), String(Character.toChars(cp2)))
        } catch (_: Exception) {
            null
        }
    }

    /**
     * Downloads an online image or generates a local sticker, returning a File.
     */
    suspend fun getMediaFile(
        context: Context,
        rawUri: String,
        emoji1: String = "",
        emoji2: String = "",
        title: String = ""
    ): File = withContext(Dispatchers.IO) {
        val mediaDir = File(context.cacheDir, "media").apply { mkdirs() }
        val ext = if (rawUri.endsWith(".gif", ignoreCase = true)) ".gif" else ".png"
        val fileName = "cached_${Math.abs(rawUri.hashCode())}$ext"
        val targetFile = File(mediaDir, fileName)

        if (targetFile.exists() && targetFile.length() > 0) {
            return@withContext targetFile
        }

        // Try downloading online
        if (rawUri.startsWith("http://") || rawUri.startsWith("https://")) {
            try {
                val url = URL(rawUri)
                val conn = url.openConnection() as HttpURLConnection
                conn.connectTimeout = 3000
                conn.readTimeout = 3000
                conn.setRequestProperty("User-Agent", "Mozilla/5.0")
                if (conn.responseCode == HttpURLConnection.HTTP_OK) {
                    conn.inputStream.use { input ->
                        FileOutputStream(targetFile).use { output ->
                            input.copyTo(output)
                        }
                    }
                    if (targetFile.length() > 0) {
                        return@withContext targetFile
                    }
                }
            } catch (_: Exception) {}
        }

        // Determine fallback emojis from explicit args, URL hex codes, or description
        var resolvedE1 = emoji1
        var resolvedE2 = emoji2

        if (resolvedE1.isBlank() || resolvedE2.isBlank()) {
            val fromUrl = extractEmojisFromKitchenUrl(rawUri)
            if (fromUrl != null) {
                resolvedE1 = fromUrl.first
                resolvedE2 = fromUrl.second
            }
        }

        if (resolvedE1.isBlank() || resolvedE2.isBlank()) {
            val fromText = extractEmojis(title)
            if (fromText.size >= 2) {
                resolvedE1 = fromText[0]
                resolvedE2 = fromText[1]
            }
        }

        // If download failed or unmapped, generate local high-res sticker
        if (resolvedE1.isNotBlank() && resolvedE2.isNotBlank()) {
            return@withContext generateMashupSticker(context, resolvedE1, resolvedE2, title)
        }

        targetFile
    }

    /**
     * Dispatches media to the active input editor using Android FileProvider & InputConnectionCompat.
     */
    suspend fun dispatchMedia(
        context: Context,
        ic: InputConnection?,
        info: EditorInfo?,
        rawUri: String,
        mimeType: String,
        description: String,
        emoji1: String = "",
        emoji2: String = ""
    ) {
        val file = getMediaFile(context, rawUri, emoji1, emoji2, description)

        withContext(Dispatchers.Main) {
            if (!file.exists() || file.length() == 0L) {
                // Fallback to text emojis if file couldn't be created
                val emojis = if (emoji1.isNotBlank() && emoji2.isNotBlank()) {
                    "$emoji1$emoji2"
                } else {
                    extractEmojis(description).joinToString("")
                }
                if (emojis.isNotBlank()) {
                    ic?.commitText(emojis, 1)
                }
                return@withContext
            }

            try {
                val contentUri: Uri = FileProvider.getUriForFile(
                    context,
                    "${context.packageName}.fileprovider",
                    file
                )

                if (info != null && ic != null) {
                    val supportedMimes = EditorInfoCompat.getContentMimeTypes(info)
                    val effectiveMime = if (file.name.endsWith(".gif", ignoreCase = true)) "image/gif" else "image/png"
                    val isSupported = supportedMimes.any { mime ->
                        effectiveMime.equals(mime, ignoreCase = true) ||
                                (mime.endsWith("/*") && effectiveMime.startsWith(mime.dropLast(2)))
                    }

                    if (isSupported) {
                        // Grant permission to receiving app (WhatsApp, Telegram, etc.)
                        try {
                            context.grantUriPermission(
                                info.packageName,
                                contentUri,
                                Intent.FLAG_GRANT_READ_URI_PERMISSION
                            )
                        } catch (_: Exception) {}

                        val descriptionCompat = android.content.ClipDescription(
                            description.ifEmpty { "Emoji Sticker" },
                            arrayOf(effectiveMime)
                        )
                        val inputContentInfo = InputContentInfoCompat(
                            contentUri,
                            descriptionCompat,
                            null
                        )

                        val committed = InputConnectionCompat.commitContent(
                            ic,
                            info,
                            inputContentInfo,
                            InputConnectionCompat.INPUT_CONTENT_GRANT_READ_URI_PERMISSION,
                            null
                        )

                        if (committed) {
                            return@withContext
                        }
                    }
                }

                // If target app doesn't support rich content (e.g. plain search field):
                // Copy the sticker to system clipboard so user can paste it anywhere!
                val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                val clip = ClipData.newUri(context.contentResolver, description, contentUri)
                clipboard.setPrimaryClip(clip)
                Toast.makeText(context, "Sticker copied! Ready to paste.", Toast.LENGTH_SHORT).show()

                // Insert emoji text representation
                if (emoji1.isNotBlank() && emoji2.isNotBlank()) {
                    ic?.commitText("$emoji1$emoji2", 1)
                }
            } catch (e: Exception) {
                e.printStackTrace()
                if (emoji1.isNotBlank() && emoji2.isNotBlank()) {
                    ic?.commitText("$emoji1$emoji2", 1)
                }
            }
        }
    }
}
