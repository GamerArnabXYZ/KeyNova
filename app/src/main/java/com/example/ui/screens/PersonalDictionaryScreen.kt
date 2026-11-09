package com.example.ui.screens

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.KeyboardPreferences
import com.example.data.PersonalDictEntry
import com.example.data.PersonalDictEntryType
import com.example.data.PersonalDictionaryManager

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PersonalDictionaryScreen(
    keyboardPreferences: KeyboardPreferences,
    onNavigateBack: () -> Unit
) {
    val context = LocalContext.current
    val dictManager = remember { PersonalDictionaryManager.getInstance(context) }
    val dictRevision = dictManager.revision.intValue
    val prefRev = keyboardPreferences.preferenceRevision.intValue

    var searchQuery by remember { mutableStateOf("") }
    var selectedCategoryFilter by remember { mutableStateOf<PersonalDictEntryType?>(null) }
    var isSelectionMode by remember { mutableStateOf(false) }
    val selectedEntryIds = remember { mutableStateListOf<String>() }

    // Dialog states
    var showAddDialog by remember { mutableStateOf(false) }
    var editingEntry by remember { mutableStateOf<PersonalDictEntry?>(null) }
    var entryToDelete by remember { mutableStateOf<PersonalDictEntry?>(null) }
    var showBulkDeleteDialog by remember { mutableStateOf(false) }
    var showClearAllDialog by remember { mutableStateOf(false) }

    val autoLearnEnabled = remember(keyboardPreferences.autoLearnPersonalDict, prefRev) {
        keyboardPreferences.autoLearnPersonalDict
    }

    // Filtered entries re-evaluated on search, category filter, or dictionary revision
    val entries: List<PersonalDictEntry> = remember(searchQuery, selectedCategoryFilter, dictRevision) {
        dictManager.getEntriesFiltered(selectedCategoryFilter, searchQuery)
    }

    val totalCount = remember(dictRevision) {
        dictManager.getTotalEntryCount()
    }

    Scaffold(
        modifier = Modifier.testTag("personal_dictionary_screen"),
        topBar = {
            TopAppBar(
                title = {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(
                            text = "Personal Dictionary",
                            fontWeight = FontWeight.Bold,
                            fontSize = 19.sp,
                            color = Color(0xFF0F172A)
                        )
                        Surface(
                            shape = CircleShape,
                            color = Color(0xFF6366F1).copy(alpha = 0.12f)
                        ) {
                            Text(
                                text = "$totalCount",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF4F46E5),
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                            )
                        }
                    }
                },
                navigationIcon = {
                    IconButton(
                        onClick = onNavigateBack,
                        modifier = Modifier.testTag("personal_dict_back_btn")
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                            tint = Color(0xFF0F172A)
                        )
                    }
                },
                actions = {
                    if (isSelectionMode) {
                        IconButton(
                            onClick = {
                                if (selectedEntryIds.isNotEmpty()) {
                                    showBulkDeleteDialog = true
                                }
                            },
                            enabled = selectedEntryIds.isNotEmpty(),
                            modifier = Modifier.testTag("personal_dict_delete_selected_btn")
                        ) {
                            Icon(
                                imageVector = Icons.Default.Delete,
                                contentDescription = "Delete Selected",
                                tint = if (selectedEntryIds.isNotEmpty()) Color(0xFFDC2626) else Color.LightGray
                            )
                        }
                        TextButton(
                            onClick = {
                                isSelectionMode = false
                                selectedEntryIds.clear()
                            }
                        ) {
                            Text("Done", color = Color(0xFF4F46E5), fontWeight = FontWeight.SemiBold)
                        }
                    } else {
                        if (totalCount > 0) {
                            TextButton(
                                onClick = {
                                    isSelectionMode = true
                                    selectedEntryIds.clear()
                                },
                                modifier = Modifier.testTag("personal_dict_select_mode_btn")
                            ) {
                                Text("Select", color = Color(0xFF4F46E5), fontSize = 14.sp)
                            }
                            IconButton(
                                onClick = { showClearAllDialog = true },
                                modifier = Modifier.testTag("personal_dict_clear_all_btn")
                            ) {
                                Icon(
                                    imageVector = Icons.Outlined.DeleteSweep,
                                    contentDescription = "Clear All",
                                    tint = Color(0xFFDC2626)
                                )
                            }
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color(0xFFF8FAFC))
            )
        },
        floatingActionButton = {
            if (!isSelectionMode) {
                FloatingActionButton(
                    onClick = { showAddDialog = true },
                    containerColor = Color(0xFF4F46E5),
                    contentColor = Color.White,
                    shape = CircleShape,
                    modifier = Modifier.testTag("personal_dict_add_fab")
                ) {
                    Icon(imageVector = Icons.Default.Add, contentDescription = "Add Entry")
                }
            }
        },
        containerColor = Color(0xFFF8FAFC)
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
            contentPadding = PaddingValues(start = 16.dp, top = 8.dp, end = 16.dp, bottom = 88.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            // ==========================================
            // 1. AUTO-LEARNING SETTING CARD
            // ==========================================
            item {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("personal_dict_autolearn_card"),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFFFFFFFF)),
                    elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(38.dp)
                                    .clip(CircleShape)
                                    .background(Color(0xFFEEF2FF)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Outlined.AutoAwesome,
                                    contentDescription = null,
                                    tint = Color(0xFF4F46E5),
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(12.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = "Automatic Learning",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 15.sp,
                                    color = Color(0xFF0F172A)
                                )
                                Text(
                                    text = "Auto-save words, emails, URLs & alphanumeric tokens",
                                    fontSize = 12.sp,
                                    color = Color(0xFF64748B)
                                )
                            }
                            Switch(
                                checked = autoLearnEnabled,
                                onCheckedChange = { checked ->
                                    keyboardPreferences.autoLearnPersonalDict = checked
                                },
                                modifier = Modifier.testTag("personal_dict_autolearn_toggle"),
                                colors = SwitchDefaults.colors(
                                    checkedThumbColor = Color.White,
                                    checkedTrackColor = Color(0xFF4F46E5)
                                )
                            )
                        }
                        Spacer(modifier = Modifier.height(10.dp))
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(8.dp))
                                .background(Color(0xFFF1F5F9))
                                .padding(horizontal = 10.dp, vertical = 6.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Outlined.Shield,
                                contentDescription = null,
                                tint = Color(0xFF10B981),
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "🔒 100% on-device local storage. Passwords/PINs are strictly blocked.",
                                fontSize = 11.sp,
                                color = Color(0xFF475569)
                            )
                        }
                    }
                }
            }

            // ==========================================
            // 2. SEARCH BAR
            // ==========================================
            item {
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("personal_dict_search_field"),
                    placeholder = {
                        Text("Search entries (e.g. Arnix, @, https)...", fontSize = 14.sp, color = Color(0xFF94A3B8))
                    },
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Default.Search,
                            contentDescription = "Search",
                            tint = Color(0xFF64748B)
                        )
                    },
                    trailingIcon = {
                        if (searchQuery.isNotEmpty()) {
                            IconButton(onClick = { searchQuery = "" }) {
                                Icon(
                                    imageVector = Icons.Default.Close,
                                    contentDescription = "Clear search",
                                    tint = Color(0xFF64748B)
                                )
                            }
                        }
                    },
                    shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedContainerColor = Color.White,
                        unfocusedContainerColor = Color.White,
                        focusedBorderColor = Color(0xFF4F46E5),
                        unfocusedBorderColor = Color(0xFFE2E8F0)
                    ),
                    singleLine = true
                )
            }

            // ==========================================
            // 3. CATEGORY FILTER CHIPS
            // ==========================================
            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    FilterChip(
                        selected = selectedCategoryFilter == null,
                        onClick = { selectedCategoryFilter = null },
                        label = { Text("All ($totalCount)") },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = Color(0xFF4F46E5),
                            selectedLabelColor = Color.White
                        )
                    )
                    PersonalDictEntryType.values().forEach { type ->
                        val count = remember(dictRevision, type) {
                            dictManager.getEntriesFiltered(type, "").size
                        }
                        FilterChip(
                            selected = selectedCategoryFilter == type,
                            onClick = {
                                selectedCategoryFilter = if (selectedCategoryFilter == type) null else type
                            },
                            label = { Text("${type.displayName} ($count)") },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = Color(0xFF4F46E5),
                                selectedLabelColor = Color.White
                            )
                        )
                    }
                }
            }

            // ==========================================
            // 4. SELECTION ACTIONS BAR (When in Selection Mode)
            // ==========================================
            if (isSelectionMode && entries.isNotEmpty()) {
                item {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "${selectedEntryIds.size} of ${entries.size} selected",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = Color(0xFF475569)
                        )
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            TextButton(
                                onClick = {
                                    if (selectedEntryIds.size == entries.size) {
                                        selectedEntryIds.clear()
                                    } else {
                                        selectedEntryIds.clear()
                                        selectedEntryIds.addAll(entries.map { it.id })
                                    }
                                }
                            ) {
                                Text(
                                    if (selectedEntryIds.size == entries.size) "Deselect All" else "Select All",
                                    fontSize = 13.sp,
                                    color = Color(0xFF4F46E5)
                                )
                            }
                        }
                    }
                }
            }

            // ==========================================
            // 5. ENTRIES LIST OR EMPTY STATE
            // ==========================================
            if (entries.isEmpty()) {
                item {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 24.dp)
                            .testTag("personal_dict_empty_card"),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = Color.White),
                        elevation = CardDefaults.cardElevation(defaultElevation = 0.5.dp)
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 40.dp, horizontal = 24.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(56.dp)
                                    .clip(CircleShape)
                                    .background(Color(0xFFEEF2FF)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Outlined.MenuBook,
                                    contentDescription = null,
                                    tint = Color(0xFF4F46E5),
                                    modifier = Modifier.size(28.dp)
                                )
                            }
                            Text(
                                text = if (searchQuery.isNotEmpty()) "No matching entries found" else "Personal Dictionary is Empty",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF1E293B)
                            )
                            Text(
                                text = if (searchQuery.isNotEmpty()) {
                                    "Try clearing the search query or category filter."
                                } else {
                                    "New words, alphanumeric combinations (Arnix90), special combinations (Arnix@67), emails, and URLs will appear here automatically as you type."
                                },
                                fontSize = 13.sp,
                                color = Color(0xFF64748B),
                                textAlign = androidx.compose.ui.text.style.TextAlign.Center
                            )
                            if (searchQuery.isEmpty()) {
                                Button(
                                    onClick = { showAddDialog = true },
                                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF4F46E5)),
                                    shape = RoundedCornerShape(10.dp)
                                ) {
                                    Icon(imageVector = Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text("Add New Entry")
                                }
                            }
                        }
                    }
                }
            } else {
                items(entries, key = { it.id }) { entry ->
                    val isSelected = selectedEntryIds.contains(entry.id)
                    PersonalDictEntryCard(
                        entry = entry,
                        isSelectionMode = isSelectionMode,
                        isSelected = isSelected,
                        onToggleSelect = {
                            if (isSelected) selectedEntryIds.remove(entry.id)
                            else selectedEntryIds.add(entry.id)
                        },
                        onEdit = { editingEntry = entry },
                        onDelete = { entryToDelete = entry }
                    )
                }
            }
        }
    }

    // ==========================================
    // DIALOGS: ADD ENTRY
    // ==========================================
    if (showAddDialog) {
        AddEntryDialog(
            onDismiss = { showAddDialog = false },
            onSave = { text, customType, lang ->
                val type = customType ?: dictManager.detectEntryType(text)
                val success = dictManager.addCustomEntry(text, type, lang)
                if (success) {
                    Toast.makeText(context, "Added \"$text\" to dictionary", Toast.LENGTH_SHORT).show()
                } else {
                    Toast.makeText(context, "Invalid or duplicate entry", Toast.LENGTH_SHORT).show()
                }
                showAddDialog = false
            }
        )
    }

    // ==========================================
    // DIALOGS: EDIT ENTRY
    // ==========================================
    editingEntry?.let { entry ->
        EditEntryDialog(
            entry = entry,
            onDismiss = { editingEntry = null },
            onUpdate = { newText, newType ->
                val success = dictManager.updateEntry(entry.id, newText, newType)
                if (success) {
                    Toast.makeText(context, "Updated successfully", Toast.LENGTH_SHORT).show()
                } else {
                    Toast.makeText(context, "Failed to update entry", Toast.LENGTH_SHORT).show()
                }
                editingEntry = null
            }
        )
    }

    // ==========================================
    // DIALOGS: DELETE SINGLE ENTRY
    // ==========================================
    entryToDelete?.let { entry ->
        AlertDialog(
            onDismissRequest = { entryToDelete = null },
            title = { Text("Delete Entry?", fontWeight = FontWeight.Bold) },
            text = { Text("Are you sure you want to delete \"${entry.text}\" from your personal dictionary?") },
            confirmButton = {
                Button(
                    onClick = {
                        dictManager.deleteEntry(entry.id)
                        Toast.makeText(context, "Entry deleted", Toast.LENGTH_SHORT).show()
                        entryToDelete = null
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFDC2626))
                ) {
                    Text("Delete")
                }
            },
            dismissButton = {
                TextButton(onClick = { entryToDelete = null }) {
                    Text("Cancel")
                }
            }
        )
    }

    // ==========================================
    // DIALOGS: BULK DELETE
    // ==========================================
    if (showBulkDeleteDialog) {
        AlertDialog(
            onDismissRequest = { showBulkDeleteDialog = false },
            title = { Text("Delete Selected?", fontWeight = FontWeight.Bold) },
            text = { Text("Are you sure you want to delete ${selectedEntryIds.size} selected entries?") },
            confirmButton = {
                Button(
                    onClick = {
                        val count = selectedEntryIds.size
                        dictManager.deleteEntries(selectedEntryIds.toSet())
                        Toast.makeText(context, "Deleted $count entries", Toast.LENGTH_SHORT).show()
                        selectedEntryIds.clear()
                        isSelectionMode = false
                        showBulkDeleteDialog = false
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFDC2626))
                ) {
                    Text("Delete")
                }
            },
            dismissButton = {
                TextButton(onClick = { showBulkDeleteDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }

    // ==========================================
    // DIALOGS: CLEAR ALL
    // ==========================================
    if (showClearAllDialog) {
        AlertDialog(
            onDismissRequest = { showClearAllDialog = false },
            title = { Text("Clear Entire Dictionary?", fontWeight = FontWeight.Bold) },
            text = { Text("This will permanently delete all $totalCount learned entries, words, emails, and URLs. This action cannot be undone.") },
            confirmButton = {
                Button(
                    onClick = {
                        dictManager.clearAll()
                        Toast.makeText(context, "Personal dictionary cleared", Toast.LENGTH_SHORT).show()
                        showClearAllDialog = false
                        isSelectionMode = false
                        selectedEntryIds.clear()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFDC2626))
                ) {
                    Text("Clear All")
                }
            },
            dismissButton = {
                TextButton(onClick = { showClearAllDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }
}

// =========================================================================
// ENTRY ITEM CARD COMPOSABLE
// =========================================================================

@Composable
private fun PersonalDictEntryCard(
    entry: PersonalDictEntry,
    isSelectionMode: Boolean,
    isSelected: Boolean,
    onToggleSelect: () -> Unit,
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("entry_card_${entry.id}")
            .clickable {
                if (isSelectionMode) onToggleSelect()
                else onEdit()
            },
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isSelected) Color(0xFFEEF2FF) else Color.White
        ),
        border = if (isSelected) CardDefaults.outlinedCardBorder().copy(
            brush = androidx.compose.ui.graphics.SolidColor(Color(0xFF4F46E5))
        ) else CardDefaults.outlinedCardBorder(),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.5.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 14.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            if (isSelectionMode) {
                Checkbox(
                    checked = isSelected,
                    onCheckedChange = { onToggleSelect() },
                    modifier = Modifier.padding(end = 8.dp),
                    colors = CheckboxDefaults.colors(checkedColor = Color(0xFF4F46E5))
                )
            }

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = entry.text,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 15.sp,
                    color = Color(0xFF0F172A),
                    fontFamily = if (entry.type != PersonalDictEntryType.WORD) FontFamily.Monospace else FontFamily.Default,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(modifier = Modifier.height(4.dp))
                Row(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Type Badge
                    val (typeBg, typeFg) = when (entry.type) {
                        PersonalDictEntryType.WORD -> Color(0xFFE0E7FF) to Color(0xFF3730A3)
                        PersonalDictEntryType.EMAIL -> Color(0xFFFFEDD5) to Color(0xFF9A3412)
                        PersonalDictEntryType.URL -> Color(0xFFF3E8FF) to Color(0xFF6B21A8)
                        PersonalDictEntryType.ALPHANUMERIC -> Color(0xFFDCFCE7) to Color(0xFF166534)
                        PersonalDictEntryType.CUSTOM -> Color(0xFFFEF3C7) to Color(0xFF92400E)
                    }
                    Surface(
                        shape = RoundedCornerShape(6.dp),
                        color = typeBg
                    ) {
                        Text(
                            text = entry.type.displayName,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Medium,
                            color = typeFg,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }

                    // Frequency Badge
                    Surface(
                        shape = RoundedCornerShape(6.dp),
                        color = Color(0xFFF1F5F9)
                    ) {
                        Text(
                            text = "⚡ ${entry.frequency}x",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = Color(0xFF475569),
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }

                    // Language Badge
                    val langLabel = when (entry.languageCode) {
                        "bn" -> "বাংলা"
                        "hi" -> "हिन्दी"
                        "en" -> "EN"
                        else -> "All"
                    }
                    Surface(
                        shape = RoundedCornerShape(6.dp),
                        color = Color(0xFFF8FAFC)
                    ) {
                        Text(
                            text = langLabel,
                            fontSize = 10.sp,
                            color = Color(0xFF64748B),
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }
                }
            }

            if (!isSelectionMode) {
                IconButton(
                    onClick = onEdit,
                    modifier = Modifier.testTag("edit_entry_${entry.id}")
                ) {
                    Icon(
                        imageVector = Icons.Default.Edit,
                        contentDescription = "Edit entry",
                        tint = Color(0xFF64748B),
                        modifier = Modifier.size(18.dp)
                    )
                }
                IconButton(
                    onClick = onDelete,
                    modifier = Modifier.testTag("delete_entry_${entry.id}")
                ) {
                    Icon(
                        imageVector = Icons.Default.DeleteOutline,
                        contentDescription = "Delete entry",
                        tint = Color(0xFFDC2626),
                        modifier = Modifier.size(18.dp)
                    )
                }
            }
        }
    }
}

// =========================================================================
// ADD ENTRY DIALOG
// =========================================================================

@Composable
private fun AddEntryDialog(
    onDismiss: () -> Unit,
    onSave: (text: String, customType: PersonalDictEntryType?, language: String) -> Unit
) {
    var text by remember { mutableStateOf("") }
    var selectedType by remember { mutableStateOf<PersonalDictEntryType?>(null) }
    var selectedLang by remember { mutableStateOf("all") }

    val detectedType = remember(text) {
        PersonalDictionaryManager.detectEntryType(text)
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Add to Personal Dictionary", fontWeight = FontWeight.Bold, fontSize = 18.sp) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                OutlinedTextField(
                    value = text,
                    onValueChange = { text = it },
                    label = { Text("Text / Word / Email / URL") },
                    placeholder = { Text("e.g. Arnix90, Arnix@67, site.com") },
                    singleLine = true,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("add_entry_text_field")
                )

                if (text.isNotBlank()) {
                    Text(
                        text = "Detected Category: ${detectedType.displayName}",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = Color(0xFF4F46E5)
                    )
                }

                Text("Override Category (Optional):", fontSize = 12.sp, color = Color(0xFF64748B))
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    FilterChip(
                        selected = selectedType == null,
                        onClick = { selectedType = null },
                        label = { Text("Auto", fontSize = 11.sp) }
                    )
                    PersonalDictEntryType.values().forEach { type ->
                        FilterChip(
                            selected = selectedType == type,
                            onClick = { selectedType = if (selectedType == type) null else type },
                            label = { Text(type.displayName, fontSize = 11.sp) }
                        )
                    }
                }

                Text("Language Mode:", fontSize = 12.sp, color = Color(0xFF64748B))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    listOf("all" to "All", "en" to "English", "bn" to "বাংলা", "hi" to "हिन्दी").forEach { (code, label) ->
                        FilterChip(
                            selected = selectedLang == code,
                            onClick = { selectedLang = code },
                            label = { Text(label, fontSize = 11.sp) }
                        )
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = { onSave(text.trim(), selectedType, selectedLang) },
                enabled = text.trim().length >= 2,
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF4F46E5)),
                modifier = Modifier.testTag("save_add_entry_btn")
            ) {
                Text("Save")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}

// =========================================================================
// EDIT ENTRY DIALOG
// =========================================================================

@Composable
private fun EditEntryDialog(
    entry: PersonalDictEntry,
    onDismiss: () -> Unit,
    onUpdate: (newText: String, newType: PersonalDictEntryType) -> Unit
) {
    var text by remember { mutableStateOf(entry.text) }
    var selectedType by remember { mutableStateOf(entry.type) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Edit Dictionary Entry", fontWeight = FontWeight.Bold, fontSize = 18.sp) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                OutlinedTextField(
                    value = text,
                    onValueChange = { text = it },
                    label = { Text("Entry Text") },
                    singleLine = true,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("edit_entry_text_field")
                )

                Text("Category:", fontSize = 12.sp, color = Color(0xFF64748B))
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    PersonalDictEntryType.values().forEach { type ->
                        FilterChip(
                            selected = selectedType == type,
                            onClick = { selectedType = type },
                            label = { Text(type.displayName, fontSize = 11.sp) }
                        )
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = { onUpdate(text.trim(), selectedType) },
                enabled = text.trim().length >= 2,
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF4F46E5)),
                modifier = Modifier.testTag("confirm_edit_entry_btn")
            ) {
                Text("Update")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}
