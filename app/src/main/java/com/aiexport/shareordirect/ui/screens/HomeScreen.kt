package com.aiexport.shareordirect.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.aiexport.shareordirect.data.AppViewModel

@Composable
fun HomeScreen(vm: AppViewModel, padding: PaddingValues = PaddingValues(0.dp)) {
    val recentExports by vm.recentExports.collectAsState()
    val folderUri by vm.exportFolderUri.collectAsState()

    Column(
        modifier = Modifier
            .padding(padding)
            .fillMaxSize()
            .padding(24.dp)
    ) {
        Text("Share to File", style = MaterialTheme.typography.headlineMedium)
        Spacer(Modifier.height(8.dp))
        Text(
            "Share any text or link from another app, or use the overlay button inside a watched AI app — " +
            "both export your conversation as PDF, TXT, or Markdown.",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        Spacer(Modifier.height(20.dp))

        if (folderUri == null) {
            Card(
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.errorContainer
                )
            ) {
                Text(
                    "No export folder set. Go to Settings to pick one.",
                    modifier = Modifier.padding(12.dp),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onErrorContainer
                )
            }
            Spacer(Modifier.height(16.dp))
        }

        if (recentExports.isNotEmpty()) {
            Text("Recent exports", style = MaterialTheme.typography.titleSmall)
            Spacer(Modifier.height(8.dp))
            LazyColumn(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                items(recentExports) { name ->
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            Icons.Default.CheckCircle,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(Modifier.width(8.dp))
                        Text(name, style = MaterialTheme.typography.bodySmall)
                    }
                }
            }
        } else {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("No exports yet", style = MaterialTheme.typography.bodyLarge, color = MaterialTheme.colorScheme.outline)
                    Spacer(Modifier.height(4.dp))
                    Text("Share content from any app to get started", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.outlineVariant)
                }
            }
        }
    }
}
