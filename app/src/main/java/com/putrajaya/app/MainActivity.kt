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
        loadAppHtml()
    }

    private fun loadAppHtml() {
        val url = "https://raw.githubusercontent.com/moyusaky-creator/putra-jaya---app/main/PUTRAJAYA_2_ONLINE.html"
        Thread {
            try {
                val connection = URL(url).openConnection() as HttpURLConnection
                connection.connectTimeout = 15000
                connection.readTimeout = 20000
                connection.requestMethod = "GET"
                connection.setRequestProperty("User-Agent", "PutraJaya-Android")
                connection.connect()
                val html = connection.inputStream.bufferedReader(Charsets.UTF_8).use { it.readText() }
                connection.disconnect()
                runOnUiThread {
                    web.loadDataWithBaseURL(
                        "https://raw.githubusercontent.com/moyusaky-creator/putra-jaya---app/main/",
                        html,
                        "text/html",
                        "UTF-8",
                        "https://github.com/moyusaky-creator/putra-jaya---app"
                    )
                }
            } catch (e: Exception) {
                runOnUiThread {
                    web.loadData(
                        "<html><body style='font-family:sans-serif;padding:24px'><h2>PUTRAJAYA 2</h2><p>Gagal memuat aplikasi. Periksa koneksi internet lalu coba lagi.</p><button onclick='location.reload()'>Coba Lagi</button></body></html>",
                        "text/html",
                        "UTF-8"
                    )
                }
            }
        }.start()
    }

    @Deprecated("Deprecated in Android API 33")
    override fun onBackPressed() {
        if (web.canGoBack()) web.goBack() else super.onBackPressed()
    }
}
