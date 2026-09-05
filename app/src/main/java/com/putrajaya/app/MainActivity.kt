package com.putrajaya.app

import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.webkit.JavascriptInterface
import android.webkit.ValueCallback
import android.webkit.WebChromeClient
import android.webkit.WebResourceRequest
import android.webkit.WebSettings
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import java.io.File
import java.net.HttpURLConnection
import java.net.URL

class MainActivity : Activity() {
    private lateinit var web: WebView

    // URL file HTML asli di GitHub raw
    private val fileUrl =
        "https://raw.githubusercontent.com/moyusaky-creator/putra-jaya---app/main/PUTRAJAYA_2_ONLINE.html"

    // --- Untuk upload foto (kamera / galeri) ---
    private var fileUploadCallback: ValueCallback<Array<Uri>>? = null
    private var cameraPhotoUri: Uri? = null
    private val FILE_CHOOSER_REQUEST_CODE = 5173
    private val CAMERA_PERMISSION_REQUEST_CODE = 5174

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

        // FIX: WebChromeClient custom supaya tombol upload (<input type="file">)
        // di halaman HTML bisa munculin pilihan "Kamera" / "Galeri".
        // Tanpa onShowFileChooser ini, tombol upload tidak akan merespons sama sekali.
        web.webChromeClient = object : WebChromeClient() {
            override fun onShowFileChooser(
                webView: WebView,
                filePathCallback: ValueCallback<Array<Uri>>,
                fileChooserParams: FileChooserParams
            ): Boolean {
                fileUploadCallback?.onReceiveValue(null)
                fileUploadCallback = filePathCallback
                openFileChooser()
                return true
            }
        }

        // WebViewClient custom: mencegat navigasi ke fileUrl saat pull-to-refresh
        // (supaya tidak load mentah dari GitHub raw dengan Content-Type: text/plain)
        web.webViewClient = object : WebViewClient() {
            override fun shouldOverrideUrlLoading(
                view: WebView,
                request: WebResourceRequest
            ): Boolean {
                val requestedUrl = request.url.toString()
                return if (requestedUrl == fileUrl) {
                    showLoadingScreen()
                    loadAppHtml()
                    true
                } else {
                    false
                }
            }
        }

        setContentView(web)
        showLoadingScreen()
        loadAppHtml()
    }

    // ---------- Bagian upload foto (kamera / galeri) ----------

    private fun openFileChooser() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA)
            != PackageManager.PERMISSION_GRANTED
        ) {
            ActivityCompat.requestPermissions(
                this,
                arrayOf(Manifest.permission.CAMERA),
                CAMERA_PERMISSION_REQUEST_CODE
            )
            // Lanjut tetap buka chooser; kalau izin kamera ditolak,
            // pengguna masih bisa pilih dari Galeri.
        }
        launchChooserIntent()
    }

    private fun launchChooserIntent() {
        // Intent buat ambil foto lewat kamera
        var cameraIntent: Intent? = null
        try {
            val photoFile = File.createTempFile(
                "PUTRAJAYA_${System.currentTimeMillis()}_",
                ".jpg",
                cacheDir
            )
            cameraPhotoUri = FileProvider.getUriForFile(
                this,
                "$packageName.fileprovider",
                photoFile
            )
            cameraIntent = Intent(MediaStore.ACTION_IMAGE_CAPTURE).apply {
                putExtra(MediaStore.EXTRA_OUTPUT, cameraPhotoUri)
                addFlags(Intent.FLAG_GRANT_WRITE_URI_PERMISSION)
            }
        } catch (e: Exception) {
            cameraPhotoUri = null
        }

        // Intent buat pilih dari Galeri
        val galleryIntent = Intent(Intent.ACTION_GET_CONTENT).apply {
            type = "image/*"
            addCategory(Intent.CATEGORY_OPENABLE)
        }

        val chooserIntent = Intent(Intent.ACTION_CHOOSER).apply {
            putExtra(Intent.EXTRA_INTENT, galleryIntent)
            putExtra(Intent.EXTRA_TITLE, "Ambil foto atau pilih dari galeri")
            if (cameraIntent != null) {
                putExtra(Intent.EXTRA_INITIAL_INTENTS, arrayOf(cameraIntent))
            }
        }

        startActivityForResult(chooserIntent, FILE_CHOOSER_REQUEST_CODE)
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        // Tidak perlu aksi khusus di sini — hasil izin akan dipakai
        // otomatis saat pengguna memilih opsi "Kamera" di chooser.
    }

    @Deprecated("Deprecated in Android API 30")
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode != FILE_CHOOSER_REQUEST_CODE) return
        if (fileUploadCallback == null) return

        var results: Array<Uri>? = null
        if (resultCode == Activity.RESULT_OK) {
            if (data != null && data.dataString != null) {
                // Dipilih dari Galeri
                results = arrayOf(Uri.parse(data.dataString))
            } else if (cameraPhotoUri != null) {
                // Difoto lewat Kamera
                results = arrayOf(cameraPhotoUri!!)
            }
        }
        fileUploadCallback?.onReceiveValue(results)
        fileUploadCallback = null
        cameraPhotoUri = null
    }

    // ---------- Bagian load HTML (sudah ada sebelumnya) ----------

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
                        @JavascriptInterface
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
