package com.example.data

import android.content.Context
import android.content.SharedPreferences
import org.json.JSONArray
import org.json.JSONObject

data class ClipboardItem(
    val id: String,
    val text: String,
    val timestamp: Long,
    val isPinned: Boolean = false
)

class ClipboardRepository(context: Context) {
    private val prefs: SharedPreferences =
        context.getSharedPreferences("keynova_clipboard", Context.MODE_PRIVATE)

    companion object {
        private const val KEY_ITEMS = "clipboard_items_json"
        private const val MAX_ITEMS = 30
    }

    @Synchronized
    fun getItems(): List<ClipboardItem> {
        val raw = prefs.getString(KEY_ITEMS, null) ?: return defaultClips()
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
            if (list.isEmpty()) defaultClips() else list
        } catch (e: Exception) {
            defaultClips()
        }
    }

    @Synchronized
    fun addItem(text: String): Boolean {
        val trimmed = text.trim()
        if (trimmed.isEmpty()) return false
        val current = getItems().toMutableList()
        // Deduplicate
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
        saveItems(pinned + unpinned)
        return true
    }

    @Synchronized
    fun togglePin(id: String) {
        val current = getItems().map {
            if (it.id == id) it.copy(isPinned = !it.isPinned) else it
        }
        saveItems(current)
    }

    @Synchronized
    fun deleteItem(id: String) {
        val current = getItems().filterNot { it.id == id }
        saveItems(current)
    }

    @Synchronized
    fun clearAll() {
        val current = getItems().filter { it.isPinned }
        saveItems(current)
    }

    @Synchronized
    fun clearEverything() {
        prefs.edit().remove(KEY_ITEMS).apply()
    }

    private fun saveItems(items: List<ClipboardItem>) {
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
            text = "https://keynova.app",
            timestamp = System.currentTimeMillis() - 120000,
            isPinned = false
        ),
        ClipboardItem(
            id = "demo_3",
            text = "Simple. Fast. Offline.",
            timestamp = System.currentTimeMillis() - 180000,
            isPinned = false
        ),
        ClipboardItem(
            id = "demo_4",
            text = "Type Better Everyday",
            timestamp = System.currentTimeMillis() - 240000,
            isPinned = false
        )
    )
}
