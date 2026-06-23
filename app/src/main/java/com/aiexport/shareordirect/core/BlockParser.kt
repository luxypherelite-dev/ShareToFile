package com.aiexport.shareordirect.core

object BlockParser {

    fun parse(raw: String): List<Block> {
        val lines = raw.lines()
        val blocks = mutableListOf<Block>()
        var i = 0
        while (i < lines.size) {
            when {
                isCodeFenceStart(lines[i]) -> {
                    val (block, next) = consumeFencedCode(lines, i)
                    blocks.add(block)
                    i = next
                }
                isIndentedCodeBlock(lines, i) -> {
                    val (block, next) = consumeIndentedCode(lines, i)
                    blocks.add(block)
                    i = next
                }
                isListItem(lines[i]) -> {
                    val (block, next) = consumeList(lines, i)
                    blocks.add(block)
                    i = next
                }
                isQaPair(lines, i) -> {
                    val (block, next) = consumeQaPair(lines, i)
                    blocks.add(block)
                    i = next
                }
                isHeading(lines, i) -> {
                    blocks.add(Block(BlockType.HEADING, lines[i].trim()))
                    i++
                }
                lines[i].isBlank() -> i++
                else -> {
                    val (block, next) = consumeParagraph(lines, i)
                    blocks.add(block)
                    i = next
                }
            }
        }
        return blocks
    }

    // ── Fenced code ────────────────────────────────────────────────────────────
    private fun isCodeFenceStart(line: String) = line.trim().startsWith("```")

    private fun consumeFencedCode(lines: List<String>, start: Int): Pair<Block, Int> {
        val sb = StringBuilder()
        var i = start + 1
        while (i < lines.size && !lines[i].trim().startsWith("```")) {
            sb.appendLine(lines[i])
            i++
        }
        return Block(BlockType.CODE, sb.toString().trimEnd()) to (i + 1)
    }

    // ── Indented / symbol-heavy code ───────────────────────────────────────────
    private val codeSymbols = setOf("{", "}", ";", "=>", "def ", "function ", "->", "import ", "class ", "return ")

    private fun looksLikeCode(line: String): Boolean {
        val trimmed = line.trimStart()
        val indented = line.length - trimmed.length >= 4
        val hasSymbol = codeSymbols.any { trimmed.contains(it) }
        return indented && hasSymbol
    }

    private fun isIndentedCodeBlock(lines: List<String>, start: Int): Boolean {
        if (start >= lines.size) return false
        val line = lines[start]
        if (line.isBlank()) return false
        // Need at least 2 consecutive code-like lines to confirm
        return looksLikeCode(line) && (start + 1 < lines.size && (looksLikeCode(lines[start + 1]) || lines[start + 1].isBlank()))
    }

    private fun consumeIndentedCode(lines: List<String>, start: Int): Pair<Block, Int> {
        val sb = StringBuilder()
        var i = start
        while (i < lines.size && (looksLikeCode(lines[i]) || (lines[i].isBlank() && i + 1 < lines.size && looksLikeCode(lines[i + 1])))) {
            sb.appendLine(lines[i])
            i++
        }
        return Block(BlockType.CODE, sb.toString().trimEnd()) to i
    }

    // ── List ───────────────────────────────────────────────────────────────────
    private val listPrefixRegex = Regex("""^(\s*)(-|\*|•|\d+[.)]) """)

    private fun isListItem(line: String) = listPrefixRegex.containsMatchIn(line)

    private fun consumeList(lines: List<String>, start: Int): Pair<Block, Int> {
        val sb = StringBuilder()
        var i = start
        while (i < lines.size && (isListItem(lines[i]) || (lines[i].isBlank() && i + 1 < lines.size && isListItem(lines[i + 1])))) {
            sb.appendLine(lines[i])
            i++
        }
        return Block(BlockType.LIST, sb.toString().trimEnd()) to i
    }

    // ── Q&A ────────────────────────────────────────────────────────────────────
    private fun looksLikeQuestion(line: String): Boolean {
        val t = line.trim()
        return t.endsWith("?") && t.length in 5..200 && !isListItem(t)
    }

    private fun isQaPair(lines: List<String>, start: Int): Boolean {
        if (!looksLikeQuestion(lines[start])) return false
        val next = start + 1
        if (next >= lines.size) return false
        val nextLine = lines[next]
        return nextLine.isNotBlank() && nextLine.trim().length > 30
    }

    private fun consumeQaPair(lines: List<String>, start: Int): Pair<Block, Int> {
        val question = lines[start].trim()
        val answerSb = StringBuilder()
        var i = start + 1
        while (i < lines.size && !looksLikeQuestion(lines[i]) && !isHeading(lines, i) && !lines[i].isBlank()) {
            answerSb.appendLine(lines[i])
            i++
        }
        val content = "$question\n${answerSb.toString().trimEnd()}"
        return Block(BlockType.QA_PAIR, content, mapOf("question" to question, "answer" to answerSb.toString().trimEnd())) to i
    }

    // ── Heading ────────────────────────────────────────────────────────────────
    private fun isHeading(lines: List<String>, idx: Int): Boolean {
        val line = lines[idx].trim()
        if (line.isBlank() || line.length > 100) return false
        val nextIdx = idx + 1
        val followedByBlank = nextIdx >= lines.size || lines[nextIdx].isBlank()
        val noTrailingPunct = !line.last().let { it == '.' || it == ',' || it == ';' }
        val startsCapital = line.first().isUpperCase()
        val wordCount = line.split(" ").size
        return followedByBlank && noTrailingPunct && startsCapital && wordCount in 1..8
    }

    // ── Paragraph ─────────────────────────────────────────────────────────────
    private fun consumeParagraph(lines: List<String>, start: Int): Pair<Block, Int> {
        val sb = StringBuilder()
        var i = start
        while (i < lines.size &&
            lines[i].isNotBlank() &&
            !isCodeFenceStart(lines[i]) &&
            !isListItem(lines[i]) &&
            !looksLikeQuestion(lines[i])
        ) {
            sb.appendLine(lines[i])
            i++
        }
        return Block(BlockType.PARAGRAPH, sb.toString().trimEnd()) to i
    }
}
