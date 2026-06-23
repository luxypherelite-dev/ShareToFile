package com.aiexport.shareordirect.ui.screens

import android.content.ClipboardManager
import android.content.Context
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.ContentPaste
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.aiexport.shareordirect.data.AppViewModel
import com.aiexport.shareordirect.util.UrlFetcher

@Composable
fun HomeScreen(
    vm: AppViewModel,
    onNavigateToShare: () -> Unit,
    padding: PaddingValues = PaddingValues(0.dp)
) {
    val ctx           = LocalContext.current
    val recentExports by vm.recentExports.collectAsState()
    val folderUri     by vm.exportFolderUri.collectAsState()

    var linkText by remember { mutableStateOf("") }
    var linkError by remember { mutableStateOf("") }

    fun fetchLink() {
        val url = linkText.trim()
        if (url.isBlank()) { linkError = "Paste a link first"; return }
        if (!UrlFetcher.isUrl(url)) { linkError = "Doesn't look like a URL — must start with https://"; return }
        linkError = ""
        linkText  = ""
        vm.processInput(url, "Pasted link")
        onNavigateToShare()
    }

    Column(
        modifier = Modifier
            .padding(padding)
            .fillMaxSize()
            .padding(horizontal = 20.dp, vertical = 16.dp)
    ) {
        Text("Share to File", style = MaterialTheme.typography.headlineMedium)
        Spacer(Modifier.height(4.dp))
        Text(
            "Export AI conversations as PDF, TXT, or Markdown.",
            style = MaterialTheme.typography.bodyMedium,
            color  = MaterialTheme.colorScheme.onSurfaceVariant
        )

        Spacer(Modifier.height(20.dp))

        // ── Paste-link card ──────────────────────────────────────────────────
        Card(
            modifier = Modifier.fillMaxWidth(),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text("Paste a link", style = MaterialTheme.typography.titleSmall)
                Spacer(Modifier.height(10.dp))

                OutlinedTextField(
                    value         = linkText,
                    onValueChange = { linkText = it; linkError = "" },
                    modifier      = Modifier.fillMaxWidth(),
                    placeholder   = { Text("https://chatgpt.com/share/…") },
                    singleLine    = true,
                    isError       = linkError.isNotEmpty(),
                    supportingText = if (linkError.isNotEmpty()) {
                        { Text(linkError, color = MaterialTheme.colorScheme.error) }
                    } else null,
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Uri,
                        imeAction    = ImeAction.Go
                    ),
                    keyboardActions = KeyboardActions(onGo = { fetchLink() }),
                    trailingIcon = {
                        IconButton(onClick = {
                            val clip = ctx.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                            val pasted = clip.primaryClip?.getItemAt(0)?.text?.toString() ?: ""
                            if (pasted.isNotBlank()) { linkText = pasted; linkError = "" }
                        }) {
                            Icon(Icons.Default.ContentPaste, contentDescription = "Paste from clipboard")
                        }
                    }
                )

                Spacer(Modifier.height(10.dp))

                Button(
                    onClick   = { fetchLink() },
                    modifier  = Modifier.fillMaxWidth(),
                    enabled   = linkText.isNotBlank()
                ) {
                    Text("Fetch & Export")
                }
            }
        }

        Spacer(Modifier.height(16.dp))

        // ── No folder warning ────────────────────────────────────────────────
        if (folderUri == null) {
            Card(
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.errorContainer
                ),
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    "⚠ No export folder set — go to Settings to pick one before exporting.",
                    modifier = Modifier.padding(12.dp),
                    style    = MaterialTheme.typography.bodySmall,
                    color    = MaterialTheme.colorScheme.onErrorContainer
                )
            }
            Spacer(Modifier.height(16.dp))
        }

        // ── Recent exports ───────────────────────────────────────────────────
        if (recentExports.isNotEmpty()) {
            Text("Recent exports", style = MaterialTheme.typography.titleSmall)
            Spacer(Modifier.height(8.dp))
            LazyColumn(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                items(recentExports) { name ->
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            Icons.Default.CheckCircle,
                            contentDescription = null,
                            tint     = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(Modifier.width(8.dp))
                        Text(name, style = MaterialTheme.typography.bodySmall)
                    }
                }
            }
        } else {
            Spacer(Modifier.height(8.dp))
            Box(Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                Text(
                    "Paste a link above, or share text from any AI app to begin.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.outlineVariant
                )
            }
        }
    }
}
