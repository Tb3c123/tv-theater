package com.tvtheater.app.domain.usecase

import kotlinx.coroutines.test.runTest
import okhttp3.OkHttpClient
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class ExtractStreamUrlUseCaseTest {

    private lateinit var useCase: ExtractStreamUrlUseCase

    @Before
    fun setup() {
        val client = OkHttpClient.Builder().build()
        useCase = ExtractStreamUrlUseCase(client)
    }

    @Test
    fun directM3u8ReturnsDirectHls() = runTest {
        val directUrl = "https://cdn.example.com/videos/master.m3u8"
        val result = useCase(directUrl)
        assertTrue(result is StreamResult.DirectHls)
        assertEquals(directUrl, (result as StreamResult.DirectHls).streamUrl)
    }

    @Test
    fun directMp4ReturnsDirectHls() = runTest {
        val directUrl = "https://cdn.example.com/videos/movie.mp4"
        val result = useCase(directUrl)
        assertTrue(result is StreamResult.DirectHls)
        assertEquals(directUrl, (result as StreamResult.DirectHls).streamUrl)
    }

    @Test
    fun htmlWithM3u8RegexExtractionSucceeds() {
        val htmlContent = """
            <html>
                <head><title>Player</title></head>
                <body>
                    <div id="player"></div>
                    <script>
                        jwplayer("player").setup({
                            file: "https://sv1.streamcdn.com/hls/test_stream/index.m3u8?token=xyz123",
                            type: "hls"
                        });
                    </script>
                </body>
            </html>
        """.trimIndent()

        val extracted = useCase.extractM3u8FromHtml(htmlContent)
        assertEquals("https://sv1.streamcdn.com/hls/test_stream/index.m3u8?token=xyz123", extracted)
    }

    @Test
    fun htmlWithoutM3u8ReturnsNull() {
        val htmlContent = "<html><body>No stream available</body></html>"
        val extracted = useCase.extractM3u8FromHtml(htmlContent)
        assertEquals(null, extracted)
    }

    @Test
    fun invalidUrlReturnsFallbackEmbed() = runTest {
        val embedUrl = "https://invalid-nonexistent-domain.xyz/embed.php?hash=123"
        val result = useCase(embedUrl)
        assertTrue(result is StreamResult.FallbackEmbed)
        assertEquals(embedUrl, (result as StreamResult.FallbackEmbed).embedUrl)
    }
}
