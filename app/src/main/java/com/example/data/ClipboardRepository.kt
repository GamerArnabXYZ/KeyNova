package com.example.data

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.SharedPreferences
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import org.json.JSONArray
import org.json.JSONObject

data class ClipboardItem(
    val id: String,
    val text: String,
    val timestamp: Long,
    val isPinned: Boolean = false
)

class ClipboardRepository(private val context: Context) {
    private val prefs: SharedPreferences =
        context.getSharedPreferences("keynova_clipboard", Context.MODE_PRIVATE)

    companion object {
        private const val KEY_ITEMS = "clipboard_items_json"
        private const val KEY_INITIALIZED = "clipboard_initialized_v2"
        private const val MAX_ITEMS = 50
    }

    private val _itemsFlow = MutableStateFlow<List<ClipboardItem>>(emptyList())
    val itemsFlow: StateFlow<List<ClipboardItem>> = _itemsFlow.asStateFlow()

    init {
        if (!prefs.getBoolean(KEY_INITIALIZED, false)) {
            if (!prefs.contains(KEY_ITEMS)) {
                saveItemsInternal(defaultClips())
            }
            prefs.edit().putBoolean(KEY_INITIALIZED, true).apply()
        }
        _itemsFlow.value = loadItemsFromPrefs()
    }

    @Synchronized
    fun getItems(): List<ClipboardItem> {
        return loadItemsFromPrefs()
    }

    private fun loadItemsFromPrefs(): List<ClipboardItem> {
        val raw = prefs.getString(KEY_ITEMS, null) ?: return emptyList()
        return try {
            val array = JSONArray(raw)
            val list = mutableListOf<ClipboardItem>()
            for (i in 0 until array.length()) {
                val obj = array.getJSONObject(i)
                list.add(
                    ClipboardItem(
                        id = obj.getString("id"),
                        text = obj.getString("text"),
                        timestamp = obj.optLong("timestamp", System.currentTimeMillis()),
                        isPinned = obj.optBoolean("isPinned", false)
                    )
                )
            }
            list
        } catch (e: Exception) {
            emptyList()
        }
    }

    @Synchronized
    fun syncFromSystemClipboard(ctx: Context? = null): Boolean {
        val appContext = ctx ?: context
        return try {
            val cm = appContext.getSystemService(Context.CLIPBOARD_SERVICE) as? ClipboardManager ?: return false
            if (!cm.hasPrimaryClip()) return false
            val clip = cm.primaryClip ?: return false
            if (clip.itemCount > 0) {
                val text = clip.getItemAt(0)?.coerceToText(appContext)?.toString()
                if (!text.isNullOrBlank()) {
                    return addItem(text)
                }
            }
            false
        } catch (e: Exception) {
            false
        }
    }

    @Synchronized
    fun copyToSystemClipboard(text: String, ctx: Context? = null) {
        try {
            val appContext = ctx ?: context
            val cm = appContext.getSystemService(Context.CLIPBOARD_SERVICE) as? ClipboardManager
            cm?.setPrimaryClip(ClipData.newPlainText("KeyNova", text))
        } catch (_: Exception) {}
    }

    @Synchronized
    fun addItem(text: String): Boolean {
        val trimmed = text.trim()
        if (trimmed.isEmpty()) return false
        val current = loadItemsFromPrefs().toMutableList()
        // If top item is identical, don't duplicate
        if (current.isNotEmpty() && current[0].text == trimmed) {
            return false
        }
        // Deduplicate unpinned copies
        current.removeAll { it.text == trimmed && !it.isPinned }
        val newItem = ClipboardItem(
            id = System.currentTimeMillis().toString(),
            text = trimmed,
            timestamp = System.currentTimeMillis(),
            isPinned = false
        )
        current.add(0, newItem)
        // Keep max items, preserving pinned
        val pinned = current.filter { it.isPinned }
        val unpinned = current.filter { !it.isPinned }.take(MAX_ITEMS - pinned.size)
        val result = pinned + unpinned
        saveItemsInternal(result)
        _itemsFlow.value = result
        return true
    }

    @Synchronized
    fun togglePin(id: String) {
        val current = loadItemsFromPrefs().map {
            if (it.id == id) it.copy(isPinned = !it.isPinned) else it
        }
        saveItemsInternal(current)
        _itemsFlow.value = current
    }

    @Synchronized
    fun deleteItem(id: String) {
        val current = loadItemsFromPrefs().filterNot { it.id == id }
        saveItemsInternal(current)
        _itemsFlow.value = current
    }

    @Synchronized
    fun clearAll() {
        val current = loadItemsFromPrefs().filter { it.isPinned }
        saveItemsInternal(current)
        _itemsFlow.value = current
    }

    @Synchronized
    fun clearEverything() {
        prefs.edit().remove(KEY_ITEMS).apply()
        _itemsFlow.value = emptyList()
    }

    private fun saveItemsInternal(items: List<ClipboardItem>) {
        val array = JSONArray()
        items.forEach { item ->
            val obj = JSONObject()
            obj.put("id", item.id)
            obj.put("text", item.text)
            obj.put("timestamp", item.timestamp)
            obj.put("isPinned", item.isPinned)
            array.put(obj)
        }
        prefs.edit().putString(KEY_ITEMS, array.toString()).apply()
    }

    private fun defaultClips(): List<ClipboardItem> = listOf(
        ClipboardItem(
            id = "demo_1",
            text = "Hello, KeyNova!",
            timestamp = System.currentTimeMillis() - 60000,
            isPinned = true
        ),
        ClipboardItem(
            id = "demo_2",
            text = "Simple. Fast. Offline.",
            timestamp = System.currentTimeMillis() - 120000,
            isPinned = false
        )
    )
}
