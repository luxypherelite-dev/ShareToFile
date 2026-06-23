package com.aiexport.shareordirect.core.renderer

import com.aiexport.shareordirect.core.Block
import com.aiexport.shareordirect.core.BlockType

object MarkdownRenderer {
    fun render(blocks: List<Block>): String {
        val sb = StringBuilder()
        for (block in blocks) {
            when (block.type) {
                BlockType.HEADING -> sb.appendLine("## ${block.content}").appendLine()
                BlockType.CODE -> {
                    sb.appendLine("```")
                    sb.appendLine(block.content)
                    sb.appendLine("```")
                    sb.appendLine()
                }
                BlockType.LIST -> {
                    block.content.lines().forEach { line ->
                        if (line.isNotBlank()) sb.appendLine(line) else sb.appendLine()
                    }
                    sb.appendLine()
                }
                BlockType.QA_PAIR -> {
                    val parts = block.content.split("\n", limit = 2)
                    sb.appendLine("**${parts[0]}**")
                    sb.appendLine()
                    if (parts.size > 1) sb.appendLine(parts[1])
                    sb.appendLine()
                }
                BlockType.PARAGRAPH -> sb.appendLine(block.content).appendLine()
            }
        }
        return sb.toString().trimEnd()
    }
}
