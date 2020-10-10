package com.smurf.app;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;

import androidx.annotation.Nullable;

import com.smurf.app.webView.X5WebView;
import com.tencent.smtt.export.external.interfaces.ConsoleMessage;
import com.tencent.smtt.sdk.WebChromeClient;

import java.util.HashMap;
import java.util.Map;

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

        if (signUrl.startsWith("weixin://") || signUrl.startsWith("alipays://")) {
            Intent intent = new Intent();
            intent.setAction(Intent.ACTION_VIEW);
            intent.setData(Uri.parse(signUrl));
            startActivity(intent);
            return;
        }

        if (signUrl.contains("https://wx.tenpay.com")) {
            Map<String, String> extraHeaders = new HashMap<>();
            extraHeaders.put("Referer", "http://smurf.langongbao.com");
            mWebView.loadUrl(signUrl, extraHeaders);
            return;
        }
        mWebView.loadUrl(signUrl);
    }
}
