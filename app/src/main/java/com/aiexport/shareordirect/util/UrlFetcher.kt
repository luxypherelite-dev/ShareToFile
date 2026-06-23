package com.aiexport.shareordirect.util

import okhttp3.OkHttpClient
import okhttp3.Request
import org.jsoup.Jsoup
import java.util.concurrent.TimeUnit

object UrlFetcher {
    private val client = OkHttpClient.Builder()
        .connectTimeout(15, TimeUnit.SECONDS)
        .readTimeout(15, TimeUnit.SECONDS)
        .build()

    private val STRIP_TAGS = setOf("script", "style", "nav", "footer", "header", "aside", "noscript")

    /**
     * Fetches and extracts readable text from [url].
     * Returns null on failure.
     */
    fun fetchText(url: String): String? {
        return try {
            val req  = Request.Builder().url(url).build()
            val body = client.newCall(req).execute().use { it.body?.string() } ?: return null
            val doc  = Jsoup.parse(body, url)
            STRIP_TAGS.forEach { tag -> doc.select(tag).remove() }
            val text = doc.body()?.wholeText()?.trim() ?: ""
            if (text.length >= 200) text else null
        } catch (e: Exception) {
            null
        }
    }

    fun isUrl(text: String): Boolean =
        text.trimStart().let { it.startsWith("http://") || it.startsWith("https://") }
}
