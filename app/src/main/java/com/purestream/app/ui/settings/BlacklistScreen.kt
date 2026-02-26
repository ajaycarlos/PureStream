package com.purestream.app.ui.settings

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.compose.ui.layout.ContentScale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BlacklistScreen(viewModel: SettingsViewModel = hiltViewModel()) {
    val words by viewModel.blacklist.collectAsState(initial = emptyList())
    var textState by remember { mutableStateOf("") }

    Scaffold(
        topBar = { TopAppBar(title = { Text("Mute Blacklist") }) }
    ) { padding ->
        Column(modifier = Modifier.padding(padding).padding(16.dp)) {
            Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                OutlinedTextField(
                    value = textState,
                    onValueChange = { textState = it },
                    modifier = Modifier.weight(1f),
                    label = { Text("Add word (e.g. shit)") }
                )
                Spacer(modifier = Modifier.width(8.dp))
                Button(onClick = { viewModel.addWord(textState); textState = "" }) {
                    Text("Add")
                }
            }
            Spacer(modifier = Modifier.height(16.dp))
            LazyColumn {
                items(words) { item ->
                    ListItem(
                        headlineContent = { Text(item.word) },
                        trailingContent = {
                            IconButton(onClick = { viewModel.removeWord(item) }) {
                                Icon(Icons.Default.Delete, contentDescription = "Delete")
                            }
                        }
                    )
                    HorizontalDivider()
                }
            }
        }
    }
}