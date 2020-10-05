package com.smurf.app;

import android.app.Activity;
import android.os.Bundle;
import android.util.Log;

import androidx.annotation.Nullable;

import com.smurf.app.webView.X5WebView;
import com.tencent.smtt.export.external.interfaces.ConsoleMessage;
import com.tencent.smtt.sdk.WebChromeClient;

/**
 * 签约&& 认证
 */

public class SignUpActivity extends Activity {
    private X5WebView mWebView;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.layout_webview);
        String signUrl = getIntent().getStringExtra("sign_url");
        mWebView = (X5WebView) findViewById(R.id.webview);
        mWebView.setWebChromeClient(new WebChromeClient() {
            @Override
            public boolean onConsoleMessage(ConsoleMessage consoleMessage) {
                Log.i("consoleMessage", consoleMessage.message());
                return super.onConsoleMessage(consoleMessage);
            }
        });
        mWebView.getSettings();
        mWebView.loadUrl(signUrl);
    }
}
