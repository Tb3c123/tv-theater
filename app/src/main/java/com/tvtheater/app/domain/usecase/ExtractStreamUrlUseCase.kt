package com.tvtheater.app.domain.usecase

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import java.util.regex.Pattern

sealed interface StreamResult {
    data class DirectHls(val streamUrl: String) : StreamResult
    data class FallbackEmbed(val embedUrl: String) : StreamResult
}

class ExtractStreamUrlUseCase(
    private val httpClient: OkHttpClient = OkHttpClient()
) {
    private val m3u8Regex = Pattern.compile("""https?://[^"'\s<>\\]+\.(m3u8|mp4)[^"'\s<>\\]*""")

    suspend operator fun invoke(embedUrl: String): StreamResult = withContext(Dispatchers.IO) {
        val trimmed = embedUrl.trim()

        // 1. Direct stream check
        if (trimmed.endsWith(".m3u8", ignoreCase = true) || trimmed.endsWith(".mp4", ignoreCase = true)) {
            return@withContext StreamResult.DirectHls(trimmed)
        }

        // 2. Fetch HTML page with User Agent & Referer
        try {
            val request = Request.Builder()
                .url(trimmed)
                .addHeader("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/122.0.0.0 Safari/537.36")
                .addHeader("Referer", "https://phim.nguonc.com/")
                .build()

            val response = httpClient.newCall(request).execute()
            if (response.isSuccessful) {
                val body = response.body?.string().orEmpty()
                val directStream = extractM3u8FromHtml(body)
                if (directStream != null) {
                    return@withContext StreamResult.DirectHls(directStream)
                }
            }
        } catch (_: Exception) {
            // In case of network timeout, firewall interception, or bad URL, gracefully fallback
        }

        // 3. Fallback to WebView Embed
        StreamResult.FallbackEmbed(trimmed)
    }

    fun extractM3u8FromHtml(html: String): String? {
        val normalized = html.replace("\\/", "/")
        val matcher = m3u8Regex.matcher(normalized)
        if (matcher.find()) {
            return matcher.group()
        }
        return null
    }
}
