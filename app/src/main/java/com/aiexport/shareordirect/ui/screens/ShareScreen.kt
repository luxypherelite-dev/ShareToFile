package com.aiexport.shareordirect.ui.screens

import android.content.Intent
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.aiexport.shareordirect.core.Block
import com.aiexport.shareordirect.data.AppViewModel
import com.aiexport.shareordirect.data.ParseState
import com.aiexport.shareordirect.data.SaveState
import com.aiexport.shareordirect.ui.components.BlockPreviewCard
import com.aiexport.shareordirect.ui.components.FormatPickerSheet
import com.aiexport.shareordirect.util.ExportFormat

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ShareScreenContent(
    vm: AppViewModel,
    onBack: (() -> Unit)?,
    padding: PaddingValues = PaddingValues(0.dp)
) {
    val ctx             = LocalContext.current
    val parseState      by vm.parseState.collectAsState()
    val saveState       by vm.saveState.collectAsState()
    val exportFolderUri by vm.exportFolderUri.collectAsState()

    var showFormatPicker by remember { mutableStateOf(false) }
    var pendingBlocks    by remember { mutableStateOf<List<Block>>(emptyList()) }

    val folderPicker = rememberLauncherForActivityResult(
        ActivityResultContracts.OpenDocumentTree()
    ) { uri ->
        if (uri != null) {
            runCatching {
                ctx.contentResolver.takePersistableUriPermission(
                    uri,
                    Intent.FLAG_GRANT_READ_URI_PERMISSION or Intent.FLAG_GRANT_WRITE_URI_PERMISSION
                )
            }
            vm.setExportFolder(uri)
        }
    }

    val snackbarHost = remember { SnackbarHostState() }

    LaunchedEffect(parseState) {
        val s = parseState
        if (s is ParseState.Ready) {
            pendingBlocks = s.blocks
            showFormatPicker = true
        }
    }

    LaunchedEffect(saveState) {
        when (val s = saveState) {
            is SaveState.Success -> { snackbarHost.showSnackbar("Saved!"); vm.resetSaveState() }
            is SaveState.Error   -> { snackbarHost.showSnackbar("Error: ${s.message}"); vm.resetSaveState() }
            else -> {}
        }
    }

    Scaffold(
        topBar = {
            if (onBack != null) {
                TopAppBar(
                    title = { Text("Captured Content") },
                    navigationIcon = {
                        IconButton(onClick = onBack) {
                            Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                        }
                    }
                )
            }
        },
        snackbarHost = { SnackbarHost(snackbarHost) }
    ) { scaffoldPadding ->

        Box(modifier = Modifier.padding(scaffoldPadding).padding(padding)) {
            when (val state = parseState) {
                is ParseState.Idle -> {
                    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(4.dp)) {
                            Text("Nothing parsed yet", style = MaterialTheme.typography.bodyLarge, color = MaterialTheme.colorScheme.outline)
                            Text("Share text from another app to begin", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.outlineVariant)
                        }
                    }
                }

                is ParseState.Loading -> {
                    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(12.dp)) {
                            CircularProgressIndicator()
                            Text("Parsing content…")
                        }
                    }
                }

                is ParseState.Ready -> {
                    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
                        Text(
                            "${state.blocks.size} blocks detected",
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.outline
                        )
                        Spacer(Modifier.height(8.dp))
                        LazyColumn(
                            modifier = Modifier.weight(1f),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            items(state.blocks) { block -> BlockPreviewCard(block) }
                        }
                        Spacer(Modifier.height(12.dp))
                        if (exportFolderUri == null) {
                            OutlinedButton(
                                onClick = { folderPicker.launch(null) },
                                modifier = Modifier.fillMaxWidth()
                            ) { Text("Set export folder first") }
                            Spacer(Modifier.height(8.dp))
                        }
                        Button(
                            onClick = {
                                if (exportFolderUri != null) {
                                    pendingBlocks = state.blocks
                                    showFormatPicker = true
                                } else {
                                    folderPicker.launch(null)
                                }
                            },
                            modifier = Modifier.fillMaxWidth(),
                            enabled = saveState !is SaveState.Saving
                        ) {
                            if (saveState is SaveState.Saving) {
                                CircularProgressIndicator(Modifier.size(18.dp), strokeWidth = 2.dp)
                            } else {
                                Text("Export…")
                            }
                        }
                    }
                }

                is ParseState.Error -> {
                    Box(Modifier.fillMaxSize().padding(16.dp), contentAlignment = Alignment.Center) {
                        Text("Error: ${state.message}", color = MaterialTheme.colorScheme.error)
                    }
                }
            }
        }
    }

    if (showFormatPicker) {
        FormatPickerSheet(
            onDismiss = { showFormatPicker = false },
            onFormatSelected = { fmt ->
                vm.export(pendingBlocks, fmt)
                showFormatPicker = false
            }
        )
    }
}
