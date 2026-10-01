package com.tvtheater.app.presentation.player

import android.annotation.SuppressLint
import android.graphics.Bitmap
import android.net.http.SslError
import android.os.Build
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
import androidx.compose.foundation.focusable
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.viewinterop.AndroidView

class WebAppInterface(
    private val onStreamFound: (String) -> Unit,
    private val onProgressUpdate: (positionMs: Long, durationMs: Long) -> Unit,
    private val onRateLimit: () -> Unit = {},
    private val onVideoTimeout: () -> Unit = {},
    private val onAdSkippable: (Boolean) -> Unit = {},
    private val onResumePopup: (String) -> Unit = {},
    private val onDialog: (id: String, title: String, message: String, buttonsJson: String) -> Unit = { _, _, _, _ -> }
) {
    @JavascriptInterface
    fun onVideoSourceFound(url: String) {
        val trimmed = url.trim()
        if (trimmed.isNotBlank() && (trimmed.contains(".m3u8") || (trimmed.contains(".mp4") && !trimmed.contains(".png")))) {
            onStreamFound(trimmed)
        }
    }

    @JavascriptInterface
    fun onTimeUpdate(currentTimeSeconds: Float, durationSeconds: Float) {
        val currentMs = (currentTimeSeconds * 1000).toLong()
        val durationMs = (durationSeconds * 1000).toLong()
        if (durationMs > 0L) {
            onProgressUpdate(currentMs, durationMs)
        }
    }

    @JavascriptInterface
    fun onRateLimitDetected() {
        onRateLimit()
    }

    @JavascriptInterface
    fun onVideoTimeoutDetected() {
        onVideoTimeout()
    }

    @JavascriptInterface
    fun onAdSkippableState(skippable: Boolean) {
        onAdSkippable(skippable)
    }

    @JavascriptInterface
    fun onResumePopupDetected(timeText: String) {
        onResumePopup(timeText)
    }

    @JavascriptInterface
    fun onDialogDetected(id: String, title: String, message: String, buttonsJson: String) {
        onDialog(id, title, message, buttonsJson)
    }
}

@SuppressLint("SetJavaScriptEnabled")
@Composable
fun FallbackWebViewPlayer(
    embedUrl: String,
    isPlaying: Boolean = true,
    seekTrigger: Int = 0,
    seekTargetMs: Long? = null,
    skipAdTrigger: Int = 0,
    resumeActionTrigger: Int = 0,
    dialogActionTrigger: Int? = null,
    dialogDismissTrigger: Int = 0,
    onProgressUpdate: (positionMs: Long, durationMs: Long) -> Unit,
    onStreamFound: (String) -> Unit = {},
    onAdSkippableState: (Boolean) -> Unit = {},
    onResumePopupDetected: (String) -> Unit = {},
    onDialogDetected: (id: String, title: String, message: String, buttonsJson: String) -> Unit = { _, _, _, _ -> },
    onError: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current

    val webView = remember(embedUrl) {
        WebView(context).apply {
            layoutParams = android.widget.FrameLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT
            )
            setLayerType(View.LAYER_TYPE_HARDWARE, null)
            setBackgroundColor(android.graphics.Color.BLACK)
            isFocusable = false
            isFocusableInTouchMode = false
            descendantFocusability = ViewGroup.FOCUS_BLOCK_DESCENDANTS

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

            addJavascriptInterface(
                WebAppInterface(
                    onStreamFound = onStreamFound,
                    onProgressUpdate = { pos, dur ->
                        post {
                            onProgressUpdate(pos, dur)
                        }
                    },
                    onRateLimit = {
                        post {
                            stopLoading()
                            loadUrl("about:blank")
                            visibility = View.INVISIBLE
                            onError("Máy chủ video đang giới hạn lưu lượng (Rate Limit: Mạng gửi quá nhiều yêu cầu). Vui lòng thử lại sau giây lát hoặc đổi sang mạng khác.")
                        }
                    },
                    onVideoTimeout = {
                        post {
                            stopLoading()
                            loadUrl("about:blank")
                            visibility = View.INVISIBLE
                            onError("Video tải lâu hơn dự kiến từ máy chủ nguồn. Bạn có thể tải lại trang hoặc quay lại chọn tập phim khác.")
                        }
                    },
                    onAdSkippable = { skippable ->
                        post {
                            onAdSkippableState(skippable)
                        }
                    },
                    onResumePopup = { timeText ->
                        post {
                            onResumePopupDetected(timeText)
                        }
                    },
                    onDialog = { id, title, message, buttonsJson ->
                        post {
                            onDialogDetected(id, title, message, buttonsJson)
                        }
                    }
                ),
                "AndroidBridge"
            )

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
                    val rawUrl = request?.url?.toString() ?: return null
                    val lowerUrl = rawUrl.lowercase()

                    // Extract genuine movie HLS streams (.m3u8 or non-ad .mp4) for metadata
                    if (lowerUrl.contains(".m3u8") || (lowerUrl.contains(".mp4") && !lowerUrl.contains(".png") && !lowerUrl.contains(".jpg"))) {
                        val isAd = lowerUrl.contains("ad-") || lowerUrl.contains("ad_") || lowerUrl.contains("/ad/") || lowerUrl.contains("promo") || lowerUrl.contains("banner")
                        if (!isAd) {
                            view?.post {
                                onStreamFound(rawUrl)
                            }
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
                    if (request?.isForMainFrame == true) {
                        view?.stopLoading()
                        view?.loadUrl("about:blank")
                        view?.visibility = View.INVISIBLE
                        val description = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                            error?.description?.toString() ?: "Lỗi kết nối"
                        } else {
                            "Lỗi kết nối"
                        }
                        onError("Không thể kết nối đến máy chủ phát video ($description). Nguồn phát có thể bị chặn bởi tường lửa mạng hoặc gián đoạn.")
                    }
                }

                override fun onPageFinished(view: WebView?, url: String?) {
                    super.onPageFinished(view, url)
                    if (url == null || url == "about:blank" || url.startsWith("chrome-error://")) {
                        return
                    }

                    // Inject stream interceptor, style skip ad buttons, and auto-skip ads
                    val js = """
                        (function() {
                            // 1. CSS styling: hide Android poster, transparent raw skip button under native overlay
                            var style = document.createElement('style');
                            style.innerHTML = 'body, html { margin:0; padding:0; width:100%; height:100%; overflow:hidden; background:#000 !important; }' +
                                ' #player, .player, .jwplayer, iframe, video { width:100% !important; height:100% !important; }' +
                                ' video::-webkit-media-controls-start-playback-button { display:none !important; -webkit-appearance:none; }' +
                                ' video { background:#000 !important; border:none !important; }' +
                                ' .jw-ad-container .jw-skip, .jw-ads .jw-skip, .jw-flag-ads .jw-skip, .jw-ad-skip, .videoAdUiSkipButton, .ytp-ad-skip-button {' +
                                '   opacity: 0.01 !important; pointer-events:auto !important;' +
                                ' }';
                            document.head.appendChild(style);

                            // Signal that ads are allowed to anti-adblock scripts
                            window.canRunAds = true;
                            window.isAdBlockActive = false;

                            // 2. Intercept video src and network requests
                            function notify(src) {
                                if (src && window.AndroidBridge && (src.indexOf('.m3u8') !== -1 || src.indexOf('.mp4') !== -1)) {
                                    var lower = src.toLowerCase();
                                    if (lower.indexOf('ad-') === -1 && lower.indexOf('promo') === -1) {
                                        window.AndroidBridge.onVideoSourceFound(src);
                                    }
                                }
                            }

                            function attachTimeUpdate(v) {
                                if (!v || v.__hasTimeUpdate) return;
                                v.__hasTimeUpdate = true;
                                v.addEventListener('timeupdate', function() {
                                    if (window.AndroidBridge && window.AndroidBridge.onTimeUpdate && !isNaN(v.duration)) {
                                        window.AndroidBridge.onTimeUpdate(v.currentTime || 0, v.duration || 0);
                                    }
                                });
                                v.addEventListener('play', function() {
                                    if (window.__isUserPaused) {
                                        try { v.pause(); } catch(e) {}
                                    }
                                });
                            }

                            // Hook HTMLVideoElement
                            var origPlay = HTMLVideoElement.prototype.play;
                            HTMLVideoElement.prototype.play = function() {
                                if (this.src) notify(this.src);
                                attachTimeUpdate(this);
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

                            window.clickDialogButton = function(btnIndex) {
                                if (window.__dialogActive && window.__dialogActive.buttons) {
                                    var btn = window.__dialogActive.buttons[btnIndex];
                                    if (btn) {
                                        try { btn.click(); } catch(e) {}
                                        var txt = (btn.innerText || btn.value || '').toLowerCase();
                                        if (txt.indexOf('tải lại') !== -1 || txt.indexOf('tai lai') !== -1 || txt.indexOf('reload') !== -1) {
                                            location.reload();
                                        }
                                    }
                                    if (window.__dialogActive.container) {
                                        if (!window.__dialogActive.container.querySelector('video')) {
                                            window.__dialogActive.container.style.display = 'none';
                                        }
                                    }
                                }
                                window.__dialogActive = null;
                                setTimeout(function() {
                                    var videos = document.querySelectorAll('video');
                                    videos.forEach(function(v) {
                                        v.style.opacity = '1';
                                        v.style.visibility = 'visible';
                                        if (v.parentElement && v.parentElement !== document.body) {
                                            v.parentElement.style.opacity = '1';
                                            v.parentElement.style.visibility = 'visible';
                                        }
                                        if (!window.__isUserPaused && v.paused) v.play().catch(function() {});
                                    });
                                    if (!window.__isUserPaused && window.jwplayer) {
                                        try { window.jwplayer().play(); } catch(e) {}
                                    }
                                }, 200);
                            };

                            window.dismissActiveDialog = function() {
                                if (window.__dialogActive) {
                                    if (window.__dialogActive.buttons) {
                                        for (var i = 0; i < window.__dialogActive.buttons.length; i++) {
                                            var b = window.__dialogActive.buttons[i];
                                            var t = (b.innerText || b.value || '').toLowerCase();
                                            if (t.indexOf('đóng') !== -1 || t.indexOf('close') !== -1 || t.indexOf('bỏ qua') !== -1 || t.indexOf('tiếp tục') !== -1) {
                                                try { b.click(); } catch(e) {}
                                                break;
                                            }
                                        }
                                    }
                                    if (window.__dialogActive.container) {
                                        if (!window.__dialogActive.container.querySelector('video')) {
                                            window.__dialogActive.container.style.display = 'none';
                                        }
                                    }
                                }
                                window.__dialogActive = null;
                                setTimeout(function() {
                                    var videos = document.querySelectorAll('video');
                                    videos.forEach(function(v) {
                                        v.style.opacity = '1';
                                        v.style.visibility = 'visible';
                                        if (v.parentElement && v.parentElement !== document.body) {
                                            v.parentElement.style.opacity = '1';
                                            v.parentElement.style.visibility = 'visible';
                                        }
                                        if (!window.__isUserPaused && v.paused) v.play().catch(function() {});
                                    });
                                    if (!window.__isUserPaused && window.jwplayer) {
                                        try { window.jwplayer().play(); } catch(e) {}
                                    }
                                }, 200);
                            };

                            // 3. Auto-play & ad skip detection loop
                            var attempts = 0;
                            var timer = setInterval(function() {
                                attempts++;
                                if (attempts > 300) clearInterval(timer);

                                // Check for real ad skip buttons in DOM (strictly ad containers)
                                var skipBtns = document.querySelectorAll('.jw-ad-container .jw-skip, .jw-ads .jw-skip, .jw-flag-ads .jw-skip, .jw-ad-skip, .videoAdUiSkipButton, .ytp-ad-skip-button');
                                var hasSkip = false;
                                skipBtns.forEach(function(b) {
                                    var rect = b.getBoundingClientRect();
                                    var style = window.getComputedStyle(b);
                                    if (rect.width > 0 && rect.height > 0 && style.display !== 'none' && style.visibility !== 'hidden' && style.opacity !== '0') {
                                        hasSkip = true;
                                    }
                                });
                                if (!hasSkip) {
                                    var adTextBtns = document.querySelectorAll('.jw-ads button, .jw-ad-container button, div[class*="ad-"] button, div[class*="ad_"] button');
                                    adTextBtns.forEach(function(b) {
                                        var txt = (b.innerText || '').trim().toLowerCase();
                                        if ((txt.indexOf('bỏ qua') !== -1 || txt.indexOf('skip') !== -1) && (txt.indexOf('quảng cáo') !== -1 || txt.indexOf('ad') !== -1)) {
                                            if (txt.indexOf('10') === -1 && txt.indexOf('giây') === -1 && txt.indexOf('phút') === -1) {
                                                var rect = b.getBoundingClientRect();
                                                if (rect.width > 0 && rect.height > 0) hasSkip = true;
                                            }
                                        }
                                    });
                                }
                                if (window.AndroidBridge && window.AndroidBridge.onAdSkippableState) {
                                    window.AndroidBridge.onAdSkippableState(hasSkip);
                                }

                                // Hook JW Player progress if present
                                if (window.jwplayer) {
                                    try {
                                        var jwp = window.jwplayer();
                                        if (jwp && jwp.on && !window.__jwpHooked) {
                                            window.__jwpHooked = true;
                                            jwp.on('time', function(e) {
                                                if (window.AndroidBridge && window.AndroidBridge.onTimeUpdate) {
                                                    window.AndroidBridge.onTimeUpdate(e.position || 0, e.duration || 0);
                                                }
                                            });
                                        }
                                    } catch(e) {}
                                }

                                var videos = document.querySelectorAll('video');
                                videos.forEach(function(v) {
                                    attachTimeUpdate(v);
                                    v.style.opacity = '1';
                                    v.style.visibility = 'visible';
                                    if (v.parentElement && v.parentElement !== document.body) {
                                        v.parentElement.style.opacity = '1';
                                        v.parentElement.style.visibility = 'visible';
                                    }
                                    if (!window.__isUserPaused && attempts < 20 && v.paused) {
                                        v.play().catch(function() {});
                                    }
                                    if (v.src) notify(v.src);
                                });

                                if (!window.__isUserPaused && attempts < 20 && window.jwplayer) {
                                    try {
                                        var jwp = window.jwplayer();
                                        if (jwp && jwp.getState() === 'idle') {
                                            jwp.play();
                                        }
                                    } catch(e) {}
                                }

                                // Auto-click "Kiểm tra lại" if anti-adblock dialog is detected
                                var antiBlockBtns = document.querySelectorAll('button, a');
                                antiBlockBtns.forEach(function(el) {
                                    var txt = el.innerText || '';
                                    if (txt.indexOf('Kiểm tra lại') !== -1 || txt.indexOf('Kiem tra lai') !== -1) {
                                        try { el.click(); } catch(e) {}
                                    }
                                });

                                // Universal In-Video Popup & Dialog Scanner (strictly for real alerts/notices, never video player controls)
                                if (!window.__dialogActive) {
                                    var allContainers = document.querySelectorAll('.modal, .popup, .dialog, [role="dialog"], .alert-box, .player-notice, .notice-box, .box-alert, .swal2-container, .sweet-alert, [class*="modal-"], [class*="popup-"]');
                                    for (var i = 0; i < allContainers.length; i++) {
                                        var el = allContainers[i];
                                        if (el === document.body || el === document.documentElement || el.id === 'player') continue;
                                        if (el.querySelector('video, audio')) continue;

                                        var cls = ((el.className || '') + ' ' + (el.id || '')).toLowerCase();
                                        if (/jw-|vjs-|video|media|control|playback|volume|speed|rate|gear|setting|track|subtitle|audio|caption|quality|buffer|time/i.test(cls)) continue;
                                        if (el.closest && el.closest('.jw-controls, .vjs-control-bar, .jw-settings-menu, .jw-media, .jwplayer, .video-js')) continue;

                                        var rect = el.getBoundingClientRect();
                                        if (rect.width < 120 || rect.height < 60) continue;
                                        var style = window.getComputedStyle(el);
                                        if (style.display === 'none' || style.visibility === 'hidden' || style.opacity === '0') continue;

                                        var text = (el.innerText || '').trim();
                                        if (text.length < 8 || text.length > 800) continue;

                                        // Explicitly exclude internal player status and playback rate/volume text
                                        if (/seconds of|volume \d+|playback rate|\b\d+(\.\d+)?x\b|quality \d+p|subtitles|captions/i.test(text)) continue;

                                        // Must contain an actual notice, resume, warning, or ad prompt keyword
                                        var hasKeyword = /thông báo|tiếp tục xem|xem lại|cảnh báo|chưa tải|quảng cáo|kiểm tra|chặn quảng cáo|notice|alert|warning|confirm/i.test(text);
                                        if (!hasKeyword) continue;

                                        var btnEls = el.querySelectorAll('button, [role="button"], input[type="button"], a.btn, a[class*="btn"], .button');
                                        if (btnEls.length === 0) {
                                            btnEls = el.querySelectorAll('a, span[onclick], div[onclick]');
                                        }

                                        var validBtns = [];
                                        var buttonsData = [];
                                        for (var bIdx = 0; bIdx < btnEls.length; bIdx++) {
                                            var b = btnEls[bIdx];
                                            var bText = (b.innerText || b.value || '').trim();
                                            if (bText.length >= 1 && bText.length <= 40 && validBtns.indexOf(b) === -1) {
                                                if (/^\d+(\.\d+)?x$/i.test(bText) || /enabled|disabled/i.test(bText)) continue;

                                                var bStyle = window.getComputedStyle(b);
                                                if (bStyle.display !== 'none' && bStyle.visibility !== 'hidden') {
                                                    validBtns.push(b);
                                                    var isPrimary = /đóng|tiếp tục|xác nhận|ok|đồng ý|close|continue|confirm/i.test(bText);
                                                    buttonsData.push({
                                                        index: validBtns.length - 1,
                                                        text: bText,
                                                        isPrimary: isPrimary
                                                    });
                                                }
                                            }
                                        }

                                        if (validBtns.length >= 1 && validBtns.length <= 6 && el.children.length <= 15) {
                                            var title = '';
                                            var heading = el.querySelector('h1, h2, h3, h4, h5, .title, [class*="title"], strong, b');
                                            if (heading && (heading.innerText || '').trim().length <= 50) {
                                                title = (heading.innerText || '').trim();
                                            }
                                            if (!title) {
                                                var lines = text.split('\n').map(function(s) { return s.trim(); }).filter(Boolean);
                                                if (lines.length > 0 && lines[0].length <= 50) {
                                                    title = lines[0];
                                                } else {
                                                    title = 'Thông báo';
                                                }
                                            }

                                            var message = '';
                                            var paragraphs = el.querySelectorAll('p, .message, [class*="message"], .content, [class*="content"], .desc');
                                            for (var p = 0; p < paragraphs.length; p++) {
                                                var pText = (paragraphs[p].innerText || '').trim();
                                                if (pText && pText !== title && buttonsData.every(function(bd) { return bd.text !== pText; })) {
                                                    message = pText;
                                                    break;
                                                }
                                            }
                                            if (!message) {
                                                var fullText = text;
                                                message = fullText.replace(title, '').trim();
                                                buttonsData.forEach(function(bd) {
                                                    message = message.replace(bd.text, '').trim();
                                                });
                                            }

                                            var dialogId = 'dlg_' + Date.now();
                                            window.__dialogActive = {
                                                id: dialogId,
                                                container: el,
                                                buttons: validBtns
                                            };

                                            if (!el.querySelector('video') && !(el.parentElement && el.parentElement.querySelector('video'))) {
                                                el.style.opacity = '0';
                                                el.style.pointerEvents = 'none';
                                            }

                                            if (window.AndroidBridge && window.AndroidBridge.onDialogDetected) {
                                                window.AndroidBridge.onDialogDetected(dialogId, title, message, JSON.stringify(buttonsData));
                                            }
                                            break;
                                        }
                                    }
                                }

                                // Check for Cloudflare / CDN rate limit or verification blocks
                                if (document.title && document.title.indexOf('Attention Required') !== -1) {
                                    clearInterval(timer);
                                    if (window.AndroidBridge && window.AndroidBridge.onRateLimitDetected) {
                                        window.AndroidBridge.onRateLimitDetected();
                                    }
                                }
                                if (document.body && (document.body.innerText.indexOf('quá nhiều yêu cầu') !== -1 || document.body.innerText.indexOf('Web Page Blocked') !== -1)) {
                                    clearInterval(timer);
                                    if (window.AndroidBridge && window.AndroidBridge.onRateLimitDetected) {
                                        window.AndroidBridge.onRateLimitDetected();
                                    }
                                }
                            }, 500);
                        })();
                    """.trimIndent()
                    view?.evaluateJavascript(js, null)
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
                window.__isUserPaused = false;
                if (window.jwplayer) {
                    try { window.jwplayer().play(true); } catch(e) {}
                }
                function playVideos(doc) {
                    if (!doc) return;
                    var videos = doc.querySelectorAll('video');
                    videos.forEach(function(v) {
                        try { if (v.paused) v.play().catch(function() {}); } catch(e) {}
                    });
                    var iframes = doc.querySelectorAll('iframe');
                    iframes.forEach(function(f) {
                        try { if (f.contentDocument) playVideos(f.contentDocument); } catch(e) {}
                    });
                }
                playVideos(document);
            })();
            """.trimIndent()
        } else {
            """
            (function() {
                window.__isUserPaused = true;
                if (window.jwplayer) {
                    try { window.jwplayer().pause(true); } catch(e) {}
                }
                function pauseVideos(doc) {
                    if (!doc) return;
                    var videos = doc.querySelectorAll('video');
                    videos.forEach(function(v) {
                        try { if (!v.paused) v.pause(); } catch(e) {}
                    });
                    var iframes = doc.querySelectorAll('iframe');
                    iframes.forEach(function(f) {
                        try { if (f.contentDocument) pauseVideos(f.contentDocument); } catch(e) {}
                    });
                }
                pauseVideos(document);
            })();
            """.trimIndent()
        }
        webView.evaluateJavascript(js, null)
    }

    LaunchedEffect(seekTrigger) {
        if (seekTrigger > 0 && seekTargetMs != null) {
            val targetSec = seekTargetMs / 1000.0
            val js = """
                (function() {
                    var targetSec = $targetSec;
                    if (window.jwplayer) {
                        try { window.jwplayer().seek(targetSec); } catch(e) {}
                    }
                    var videos = document.querySelectorAll('video');
                    videos.forEach(function(v) {
                        try { v.currentTime = targetSec; } catch(e) {}
                    });
                })();
            """.trimIndent()
            webView.evaluateJavascript(js, null)
        }
    }

    LaunchedEffect(skipAdTrigger) {
        if (skipAdTrigger > 0) {
            val js = """
                (function() {
                    // 1. Try JW Player API
                    if (window.jwplayer) {
                        try {
                            var jwp = window.jwplayer();
                            if (jwp && typeof jwp.skipAd === 'function') {
                                jwp.skipAd();
                            }
                        } catch(e) {}
                    }
                    // 2. Click strictly ad skip elements
                    var selectors = [
                        '.jw-ad-container .jw-skip', '.jw-ads .jw-skip', '.jw-flag-ads .jw-skip',
                        '.jw-ad-skip', '.videoAdUiSkipButton', '.videoAdUiAction', '.ytp-ad-skip-button',
                        'button.jw-skip', 'div.jw-skip', '[class*="skipAd"]', '[class*="skip-ad"]',
                        '[id*="skipAd"]', '[id*="skip-ad"]', '.skip-ad', '.skip-button'
                    ];
                    function fireClick(el) {
                        try {
                            el.click();
                            var evt = new MouseEvent('click', { bubbles: true, cancelable: true, view: window });
                            el.dispatchEvent(evt);
                        } catch(e) {}
                    }
                    selectors.forEach(function(sel) {
                        var els = document.querySelectorAll(sel);
                        els.forEach(function(el) {
                            fireClick(el);
                        });
                    });
                    // 3. Click buttons strictly within ad containers or explicitly saying 'Bỏ qua quảng cáo'
                    var all = document.querySelectorAll('.jw-ads button, .jw-ad-container button, div[class*="ad-"] button, div[class*="ad_"] button');
                    for (var i = 0; i < all.length; i++) {
                        var item = all[i];
                        var txt = (item.innerText || '').trim().toLowerCase();
                        if ((txt.indexOf('bỏ qua') !== -1 || txt.indexOf('skip') !== -1) && (txt.indexOf('quảng cáo') !== -1 || txt.indexOf('ad') !== -1)) {
                            if (txt.indexOf('10') === -1 && txt.indexOf('giây') === -1 && txt.indexOf('phút') === -1 && txt.indexOf('forward') === -1 && txt.indexOf('backward') === -1) {
                                fireClick(item);
                            }
                        }
                    }
                    // 4. Resume playback if paused
                    setTimeout(function() {
                        var v = document.querySelector('video');
                        if (v && v.paused) v.play().catch(function() {});
                        if (window.jwplayer) {
                            try { window.jwplayer().play(); } catch(e) {}
                        }
                    }, 300);
                })();
            """.trimIndent()
            webView.evaluateJavascript(js, null)
        }
    }

    LaunchedEffect(resumeActionTrigger) {
        if (resumeActionTrigger == 1) {
            // Resume action: click "Tiếp tục xem" button
            val js = """
                (function() {
                    var clicked = false;
                    var buttons = document.querySelectorAll('button, a, [role="button"], input[type="button"]');
                    for (var i = 0; i < buttons.length; i++) {
                        var b = buttons[i];
                        var t = (b.innerText || b.value || '').trim();
                        if (t.indexOf('Tiếp tục') !== -1 || t.indexOf('Tiep tuc') !== -1) {
                            try { b.click(); } catch(e) {}
                            clicked = true;
                            break;
                        }
                    }
                    if (!clicked) {
                        var all = document.querySelectorAll('span, p, div');
                        for (var i = all.length - 1; i >= 0; i--) {
                            var el = all[i];
                            if (el.children.length === 0 && ((el.innerText || '').trim().indexOf('Tiếp tục') !== -1 || (el.innerText || '').trim().indexOf('Tiep tuc') !== -1)) {
                                try { el.click(); } catch(e) {}
                                break;
                            }
                        }
                    }
                    setTimeout(function() {
                        var v = document.querySelector('video');
                        if (v && v.paused) v.play().catch(function() {});
                        if (window.jwplayer) {
                            try { window.jwplayer().play(); } catch(e) {}
                        }
                    }, 200);
                })();
            """.trimIndent()
            webView.evaluateJavascript(js, null)
        } else if (resumeActionTrigger == 2) {
            // Restart action: click "Xem lại từ đầu" button
            val js = """
                (function() {
                    var clicked = false;
                    var buttons = document.querySelectorAll('button, a, [role="button"], input[type="button"]');
                    for (var i = 0; i < buttons.length; i++) {
                        var b = buttons[i];
                        var t = (b.innerText || b.value || '').trim();
                        if (t.indexOf('Xem lại') !== -1 || t.indexOf('Xem lai') !== -1 || t.indexOf('từ đầu') !== -1) {
                            try { b.click(); } catch(e) {}
                            clicked = true;
                            break;
                        }
                    }
                    if (!clicked) {
                        var all = document.querySelectorAll('span, p, div');
                        for (var i = all.length - 1; i >= 0; i--) {
                            var el = all[i];
                            if (el.children.length === 0 && ((el.innerText || '').trim().indexOf('Xem lại') !== -1 || (el.innerText || '').trim().indexOf('Xem lai') !== -1)) {
                                try { el.click(); } catch(e) {}
                                break;
                            }
                        }
                    }
                    if (window.jwplayer) {
                        try { window.jwplayer().seek(0); window.jwplayer().play(); } catch(e) {}
                    }
                    var v = document.querySelector('video');
                    if (v) {
                        v.currentTime = 0;
                        v.play().catch(function() {});
                    }
                })();
            """.trimIndent()
            webView.evaluateJavascript(js, null)
        }
    }

    LaunchedEffect(dialogActionTrigger) {
        if (dialogActionTrigger != null) {
            val js = "if (window.clickDialogButton) { window.clickDialogButton($dialogActionTrigger); }"
            webView.evaluateJavascript(js, null)
        }
    }

    LaunchedEffect(dialogDismissTrigger) {
        if (dialogDismissTrigger > 0) {
            val js = "if (window.dismissActiveDialog) { window.dismissActiveDialog(); }"
            webView.evaluateJavascript(js, null)
        }
    }

    LaunchedEffect(isPlaying) {
        val js = if (isPlaying) {
            """
            (function() {
                var v = document.querySelector('video');
                if (v && v.paused) v.play().catch(function() {});
                if (window.jwplayer) {
                    try { window.jwplayer().play(); } catch(e) {}
                }
            })();
            """.trimIndent()
        } else {
            """
            (function() {
                var v = document.querySelector('video');
                if (v && !v.paused) v.pause();
                if (window.jwplayer) {
                    try { window.jwplayer().pause(); } catch(e) {}
                }
            })();
            """.trimIndent()
        }
        webView.evaluateJavascript(js, null)
    }

    AndroidView(
        factory = { webView },
        modifier = modifier.focusable(false)
    )
}
