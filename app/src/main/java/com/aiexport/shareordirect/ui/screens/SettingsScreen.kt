package com.aiexport.shareordirect.ui.screens

import android.content.Intent
import android.net.Uri
import android.provider.Settings
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.aiexport.shareordirect.data.AppViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(vm: AppViewModel, padding: PaddingValues = PaddingValues(0.dp)) {
    val ctx              = LocalContext.current
    val exportFolderUri  by vm.exportFolderUri.collectAsState()
    val watchedApps      by vm.watchedApps.collectAsState()
    val maxIter          by vm.maxScrollIterations.collectAsState()
    var maxIterText      by remember { mutableStateOf(maxIter.toString()) }
    var showOverlayInfo  by remember { mutableStateOf(false) }

    val folderPicker = rememberLauncherForActivityResult(
        ActivityResultContracts.OpenDocumentTree()
    ) { uri ->
        if (uri != null) {
            ctx.contentResolver.takePersistableUriPermission(
                uri,
                Intent.FLAG_GRANT_READ_URI_PERMISSION or Intent.FLAG_GRANT_WRITE_URI_PERMISSION
            )
            vm.setExportFolder(uri)
        }
    }

    LazyColumn(
        modifier = Modifier.padding(padding).fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Text("Settings", style = MaterialTheme.typography.headlineMedium)
            Spacer(Modifier.height(4.dp))
        }

        // Export folder section
        item {
            Card(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Folder, null, tint = MaterialTheme.colorScheme.primary)
                        Spacer(Modifier.width(8.dp))
                        Text("Export Folder", style = MaterialTheme.typography.titleMedium)
                    }
                    Spacer(Modifier.height(8.dp))
                    Text(
                        exportFolderUri?.lastPathSegment ?: "Not set",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(Modifier.height(12.dp))
                    Button(onClick = { folderPicker.launch(null) }) {
                        Text(if (exportFolderUri == null) "Choose folder" else "Change folder")
                    }
                }
            }
        }

        // Accessibility / Overlay section
        item {
            Card(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("Overlay Capture", style = MaterialTheme.typography.titleMedium)
                    Spacer(Modifier.height(4.dp))
                    Text(
                        "Enables a floating button inside watched apps that captures the full conversation.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(Modifier.height(12.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedButton(onClick = {
                            ctx.startActivity(Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS))
                        }) { Text("Enable Accessibility") }
                        OutlinedButton(onClick = {
                            ctx.startActivity(Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                                Uri.parse("package:${ctx.packageName}")))
                        }) { Text("Allow Overlay") }
                    }
                }
            }
        }

        // Watched apps
        item {
            Card(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("Watched Apps", style = MaterialTheme.typography.titleMedium)
                        var showAddDialog by remember { mutableStateOf(false) }
                        IconButton(onClick = { showAddDialog = true }) {
                            Icon(Icons.Default.Add, contentDescription = "Add")
                        }
                        if (showAddDialog) {
                            var inputText by remember { mutableStateOf("") }
                            AlertDialog(
                                onDismissRequest = { showAddDialog = false },
                                title = { Text("Add App Package") },
                                text = {
                                    Column {
                                        Text("Enter the package name (e.g. com.openai.chatgpt)", style = MaterialTheme.typography.bodySmall)
                                        Spacer(Modifier.height(8.dp))
                                        OutlinedTextField(
                                            value = inputText,
                                            onValueChange = { inputText = it },
                                            placeholder = { Text("com.example.app") },
                                            singleLine = true
                                        )
                                    }
                                },
                                confirmButton = {
                                    TextButton(onClick = {
                                        if (inputText.isNotBlank()) {
                                            vm.setWatchedApps(watchedApps + inputText.trim())
                                        }
                                        showAddDialog = false
                                    }) { Text("Add") }
                                },
                                dismissButton = {
                                    TextButton(onClick = { showAddDialog = false }) { Text("Cancel") }
                                }
                            )
                        }
                    }
                    if (watchedApps.isEmpty()) {
                        Text("No apps added yet", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.outline)
                    } else {
                        watchedApps.forEach { pkg ->
                            Row(
                                modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(pkg, style = MaterialTheme.typography.bodyMedium, modifier = Modifier.weight(1f))
                                IconButton(onClick = { vm.setWatchedApps(watchedApps - pkg) }) {
                                    Icon(Icons.Default.Delete, contentDescription = "Remove", tint = MaterialTheme.colorScheme.error)
                                }
                            }
                        }
                    }
                }
            }
        }

        // Max scroll iterations
        item {
            Card(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("Max Scroll Iterations", style = MaterialTheme.typography.titleMedium)
                    Spacer(Modifier.height(8.dp))
                    OutlinedTextField(
                        value = maxIterText,
                        onValueChange = {
                            maxIterText = it
                            it.toIntOrNull()?.let { v -> vm.setMaxScrollIterations(v) }
                        },
                        label = { Text("Iterations (default 200)") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }
        }
    }
}
