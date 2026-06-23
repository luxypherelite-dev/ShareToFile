package com.aiexport.shareordirect.data

import android.app.Application
import android.net.Uri
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.aiexport.shareordirect.core.Block
import com.aiexport.shareordirect.core.BlockParser
import com.aiexport.shareordirect.util.ExportFormat
import com.aiexport.shareordirect.util.FileSaver
import com.aiexport.shareordirect.util.UrlFetcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

sealed class ParseState {
    object Idle : ParseState()
    object Loading : ParseState()
    data class Ready(val blocks: List<Block>, val rawText: String) : ParseState()
    data class Error(val message: String) : ParseState()
}

sealed class SaveState {
    object Idle : SaveState()
    object Saving : SaveState()
    data class Success(val uri: Uri) : SaveState()
    data class Error(val message: String) : SaveState()
}

class AppViewModel(app: Application) : AndroidViewModel(app) {

    private val ctx = app.applicationContext

    val exportFolderUri: StateFlow<Uri?> = AppDataStore.exportFolderUri(ctx)
        .catch { emit(null) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    val watchedApps: StateFlow<Set<String>> = AppDataStore.watchedApps(ctx)
        .catch { emit(emptySet()) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptySet())

    val maxScrollIterations: StateFlow<Int> = AppDataStore.maxScrollIterations(ctx)
        .catch { emit(200) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 200)

    private val _parseState = MutableStateFlow<ParseState>(ParseState.Idle)
    val parseState: StateFlow<ParseState> = _parseState.asStateFlow()

    private val _saveState = MutableStateFlow<SaveState>(SaveState.Idle)
    val saveState: StateFlow<SaveState> = _saveState.asStateFlow()

    private val _recentExports = MutableStateFlow<List<String>>(emptyList())
    val recentExports: StateFlow<List<String>> = _recentExports.asStateFlow()

    fun processInput(rawInput: String, sourceLabel: String = "") {
        if (rawInput.isBlank()) {
            _parseState.value = ParseState.Error("No content received")
            return
        }
        viewModelScope.launch {
            _parseState.value = ParseState.Loading
            try {
                val text = withContext(Dispatchers.IO) {
                    if (UrlFetcher.isUrl(rawInput.trim())) {
                        UrlFetcher.fetchText(rawInput.trim()) ?: rawInput
                    } else {
                        rawInput
                    }
                }
                val blocks = withContext(Dispatchers.Default) { BlockParser.parse(text) }
                if (blocks.isEmpty()) {
                    _parseState.value = ParseState.Error("No content blocks found in this text")
                } else {
                    _parseState.value = ParseState.Ready(blocks, text)
                }
            } catch (e: Exception) {
                _parseState.value = ParseState.Error(e.message ?: "Failed to parse content")
            }
        }
    }

    fun export(blocks: List<Block>, format: ExportFormat) {
        val folderUri = exportFolderUri.value
        if (folderUri == null) {
            _saveState.value = SaveState.Error("No export folder set. Go to Settings.")
            return
        }
        if (blocks.isEmpty()) {
            _saveState.value = SaveState.Error("Nothing to export")
            return
        }
        viewModelScope.launch {
            _saveState.value = SaveState.Saving
            try {
                val docUri = withContext(Dispatchers.IO) {
                    FileSaver.save(ctx, blocks, format, folderUri)
                }
                _saveState.value = SaveState.Success(docUri)
                val name = docUri.lastPathSegment ?: "export.${format.name.lowercase()}"
                _recentExports.value = (listOf(name) + _recentExports.value).take(10)
                _parseState.value = ParseState.Idle
            } catch (e: Exception) {
                _saveState.value = SaveState.Error(e.message ?: "Export failed")
            }
        }
    }

    fun setExportFolder(treeUri: Uri) {
        viewModelScope.launch {
            try {
                FileSaver.takeFolderPermission(ctx, treeUri)
                val docUri = FileSaver.treeToDocumentUri(treeUri)
                AppDataStore.setExportFolderUri(ctx, docUri)
            } catch (e: Exception) {
                _saveState.value = SaveState.Error("Could not save folder: ${e.message}")
            }
        }
    }

    fun setWatchedApps(packages: Set<String>) {
        viewModelScope.launch { AppDataStore.setWatchedApps(ctx, packages) }
    }

    fun setMaxScrollIterations(value: Int) {
        viewModelScope.launch { AppDataStore.setMaxScrollIterations(ctx, value.coerceIn(10, 500)) }
    }

    fun resetSaveState()  { _saveState.value  = SaveState.Idle }
    fun resetParseState() { _parseState.value = ParseState.Idle }
}
