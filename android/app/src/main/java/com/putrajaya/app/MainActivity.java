package com.putrajaya.app;

import android.app.Activity;
import android.os.Bundle;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.webkit.WebChromeClient;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

public class MainActivity extends Activity {
    private WebView webView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        webView = new WebView(this);
        setContentView(webView);

        WebSettings settings = webView.getSettings();
        settings.setJavaScriptEnabled(true);
        settings.setDomStorageEnabled(true);
        settings.setDatabaseEnabled(true);
        settings.setAllowFileAccess(true);
        settings.setAllowContentAccess(true);
        settings.setSupportZoom(false);
        settings.setBuiltInZoomControls(false);
        settings.setDisplayZoomControls(false);
        settings.setMixedContentMode(WebSettings.MIXED_CONTENT_COMPATIBILITY_MODE);
        webView.setWebChromeClient(new WebChromeClient());
        webView.setWebViewClient(new WebViewClient());
        loadAppHtml();
    }

    private void loadAppHtml() {
        final String url = "https://raw.githubusercontent.com/moyusaky-creator/putra-jaya---app/main/PUTRAJAYA_2_ONLINE.html";
        new Thread(() -> {
            try {
                HttpURLConnection connection = (HttpURLConnection) new URL(url).openConnection();
                connection.setConnectTimeout(15000);
                connection.setReadTimeout(20000);
                connection.setRequestMethod("GET");
                connection.setRequestProperty("User-Agent", "PutraJaya-Android");
                connection.connect();

                BufferedReader reader = new BufferedReader(
                        new InputStreamReader(connection.getInputStream(), "UTF-8"));
                StringBuilder html = new StringBuilder();
                String line;
                while ((line = reader.readLine()) != null) html.append(line).append('\n');
                reader.close();
                connection.disconnect();

                runOnUiThread(() -> webView.loadDataWithBaseURL(
                        "https://raw.githubusercontent.com/moyusaky-creator/putra-jaya---app/main/",
                        html.toString(),
                        "text/html",
                        "UTF-8",
                        "https://github.com/moyusaky-creator/putra-jaya---app"
                ));
            } catch (Exception e) {
                runOnUiThread(() -> webView.loadData(
                        "<html><body style='font-family:sans-serif;padding:24px'><h2>PUTRAJAYA 2</h2><p>Gagal memuat aplikasi. Periksa koneksi internet lalu coba lagi.</p><button onclick='location.reload()'>Coba Lagi</button></body></html>",
                        "text/html",
                        "UTF-8"
                ));
            }
        }).start();
    }

    @Override
    public void onBackPressed() {
        if (webView != null && webView.canGoBack()) webView.goBack();
        else super.onBackPressed();
    }

    @Override
    protected void onDestroy() {
        if (webView != null) webView.destroy();
        super.onDestroy();
    }
}
