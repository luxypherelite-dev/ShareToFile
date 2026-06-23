package com.aiexport.shareordirect.util

import android.content.Context
import android.os.Handler
import android.os.Looper
import android.webkit.WebResourceError
import android.webkit.WebResourceRequest
import android.webkit.WebView
import android.webkit.WebViewClient
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withTimeout
import kotlin.coroutines.resume

object WebViewFetcher {

    private val JS_RENDERED_DOMAINS = listOf(
        "chatgpt.com/share",
        "chat.openai.com/share",
        "claude.ai/share",
        "gemini.google.com",
        "perplexity.ai",
        "copilot.microsoft.com",
        "poe.com",
        "character.ai",
        "pi.ai",
        "you.com",
    )

    fun needsWebView(url: String): Boolean =
        JS_RENDERED_DOMAINS.any { url.contains(it, ignoreCase = true) }

    private val EXTRACT_JS = """
        (function() {
            ['script','style','nav','footer','header','aside','noscript',
             '[role=navigation]','[role=banner]','[role=complementary]']
                .forEach(function(sel) {
                    document.querySelectorAll(sel).forEach(function(el) { el.remove(); });
                });
            var main = document.querySelector('main') ||
                       document.querySelector('[role=main]') ||
                       document.querySelector('article') ||
                       document.body;
            return main ? main.innerText : '';
        })()
    """.trimIndent()

    suspend fun fetchText(context: Context, url: String): String? {
        return try {
            withTimeout(35_000L) {
                suspendCancellableCoroutine { cont ->
                    val mainHandler = Handler(Looper.getMainLooper())
                    mainHandler.post {
                        val webView = WebView(context.applicationContext)
                        webView.settings.apply {
                            javaScriptEnabled = true
                            domStorageEnabled  = true
                            @Suppress("DEPRECATION")
                            allowFileAccess    = false
                            userAgentString    =
                                "Mozilla/5.0 (Linux; Android 13; Pixel 7) " +
                                "AppleWebKit/537.36 (KHTML, like Gecko) " +
                                "Chrome/120.0.0.0 Mobile Safari/537.36"
                        }

                        var settled = false
                        val settle: (String?) -> Unit = { text ->
                            if (!settled) {
                                settled = true
                                mainHandler.post { webView.destroy() }
                                if (cont.isActive) cont.resume(text)
                            }
                        }

                        webView.webViewClient = object : WebViewClient() {
                            override fun onPageFinished(view: WebView, url: String) {
                                // Give JS 4 s to render dynamic content
                                mainHandler.postDelayed({
                                    view.evaluateJavascript(EXTRACT_JS) { raw ->
                                        val text = raw
                                            ?.removeSurrounding("\"")
                                            ?.replace("\\n", "\n")
                                            ?.replace("\\t", "\t")
                                            ?.replace("\\\"", "\"")
                                            ?.replace("\\u003C", "<")
                                            ?.trim()
                                            ?.takeIf { it.length >= 300 }
                                        settle(text)
                                    }
                                }, 4_000L)
                            }

                            override fun onReceivedError(
                                view: WebView,
                                request: WebResourceRequest,
                                error: WebResourceError
                            ) {
                                if (request.isForMainFrame) settle(null)
                            }
                        }

                        cont.invokeOnCancellation {
                            mainHandler.post { if (!settled) { settled = true; webView.destroy() } }
                        }

                        webView.loadUrl(url)
                    }
                }
            }
        } catch (_: Exception) {
            null
        }
    }
}
