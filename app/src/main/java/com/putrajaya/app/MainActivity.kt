package com.putrajaya.app

import android.annotation.SuppressLint
import android.app.Activity
import android.os.Bundle
import android.webkit.WebChromeClient
import android.webkit.WebSettings
import android.webkit.WebView
import android.webkit.WebViewClient
import java.net.HttpURLConnection
import java.net.URL

class MainActivity : Activity() {
    private lateinit var web: WebView

    @SuppressLint("SetJavaScriptEnabled")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        web = WebView(this)
        web.settings.javaScriptEnabled = true
        web.settings.domStorageEnabled = true
        web.settings.databaseEnabled = true
        web.settings.allowFileAccess = true
        web.settings.allowContentAccess = true
        web.settings.mixedContentMode = WebSettings.MIXED_CONTENT_COMPATIBILITY_MODE
        web.settings.cacheMode = WebSettings.LOAD_DEFAULT
        web.webChromeClient = WebChromeClient()
        web.webViewClient = WebViewClient()
        setContentView(web)
        showLoadingScreen()
        loadAppHtml()
    }

    private fun showLoadingScreen() {
        web.loadDataWithBaseURL(
            null,
            """
            <!DOCTYPE html>
            <html>
            <head>
            <meta name="viewport" content="width=device-width, initial-scale=1.0">
            <style>
                * { margin:0; padding:0; box-sizing:border-box; }
                body {
                    font-family: sans-serif;
                    background: linear-gradient(180deg, #0d2a52 0%, #07172e 100%);
                    height: 100vh;
                    display: flex;
                    align-items: center;
                    justify-content: center;
                }
                .spinner {
                    width: 36px;
                    height: 36px;
                    margin: 0 auto 22px;
                    border: 3px solid rgba(255,255,255,0.15);
                    border-top-color: #ffffff;
                    border-radius: 50%;
                    animation: spin 0.8s linear infinite;
                }
                @keyframes spin { to { transform: rotate(360deg); } }
                .title { color: #ffffff; font-size: 16px; font-weight: 600; margin-bottom: 6px; text-align: center; }
                .subtitle { color: #a9bad6; font-size: 13px; text-align: center; }
            </style>
            </head>
            <body>
                <div>
                    <div class="spinner"></div>
                    <div class="title">PUTRAJAYA 2</div>
                    <div class="subtitle">Memuat aplikasi...</div>
                </div>
            </body>
            </html>
            """.trimIndent(),
            "text/html",
            "UTF-8",
            null
        )
    }

    private fun loadAppHtml() {
        // URL file HTML yang sebenarnya (dipakai untuk download konten)
        val fileUrl = "https://raw.githubusercontent.com/moyusaky-creator/putra-jaya---app/main/PUTRAJAYA_2_ONLINE.html"
        Thread {
            try {
                val connection = URL(fileUrl).openConnection() as HttpURLConnection
                connection.connectTimeout = 15000
                connection.readTimeout = 20000
                connection.requestMethod = "GET"
                connection.setRequestProperty("User-Agent", "PutraJaya-Android")
                connection.connect()
                val html = connection.inputStream.bufferedReader(Charsets.UTF_8).use { it.readText() }
                connection.disconnect()
                runOnUiThread {
                    web.loadDataWithBaseURL(
                        // FIX: baseURL sekarang menunjuk LANGSUNG ke file HTML-nya,
                        // sama seperti fileUrl di atas. Ini penting karena WebView
                        // memakai baseURL ini saat auto-reload (misalnya saat
                        // gesture "pull to refresh"). Kalau baseURL cuma folder
                        // (".../main/") tanpa nama file, GitHub raw balikin 404.
                        fileUrl,
                        html,
                        "text/html",
                        "UTF-8",
                        fileUrl
                    )
                }
            } catch (e: Exception) {
                runOnUiThread {
                    web.loadDataWithBaseURL(
                        null,
                        """
                        <!DOCTYPE html>
                        <html>
                        <head>
                        <meta name="viewport" content="width=device-width, initial-scale=1.0">
                        <style>
                            * { margin:0; padding:0; box-sizing:border-box; }
                            body {
                                font-family: sans-serif;
                                background: linear-gradient(180deg, #0d2a52 0%, #07172e 100%);
                                height: 100vh;
                                display: flex;
                                align-items: center;
                                justify-content: center;
                            }
                            .card { text-align: center; padding: 32px 28px; max-width: 320px; }
                            .icon-wrap {
                                width: 88px; height: 88px; margin: 0 auto 24px;
                                border-radius: 50%; background: rgba(255,255,255,0.08);
                                display: flex; align-items: center; justify-content: center;
                            }
                            .icon-wrap svg { width: 44px; height: 44px; }
                            h1 { color: #ffffff; font-size: 20px; font-weight: 600; margin-bottom: 10px; }
                            p { color: #a9bad6; font-size: 14px; line-height: 1.5; margin-bottom: 28px; }
                            button {
                                background: #ffffff; color: #0d2a52; border: none;
                                padding: 13px 32px; border-radius: 24px;
                                font-size: 15px; font-weight: 600;
                            }
                            button:active { opacity: 0.8; }
                            .brand { margin-top: 40px; color: #5b6f8f; font-size: 12px; letter-spacing: 1px; }
                        </style>
                        </head>
                        <body>
                            <div class="card">
                                <div class="icon-wrap">
                                    <svg viewBox="0 0 24 24" fill="none" stroke="#ffffff" stroke-width="1.6" stroke-linecap="round" stroke-linejoin="round">
                                        <path d="M12 18h.01"/>
                                        <path d="M9.17 15.17a5 5 0 0 1 5.66 0"/>
                                        <path d="M6.34 12.34a9 9 0 0 1 11.32 0"/>
                                        <path d="M3.51 9.51a13 13 0 0 1 16.98 0"/>
                                        <line x1="2" y1="2" x2="22" y2="22"/>
                                    </svg>
                                </div>
                                <h1>Tidak ada koneksi</h1>
                                <p>Periksa jaringan WiFi atau data seluler Anda, lalu coba lagi.</p>
                                <button onclick="Android.retry()">Coba Lagi</button>
                                <div class="brand">PUTRAJAYA 2</div>
                            </div>
                        </body>
                        </html>
                        """.trimIndent(),
                        "text/html",
                        "UTF-8",
                        null
                    )
                    web.addJavascriptInterface(object {
                        @android.webkit.JavascriptInterface
                        fun retry() {
                            runOnUiThread {
                                showLoadingScreen()
                                loadAppHtml()
                            }
                        }
                    }, "Android")
                }
            }
        }.start()
    }

    @Deprecated("Deprecated in Android API 33")
    override fun onBackPressed() {
        if (web.canGoBack()) web.goBack() else super.onBackPressed()
    }
}
