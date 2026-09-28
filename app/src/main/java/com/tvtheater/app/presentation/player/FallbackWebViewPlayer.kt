package com.tvtheater.app.presentation.player

import android.annotation.SuppressLint
import android.graphics.Bitmap
import android.net.http.SslError
import android.os.SystemClock
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.webkit.CookieManager
import android.webkit.JavascriptInterface
import android.webkit.PermissionRequest
import android.webkit.SslErrorHandler
import android.webkit.WebChromeClient
import android.webkit.WebResourceError
import android.webkit.WebResourceRequest
import android.webkit.WebResourceResponse
import android.webkit.WebSettings
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.FrameLayout
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.viewinterop.AndroidView

class WebAppInterface(private val onStreamFound: (String) -> Unit) {
    @JavascriptInterface
    fun onVideoSourceFound(url: String) {
        val trimmed = url.trim()
        if (trimmed.isNotBlank() && (trimmed.contains(".m3u8") || (trimmed.contains(".mp4") && !trimmed.contains(".png")))) {
            onStreamFound(trimmed)
        }
    }
}

@SuppressLint("SetJavaScriptEnabled")
@Composable
fun FallbackWebViewPlayer(
    embedUrl: String,
    isPlaying: Boolean = true,
    onProgressUpdate: (positionMs: Long, durationMs: Long) -> Unit,
    onStreamFound: (String) -> Unit = {},
    onError: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current

    val webView = remember(embedUrl) {
        WebView(context).apply {
            layoutParams = FrameLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT
            )
            setLayerType(View.LAYER_TYPE_HARDWARE, null)
            setBackgroundColor(android.graphics.Color.BLACK)

            val cookieManager = CookieManager.getInstance()
            cookieManager.setAcceptCookie(true)
            cookieManager.setAcceptThirdPartyCookies(this, true)

            settings.apply {
                javaScriptEnabled = true
                domStorageEnabled = true
                mediaPlaybackRequiresUserGesture = false
                mixedContentMode = WebSettings.MIXED_CONTENT_ALWAYS_ALLOW
                javaScriptCanOpenWindowsAutomatically = true
                setSupportMultipleWindows(false)
                allowFileAccess = true
                allowContentAccess = true
                cacheMode = WebSettings.LOAD_DEFAULT
                useWideViewPort = true
                loadWithOverviewMode = true
                userAgentString = "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/122.0.0.0 Safari/537.36"
            }

            addJavascriptInterface(WebAppInterface(onStreamFound), "AndroidBridge")

            webChromeClient = object : WebChromeClient() {
                override fun getDefaultVideoPoster(): Bitmap {
                    return Bitmap.createBitmap(1, 1, Bitmap.Config.ARGB_8888)
                }

                override fun onPermissionRequest(request: PermissionRequest?) {
                    request?.grant(request.resources)
                }
            }

            webViewClient = object : WebViewClient() {
                override fun shouldOverrideUrlLoading(view: WebView?, request: WebResourceRequest?): Boolean {
                    return false
                }

                override fun shouldInterceptRequest(view: WebView?, request: WebResourceRequest?): WebResourceResponse? {
                    val url = request?.url?.toString()
                    if (url != null && (url.contains(".m3u8") || (url.contains(".mp4") && !url.contains(".png") && !url.contains(".jpg")))) {
                        view?.post {
                            onStreamFound(url)
                        }
                    }
                    return super.shouldInterceptRequest(view, request)
                }

                override fun onReceivedSslError(view: WebView?, handler: SslErrorHandler?, error: SslError?) {
                    // Gracefully handle self-signed CDN certificates
                    handler?.proceed()
                }

                override fun onReceivedError(view: WebView?, request: WebResourceRequest?, error: WebResourceError?) {
                    super.onReceivedError(view, request, error)
                    if (request?.isForMainFrame == true && error?.errorCode == ERROR_HOST_LOOKUP) {
                        onError("Không thể kết nối đến máy chủ phát video")
                    }
                }

                override fun onPageFinished(view: WebView?, url: String?) {
                    super.onPageFinished(view, url)

                    // Inject stream interceptor, hide Android poster, and auto-click play
                    val js = """
                        (function() {
                            // 1. CSS styling: hide Android poster and fit TV screen
                            var style = document.createElement('style');
                            style.innerHTML = 'body, html { margin:0; padding:0; width:100%; height:100%; overflow:hidden; background:#000 !important; }' +
                                ' video::-webkit-media-controls-start-playback-button { display:none !important; -webkit-appearance:none; }' +
                                ' video { background:#000 !important; }' +
                                ' iframe, video { width:100% !important; height:100% !important; position:fixed; top:0; left:0; border:none; }';
                            document.head.appendChild(style);

                            // 2. Intercept video src and network requests
                            function notify(src) {
                                if (src && window.AndroidBridge && (src.indexOf('.m3u8') !== -1 || src.indexOf('.mp4') !== -1)) {
                                    window.AndroidBridge.onVideoSourceFound(src);
                                }
                            }

                            // Hook HTMLVideoElement
                            var origPlay = HTMLVideoElement.prototype.play;
                            HTMLVideoElement.prototype.play = function() {
                                if (this.src) notify(this.src);
                                var sources = this.querySelectorAll('source');
                                for (var i = 0; i < sources.length; i++) {
                                    if (sources[i].src) notify(sources[i].src);
                                }
                                return origPlay.apply(this, arguments);
                            };

                            // Hook XMLHttpRequest
                            var origOpen = XMLHttpRequest.prototype.open;
                            XMLHttpRequest.prototype.open = function(method, reqUrl) {
                                notify(reqUrl);
                                return origOpen.apply(this, arguments);
                            };

                            // Hook fetch
                            var origFetch = window.fetch;
                            window.fetch = function(input, init) {
                                var reqUrl = typeof input === 'string' ? input : (input ? input.url : '');
                                notify(reqUrl);
                                return origFetch.apply(this, arguments);
                            };

                            // 3. Auto-play loop to click play buttons & unmute
                            var attempts = 0;
                            var timer = setInterval(function() {
                                attempts++;
                                if (attempts > 15) clearInterval(timer);

                                var videos = document.querySelectorAll('video');
                                videos.forEach(function(v) {
                                    v.muted = false;
                                    v.play().catch(function() {
                                        v.muted = true;
                                        v.play().then(function() { v.muted = false; });
                                    });
                                    if (v.src) notify(v.src);
                                });

                                var clickables = document.querySelectorAll('.jw-display-icon-container, .vjs-big-play-button, .play, .play-button, [class*="play"], [aria-label*="Play"], button');
                                clickables.forEach(function(btn) {
                                    btn.click();
                                });

                                var center = document.elementFromPoint(window.innerWidth / 2, window.innerHeight / 2);
                                if (center) {
                                    center.click();
                                }
                            }, 400);
                        })();
                    """.trimIndent()
                    view?.evaluateJavascript(js, null)

                    // 4. Simulate a real Android touch event at screen center after 1.2s
                    view?.postDelayed({
                        val w = view.width.toFloat().takeIf { it > 0f } ?: 1920f
                        val h = view.height.toFloat().takeIf { it > 0f } ?: 1080f
                        val now = SystemClock.uptimeMillis()
                        view.dispatchTouchEvent(MotionEvent.obtain(now, now, MotionEvent.ACTION_DOWN, w / 2f, h / 2f, 0))
                        view.dispatchTouchEvent(MotionEvent.obtain(now, now + 50, MotionEvent.ACTION_UP, w / 2f, h / 2f, 0))
                    }, 1200L)
                }
            }

            loadUrl(embedUrl, mapOf("Referer" to "https://phim.nguonc.com/"))
        }
    }

    DisposableEffect(embedUrl) {
        onDispose {
            webView.stopLoading()
            webView.destroy()
        }
    }

    LaunchedEffect(isPlaying) {
        val js = if (isPlaying) {
            """
            (function() {
                var v = document.querySelector('video');
                if (v) v.play();
                var btn = document.querySelector('.jw-display-icon-container, .vjs-big-play-button, .play, .play-button, button');
                if (btn) btn.click();
            })();
            """.trimIndent()
        } else {
            "var v = document.querySelector('video'); if (v) v.pause();"
        }
        webView.evaluateJavascript(js, null)
    }

    AndroidView(
        factory = { webView },
        modifier = modifier
    )
}
