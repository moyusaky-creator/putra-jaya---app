package com.putrajaya.app;

import android.app.Activity;
import android.os.Bundle;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;

public class MainActivity extends Activity {
  @Override public void onCreate(Bundle b){ super.onCreate(b);
    WebView w=new WebView(this); setContentView(w);
    WebSettings s=w.getSettings(); s.setJavaScriptEnabled(true); s.setDomStorageEnabled(true); s.setDatabaseEnabled(true); s.setAllowFileAccess(true); s.setSupportZoom(false);
    w.setWebViewClient(new WebViewClient());
    w.loadUrl("https://moyusaky-creator.github.io/putra-jaya---app/PUTRAJAYA_2_ONLINE.html");
  }
  @Override public void onBackPressed(){ WebView w=(WebView)findViewById(android.R.id.content); super.onBackPressed(); }
}
