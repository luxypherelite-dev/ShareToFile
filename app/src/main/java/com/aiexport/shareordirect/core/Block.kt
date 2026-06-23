package com.aiexport.shareordirect.core

enum class BlockType { HEADING, CODE, LIST, QA_PAIR, PARAGRAPH }

data class Block(
    val type: BlockType,
    val content: String,
    val meta: Map<String, String> = emptyMap()
)
