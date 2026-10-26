package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.CodeSnippet
import com.example.data.KeyboardPreferences
import com.example.data.SnippetsRepository

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GamingCodingScreen(
    keyboardPreferences: KeyboardPreferences,
    snippetsRepository: SnippetsRepository,
    onNavigateBack: () -> Unit
) {
    var snippets by remember { mutableStateOf(snippetsRepository.getAllSnippets()) }
    var showAddDialog by remember { mutableStateOf(false) }

    var newTitle by remember { mutableStateOf("") }
    var newCode by remember { mutableStateOf("") }
    var newCategory by remember { mutableStateOf("Coding") }

    fun refresh() {
        snippets = snippetsRepository.getAllSnippets()
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Gaming & Coding Tools",
                        fontWeight = FontWeight.Bold,
                        fontSize = 19.sp,
                        color = Color(0xFF0F172A)
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                            tint = Color(0xFF0F172A)
                        )
                    }
                },
                actions = {
                    IconButton(onClick = { showAddDialog = true }) {
                        Icon(imageVector = Icons.Default.Add, contentDescription = "Add Snippet", tint = Color(0xFF2563EB))
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color(0xFFF8FAFC))
            )
        },
        containerColor = Color(0xFFF8FAFC)
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
            contentPadding = PaddingValues(start = 16.dp, top = 16.dp, end = 16.dp, bottom = 48.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFFEFF6FF)),
                    border = CardDefaults.outlinedCardBorder()
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            text = "</> Developer & Gaming Power Bar",
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF1E3A8A)
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Access dedicated symbols { } [ ] ( ) < > / \\, quick macros (GG, glhf) and reusable code blocks right from the keyboard toolbar.",
                            fontSize = 12.sp,
                            color = Color(0xFF3B82F6),
                            lineHeight = 17.sp
                        )
                    }
                }
            }

            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Available Snippets & Macros",
                        modifier = Modifier.weight(1f, fill = false),
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF475569),
                        maxLines = 1
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    FilledTonalButton(
                        onClick = { showAddDialog = true },
                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                        shape = RoundedCornerShape(8.dp),
                        colors = ButtonDefaults.filledTonalButtonColors(
                            containerColor = Color(0xFFEFF6FF),
                            contentColor = Color(0xFF2563EB)
                        )
                    ) {
                        Text(
                            text = "+ New Snippet",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.SemiBold,
                            maxLines = 1,
                            softWrap = false
                        )
                    }
                }
            }

            items(snippets, key = { it.id }) { snippet ->
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    border = CardDefaults.outlinedCardBorder()
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(14.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = snippet.title,
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = Color(0xFF0F172A)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                AssistChip(
                                    onClick = {},
                                    label = { Text(snippet.category, fontSize = 10.sp) },
                                    colors = AssistChipDefaults.assistChipColors(containerColor = Color(0xFFF1F5F9))
                                )
                            }
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = snippet.code.replace("\n", " "),
                                fontSize = 12.sp,
                                fontFamily = FontFamily.Monospace,
                                color = Color(0xFF64748B),
                                maxLines = 1
                            )
                        }

                        if (snippet.isCustom) {
                            IconButton(onClick = {
                                snippetsRepository.deleteCustomSnippet(snippet.id)
                                refresh()
                            }) {
                                Icon(
                                    imageVector = Icons.Default.Delete,
                                    contentDescription = "Delete",
                                    tint = Color(0xFFEF4444)
                                )
                            }
                        }
                    }
                }
            }
        }
    }

    if (showAddDialog) {
        AlertDialog(
            onDismissRequest = { showAddDialog = false },
            title = { Text("Add Custom Snippet") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = newTitle,
                        onValueChange = { newTitle = it },
                        label = { Text("Snippet Title") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )

                    OutlinedTextField(
                        value = newCode,
                        onValueChange = { newCode = it },
                        label = { Text("Code or Macro Text") },
                        modifier = Modifier.fillMaxWidth(),
                        minLines = 3
                    )

                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        FilterChip(
                            selected = newCategory == "Coding",
                            onClick = { newCategory = "Coding" },
                            label = { Text("Coding") }
                        )
                        FilterChip(
                            selected = newCategory == "Gaming",
                            onClick = { newCategory = "Gaming" },
                            label = { Text("Gaming") }
                        )
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (newTitle.isNotBlank() && newCode.isNotBlank()) {
                            snippetsRepository.addCustomSnippet(newTitle, newCode, newCategory)
                            newTitle = ""
                            newCode = ""
                            showAddDialog = false
                            refresh()
                        }
                    }
                ) {
                    Text("Save")
                }
            },
            dismissButton = {
                TextButton(onClick = { showAddDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }
}
