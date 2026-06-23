package com.aiexport.shareordirect.core.renderer

import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Typeface
import android.graphics.pdf.PdfDocument
import com.aiexport.shareordirect.core.Block
import com.aiexport.shareordirect.core.BlockType
import java.io.OutputStream

object PdfRenderer {

    private const val PAGE_WIDTH = 595
    private const val PAGE_HEIGHT = 842
    private const val MARGIN = 50f
    private const val LINE_HEIGHT_NORMAL = 18f
    private const val LINE_HEIGHT_CODE = 16f

    private class DrawState {
        var pageNum = 0
        var page: PdfDocument.Page? = null
        var canvas: Canvas? = null
        var y = MARGIN
    }

    fun render(blocks: List<Block>, outputStream: OutputStream) {
        val doc = PdfDocument()
        val state = DrawState()
        newPage(doc, state)
        for (block in blocks) {
            when (block.type) {
                BlockType.HEADING   -> drawHeading(doc, state, block.content)
                BlockType.CODE      -> drawCode(doc, state, block.content)
                BlockType.LIST      -> drawList(doc, state, block.content)
                BlockType.QA_PAIR   -> drawQa(doc, state, block)
                BlockType.PARAGRAPH -> drawParagraph(doc, state, block.content)
            }
        }
        if (state.page != null) doc.finishPage(state.page)
        doc.writeTo(outputStream)
        doc.close()
    }

    private fun newPage(doc: PdfDocument, state: DrawState) {
        if (state.page != null) doc.finishPage(state.page)
        state.pageNum++
        val pi = PdfDocument.PageInfo.Builder(PAGE_WIDTH, PAGE_HEIGHT, state.pageNum).create()
        state.page = doc.startPage(pi)
        state.canvas = state.page!!.canvas
        state.y = MARGIN
    }

    private fun ensureSpace(doc: PdfDocument, state: DrawState, needed: Float) {
        if (state.y + needed > PAGE_HEIGHT - MARGIN) newPage(doc, state)
    }

    private fun c(state: DrawState) = state.canvas!!

    private val normalPaint  = Paint().apply { textSize = 12f; color = Color.BLACK; isAntiAlias = true }
    private val boldPaint    = Paint().apply { textSize = 13f; color = Color.BLACK; isFakeBoldText = true; isAntiAlias = true }
    private val headingPaint = Paint().apply { textSize = 18f; color = Color.BLACK; isFakeBoldText = true; isAntiAlias = true }
    private val codePaint    = Paint().apply { textSize = 11f; color = Color.parseColor("#222222"); typeface = Typeface.MONOSPACE; isAntiAlias = true }
    private val codeBoxPaint = Paint().apply { color = Color.parseColor("#F3F4F6"); style = Paint.Style.FILL }

    private fun drawHeading(doc: PdfDocument, state: DrawState, text: String) {
        ensureSpace(doc, state, 34f)
        state.y += 8f
        c(state).drawText(text.take(80), MARGIN, state.y, headingPaint)
        state.y += 26f
    }

    private fun drawCode(doc: PdfDocument, state: DrawState, text: String) {
        val lines = text.lines()
        val boxH = lines.size * LINE_HEIGHT_CODE + 16f
        ensureSpace(doc, state, boxH)
        c(state).drawRect(MARGIN - 6f, state.y, (PAGE_WIDTH - MARGIN + 6f), state.y + boxH, codeBoxPaint)
        state.y += 8f
        for (line in lines) {
            c(state).drawText(line.take(90), MARGIN, state.y, codePaint)
            state.y += LINE_HEIGHT_CODE
        }
        state.y += 10f
    }

    private fun drawList(doc: PdfDocument, state: DrawState, text: String) {
        for (line in text.lines()) {
            if (line.isBlank()) continue
            ensureSpace(doc, state, LINE_HEIGHT_NORMAL + 4f)
            c(state).drawText(line.take(90), MARGIN, state.y, normalPaint)
            state.y += LINE_HEIGHT_NORMAL
        }
        state.y += 6f
    }

    private fun drawQa(doc: PdfDocument, state: DrawState, block: Block) {
        val question = block.meta["question"] ?: ""
        val answer   = block.meta["answer"]   ?: ""
        ensureSpace(doc, state, LINE_HEIGHT_NORMAL + 4f)
        c(state).drawText(question.take(90), MARGIN, state.y, boldPaint)
        state.y += LINE_HEIGHT_NORMAL + 4f
        for (line in answer.lines()) {
            if (line.isBlank()) { state.y += 6f; continue }
            ensureSpace(doc, state, LINE_HEIGHT_NORMAL)
            c(state).drawText(line.take(90), MARGIN + 14f, state.y, normalPaint)
            state.y += LINE_HEIGHT_NORMAL
        }
        state.y += 8f
    }

    private fun drawParagraph(doc: PdfDocument, state: DrawState, text: String) {
        for (line in text.lines()) {
            if (line.isBlank()) { state.y += 6f; continue }
            ensureSpace(doc, state, LINE_HEIGHT_NORMAL)
            c(state).drawText(line.take(90), MARGIN, state.y, normalPaint)
            state.y += LINE_HEIGHT_NORMAL
        }
        state.y += 8f
    }
}
