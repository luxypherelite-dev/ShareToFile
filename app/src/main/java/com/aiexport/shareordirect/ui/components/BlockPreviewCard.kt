package com.aiexport.shareordirect.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.aiexport.shareordirect.core.Block
import com.aiexport.shareordirect.core.BlockType

@Composable
fun BlockPreviewCard(block: Block, modifier: Modifier = Modifier) {
    val chipColor = when (block.type) {
        BlockType.HEADING   -> MaterialTheme.colorScheme.primary
        BlockType.CODE      -> Color(0xFF37474F)
        BlockType.LIST      -> MaterialTheme.colorScheme.secondary
        BlockType.QA_PAIR   -> MaterialTheme.colorScheme.tertiary
        BlockType.PARAGRAPH -> MaterialTheme.colorScheme.surfaceVariant
    }
    val chipLabel = when (block.type) {
        BlockType.HEADING   -> "Heading"
        BlockType.CODE      -> "Code"
        BlockType.LIST      -> "List"
        BlockType.QA_PAIR   -> "Q&A"
        BlockType.PARAGRAPH -> "Paragraph"
    }

    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(10.dp),
        elevation = CardDefaults.cardElevation(2.dp)
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Surface(
                color = chipColor,
                shape = RoundedCornerShape(4.dp)
            ) {
                Text(
                    text = chipLabel,
                    color = Color.White,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.SemiBold,
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                )
            }
            Spacer(modifier = Modifier.height(6.dp))
            if (block.type == BlockType.CODE) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Color(0xFFF3F4F6), RoundedCornerShape(6.dp))
                        .padding(8.dp)
                ) {
                    Text(
                        text = block.content.take(300).let { if (block.content.length > 300) "$it…" else it },
                        fontFamily = FontFamily.Monospace,
                        fontSize = 12.sp
                    )
                }
            } else {
                Text(
                    text = block.content.take(300).let { if (block.content.length > 300) "$it…" else it },
                    fontSize = 13.sp,
                    lineHeight = 19.sp
                )
            }
        }
    }
}
