package com.aiexport.shareordirect.util

import android.content.Context
import android.net.Uri
import android.provider.DocumentsContract
import com.aiexport.shareordirect.core.Block
import com.aiexport.shareordirect.core.renderer.MarkdownRenderer
import com.aiexport.shareordirect.core.renderer.PdfRenderer
import com.aiexport.shareordirect.core.renderer.TxtRenderer
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

enum class ExportFormat { PDF, TXT, MD }

object FileSaver {

    fun save(ctx: Context, blocks: List<Block>, format: ExportFormat, folderUri: Uri): Uri {
        val timestamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.US).format(Date())
        val ext = format.name.lowercase()
        val filename = "chat_$timestamp.$ext"
        val mimeType = when (format) {
            ExportFormat.PDF -> "application/pdf"
            ExportFormat.TXT -> "text/plain"
            ExportFormat.MD  -> "text/markdown"
        }

        val docUri = DocumentsContract.createDocument(
            ctx.contentResolver, folderUri, mimeType, filename
        ) ?: error("Could not create file in the selected folder. Please choose the folder again in Settings.")

        ctx.contentResolver.openOutputStream(docUri)?.use { out ->
            when (format) {
                ExportFormat.PDF -> PdfRenderer.render(blocks, out)
                ExportFormat.TXT -> out.write(TxtRenderer.render(blocks).toByteArray(Charsets.UTF_8))
                ExportFormat.MD  -> out.write(MarkdownRenderer.render(blocks).toByteArray(Charsets.UTF_8))
            }
        } ?: error("Could not open output stream for the selected folder.")

        return docUri
    }

    fun takeFolderPermission(ctx: Context, treeUri: Uri) {
        try {
            ctx.contentResolver.takePersistableUriPermission(
                treeUri,
                android.content.Intent.FLAG_GRANT_READ_URI_PERMISSION or
                android.content.Intent.FLAG_GRANT_WRITE_URI_PERMISSION
            )
        } catch (e: Exception) {
            // Permission already held or revoked — non-fatal
        }
    }

    fun treeToDocumentUri(treeUri: Uri): Uri =
        DocumentsContract.buildDocumentUriUsingTree(
            treeUri,
            DocumentsContract.getTreeDocumentId(treeUri)
        )
}
