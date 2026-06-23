package com.aiexport.shareordirect.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.aiexport.shareordirect.util.ExportFormat

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FormatPickerSheet(
    onDismiss: () -> Unit,
    onFormatSelected: (ExportFormat) -> Unit
) {
    ModalBottomSheet(onDismissRequest = onDismiss) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp)
                .padding(bottom = 40.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "Export as…",
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier.padding(bottom = 20.dp)
            )
            Row(
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                FormatButton("PDF", Modifier.weight(1f)) { onFormatSelected(ExportFormat.PDF); onDismiss() }
                FormatButton("TXT", Modifier.weight(1f)) { onFormatSelected(ExportFormat.TXT); onDismiss() }
                FormatButton("MD",  Modifier.weight(1f)) { onFormatSelected(ExportFormat.MD);  onDismiss() }
            }
        }
    }
}

@Composable
private fun FormatButton(label: String, modifier: Modifier = Modifier, onClick: () -> Unit) {
    FilledTonalButton(onClick = onClick, modifier = modifier.height(56.dp)) {
        Text(label, style = MaterialTheme.typography.titleSmall)
    }
}
