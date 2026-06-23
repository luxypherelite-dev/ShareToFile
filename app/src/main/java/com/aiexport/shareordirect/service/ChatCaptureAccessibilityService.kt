package com.aiexport.shareordirect.service

import android.accessibilityservice.AccessibilityService
import android.accessibilityservice.AccessibilityServiceInfo
import android.content.Intent
import android.graphics.Rect
import android.os.Handler
import android.os.Looper
import android.provider.Settings
import android.view.accessibility.AccessibilityEvent
import android.view.accessibility.AccessibilityNodeInfo
import com.aiexport.shareordirect.data.AppDataStore
import com.aiexport.shareordirect.ui.screens.CaptureResultActivity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class ChatCaptureAccessibilityService : AccessibilityService() {

    companion object {
        var instance: ChatCaptureAccessibilityService? = null
    }

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Main)
    private var overlayManager: OverlayManager? = null
    private var watchedApps = emptySet<String>()
    private var currentForegroundPackage = ""
    private var isCapturing = false
    private var maxIterations = 200

    override fun onServiceConnected() {
        super.onServiceConnected()
        instance = this
        val info = AccessibilityServiceInfo().apply {
            eventTypes = AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED or
                    AccessibilityEvent.TYPE_WINDOW_CONTENT_CHANGED
            feedbackType = AccessibilityServiceInfo.FEEDBACK_GENERIC
            flags = AccessibilityServiceInfo.FLAG_REPORT_VIEW_IDS or
                    AccessibilityServiceInfo.FLAG_RETRIEVE_INTERACTIVE_WINDOWS
            notificationTimeout = 100
        }
        serviceInfo = info

        scope.launch {
            AppDataStore.watchedApps(applicationContext).collect { apps -> watchedApps = apps }
        }
        scope.launch {
            AppDataStore.maxScrollIterations(applicationContext).collect { m -> maxIterations = m }
        }
        overlayManager = OverlayManager(this) { startCapture() }
    }

    override fun onAccessibilityEvent(event: AccessibilityEvent) {
        if (event.eventType == AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED) {
            val pkg = event.packageName?.toString() ?: return
            if (pkg == currentForegroundPackage) return
            currentForegroundPackage = pkg
            if (!Settings.canDrawOverlays(this)) return
            if (pkg in watchedApps) overlayManager?.show() else overlayManager?.hide()
        }
    }

    override fun onInterrupt() { overlayManager?.hide() }

    override fun onDestroy() {
        super.onDestroy()
        instance = null
        overlayManager?.hide()
        scope.cancel()
    }

    private fun startCapture() {
        if (isCapturing) return
        isCapturing = true
        overlayManager?.setCapturing(true)
        scope.launch {
            val transcript = withContext(Dispatchers.Main) { captureFullScroll() }
            isCapturing = false
            overlayManager?.setCapturing(false)
            if (transcript.isNotBlank()) {
                val intent = Intent(applicationContext, CaptureResultActivity::class.java).apply {
                    putExtra("transcript", transcript)
                    addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                }
                startActivity(intent)
            }
        }
    }

    // Must be called on Main thread — rootInActiveWindow & node operations are thread-sensitive
    private suspend fun captureFullScroll(): String {
        val root = rootInActiveWindow ?: return ""
        val scrollable = findLargestScrollable(root)

        if (scrollable == null) return captureVisibleText(root)

        // Scroll to top
        var prevText = ""
        repeat(300) {
            scrollable.performAction(AccessibilityNodeInfo.ACTION_SCROLL_BACKWARD)
            delay(150)
            val cur = captureVisibleText(rootInActiveWindow ?: return@repeat)
            if (cur == prevText) return@repeat
            prevText = cur
        }

        val segments = mutableListOf<TextEntry>()
        var lastCaptureText = ""
        var iteration = 0

        while (iteration < maxIterations) {
            val currentRoot = rootInActiveWindow ?: break
            val entries = captureTextEntries(currentRoot)
            val newEntries = dedupOverlap(segments, entries)
            segments.addAll(newEntries)

            val didScroll = scrollable.performAction(AccessibilityNodeInfo.ACTION_SCROLL_FORWARD)
            delay(300)

            val newCaptureText = captureVisibleText(rootInActiveWindow ?: break)
            if (newCaptureText == lastCaptureText || !didScroll) break
            lastCaptureText = newCaptureText
            iteration++
        }

        return segments.sortedBy { it.boundsTop }.joinToString("\n") { it.text }
    }

    private data class TextEntry(val text: String, val boundsTop: Int)

    private fun findLargestScrollable(root: AccessibilityNodeInfo): AccessibilityNodeInfo? {
        val queue = ArrayDeque<AccessibilityNodeInfo>()
        queue.add(root)
        var largest: AccessibilityNodeInfo? = null
        var largestArea = 0
        while (queue.isNotEmpty()) {
            val node = queue.removeFirst()
            if (node.isScrollable) {
                val rect = Rect()
                node.getBoundsInScreen(rect)
                val area = rect.width() * rect.height()
                if (area > largestArea) { largestArea = area; largest = node }
            }
            for (i in 0 until node.childCount) node.getChild(i)?.let { queue.add(it) }
        }
        return largest
    }

    private fun captureVisibleText(root: AccessibilityNodeInfo): String =
        captureTextEntries(root).joinToString("\n") { it.text }

    private fun captureTextEntries(root: AccessibilityNodeInfo): List<TextEntry> {
        val result = mutableListOf<TextEntry>()
        val displayH = resources.displayMetrics.heightPixels
        val exclusionZone = (displayH * 0.10).toInt()
        val queue = ArrayDeque<AccessibilityNodeInfo>()
        queue.add(root)
        while (queue.isNotEmpty()) {
            val node = queue.removeFirst()
            val text = node.text?.toString() ?: node.contentDescription?.toString()
            if (text != null && text.length >= 3 && !node.isClickable) {
                val rect = Rect()
                node.getBoundsInScreen(rect)
                if (rect.top > exclusionZone && rect.bottom < displayH - exclusionZone) {
                    result.add(TextEntry(text, rect.top))
                }
            }
            for (i in 0 until node.childCount) node.getChild(i)?.let { queue.add(it) }
        }
        return result.distinctBy { it.text }.sortedBy { it.boundsTop }
    }

    private fun dedupOverlap(existing: List<TextEntry>, incoming: List<TextEntry>): List<TextEntry> {
        if (existing.isEmpty()) return incoming
        val overlapWindow = (existing.size * 0.30).toInt().coerceAtLeast(3)
        val tail = existing.takeLast(overlapWindow).map { it.text }
        var matchLen = 0
        for (len in tail.size downTo 1) {
            val suffix = tail.takeLast(len)
            val incomingTexts = incoming.map { it.text }
            if (incomingTexts.size >= len && incomingTexts.take(len) == suffix) {
                matchLen = len; break
            }
        }
        return incoming.drop(matchLen)
    }
}
